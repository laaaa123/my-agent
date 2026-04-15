package com.yupi.yuaiagent.chatmemory.jdbc.repository;

import com.yupi.yuaiagent.chatmemory.jdbc.model.ChatMessageEntity;
import com.yupi.yuaiagent.chatmemory.jdbc.model.ChatSessionEntity;
import com.yupi.yuaiagent.chatmemory.jdbc.model.ChatSummaryEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 聊天记忆 JDBC 仓储。
 */
@Repository
public class ChatMemoryJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    public ChatMemoryJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean existsSession(String sessionId) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(1) FROM chat_session WHERE session_id = ?",
                Integer.class,
                sessionId
        );
        return count != null && count > 0;
    }

    public void createSessionIfAbsent(String sessionId, String title, LocalDateTime now) {
        jdbcTemplate.update("""
                INSERT INTO chat_session(session_id, title, created_at, updated_at, last_message_at, message_count)
                VALUES (?, ?, ?, ?, NULL, 0)
                ON DUPLICATE KEY UPDATE updated_at = VALUES(updated_at)
                """, sessionId, title, Timestamp.valueOf(now), Timestamp.valueOf(now));
    }

    public void touchSessionOnMessage(String sessionId, LocalDateTime now, int addedCount) {
        jdbcTemplate.update("""
                UPDATE chat_session
                SET updated_at = ?, last_message_at = ?, message_count = message_count + ?
                WHERE session_id = ?
                """, Timestamp.valueOf(now), Timestamp.valueOf(now), addedCount, sessionId);
    }

    public int findMaxSeq(String sessionId) {
        Integer maxSeq = jdbcTemplate.queryForObject(
                "SELECT COALESCE(MAX(seq_no), 0) FROM chat_message WHERE session_id = ?",
                Integer.class,
                sessionId
        );
        return maxSeq == null ? 0 : maxSeq;
    }

    public void insertMessage(String sessionId, int seqNo, String role, String content, LocalDateTime now) {
        jdbcTemplate.update("""
                INSERT INTO chat_message(session_id, seq_no, role, content, created_at)
                VALUES (?, ?, ?, ?, ?)
                """, sessionId, seqNo, role, content, Timestamp.valueOf(now));
    }

    public List<ChatMessageEntity> findMessagesAfterSeq(String sessionId, int afterSeq, int limit) {
        return jdbcTemplate.query("""
                        SELECT id, session_id, seq_no, role, content, created_at
                        FROM chat_message
                        WHERE session_id = ? AND seq_no > ?
                        ORDER BY seq_no ASC
                        LIMIT ?
                        """,
                (rs, rowNum) -> new ChatMessageEntity(
                        rs.getLong("id"),
                        rs.getString("session_id"),
                        rs.getInt("seq_no"),
                        rs.getString("role"),
                        rs.getString("content"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                ),
                sessionId, afterSeq, limit
        );
    }

    public List<ChatMessageEntity> findRecentMessages(String sessionId, int limit) {
        List<ChatMessageEntity> desc = jdbcTemplate.query("""
                        SELECT id, session_id, seq_no, role, content, created_at
                        FROM chat_message
                        WHERE session_id = ?
                        ORDER BY seq_no DESC
                        LIMIT ?
                        """,
                (rs, rowNum) -> new ChatMessageEntity(
                        rs.getLong("id"),
                        rs.getString("session_id"),
                        rs.getInt("seq_no"),
                        rs.getString("role"),
                        rs.getString("content"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                ),
                sessionId, limit
        );
        desc.sort((a, b) -> Integer.compare(a.seqNo(), b.seqNo()));
        return desc;
    }

    public List<ChatMessageEntity> findAllMessages(String sessionId, int limit) {
        return jdbcTemplate.query("""
                        SELECT id, session_id, seq_no, role, content, created_at
                        FROM chat_message
                        WHERE session_id = ?
                        ORDER BY seq_no ASC
                        LIMIT ?
                        """,
                (rs, rowNum) -> new ChatMessageEntity(
                        rs.getLong("id"),
                        rs.getString("session_id"),
                        rs.getInt("seq_no"),
                        rs.getString("role"),
                        rs.getString("content"),
                        rs.getTimestamp("created_at").toLocalDateTime()
                ),
                sessionId, limit
        );
    }

    public Optional<ChatSummaryEntity> findSummary(String sessionId) {
        List<ChatSummaryEntity> results = jdbcTemplate.query("""
                        SELECT session_id, summary_text, covered_seq, updated_at
                        FROM chat_summary
                        WHERE session_id = ?
                        """,
                (rs, rowNum) -> new ChatSummaryEntity(
                        rs.getString("session_id"),
                        rs.getString("summary_text"),
                        rs.getInt("covered_seq"),
                        rs.getTimestamp("updated_at").toLocalDateTime()
                ),
                sessionId
        );
        if (results.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(results.get(0));
    }

    public void upsertSummary(String sessionId, String summaryText, int coveredSeq, LocalDateTime now) {
        jdbcTemplate.update("""
                INSERT INTO chat_summary(session_id, summary_text, covered_seq, updated_at)
                VALUES (?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    summary_text = VALUES(summary_text),
                    covered_seq = VALUES(covered_seq),
                    updated_at = VALUES(updated_at)
                """, sessionId, summaryText, coveredSeq, Timestamp.valueOf(now));
    }

    public List<ChatSessionEntity> listSessions(int limit) {
        return jdbcTemplate.query("""
                        SELECT session_id, title, created_at, updated_at, last_message_at, message_count
                        FROM chat_session
                        ORDER BY COALESCE(last_message_at, created_at) DESC
                        LIMIT ?
                        """,
                (rs, rowNum) -> {
                    Timestamp lastMessageAt = rs.getTimestamp("last_message_at");
                    return new ChatSessionEntity(
                            rs.getString("session_id"),
                            rs.getString("title"),
                            rs.getTimestamp("created_at").toLocalDateTime(),
                            rs.getTimestamp("updated_at").toLocalDateTime(),
                            lastMessageAt == null ? null : lastMessageAt.toLocalDateTime(),
                            rs.getInt("message_count")
                    );
                },
                limit
        );
    }

    public Optional<ChatSessionEntity> findSession(String sessionId) {
        List<ChatSessionEntity> list = jdbcTemplate.query("""
                        SELECT session_id, title, created_at, updated_at, last_message_at, message_count
                        FROM chat_session
                        WHERE session_id = ?
                        """,
                (rs, rowNum) -> {
                    Timestamp lastMessageAt = rs.getTimestamp("last_message_at");
                    return new ChatSessionEntity(
                            rs.getString("session_id"),
                            rs.getString("title"),
                            rs.getTimestamp("created_at").toLocalDateTime(),
                            rs.getTimestamp("updated_at").toLocalDateTime(),
                            lastMessageAt == null ? null : lastMessageAt.toLocalDateTime(),
                            rs.getInt("message_count")
                    );
                },
                sessionId
        );
        if (list.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(list.get(0));
    }

    public void deleteSession(String sessionId) {
        jdbcTemplate.update("DELETE FROM chat_session WHERE session_id = ?", sessionId);
    }
}
