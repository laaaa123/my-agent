import axios from 'axios'

const API_BASE_URL = process.env.NODE_ENV === 'production'
  ? '/api'
  : 'http://localhost:8123/api'

const request = axios.create({
  baseURL: API_BASE_URL,
  timeout: 60000
})

export const buildApiUrl = (path = '') => {
  if (!path) {
    return ''
  }
  if (/^https?:\/\//i.test(path)) {
    return path
  }
  if (path.startsWith(API_BASE_URL)) {
    return path
  }
  if (API_BASE_URL.endsWith('/api') && path.startsWith('/api/')) {
    const origin = API_BASE_URL.slice(0, -4)
    return `${origin}${path}`
  }
  return `${API_BASE_URL}${path.startsWith('/') ? path : `/${path}`}`
}

export const connectSSE = (url, params = {}, onMessage, onError) => {
  const queryString = Object.keys(params)
    .map((key) => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
    .join('&')

  const fullUrl = queryString
    ? `${API_BASE_URL}${url}?${queryString}`
    : `${API_BASE_URL}${url}`

  const eventSource = new EventSource(fullUrl)

  eventSource.onmessage = (event) => {
    if (onMessage) {
      onMessage(event.data)
    }
  }

  eventSource.onerror = (error) => {
    if (onError) {
      onError(error)
    }
    eventSource.close()
  }

  return eventSource
}

export const chatWithLoveApp = (message, chatId) => connectSSE('/ai/emotion_app/chat/sse', { message, chatId })

export const chatWithManus = (message, chatId) => connectSSE('/ai/manus/chat', { message, chatId })

export const listChatSessions = async (limit = 50, assistantType = '') => {
  const params = { limit }
  if (assistantType) {
    params.assistantType = assistantType
  }
  const response = await request.get('/chat-sessions', { params })
  return response.data
}

export const createChatSession = async (title = '', assistantType = '') => {
  const params = {}
  if (assistantType) {
    params.assistantType = assistantType
  }
  const response = await request.post('/chat-sessions', { title }, { params })
  return response.data
}

export const listChatMessages = async (sessionId, limit = 300) => {
  const response = await request.get(`/chat-sessions/${encodeURIComponent(sessionId)}/messages`, {
    params: { limit }
  })
  return response.data
}

export const deleteChatSession = async (sessionId) => {
  const response = await request.delete(`/chat-sessions/${encodeURIComponent(sessionId)}`)
  return response.data
}

export const uploadKnowledgeDocument = async (file, chatId) => {
  const formData = new FormData()
  formData.append('file', file)
  if (chatId) {
    formData.append('chatId', chatId)
  }
  const response = await request.post('/knowledge-base/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
  return response.data
}

export const getKnowledgeBaseStatus = async () => {
  const response = await request.get('/knowledge-base/status')
  return response.data
}

export default {
  chatWithLoveApp,
  chatWithManus,
  listChatSessions,
  createChatSession,
  listChatMessages,
  deleteChatSession,
  uploadKnowledgeDocument,
  getKnowledgeBaseStatus
}
