// ============================================
// 订单管理 API
// ============================================

import request from '@/utils/request'

export const orderApi = {
  // 获取订单列表
  getList(params) {
    return request.get('/order/list', { params })
  },

  // 获取订单详情
  getById(id) {
    return request.get(`/order/${id}`)
  },

  // 更新订单状态
  updateStatus(id, status) {
    return request.put(`/order/${id}/status`, { status })
  },

  // 发货
  ship(id, data) {
    return request.put(`/order/${id}/ship`, data)
  },

  // 取消订单
  cancel(id) {
    return request.put(`/order/${id}/cancel`)
  },

  // 获取订单统计数据
  getStatistics() {
    return request.get('/order/statistics')
  }
}
