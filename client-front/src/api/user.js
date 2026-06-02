import request from './request'

// 用户登录（微信小程序登录模式）
export function wxLogin(code) {
  return request.post('/user/wx-login', { code })
}

// 后台管理员登录（手机号密码）
export function adminLogin(username, password) {
  return request.post('/admin/login', { username, password })
}

// 前台用户登录（手机号/邮箱 + 密码）
export function userLogin(account, password) {
  return request.post('/user/login', { account, password })
}

// 前台用户注册（type=2 普通用户），需验证码
export function registerUser(phone, password, email, code) {
  return request.post('/user/register', { phone, password, email, code })
}

// 获取用户信息
export function getUserProfile() {
  return request.get('/user/profile')
}

// 更新用户信息
export function updateUserProfile(data) {
  return request.put('/user/profile', data)
}

// 获取地址列表
export function getUserAddresses() {
  return request.get('/user/addresses')
}

// 添加地址
export function addUserAddress(data) {
  return request.post('/user/addresses', data)
}

// 更新地址
export function updateUserAddress(id, data) {
  return request.put(`/user/addresses/${id}`, data)
}

// 删除地址
export function deleteUserAddress(id) {
  return request.delete(`/user/addresses/${id}`)
}

// 收藏列表
export function getFavorites() {
  return request.get('/user/favorites')
}

// 添加收藏
export function addFavorite(productId) {
  return request.post('/user/favorites', { productId })
}

// 删除收藏
export function removeFavorite(productId) {
  return request.delete(`/user/favorites/${productId}`)
}

// 更新手机号
export function updateUserPhone(phone) {
  return request.put('/user/phone', { phone })
}

// 更新邮箱
export function updateUserEmail(email) {
  return request.put('/user/email', { email })
}

// 设置为默认地址
export function setDefaultAddress(id) {
  return request.put(`/user/addresses/${id}/default`)
}
