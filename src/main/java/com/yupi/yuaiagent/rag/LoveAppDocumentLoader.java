package com.yupi.yuaiagent.rag;

import com.yupi.yuaiagent.config.KnowledgeDocumentProperties;
import com.yupi.yuaiagent.rag.model.DocumentParseRequest;
import com.yupi.yuaiagent.rag.parser.KnowledgeDocumentParser;
import com.yupi.yuaiagent.rag.parser.KnowledgeDocumentParserFactory;
import com.yupi.yuaiagent.rag.splitter.RecursiveOverlapDocumentSplitter;
import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 情感助手知识库文档加载器
 */
@Component
public class LoveAppDocumentLoader {

    @Resource
    private KnowledgeDocumentProperties knowledgeDocumentProperties;

    @Resource
    private ResourcePatternResolver resourcePatternResolver;

    @Resource
    private KnowledgeDocumentParserFactory knowledgeDocumentParserFactory;

    @Resource
    private RecursiveOverlapDocumentSplitter recursiveOverlapDocumentSplitter;

    /**
     * 加载类路径下的知识库文档，并完成解析与切分
     *
     * @return 文档分段列表
     */
    public List<Document> loadDocuments() {
        try {
            org.springframework.core.io.Resource[] resources =
                    resourcePatternResolver.getResources(knowledgeDocumentProperties.getClasspathPattern());
            List<Document> result = new ArrayList<>();
            for (org.springframework.core.io.Resource resource : resources) {
                String filename = resource.getFilename();
                if (!isSupported(filename)) {
                    continue;
                }
                result.addAll(parseAndSplit(resource, filename, "classpath"));
            }
            return result;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load knowledge documents from classpath", e);
        }
    }

    /**
     * 解析并切分单个资源
     *
     * @param resource 资源
     * @param filename 文件名
     * @param source 来源
     * @return 文档分段列表
     */
    public List<Document> parseAndSplit(org.springframework.core.io.Resource resource, String filename, String source) {
        KnowledgeDocumentParser parser = knowledgeDocumentParserFactory.getParser(filename);
        List<Document> documents = parser.parse(new DocumentParseRequest(resource, filename, source));
        return recursiveOverlapDocumentSplitter.split(documents);
    }

    private boolean isSupported(String filename) {
        if (filename == null || filename.isBlank()) {
            return false;
        }
        String extension = getExtension(filename);
        return knowledgeDocumentProperties.getSupportedExtensions().contains(extension);
    }

    private String getExtension(String filename) {
        int index = filename.lastIndexOf(".");
        if (index < 0) {
            return "";
        }
        return filename.substring(index + 1).toLowerCase();
    }
}
