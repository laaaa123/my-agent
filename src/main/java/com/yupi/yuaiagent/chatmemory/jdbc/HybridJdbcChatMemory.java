package com.yupi.yuaiagent.chatmemory.jdbc;

import com.yupi.yuaiagent.chatmemory.jdbc.model.ChatMessageEntity;
import com.yupi.yuaiagent.chatmemory.jdbc.model.ChatSummaryEntity;
import com.yupi.yuaiagent.chatmemory.jdbc.repository.ChatMemoryJdbcRepository;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * 基于 MySQL 的混合记忆实现：摘要 + 最近 N 条消息。
 */
public class HybridJdbcChatMemory implements ChatMemory {

    private static final String DEFAULT_TITLE = "新会话";
    private static final String SUMMARY_SYSTEM_PREFIX = "以下是该会话的历史摘要，请结合后续对话继续回答：";

    private final ChatMemoryJdbcRepository repository;
    private final MemorySummaryService summaryService;
    private final int recentMessageLimit;
    private final int summarizeTriggerCount;
    private final int summarizeBatchLimit;

    public HybridJdbcChatMemory(ChatMemoryJdbcRepository repository,
                                MemorySummaryService summaryService,
                                int recentMessageLimit,
                                int summarizeTriggerCount,
                                int summarizeBatchLimit) {
        this.repository = repository;
        this.summaryService = summaryService;
        this.recentMessageLimit = Math.max(2, recentMessageLimit);
        this.summarizeTriggerCount = Math.max(4, summarizeTriggerCount);
        this.summarizeBatchLimit = Math.max(8, summarizeBatchLimit);
    }

    @Override
    public void add(String conversationId, List<Message> messages) {
        if (conversationId == null || conversationId.isBlank() || messages == null || messages.isEmpty()) {
            return;
        }
        String sessionId = conversationId.trim();
        LocalDateTime now = LocalDateTime.now();
        repository.createSessionIfAbsent(sessionId, deriveTitle(messages), now);

        int seq = repository.findMaxSeq(sessionId);
        int addedCount = 0;
        for (Message message : messages) {
            if (message == null || message.getText() == null || message.getText().isBlank()) {
                continue;
            }
            seq++;
            repository.insertMessage(sessionId, seq, normalizeRole(message), message.getText().trim(), now);
            addedCount++;
        }
        if (addedCount == 0) {
            return;
        }
        repository.touchSessionOnMessage(sessionId, now, addedCount);
        compactSummaryIfNeeded(sessionId, now);
    }

    public List<Message> get(String conversationId) {
        return get(conversationId, recentMessageLimit);
    }

    public List<Message> get(String conversationId, int lastN) {
        if (conversationId == null || conversationId.isBlank()) {
            return List.of();
        }
        String sessionId = conversationId.trim();
        int limit = lastN <= 0 ? recentMessageLimit : lastN;

        List<Message> result = new ArrayList<>();
        Optional<ChatSummaryEntity> summaryOpt = repository.findSummary(sessionId);
        summaryOpt.ifPresent(summary -> {
            if (summary.summaryText() != null && !summary.summaryText().isBlank()) {
                result.add(new SystemMessage(SUMMARY_SYSTEM_PREFIX + "\n" + summary.summaryText().trim()));
            }
        });

        List<ChatMessageEntity> recentMessages = repository.findRecentMessages(sessionId, limit);
        for (ChatMessageEntity entity : recentMessages) {
            result.add(toMessage(entity));
        }
        return result;
    }

    @Override
    public void clear(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            return;
        }
        repository.deleteSession(conversationId.trim());
    }

    private void compactSummaryIfNeeded(String sessionId, LocalDateTime now) {
        Optional<ChatSummaryEntity> summaryOpt = repository.findSummary(sessionId);
        int coveredSeq = summaryOpt.map(ChatSummaryEntity::coveredSeq).orElse(0);

        List<ChatMessageEntity> pendingMessages = repository.findMessagesAfterSeq(sessionId, coveredSeq, summarizeBatchLimit);
        if (pendingMessages.size() < summarizeTriggerCount) {
            return;
        }
        String previousSummary = summaryOpt.map(ChatSummaryEntity::summaryText).orElse("");
        String newSummary = summaryService.summarize(previousSummary, pendingMessages);
        int newCoveredSeq = pendingMessages.get(pendingMessages.size() - 1).seqNo();
        repository.upsertSummary(sessionId, newSummary, newCoveredSeq, now);
    }

    private Message toMessage(ChatMessageEntity entity) {
        String role = entity.role() == null ? "" : entity.role().trim().toLowerCase(Locale.ROOT);
        return switch (role) {
            case "user" -> new UserMessage(entity.content());
            case "system" -> new SystemMessage(entity.content());
            default -> new AssistantMessage(entity.content());
        };
    }

    private String deriveTitle(List<Message> messages) {
        for (Message message : messages) {
            if (message == null || message.getText() == null) {
                continue;
            }
            if (message.getMessageType() == MessageType.USER) {
                String text = message.getText().trim();
                if (!text.isEmpty()) {
                    return text.length() > 20 ? text.substring(0, 20) : text;
                }
            }
        }
        return DEFAULT_TITLE;
    }

    private String normalizeRole(Message message) {
        if (message == null || message.getMessageType() == null) {
            return "assistant";
        }
        return message.getMessageType().name().toLowerCase(Locale.ROOT);
    }
}
