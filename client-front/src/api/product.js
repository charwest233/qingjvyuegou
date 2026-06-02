import request from './request'

// 商品接口
export function getProducts(params) {
  return request.get('/product/list', { params })
}

export function getProductDetail(id) {
  return request.get(`/product/${id}`)
}

export function getHotProducts() {
  return request.get('/product/hot')
}

// 分类接口
export function getCategories() {
  return request.get('/category/list')
}

// RAG 模糊搜索（自然语言智能推荐）
export function getRagSearch(query, limit = 8) {
  return request.get('/product/rag-search', { params: { q: query, limit } })
}
