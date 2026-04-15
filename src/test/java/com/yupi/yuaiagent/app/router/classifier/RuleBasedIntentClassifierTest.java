package com.yupi.yuaiagent.app.router.classifier;

import com.yupi.yuaiagent.app.router.IntentType;
import com.yupi.yuaiagent.app.router.model.IntentRoutingResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * 规则意图分类器测试
 */
class RuleBasedIntentClassifierTest {

    private final RuleBasedIntentClassifier classifier = new RuleBasedIntentClassifier();

    @Test
    void shouldClassifyChitchat() {
        IntentRoutingResult result = classifier.classify("你好");
        Assertions.assertNotNull(result);
        Assertions.assertEquals(IntentType.CHITCHAT, result.intent());
    }

    @Test
    void shouldClassifyTool() {
        IntentRoutingResult result = classifier.classify("帮我查一下网页内容");
        Assertions.assertNotNull(result);
        Assertions.assertEquals(IntentType.TOOL, result.intent());
    }

    @Test
    void shouldClassifyClarificationWhenQueryTooShort() {
        IntentRoutingResult result = classifier.classify("怎么办");
        Assertions.assertNotNull(result);
        Assertions.assertEquals(IntentType.CLARIFICATION, result.intent());
    }

    @Test
    void shouldReturnNullWhenNeedLlmFallback() {
        IntentRoutingResult result = classifier.classify("我总是因为边界感的问题和对象争吵，该怎么沟通");
        Assertions.assertNull(result);
    }
}
