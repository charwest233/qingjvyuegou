import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getCategories } from '@/api/category'
import { getProducts, getHotProducts } from '@/api/product'

export const useProductStore = defineStore('product', () => {
  // 分类列表
  const categories = ref([])
  // 热门商品
  const hotProducts = ref([])
  // 加载状态
  const loading = ref(false)

  // 加载分类
  async function loadCategories() {
    try {
      const res = await getCategories()
      if (res.code === 200) {
        categories.value = res.data || []
      }
    } catch (err) {
      console.error('加载分类失败:', err)
    }
  }

  // 加载热门商品
  async function loadHotProducts() {
    try {
      const res = await getHotProducts()
      if (res.code === 200) {
        hotProducts.value = res.data || []
      }
    } catch (err) {
      console.error('加载热门商品失败:', err)
    }
  }

  // 获取商品列表
  async function getProductList(params) {
    loading.value = true
    try {
      const res = await getProducts(params)
      return res
    } catch (err) {
      console.error('加载商品列表失败:', err)
      return null
    } finally {
      loading.value = false
    }
  }

  return {
    categories,
    hotProducts,
    loading,
    loadCategories,
    loadHotProducts,
    getProductList
  }
})
