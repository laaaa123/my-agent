package com.yupi.yuaiagent.app.router.model;

/**
 * 大模型意图识别结果
 *
 * @param intent 意图名称
 * @param confidence 置信度
 */
public record LlmIntentResult(String intent, Double confidence) {
}
