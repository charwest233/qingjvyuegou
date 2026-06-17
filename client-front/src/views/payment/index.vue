<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 to-indigo-50">
    <div class="text-center p-8 max-w-md">
      <!-- 加载动画（未提交表单） -->
      <div v-if="!submitted" class="relative w-20 h-20 mx-auto mb-6">
        <div class="absolute inset-0 border-4 border-blue-200 rounded-full" />
        <div class="absolute inset-0 border-4 border-transparent border-t-primary rounded-full animate-spin" />
        <div class="absolute inset-0 flex items-center justify-center">
          <Wallet class="w-8 h-8 text-primary" />
        </div>
      </div>

      <template v-if="!submitted && !error">
        <h2 class="text-xl font-semibold text-gray-800 mb-2">正在跳转至支付宝...</h2>
        <p class="text-sm text-gray-500 mb-6">请稍候，正在创建支付请求</p>
      </template>

      <!-- 支付确认中（正在轮询） -->
      <template v-else-if="submitted && polling">
        <div class="relative w-20 h-20 mx-auto mb-6">
          <div class="absolute inset-0 border-4 border-yellow-200 rounded-full" />
          <div class="absolute inset-0 border-4 border-transparent border-t-yellow-500 rounded-full animate-spin" />
          <div class="absolute inset-0 flex items-center justify-center">
            <Clock class="w-8 h-8 text-yellow-500" />
          </div>
        </div>
        <h2 class="text-xl font-semibold text-gray-800 mb-2">支付确认中</h2>
        <p class="text-sm text-gray-500 mb-2">正在查询支付结果...</p>
        <p class="text-xs text-gray-400 mb-6">{{ redirectCountdown }}秒后未跳转将自动重试</p>
        <div class="flex flex-col items-center gap-3">
          <button class="btn-primary px-6 py-2" @click="checkNow">我已支付，立即查询</button>
          <router-link to="/orders" class="text-sm text-gray-500 hover:text-primary underline">返回订单列表</router-link>
        </div>
      </template>

      <!-- 轮询超时（用户刷新后/等待超时） -->
      <template v-else-if="submitted && !polling && !error">
        <div class="w-20 h-20 mx-auto mb-6 bg-orange-100 rounded-full flex items-center justify-center">
          <Clock class="w-10 h-10 text-orange-500" />
        </div>
        <h2 class="text-xl font-semibold text-gray-800 mb-2">支付处理超时</h2>
        <p class="text-sm text-gray-500 mb-6">未检测到支付结果，请确认是否已在支付宝完成支付</p>
        <div class="flex flex-col items-center gap-3">
          <button class="btn-primary px-6 py-2" @click="checkNow">我已支付，查询状态</button>
          <button class="border border-gray-300 text-gray-600 px-6 py-2 rounded-lg hover:bg-gray-50 text-sm" @click="goToAlipayAgain">
            重新跳转到支付宝
          </button>
        </div>
      </template>

      <!-- 错误状态 -->
      <div v-else-if="error" class="mt-4">
        <div class="bg-red-50 text-red-600 rounded-lg px-6 py-3 mb-4">
          <p class="text-sm">{{ error }}</p>
        </div>
        <div class="flex flex-col items-center gap-3">
          <button class="btn-primary px-6 py-2" @click="retry">重新支付</button>
          <router-link to="/orders" class="text-sm text-gray-500 hover:text-primary underline">返回订单列表</router-link>
        </div>
      </div>

      <!-- 支付宝表单容器（隐藏） -->
      <div ref="formContainer" style="display:none" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Wallet, Clock } from 'lucide-vue-next'
import { ElMessage } from 'element-plus'
import { createAlipayPay, getOrderDetail } from '@/api/order'

const POLL_KEY = 'pending_alipay_order'
const POLL_INTERVAL = 3000
const POLL_MAX_COUNT = 10 // 最多轮询10次（30秒）
const REDIRECT_TIMEOUT = 5 // 5秒未跳转则自动重新跳转

const route = useRoute()
const router = useRouter()
const formContainer = ref(null)
const error = ref('')
const submitted = ref(false)
const polling = ref(false)
const pollCount = ref(0)
const redirectCountdown = ref(0)
let pollTimer = null
let redirectTimer = null
let savedFormHtml = '' // 保存表单HTML用于重新跳转

async function doPay() {
  const orderId = route.params.id
  if (!orderId) {
    error.value = '订单号无效'
    return
  }
  error.value = ''

  try {
    const res = await createAlipayPay(orderId)
    if (res.code === 200 && res.data) {
      savedFormHtml = res.data
      // 保存到 localStorage
      localStorage.setItem(POLL_KEY, JSON.stringify({ orderId, startTime: Date.now() }))
      // 注入表单并提交（POST方式，必须用 form.submit()）
      const wrapper = document.createElement('div')
      wrapper.innerHTML = savedFormHtml
      document.body.appendChild(wrapper)
      const form = wrapper.querySelector('form')
      if (form) {
        form.submit()
        submitted.value = true
        polling.value = true
        startPolling(orderId)
        // 5秒后未跳转则自动重新跳转
        redirectCountdown.value = REDIRECT_TIMEOUT
        redirectTimer = setInterval(() => {
          redirectCountdown.value--
          if (redirectCountdown.value <= 0) {
            clearInterval(redirectTimer)
            redirectTimer = null
            goToAlipayAgain()
          }
        }, 1000)
      } else {
        document.body.removeChild(wrapper)
        error.value = '支付表单生成失败，请稍后重试'
      }
    } else {
      error.value = res.msg || '创建支付失败'
    }
  } catch (err) {
    console.error('创建支付失败:', err)
    error.value = '网络异常，请检查后重试'
  }
}

function startPolling(orderId) {
  pollCount.value = 0
  pollTimer = setInterval(async () => {
    pollCount.value++
    try {
      const res = await getOrderDetail(orderId)
      if (res.code === 200 && res.data && res.data.status >= 1) {
        // 支付成功
        clearInterval(pollTimer)
        pollTimer = null
        polling.value = false
        localStorage.removeItem(POLL_KEY)
        router.replace('/payment/success?id=' + orderId)
        return
      }
    } catch { /* 忽略 */ }
    // 轮询达到上限
    if (pollCount.value >= POLL_MAX_COUNT) {
      clearInterval(pollTimer)
      pollTimer = null
      polling.value = false
      savedFormHtml = localStorage.getItem(POLL_KEY + '_form') || savedFormHtml
    }
  }, POLL_INTERVAL)
}

async function checkNow() {
  const orderId = route.params.id
  try {
    const res = await getOrderDetail(orderId)
    if (res.code === 200 && res.data && res.data.status >= 1) {
      ElMessage.success('支付成功！')
      localStorage.removeItem(POLL_KEY)
      router.replace('/payment/success?id=' + orderId)
    } else {
      ElMessage.info('暂未检测到支付成功，请确认已在支付宝完成支付')
    }
  } catch {
    ElMessage.error('查询失败')
  }
}

function goToAlipayAgain() {
  if (savedFormHtml) {
    const div = document.createElement('div')
    div.innerHTML = savedFormHtml
    formContainer.value.appendChild(div)
    const form = div.querySelector('form')
    if (form) {
      polling.value = true
      const orderId = route.params.id
      form.submit()
      startPolling(orderId)
      // 重新启动倒计时
      redirectCountdown.value = REDIRECT_TIMEOUT
      redirectTimer = setInterval(() => {
        redirectCountdown.value--
        if (redirectCountdown.value <= 0) {
          clearInterval(redirectTimer)
          redirectTimer = null
          goToAlipayAgain()
        }
      }, 1000)
    }
  } else {
    doPay()
  }
}

function retry() {
  error.value = ''
  submitted.value = false
  polling.value = false
  doPay()
}

// 页面加载时检查是否有待轮询的订单
async function checkPendingPayment() {
  const pending = localStorage.getItem(POLL_KEY)
  if (pending) {
    try {
      const { orderId, startTime } = JSON.parse(pending)
      if (orderId && Date.now() - startTime < 60000) {
        submitted.value = true
        // **先立即查询一次**，因为可能已经支付成功
        const res = await getOrderDetail(orderId)
        if (res.code === 200 && res.data && res.data.status >= 1) {
          localStorage.removeItem(POLL_KEY)
          router.replace('/payment/success?id=' + orderId)
          return
        }
        // 未支付 → 启动轮询
        polling.value = true
        startPolling(orderId)
      } else {
        localStorage.removeItem(POLL_KEY)
      }
    } catch {
      localStorage.removeItem(POLL_KEY)
    }
  }
}

onMounted(() => {
  checkPendingPayment()
  if (!submitted.value) {
    doPay()
  }
})

onUnmounted(() => {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
  if (redirectTimer) {
    clearInterval(redirectTimer)
    redirectTimer = null
  }
})
</script>
