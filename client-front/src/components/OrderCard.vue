<template>
  <div class="bg-white rounded-xl shadow-sm overflow-hidden">
    <!-- 订单头部 -->
    <div class="flex items-center justify-between px-4 py-3 border-b border-gray-50">
      <div class="flex items-center gap-2 text-sm">
        <span class="text-gray-500">订单号：</span>
        <span class="text-gray-700 font-mono text-xs">{{ order.orderNo }}</span>
      </div>
      <el-tag :type="getOrderStatusType(order.status)" size="small" effect="dark">
        {{ getOrderStatusText(order.status) }}
      </el-tag>
    </div>

    <!-- 未支付倒计时 -->
    <div v-if="Number(order.status) === 0 && countdown > 0" class="px-4 py-1.5 bg-amber-50 border-b border-amber-100">
      <div class="flex items-center justify-between text-xs">
        <span class="text-amber-600 flex items-center gap-1">
          <Clock class="w-3.5 h-3.5" />
          剩余 {{ countdownText }} 自动取消
        </span>
      </div>
    </div>

    <!-- 订单商品 -->
    <div class="px-4 py-3 space-y-3">
      <div
        v-for="item in order.items"
        :key="item.id"
        class="flex items-center gap-3"
      >
        <div class="w-16 h-16 bg-gray-50 rounded-lg overflow-hidden shrink-0">
          <img
            v-if="item.productImage"
            :src="item.productImage"
            :alt="item.productName"
            class="w-full h-full object-cover"
          />
          <div v-else class="w-full h-full flex items-center justify-center text-gray-300">
            <Package class="w-6 h-6" />
          </div>
        </div>
        <div class="flex-1 min-w-0">
          <p class="text-sm text-gray-900 line-clamp-1">{{ item.productName }}</p>
          <p class="text-xs text-gray-400 mt-1">x{{ item.quantity }}</p>
        </div>
        <p class="text-sm font-medium text-gray-900 shrink-0">¥{{ formatPrice(item.price * item.quantity) }}</p>
      </div>
    </div>

    <!-- 订单底部 -->
    <div class="px-4 py-3 bg-gray-50 flex items-center justify-between">
      <div class="text-sm text-gray-500">
        共 {{ order.items?.length || 0 }} 件商品
        <span class="ml-2">合计：<span class="font-bold text-functional-danger">¥{{ formatPrice(order.totalAmount) }}</span></span>
      </div>
      <div class="flex gap-2">
        <button
          v-if="Number(order.status) === 2"
          class="btn-cta text-sm !px-4 !py-1.5"
          @click="$emit('ship', order)"
        >
          确认收货
        </button>
        <!-- 已发货 → 查看物流 -->
        <button
          v-if="Number(order.status) === 2"
          class="border border-blue-300 text-blue-500 text-sm px-3 py-1.5 rounded-lg
                 hover:bg-blue-50 transition-all cursor-pointer"
          @click="handleTracking"
        >
          查看物流
        </button>
        <!-- 已支付/已发货/已完成 → 申请售后 -->
        <template v-if="Number(order.status) === 1 || Number(order.status) === 2 || Number(order.status) === 3 || Number(order.status) === 4">
          <span v-if="order.refundStatus === 1"
            class="text-xs text-gray-400 bg-gray-100 px-3 py-1.5 rounded-lg cursor-not-allowed">
            已申请售后
          </span>
          <span v-else-if="order.refundStatus === 2"
            class="text-xs text-gray-400 bg-gray-100 px-3 py-1.5 rounded-lg">
            售后已完成
          </span>
          <button v-else
            class="border border-orange-300 text-orange-500 text-sm px-3 py-1.5 rounded-lg
                   hover:bg-orange-50 transition-all cursor-pointer"
            @click="handleRefund(order)"
          >
            申请售后
          </button>
        </template>
        <!-- 已完成 → 去评价/已评价 -->
        <template v-if="Number(order.status) === 3">
          <span
            v-if="allReviewed"
            class="text-xs text-gray-400 bg-gray-100 px-3 py-1.5 rounded-lg"
          >
            已评价
          </span>
          <button
            v-else
            class="border border-orange-300 text-orange-500 text-sm px-4 py-1.5 rounded-lg
                   hover:bg-orange-50 transition-all cursor-pointer"
            @click="handleReview"
          >
            去评价
          </button>
        </template>
        <button
          v-if="Number(order.status) === 0"
          class="btn-primary text-sm !px-4 !py-1.5"
          @click="handlePay"
        >
          立即支付
        </button>
        <button
          v-if="Number(order.status) === 0"
          class="border border-gray-300 text-gray-600 text-sm px-4 py-1.5 rounded-lg
                 hover:border-functional-danger hover:text-functional-danger transition-colors cursor-pointer"
          @click="$emit('cancel', order)"
        >
          取消订单
        </button>
        <!-- 已完成/已取消/已售后 → 删除订单 -->
        <button
          v-if="Number(order.status) === 3 || Number(order.status) === -1 || Number(order.status) === 4"
          class="border border-gray-300 text-gray-400 text-sm px-3 py-1.5 rounded-lg
                 hover:border-functional-danger hover:text-functional-danger transition-colors cursor-pointer"
          @click="handleDelete"
        >
          删除订单
        </button>
      </div>
    </div>

    <!-- 评价弹窗 -->
    <ReviewDialog
      v-model="showReview"
      :order-id="order.id"
      :items="order.items || []"
      @submitted="onReviewSubmitted"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { Package, Clock } from 'lucide-vue-next'
import { formatPrice, getOrderStatusText, getOrderStatusType } from '@/utils/format'
import { getOrderReviews } from '@/api/review'
import { deleteOrder } from '@/api/order'
import { ElMessage, ElMessageBox } from 'element-plus'
import ReviewDialog from './ReviewDialog.vue'

const props = defineProps({
  order: { type: Object, required: true }
})

const router = useRouter()
const emit = defineEmits(['cancel', 'ship', 'reviewed', 'delete'])

const showReview = ref(false)

/** 跳转到支付宝支付页面 */
function handlePay() {
  router.push(`/payment/${props.order.id}`)
}
const allReviewed = ref(false)

// 倒计时：15分钟（与后端自动取消时间一致）
const CANCEL_MINUTES = 15
const CANCEL_MS = CANCEL_MINUTES * 60 * 1000
const countdown = ref(0)
let timer = null

function calcCountdown() {
  if (!props.order.createTime) return 0
  const created = new Date(props.order.createTime).getTime()
  const now = Date.now()
  const elapsed = now - created
  const remaining = Math.max(0, CANCEL_MS - elapsed)
  return remaining
}

const countdownText = computed(() => {
  const totalSec = Math.floor(countdown.value / 1000)
  const min = Math.floor(totalSec / 60)
  const sec = totalSec % 60
  return `${String(min).padStart(2, '0')}分${String(sec).padStart(2, '0')}秒`
})

function startTimer() {
  countdown.value = calcCountdown()
  if (countdown.value > 0) {
    timer = setInterval(() => {
      countdown.value = calcCountdown()
      if (countdown.value <= 0) {
        clearInterval(timer)
        timer = null
      }
    }, 1000)
  }
}

async function checkReviewStatus() {
  if (Number(props.order.status) !== 3) return
  try {
    const res = await getOrderReviews(props.order.id)
    if (res.code === 200) {
      const reviewedIds = res.data?.reviewedItemIds || []
      const totalItems = props.order.items?.length || 0
      allReviewed.value = totalItems > 0 && reviewedIds.length >= totalItems
    }
  } catch (err) {
    // 静默失败，保持"去评价"状态
  }
}

function handleRefund(order) {
  router.push(`/orders/refund/apply/${order.id}`)
}

function handleTracking() {
  router.push(`/orders/tracking/${props.order.id}`)
}

function handleReview() {
  showReview.value = true
}

function onReviewSubmitted() {
  allReviewed.value = true
  emit('reviewed', props.order.id)
}

async function handleDelete() {
  try {
    await ElMessageBox.confirm('确定要删除该订单记录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await deleteOrder(props.order.id)
    if (res.code === 200) {
      ElMessage.success('订单已删除')
      emit('delete', props.order.id)
    }
  } catch {
    // 用户取消
  }
}

onMounted(() => {
  startTimer()
  checkReviewStatus()
})

onUnmounted(() => {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
})
</script>
