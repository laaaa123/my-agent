package com.yupi.yuaiagent.controller;

import com.yupi.yuaiagent.chatmemory.jdbc.model.ChatMessageEntity;
import com.yupi.yuaiagent.chatmemory.jdbc.model.ChatSessionEntity;
import com.yupi.yuaiagent.chatmemory.service.ChatSessionService;
import com.yupi.yuaiagent.controller.model.ChatMessageVO;
import com.yupi.yuaiagent.controller.model.ChatSessionCreateRequest;
import com.yupi.yuaiagent.controller.model.ChatSessionVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 会话管理接口。
 */
@RestController
@RequestMapping("/chat-sessions")
public class ChatSessionController {

    @Resource
    private ChatSessionService chatSessionService;

    @GetMapping
    public List<ChatSessionVO> listSessions(@RequestParam(defaultValue = "50") int limit) {
        List<ChatSessionEntity> sessions = chatSessionService.listSessions(limit);
        return sessions.stream()
                .map(session -> new ChatSessionVO(
                        session.sessionId(),
                        session.title(),
                        session.createdAt(),
                        session.updatedAt(),
                        session.lastMessageAt(),
                        session.messageCount()
                ))
                .toList();
    }

    @PostMapping
    public Map<String, String> createSession(@RequestBody(required = false) ChatSessionCreateRequest request) {
        String title = request == null ? null : request.title();
        String sessionId = chatSessionService.createSession(title);
        return Map.of("sessionId", sessionId);
    }

    @GetMapping("/{sessionId}/messages")
    public List<ChatMessageVO> listMessages(@PathVariable String sessionId,
                                            @RequestParam(defaultValue = "300") int limit) {
        List<ChatMessageEntity> messages = chatSessionService.listMessages(sessionId, limit);
        return messages.stream()
                .map(message -> new ChatMessageVO(
                        message.id(),
                        message.sessionId(),
                        message.seqNo(),
                        message.role(),
                        message.content(),
                        message.createdAt()
                ))
                .toList();
    }

    @DeleteMapping("/{sessionId}")
    public Map<String, Boolean> deleteSession(@PathVariable String sessionId) {
        chatSessionService.deleteSession(sessionId);
        return Map.of("success", true);
    }
}
