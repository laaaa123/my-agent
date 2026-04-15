package com.yupi.yuaiagent.controller.model;

import java.time.LocalDateTime;

/**
 * 会话展示对象。
 */
public record ChatSessionVO(
        String sessionId,
        String title,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        LocalDateTime lastMessageAt,
        int messageCount
) {
}
