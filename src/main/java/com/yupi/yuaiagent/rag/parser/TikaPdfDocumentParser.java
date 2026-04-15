package com.yupi.yuaiagent.rag.parser;

import com.yupi.yuaiagent.config.KnowledgeDocumentProperties;
import com.yupi.yuaiagent.rag.model.DocumentParseRequest;
import jakarta.annotation.Resource;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.microsoft.OfficeParserConfig;
import org.apache.tika.parser.ocr.TesseractOCRConfig;
import org.apache.tika.parser.pdf.PDFParserConfig;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * 基于 Tika 的 PDF 文档解析器
 */
@Component
public class TikaPdfDocumentParser implements KnowledgeDocumentParser {

    private static final Logger log = LoggerFactory.getLogger(TikaPdfDocumentParser.class);

    @Resource
    private KnowledgeDocumentProperties knowledgeDocumentProperties;

    @Override
    public boolean supports(String filename) {
        return filename != null && filename.toLowerCase().endsWith(".pdf");
    }

    @Override
    public List<Document> parse(DocumentParseRequest request) {
        try {
            return List.of(parseDocument(request, knowledgeDocumentProperties.isOcrEnabled()));
        } catch (Exception firstException) {
            if (!knowledgeDocumentProperties.isOcrEnabled()) {
                throw new IllegalStateException("Failed to parse pdf: " + request.filename(), firstException);
            }
            log.warn("PDF OCR parsing failed, fallback to plain parsing, filename={}", request.filename(), firstException);
            try {
                return List.of(parseDocument(request, false));
            } catch (Exception secondException) {
                throw new IllegalStateException("Failed to parse pdf: " + request.filename(), secondException);
            }
        }
    }

    private Document parseDocument(DocumentParseRequest request, boolean ocrEnabled)
            throws IOException, TikaException, SAXException {
        try (InputStream inputStream = request.resource().getInputStream()) {
            BodyContentHandler handler = new BodyContentHandler(-1);
            Metadata metadata = new Metadata();
            metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, request.filename());
            ParseContext parseContext = buildParseContext(ocrEnabled);
            AutoDetectParser parser = new AutoDetectParser(TikaConfig.getDefaultConfig());
            parser.parse(inputStream, handler, metadata, parseContext);
            String content = handler.toString();
            return new Document(content, buildMetadata(request, metadata, ocrEnabled));
        }
    }

    private ParseContext buildParseContext(boolean ocrEnabled) {
        ParseContext parseContext = new ParseContext();
        PDFParserConfig pdfParserConfig = new PDFParserConfig();
        pdfParserConfig.setExtractInlineImages(true);
        pdfParserConfig.setOcrStrategy(ocrEnabled
                ? PDFParserConfig.OCR_STRATEGY.AUTO
                : PDFParserConfig.OCR_STRATEGY.NO_OCR);
        parseContext.set(PDFParserConfig.class, pdfParserConfig);

        TesseractOCRConfig tesseractOCRConfig = new TesseractOCRConfig();
        // Tika 2.9.2 的 TesseractOCRConfig 不提供 setTesseractPath，
        // 当前版本通过系统环境或默认 PATH 寻找 tesseract 可执行文件。
        parseContext.set(TesseractOCRConfig.class, tesseractOCRConfig);
        parseContext.set(OfficeParserConfig.class, new OfficeParserConfig());
        return parseContext;
    }

    private Map<String, Object> buildMetadata(DocumentParseRequest request, Metadata metadata, boolean ocrEnabled) {
        return Map.of(
                "filename", request.filename(),
                "source", request.source(),
                "fileType", "pdf",
                "ocrEnabled", ocrEnabled,
                "title", defaultString(metadata.get("title")),
                "author", defaultString(metadata.get("Author"))
        );
    }

    private String defaultString(String value) {
        return value == null ? "" : value;
    }
}
