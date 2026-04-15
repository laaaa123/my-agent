<template>
  <div class="chat-container">
    <div v-if="showUpload" class="chat-toolbar">
      <input
        ref="fileInputRef"
        class="file-input"
        type="file"
        accept=".md,.pdf,.docx"
        @change="handleFileChange"
      />
      <button
        class="upload-button"
        type="button"
        :disabled="uploading"
        @click="openFilePicker"
      >
        {{ uploading ? '上传中...' : '上传文档' }}
      </button>
      <span class="upload-tip">支持 md / pdf / docx</span>
    </div>

    <div ref="messagesContainer" class="chat-messages" :class="{ 'with-toolbar': showUpload }">
      <div v-for="(msg, index) in messages" :key="index" class="message-wrapper">
        <div v-if="!msg.isUser" class="message ai-message" :class="[msg.type]">
          <div class="avatar ai-avatar">
            <AiAvatarFallback :type="aiType" />
          </div>
          <div class="message-bubble">
            <div class="message-content">
              {{ msg.content }}
              <span
                v-if="connectionStatus === 'connecting' && index === messages.length - 1"
                class="typing-indicator"
              >|</span>
            </div>
            <div class="message-time">{{ formatTime(msg.time) }}</div>
          </div>
        </div>

        <div v-else class="message user-message" :class="[msg.type]">
          <div class="message-bubble">
            <div class="message-content">{{ msg.content }}</div>
            <div class="message-time">{{ formatTime(msg.time) }}</div>
          </div>
          <div class="avatar user-avatar">
            <div class="avatar-placeholder">我</div>
          </div>
        </div>
      </div>
    </div>

    <div class="chat-input-container">
      <div class="chat-input">
        <textarea
          v-model="inputMessage"
          class="input-box"
          :disabled="connectionStatus === 'connecting'"
          placeholder="请输入消息..."
          @keydown.enter.prevent="sendMessage"
        />
        <button
          class="send-button"
          type="button"
          :disabled="connectionStatus === 'connecting' || !inputMessage.trim()"
          @click="sendMessage"
        >
          发送
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { nextTick, ref, watch } from 'vue'
import AiAvatarFallback from './AiAvatarFallback.vue'

const props = defineProps({
  messages: {
    type: Array,
    default: () => []
  },
  connectionStatus: {
    type: String,
    default: 'disconnected'
  },
  aiType: {
    type: String,
    default: 'default'
  },
  showUpload: {
    type: Boolean,
    default: false
  },
  uploading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['send-message', 'upload-file'])

const inputMessage = ref('')
const messagesContainer = ref(null)
const fileInputRef = ref(null)

const sendMessage = () => {
  if (!inputMessage.value.trim()) {
    return
  }
  emit('send-message', inputMessage.value.trim())
  inputMessage.value = ''
}

const openFilePicker = () => {
  if (!props.uploading) {
    fileInputRef.value?.click()
  }
}

const handleFileChange = (event) => {
  const [file] = event.target.files || []
  if (file) {
    emit('upload-file', file)
  }
  event.target.value = ''
}

const formatTime = (timestamp) => {
  const date = new Date(timestamp)
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

const scrollToBottom = async () => {
  await nextTick()
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

watch(() => props.messages.length, () => {
  scrollToBottom()
})

watch(
  () => props.messages.map((message) => message.content).join(''),
  () => {
    scrollToBottom()
  }
)
</script>

<style scoped>
.chat-container {
  display: flex;
  flex-direction: column;
  height: 70vh;
  min-height: 600px;
  background-color: #f7f8fa;
  border-radius: 10px;
  overflow: hidden;
  position: relative;
}

.chat-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  background: #fff7f8;
  border-bottom: 1px solid #f1dce2;
}

.file-input {
  display: none;
}

.upload-button {
  border: none;
  border-radius: 16px;
  background-color: #ff5f82;
  color: #fff;
  padding: 8px 16px;
  cursor: pointer;
  font-size: 14px;
  line-height: 20px;
}

.upload-button:disabled {
  opacity: 0.75;
  cursor: not-allowed;
}

.upload-tip {
  font-size: 13px;
  color: #8b6472;
}

.chat-messages {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 72px;
  left: 0;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
}

.chat-messages.with-toolbar {
  top: 52px;
}

.message-wrapper {
  margin-bottom: 14px;
  width: 100%;
}

.message {
  display: flex;
  align-items: flex-start;
  max-width: 86%;
}

.ai-message {
  margin-right: auto;
}

.user-message {
  margin-left: auto;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ai-avatar {
  margin-right: 8px;
}

.user-avatar {
  margin-left: 8px;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #1989fa;
  color: #fff;
  font-weight: 600;
}

.message-bubble {
  min-width: 120px;
  padding: 12px 14px;
  border-radius: 14px;
  position: relative;
}

.ai-message .message-bubble {
  background-color: #eff1f5;
  border-bottom-left-radius: 4px;
}

.user-message .message-bubble {
  background-color: #1989fa;
  color: #fff;
  border-bottom-right-radius: 4px;
}

.message-content {
  font-size: 15px;
  line-height: 1.65;
  text-align: left;
  white-space: pre-wrap;
  word-break: break-word;
}

.message-time {
  margin-top: 4px;
  font-size: 12px;
  opacity: 0.75;
  text-align: right;
}

.chat-input-container {
  position: absolute;
  right: 0;
  bottom: 0;
  left: 0;
  height: 72px;
  background-color: #fff;
  border-top: 1px solid #e5e7eb;
}

.chat-input {
  height: 100%;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
}

.input-box {
  flex: 1;
  min-height: 20px;
  max-height: 40px;
  resize: none;
  border: 1px solid #d8dce3;
  border-radius: 20px;
  padding: 10px 16px;
  font-size: 15px;
  outline: none;
}

.send-button {
  border: none;
  border-radius: 20px;
  background-color: #1989fa;
  color: #fff;
  font-size: 15px;
  padding: 0 20px;
  height: 40px;
  cursor: pointer;
}

.typing-indicator {
  display: inline-block;
  margin-left: 2px;
  animation: blink 0.7s infinite;
}

@keyframes blink {
  0% {
    opacity: 0;
  }
  50% {
    opacity: 1;
  }
  100% {
    opacity: 0;
  }
}

@media (max-width: 768px) {
  .chat-toolbar {
    flex-wrap: wrap;
    gap: 8px;
  }

  .chat-messages.with-toolbar {
    top: 68px;
  }

  .message {
    max-width: 92%;
  }
}
</style>

