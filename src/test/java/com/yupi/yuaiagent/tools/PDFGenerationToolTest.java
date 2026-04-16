package com.yupi.yuaiagent.tools;

import com.yupi.yuaiagent.constant.FileConstant;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PDFGenerationToolTest {

    @Test
    void generatePDF() {
        PDFGenerationTool tool = new PDFGenerationTool();
        String fileName = "test-generate.pdf";
        String content = "PDF generation smoke test.";
        String result = tool.generatePDF(fileName, content);

        assertNotNull(result);
        assertTrue(result.contains("PDF generated successfully"));
        assertTrue(Files.exists(Path.of(FileConstant.FILE_SAVE_DIR, "pdf", fileName)));
    }

    @Test
    void generateStyledPdf() {
        PDFGenerationTool tool = new PDFGenerationTool();
        String fileName = "test-styled.pdf";
        String result = tool.generateStyledPdf(
                fileName,
                "poster",
                "视觉化 PDF 标题",
                "副标题说明",
                "第一段内容。\n\n第二段内容。",
                "",
                "#ff4d6d"
        );

        assertNotNull(result);
        assertTrue(result.contains("PDF generated successfully"));
        assertTrue(Files.exists(Path.of(FileConstant.FILE_SAVE_DIR, "pdf", fileName)));
    }
}
