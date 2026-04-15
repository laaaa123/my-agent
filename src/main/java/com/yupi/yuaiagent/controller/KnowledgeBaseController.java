package com.yupi.yuaiagent.controller;

import com.yupi.yuaiagent.rag.model.DocumentIngestResult;
import com.yupi.yuaiagent.rag.model.KnowledgeBaseUploadResponse;
import com.yupi.yuaiagent.rag.model.KnowledgeBaseStatusResponse;
import com.yupi.yuaiagent.rag.service.KnowledgeBaseIngestionService;
import com.yupi.yuaiagent.rag.service.KnowledgeDocumentRegistry;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 知识库文档管理接口
 */
@RestController
@RequestMapping("/knowledge-base")
public class KnowledgeBaseController {

    @Resource
    private KnowledgeBaseIngestionService knowledgeBaseIngestionService;

    @Resource
    private KnowledgeDocumentRegistry knowledgeDocumentRegistry;

    /**
     * 查看当前知识库状态
     *
     * @return 知识库状态
     */
    @GetMapping("/status")
    public KnowledgeBaseStatusResponse getKnowledgeBaseStatus() {
        return new KnowledgeBaseStatusResponse(
                knowledgeDocumentRegistry.getDocumentCount(),
                knowledgeDocumentRegistry.getChunkCount(),
                knowledgeDocumentRegistry.getLoadedDocuments()
        );
    }

    /**
     * 重新加载类路径知识库文档
     *
     * @return 入库结果
     */
    @PostMapping("/reload")
    public List<DocumentIngestResult> reloadDocuments() {
        return knowledgeBaseIngestionService.ingestClasspathDocuments();
    }

    /**
     * 上传文档并写入知识库
     *
     * @param file 上传文件
     * @param chatId 会话 id
     * @return 上传响应
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public KnowledgeBaseUploadResponse uploadDocument(@RequestParam("file") MultipartFile file,
                                                      @RequestParam(value = "chatId", required = false) String chatId) {
        DocumentIngestResult result = knowledgeBaseIngestionService.ingestMultipartFile(file, chatId);
        boolean hasChunks = result.chunkCount() > 0;
        String message = hasChunks ? "上传成功" : "上传成功，但未解析到可检索内容，请检查文档格式";
        return new KnowledgeBaseUploadResponse(
                result.filename(),
                hasChunks,
                message,
                result.chunkCount()
        );
    }
}
