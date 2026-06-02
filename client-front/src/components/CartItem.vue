<template>
  <div class="flex items-center gap-3 bg-white p-4 rounded-xl shadow-sm">
    <!-- 选择框 -->
    <button
      class="w-6 h-6 rounded-full border-2 flex items-center justify-center transition-all duration-200 cursor-pointer shrink-0"
      :class="item.selected
        ? 'bg-primary border-primary text-white'
        : 'border-gray-300 hover:border-primary'"
      @click="$emit('toggle-select')"
    >
      <Check v-if="item.selected" class="w-4 h-4" :stroke-width="3" />
    </button>

    <!-- 商品缩略图 -->
    <router-link :to="`/product/${item.productId}`" class="shrink-0">
      <div class="w-20 h-20 bg-gray-50 rounded-lg overflow-hidden">
        <img
          v-if="item.productImage"
          :src="item.productImage"
          :alt="item.productName"
          class="w-full h-full object-cover"
        />
        <div v-else class="w-full h-full flex items-center justify-center text-gray-300">
          <Package class="w-8 h-8" />
        </div>
      </div>
    </router-link>

    <!-- 商品信息 -->
    <div class="flex-1 min-w-0">
      <h3 class="text-sm font-medium text-gray-900 line-clamp-2">{{ item.productName }}</h3>
      <p class="text-lg font-bold text-functional-danger mt-1">¥{{ formatPrice(item.price) }}</p>
    </div>

    <!-- 数量选择器 -->
    <div class="flex items-center gap-2 shrink-0">
      <button
        class="w-8 h-8 rounded-full border border-gray-200 flex items-center justify-center
               hover:bg-gray-50 transition-colors cursor-pointer disabled:opacity-50"
        :disabled="item.quantity <= 1"
        @click="$emit('decrease')"
      >
        <Minus class="w-4 h-4" />
      </button>
      <span class="w-8 text-center text-sm font-medium">{{ item.quantity }}</span>
      <button
        class="w-8 h-8 rounded-full border border-gray-200 flex items-center justify-center
               hover:bg-gray-50 transition-colors cursor-pointer"
        :disabled="item.quantity >= 99"
        @click="$emit('increase')"
      >
        <Plus class="w-4 h-4" />
      </button>
    </div>

    <!-- 删除 -->
    <button
      class="p-2 text-gray-400 hover:text-functional-danger transition-colors cursor-pointer shrink-0"
      @click="$emit('remove')"
    >
      <Trash2 class="w-5 h-5" />
    </button>
  </div>
</template>

<script setup>
import { Package, Check, Minus, Plus, Trash2 } from 'lucide-vue-next'
import { formatPrice } from '@/utils/format'

defineProps({
  item: { type: Object, required: true }
})

defineEmits(['toggle-select', 'increase', 'decrease', 'remove'])
</script>
