package com.yupi.yuaiagent.controller;

import com.yupi.yuaiagent.app.LoveApp;
import com.yupi.yuaiagent.app.manus.service.ManusAppService;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;

@RestController
@RequestMapping("/ai")
public class AiController {

    @Resource
    private LoveApp loveApp;

    @Resource
    private ManusAppService manusAppService;

    /**
     * 同步调用情感助手。
     *
     * @param message 用户消息
     * @param chatId 会话标识
     * @return 回复内容
     */
    @GetMapping({"/emotion_app/chat/sync", "/love_app/chat/sync"})
    public String doChatWithLoveAppSync(String message, String chatId) {
        return loveApp.doChat(message, chatId);
    }

    /**
     * 通过 Flux 方式流式调用情感助手。
     *
     * @param message 用户消息
     * @param chatId 会话标识
     * @return 流式回复
     */
    @GetMapping(value = {"/emotion_app/chat/sse", "/love_app/chat/sse"}, produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> doChatWithLoveAppSSE(String message, String chatId) {
        return loveApp.doChatByStream(message, chatId);
    }

    /**
     * 通过 ServerSentEvent 包装流式调用情感助手。
     *
     * @param message 用户消息
     * @param chatId 会话标识
     * @return 流式回复事件
     */
    @GetMapping(value = {"/emotion_app/chat/server_sent_event", "/love_app/chat/server_sent_event"})
    public Flux<ServerSentEvent<String>> doChatWithLoveAppServerSentEvent(String message, String chatId) {
        return loveApp.doChatByStream(message, chatId)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }

    /**
     * 通过 SseEmitter 方式流式调用情感助手。
     *
     * @param message 用户消息
     * @param chatId 会话标识
     * @return SSE 输出
     */
    @GetMapping(value = {"/emotion_app/chat/sse_emitter", "/love_app/chat/sse_emitter"})
    public SseEmitter doChatWithLoveAppServerSseEmitter(String message, String chatId) {
        SseEmitter sseEmitter = new SseEmitter(180000L);
        loveApp.doChatByStream(message, chatId)
                .subscribe(chunk -> {
                    try {
                        sseEmitter.send(chunk);
                    } catch (IOException e) {
                        sseEmitter.completeWithError(e);
                    }
                }, sseEmitter::completeWithError, sseEmitter::complete);
        return sseEmitter;
    }

    /**
     * 流式调用智能体应用。
     *
     * @param message 用户消息
     * @param chatId 会话标识
     * @return SSE 输出
     */
    @GetMapping(value = "/manus/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter doChatWithManus(String message, String chatId) {
        return manusAppService.doChatByStream(message, chatId);
    }
}
