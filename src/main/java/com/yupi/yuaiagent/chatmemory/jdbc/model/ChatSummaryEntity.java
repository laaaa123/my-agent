package com.yupi.yuaiagent.chatmemory.jdbc.model;

import java.time.LocalDateTime;

/**
 * 会话摘要实体。
 */
public record ChatSummaryEntity(
        String sessionId,
        String summaryText,
        int coveredSeq,
        LocalDateTime updatedAt
) {
}
