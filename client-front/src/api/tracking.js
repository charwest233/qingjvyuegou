import request from './request'

export function getOrderTracking(orderId) {
  return request.get(`/order/${orderId}/tracking`)
}
