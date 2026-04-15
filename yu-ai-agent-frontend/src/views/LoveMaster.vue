<template>
  <div class="assistant-container">
    <header class="header">
      <div class="back-button" @click="goBack">返回</div>
      <h1 class="title">AI情感助手</h1>
      <div class="chat-id">会话ID：{{ chatId || '未选择' }}</div>
    </header>

    <div class="main-layout">
      <aside class="session-sidebar">
        <button class="new-session-button" type="button" @click="handleCreateSession">
          + 新建会话
        </button>

        <div class="session-list">
          <div
            v-for="session in sessions"
            :key="session.sessionId"
            class="session-item"
            :class="{ active: session.sessionId === chatId }"
            @click="switchSession(session.sessionId)"
          >
            <div class="session-item-title">{{ session.title || '新会话' }}</div>
            <div class="session-item-meta">
              <span>{{ formatDateTime(session.lastMessageAt || session.createdAt) }}</span>
              <span>{{ session.messageCount || 0 }} 条</span>
            </div>
            <button class="session-delete" type="button" @click.stop="handleDeleteSession(session.sessionId)">
              删除
            </button>
          </div>
        </div>
      </aside>

      <section class="chat-panel">
        <ChatRoom
          :messages="messages"
          :connection-status="connectionStatus"
          :show-upload="true"
          :uploading="uploading"
          ai-type="love"
          @send-message="sendMessage"
          @upload-file="uploadFile"
        />
      </section>
    </div>
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import ChatRoom from '../components/ChatRoom.vue'
import {
  chatWithLoveApp,
  createChatSession,
  deleteChatSession,
  listChatMessages,
  listChatSessions,
  uploadKnowledgeDocument
} from '../api'

const SESSION_STORAGE_KEY = 'emotion_assistant_current_session_id'

const router = useRouter()
const sessions = ref([])
const messages = ref([])
const chatId = ref('')
const connectionStatus = ref('disconnected')
const uploading = ref(false)
let eventSource = null

const addMessage = (content, isUser, type = '') => {
  messages.value.push({
    content,
    isUser,
    type,
    time: Date.now()
  })
}

const closeEventSource = () => {
  if (eventSource) {
    eventSource.close()
    eventSource = null
  }
}

const sanitizeAiMessage = (rawText) => {
  if (!rawText) {
    return ''
  }
  return rawText
    .replace(/\r\n/g, '\n')
    .replace(/\r/g, '\n')
    .replace(/```/g, '')
    .replace(/\*\*/g, '')
    .replace(/__/g, '')
    .replace(/`/g, '')
    .replace(/\n{3,}/g, '\n\n')
    .trim()
}

const mapHistoryMessages = (historyMessages = []) =>
  historyMessages
    .filter((item) => item && item.content)
    .map((item) => ({
      content: item.content,
      isUser: item.role === 'user',
      type: item.role === 'user' ? 'user-question' : 'ai-history',
      time: item.createdAt ? new Date(item.createdAt).getTime() : Date.now()
    }))

const refreshSessions = async () => {
  sessions.value = await listChatSessions(100)
}

const loadCurrentSessionMessages = async () => {
  if (!chatId.value) {
    messages.value = []
    return
  }
  const historyMessages = await listChatMessages(chatId.value, 500)
  messages.value = mapHistoryMessages(historyMessages)
}

const ensureGuideMessages = async () => {
  if (messages.value.length > 0) {
    return
  }
  addMessage('欢迎来到AI情感助手，请告诉我你的情绪或关系困扰，我会尽力提供帮助和建议。', false, 'ai-answer')
}

const setCurrentSessionId = (sessionId) => {
  chatId.value = sessionId
  if (sessionId) {
    localStorage.setItem(SESSION_STORAGE_KEY, sessionId)
  } else {
    localStorage.removeItem(SESSION_STORAGE_KEY)
  }
}

const createAndSwitchSession = async (title = '新会话') => {
  const created = await createChatSession(title)
  if (!created || !created.sessionId) {
    throw new Error('创建会话失败')
  }
  await refreshSessions()
  setCurrentSessionId(created.sessionId)
  await loadCurrentSessionMessages()
  await ensureGuideMessages()
}

const switchSession = async (sessionId) => {
  if (!sessionId || sessionId === chatId.value) {
    return
  }
  closeEventSource()
  connectionStatus.value = 'disconnected'
  setCurrentSessionId(sessionId)
  await loadCurrentSessionMessages()
  await ensureGuideMessages()
}

const handleCreateSession = async () => {
  closeEventSource()
  connectionStatus.value = 'disconnected'
  await createAndSwitchSession('新会话')
}

const handleDeleteSession = async (sessionId) => {
  await deleteChatSession(sessionId)
  await refreshSessions()
  if (sessionId !== chatId.value) {
    return
  }
  if (sessions.value.length > 0) {
    await switchSession(sessions.value[0].sessionId)
  } else {
    await createAndSwitchSession('新会话')
  }
}

const sendMessage = async (message) => {
  if (!chatId.value) {
    await createAndSwitchSession(message)
  }
  addMessage(message, true, 'user-question')

  closeEventSource()
  const aiMessageIndex = messages.value.length
  addMessage('', false, 'ai-answer')

  connectionStatus.value = 'connecting'
  eventSource = chatWithLoveApp(message, chatId.value)

  eventSource.onmessage = async (event) => {
    const data = event.data
    if (data && data !== '[DONE]' && aiMessageIndex < messages.value.length) {
      messages.value[aiMessageIndex].content += data
    }
    if (data === '[DONE]') {
      if (aiMessageIndex < messages.value.length) {
        messages.value[aiMessageIndex].content = sanitizeAiMessage(messages.value[aiMessageIndex].content)
      }
      connectionStatus.value = 'disconnected'
      closeEventSource()
      await refreshSessions()
    }
  }

  eventSource.onerror = async (error) => {
    console.error('SSE Error:', error)
    connectionStatus.value = 'error'
    closeEventSource()
    await refreshSessions()
  }
}

const uploadFile = async (file) => {
  if (!chatId.value) {
    await createAndSwitchSession('新会话')
  }
  uploading.value = true
  try {
    const result = await uploadKnowledgeDocument(file, chatId.value)
    const successName = result?.filename || file.name
    const chunkCount = Number(result?.chunkCount || 0)
    const uploadMessage = result?.message || '上传成功'
    const isSuccess = Boolean(result?.success)
    const messageType = isSuccess ? 'ai-final' : 'ai-error'
    addMessage(`${uploadMessage}：${successName}（入库分片：${chunkCount}）`, false, messageType)
  } catch (error) {
    console.error('Upload Error:', error)
    addMessage('文档上传失败，请检查文件格式或后端服务日志。', false, 'ai-error')
  } finally {
    uploading.value = false
  }
}

const goBack = () => {
  router.push('/')
}

const formatDateTime = (value) => {
  if (!value) {
    return '刚刚'
  }
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return '刚刚'
  }
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

onMounted(async () => {
  await refreshSessions()
  const cachedSessionId = localStorage.getItem(SESSION_STORAGE_KEY)
  if (cachedSessionId && sessions.value.some((item) => item.sessionId === cachedSessionId)) {
    setCurrentSessionId(cachedSessionId)
    await loadCurrentSessionMessages()
    await ensureGuideMessages()
    return
  }
  if (sessions.value.length > 0) {
    setCurrentSessionId(sessions.value[0].sessionId)
    await loadCurrentSessionMessages()
    await ensureGuideMessages()
    return
  }
  await createAndSwitchSession('新会话')
})

onBeforeUnmount(() => {
  closeEventSource()
})
</script>

<style scoped>
.assistant-container {
  height: 100vh;
  background-color: #fff9f9;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  background-color: #ff6b8b;
  color: #fff;
}

.back-button {
  cursor: pointer;
}

.title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
}

.chat-id {
  font-size: 14px;
  opacity: 0.9;
}

.main-layout {
  flex: 1;
  display: flex;
  min-height: 0;
  overflow: hidden;
}

.session-sidebar {
  width: 270px;
  flex-shrink: 0;
  background-color: #fff;
  border-right: 1px solid #f0d7de;
  padding: 12px;
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.new-session-button {
  border: none;
  border-radius: 16px;
  background-color: #ff6b8b;
  color: #fff;
  padding: 8px 12px;
  cursor: pointer;
}

.session-list {
  margin-top: 10px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.session-item {
  border: 1px solid #f2d9df;
  border-radius: 8px;
  padding: 8px;
  cursor: pointer;
  background: #fff;
}

.session-item.active {
  border-color: #ff6b8b;
  background-color: #fff2f6;
}

.session-item-title {
  font-size: 14px;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.session-item-meta {
  margin-top: 4px;
  display: flex;
  justify-content: space-between;
  color: #777;
  font-size: 12px;
}

.session-delete {
  margin-top: 6px;
  border: none;
  background: transparent;
  color: #e14a70;
  padding: 0;
  cursor: pointer;
  font-size: 12px;
}

.chat-panel {
  flex: 1;
  min-width: 0;
  padding: 12px;
  overflow: hidden;
}

@media (max-width: 960px) {
  .main-layout {
    flex-direction: column;
  }

  .session-sidebar {
    width: 100%;
    border-right: none;
    border-bottom: 1px solid #f0d7de;
    min-height: 220px;
  }
}
</style>

