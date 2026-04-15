package com.yupi.yuaiagent.advisor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import reactor.core.publisher.Flux;

/**
 * 自定义日志 Advisor。
 * 同步调用仅记录请求与摘要信息，流式调用只记录生命周期，
 * 避免为了打印完整响应而把流式结果聚合成一次性输出。
 */
public class MyLoggerAdvisor implements CallAdvisor, StreamAdvisor {

    private static final Logger log = LoggerFactory.getLogger(MyLoggerAdvisor.class);

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return 0;
    }

    private ChatClientRequest logBefore(ChatClientRequest request) {
        log.info("AI 请求: {}", request.prompt());
        return request;
    }

    private void logAfterCall(ChatClientResponse response) {
        String text = extractText(response);
        log.info("AI 调用完成，响应长度={}", text.length());
    }

    private String extractText(ChatClientResponse response) {
        if (response == null || response.chatResponse() == null || response.chatResponse().getResult() == null
                || response.chatResponse().getResult().getOutput() == null) {
            return "";
        }
        String text = response.chatResponse().getResult().getOutput().getText();
        return text == null ? "" : text;
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain chain) {
        ChatClientRequest request = logBefore(chatClientRequest);
        ChatClientResponse response = chain.nextCall(request);
        logAfterCall(response);
        return response;
    }

    @Override
    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain chain) {
        ChatClientRequest request = logBefore(chatClientRequest);
        return chain.nextStream(request)
                .doOnSubscribe(subscription -> log.info("AI 流式响应开始"))
                .doOnError(error -> log.warn("AI 流式响应异常: {}", error.getMessage(), error))
                .doOnComplete(() -> log.info("AI 流式响应结束"));
    }
}
