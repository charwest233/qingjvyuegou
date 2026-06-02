// ============================================
// 分类管理 API
// ============================================

import request from '@/utils/request'

export const categoryApi = {
  // 获取分类列表
  getList() {
    return request.get('/category/list')
  },

  // 获取分类树
  getTree() {
    return request.get('/category/tree')
  },

  // 创建分类
  create(data) {
    return request.post('/category', data)
  },

  // 更新分类
  update(id, data) {
    return request.put(`/category/${id}`, data)
  },

  // 删除分类
  delete(id) {
    return request.delete(`/category/${id}`)
  },

  // 更新分类排序
  updateSort(id, sort) {
    return request.put(`/category/${id}/sort`, { sort })
  }
}
