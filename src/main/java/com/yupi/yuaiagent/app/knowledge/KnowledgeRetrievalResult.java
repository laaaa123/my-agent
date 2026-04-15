package com.yupi.yuaiagent.app.knowledge;

import org.springframework.ai.document.Document;

import java.util.List;

/**
 * 知识检索结果。
 *
 * @param context 提供给模型的检索上下文
 * @param documents 命中的文档分片
 * @param sessionScoped 是否启用了会话私有知识范围
 */
public record KnowledgeRetrievalResult(String context, List<Document> documents, boolean sessionScoped) {
}

