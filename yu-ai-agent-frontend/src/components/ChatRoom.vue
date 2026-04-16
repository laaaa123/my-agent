<template>
  <div class="chat-container" :class="themeClass">
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
      <div
        v-for="(msg, index) in messages"
        :key="index"
        class="message-wrapper"
        :class="msg.isUser ? 'message-wrapper-user' : 'message-wrapper-ai'"
      >
        <div v-if="!msg.isUser" class="message ai-message" :class="[msg.type]">
          <div class="avatar ai-avatar">
            <AiAvatarFallback :type="aiType" />
          </div>
          <div class="message-bubble">
            <div class="message-content">
              <div
                v-if="showReasoning(msg)"
                class="reasoning-panel"
                :class="{ 'reasoning-panel-done': msg.reasoningStatus === 'done' }"
              >
                <div class="reasoning-header">{{ getReasoningTitle(msg) }}</div>
                <div
                  v-for="(item, reasoningIndex) in msg.reasoning"
                  :key="`${index}-reasoning-${reasoningIndex}`"
                  class="reasoning-item"
                >
                  {{ item }}
                </div>
              </div>

              <div>{{ getDisplayContent(msg.content) }}</div>

              <div v-if="getPdfUrl(msg.content)" class="pdf-preview-card">
                <div class="pdf-preview-title">PDF 预览</div>
                <PdfPreview
                  class="pdf-preview-frame"
                  :src="getPdfUrl(msg.content)"
                  :scale="0.82"
                />
                <a
                  class="pdf-preview-link"
                  :href="getPdfUrl(msg.content)"
                  target="_blank"
                  rel="noopener noreferrer"
                >
                  在新窗口打开
                </a>
              </div>

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
import PdfPreview from './PdfPreview.vue'
import { buildApiUrl } from '../api'

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
const themeClass = `theme-${props.aiType || 'default'}`

const PDF_MARKER_PATTERN = /PDF_URL::([^\s]+)/i
const PDF_MARKDOWN_PATTERN = /\[[^\]]*\.pdf[^\]]*]\((\/api\/files\/pdf\/[^)\s]+)\)/i
const PDF_URL_PATTERN = /(\/api\/files\/pdf\/[^\s)\]]+)/i

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

const normalizePdfUrl = (url = '') => {
  return url
    .trim()
    .replace(/[)\]]+$/g, '')
}

const extractPdfUrl = (content = '') => {
  const markerMatch = content.match(PDF_MARKER_PATTERN)
  if (markerMatch) {
    return normalizePdfUrl(markerMatch[1])
  }

  const markdownMatch = content.match(PDF_MARKDOWN_PATTERN)
  if (markdownMatch) {
    return normalizePdfUrl(markdownMatch[1])
  }

  const directMatch = content.match(PDF_URL_PATTERN)
  if (directMatch) {
    return normalizePdfUrl(directMatch[1])
  }

  return ''
}

const getPdfUrl = (content = '') => {
  const extracted = extractPdfUrl(content)
  return extracted ? buildApiUrl(extracted) : ''
}

const getDisplayContent = (content = '') => {
  return content
    .replace(PDF_MARKER_PATTERN, '')
    .replace(PDF_MARKDOWN_PATTERN, '')
    .replace(PDF_URL_PATTERN, '')
    .replace(/\n{3,}/g, '\n\n')
    .trim()
}

const showReasoning = (message = {}) => {
  return Array.isArray(message.reasoning) && message.reasoning.length > 0
}

const getReasoningTitle = (message = {}) => {
  if (message.reasoningStatus === 'done') {
    if (typeof message.reasoningElapsedMs === 'number' && message.reasoningElapsedMs >= 0) {
      const seconds = Math.max(1, Math.round(message.reasoningElapsedMs / 1000))
      return `已思考（用时 ${seconds} 秒）`
    }
    return '已思考'
  }
  return '思考中'
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
  () => props.messages.map((message) => `${message.content || ''}${(message.reasoning || []).join('')}`).join(''),
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

.theme-super .chat-toolbar {
  background: #f3f6ff;
  border-bottom-color: #dbe3ef;
}

.theme-super .upload-button {
  background-color: #3f51b5;
}

.theme-super .upload-tip {
  color: #5b6b8c;
}

.chat-messages {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 72px;
  left: 0;
  overflow-y: auto;
  padding: 20px 20px 24px;
  display: flex;
  flex-direction: column;
}

.chat-messages.with-toolbar {
  top: 52px;
}

.message-wrapper {
  margin-bottom: 16px;
  width: 100%;
  display: flex;
}

.message-wrapper-ai {
  justify-content: flex-start;
}

.message-wrapper-user {
  justify-content: flex-end;
}

.message {
  display: flex;
  align-items: flex-start;
  max-width: min(920px, 78%);
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
  max-width: 100%;
  padding: 12px 16px;
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

.reasoning-panel {
  margin-bottom: 12px;
  padding: 10px 12px;
  border-radius: 12px;
  background: #f7f9fc;
  border: 1px solid #dbe3ef;
}

.reasoning-panel-done {
  background: #f6f8fb;
}

.reasoning-header {
  margin-bottom: 6px;
  color: #5b6b8c;
  font-size: 13px;
  font-weight: 600;
}

.reasoning-item {
  color: #5f6f89;
  font-size: 13px;
  line-height: 1.6;
}

.reasoning-item + .reasoning-item {
  margin-top: 4px;
}

.pdf-preview-card {
  margin-top: 12px;
  border: 1px solid #dbe3ef;
  border-radius: 14px;
  background-color: #ffffff;
  overflow: hidden;
}

.pdf-preview-title {
  padding: 10px 14px;
  font-size: 13px;
  color: #526075;
  background-color: #f7f9fc;
  border-bottom: 1px solid #e5e7eb;
}

.pdf-preview-frame {
  width: 100%;
  min-height: 260px;
  background-color: #ffffff;
}

.pdf-preview-link {
  display: inline-block;
  padding: 10px 14px 14px;
  color: #2563eb;
  font-size: 13px;
  text-decoration: none;
}

.pdf-preview-link:hover {
  text-decoration: underline;
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
  padding: 0 20px 16px;
}

.chat-input {
  height: 100%;
  width: 100%;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px 0 0;
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

  .chat-messages {
    padding: 16px 14px 20px;
  }

  .chat-input-container {
    padding: 0 14px 12px;
  }
}
</style>
