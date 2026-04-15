package com.yupi.yuaiagent.advisor;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MyLoggerAdvisorTest {

    @Test
    void adviseStreamShouldKeepOriginalChunks() {
        MyLoggerAdvisor advisor = new MyLoggerAdvisor();
        ChatClientRequest request = new ChatClientRequest(new Prompt("测试流式输出"), new HashMap<>());

        ChatClientResponse firstChunk = mock(ChatClientResponse.class);
        ChatClientResponse secondChunk = mock(ChatClientResponse.class);
        StreamAdvisorChain chain = mock(StreamAdvisorChain.class);

        when(chain.nextStream(request)).thenReturn(Flux.just(firstChunk, secondChunk));

        List<ChatClientResponse> responses = advisor.adviseStream(request, chain).collectList().block();

        assertEquals(List.of(firstChunk, secondChunk), responses);
    }
}
