import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getCartList, addToCart, updateCartItem, removeCartItem, clearCart, toggleCartItem, toggleCartAll } from '@/api/cart'
import { isLoggedIn } from '@/utils/auth'

export const useCartStore = defineStore('cart', () => {
  // 购物车项列表
  const items = ref([])

  // 总数量
  const totalCount = computed(() => {
    return items.value.reduce((sum, item) => sum + item.quantity, 0)
  })

  // 选中项数量
  const selectedCount = computed(() => {
    return items.value.filter(item => item.selected === 1).reduce((sum, item) => sum + item.quantity, 0)
  })

  // 选中项总金额
  const selectedTotal = computed(() => {
    return items.value
      .filter(item => item.selected === 1)
      .reduce((sum, item) => sum + item.price * item.quantity, 0)
  })

  // 是否全选
  const isAllSelected = computed(() => {
    if (items.value.length === 0) return false
    return items.value.every(item => item.selected === 1)
  })

  // 加载购物车
  async function loadCart() {
    if (!isLoggedIn()) return
    try {
      const res = await getCartList()
      if (res.code === 200) {
        items.value = res.data || []
      }
    } catch (err) {
      console.error('加载购物车失败:', err)
    }
  }

  // 添加到购物车
  async function add(productId, quantity = 1) {
    if (!isLoggedIn()) return false
    try {
      const res = await addToCart(productId, quantity)
      if (res.code === 200) {
        await loadCart()
        return true
      }
      return false
    } catch (err) {
      console.error('添加购物车失败:', err)
      return false
    }
  }

  // 更新数量
  async function updateQuantity(productId, quantity) {
    try {
      const res = await updateCartItem(productId, quantity)
      if (res.code === 200) {
        const item = items.value.find(i => i.productId === productId)
        if (item) item.quantity = quantity
      }
    } catch (err) {
      console.error('更新数量失败:', err)
    }
  }

  // 删除商品
  async function remove(productId) {
    try {
      const res = await removeCartItem(productId)
      if (res.code === 200) {
        items.value = items.value.filter(i => i.productId !== productId)
      }
    } catch (err) {
      console.error('删除购物车商品失败:', err)
    }
  }

  // 切换选中
  async function toggleSelected(productId) {
    const item = items.value.find(i => i.productId === productId)
    if (!item) return
    try {
      const newSelected = item.selected === 1 ? 0 : 1
      await toggleCartItem(productId, newSelected === 1)
      item.selected = newSelected
    } catch (err) {
      console.error('切换选中失败:', err)
    }
  }

  // 全选/取消全选
  async function toggleAll() {
    const newSelected = !isAllSelected.value
    try {
      await toggleCartAll(newSelected)
      items.value.forEach(item => { item.selected = newSelected ? 1 : 0 })
    } catch (err) {
      console.error('全选操作失败:', err)
    }
  }

  // 清空购物车
  async function clear() {
    try {
      const res = await clearCart()
      if (res.code === 200) {
        items.value = []
      }
    } catch (err) {
      console.error('清空购物车失败:', err)
    }
  }

  return {
    items,
    totalCount,
    selectedCount,
    selectedTotal,
    isAllSelected,
    loadCart,
    add,
    updateQuantity,
    remove,
    toggleSelected,
    toggleAll,
    clear
  }
})
