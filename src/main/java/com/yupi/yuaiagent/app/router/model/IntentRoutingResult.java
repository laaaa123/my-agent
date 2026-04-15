package com.yupi.yuaiagent.app.router.model;

import com.yupi.yuaiagent.app.router.IntentType;

/**
 * 意图路由结果
 *
 * @param intent 意图类型
 * @param confidence 置信度
 * @param source 来源
 */
public record IntentRoutingResult(IntentType intent, double confidence, String source) {
}
