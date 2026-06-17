<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-green-50 to-emerald-50">
    <div class="text-center p-8 max-w-md">
      <!-- 加载中 -->
      <template v-if="loading">
        <div class="relative w-20 h-20 mx-auto mb-6">
          <div class="absolute inset-0 border-4 border-blue-200 rounded-full" />
          <div class="absolute inset-0 border-4 border-transparent border-t-primary rounded-full animate-spin" />
          <div class="absolute inset-0 flex items-center justify-center">
            <Wallet class="w-8 h-8 text-primary" />
          </div>
        </div>
        <h2 class="text-xl font-semibold text-gray-800 mb-2">正在确认支付状态...</h2>
        <p class="text-sm text-gray-500 mb-6">请稍候</p>
      </template>

      <!-- 支付成功 -->
      <template v-else-if="paid">
        <div class="w-20 h-20 mx-auto mb-6 bg-green-100 rounded-full flex items-center justify-center">
          <CheckCircle class="w-10 h-10 text-green-500" />
        </div>
        <h2 class="text-2xl font-bold text-gray-800 mb-2">支付成功</h2>
        <p class="text-sm text-gray-500 mb-6">感谢您的购买，商品将尽快为您发出</p>

        <div class="bg-white rounded-xl shadow-sm p-5 mb-6 text-left">
          <div class="flex justify-between items-center py-2 border-b border-gray-50">
            <span class="text-gray-500 text-sm">订单编号</span>
            <span class="text-gray-800 text-sm font-mono">{{ orderNo }}</span>
          </div>
          <div class="flex justify-between items-center py-2">
            <span class="text-gray-500 text-sm">支付金额</span>
            <span class="text-lg font-bold text-functional-danger">¥{{ amount }}</span>
          </div>
        </div>

        <div class="flex gap-3 justify-center">
          <router-link to="/orders" class="btn-primary px-6 py-2.5 inline-flex items-center gap-2">
            <ClipboardList class="w-4 h-4" />
            查看订单
          </router-link>
          <router-link to="/" class="border border-gray-300 text-gray-600 px-6 py-2.5 rounded-lg
            hover:bg-gray-50 transition-all inline-flex items-center gap-2 text-sm">
            <Home class="w-4 h-4" />
            返回首页
          </router-link>
        </div>
        <p class="text-xs text-gray-400 mt-6">
          {{ countdown > 0 ? `${countdown}秒后自动跳转到订单列表` : '正在跳转...' }}
        </p>
      </template>

      <!-- 未支付（兜底：跳转到订单列表） -->
      <template v-else>
        <div class="w-20 h-20 mx-auto mb-6 bg-yellow-100 rounded-full flex items-center justify-center">
          <Clock class="w-10 h-10 text-yellow-500" />
        </div>
        <h2 class="text-xl font-semibold text-gray-800 mb-2">支付处理中</h2>
        <p class="text-sm text-gray-500 mb-6">支付正在确认中，请稍后查看订单状态</p>
        <router-link to="/orders" class="btn-primary px-6 py-2.5 inline-flex items-center gap-2">
          <ClipboardList class="w-4 h-4" />
          查看订单列表
        </router-link>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { CheckCircle, ClipboardList, Home, Wallet, Clock } from 'lucide-vue-next'
import { getOrderDetail } from '@/api/order'
import { getUser } from '@/utils/auth'

const route = useRoute()
const router = useRouter()

const orderNo = ref('')
const amount = ref('0.00')
const loading = ref(true)
const paid = ref(false)
const countdown = ref(5)
let timer = null

onMounted(async () => {
  const orderId = route.query.id
  if (!orderId) {
    router.replace('/orders')
    return
  }

  // 查询订单直到确认已支付或超时
  for (let i = 0; i < 10; i++) {
    try {
      const res = await getOrderDetail(orderId)
      if (res.code !== 200) {
        // 无权限或订单不存在 → 跳转到订单列表
        ElMessage.error('无权访问该订单')
        router.replace('/orders')
        return
      }
      const order = res.data
      orderNo.value = order.orderNo || ''
      amount.value = order.totalAmount || '0.00'

      if (order.status >= 1) {
        paid.value = true
        loading.value = false
        startCountdown()
        return
      }
    } catch {
      // 网络错误，继续重试
    }

    // 等待1.5秒再查一次
    await new Promise(resolve => setTimeout(resolve, 1500))
  }

  // 超时 + 仍显示未支付
  loading.value = false
  paid.value = false
})

function startCountdown() {
  timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
      timer = null
      router.replace('/orders')
    }
  }, 1000)
}

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>
