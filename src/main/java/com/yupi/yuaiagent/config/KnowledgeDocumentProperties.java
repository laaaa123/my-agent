package com.yupi.yuaiagent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 知识库文档处理配置
 */
@Component
@ConfigurationProperties(prefix = "knowledge.document")
public class KnowledgeDocumentProperties {

    /**
     * 文档扫描目录
     */
    private String classpathPattern = "classpath*:document/**/*.*";

    /**
     * 允许解析的文件后缀
     */
    private List<String> supportedExtensions = List.of("md", "pdf", "docx");

    /**
     * 上传文件保存目录
     */
    private String uploadDir = "tmp/uploads";

    /**
     * 是否启用 OCR
     */
    private boolean ocrEnabled = true;

    /**
     * Tesseract 可执行文件目录，可为空
     */
    private String tesseractPath;

    /**
     * 单段最大字符数
     */
    private int maxChunkSize = 800;

    /**
     * 分段重叠字符数
     */
    private int overlapSize = 120;

    public String getClasspathPattern() {
        return classpathPattern;
    }

    public void setClasspathPattern(String classpathPattern) {
        this.classpathPattern = classpathPattern;
    }

    public List<String> getSupportedExtensions() {
        return supportedExtensions;
    }

    public void setSupportedExtensions(List<String> supportedExtensions) {
        this.supportedExtensions = supportedExtensions;
    }

    public String getUploadDir() {
        return uploadDir;
    }

    public void setUploadDir(String uploadDir) {
        this.uploadDir = uploadDir;
    }

    public boolean isOcrEnabled() {
        return ocrEnabled;
    }

    public void setOcrEnabled(boolean ocrEnabled) {
        this.ocrEnabled = ocrEnabled;
    }

    public String getTesseractPath() {
        return tesseractPath;
    }

    public void setTesseractPath(String tesseractPath) {
        this.tesseractPath = tesseractPath;
    }

    public int getMaxChunkSize() {
        return maxChunkSize;
    }

    public void setMaxChunkSize(int maxChunkSize) {
        this.maxChunkSize = maxChunkSize;
    }

    public int getOverlapSize() {
        return overlapSize;
    }

    public void setOverlapSize(int overlapSize) {
        this.overlapSize = overlapSize;
    }
}
