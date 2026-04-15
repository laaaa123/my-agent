package com.yupi.yuaiagent.app.router.service;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

/**
 * 对话历史服务
 */
@Service
public class ConversationHistoryService {

    @Resource
    private ChatMemory chatMemory;

    /**
     * 获取最近几条对话历史
     *
     * @param chatId 会话 id
     * @param maxMessages 最大消息数
     * @return 格式化后的历史文本
     */
    public String getRecentHistory(String chatId, int maxMessages) {
        List<Message> messages = chatMemory.get(chatId);
        if (messages == null || messages.isEmpty()) {
            return "无";
        }
        int fromIndex = Math.max(0, messages.size() - maxMessages);
        StringBuilder historyBuilder = new StringBuilder();
        for (Message message : messages.subList(fromIndex, messages.size())) {
            String role = message.getMessageType().name().toLowerCase(Locale.ROOT);
            historyBuilder.append(role)
                    .append(": ")
                    .append(message.getText())
                    .append(System.lineSeparator());
        }
        return historyBuilder.toString().trim();
    }
}
