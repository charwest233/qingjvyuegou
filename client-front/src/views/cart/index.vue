<template>
  <div class="page-container py-4">
    <div v-if="!isLoggedIn" class="text-center py-20">
      <ShoppingCart class="w-20 h-20 mx-auto text-gray-300 mb-4" />
      <p class="text-gray-400 mb-4">请先登录查看购物车</p>
      <router-link to="/login" class="btn-primary inline-block">去登录</router-link>
    </div>
    <template v-else>
      <h1 class="section-title">购物车</h1>

      <!-- 空购物车 -->
      <div v-if="cartStore.items.length === 0" class="text-center py-20">
        <ShoppingCart class="w-20 h-20 mx-auto text-gray-300 mb-4" />
        <p class="text-gray-400 mb-2">购物车是空的</p>
        <p class="text-sm text-gray-300 mb-6">快去挑选心仪的商品吧</p>
        <router-link to="/" class="btn-primary inline-block">去逛逛</router-link>
      </div>

      <!-- 购物车列表 -->
      <div v-else class="space-y-3">
        <CartItem
          v-for="item in cartStore.items"
          :key="item.productId || item.product_id"
          :item="item"
          @toggle-select="cartStore.toggleSelected(item.productId || item.product_id)"
          @increase="cartStore.add(item.productId || item.product_id, 1)"
          @decrease="cartStore.updateQuantity(item.productId || item.product_id, Math.max(1, item.quantity - 1))"
          @remove="handleRemove(item.productId || item.product_id)"
        />
      </div>
    </template>

    <!-- 底部操作栏 -->
    <div
      v-if="isLoggedIn && cartStore.items.length > 0"
      class="fixed bottom-16 left-0 right-0 bg-white border-t border-gray-100 p-4 z-40"
    >
      <div class="max-w-7xl mx-auto flex items-center justify-between">
        <div class="flex items-center gap-4">
          <button
            class="flex items-center gap-2 text-sm cursor-pointer"
            @click="cartStore.toggleAll()"
          >
            <div
              class="w-5 h-5 rounded border-2 flex items-center justify-center transition-all"
              :class="cartStore.isAllSelected
                ? 'bg-primary border-primary text-white'
                : 'border-gray-300'"
            >
              <Check v-if="cartStore.isAllSelected" class="w-3 h-3" :stroke-width="3" />
            </div>
            全选
          </button>
          <button
            class="text-sm text-gray-400 hover:text-functional-danger transition-colors cursor-pointer"
            @click="handleClear"
          >
            清空
          </button>
        </div>
        <div class="flex items-center gap-4">
          <div class="text-sm text-gray-500">
            合计：
            <span class="text-2xl font-bold text-functional-danger">¥{{ formatPrice(cartStore.selectedTotal) }}</span>
          </div>
          <button
            class="btn-cta !py-2.5 !px-8"
            :class="cartStore.selectedCount === 0 ? 'opacity-50 cursor-not-allowed' : ''"
            :disabled="cartStore.selectedCount === 0"
            @click="goCheckout"
          >
            结算 ({{ cartStore.selectedCount }})
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ShoppingCart, Check } from 'lucide-vue-next'
import { ElMessage, ElMessageBox } from 'element-plus'
import CartItem from '@/components/CartItem.vue'
import { useCartStore } from '@/stores/cart'
import { isLoggedIn } from '@/utils/auth'
import { formatPrice } from '@/utils/format'

const router = useRouter()
const cartStore = useCartStore()

async function handleRemove(productId) {
  try {
    await ElMessageBox.confirm('确定要删除该商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await cartStore.remove(productId)
    ElMessage.success('已删除')
  } catch {
    // 用户取消
  }
}

async function handleClear() {
  try {
    await ElMessageBox.confirm('确定要清空购物车吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await cartStore.clear()
    ElMessage.success('购物车已清空')
  } catch {
    // 用户取消
  }
}

function goCheckout() {
  if (cartStore.selectedCount > 0) {
    router.push({ name: 'Checkout' })
  }
}

onMounted(() => {
  cartStore.loadCart()
})
</script>
