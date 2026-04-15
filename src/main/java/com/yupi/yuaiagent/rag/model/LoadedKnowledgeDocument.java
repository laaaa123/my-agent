package com.yupi.yuaiagent.rag.model;

/**
 * 已加载知识库文档信息
 *
 * @param filename 文件名
 * @param source 来源
 * @param chunkCount 分段数
 */
public record LoadedKnowledgeDocument(String filename, String source, int chunkCount) {
}
