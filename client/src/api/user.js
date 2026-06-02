// ============================================
// 用户管理 API
// ============================================

import request from '@/utils/request'

export const userApi = {
  // 获取用户列表
  getList(params) {
    return request.get('/user/list', { params })
  },

  // 获取用户详情
  getById(id) {
    return request.get(`/user/${id}`)
  },

  // 更新用户状态
  updateStatus(id, status) {
    return request.put(`/user/${id}/status`, { status })
  },

  // 获取用户统计数据
  getStatistics() {
    return request.get('/user/statistics')
  }
}
