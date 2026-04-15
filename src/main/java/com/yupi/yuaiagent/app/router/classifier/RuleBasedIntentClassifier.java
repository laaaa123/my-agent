package com.yupi.yuaiagent.app.router.classifier;

import com.yupi.yuaiagent.app.router.IntentType;
import com.yupi.yuaiagent.app.router.model.IntentRoutingResult;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 规则意图分类器
 */
@Component
public class RuleBasedIntentClassifier {

    /**
     * 闲聊关键词
     */
    private static final Set<String> CHITCHAT_KEYWORDS = Set.of(
            "你好", "您好", "嗨", "哈喽", "谢谢", "感谢", "再见", "拜拜",
            "哈哈", "嗯嗯", "好的", "收到", "明白了"
    );

    /**
     * 工具调用关键词
     */
    private static final Set<String> TOOL_KEYWORDS = Set.of(
            "帮我查", "帮我找", "查一下", "搜索", "搜一下", "联网",
            "下载", "上传", "抓取", "生成", "执行", "保存", "读取",
            "打开", "调用工具", "用工具", "文件", "网页", "图片", "pdf"
    );

    /**
     * 规则分类
     *
     * @param query 用户问题
     * @return 命中的高置信度结果，未命中则返回 null
     */
    public IntentRoutingResult classify(String query) {
        String normalizedQuery = normalize(query);
        if (normalizedQuery.isEmpty()) {
            return new IntentRoutingResult(IntentType.CLARIFICATION, 0.99D, "rule");
        }
        if (normalizedQuery.length() <= 6 && CHITCHAT_KEYWORDS.contains(normalizedQuery)) {
            return new IntentRoutingResult(IntentType.CHITCHAT, 0.98D, "rule");
        }
        for (String keyword : TOOL_KEYWORDS) {
            if (normalizedQuery.contains(keyword)) {
                return new IntentRoutingResult(IntentType.TOOL, 0.92D, "rule");
            }
        }
        if (normalizedQuery.length() < 5) {
            return new IntentRoutingResult(IntentType.CLARIFICATION, 0.85D, "rule");
        }
        return null;
    }

    private String normalize(String query) {
        if (query == null) {
            return "";
        }
        return query.trim().replace("？", "").replace("?", "");
    }
}
