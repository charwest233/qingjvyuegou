// ============================================
// 人工客服 API
// ============================================

import request from '@/utils/request'

export const csApi = {
  /** 获取最近活跃会话 */
  getSessions() {
    return request.get('/customer-service/sessions')
  },

  /** 获取会话消息历史 */
  getMessages(sessionId) {
    return request.get(`/customer-service/messages/${encodeURIComponent(sessionId)}`)
  }
}
