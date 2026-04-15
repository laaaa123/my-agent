package com.yupi.yuaiagent.chatmemory.service;

import com.yupi.yuaiagent.chatmemory.jdbc.model.ChatMessageEntity;
import com.yupi.yuaiagent.chatmemory.jdbc.model.ChatSessionEntity;
import com.yupi.yuaiagent.chatmemory.jdbc.repository.ChatMemoryJdbcRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 会话管理服务。
 */
@Service
public class ChatSessionService {

    private final ChatMemoryJdbcRepository repository;

    public ChatSessionService(ChatMemoryJdbcRepository repository) {
        this.repository = repository;
    }

    public String createSession(String title) {
        String sessionId = "love_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String resolvedTitle = resolveTitle(title);
        LocalDateTime now = LocalDateTime.now();
        repository.createSessionIfAbsent(sessionId, resolvedTitle, now);
        return sessionId;
    }

    public List<ChatSessionEntity> listSessions(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 200));
        return repository.listSessions(safeLimit);
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
}
