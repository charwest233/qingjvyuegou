<template>
  <div class="page-container py-4">
    <!-- 标题 → 流动渐变 -->
    <div class="relative rounded-xl overflow-hidden mb-4">
      <div class="absolute inset-0 bg-gradient-to-r from-primary via-cyan-400 via-emerald-300 to-primary bg-[length:400%_100%] animate-gradient-flow opacity-20" />
      <div class="absolute inset-0">
        <div class="absolute top-0 left-1/3 w-20 h-20 bg-white/20 rounded-full -translate-y-1/2 blur-xl animate-pulse-slow" />
        <div class="absolute bottom-0 right-1/4 w-16 h-16 bg-white/10 rounded-full blur-xl animate-pulse-slow" style="animation-delay: 1.5s" />
      </div>
      <div class="relative px-4 py-3">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 rounded-xl bg-white/60 backdrop-blur-sm flex items-center justify-center shadow-sm">
            <ShoppingCart class="w-5 h-5 text-primary" />
          </div>
          <div>
            <h1 class="text-lg font-heading font-bold text-gray-800">购物车</h1>
            <p v-if="cartStore.items.length > 0" class="text-xs text-gray-500">{{ cartStore.items.length }} 件商品</p>
          </div>
        </div>
      </div>
    </div>

    <div v-if="!isLoggedIn" class="text-center py-20">
      <div class="inline-flex items-center justify-center w-20 h-20 bg-gray-50 rounded-2xl mb-4">
        <ShoppingCart class="w-10 h-10 text-gray-300" />
      </div>
      <p class="text-gray-400 mb-4">请先登录查看购物车</p>
      <router-link to="/login" class="btn-primary inline-block">去登录</router-link>
    </div>

    <template v-else>
      <!-- 空购物车 -->
      <div v-if="cartStore.items.length === 0" class="text-center py-20">
        <div class="inline-flex items-center justify-center w-24 h-24 bg-gradient-to-br from-primary/10 to-primary/5 rounded-3xl mb-4 animate-bounce-in">
          <ShoppingCart class="w-12 h-12 text-primary/60" />
        </div>
        <p class="text-gray-500 font-medium mb-2">购物车是空的</p>
        <p class="text-sm text-gray-300 mb-6">快去挑选心仪的商品吧</p>
        <router-link to="/" class="btn-primary inline-block shadow-lg shadow-primary/20">去逛逛</router-link>
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
      class="fixed bottom-16 left-0 right-0 bg-white/80 backdrop-blur-md border-t border-gray-100 p-4 z-40 shadow-[0_-4px_20px_rgba(0,0,0,0.05)]"
    >
      <div class="max-w-7xl mx-auto flex items-center justify-between">
        <div class="flex items-center gap-4">
          <button
            class="flex items-center gap-2 text-sm cursor-pointer"
            @click="cartStore.toggleAll()"
          >
            <div
              class="w-5 h-5 rounded border-2 flex items-center justify-center transition-all duration-200"
              :class="cartStore.isAllSelected
                ? 'bg-primary border-primary text-white shadow-sm shadow-primary/30'
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
            <span class="text-2xl font-bold text-functional-danger">{{ formatPrice(cartStore.selectedTotal) }}</span>
          </div>
          <button
            class="btn-cta !py-2.5 !px-8 relative overflow-hidden group"
            :class="cartStore.selectedCount === 0 ? 'opacity-50 cursor-not-allowed' : ''"
            :disabled="cartStore.selectedCount === 0"
            @click="goCheckout"
          >
            <span class="relative z-10">结算 ({{ cartStore.selectedCount }})</span>
            <div v-if="cartStore.selectedCount > 0" class="absolute inset-0 bg-gradient-to-r from-transparent via-white/20 to-transparent -translate-x-full group-hover:translate-x-full transition-transform duration-700" />
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

<style scoped>
@keyframes gradient-flow {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}
.animate-gradient-flow {
  animation: gradient-flow 6s ease infinite;
}
@keyframes pulse-slow {
  0%, 100% { opacity: 0.4; transform: scale(1); }
  50% { opacity: 0.8; transform: scale(1.15); }
}
.animate-pulse-slow {
  animation: pulse-slow 4s ease-in-out infinite;
}
@keyframes bounce-in {
  0% { opacity: 0; transform: scale(0.3); }
  50% { transform: scale(1.1); }
  70% { transform: scale(0.95); }
  100% { opacity: 1; transform: scale(1); }
}
.animate-bounce-in {
  animation: bounce-in 0.5s ease-out both;
}
</style>
