package com.yupi.yuaiagent.rag.service;

import com.yupi.yuaiagent.rag.model.LoadedKnowledgeDocument;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 知识库文档注册表
 */
@Component
public class KnowledgeDocumentRegistry {

    private final Set<String> ingestedChunkKeys = ConcurrentHashMap.newKeySet();

    private final ConcurrentMap<String, LoadedKnowledgeDocument> loadedDocuments = new ConcurrentHashMap<>();

    /**
     * 注册文档分段指纹
     *
     * @param chunkKey 分段指纹
     * @return 首次注册返回 true，重复返回 false
     */
    public boolean register(String chunkKey) {
        return ingestedChunkKeys.add(chunkKey);
    }

    /**
     * 记录文档加载结果
     *
     * @param filename 文件名
     * @param source 来源
     * @param chunkCount 新增分段数
     */
    public void recordDocument(String filename, String source, int chunkCount) {
        if (chunkCount <= 0) {
            return;
        }
        String key = source + ":" + filename;
        loadedDocuments.compute(key, (ignored, existing) -> {
            if (existing == null) {
                return new LoadedKnowledgeDocument(filename, source, chunkCount);
            }
            return new LoadedKnowledgeDocument(
                    existing.filename(),
                    existing.source(),
                    existing.chunkCount() + chunkCount
            );
        });
    }

    /**
     * 获取所有已加载文档
     *
     * @return 文档明细
     */
    public List<LoadedKnowledgeDocument> getLoadedDocuments() {
        List<LoadedKnowledgeDocument> documents = new ArrayList<>(loadedDocuments.values());
        documents.sort(Comparator.comparing(LoadedKnowledgeDocument::source)
                .thenComparing(LoadedKnowledgeDocument::filename));
        return documents;
    }

    /**
     * 获取已加载文档总数
     *
     * @return 文档总数
     */
    public int getDocumentCount() {
        return loadedDocuments.size();
    }

    /**
     * 获取已加载分段总数
     *
     * @return 分段总数
     */
    public int getChunkCount() {
        return loadedDocuments.values().stream()
                .mapToInt(LoadedKnowledgeDocument::chunkCount)
                .sum();
    }
}
