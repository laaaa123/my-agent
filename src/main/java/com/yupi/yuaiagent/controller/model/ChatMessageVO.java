package com.yupi.yuaiagent.controller.model;

import java.time.LocalDateTime;

/**
 * 消息展示对象。
 */
public record ChatMessageVO(
        long id,
        String sessionId,
        int seqNo,
        String role,
        String content,
        LocalDateTime createdAt
) {
}
