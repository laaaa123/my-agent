package com.yupi.yuaiagent.rag.parser;

import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 文档解析器工厂
 */
@Component
public class KnowledgeDocumentParserFactory {

    @Resource
    private List<KnowledgeDocumentParser> knowledgeDocumentParsers;

    /**
     * 按文件名获取解析器
     *
     * @param filename 文件名
     * @return 解析器
     */
    public KnowledgeDocumentParser getParser(String filename) {
        return knowledgeDocumentParsers.stream()
                .filter(parser -> parser.supports(filename))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("暂不支持的文档类型：" + filename));
    }
}
