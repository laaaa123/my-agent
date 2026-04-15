package com.yupi.yuaiagent.rag;

import com.yupi.yuaiagent.rag.service.KnowledgeDocumentRegistry;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.yupi.yuaiagent.rag.service.KnowledgeBaseIngestionService.GLOBAL_KNOWLEDGE_SCOPE;

/**
 * 情感助手向量存储配置
 */
@Configuration
public class LoveAppVectorStoreConfig {

    private static final Logger log = LoggerFactory.getLogger(LoveAppVectorStoreConfig.class);

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    @Resource
    private KnowledgeDocumentRegistry knowledgeDocumentRegistry;

    @Bean
    VectorStore loveAppVectorStore(EmbeddingModel dashscopeEmbeddingModel) {
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(dashscopeEmbeddingModel).build();
        List<Document> documents = loveAppDocumentLoader.loadDocuments();
        documents.forEach(document -> document.getMetadata().put("session_id", GLOBAL_KNOWLEDGE_SCOPE));
        List<Document> documentsToIngest = documents.stream()
                .filter(document -> knowledgeDocumentRegistry.register(buildChunkKey(document)))
                .toList();
        if (!documentsToIngest.isEmpty()) {
            simpleVectorStore.add(documentsToIngest);
            recordDocuments(documentsToIngest);
        }
        printKnowledgeBaseSummary();
        return simpleVectorStore;
    }

    private String buildChunkKey(Document document) {
        String filename = String.valueOf(document.getMetadata().getOrDefault("filename", "unknown"));
        String source = String.valueOf(document.getMetadata().getOrDefault("source", "classpath"));
        String chunkIndex = String.valueOf(document.getMetadata().getOrDefault("chunkIndex", 0));
        String contentMd5 = DigestUtils.md5DigestAsHex(document.getText().getBytes(StandardCharsets.UTF_8));
        return source + ":" + filename + ":" + chunkIndex + ":" + contentMd5;
    }

    private void recordDocuments(List<Document> documents) {
        java.util.Map<String, Integer> documentChunkCounter = new java.util.LinkedHashMap<>();
        for (Document document : documents) {
            String filename = String.valueOf(document.getMetadata().getOrDefault("filename", "unknown"));
            String source = String.valueOf(document.getMetadata().getOrDefault("source", "classpath"));
            String key = source + ":" + filename;
            documentChunkCounter.merge(key, 1, Integer::sum);
        }
        documentChunkCounter.forEach((key, count) -> {
            String[] parts = key.split(":", 2);
            String source = parts[0];
            String filename = parts.length > 1 ? parts[1] : "unknown";
            knowledgeDocumentRegistry.recordDocument(filename, source, count);
        });
    }

    private void printKnowledgeBaseSummary() {
        int documentCount = knowledgeDocumentRegistry.getDocumentCount();
        int chunkCount = knowledgeDocumentRegistry.getChunkCount();
        List<String> lines = new ArrayList<>();
        lines.add("Knowledge base loaded: documents=" + documentCount + ", chunks=" + chunkCount);
        knowledgeDocumentRegistry.getLoadedDocuments().forEach(document ->
                lines.add(String.format(" - source=%s, filename=%s, chunks=%d",
                        document.source(), document.filename(), document.chunkCount()))
        );
        log.info("\n{}", String.join("\n", lines));
    }
}
