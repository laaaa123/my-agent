package com.yupi.yuaiagent.app;

import com.yupi.yuaiagent.advisor.MyLoggerAdvisor;
import com.yupi.yuaiagent.app.formatter.AnswerFormattingService;
import com.yupi.yuaiagent.app.knowledge.KnowledgeRetrievalResult;
import com.yupi.yuaiagent.app.knowledge.KnowledgeRetrievalService;
import com.yupi.yuaiagent.app.prompt.EmotionalAssistantPrompts;
import com.yupi.yuaiagent.app.router.HybridIntentRouter;
import com.yupi.yuaiagent.app.router.model.IntentRoutingResult;
import com.yupi.yuaiagent.tools.routing.ToolRoutingDecision;
import com.yupi.yuaiagent.tools.routing.UnifiedToolRegistry;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * AI 情感助手应用编排层。
 */
@Component
public class LoveApp {

    private static final Logger log = LoggerFactory.getLogger(LoveApp.class);
    private static final String CHAT_MEMORY_CONVERSATION_ID_KEY = "chat_memory_conversation_id";

    private final ChatClient chatClient;

    @Resource
    private HybridIntentRouter hybridIntentRouter;

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ToolCallbackProvider toolCallbackProvider;

    @Resource
    private AnswerFormattingService answerFormattingService;

    @Resource
    private KnowledgeRetrievalService knowledgeRetrievalService;

    @Resource
    private UnifiedToolRegistry unifiedToolRegistry;

    public LoveApp(ChatModel dashscopeChatModel, ChatMemory chatMemory) {
        this.chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    /**
     * AI 情感助手同步对话。
     *
     * @param message 用户消息
     * @param chatId 会话 id
     * @return AI 回复
     */
    public String doChat(String message, String chatId) {
        IntentRoutingResult routingResult = hybridIntentRouter.route(message, chatId);
        log.info("同步对话路由结果：intent={}, confidence={}, source={}",
                routingResult.intent(), routingResult.confidence(), routingResult.source());
        return switch (routingResult.intent()) {
            case CHITCHAT -> doChatByCall(message, chatId, EmotionalAssistantPrompts.CHITCHAT_SYSTEM_PROMPT);
            case TOOL -> doToolChatByCall(message, chatId);
            case CLARIFICATION -> doClarificationByCall(message, chatId);
            case KNOWLEDGE -> doKnowledgeChatByCall(message, chatId);
        };
    }

    /**
     * AI 情感助手流式对话。
     *
     * @param message 用户消息
     * @param chatId 会话 id
     * @return 流式回复
     */
    public Flux<String> doChatByStream(String message, String chatId) {
        IntentRoutingResult routingResult = hybridIntentRouter.route(message, chatId);
        log.info("流式对话路由结果：intent={}, confidence={}, source={}",
                routingResult.intent(), routingResult.confidence(), routingResult.source());
        return switch (routingResult.intent()) {
            case CHITCHAT -> doChatByStream(message, chatId, EmotionalAssistantPrompts.CHITCHAT_SYSTEM_PROMPT);
            case TOOL -> doToolChatByStream(message, chatId);
            case CLARIFICATION -> doClarificationByStream(message, chatId);
            case KNOWLEDGE -> doKnowledgeChatByStream(message, chatId);
        };
    }

    /**
     * 情感分析报告。
     *
     * @param message 用户消息
     * @param chatId 会话 id
     * @return 结构化报告
     */
    public LoveReport doChatWithReport(String message, String chatId) {
        LoveReport loveReport = chatClient.prompt()
                .system(EmotionalAssistantPrompts.REPORT_SYSTEM_PROMPT)
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId))
                .advisors(new MyLoggerAdvisor())
                .call()
                .entity(LoveReport.class);
        log.info("情感分析报告：{}", loveReport);
        return loveReport;
    }

    /**
     * 显式走知识库问答。
     *
     * @param message 用户消息
     * @param chatId 会话 id
     * @return 回复内容
     */
    public String doChatWithRag(String message, String chatId) {
        return doKnowledgeChatByCall(message, chatId);
    }

    /**
     * 显式走工具调用。
     *
     * @param message 用户消息
     * @param chatId 会话 id
     * @return 回复内容
     */
    public String doChatWithTools(String message, String chatId) {
        return doToolChatByCall(message, chatId);
    }

    /**
     * 显式走 MCP 工具调用。
     *
     * @param message 用户消息
     * @param chatId 会话 id
     * @return 回复内容
     */
    public String doChatWithMcp(String message, String chatId) {
        ChatResponse chatResponse = chatClient.prompt()
                .system(EmotionalAssistantPrompts.TOOL_SYSTEM_PROMPT)
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId))
                .advisors(new MyLoggerAdvisor())
                .toolCallbacks(toolCallbackProvider)
                .call()
                .chatResponse();
        String content = formatAnswer(chatResponse.getResult().getOutput().getText());
        log.info("MCP 工具调用回复：{}", content);
        return content;
    }

    private String doKnowledgeChatByCall(String message, String chatId) {
        KnowledgeRetrievalResult retrievalResult = knowledgeRetrievalService.retrieve(message, chatId);
        String knowledgePrompt = EmotionalAssistantPrompts.buildKnowledgeUserPrompt(message, retrievalResult.context());
        ChatResponse chatResponse = chatClient.prompt()
                .system(EmotionalAssistantPrompts.KNOWLEDGE_SYSTEM_PROMPT)
                .user(knowledgePrompt)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId))
                .advisors(new MyLoggerAdvisor())
                .call()
                .chatResponse();
        String content = formatAnswer(chatResponse.getResult().getOutput().getText());
        log.info("知识库回复：contextDocs={}, sessionScoped={}",
                retrievalResult.documents().size(), retrievalResult.sessionScoped());
        return content;
    }

    private Flux<String> doKnowledgeChatByStream(String message, String chatId) {
        KnowledgeRetrievalResult retrievalResult = knowledgeRetrievalService.retrieve(message, chatId);
        String knowledgePrompt = EmotionalAssistantPrompts.buildKnowledgeUserPrompt(message, retrievalResult.context());
        log.info("知识库流式回复：contextDocs={}, sessionScoped={}",
                retrievalResult.documents().size(), retrievalResult.sessionScoped());
        return chatClient.prompt()
                .system(EmotionalAssistantPrompts.KNOWLEDGE_SYSTEM_PROMPT)
                .user(knowledgePrompt)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId))
                .advisors(new MyLoggerAdvisor())
                .stream()
                .content()
                .map(this::formatChunk);
    }

    private String doToolChatByCall(String message, String chatId) {
        ToolRoutingDecision routingDecision = unifiedToolRegistry.resolveForMessage(message);
        log.info("工具二级路由：capabilities={}, tools={}",
                routingDecision.capabilities(), routingDecision.toolNames());
        ChatResponse chatResponse = chatClient.prompt()
                .system(routingDecision.systemPrompt())
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId))
                .advisors(new MyLoggerAdvisor())
                .toolCallbacks(routingDecision.callbacks())
                .call()
                .chatResponse();
        String content = formatAnswer(chatResponse.getResult().getOutput().getText());
        log.info("工具调用回复：{}", content);
        return content;
    }

    private Flux<String> doToolChatByStream(String message, String chatId) {
        ToolRoutingDecision routingDecision = unifiedToolRegistry.resolveForMessage(message);
        log.info("工具二级路由：capabilities={}, tools={}",
                routingDecision.capabilities(), routingDecision.toolNames());
        return chatClient.prompt()
                .system(routingDecision.systemPrompt())
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId))
                .advisors(new MyLoggerAdvisor())
                .toolCallbacks(routingDecision.callbacks())
                .stream()
                .content()
                .map(this::formatChunk);
    }

    private String doClarificationByCall(String message, String chatId) {
        return doChatByCall(message, chatId, EmotionalAssistantPrompts.CLARIFICATION_SYSTEM_PROMPT);
    }

    private Flux<String> doClarificationByStream(String message, String chatId) {
        return doChatByStream(message, chatId, EmotionalAssistantPrompts.CLARIFICATION_SYSTEM_PROMPT);
    }

    private String doChatByCall(String message, String chatId, String systemPrompt) {
        ChatResponse chatResponse = chatClient.prompt()
                .system(systemPrompt)
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId))
                .advisors(new MyLoggerAdvisor())
                .call()
                .chatResponse();
        String content = formatAnswer(chatResponse.getResult().getOutput().getText());
        log.info("普通回复：{}", content);
        return content;
    }

    private Flux<String> doChatByStream(String message, String chatId, String systemPrompt) {
        return chatClient.prompt()
                .system(systemPrompt)
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId))
                .advisors(new MyLoggerAdvisor())
                .stream()
                .content()
                .map(this::formatChunk);
    }

    private String formatAnswer(String content) {
        return answerFormattingService.format(content);
    }

    private String formatChunk(String chunk) {
        if (chunk == null || chunk.isBlank()) {
            return chunk;
        }
        return chunk
                .replace("###", "")
                .replace("##", "")
                .replace("#", "")
                .replace("**", "")
                .replace("__", "")
                .replace("```", "")
                .replace("`", "");
    }

    public record LoveReport(String title, List<String> suggestions) {
    }
}

