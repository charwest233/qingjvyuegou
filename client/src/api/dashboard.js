// ============================================
// 仪表盘数据 API
// ============================================

import request from '@/utils/request'

export const dashboardApi = {
  // 获取仪表盘统计数据
  getStatistics() {
    return request.get('/dashboard/statistics')
  },

  // 获取销售趋势
  getSalesTrend(days = 7) {
    return request.get('/dashboard/sales-trend', { params: { days } })
  },

  // 获取热门商品
  getHotProducts(limit = 10) {
    return request.get('/dashboard/hot-products', { params: { limit } })
  },

  // 获取最新订单
  getRecentOrders(limit = 10) {
    return request.get('/dashboard/recent-orders', { params: { limit } })
  }
}
