package com.yupi.yuaiagent.app.router;

import com.yupi.yuaiagent.app.router.classifier.LlmIntentClassifier;
import com.yupi.yuaiagent.app.router.classifier.RuleBasedIntentClassifier;
import com.yupi.yuaiagent.app.router.model.IntentRoutingResult;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 混合意图路由器
 */
@Component
public class HybridIntentRouter {

    private static final Logger log = LoggerFactory.getLogger(HybridIntentRouter.class);

    @Resource
    private RuleBasedIntentClassifier ruleBasedIntentClassifier;

    @Resource
    private LlmIntentClassifier llmIntentClassifier;

    /**
     * 规则优先，大模型兜底
     *
     * @param query 用户问题
     * @param chatId 会话 id
     * @return 路由结果
     */
    public IntentRoutingResult route(String query, String chatId) {
        IntentRoutingResult ruleResult = ruleBasedIntentClassifier.classify(query);
        if (ruleResult != null) {
            log.info("命中规则路由，intent={}, confidence={}", ruleResult.intent(), ruleResult.confidence());
            return ruleResult;
        }
        IntentRoutingResult llmResult = llmIntentClassifier.classify(query, chatId);
        log.info("命中大模型路由，intent={}, confidence={}", llmResult.intent(), llmResult.confidence());
        return llmResult;
    }
}
