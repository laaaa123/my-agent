package com.yupi.yuaiagent.chatmemory.jdbc;

import com.yupi.yuaiagent.chatmemory.jdbc.model.ChatMessageEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 会话摘要服务。
 */
@Service
public class MemorySummaryService {

    private static final Logger log = LoggerFactory.getLogger(MemorySummaryService.class);

    private final ChatClient chatClient;

    public MemorySummaryService(ChatModel dashscopeChatModel) {
        this.chatClient = ChatClient.builder(dashscopeChatModel).build();
    }

    /**
     * 基于历史摘要与新增消息，生成新的压缩摘要。
     *
     * @param previousSummary 历史摘要
     * @param messages        新增消息
     * @return 新摘要
     */
    public String summarize(String previousSummary, List<ChatMessageEntity> messages) {
        if (messages == null || messages.isEmpty()) {
            return previousSummary == null ? "" : previousSummary;
        }
        String incrementalText = messagesToPlainText(messages);
        try {
            String summary = chatClient.prompt()
                    .system("""
                            你是对话摘要助手，请将历史摘要和新增对话压缩为新的中文摘要。
                            要求：
                            1. 保留关键信息：人物、关系、目标、事件、偏好、禁忌、时间线。
                            2. 不要臆造，不确定就忽略。
                            3. 输出纯文本，不要 Markdown。
                            4. 长度控制在 220 字以内。
                            """)
                    .user("""
                            历史摘要：
                            %s

                            新增对话：
                            %s
                            """.formatted(safeText(previousSummary), incrementalText))
                    .call()
                    .content();
            if (summary == null || summary.isBlank()) {
                return fallbackSummary(previousSummary, incrementalText);
            }
            return summary.trim();
        } catch (Exception e) {
            log.warn("生成会话摘要失败，降级为拼接压缩: {}", e.getMessage());
            return fallbackSummary(previousSummary, incrementalText);
        }
    }

    private String messagesToPlainText(List<ChatMessageEntity> messages) {
        StringBuilder builder = new StringBuilder();
        for (ChatMessageEntity message : messages) {
            builder.append(message.role())
                    .append(": ")
                    .append(safeText(message.content()))
                    .append('\n');
        }
        return builder.toString().trim();
    }

    private String fallbackSummary(String previousSummary, String incrementalText) {
        String merged = (safeText(previousSummary) + "\n" + safeText(incrementalText)).trim();
        if (merged.length() <= 220) {
            return merged;
        }
        return merged.substring(merged.length() - 220);
    }

    private String safeText(String text) {
        if (text == null) {
            return "";
        }
        return text.trim();
    }
}
