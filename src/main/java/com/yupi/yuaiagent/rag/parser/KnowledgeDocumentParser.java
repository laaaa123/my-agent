package com.yupi.yuaiagent.rag.parser;

import com.yupi.yuaiagent.rag.model.DocumentParseRequest;
import org.springframework.ai.document.Document;

import java.util.List;

/**
 * 知识库文档解析器
 */
public interface KnowledgeDocumentParser {

    /**
     * 是否支持当前文件
     *
     * @param filename 文件名
     * @return 是否支持
     */
    boolean supports(String filename);

    /**
     * 解析文档
     *
     * @param request 解析请求
     * @return 文档列表
     */
    List<Document> parse(DocumentParseRequest request);
}
