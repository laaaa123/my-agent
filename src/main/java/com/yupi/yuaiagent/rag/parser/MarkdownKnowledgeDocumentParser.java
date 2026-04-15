package com.yupi.yuaiagent.rag.parser;

import com.yupi.yuaiagent.rag.model.DocumentParseRequest;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Markdown 文档解析器
 */
@Component
public class MarkdownKnowledgeDocumentParser implements KnowledgeDocumentParser {

    @Override
    public boolean supports(String filename) {
        return filename != null && filename.toLowerCase().endsWith(".md");
    }

    @Override
    public List<Document> parse(DocumentParseRequest request) {
        MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                .withHorizontalRuleCreateDocument(true)
                .withIncludeCodeBlock(false)
                .withIncludeBlockquote(false)
                .withAdditionalMetadata("filename", request.filename())
                .withAdditionalMetadata("source", request.source())
                .withAdditionalMetadata("fileType", "md")
                .build();
        MarkdownDocumentReader markdownDocumentReader = new MarkdownDocumentReader(request.resource(), config);
        return markdownDocumentReader.get();
    }
}
