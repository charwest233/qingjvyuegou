<template>
  <div class="page-container py-4">
    <h1 class="section-title">我的评价</h1>

    <div v-if="loading" class="space-y-4 mt-4">
      <div v-for="n in 3" :key="n" class="animate-pulse bg-white rounded-xl p-4 shadow-sm">
        <div class="flex gap-3">
          <div class="w-16 h-16 bg-gray-200 rounded-lg" />
          <div class="flex-1 space-y-2">
            <div class="h-4 bg-gray-200 rounded w-2/3" />
            <div class="h-3 bg-gray-200 rounded w-1/3" />
            <div class="h-3 bg-gray-200 rounded w-full" />
          </div>
        </div>
      </div>
    </div>

    <div v-else-if="reviews.length === 0" class="text-center py-20">
      <Star class="w-16 h-16 mx-auto text-gray-300 mb-3" />
      <p class="text-gray-400">暂无评价</p>
      <router-link to="/orders" class="text-sm text-primary mt-2 inline-block">去订单列表评价 →</router-link>
    </div>

    <div v-else class="space-y-3 mt-4">
        <div
          v-for="review in reviews"
          :key="review.id"
          class="bg-white rounded-xl p-4 shadow-sm"
        >
          <div class="flex items-start gap-3">
            <div class="w-14 h-14 bg-gray-50 rounded-lg overflow-hidden shrink-0">
              <img
                v-if="review.productImage"
                :src="review.productImage"
                :alt="review.productName"
                class="w-full h-full object-cover"
              />
              <div v-else class="w-full h-full flex items-center justify-center text-gray-300 text-xs">
                商品
              </div>
            </div>
            <div class="flex-1 min-w-0">
              <p class="text-sm font-medium text-gray-900 line-clamp-1">{{ review.productName || '商品' }}</p>
              <div class="flex items-center gap-0.5 mt-1">
                <Star
                  v-for="s in 5"
                  :key="s"
                  class="w-3.5 h-3.5"
                  :class="s <= review.rating ? 'text-amber-400 fill-amber-400' : 'text-gray-200'"
                />
              </div>
              <p class="text-sm text-gray-600 mt-2 leading-relaxed">{{ review.content || '用户未填写评价内容' }}</p>
              <p class="text-xs text-gray-300 mt-2">{{ formatTime(review.createTime) }}</p>
            </div>
            <!-- 删除按钮 -->
            <button
              class="shrink-0 text-gray-300 hover:text-functional-danger transition-colors cursor-pointer p-1"
              title="删除评价"
              @click="handleDelete(review.id)"
            >
              <Trash2 class="w-4 h-4" />
            </button>
          </div>
        </div>

      <!-- 分页 -->
      <div class="flex justify-center py-4">
        <el-pagination
          v-if="total > size"
          background
          layout="prev, pager, next"
          :pager-count="5"
          :total="total"
          :page-size="size"
          :current-page="page"
          @current-change="onPageChange"
          small
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Star, Trash2 } from 'lucide-vue-next'
import { getUserReviews, deleteUserReview } from '@/api/review'
import { formatTime } from '@/utils/format'
import { ElMessage, ElMessageBox } from 'element-plus'

const reviews = ref([])
const loading = ref(true)
const page = ref(1)
const size = ref(10)
const total = ref(0)

async function loadReviews() {
  loading.value = true
  try {
    const res = await getUserReviews(page.value, size.value)
    if (res.code === 200) {
      reviews.value = res.data?.list || []
      total.value = res.data?.total || 0
    }
  } catch (err) {
    console.error('加载评价失败:', err)
  } finally {
    loading.value = false
  }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定要删除这条评价吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await deleteUserReview(id)
    if (res.code === 200) {
      ElMessage.success('评价已删除')
      loadReviews()
    }
  } catch {
    // 用户取消或删除失败
  }
}

function onPageChange(p) {
  page.value = p
  loadReviews()
}

onMounted(() => {
  loadReviews()
})
</script>
