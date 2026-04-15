package com.yupi.yuaiagent.chatmemory.jdbc;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 聊天记忆表结构初始化器。
 */
@Component
public class ChatMemorySchemaInitializer {

    private final JdbcTemplate jdbcTemplate;

    public ChatMemorySchemaInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void initSchema() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS chat_session (
                    session_id VARCHAR(64) PRIMARY KEY,
                    title VARCHAR(128) NOT NULL,
                    created_at DATETIME NOT NULL,
                    updated_at DATETIME NOT NULL,
                    last_message_at DATETIME NULL,
                    message_count INT NOT NULL DEFAULT 0
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                """);

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS chat_message (
                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    session_id VARCHAR(64) NOT NULL,
                    seq_no INT NOT NULL,
                    role VARCHAR(32) NOT NULL,
                    content MEDIUMTEXT NOT NULL,
                    created_at DATETIME NOT NULL,
                    INDEX idx_chat_message_session_seq (session_id, seq_no),
                    INDEX idx_chat_message_session_created (session_id, created_at),
                    CONSTRAINT fk_chat_message_session
                        FOREIGN KEY (session_id) REFERENCES chat_session(session_id)
                        ON DELETE CASCADE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                """);

        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS chat_summary (
                    session_id VARCHAR(64) PRIMARY KEY,
                    summary_text MEDIUMTEXT NOT NULL,
                    covered_seq INT NOT NULL DEFAULT 0,
                    updated_at DATETIME NOT NULL,
                    CONSTRAINT fk_chat_summary_session
                        FOREIGN KEY (session_id) REFERENCES chat_session(session_id)
                        ON DELETE CASCADE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
                """);
    }
}
