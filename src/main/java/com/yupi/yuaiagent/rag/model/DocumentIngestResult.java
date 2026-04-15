package com.yupi.yuaiagent.rag.model;

/**
 * 文档入库结果
 *
 * @param filename 文件名
 * @param originalDocumentCount 原始文档数
 * @param chunkCount 切分后分段数
 */
public record DocumentIngestResult(String filename, int originalDocumentCount, int chunkCount) {
}
