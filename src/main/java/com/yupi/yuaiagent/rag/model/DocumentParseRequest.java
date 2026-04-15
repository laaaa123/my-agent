package com.yupi.yuaiagent.rag.model;

import org.springframework.core.io.Resource;

/**
 * 文档解析请求
 *
 * @param resource 文档资源
 * @param filename 文件名
 * @param source 来源
 */
public record DocumentParseRequest(Resource resource, String filename, String source) {
}
