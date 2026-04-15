package com.yupi.yuaiagent.rag.service;

import com.yupi.yuaiagent.config.KnowledgeDocumentProperties;
import com.yupi.yuaiagent.rag.LoveAppDocumentLoader;
import com.yupi.yuaiagent.rag.model.DocumentIngestResult;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

/**
 * 知识库文档入库服务
 */
@Service
public class KnowledgeBaseIngestionService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeBaseIngestionService.class);
    public static final String GLOBAL_KNOWLEDGE_SCOPE = "global";
    private static final Pattern MARKDOWN_LINK_PATTERN = Pattern.compile("\\[([^\\]]+)]\\(([^)]+)\\)");
    private static final Pattern URL_PATTERN = Pattern.compile("(https?|sslocal)://\\S+");
    /**
     * 会话私有知识分片计数（仅统计已成功写入向量库的分片）。
     */
    private final ConcurrentMap<String, Integer> sessionChunkCounter = new ConcurrentHashMap<>();

    @Resource
    private KnowledgeDocumentProperties knowledgeDocumentProperties;

    @Resource
    private LoveAppDocumentLoader loveAppDocumentLoader;

    @Resource
    private VectorStore loveAppVectorStore;

    @Resource
    private KnowledgeDocumentRegistry knowledgeDocumentRegistry;

    /**
     * 加载类路径知识库文档并入库
     *
     * @return 入库结果
     */
    public List<DocumentIngestResult> ingestClasspathDocuments() {
        List<DocumentIngestResult> results = new ArrayList<>();
        try {
            org.springframework.core.io.support.PathMatchingResourcePatternResolver resolver =
                    new org.springframework.core.io.support.PathMatchingResourcePatternResolver();
            org.springframework.core.io.Resource[] resources =
                    resolver.getResources(knowledgeDocumentProperties.getClasspathPattern());
            for (org.springframework.core.io.Resource resource : resources) {
                String filename = resource.getFilename();
                if (!isSupported(filename)) {
                    continue;
                }
                results.add(ingestResource(resource, filename, "classpath", GLOBAL_KNOWLEDGE_SCOPE));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load knowledge documents from classpath", e);
        }
        return results;
    }

    /**
     * 上传文档后写入知识库
     *
     * @param file 上传文件
     * @param chatId 会话 id
     * @return 入库结果
     */
    public DocumentIngestResult ingestMultipartFile(MultipartFile file, String chatId) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("Uploaded filename must not be blank");
        }
        String filename = StringUtils.cleanPath(originalFilename);
        validateSupported(filename);
        String sessionScope = resolveSessionScope(chatId);
        try {
            Path uploadDir = Path.of(knowledgeDocumentProperties.getUploadDir(), sessionScope);
            Files.createDirectories(uploadDir);
            Path targetFile = uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);
            FileSystemResource resource = new FileSystemResource(targetFile);
            return ingestResource(resource, filename, "upload", sessionScope);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to ingest uploaded document: " + filename, e);
        }
    }

    /**
     * 判断指定会话是否已上传私有知识文档。
     *
     * @param chatId 会话 id
     * @return true-存在会话私有文档，false-不存在
     */
    public boolean hasSessionKnowledge(String chatId) {
        String sessionScope = resolveSessionScope(chatId);
        if (GLOBAL_KNOWLEDGE_SCOPE.equals(sessionScope)) {
            return false;
        }
        return sessionChunkCounter.getOrDefault(sessionScope, 0) > 0;
    }

    private DocumentIngestResult ingestResource(org.springframework.core.io.Resource resource,
                                                String filename,
                                                String source,
                                                String sessionScope) {
        List<Document> splitDocuments = loveAppDocumentLoader.parseAndSplit(resource, filename, source);
        List<Document> normalizedDocuments = splitDocuments.stream()
                .map(document -> normalizeDocument(document, sessionScope))
                .toList();
        List<Document> documentsToIngest = normalizedDocuments.stream()
                .filter(document -> document.getText() != null && !document.getText().isBlank())
                .filter(document -> knowledgeDocumentRegistry.register(buildChunkKey(filename, source, sessionScope, document)))
                .toList();
        if (!documentsToIngest.isEmpty()) {
            loveAppVectorStore.add(documentsToIngest);
            knowledgeDocumentRegistry.recordDocument(filename, source, documentsToIngest.size());
            if (!GLOBAL_KNOWLEDGE_SCOPE.equals(sessionScope)) {
                sessionChunkCounter.merge(sessionScope, documentsToIngest.size(), Integer::sum);
            }
        }
        log.info("Knowledge documents ingested, filename={}, sessionScope={}, chunkCount={}",
                filename, sessionScope, documentsToIngest.size());
        return new DocumentIngestResult(filename, 1, documentsToIngest.size());
    }

    private Document normalizeDocument(Document document, String sessionScope) {
        String normalizedText = normalizeForEmbedding(document.getText());
        java.util.Map<String, Object> metadata = new java.util.LinkedHashMap<>(document.getMetadata());
        metadata.put("session_id", sessionScope);
        metadata.put("normalized", true);
        return new Document(normalizedText, metadata);
    }

    private String normalizeForEmbedding(String rawText) {
        if (rawText == null) {
            return "";
        }
        String text = MARKDOWN_LINK_PATTERN.matcher(rawText).replaceAll("$1");
        text = URL_PATTERN.matcher(text).replaceAll(" ");
        text = text.replaceAll("\\s{2,}", " ").trim();
        return text;
    }

    private boolean isSupported(String filename) {
        if (filename == null || filename.isBlank()) {
            return false;
        }
        String extension = getExtension(filename);
        return knowledgeDocumentProperties.getSupportedExtensions().contains(extension);
    }

    private void validateSupported(String filename) {
        if (!isSupported(filename)) {
            throw new IllegalArgumentException("Only supported file types are: "
                    + knowledgeDocumentProperties.getSupportedExtensions());
        }
    }

    private String getExtension(String filename) {
        int index = filename.lastIndexOf(".");
        if (index < 0) {
            return "";
        }
        return filename.substring(index + 1).toLowerCase();
    }

    private String buildChunkKey(String filename, String source, String sessionScope, Document document) {
        String chunkIndex = String.valueOf(document.getMetadata().getOrDefault("chunkIndex", 0));
        String contentMd5 = DigestUtils.md5DigestAsHex(document.getText().getBytes(StandardCharsets.UTF_8));
        return source + ":" + sessionScope + ":" + filename + ":" + chunkIndex + ":" + contentMd5;
    }

    private String resolveSessionScope(String chatId) {
        if (chatId == null || chatId.isBlank()) {
            return GLOBAL_KNOWLEDGE_SCOPE;
        }
        return chatId.trim();
    }
}
