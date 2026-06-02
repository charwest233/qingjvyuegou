<template>
  <div class="page-container py-4">
    <h1 class="section-title">评价管理</h1>

    <!-- 搜索 -->
    <div class="mb-4">
      <el-input
        v-model="keyword"
        placeholder="搜索评价内容"
        clearable
        class="w-full"
        @clear="loadReviews"
        @keyup.enter="loadReviews"
      >
        <template #append>
          <el-button @click="loadReviews">搜索</el-button>
        </template>
      </el-input>
    </div>

    <!-- 评价列表 -->
    <div v-if="loading" class="space-y-4">
      <div v-for="n in 5" :key="n" class="bg-white rounded-xl p-4 shadow-sm animate-pulse">
        <div class="h-4 bg-gray-200 rounded w-1/4 mb-3" />
        <div class="h-3 bg-gray-200 rounded w-3/4 mb-2" />
        <div class="h-3 bg-gray-200 rounded w-1/2" />
      </div>
    </div>
    <div v-else-if="reviews.length === 0" class="text-center py-20">
      <MessageSquare class="w-20 h-20 mx-auto text-gray-300 mb-4" />
      <p class="text-gray-400">暂无评价</p>
    </div>
    <div v-else class="space-y-4">
      <div
        v-for="review in reviews"
        :key="review.id"
        class="bg-white rounded-xl shadow-sm p-4"
      >
        <div class="flex items-start justify-between mb-3">
          <div class="flex items-center gap-2">
            <div class="w-8 h-8 bg-gray-100 rounded-full overflow-hidden shrink-0">
              <img
                v-if="review.avatarUrl"
                :src="review.avatarUrl"
                class="w-full h-full object-cover"
              />
              <div v-else class="w-full h-full flex items-center justify-center text-gray-400 text-xs">
                {{ (review.nickname || '用')[0] }}
              </div>
            </div>
            <div>
              <p class="text-sm font-medium text-gray-800">{{ review.nickname || '匿名用户' }}</p>
              <p class="text-xs text-gray-400">{{ formatTime(review.createTime) }}</p>
            </div>
          </div>
          <div class="flex items-center gap-0.5">
            <Star
              v-for="s in 5"
              :key="s"
              class="w-3.5 h-3.5"
              :class="s <= review.rating ? 'text-amber-400 fill-amber-400' : 'text-gray-200'"
            />
          </div>
        </div>
        <p class="text-sm text-gray-600 leading-relaxed mb-2">{{ review.content || '用户未填写评价内容' }}</p>
        <div class="flex items-center justify-between text-xs text-gray-400">
          <span>评价ID: {{ review.id }} | 商品ID: {{ review.productId }}</span>
          <button
            class="px-3 py-1 rounded-lg border border-functional-danger text-functional-danger text-xs
                   hover:bg-functional-danger hover:text-white transition-all cursor-pointer"
            @click="handleDelete(review.id)"
          >
            删除
          </button>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div v-if="totalPages > 1" class="flex justify-center mt-6">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        background
        @current-change="loadReviews"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Star, MessageSquare } from 'lucide-vue-next'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getReviewListForAdmin, deleteReview } from '@/api/review'
import { formatTime } from '@/utils/format'

const keyword = ref('')
const reviews = ref([])
const loading = ref(true)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const totalPages = ref(0)

async function loadReviews() {
  loading.value = true
  try {
    const res = await getReviewListForAdmin(keyword.value, currentPage.value, pageSize.value)
    if (res.code === 200) {
      reviews.value = res.data?.list || []
      total.value = res.data?.total || 0
      totalPages.value = res.data?.pages || Math.ceil(total.value / pageSize.value) || 0
    }
  } catch (err) {
    console.error('加载评价列表失败:', err)
  } finally {
    loading.value = false
  }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定要删除该评价吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await deleteReview(id)
    if (res.code === 200) {
      ElMessage.success('已删除')
      loadReviews()
    }
  } catch {
    // 用户取消
  }
}

onMounted(() => {
  loadReviews()
})
</script>
