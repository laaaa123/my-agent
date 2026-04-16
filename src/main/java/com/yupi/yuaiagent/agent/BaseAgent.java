package com.yupi.yuaiagent.agent;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yupi.yuaiagent.agent.model.AgentState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Base abstraction for agents with step-by-step execution.
 */
public abstract class BaseAgent {

    private static final Logger log = LoggerFactory.getLogger(BaseAgent.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private String name;

    private String systemPrompt;

    private String nextStepPrompt;

    private AgentState state = AgentState.IDLE;

    private int currentStep = 0;

    private int maxSteps = 10;

    private ChatClient chatClient;

    private List<Message> messageList = new ArrayList<>();

    private transient SseEmitter currentEmitter;

    private transient boolean reasoningStarted;

    private transient boolean reasoningDone;

    private transient boolean answerStarted;

    public String run(String userPrompt) {
        if (this.state != AgentState.IDLE) {
            throw new RuntimeException("Cannot run agent from state: " + this.state);
        }
        if (StrUtil.isBlank(userPrompt)) {
            throw new RuntimeException("Cannot run agent with empty user prompt");
        }
        this.state = AgentState.RUNNING;
        messageList.add(new UserMessage(userPrompt));
        List<String> results = new ArrayList<>();
        try {
            for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
                int stepNumber = i + 1;
                currentStep = stepNumber;
                log.info("Executing step {}/{}", stepNumber, maxSteps);
                String stepResult = step();
                if (StrUtil.isNotBlank(stepResult)) {
                    results.add(stepResult);
                }
            }
            if (state != AgentState.FINISHED && currentStep >= maxSteps) {
                state = AgentState.FINISHED;
                results.add("Task stopped after reaching max steps: " + maxSteps + ".");
            }
            return String.join("\n", results);
        } catch (Exception e) {
            state = AgentState.ERROR;
            log.error("Error executing agent", e);
            return "Execution failed: " + e.getMessage();
        } finally {
            cleanup();
        }
    }

    public SseEmitter runStream(String userPrompt) {
        SseEmitter sseEmitter = new SseEmitter(300000L);
        CompletableFuture.runAsync(() -> {
            this.currentEmitter = sseEmitter;
            this.reasoningStarted = false;
            this.reasoningDone = false;
            this.answerStarted = false;
            try {
                if (this.state != AgentState.IDLE) {
                    emitError("Agent state is not executable: " + this.state);
                    emitDone();
                    sseEmitter.complete();
                    return;
                }
                if (StrUtil.isBlank(userPrompt)) {
                    emitError("User prompt cannot be empty.");
                    emitDone();
                    sseEmitter.complete();
                    return;
                }
            } catch (Exception e) {
                sseEmitter.completeWithError(e);
                return;
            }
            this.state = AgentState.RUNNING;
            messageList.add(new UserMessage(userPrompt));
            emitReasoningStart();
            try {
                for (int i = 0; i < maxSteps && state != AgentState.FINISHED; i++) {
                    int stepNumber = i + 1;
                    currentStep = stepNumber;
                    log.info("Executing step {}/{}", stepNumber, maxSteps);
                    String result = step();
                    if (StrUtil.isNotBlank(result)) {
                        emitAnswer(result);
                    }
                }
                if (state != AgentState.FINISHED && currentStep >= maxSteps) {
                    state = AgentState.FINISHED;
                    String fallbackAnswer = onMaxStepsReached();
                    if (StrUtil.isNotBlank(fallbackAnswer)) {
                        emitAnswer(fallbackAnswer);
                    } else {
                        emitAnswer("Task stopped after reaching max steps: " + maxSteps + ".");
                    }
                }
                emitAnswerDone();
                emitDone();
                sseEmitter.complete();
            } catch (Exception e) {
                state = AgentState.ERROR;
                log.error("Error executing agent", e);
                try {
                    emitAnswerDone();
                    emitError("Execution failed: " + e.getMessage());
                    emitDone();
                    sseEmitter.complete();
                } catch (IOException ex) {
                    sseEmitter.completeWithError(ex);
                }
            } finally {
                cleanup();
                this.currentEmitter = null;
            }
        });
        sseEmitter.onTimeout(() -> {
            this.state = AgentState.ERROR;
            cleanup();
            log.warn("SSE connection timeout");
        });
        sseEmitter.onCompletion(() -> {
            if (this.state == AgentState.RUNNING) {
                this.state = AgentState.FINISHED;
            }
            cleanup();
            log.info("SSE connection completed");
        });
        return sseEmitter;
    }

    public abstract String step();

    protected String onMaxStepsReached() {
        return "Task stopped after reaching max steps: " + maxSteps + ".";
    }

    protected void emitReasoningStart() {
        if (reasoningStarted) {
            return;
        }
        reasoningStarted = true;
        emitEvent("reasoning_start", null, null);
    }

    protected void emitReasoningStep(String content) {
        if (StrUtil.isBlank(content)) {
            return;
        }
        emitReasoningStart();
        emitEvent("reasoning_step", content.trim(), null);
    }

    protected void emitAnswer(String content) {
        if (StrUtil.isBlank(content)) {
            return;
        }
        emitReasoningDone();
        emitAnswerStart();
        emitEvent("answer_chunk", content, null);
    }

    protected void emitError(String content) throws IOException {
        emitReasoningDone();
        emitEvent("agent_error", content, null);
    }

    protected void emitDone() throws IOException {
        emitReasoningDone();
        emitEvent("done", null, null);
    }

    protected void emitToolProgress(String toolName, String content) {
        Map<String, Object> extra = new LinkedHashMap<>();
        extra.put("toolName", toolName);
        emitEvent("tool", content, extra);
    }

    protected void emitToolStart(String toolName, String content) {
        Map<String, Object> extra = new LinkedHashMap<>();
        extra.put("toolName", toolName);
        emitEvent("tool_start", content, extra);
    }

    protected void emitToolDone(String toolName, String content) {
        Map<String, Object> extra = new LinkedHashMap<>();
        extra.put("toolName", toolName);
        emitEvent("tool_done", content, extra);
    }

    private void emitReasoningDone() {
        if (!reasoningStarted || reasoningDone) {
            return;
        }
        reasoningDone = true;
        emitEvent("reasoning_done", null, null);
    }

    protected void emitAnswerStart() {
        if (answerStarted) {
            return;
        }
        answerStarted = true;
        emitEvent("answer_start", null, null);
    }

    protected void emitAnswerDone() {
        if (!answerStarted) {
            return;
        }
        emitEvent("answer_done", null, null);
    }

    private void emitEvent(String type, String content, Map<String, Object> extra) {
        if (currentEmitter == null) {
            return;
        }
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("type", type);
            if (content != null) {
                payload.put("content", content);
            }
            if (extra != null && !extra.isEmpty()) {
                payload.putAll(extra);
            }
            currentEmitter.send(OBJECT_MAPPER.writeValueAsString(payload));
        } catch (Exception e) {
            log.warn("Failed to emit agent SSE event", e);
        }
    }

    protected boolean hasActiveEmitter() {
        return currentEmitter != null;
    }

    protected void cleanup() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public String getNextStepPrompt() {
        return nextStepPrompt;
    }

    public void setNextStepPrompt(String nextStepPrompt) {
        this.nextStepPrompt = nextStepPrompt;
    }

    public AgentState getState() {
        return state;
    }

    public void setState(AgentState state) {
        this.state = state;
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(int currentStep) {
        this.currentStep = currentStep;
    }

    public int getMaxSteps() {
        return maxSteps;
    }

    public void setMaxSteps(int maxSteps) {
        this.maxSteps = maxSteps;
    }

    public ChatClient getChatClient() {
        return chatClient;
    }

    public void setChatClient(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public List<Message> getMessageList() {
        return messageList;
    }

    public void setMessageList(List<Message> messageList) {
        this.messageList = messageList;
    }
}
