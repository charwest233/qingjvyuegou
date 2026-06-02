<template>
  <nav class="fixed bottom-0 left-0 right-0 z-50 bg-white border-t border-gray-100">
    <div class="max-w-lg mx-auto flex items-center justify-around h-16">
      <router-link
        v-for="tab in tabs"
        :key="tab.path"
        :to="tab.path"
        class="flex flex-col items-center justify-center flex-1 h-full gap-1
               transition-colors duration-200 group"
        :class="isActive(tab.path) ? 'text-primary' : 'text-gray-400 hover:text-gray-600'"
      >
        <component :is="tab.icon" class="w-6 h-6" :stroke-width="isActive(tab.path) ? 2.5 : 2" />
        <span class="text-xs font-medium">{{ tab.label }}</span>
        <!-- 活跃指示器 -->
        <div
          v-if="isActive(tab.path)"
          class="absolute -top-0.5 w-8 h-0.5 bg-primary rounded-full"
        />
      </router-link>
    </div>
  </nav>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { Home, Grid3X3, ShoppingCart, User } from 'lucide-vue-next'

const route = useRoute()

const tabs = [
  { path: '/', label: '首页', icon: Home },
  { path: '/product-list', label: '分类', icon: Grid3X3 },
  { path: '/cart', label: '购物车', icon: ShoppingCart },
  { path: '/user', label: '我的', icon: User }
]

function isActive(path) {
  if (path === '/') return route.path === '/'
  return route.path.startsWith(path)
}
</script>
