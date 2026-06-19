<template>
  <div class="page-container py-4">
    <!-- 分类Tab -->
    <div class="flex gap-2 overflow-x-auto pb-2 mb-4 scrollbar-hide">
      <button
        v-for="cat in allCategories"
        :key="cat.id"
        class="px-4 py-2 rounded-full text-sm font-medium whitespace-nowrap transition-all duration-200 cursor-pointer"
        :class="currentCategory === cat.id
          ? 'bg-primary text-white shadow-md'
          : 'bg-white text-gray-600 hover:bg-primary/10 border border-gray-200'"
        @click="switchCategory(cat.id)"
      >
        {{ cat.name }}
      </button>
    </div>

    <!-- 筛选排序栏 -->
    <div class="flex items-center justify-between mb-4">
      <div class="flex gap-2">
        <button
          v-for="sort in sortOptions"
          :key="sort.key"
          class="text-sm px-3 py-1.5 rounded-lg transition-colors cursor-pointer"
          :class="currentSort === sort.key
            ? 'bg-primary/10 text-primary font-medium'
            : 'text-gray-500 hover:text-gray-700'"
          @click="currentSort = sort.key"
        >
          {{ sort.label }}
        </button>
      </div>
      <div class="flex items-center gap-2">
        <button
          class="p-2 rounded-lg hover:bg-gray-100 transition-colors cursor-pointer"
          :class="viewMode === 'grid' ? 'text-primary' : 'text-gray-400'"
          @click="viewMode = 'grid'"
        >
          <Grid class="w-5 h-5" />
        </button>
        <button
          class="p-2 rounded-lg hover:bg-gray-100 transition-colors cursor-pointer"
          :class="viewMode === 'list' ? 'text-primary' : 'text-gray-400'"
          @click="viewMode = 'list'"
        >
          <List class="w-5 h-5" />
        </button>
      </div>
    </div>

    <!-- 商品列表 -->
    <div v-if="loading" class="grid grid-cols-2 md:grid-cols-4 gap-4">
      <div v-for="n in 8" :key="n" class="bg-white rounded-xl overflow-hidden shadow-sm animate-pulse">
        <div class="aspect-square bg-gray-200" />
        <div class="p-3 space-y-2">
          <div class="h-4 bg-gray-200 rounded w-3/4" />
          <div class="h-4 bg-gray-200 rounded w-1/2" />
        </div>
      </div>
    </div>
    <div v-else-if="products.length === 0" class="text-center py-20">
      <Package class="w-16 h-16 mx-auto text-gray-300 mb-4" />
      <p class="text-gray-400">暂无商品</p>
    </div>
    <template v-else>
      <!-- 商品网格 -->
      <div
        :class="viewMode === 'grid'
          ? 'grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4'
          : 'space-y-4'"
      >
        <ProductCard
          v-for="product in products"
          :key="product.id"
          :product="product"
          :list-mode="viewMode === 'list'"
        />
      </div>

      <!-- 底部翻页 -->
      <div v-if="totalPages > 1" class="flex justify-center mt-6 mb-4">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          :pager-count="5"
          background
          small
          @current-change="loadProducts"
        />
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { Grid, List, Package } from 'lucide-vue-next'
import ProductCard from '@/components/ProductCard.vue'
import { useProductStore } from '@/stores/product'

const route = useRoute()
const productStore = useProductStore()

const allCategories = ref([{ id: null, name: '全部' }])
const currentCategory = ref(null)
const currentSort = ref('default')
const viewMode = ref('grid')
const products = ref([])
const loading = ref(true)
const currentPage = ref(1)
const pageSize = ref(8)
const total = ref(0)
const totalPages = ref(0)

const sortOptions = [
  { key: 'default', label: '综合' },
  { key: 'price_asc', label: '价格↑' },
  { key: 'price_desc', label: '价格↓' },
  { key: 'sales', label: '销量' }
]

async function loadProducts() {
  loading.value = true
  const params = {
    page: currentPage.value,
    size: pageSize.value,
    categoryId: currentCategory.value,
    sort: currentSort.value !== 'default' ? currentSort.value : undefined
  }
  if (route.query.keyword) {
    params.name = route.query.keyword
  }
  const res = await productStore.getProductList(params)
  if (res?.code === 200) {
    products.value = res.data?.list || res.data?.records || res.data || []
    total.value = res.data?.total || 0
    totalPages.value = res.data?.pages || Math.ceil(total.value / pageSize.value) || 0
  }
  loading.value = false
}

function switchCategory(id) {
  currentCategory.value = id
  currentPage.value = 1
  loadProducts()
}

onMounted(async () => {
  await productStore.loadCategories()
  allCategories.value = [{ id: null, name: '全部' }, ...productStore.categories]

  if (route.query.categoryId) {
    currentCategory.value = Number(route.query.categoryId)
  }
  if (route.query.keyword) {
    currentSort.value = 'default'
  }
  loadProducts()
})

watch(() => currentSort.value, () => {
  currentPage.value = 1
  loadProducts()
})

watch(() => route.query, () => {
  if (route.query.keyword) {
    loadProducts()
  }
})
</script>
