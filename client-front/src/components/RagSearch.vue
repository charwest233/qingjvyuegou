<template>
  <div class="bg-white rounded-2xl shadow-sm overflow-hidden">
    <!-- 搜索头部 -->
    <div class="p-4 border-b border-gray-100">
      <h3 class="text-sm font-medium text-gray-700 mb-3 flex items-center gap-2">
        <Sparkles class="w-4 h-4 text-primary" />
        AI 智能推荐
        <span class="text-[10px] text-gray-400 font-normal">输入自然语言描述，AI 帮你找商品</span>
      </h3>
      <div class="flex gap-2">
        <div class="flex-1 relative">
          <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
          <input
            v-model="searchText"
            type="text"
            placeholder="试试输入「适合办公的笔记本」或「千元左右的手机」..."
            class="w-full pl-9 pr-3 py-2.5 bg-gray-50 border border-gray-200 rounded-xl text-sm
                   focus:outline-none focus:border-primary focus:bg-white transition-all"
            @keyup.enter="doSearch"
            @input="debouncedSearch"
          />
        </div>
        <button
          class="px-4 py-2.5 bg-primary text-white rounded-xl text-sm font-medium
                 hover:bg-primary-dark transition-colors disabled:opacity-50 cursor-pointer shrink-0"
          :disabled="searching || !searchText.trim()"
          @click="doSearch"
        >
          {{ searching ? '搜索中...' : '搜索' }}
        </button>
      </div>
    </div>

    <!-- 搜索结果 -->
    <div v-if="results.length > 0" class="p-4 pt-0">
      <div class="flex items-center justify-between mb-3 pt-3 border-t border-gray-50">
        <h4 class="text-xs text-gray-500">
          <template v-if="isFallback">
            <span class="text-amber-500 font-medium">未找到精确匹配</span>，自动推荐热门产品
          </template>
          <template v-else>
            找到 <span class="text-primary font-medium">{{ results.length }}</span> 件相关商品
          </template>
        </h4>
        <button
          class="text-xs text-primary hover:text-primary-dark transition-colors cursor-pointer shrink-0"
          @click="expanded = !expanded"
        >
          {{ expanded ? '收起' : '展开全部' }}
        </button>
      </div>
      <div
        :class="expanded
          ? 'grid grid-cols-2 md:grid-cols-4 gap-3'
          : 'flex gap-3 overflow-x-auto pb-2 scrollbar-hide'"
      >
        <div
          v-for="item in displayItems"
          :key="item.id"
          class="bg-white rounded-xl border border-gray-100 overflow-hidden hover:shadow-md transition-all shrink-0"
          :class="expanded ? '' : 'w-36'"
        >
          <router-link :to="`/product/${item.id}`" class="block">
            <div class="aspect-square bg-gray-50 relative overflow-hidden">
              <img
                v-if="item.mainImage"
                :src="item.mainImage"
                :alt="item.name"
                class="w-full h-full object-cover"
              />
              <div v-else class="w-full h-full flex items-center justify-center text-gray-300">
                <Package class="w-8 h-8" />
              </div>
              <!-- 相似度标签 -->
              <div
                v-if="item.similarity > 0"
                class="absolute top-1 left-1 bg-primary/80 text-white text-[10px] px-1.5 py-0.5 rounded font-medium"
              >
                {{ Math.round(item.similarity * 100) }}% 匹配
              </div>
            </div>
            <div class="p-2">
              <p class="text-xs text-gray-900 line-clamp-2 leading-tight mb-1">{{ item.name }}</p>
              <p class="text-sm font-bold text-functional-danger">¥{{ formatPrice(item.price) }}</p>
            </div>
          </router-link>
        </div>
      </div>
    </div>

    <!-- 无结果 -->
    <div v-else-if="searched && !searching" class="p-4 pt-2 text-center text-gray-400 text-sm">
      <SearchX class="w-10 h-10 mx-auto mb-2 text-gray-300" />
      <p>没有找到匹配的商品，换个关键词试试</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Search, Sparkles, Package, SearchX } from 'lucide-vue-next'
import { formatPrice } from '@/utils/format'
import { getRagSearch } from '@/api/product'

const searchText = ref('')
const results = ref([])
const searching = ref(false)
const searched = ref(false)
const expanded = ref(false)

let debounceTimer = null

const displayItems = computed(() => {
  if (expanded.value) return results.value
  return results.value.slice(0, 4)
})

// 检查是否为兜底推荐（自动推荐热门产品）
const isFallback = computed(() => {
  return results.value.length > 0 && results.value.every(item => item.isFallback === true)
})

function debouncedSearch() {
  clearTimeout(debounceTimer)
  if (!searchText.value.trim()) return
  debounceTimer = setTimeout(() => {
    doSearch()
  }, 600)
}

async function doSearch() {
  const q = searchText.value.trim()
  if (!q) return

  searching.value = true
  expanded.value = false
  try {
    const res = await getRagSearch(q)
    if (res.code === 200) {
      results.value = res.data || []
      searched.value = true
    }
  } catch (err) {
    console.error('RAG搜索失败:', err)
  } finally {
    searching.value = false
  }
}
</script>
