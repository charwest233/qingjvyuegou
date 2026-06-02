// ============================================
// 认证相关 API
// ============================================

import request from '@/utils/request'

export const authApi = {
  // 管理员登录
  login(data) {
    return request.post('/admin/login', data)
  },

  // 获取当前管理员信息
  getInfo() {
    return request.get('/admin/info')
  },

  // 修改密码
  changePassword(data) {
    return request.post('/admin/change-password', data)
  }
}
