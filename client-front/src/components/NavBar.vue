<template>
  <nav class="fixed top-0 left-0 right-0 z-50 bg-white/90 backdrop-blur-md border-b border-gray-100">
    <div class="max-w-7xl mx-auto px-4 h-16 flex items-center justify-between">
      <!-- Logo -->
      <router-link to="/" class="flex items-center gap-2">
        <div class="w-8 h-8 bg-gradient-to-br from-primary to-primary-light rounded-lg flex items-center justify-center">
          <ShoppingBag class="w-5 h-5 text-white" />
        </div>
        <span class="text-xl font-heading font-bold text-primary">悦选商城</span>
      </router-link>

      <!-- 搜索框 -->
      <div class="hidden md:flex flex-1 max-w-md mx-8">
        <div class="relative w-full">
          <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="搜索商品..."
            class="w-full pl-10 pr-4 py-2 bg-gray-50 border border-gray-200 rounded-full text-sm
                   focus:outline-none focus:border-primary focus:bg-white transition-all"
            @keyup.enter="handleSearch"
          />
        </div>
      </div>

      <!-- 右侧操作 -->
      <div class="flex items-center gap-4">
        <!-- 搜索按钮（移动端） -->
        <button class="md:hidden p-2 hover:bg-gray-100 rounded-lg cursor-pointer transition-colors">
          <Search class="w-5 h-5 text-gray-600" />
        </button>

        <!-- 购物车 -->
        <router-link to="/cart" class="relative p-2 hover:bg-gray-100 rounded-lg transition-colors">
          <ShoppingCart class="w-5 h-5 text-gray-600" />
          <span v-if="cartCount > 0" class="absolute -top-1 -right-1 w-5 h-5 bg-functional-danger text-white text-xs
                    rounded-full flex items-center justify-center font-medium">
            {{ cartCount > 99 ? '99+' : cartCount }}
          </span>
        </router-link>

        <!-- 用户 -->
        <router-link
          v-if="isLoggedIn"
          to="/user"
          class="flex items-center gap-2 p-2 hover:bg-gray-100 rounded-lg transition-colors"
        >
          <User class="w-5 h-5 text-gray-600" />
          <span class="hidden sm:inline text-sm text-gray-700">{{ user?.nickname || '用户' }}</span>
        </router-link>
        <router-link
          v-else
          to="/login"
          class="btn-primary text-sm !px-4 !py-2"
        >
          登录
        </router-link>
      </div>
    </div>
  </nav>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ShoppingBag, Search, ShoppingCart, User } from 'lucide-vue-next'
import { isLoggedIn, getUser } from '@/utils/auth'
import { useCartStore } from '@/stores/cart'

const router = useRouter()
const cartStore = useCartStore()
const searchKeyword = ref('')

const user = computed(() => getUser())
const cartCount = computed(() => cartStore.totalCount)

function handleSearch() {
  if (searchKeyword.value.trim()) {
    router.push({ name: 'ProductList', query: { keyword: searchKeyword.value.trim() } })
  }
}
</script>
