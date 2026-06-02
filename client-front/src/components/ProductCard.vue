<template>
  <router-link
    :to="`/product/${product.id}`"
    class="block bg-white rounded-xl overflow-hidden shadow-sm card-hover group cursor-pointer"
    :class="listMode ? 'flex flex-row' : ''"
  >
    <!-- 商品图片 -->
    <div
      :class="listMode
        ? 'w-24 h-24 shrink-0 bg-gray-50 relative overflow-hidden'
        : 'aspect-square bg-gray-50 relative overflow-hidden'"
    >
      <img
        v-if="product.mainImage"
        :src="product.mainImage"
        :alt="product.name"
        class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
      />
      <div v-else class="w-full h-full flex items-center justify-center text-gray-300">
        <Package class="w-12 h-12" />
      </div>
    </div>

    <!-- 商品信息 -->
    <div :class="listMode ? 'flex-1 p-3 flex flex-col justify-center' : 'p-3'">
      <h3 class="text-sm font-medium text-gray-900 line-clamp-2 mb-2 leading-tight">
        {{ product.name }}
      </h3>
      <div class="flex items-center justify-between">
        <div class="flex items-baseline gap-1">
          <span class="text-xs text-gray-400">¥</span>
          <span class="text-lg font-bold text-functional-danger">{{ formatPrice(product.price) }}</span>
        </div>
        <span class="text-xs text-gray-400">已售 {{ product.salesCount || 0 }}</span>
      </div>
    </div>
  </router-link>
</template>

<script setup>
import { Package } from 'lucide-vue-next'
import { formatPrice } from '@/utils/format'

defineProps({
  product: {
    type: Object,
    required: true
  },
  listMode: {
    type: Boolean,
    default: false
  }
})
</script>
