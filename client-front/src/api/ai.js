import request from './request'

/**
 * 获取可用模型列表
 */
export function getModels() {
  return request.get('/ai/models')
}

/**
 * 创建新会话
 */
export function createSession(model) {
  return request.post('/ai/session', { model })
}

/**
 * 获取会话列表
 */
export function listSessions() {
  return request.get('/ai/session/list')
}

/**
 * 删除会话
 */
export function deleteSession(id) {
  return request.delete(`/ai/session/${id}`)
}

/**
 * 获取会话消息列表
 */
export function listMessages(sessionId) {
  return request.get(`/ai/session/${sessionId}/messages`)
}

/**
 * SSE 流式聊天 - 使用原生 fetch 读取流
 * 返回 { abort, streamPromise }
 */
export function streamChat(sessionId, text, image) {
  const controller = new AbortController()
  const formData = new FormData()
  formData.append('text', text)
  if (image) {
    formData.append('image', image)
  }

  const token = localStorage.getItem('token')
  const headers = {}
  if (token) {
    headers['Authorization'] = `Bearer ${token}`
  }

  const streamPromise = fetch(`/api/ai/chat/${sessionId}`, {
    method: 'POST',
    headers,
    body: formData,
    signal: controller.signal
  })

  return {
    abort: () => controller.abort(),
    streamPromise
  }
}
