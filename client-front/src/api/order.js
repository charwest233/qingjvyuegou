import request from './request'

// 创建订单
export function createOrder(data) {
  return request.post('/order', data)
}

// 获取订单列表
export function getOrders(params) {
  return request.get('/order/list', { params })
}

// 获取订单详情
export function getOrderDetail(id) {
  return request.get(`/order/${id}`)
}

// 支付订单（使用订单ID，不是订单号）
export function payOrder(id) {
  return request.put(`/order/${id}/pay`)
}

// 取消订单
export function cancelOrder(id) {
  return request.put(`/order/${id}/cancel`)
}

// 确认收货
export function confirmOrder(id) {
  return request.put(`/order/${id}/confirm`)
}

// 删除订单（已完成/已取消）
export function deleteOrder(id) {
  return request.delete(`/order/${id}`)
}
