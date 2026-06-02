import request from './request'

// 获取购物车列表
export function getCartList() {
  return request.get('/cart/list')
}

// 添加商品到购物车
export function addToCart(productId, quantity) {
  return request.post('/cart/add', { productId, quantity })
}

// 更新购物车商品数量
export function updateCartItem(productId, quantity) {
  return request.put('/cart/update', { productId, quantity })
}

// 删除购物车商品
export function removeCartItem(productId) {
  return request.delete(`/cart/remove/${productId}`)
}

// 清空购物车
export function clearCart() {
  return request.delete('/cart/clear')
}

// 选中/取消选中
export function toggleCartItem(productId, selected) {
  return request.put('/cart/toggle', { productId, selected })
}

// 全选/取消全选
export function toggleCartAll(selected) {
  return request.put('/cart/toggle-all', { selected })
}
