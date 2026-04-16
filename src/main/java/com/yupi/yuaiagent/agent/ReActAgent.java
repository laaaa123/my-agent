package com.yupi.yuaiagent.agent;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;

/**
 * ReAct-style agent abstraction.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public abstract class ReActAgent extends BaseAgent {

    public abstract boolean think();

    public abstract String act();

    @Override
    public String step() {
        try {
            boolean shouldAct = think();
            if (!shouldAct) {
                return resolveTerminalText();
            }
            return act();
        } catch (Exception e) {
            log.error("Step execution failed", e);
            return "Step execution failed: " + e.getMessage();
        }
    }

    private String resolveTerminalText() {
        if (getMessageList().isEmpty()) {
            return "Task completed.";
        }
        Message lastMessage = getMessageList().get(getMessageList().size() - 1);
        if (lastMessage == null || lastMessage.getText() == null || lastMessage.getText().isBlank()) {
            return "Task completed.";
        }
        return lastMessage.getText();
    }
}
