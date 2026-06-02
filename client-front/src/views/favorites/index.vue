<template>
  <div class="page-container py-4">
    <div class="flex items-center gap-3 mb-6">
      <button @click="$router.back()" class="p-2 -ml-2 hover:bg-gray-100 rounded-lg transition-colors cursor-pointer">
        <ArrowLeft class="w-5 h-5 text-gray-600" />
      </button>
      <h1 class="section-title !mb-0">我的收藏</h1>
    </div>

    <!-- 加载骨架 -->
    <div v-if="loading" class="grid grid-cols-4 gap-3">
      <div v-for="n in 8" :key="n" class="bg-white rounded-xl overflow-hidden shadow-sm animate-pulse">
        <div class="aspect-[4/3] bg-gray-200" />
        <div class="p-2.5 space-y-1.5">
          <div class="h-3 bg-gray-200 rounded w-3/4" />
          <div class="h-3 bg-gray-200 rounded w-1/3" />
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-else-if="favorites.length === 0" class="flex flex-col items-center justify-center py-24">
      <Heart class="w-16 h-16 text-gray-300 mb-4" />
      <p class="text-gray-400 mb-6">暂无收藏商品</p>
      <router-link to="/product-list" class="btn-primary inline-block">去逛逛</router-link>
    </div>

    <!-- 收藏商品列表（一排4列，类似商品分类布局） -->
    <div v-else class="grid grid-cols-4 gap-3">
      <div
        v-for="fav in favorites"
        :key="fav.id"
        class="bg-white rounded-xl overflow-hidden shadow-sm card-hover cursor-pointer group relative"
        @click="goDetail(fav.id)"
      >
        <!-- 商品图片 -->
        <div class="aspect-[4/3] bg-gray-50 relative">
          <img
            v-if="fav.mainImage"
            :src="fav.mainImage"
            :alt="fav.name"
            class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
          />
          <div v-else class="w-full h-full flex items-center justify-center text-gray-300">
            <Image class="w-8 h-8" />
          </div>
          <!-- 移除收藏按钮 -->
          <button
            class="absolute top-1.5 right-1.5 w-7 h-7 bg-white/80 backdrop-blur-sm rounded-full flex items-center justify-center
                   text-functional-danger hover:bg-white hover:scale-110 transition-all cursor-pointer shadow-sm"
            @click.stop="handleRemove(fav.id)"
          >
            <Heart class="w-3.5 h-3.5 fill-current" />
          </button>
        </div>

        <!-- 商品信息 -->
        <div class="p-2.5">
          <h3 class="text-xs font-medium text-gray-900 line-clamp-2 leading-tight mb-1.5">
            {{ fav.name }}
          </h3>
          <div class="flex items-center justify-between">
            <span class="text-sm font-bold text-functional-danger">¥{{ fav.price }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowLeft, Heart, Image } from 'lucide-vue-next'
import { ElMessage } from 'element-plus'
import { getFavorites, removeFavorite } from '@/api/user'

const router = useRouter()
const favorites = ref([])
const loading = ref(true)

async function loadFavorites() {
  loading.value = true
  try {
    const res = await getFavorites()
    if (res.code === 200) {
      // 服务器返回 { list: [...Product对象...], total: N }
      // Product 字段: id, name, mainImage, price
      favorites.value = res.data?.list || []
    }
  } catch (err) {
    console.error('加载收藏失败:', err)
  } finally {
    loading.value = false
  }
}

function goDetail(productId) {
  router.push(`/product/${productId}`)
}

async function handleRemove(id) {
  try {
    await removeFavorite(id)
    ElMessage.success('已取消收藏')
    await loadFavorites()
  } catch (err) {
    console.error('取消收藏失败:', err)
  }
}

onMounted(() => {
  loadFavorites()
})
</script>
