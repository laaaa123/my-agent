package com.yupi.yuaiagent.chatmemory.service;

import com.yupi.yuaiagent.chatmemory.jdbc.model.ChatMessageEntity;
import com.yupi.yuaiagent.chatmemory.jdbc.model.ChatSessionEntity;
import com.yupi.yuaiagent.chatmemory.jdbc.repository.ChatMemoryJdbcRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Session management service.
 */
@Service
public class ChatSessionService {

    public static final String ASSISTANT_TYPE_LOVE = "love";
    public static final String ASSISTANT_TYPE_MANUS = "manus";

    private final ChatMemoryJdbcRepository repository;

    public ChatSessionService(ChatMemoryJdbcRepository repository) {
        this.repository = repository;
    }

    public String createSession(String title) {
        return createSession(ASSISTANT_TYPE_LOVE, title);
    }

    public String createSession(String assistantType, String title) {
        String sessionId = resolvePrefix(assistantType) + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String resolvedTitle = resolveTitle(title);
        LocalDateTime now = LocalDateTime.now();
        repository.createSessionIfAbsent(sessionId, resolvedTitle, now);
        return sessionId;
    }

    public List<ChatSessionEntity> listSessions(int limit) {
        return listSessions(null, limit);
    }

    public List<ChatSessionEntity> listSessions(String assistantType, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 200));
        String prefix = resolvePrefixOrNull(assistantType);
        if (prefix == null) {
            return repository.listSessions(safeLimit);
        }
        return repository.listSessionsByPrefix(prefix, safeLimit);
    }

    public List<ChatMessageEntity> listMessages(String sessionId, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 1000));
        return repository.findAllMessages(sessionId, safeLimit);
    }

    public void deleteSession(String sessionId) {
        repository.deleteSession(sessionId);
    }

    private String resolveTitle(String title) {
        if (title == null || title.isBlank()) {
            return "新会话";
        }
        String trimmed = title.trim();
        return trimmed.length() > 40 ? trimmed.substring(0, 40) : trimmed;
    }

    private String resolvePrefix(String assistantType) {
        String normalized = normalizeAssistantType(assistantType);
        if (normalized == null || ASSISTANT_TYPE_LOVE.equals(normalized)) {
            return "love_";
        }
        if (ASSISTANT_TYPE_MANUS.equals(normalized)) {
            return "manus_";
        }
        return "love_";
    }

    private String resolvePrefixOrNull(String assistantType) {
        String normalized = normalizeAssistantType(assistantType);
        if (normalized == null) {
            return null;
        }
        return resolvePrefix(normalized);
    }

    private String normalizeAssistantType(String assistantType) {
        if (assistantType == null || assistantType.isBlank()) {
            return null;
        }
        String normalized = assistantType.trim().toLowerCase();
        if (ASSISTANT_TYPE_MANUS.equals(normalized)) {
            return ASSISTANT_TYPE_MANUS;
        }
        return ASSISTANT_TYPE_LOVE;
    }
}
