<template>
  <div class="min-h-screen bg-gray-50">
    <div class="page-container py-3">
      <div v-if="loading" class="animate-pulse space-y-4">
        <div class="flex gap-4">
          <div class="w-44 h-44 shrink-0 bg-gray-200 rounded-lg" />
          <div class="flex-1 space-y-3">
            <div class="h-5 bg-gray-200 rounded w-3/4" />
            <div class="h-4 bg-gray-200 rounded w-1/2" />
            <div class="h-10 bg-gray-200 rounded w-1/3" />
          </div>
        </div>
      </div>
      <template v-else-if="product">
        <!-- 顶部导航 -->
        <div class="flex items-center justify-between mb-4">
          <button
            @click="$router.back()"
            class="p-1.5 -ml-1.5 hover:bg-gray-100 rounded-lg transition-all cursor-pointer"
          >
            <ArrowLeft class="w-5 h-5 text-gray-600" />
          </button>
          <div class="flex items-center gap-1 text-xs text-gray-400">
            <router-link to="/" class="hover:text-primary transition-colors">首页</router-link>
            <ChevronRight class="w-3 h-3" />
            <span class="text-gray-600">商品详情</span>
          </div>
        </div>

        <!-- 京东风格：左图右信息 -->
        <div class="bg-white rounded-xl shadow-sm overflow-hidden mb-3">
          <div class="flex flex-col md:flex-row">
            <!-- 左侧：商品图片 -->
            <div class="md:w-72 lg:w-80 shrink-0 bg-gray-50 flex items-center justify-center p-4">
              <div class="w-full max-w-[260px] aspect-square rounded-lg overflow-hidden">
                <img
                  v-if="product.mainImage"
                  :src="product.mainImage"
                  :alt="product.name"
                  class="w-full h-full object-cover"
                />
                <div v-else class="w-full h-full flex items-center justify-center text-gray-300">
                  <Image class="w-16 h-16" />
                </div>
              </div>
            </div>

            <!-- 右侧：商品信息 -->
            <div class="flex-1 p-4 md:p-5 space-y-4">
              <!-- 标题 + 收藏 -->
              <div class="flex items-start justify-between gap-3">
                <h1 class="text-base md:text-lg font-bold text-gray-900 leading-snug flex-1">
                  {{ product.name }}
                </h1>
                <button
                  class="flex-shrink-0 p-2 rounded-lg transition-all cursor-pointer"
                  :class="isFavorited
                    ? 'bg-red-50 text-red-500'
                    : 'bg-gray-50 text-gray-400 hover:bg-red-50 hover:text-red-500'"
                  @click="toggleFavorite"
                >
                  <Heart class="w-5 h-5" :fill="isFavorited ? 'currentColor' : 'none'" />
                </button>
              </div>

              <!-- 价格区（高对比） -->
              <div class="bg-gradient-to-r from-red-50 to-orange-50 rounded-xl p-4">
                <div class="flex items-baseline gap-1">
                  <span class="text-sm text-red-500 font-bold">¥</span>
                  <span class="text-3xl md:text-4xl font-bold text-red-500">{{ formatPrice(product.price) }}</span>
                </div>
                <div class="flex items-center gap-4 mt-2 text-xs text-gray-500">
                  <span>已售 <strong class="text-gray-700">{{ product.salesCount || 0 }}</strong></span>
                  <span>库存 <strong class="text-gray-700">{{ product.stock || 0 }}</strong></span>
                </div>
              </div>

              <!-- 促销信息 -->
              <div class="flex items-center gap-2 text-xs">
                <span class="bg-red-500 text-white px-2 py-0.5 rounded font-medium">满减</span>
                <span class="text-gray-600">满299减30，满599减80</span>
              </div>

              <!-- 数量选择（高对比边框） -->
              <div class="flex items-center justify-between py-2 border-t border-gray-100">
                <span class="text-sm text-gray-600">数量</span>
                <div class="flex items-center gap-2">
                  <button
                    class="w-8 h-8 border border-gray-300 rounded flex items-center justify-center
                           hover:border-red-400 hover:text-red-500 transition-all cursor-pointer disabled:bg-gray-50 disabled:text-gray-300"
                    :disabled="quantity <= 1"
                    @click="quantity--"
                  >
                    <Minus class="w-3.5 h-3.5" />
                  </button>
                  <span class="w-10 text-center text-sm font-medium text-gray-900">{{ quantity }}</span>
                  <button
                    class="w-8 h-8 border border-gray-300 rounded flex items-center justify-center
                           hover:border-red-400 hover:text-red-500 transition-all cursor-pointer"
                    :disabled="quantity >= (product.stock || 99)"
                    @click="quantity++"
                  >
                    <Plus class="w-3.5 h-3.5" />
                  </button>
                </div>
              </div>

              <!-- 服务承诺 -->
              <div class="flex items-center gap-4 text-xs text-gray-400 py-2 border-t border-gray-100">
                <span class="flex items-center gap-1"><ShieldCheck class="w-3.5 h-3.5" /> 品质保障</span>
                <span class="flex items-center gap-1"><Truck class="w-3.5 h-3.5" /> 极速发货</span>
                <span class="flex items-center gap-1"><RotateCcw class="w-3.5 h-3.5" /> 七天退换</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 商品描述 -->
        <div class="bg-white rounded-xl shadow-sm p-4">
          <h3 class="text-sm font-bold text-gray-800 mb-3 flex items-center gap-2 pb-2 border-b border-gray-100">
            <FileText class="w-4 h-4 text-red-500" />
            商品详情
          </h3>
          <p class="text-sm text-gray-600 leading-relaxed">{{ product.description || '暂无描述' }}</p>
        </div>

        <!-- 商品评价 -->
        <div class="bg-white rounded-xl shadow-sm p-4 mt-3 mb-20">
          <h3 class="text-sm font-bold text-gray-800 mb-3 flex items-center gap-2 pb-2 border-b border-gray-100">
            <Star class="w-4 h-4 text-amber-400 fill-amber-400" />
            商品评价
            <span v-if="product.reviewCount > 0" class="text-xs text-gray-400 font-normal ml-1">
              ({{ product.reviewCount }}条)
            </span>
          </h3>

          <!-- 评分概览 -->
          <div v-if="product.reviewCount > 0" class="flex items-center gap-3 mb-4 p-3 bg-orange-50/50 rounded-lg">
            <div class="text-center">
              <span class="text-2xl font-bold text-orange-500">{{ avgRating }}</span>
              <span class="text-xs text-gray-400 ml-0.5">分</span>
            </div>
            <div class="flex flex-col gap-0.5">
              <div class="flex items-center gap-0.5">
                <Star
                  v-for="s in 5"
                  :key="s"
                  class="w-3.5 h-3.5"
                  :class="s <= Math.round(Number(avgRating)) ? 'text-amber-400 fill-amber-400' : 'text-gray-300'"
                />
              </div>
              <span class="text-xs text-gray-400">{{ product.reviewCount }}人评价</span>
            </div>
          </div>

          <!-- 评价列表 -->
          <div v-if="reviewLoading" class="space-y-3">
            <div v-for="n in 3" :key="n" class="animate-pulse flex gap-3">
              <div class="w-8 h-8 bg-gray-200 rounded-full shrink-0" />
              <div class="flex-1 space-y-2">
                <div class="h-3 bg-gray-200 rounded w-1/4" />
                <div class="h-3 bg-gray-200 rounded w-3/4" />
              </div>
            </div>
          </div>
          <div v-else-if="reviews.length === 0" class="text-center py-8 text-gray-400 text-sm">
            暂无评价，快来发表第一条评价吧
          </div>
          <div v-else class="space-y-4">
            <div
              v-for="review in reviews"
              :key="review.id"
              class="pb-4 border-b border-gray-50 last:border-0"
            >
              <div class="flex items-center justify-between mb-2">
                <div class="flex items-center gap-2">
                  <div class="w-7 h-7 bg-gray-100 rounded-full overflow-hidden shrink-0">
                    <img
                      v-if="review.avatarUrl"
                      :src="review.avatarUrl"
                      class="w-full h-full object-cover"
                    />
                    <div v-else class="w-full h-full flex items-center justify-center text-gray-400 text-xs">
                      {{ (review.nickname || '用')[0] }}
                    </div>
                  </div>
                  <span class="text-xs text-gray-600">{{ review.nickname || '匿名用户' }}</span>
                </div>
                <div class="flex items-center gap-0.5">
                  <Star
                    v-for="s in 5"
                    :key="s"
                    class="w-3 h-3"
                    :class="s <= review.rating ? 'text-amber-400 fill-amber-400' : 'text-gray-200'"
                  />
                </div>
              </div>
              <p class="text-sm text-gray-600 leading-relaxed">{{ review.content || '用户未填写评价内容' }}</p>
              <p class="text-xs text-gray-300 mt-1">{{ formatTime(review.createTime) }}</p>
            </div>
          </div>
        </div>

        <!-- 底部操作栏（京东风格按钮） -->
        <div class="fixed bottom-16 left-0 right-0 bg-white border-t border-gray-200 p-3 z-40">
          <div class="max-w-7xl mx-auto flex gap-2">
            <button
              class="flex-1 py-2.5 rounded-lg border-2 border-orange-500 text-orange-500 font-semibold text-sm
                     hover:bg-orange-50 transition-all flex items-center justify-center gap-1.5 cursor-pointer"
              @click="addToCart"
            >
              <ShoppingCart class="w-4 h-4" />
              加入购物车
            </button>
            <button
              class="flex-1 py-2.5 rounded-lg bg-gradient-to-r from-orange-500 to-amber-500 text-white font-semibold text-sm
                     hover:shadow-lg transition-all cursor-pointer"
              @click="buyNow"
            >
              立即购买
            </button>
          </div>
        </div>
        <!-- 底部避让 -->
        <div class="h-16" />
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, ChevronRight, Heart, Minus, Plus, ShoppingCart, Image, FileText, ShieldCheck, Truck, RotateCcw, Star } from 'lucide-vue-next'
import { ElMessage } from 'element-plus'
import { getProductDetail } from '@/api/product'
import { addFavorite, removeFavorite, getFavorites } from '@/api/user'
import { toggleCartItem, toggleCartAll } from '@/api/cart'
import { getProductReviews } from '@/api/review'
import { formatPrice, formatTime } from '@/utils/format'
import { isLoggedIn } from '@/utils/auth'
import { useCartStore } from '@/stores/cart'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()

const product = ref(null)
const loading = ref(true)
const quantity = ref(1)
const isFavorited = ref(false)
const reviews = ref([])
const reviewLoading = ref(true)
const avgRating = ref('0.0')

async function loadProduct() {
  try {
    const res = await getProductDetail(route.params.id)
    if (res.code === 200) {
      product.value = res.data
    }
  } catch (err) {
    console.error('加载商品详情失败:', err)
  } finally {
    loading.value = false
  }
}

async function loadReviews() {
  try {
    const res = await getProductReviews(route.params.id)
    if (res.code === 200) {
      reviews.value = res.data?.list || []
      if (reviews.value.length > 0) {
        const total = res.data?.total || 0
        const sum = reviews.value.reduce((acc, r) => acc + r.rating, 0)
        const by = res.data?.list?.length || total
        avgRating.value = (sum / by).toFixed(1)
        if (product.value) {
          product.value.reviewCount = total
        }
      }
    }
  } catch (err) {
    console.error('加载评价失败:', err)
  } finally {
    reviewLoading.value = false
  }
}

async function checkFavorite() {
  if (!isLoggedIn()) return
  try {
    const res = await getFavorites()
    if (res.code === 200) {
      isFavorited.value = (res.data?.list || res.data || []).some(f => (f.id || f.productId) === Number(route.params.id))
    }
  } catch (err) {
    console.error('检查收藏失败:', err)
  }
}

async function toggleFavorite() {
  if (!isLoggedIn()) {
    router.push({ name: 'Login', query: { redirect: route.fullPath } })
    return
  }
  try {
    if (isFavorited.value) {
      await removeFavorite(route.params.id)
      isFavorited.value = false
      ElMessage.success('已取消收藏')
    } else {
      await addFavorite(route.params.id)
      isFavorited.value = true
      ElMessage.success('收藏成功')
    }
  } catch (err) {
    console.error('操作收藏失败:', err)
  }
}

async function addToCart() {
  if (!isLoggedIn()) {
    router.push({ name: 'Login', query: { redirect: route.fullPath } })
    return
  }
  const success = await cartStore.add(product.value.id, quantity.value)
  if (success) {
    ElMessage.success('已加入购物车')
  }
}

async function buyNow() {
  if (!isLoggedIn()) {
    router.push({ name: 'Login', query: { redirect: route.fullPath } })
    return
  }
  // 修正：先检查购物车中是否已存在该商品，若存在则直接设置数量，避免累加 bug
  const existingItem = cartStore.items.find(i => i.productId === product.value.id)
  let success
  if (existingItem) {
    await cartStore.updateQuantity(product.value.id, quantity.value)
    success = true
  } else {
    success = await cartStore.add(product.value.id, quantity.value)
  }
  if (!success) {
    ElMessage.error('操作失败，请重试')
    return
  }
  try {
    await toggleCartAll(false)
    await toggleCartItem(product.value.id, true)
    await cartStore.loadCart()
  } catch (err) {
    console.error('选择商品失败:', err)
  }
  router.push({ name: 'Checkout' })
}

onMounted(() => {
  loadProduct()
  loadReviews()
  checkFavorite()
})
</script>
