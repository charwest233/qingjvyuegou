<template>
  <div class="page-container py-4">
    <!-- Banner轮播 -->
    <div class="relative rounded-2xl overflow-hidden shadow-lg mb-8">
      <div class="aspect-[21/9] md:aspect-[3/1] bg-gradient-to-r from-primary via-primary-light to-primary overflow-hidden">
        <div
          class="flex h-full transition-transform duration-500 ease-out"
          :style="{ transform: `translateX(-${currentBanner * 100}%)` }"
        >
          <div
            v-for="(banner, index) in banners"
            :key="index"
            class="min-w-full h-full flex items-center justify-center px-8"
          >
            <div class="text-center text-white">
              <h2 class="text-2xl md:text-4xl font-heading font-bold mb-2">{{ banner.title }}</h2>
              <p class="text-sm md:text-lg opacity-90">{{ banner.subtitle }}</p>
            </div>
          </div>
        </div>
        <!-- 轮播指示器 -->
        <div class="absolute bottom-4 left-1/2 -translate-x-1/2 flex gap-2">
          <button
            v-for="(_, index) in banners"
            :key="index"
            class="w-2 h-2 rounded-full transition-all duration-300 cursor-pointer"
            :class="currentBanner === index ? 'bg-white w-6' : 'bg-white/50'"
            @click="currentBanner = index"
          />
        </div>
      </div>
      <!-- 自动播放 -->
      <div class="absolute top-4 right-4 bg-black/20 text-white text-xs px-2 py-1 rounded-full">
        {{ currentBanner + 1 }} / {{ banners.length }}
      </div>
    </div>

    <!-- 搜索栏（移动端） -->
    <div class="md:hidden mb-6">
      <div class="relative">
        <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
        <input
          v-model="searchKeyword"
          type="text"
          placeholder="搜索商品..."
          class="w-full pl-10 pr-4 py-3 bg-white border border-gray-200 rounded-xl text-sm
                 focus:outline-none focus:border-primary transition-all shadow-sm"
          @keyup.enter="handleSearch"
        />
      </div>
    </div>

    <!-- 分类导航 -->
    <section class="mb-8">
      <h2 class="section-title">商品分类</h2>
      <div class="grid grid-cols-4 md:grid-cols-5 gap-4">
        <router-link
          v-for="cat in categories"
          :key="cat.id"
          :to="{ name: 'ProductList', query: { categoryId: cat.id } }"
          class="flex flex-col items-center gap-2 p-4 bg-white rounded-xl shadow-sm card-hover cursor-pointer group"
        >
          <div class="w-12 h-12 bg-gradient-to-br from-primary/10 to-primary/5 rounded-xl flex items-center justify-center
                      group-hover:from-primary/20 group-hover:to-primary/10 transition-all">
            <component :is="categoryIcons[cat.id % categoryIcons.length]" class="w-6 h-6 text-primary" />
          </div>
          <span class="text-sm font-medium text-gray-700">{{ cat.name }}</span>
        </router-link>
      </div>
    </section>

    <!-- 热门推荐 -->
    <section>
      <div class="flex items-center justify-between mb-6">
        <h2 class="section-title mb-0">热门推荐</h2>
        <router-link
          to="/product-list"
          class="text-sm text-primary hover:text-primary-dark transition-colors"
        >
          查看更多 →
        </router-link>
      </div>
      <div v-if="loading" class="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div v-for="n in 4" :key="n" class="bg-white rounded-xl overflow-hidden shadow-sm animate-pulse">
          <div class="aspect-square bg-gray-200" />
          <div class="p-3 space-y-2">
            <div class="h-4 bg-gray-200 rounded w-3/4" />
            <div class="h-4 bg-gray-200 rounded w-1/2" />
          </div>
        </div>
      </div>
      <div v-else class="grid grid-cols-2 md:grid-cols-4 gap-4">
        <ProductCard v-for="product in hotProducts" :key="product.id" :product="product" />
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Smartphone, Monitor, Wind, Palette, Apple, Heart, Coffee } from 'lucide-vue-next'
import ProductCard from '@/components/ProductCard.vue'
import { useProductStore } from '@/stores/product'

const router = useRouter()
const productStore = useProductStore()

const currentBanner = ref(0)
const searchKeyword = ref('')

const categoryIcons = [Smartphone, Monitor, Wind, Palette, Apple, Heart, Coffee, Smartphone]

const banners = [
  { title: '夏日狂欢购', subtitle: '全场低至5折，限时特惠' },
  { title: '新品首发', subtitle: '品质甄选，抢先体验' },
  { title: '品质生活', subtitle: '精选好物，悦享生活' }
]

const categories = ref([])
const hotProducts = ref([])
const loading = ref(true)

// 自动轮播
let bannerTimer
function startBannerAutoPlay() {
  bannerTimer = setInterval(() => {
    currentBanner.value = (currentBanner.value + 1) % banners.length
  }, 4000)
}

onMounted(async () => {
  startBannerAutoPlay()
  await productStore.loadCategories()
  await productStore.loadHotProducts()
  categories.value = productStore.categories
  hotProducts.value = productStore.hotProducts
  loading.value = false
})

function handleSearch() {
  if (searchKeyword.value.trim()) {
    router.push({ name: 'ProductList', query: { keyword: searchKeyword.value.trim() } })
  }
}
</script>
