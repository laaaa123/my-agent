package com.yupi.yuaiagent.rag.splitter;

import com.yupi.yuaiagent.config.KnowledgeDocumentProperties;
import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 递归加重叠文档切分器
 */
@Component
public class RecursiveOverlapDocumentSplitter {

    private static final List<String> SEPARATORS = List.of(
            "\n\n",
            "\n",
            "## ",
            "# ",
            "。 ",
            "！ ",
            "？ ",
            "； ",
            "， ",
            "。",
            "！",
            "？",
            "；",
            "，",
            ". ",
            "! ",
            "? ",
            "; ",
            ", ",
            " ",
            ""
    );

    @Resource
    private KnowledgeDocumentProperties knowledgeDocumentProperties;

    /**
     * 切分文档
     *
     * @param documents 原始文档
     * @return 切分后的文档
     */
    public List<Document> split(List<Document> documents) {
        List<Document> result = new ArrayList<>();
        for (Document document : documents) {
            String text = document.getText();
            if (text == null || text.isBlank()) {
                continue;
            }
            List<String> chunks = splitText(text, 0);
            List<String> overlapChunks = applyOverlap(chunks);
            int index = 0;
            for (String chunk : overlapChunks) {
                if (chunk.isBlank()) {
                    continue;
                }
                Map<String, Object> metadata = new LinkedHashMap<>(document.getMetadata());
                metadata.put("chunkIndex", index++);
                metadata.put("chunkLength", chunk.length());
                result.add(new Document(chunk.trim(), metadata));
            }
        }
        return result;
    }

    private List<String> splitText(String text, int separatorIndex) {
        int maxChunkSize = knowledgeDocumentProperties.getMaxChunkSize();
        if (text.length() <= maxChunkSize || separatorIndex >= SEPARATORS.size() - 1) {
            return List.of(text);
        }
        String separator = SEPARATORS.get(separatorIndex);
        if (separator.isEmpty()) {
            return forceSplit(text, maxChunkSize);
        }
        String[] parts = text.split(Pattern.quote(separator));
        List<String> chunks = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String part : parts) {
            String normalizedPart = part == null ? "" : part.trim();
            if (normalizedPart.isEmpty()) {
                continue;
            }
            String candidate = current.isEmpty()
                    ? normalizedPart
                    : current + separator + normalizedPart;
            if (candidate.length() <= maxChunkSize) {
                current = new StringBuilder(candidate);
                continue;
            }
            if (!current.isEmpty()) {
                chunks.add(current.toString());
                current = new StringBuilder(normalizedPart);
            } else {
                chunks.addAll(splitText(normalizedPart, separatorIndex + 1));
            }
        }
        if (!current.isEmpty()) {
            chunks.add(current.toString());
        }
        List<String> normalizedChunks = new ArrayList<>();
        for (String chunk : chunks) {
            if (chunk.length() > maxChunkSize) {
                normalizedChunks.addAll(splitText(chunk, separatorIndex + 1));
            } else {
                normalizedChunks.add(chunk);
            }
        }
        return normalizedChunks;
    }

    private List<String> forceSplit(String text, int maxChunkSize) {
        List<String> chunks = new ArrayList<>();
        for (int start = 0; start < text.length(); start += maxChunkSize) {
            int end = Math.min(text.length(), start + maxChunkSize);
            chunks.add(text.substring(start, end));
        }
        return chunks;
    }

    private List<String> applyOverlap(List<String> chunks) {
        int overlapSize = knowledgeDocumentProperties.getOverlapSize();
        if (chunks.size() <= 1 || overlapSize <= 0) {
            return chunks;
        }
        List<String> result = new ArrayList<>();
        String previousChunk = null;
        for (String chunk : chunks) {
            if (previousChunk == null) {
                result.add(chunk);
            } else {
                String overlapPrefix = previousChunk.substring(Math.max(0, previousChunk.length() - overlapSize));
                result.add(overlapPrefix + chunk);
            }
            previousChunk = chunk;
        }
        return result;
    }
}
