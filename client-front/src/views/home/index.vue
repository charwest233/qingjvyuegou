<template>
  <div class="page-container py-4">
    <!-- Banner轮播 - 华丽版 -->
    <div class="relative rounded-2xl overflow-hidden shadow-lg mb-8 group">
      <div class="aspect-[21/9] md:aspect-[3/1] overflow-hidden relative">
        <!-- 流动渐变背景层：青桔夏日配色 -->
        <div class="absolute inset-0 bg-gradient-to-r from-primary via-cyan-400 via-emerald-300 to-primary bg-[length:400%_100%] animate-gradient-flow" />
        
        <!-- 光晕装饰 -->
        <div class="absolute inset-0">
          <div class="absolute top-1/4 left-1/4 w-32 h-32 md:w-48 md:h-48 bg-white/15 rounded-full blur-3xl animate-pulse-slow" />
          <div class="absolute bottom-1/4 right-1/4 w-24 h-24 md:w-36 md:h-36 bg-white/10 rounded-full blur-2xl animate-pulse-slow" style="animation-delay: 1.5s" />
          <div class="absolute top-1/3 right-1/3 w-16 h-16 md:w-24 md:h-24 bg-white/10 rounded-full blur-2xl animate-pulse-slow" style="animation-delay: 0.8s" />
        </div>

        <!-- 闪烁粒子 -->
        <div class="absolute inset-0 overflow-hidden pointer-events-none">
          <div v-for="n in 12" :key="n" 
            class="absolute w-1 h-1 bg-white/60 rounded-full animate-float-particle"
            :style="{
              left: `${Math.random() * 100}%`,
              top: `${Math.random() * 100}%`,
              animationDelay: `${Math.random() * 5}s`,
              animationDuration: `${3 + Math.random() * 4}s`,
              width: `${2 + Math.random() * 3}px`,
              height: `${2 + Math.random() * 3}px`,
              opacity: 0.3 + Math.random() * 0.5
            }"
          />
        </div>

        <!-- 内容层 -->
        <div
          class="flex h-full transition-transform duration-500 ease-out relative z-10"
          :style="{ transform: `translateX(-${currentBanner * 100}%)` }"
        >
          <div
            v-for="(banner, index) in banners"
            :key="index"
            class="min-w-full h-full flex items-center justify-center px-8"
          >
            <div class="text-center text-white">
              <h2 class="text-2xl md:text-4xl font-heading font-bold mb-2 animate-slide-up" :style="{ animationDelay: '0.1s' }">{{ banner.title }}</h2>
              <p class="text-sm md:text-lg opacity-90 animate-slide-up" :style="{ animationDelay: '0.3s' }">{{ banner.subtitle }}</p>
            </div>
          </div>
        </div>
        
        <!-- 左右箭头 -->
        <button 
          class="absolute left-3 top-1/2 -translate-y-1/2 w-9 h-9 md:w-11 md:h-11 bg-white/20 backdrop-blur-sm rounded-full flex items-center justify-center text-white opacity-0 group-hover:opacity-100 transition-all duration-300 hover:bg-white/30 z-20 cursor-pointer"
          @click="prevBanner"
        >
          <ChevronLeft class="w-5 h-5" />
        </button>
        <button 
          class="absolute right-3 top-1/2 -translate-y-1/2 w-9 h-9 md:w-11 md:h-11 bg-white/20 backdrop-blur-sm rounded-full flex items-center justify-center text-white opacity-0 group-hover:opacity-100 transition-all duration-300 hover:bg-white/30 z-20 cursor-pointer"
          @click="nextBanner"
        >
          <ChevronRight class="w-5 h-5" />
        </button>
      </div>
      <!-- 轮播指示器 -->
      <div class="absolute bottom-4 left-1/2 -translate-x-1/2 flex gap-2 z-20">
        <button
          v-for="(_, index) in banners"
          :key="index"
          class="h-2 rounded-full transition-all duration-300 cursor-pointer"
          :class="currentBanner === index 
            ? 'bg-white w-6 shadow-lg' 
            : 'bg-white/40 w-2 hover:bg-white/60'"
          @click="currentBanner = index"
        />
      </div>
      <div class="absolute top-4 right-4 bg-black/20 backdrop-blur-sm text-white text-xs px-3 py-1.5 rounded-full z-20 font-medium">
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
      <div class="grid grid-cols-5 gap-4">
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

    <!-- AI 智能推荐 -->
    <section class="mb-8">
      <RagSearch />
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
import { Search, Smartphone, Monitor, Wind, Palette, Apple, Heart, Coffee, ChevronLeft, ChevronRight } from 'lucide-vue-next'
import ProductCard from '@/components/ProductCard.vue'
import RagSearch from '@/components/RagSearch.vue'
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

let bannerTimer
function startBannerAutoPlay() {
  bannerTimer = setInterval(() => {
    currentBanner.value = (currentBanner.value + 1) % banners.length
  }, 4500)
}

function prevBanner() {
  currentBanner.value = (currentBanner.value - 1 + banners.length) % banners.length
}

function nextBanner() {
  currentBanner.value = (currentBanner.value + 1) % banners.length
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

@keyframes float-particle {
  0% { transform: translateY(0) scale(1); opacity: 1; }
  50% { transform: translateY(-20px) scale(1.5); opacity: 0.8; }
  100% { transform: translateY(0) scale(1); opacity: 1; }
}
.animate-float-particle {
  animation: float-particle 4s ease-in-out infinite;
}

@keyframes bounce-in {
  0% { opacity: 0; transform: scale(0.3); }
  50% { transform: scale(1.15); }
  70% { transform: scale(0.9); }
  100% { opacity: 1; transform: scale(1); }
}
.animate-bounce-in {
  animation: bounce-in 0.6s ease-out both;
}

@keyframes slide-up {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}
.animate-slide-up {
  animation: slide-up 0.5s ease-out both;
}
</style>
