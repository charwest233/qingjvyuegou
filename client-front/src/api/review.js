import request from './request'

/**
 * 提交评价（批量）
 * @param {Array} reviews - [{ orderItemId, productId, rating, content }]
 */
export function submitReviews(reviews) {
  return request({
    url: '/review',
    method: 'POST',
    data: { reviews }
  })
}

/**
 * 查询商品评价（公开）
 * @param {number} productId
 * @param {number} page
 * @param {number} size
 */
export function getProductReviews(productId, page = 1, size = 10) {
  return request({
    url: `/review/product/${productId}`,
    method: 'GET',
    params: { page, size }
  })
}

/**
 * 查询某订单已评价的 orderItemId 列表
 * @param {number} orderId
 */
export function getOrderReviews(orderId) {
  return request({
    url: `/review/order/${orderId}`,
    method: 'GET'
  })
}

/**
 * 管理端分页查询评价
 * @param {string} keyword - 搜索关键词
 * @param {number} page
 * @param {number} size
 */
export function getReviewListForAdmin(keyword = '', page = 1, size = 10) {
  return request({
    url: '/review/list',
    method: 'GET',
    params: { keyword, page, size }
  })
}

/**
 * 管理端删除评价
 * @param {number} id
 */
export function deleteReview(id) {
  return request({
    url: `/review/${id}`,
    method: 'DELETE'
  })
}

/**
 * 查询当前用户的评价列表
 * @param {number} page
 * @param {number} size
 */
export function getUserReviews(page = 1, size = 10) {
  return request({
    url: '/review/user',
    method: 'GET',
    params: { page, size }
  })
}

/**
 * 用户删除自己的评价
 * @param {number} id
 */
export function deleteUserReview(id) {
  return request({
    url: `/review/${id}`,
    method: 'DELETE'
  })
}
