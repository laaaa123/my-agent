package com.yupi.yuaiagent.agent;

import com.yupi.yuaiagent.advisor.MyLoggerAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;

import java.util.ArrayList;
import java.util.List;

/**
 * Task-oriented agent with multi-step reasoning, tool usage, and chat memory.
 */
public class YuManus extends ToolCallAgent {

    private final ChatMemory chatMemory;

    private String chatId;

    private int initialMessageCount;

    private boolean memoryFlushed;

    public YuManus(ToolCallback[] allTools, ChatModel dashscopeChatModel, ChatMemory chatMemory) {
        super(allTools);
        this.chatMemory = chatMemory;
        this.setName("yuManus");
        this.setSystemPrompt("""
                You are YuManus, a task-oriented autonomous assistant.
                Your job is to understand the user goal, decide when tools are needed, use the best available tools,
                and continue until you can provide a useful final answer.

                Rules:
                1. Focus on completing the user task rather than chatting vaguely.
                2. If information is missing, use tools or retrieval before answering.
                3. Multi-step tool use is allowed when it helps complete the task.
                4. Never expose raw errors, debug text, or internal planning to the user.
                5. When enough information is available, reply with a clear final answer and stop.
                6. If the task is already solvable, stop searching for more details.
                7. When the task is complete, prefer calling the terminate tool to finish cleanly.
                """);
        this.setNextStepPrompt("""
                Continue the task using the current conversation, memory, and tool results.
                If more information is needed, call the most appropriate tool.
                If the answer is already clear, respond with the final answer only and terminate the task.
                """);
        this.setMaxSteps(20);
        ChatClient chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultAdvisors(new MyLoggerAdvisor())
                .build();
        this.setChatClient(chatClient);
    }

    /**
     * Initializes the conversation context so the agent can continue from history.
     */
    public void initializeConversation(String chatId, List<Message> history) {
        this.chatId = chatId;
        this.memoryFlushed = false;
        List<Message> messageList = history == null ? new ArrayList<>() : new ArrayList<>(history);
        this.setMessageList(messageList);
        this.initialMessageCount = messageList.size();
    }

    @Override
    protected void cleanup() {
        flushConversationMemory();
    }

    private void flushConversationMemory() {
        if (memoryFlushed || chatMemory == null || chatId == null || chatId.isBlank()) {
            return;
        }
        List<Message> currentMessages = getMessageList();
        if (currentMessages == null || currentMessages.size() <= initialMessageCount) {
            memoryFlushed = true;
            return;
        }
        List<Message> deltaMessages = currentMessages.subList(initialMessageCount, currentMessages.size())
                .stream()
                .filter(this::shouldPersist)
                .toList();
        if (!deltaMessages.isEmpty()) {
            chatMemory.add(chatId, deltaMessages);
        }
        memoryFlushed = true;
    }

    private boolean shouldPersist(Message message) {
        if (message == null || message.getText() == null || message.getText().isBlank()) {
            return false;
        }
        if (message.getMessageType() == MessageType.TOOL) {
            return false;
        }
        return !message.getText().trim().equals(getNextStepPrompt().trim());
    }
}
