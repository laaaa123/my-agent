package com.yupi.yuaiagent.chatmemory.jdbc.model;

import java.time.LocalDateTime;

/**
 * 聊天消息实体。
 */
public record ChatMessageEntity(
        long id,
        String sessionId,
        int seqNo,
        String role,
        String content,
        LocalDateTime createdAt
) {
}
