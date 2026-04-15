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

import java.util.List;
import java.util.stream.Collectors;

/**
 * 工具调用代理基类
 */
public class ToolCallAgent extends ReActAgent {

    private static final Logger log = LoggerFactory.getLogger(ToolCallAgent.class);

    private final ToolCallback[] availableTools;

    private ChatResponse toolCallChatResponse;

    private final ToolCallingManager toolCallingManager;

    private final ChatOptions chatOptions;

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
            UserMessage userMessage = new UserMessage(getNextStepPrompt());
            getMessageList().add(userMessage);
        }
        List<Message> messageList = getMessageList();
        Prompt prompt = new Prompt(messageList, this.chatOptions);
        try {
            ChatResponse chatResponse = getChatClient().prompt(prompt)
                    .system(getSystemPrompt())
                    .tools(availableTools)
                    .call()
                    .chatResponse();
            this.toolCallChatResponse = chatResponse;
            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
            List<AssistantMessage.ToolCall> toolCallList = assistantMessage.getToolCalls();
            log.info("{} think result: {}", getName(), assistantMessage.getText());
            log.info("{} selected {} tools", getName(), toolCallList.size());
            String toolCallInfo = toolCallList.stream()
                    .map(toolCall -> String.format("tool=%s, args=%s", toolCall.name(), toolCall.arguments()))
                    .collect(Collectors.joining("\n"));
            if (!toolCallInfo.isBlank()) {
                log.info(toolCallInfo);
            }
            if (toolCallList.isEmpty()) {
                getMessageList().add(assistantMessage);
                return false;
            }
            return true;
        } catch (Exception e) {
            log.error("{} think failed", getName(), e);
            getMessageList().add(new AssistantMessage("Tool planning failed: " + e.getMessage()));
            return false;
        }
    }

    @Override
    public String act() {
        if (!toolCallChatResponse.hasToolCalls()) {
            return "No tool call is required";
        }
        Prompt prompt = new Prompt(getMessageList(), this.chatOptions);
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, toolCallChatResponse);
        setMessageList(toolExecutionResult.conversationHistory());
        ToolResponseMessage toolResponseMessage =
                (ToolResponseMessage) CollUtil.getLast(toolExecutionResult.conversationHistory());
        boolean terminateToolCalled = toolResponseMessage.getResponses().stream()
                .anyMatch(response -> response.name().equals("doTerminate"));
        if (terminateToolCalled) {
            setState(AgentState.FINISHED);
        }
        String results = toolResponseMessage.getResponses().stream()
                .map(response -> "tool=" + response.name() + ", result=" + response.responseData())
                .collect(Collectors.joining("\n"));
        log.info(results);
        return results;
    }
}
