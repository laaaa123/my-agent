package com.yupi.yuaiagent.app.router.classifier;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.yupi.yuaiagent.app.prompt.EmotionalAssistantPrompts;
import com.yupi.yuaiagent.app.router.IntentType;
import com.yupi.yuaiagent.app.router.model.IntentRoutingResult;
import com.yupi.yuaiagent.app.router.model.LlmIntentResult;
import com.yupi.yuaiagent.app.router.service.ConversationHistoryService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * 大模型意图分类器
 */
@Component
public class LlmIntentClassifier {

    private static final Logger log = LoggerFactory.getLogger(LlmIntentClassifier.class);

    private final ChatClient chatClient;

    @Resource
    private ConversationHistoryService conversationHistoryService;

    public LlmIntentClassifier(ChatModel dashscopeChatModel) {
        this.chatClient = ChatClient.builder(dashscopeChatModel).build();
    }

    /**
     * 使用大模型进行意图分类
     *
     * @param query 用户最新问题
     * @param chatId 会话 id
     * @return 意图路由结果
     */
    public IntentRoutingResult classify(String query, String chatId) {
        String history = conversationHistoryService.getRecentHistory(chatId, 8);
        String prompt = """
                对话历史：
                %s

                用户最新消息：
                %s
                """.formatted(history, query);
        try {
            String response = chatClient.prompt()
                    .system(EmotionalAssistantPrompts.INTENT_CLASSIFIER_SYSTEM_PROMPT)
                    .user(prompt)
                    .call()
                    .content();
            LlmIntentResult llmIntentResult = parseResult(response);
            IntentType intentType = parseIntent(llmIntentResult.intent());
            double confidence = llmIntentResult.confidence() == null ? 0.70D : llmIntentResult.confidence();
            return new IntentRoutingResult(intentType, confidence, "llm");
        } catch (Exception e) {
            log.warn("大模型意图识别失败，降级走知识检索。query={}", query, e);
            return new IntentRoutingResult(IntentType.KNOWLEDGE, 0.50D, "fallback");
        }
    }

    private LlmIntentResult parseResult(String response) {
        JSONObject jsonObject = JSONUtil.parseObj(response);
        return new LlmIntentResult(
                jsonObject.getStr("intent"),
                jsonObject.getDouble("confidence")
        );
    }

    private IntentType parseIntent(String intent) {
        if (intent == null || intent.isBlank()) {
            return IntentType.KNOWLEDGE;
        }
        return switch (intent.trim().toLowerCase(Locale.ROOT)) {
            case "tool" -> IntentType.TOOL;
            case "chitchat" -> IntentType.CHITCHAT;
            case "clarification" -> IntentType.CLARIFICATION;
            default -> IntentType.KNOWLEDGE;
        };
    }
}
