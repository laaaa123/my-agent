package com.yupi.yuaiagent.rag.model;

/**
 * 知识库上传响应
 *
 * @param filename 文件名
 * @param success 是否成功
 * @param message 响应消息
 * @param chunkCount 入库分段数
 */
public record KnowledgeBaseUploadResponse(String filename, boolean success, String message, int chunkCount) {
}
