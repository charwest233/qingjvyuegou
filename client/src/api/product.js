// ============================================
// 商品管理 API
// ============================================

import request from '@/utils/request'

export const productApi = {
  // 获取商品列表
  getList(params) {
    return request.get('/product/list', { params })
  },

  // 获取商品详情
  getById(id) {
    return request.get(`/product/${id}`)
  },

  // 创建商品
  create(data) {
    return request.post('/product', data)
  },

  // 更新商品
  update(id, data) {
    return request.put(`/product/${id}`, data)
  },

  // 删除商品
  delete(id) {
    return request.delete(`/product/${id}`)
  },

  // 更新商品状态
  updateStatus(id, status) {
    return request.put(`/product/${id}/status`, { status })
  },

  // 上传商品图片
  uploadImage(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/upload/image', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  }
}
