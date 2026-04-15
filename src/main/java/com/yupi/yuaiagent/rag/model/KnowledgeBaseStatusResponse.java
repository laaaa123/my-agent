package com.yupi.yuaiagent.rag.model;

import java.util.List;

/**
 * 知识库状态响应
 *
 * @param documentCount 文档数
 * @param chunkCount 分段总数
 * @param documents 文档明细
 */
public record KnowledgeBaseStatusResponse(int documentCount, int chunkCount, List<LoadedKnowledgeDocument> documents) {
}
