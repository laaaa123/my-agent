package com.yupi.yuaiagent.app.knowledge;

import com.yupi.yuaiagent.rag.service.KnowledgeBaseIngestionService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 知识库检索服务，负责会话私有知识和公共知识的混合召回与回退。
 */
@Service
public class KnowledgeRetrievalService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeRetrievalService.class);
    private static final String GLOBAL_SCOPE = "global";
    private static final int PRIMARY_TOP_K = 28;
    private static final int FALLBACK_TOP_K = 48;
    private static final double PRIMARY_THRESHOLD = 0.20D;
    private static final double FALLBACK_THRESHOLD = 0.0D;
    private static final int MAX_CONTEXT_DOCS = 10;

    @Resource
    private VectorStore loveAppVectorStore;

    @Resource
    private KnowledgeBaseIngestionService knowledgeBaseIngestionService;

    /**
     * 检索知识上下文。
     *
     * @param query 用户问题
     * @param chatId 会话 id
     * @return 检索结果
     */
    public KnowledgeRetrievalResult retrieve(String query, String chatId) {
        String normalizedChatId = sanitizeChatId(chatId);
        boolean sessionScoped = knowledgeBaseIngestionService.hasSessionKnowledge(normalizedChatId);

        List<Document> primaryCandidates = similaritySearch(query, PRIMARY_TOP_K, PRIMARY_THRESHOLD);
        List<Document> primaryFiltered = filterByScope(primaryCandidates, normalizedChatId, sessionScoped);
        List<Document> merged = deduplicate(primaryFiltered);

        if (merged.isEmpty()) {
            List<Document> fallbackCandidates = similaritySearch(query, FALLBACK_TOP_K, FALLBACK_THRESHOLD);
            List<Document> fallbackFiltered = filterByScope(fallbackCandidates, normalizedChatId, sessionScoped);
            merged = deduplicate(fallbackFiltered);
        }

        List<Document> selected = merged.stream().limit(MAX_CONTEXT_DOCS).toList();
        String context = buildContext(selected);
        String hitSummary = selected.stream()
                .map(document -> String.valueOf(document.getMetadata().getOrDefault("filename", "unknown"))
                        + "@"
                        + String.valueOf(document.getMetadata().getOrDefault("session_id", GLOBAL_SCOPE)))
                .collect(Collectors.joining(","));
        log.info("知识检索完成：chatId={}, sessionScoped={}, primaryHits={}, finalHits={}, hits=[{}]",
                normalizedChatId, sessionScoped, primaryFiltered.size(), selected.size(), hitSummary);
        return new KnowledgeRetrievalResult(context, selected, sessionScoped);
    }

    private List<Document> similaritySearch(String query, int topK, double threshold) {
        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(threshold)
                .build();
        List<Document> documents = loveAppVectorStore.similaritySearch(searchRequest);
        return documents == null ? List.of() : documents;
    }

    private List<Document> filterByScope(List<Document> documents, String chatId, boolean sessionScoped) {
        Set<String> allowedScopes = new LinkedHashSet<>();
        allowedScopes.add(GLOBAL_SCOPE);
        if (sessionScoped) {
            allowedScopes.add(chatId);
        }
        return documents.stream()
                .filter(document -> isAllowedScope(document, allowedScopes))
                .toList();
    }

    private boolean isAllowedScope(Document document, Set<String> allowedScopes) {
        Object scope = document.getMetadata().getOrDefault("session_id", GLOBAL_SCOPE);
        return allowedScopes.contains(String.valueOf(scope));
    }

    private List<Document> deduplicate(List<Document> documents) {
        Map<String, Document> distinctDocuments = new LinkedHashMap<>();
        for (Document document : documents) {
            String text = document.getText() == null ? "" : document.getText().trim();
            if (text.isEmpty()) {
                continue;
            }
            String key = DigestUtils.md5DigestAsHex(text.getBytes(StandardCharsets.UTF_8));
            distinctDocuments.putIfAbsent(key, document);
        }
        return new ArrayList<>(distinctDocuments.values());
    }

    private String buildContext(List<Document> documents) {
        if (documents.isEmpty()) {
            return "";
        }
        return documents.stream()
                .map(this::formatDocument)
                .collect(Collectors.joining("\n\n"));
    }

    private String formatDocument(Document document) {
        String filename = String.valueOf(document.getMetadata().getOrDefault("filename", "unknown"));
        String scope = String.valueOf(document.getMetadata().getOrDefault("session_id", GLOBAL_SCOPE));
        return "来源文件：" + filename + "（scope=" + scope + "）\n" + document.getText();
    }

    private String sanitizeChatId(String chatId) {
        if (chatId == null || chatId.isBlank()) {
            return GLOBAL_SCOPE;
        }
        return chatId.trim().replaceAll("[^a-zA-Z0-9_\\-]", "_");
    }
}

