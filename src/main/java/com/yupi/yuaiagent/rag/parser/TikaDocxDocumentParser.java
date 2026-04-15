package com.yupi.yuaiagent.rag.parser;

import com.yupi.yuaiagent.rag.model.DocumentParseRequest;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * 基于 Tika 的 DOCX 文档解析器
 */
@Component
public class TikaDocxDocumentParser implements KnowledgeDocumentParser {

    @Override
    public boolean supports(String filename) {
        return filename != null && filename.toLowerCase().endsWith(".docx");
    }

    @Override
    public List<Document> parse(DocumentParseRequest request) {
        try (InputStream inputStream = request.resource().getInputStream()) {
            BodyContentHandler handler = new BodyContentHandler(-1);
            Metadata metadata = new Metadata();
            metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, request.filename());
            AutoDetectParser parser = new AutoDetectParser(TikaConfig.getDefaultConfig());
            parser.parse(inputStream, handler, metadata, new ParseContext());
            Document document = new Document(handler.toString(), Map.of(
                    "filename", request.filename(),
                    "source", request.source(),
                    "fileType", "docx",
                    "title", defaultString(metadata.get(TikaCoreProperties.TITLE)),
                    "creator", defaultString(metadata.get(TikaCoreProperties.CREATOR))
            ));
            return List.of(document);
        } catch (IOException | TikaException | SAXException e) {
            throw new IllegalStateException("Failed to parse docx: " + request.filename(), e);
        }
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}
