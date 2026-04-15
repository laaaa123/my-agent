package com.yupi.yuaiagent.chatmemory.jdbc.model;

import java.time.LocalDateTime;

/**
 * 会话实体。
 */
public record ChatSessionEntity(
        String sessionId,
        String title,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime lastMessageAt,
        int messageCount
) {
}
