package com.yupi.yuaiagent.config;

import com.yupi.yuaiagent.chatmemory.jdbc.HybridJdbcChatMemory;
import com.yupi.yuaiagent.chatmemory.jdbc.MemorySummaryService;
import com.yupi.yuaiagent.chatmemory.jdbc.repository.ChatMemoryJdbcRepository;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 对话记忆配置。
 */
@Configuration
public class ChatMemoryConfig {

    /**
     * 基于 MySQL 的混合记忆策略：
     * 摘要 + 最近 N 条消息。
     *
     * @param repository 聊天记忆仓储
     * @param summaryService 摘要服务
     * @return 聊天记忆组件
     */
    @Bean
    public ChatMemory chatMemory(ChatMemoryJdbcRepository repository, MemorySummaryService summaryService) {
        return new HybridJdbcChatMemory(
                repository,
                summaryService,
                16, // 最近上下文条数
                12, // 触发摘要阈值
                40  // 每次摘要处理上限
        );
    }
}
