<template>
  <el-dialog
    v-model="visible"
    title="商品评价"
    width="90%"
    :close-on-click-modal="false"
    class="review-dialog"
  >
    <div class="space-y-5">
      <div
        v-for="item in items"
        :key="item.id"
        class="bg-gray-50 rounded-xl p-4"
      >
        <!-- 商品信息 -->
        <div class="flex items-center gap-3 mb-3">
          <div class="w-12 h-12 bg-white rounded-lg overflow-hidden shrink-0">
            <img
              v-if="item.productImage"
              :src="item.productImage"
              :alt="item.productName"
              class="w-full h-full object-cover"
            />
            <div v-else class="w-full h-full flex items-center justify-center text-gray-300">
              <Package class="w-5 h-5" />
            </div>
          </div>
          <div class="flex-1 min-w-0">
            <p class="text-sm font-medium text-gray-800 line-clamp-1">{{ item.productName }}</p>
            <p class="text-xs text-gray-400 mt-0.5">x{{ item.quantity }}</p>
          </div>
        </div>

        <!-- 星级评分 -->
        <div class="flex items-center gap-1 mb-3">
          <span class="text-xs text-gray-500 mr-2">评分：</span>
          <button
            v-for="s in 5"
            :key="s"
            class="p-0.5 transition-all cursor-pointer"
            @click="setRating(item.id, s)"
          >
            <Star
              class="w-6 h-6"
              :class="s <= (reviewMap[item.id]?.rating || 0)
                ? 'text-amber-400 fill-amber-400'
                : 'text-gray-300'"
            />
          </button>
        </div>

        <!-- 评价文字 -->
        <textarea
          v-model="reviewMap[item.id].content"
          :placeholder="`对${item.productName}的评价...`"
          class="w-full px-3 py-2 rounded-lg border border-gray-200 text-sm
                 placeholder-gray-400 focus:border-primary focus:ring-1 focus:ring-primary
                 outline-none resize-none transition-all"
          rows="3"
          maxlength="500"
        />
        <div class="text-right text-xs text-gray-400 mt-1">
          {{ (reviewMap[item.id]?.content || '').length }}/500
        </div>
      </div>
    </div>

    <template #footer>
      <div class="flex gap-3">
        <button
          class="flex-1 py-2.5 rounded-lg border border-gray-300 text-gray-600 text-sm
                 hover:bg-gray-50 transition-all cursor-pointer"
          @click="visible = false"
        >
          取消
        </button>
        <button
          class="flex-1 py-2.5 rounded-lg bg-gradient-to-r from-orange-500 to-amber-500 text-white text-sm
                 font-semibold hover:shadow-lg transition-all cursor-pointer disabled:opacity-50"
          :disabled="submitting"
          @click="submitAll"
        >
          {{ submitting ? '提交中...' : '提交评价' }}
        </button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { Star, Package } from 'lucide-vue-next'
import { ElMessage } from 'element-plus'
import { submitReviews } from '@/api/review'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  orderId: { type: Number, default: null },
  items: { type: Array, default: () => [] }
})

const emit = defineEmits(['update:modelValue', 'submitted'])

const visible = ref(false)
const submitting = ref(false)

// 评价数据映射：{ [orderItemId]: { rating, content } }
const reviewMap = reactive({})

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
    initReviews()
  }
})

watch(visible, (val) => {
  if (!val) emit('update:modelValue', val)
})

function initReviews() {
  for (const key of Object.keys(reviewMap)) {
    delete reviewMap[key]
  }
  for (const item of props.items) {
    reviewMap[item.id] = { rating: 0, content: '' }
  }
}

function setRating(itemId, rating) {
  if (!reviewMap[itemId]) {
    reviewMap[itemId] = { rating: 0, content: '' }
  }
  reviewMap[itemId].rating = rating
}

async function submitAll() {
  const reviews = []
  for (const item of props.items) {
    const data = reviewMap[item.id]
    if (!data || !data.rating) {
      ElMessage.warning(`请给「${item.productName}」评分`)
      return
    }
    reviews.push({
      orderItemId: item.id,
      productId: item.productId,
      rating: data.rating,
      content: data.content || ''
    })
  }

  submitting.value = true
  try {
    const res = await submitReviews(reviews)
    if (res.code === 200) {
      ElMessage.success('评价成功')
      visible.value = false
      emit('submitted')
    } else {
      ElMessage.error(res.message || '提交失败')
    }
  } catch (err) {
    ElMessage.error('提交失败，请重试')
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.review-dialog :deep(.el-dialog__header) {
  padding: 16px 20px;
  margin: 0;
  border-bottom: 1px solid #f3f4f6;
}
.review-dialog :deep(.el-dialog__body) {
  padding: 16px 20px;
}
.review-dialog :deep(.el-dialog__footer) {
  padding: 12px 20px;
  border-top: 1px solid #f3f4f6;
}
</style>
