package com.yupi.yuaiagent.agent;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.yupi.yuaiagent.agent.model.AgentState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Base class for tool-driven agents.
 */
public class ToolCallAgent extends ReActAgent {

    private static final Logger log = LoggerFactory.getLogger(ToolCallAgent.class);
    private static final String EXECUTION_FAILURE_REPLY = "抱歉，我在执行任务时遇到问题，暂时无法继续。";

    private final ToolCallback[] availableTools;

    private final ToolCallingManager toolCallingManager;

    private final ChatOptions chatOptions;

    private ChatResponse toolCallChatResponse;

    public ToolCallAgent(ToolCallback[] availableTools) {
        this.availableTools = availableTools;
        this.toolCallingManager = ToolCallingManager.builder().build();
        this.chatOptions = DashScopeChatOptions.builder()
                .withInternalToolExecutionEnabled(false)
                .build();
    }

    @Override
    public boolean think() {
        if (StrUtil.isNotBlank(getNextStepPrompt())) {
            getMessageList().add(new UserMessage(getNextStepPrompt()));
        }
        emitReasoningStep("正在分析当前任务");
        List<Message> messageList = getMessageList();
        try {
            ChatResponse chatResponse = getChatClient().prompt()
                    .system(getSystemPrompt())
                    .messages(messageList)
                    .options(this.chatOptions)
                    .toolCallbacks(availableTools)
                    .call()
                    .chatResponse();
            this.toolCallChatResponse = chatResponse;

            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
            List<AssistantMessage.ToolCall> toolCallList = assistantMessage.getToolCalls();

            log.info("{} think result: {}", getName(), assistantMessage.getText());
            log.info("{} selected {} tools", getName(), toolCallList.size());

            String summary = summarizeAssistantStep(assistantMessage.getText());
            if (StrUtil.isNotBlank(summary)) {
                emitReasoningStep(summary);
            }

            if (!toolCallList.isEmpty()) {
                toolCallList.stream()
                        .map(AssistantMessage.ToolCall::name)
                        .distinct()
                        .forEach(toolName -> emitToolStart(toolName, "正在" + describeTool(toolName)));
            }

            if (toolCallList.isEmpty()) {
                getMessageList().add(assistantMessage);
                setState(AgentState.FINISHED);
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("{} think failed", getName(), e);
            getMessageList().add(new AssistantMessage(EXECUTION_FAILURE_REPLY));
            setState(AgentState.FINISHED);
            return false;
        }
    }

    @Override
    public String act() {
        if (toolCallChatResponse == null || !toolCallChatResponse.hasToolCalls()) {
            return "";
        }
        emitReasoningStep("正在执行工具操作");
        Prompt prompt = new Prompt(getMessageList(), this.chatOptions);
        try {
            ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, toolCallChatResponse);
            setMessageList(toolExecutionResult.conversationHistory());

            ToolResponseMessage toolResponseMessage =
                    (ToolResponseMessage) CollUtil.getLast(toolExecutionResult.conversationHistory());

            toolResponseMessage.getResponses().forEach(response ->
                    emitToolDone(response.name(), "已完成" + describeTool(response.name())));

            boolean terminateToolCalled = toolResponseMessage.getResponses().stream()
                    .anyMatch(response -> response.name().equals("doTerminate"));
            if (terminateToolCalled) {
                setState(AgentState.FINISHED);
                return summarizeCurrentState();
            }
            return "";
        } catch (Exception e) {
            log.error("{} tool execution failed", getName(), e);
            getMessageList().add(new AssistantMessage(EXECUTION_FAILURE_REPLY));
            setState(AgentState.FINISHED);
            return EXECUTION_FAILURE_REPLY;
        }
    }

    private String summarizeCurrentState() {
        try {
            if (!hasActiveEmitter()) {
                String summary = getChatClient().prompt()
                        .system("""
                                你是一个任务执行型 AI 助手。
                                请基于完整对话、工具结果和上下文，直接给出最终答案。
                                不要暴露思考过程，不要再调用工具，输出简洁、清晰、可执行的中文答案。
                                """)
                        .messages(getMessageList())
                        .call()
                        .content();
                if (StrUtil.isBlank(summary)) {
                    return "任务已完成。";
                }
                getMessageList().add(new AssistantMessage(summary));
                return summary;
            }

            emitReasoningStep("正在整理最终答案");
            emitAnswerStart();

            Flux<String> summaryFlux = getChatClient().prompt()
                    .system("""
                            你是一个任务执行型 AI 助手。
                            请基于完整对话、工具结果和上下文，直接给出最终答案。
                            不要暴露思考过程，不要再调用工具，输出简洁、清晰、可执行的中文答案。
                            """)
                    .messages(getMessageList())
                    .stream()
                    .content();

            List<String> chunks = new ArrayList<>();
            for (String chunk : summaryFlux.toIterable()) {
                if (StrUtil.isBlank(chunk)) {
                    continue;
                }
                chunks.add(chunk);
                emitAnswer(chunk);
            }
            emitAnswerDone();

            String summary = String.join("", chunks);
            if (StrUtil.isBlank(summary)) {
                summary = "任务已完成。";
            }
            getMessageList().add(new AssistantMessage(summary));
            return "";
        } catch (Exception e) {
            log.error("{} failed to summarize final answer", getName(), e);
            return "任务已完成，但最终总结生成失败。";
        }
    }

    @Override
    protected String onMaxStepsReached() {
        return summarizeCurrentState();
    }

    private String summarizeAssistantStep(String text) {
        if (StrUtil.isBlank(text)) {
            return "";
        }
        String normalized = text.replace("\r", "")
                .replace("\n", " ")
                .replaceAll("\\s+", " ")
                .trim();
        int sentenceEnd = normalized.indexOf('。');
        if (sentenceEnd > 0) {
            return normalized.substring(0, sentenceEnd + 1);
        }
        int englishEnd = normalized.indexOf('.');
        if (englishEnd > 0) {
            return normalized.substring(0, englishEnd + 1);
        }
        return normalized.length() > 60 ? normalized.substring(0, 60) + "..." : normalized;
    }

    private String describeTool(String toolName) {
        String normalized = toolName == null ? "" : toolName.toLowerCase(Locale.ROOT);
        if (normalized.contains("maps_geo")) {
            return "定位地点";
        }
        if (normalized.contains("around_search")) {
            return "搜索附近地点";
        }
        if (normalized.contains("text_search")) {
            return "检索候选地点";
        }
        if (normalized.contains("search_detail")) {
            return "获取地点详情";
        }
        if (normalized.contains("direction_walking")) {
            return "生成步行路线";
        }
        if (normalized.contains("direction_transit")) {
            return "生成公交路线";
        }
        if (normalized.contains("direction_driving")) {
            return "生成驾车路线";
        }
        if (normalized.contains("distance")) {
            return "比较地点距离";
        }
        if (normalized.contains("searchweb") || normalized.contains("web_search")) {
            return "搜索网页资料";
        }
        if (normalized.contains("scrape")) {
            return "抓取网页内容";
        }
        if (normalized.contains("knowledge")) {
            return "检索知识库";
        }
        if (normalized.contains("pdf")) {
            return "生成 PDF";
        }
        return "调用工具";
    }
}
