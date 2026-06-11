<template>
  <div class="flex flex-col h-[calc(100vh-8rem)] bg-gradient-to-b from-blue-50/30 to-white">
    <div class="sticky top-0 z-10 bg-white/80 backdrop-blur-md border-b border-gray-100 px-4 py-3 flex items-center justify-between flex-shrink-0">
      <div class="flex items-center gap-3">
        <button @click="$router.back()" class="p-2 hover:bg-gray-100 rounded-xl transition-colors cursor-pointer">
          <ArrowLeft class="w-5 h-5 text-text-secondary" />
        </button>
        <div class="flex items-center gap-2">
          <div class="w-8 h-8 bg-gradient-to-br from-blue-500 to-blue-400 rounded-lg flex items-center justify-center">
            <Headset class="w-5 h-5 text-white" />
          </div>
          <div>
            <h1 class="text-sm font-heading font-semibold text-text-primary">人工客服</h1>
            <p class="text-xs" :class="connected ? 'text-green-500' : 'text-gray-400'">{{ connected ? '在线' : '连接中...' }}</p>
          </div>
        </div>
      </div>
      <button class="flex items-center gap-1.5 px-3 py-1.5 text-xs font-medium rounded-lg transition-all cursor-pointer"
        :class="showOrderPanel ? 'bg-blue-50 text-blue-600' : 'bg-gray-50 text-gray-500 hover:bg-gray-100'"
        @click="showOrderPanel = !showOrderPanel">
        <PackageSearch class="w-4 h-4" />查订单
      </button>
    </div>

    <div ref="chatContainer" class="flex-1 overflow-y-auto px-4 py-4 space-y-4">
      <div v-if="messages.length === 0" class="flex flex-col items-center justify-center h-full text-center py-12">
        <div class="w-24 h-24 bg-gradient-to-br from-blue-500/20 to-blue-400/5 rounded-full flex items-center justify-center mb-6">
          <Headset class="w-12 h-12 text-blue-500" />
        </div>
        <h2 class="text-xl font-heading font-semibold text-text-primary mb-2">您好，这里是人工客服</h2>
        <p class="text-sm text-text-secondary max-w-xs mb-8">您可以咨询订单问题、售后申请，或直接发送文字消息</p>
        <div class="grid grid-cols-1 gap-3 w-full max-w-sm">
          <button v-for="q in quickReplies" :key="q.text"
            class="text-left px-4 py-3 bg-white border border-gray-200 rounded-xl text-sm text-text-secondary hover:border-blue-400 hover:text-blue-600 hover:bg-blue-50/50 transition-all cursor-pointer"
            @click="sendMessage(q.text)">{{ q.text }}</button>
        </div>
      </div>
      <div v-for="(msg, index) in messages" :key="index" class="flex gap-3" :class="msg.role === 'user' ? 'justify-end' : 'justify-start'">
        <div v-if="msg.role === 'agent'" class="flex-shrink-0 mt-1">
          <div class="w-8 h-8 bg-gradient-to-br from-blue-500 to-blue-400 rounded-full flex items-center justify-center"><Headset class="w-4 h-4 text-white" /></div>
        </div>
        <div class="max-w-[85%] sm:max-w-[70%]">
          <div v-if="msg.content" class="px-4 py-3 rounded-2xl text-sm leading-relaxed whitespace-pre-wrap break-words"
            :class="msg.role === 'user' ? 'bg-primary text-white rounded-br-md' : 'bg-white border border-gray-100 shadow-sm rounded-bl-md'">{{ msg.content }}</div>
          <span class="text-[10px] text-gray-400 mt-1 block" :class="msg.role === 'user' ? 'text-right' : 'text-left'">{{ formatMsgTime(msg.time) }}</span>
        </div>
        <div v-if="msg.role === 'user'" class="flex-shrink-0 mt-1">
          <div class="w-8 h-8 bg-gradient-to-br from-primary to-primary-light rounded-full flex items-center justify-center"><User class="w-4 h-4 text-white" /></div>
        </div>
      </div>
    </div>

    <transition name="slide-up">
      <div v-if="showOrderPanel" class="border-t border-gray-100 bg-white flex-shrink-0 max-h-[260px] flex flex-col">
        <div class="flex items-center justify-between gap-2 px-4 py-2.5 border-b border-gray-50">
          <div class="flex items-center gap-2">
            <PackageSearch class="w-4 h-4 text-blue-500" />
            <span class="text-sm font-medium text-gray-700">选择订单咨询</span>
          </div>
          <span v-if="orderLoading" class="text-xs text-gray-400">加载中...</span>
        </div>
        <div class="overflow-y-auto flex-1 px-2 py-1">
          <div v-if="orderList.length === 0 && !orderLoading" class="text-center py-6 text-xs text-gray-400">
            暂无订单
          </div>
          <div
            v-for="o in orderList" :key="o.id"
            class="flex items-center gap-3 px-3 py-3 rounded-lg hover:bg-blue-50 transition-colors cursor-pointer border-b border-gray-50 last:border-b-0"
            @click="selectOrder(o)"
          >
            <div class="w-10 h-10 rounded-lg bg-gray-100 flex-shrink-0 overflow-hidden">
              <img v-if="o.items?.[0]?.productImage" :src="o.items[0].productImage" class="w-full h-full object-cover" alt="" />
              <div v-else class="w-full h-full flex items-center justify-center text-gray-300"><PackageSearch class="w-5 h-5" /></div>
            </div>
            <div class="flex-1 min-w-0">
              <div class="text-sm font-medium text-gray-800 truncate">{{ o.items?.[0]?.productName || '订单' }}</div>
              <div class="text-xs text-gray-400 mt-0.5 truncate">
                {{ o.orderNo }} · ¥{{ o.totalAmount?.toFixed(2) }}
                <span v-if="(o.items?.length || 0) > 1" class="text-gray-300">等{{ o.items.length }}件商品</span>
              </div>
            </div>
            <div class="text-xs px-2 py-0.5 rounded-full flex-shrink-0" :class="statusBadgeClass(o.status)">{{ ORDER_STATUS_MAP[o.status] || '未知' }}</div>
          </div>
        </div>
      </div>
    </transition>

    <div class="border-t border-gray-100 bg-white px-4 py-3 flex items-center gap-3 flex-shrink-0">
      <input v-model="inputText" type="text" placeholder="输入消息..." class="flex-1 px-4 py-2.5 text-sm bg-gray-50 border border-gray-200 rounded-xl focus:border-primary focus:ring-1 focus:ring-primary/20 outline-none transition-all" :disabled="!connected" @keyup.enter="handleSend" />
      <button class="w-10 h-10 rounded-xl bg-primary text-white flex items-center justify-center hover:bg-primary-dark transition-colors cursor-pointer disabled:opacity-50" :disabled="!inputText.trim() || !connected" @click="handleSend"><Send class="w-5 h-5" /></button>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, onUnmounted, watch } from 'vue'
import { ArrowLeft, Headset, PackageSearch, Send, User } from 'lucide-vue-next'
import { ElMessage } from 'element-plus'
import { getUser } from '@/utils/auth'
import request from '@/api/request'
import { getOrders } from '@/api/order'

const chatContainer = ref(null)
const messages = ref([])
const inputText = ref('')
const connected = ref(false)
const showOrderPanel = ref(false)
const orderList = ref([])
const orderLoading = ref(false)
let ws = null
let reconnectTimer = null

const ORDER_STATUS_MAP = {
  0: '待付款', 1: '已付款', 2: '已发货', 3: '已完成', 4: '已取消'
}

const quickReplies = [
  { text: '我的订单什么时候发货？' }, { text: '如何申请退款？' },
  { text: '商品有问题，怎么售后？' }, { text: '帮我查一下最近的订单' }
]

function formatMsgTime(t) { if (!t) return ''; const d = new Date(t); return `${d.getHours().toString().padStart(2,'0')}:${d.getMinutes().toString().padStart(2,'0')}` }

async function loadHistory() {
  const user = getUser()
  if (!user || !user.id) return
  try {
    const res = await request.get(`/customer-service/messages/${user.id}`)
    if (res.code === 200 && res.data) {
      messages.value = res.data.filter(m => m.content).map(m => ({
        role: m.senderType === 1 ? 'user' : 'agent',
        content: m.content,
        time: m.createdAt
      }))
      scrollToBottom()
    }
  } catch(e) {}
}

function connect() {
  const user = getUser()
  if (!user || !user.id) return
  const nickname = encodeURIComponent(user.nickname || user.phone || '用户')
  const wsUrl = `ws://localhost:8080/ws/customer-service/user_${user.id}_${nickname}`
  ws = new WebSocket(wsUrl)
  ws.onopen = () => {
    connected.value = true
    if (reconnectTimer) { clearTimeout(reconnectTimer); reconnectTimer = null }
    loadHistory()
  }
  ws.onmessage = (event) => {
    try {
      const data = JSON.parse(event.data)
      if (data.type === 'message') {
        messages.value.push({ role: 'agent', content: data.content, time: data.time || new Date().toISOString() })
        scrollToBottom()
      } else if (data.type === 'system') {
        ElMessage.info(data.content)
      }
    } catch (e) { console.error('消息解析失败:', e) }
  }
  ws.onclose = () => { connected.value = false; reconnectTimer = setTimeout(connect, 3000) }
  ws.onerror = () => {}
}

function sendMessage(text) {
  messages.value.push({ role: 'user', content: text, time: new Date().toISOString() })
  scrollToBottom()
  if (ws && ws.readyState === WebSocket.OPEN) {
    ws.send(JSON.stringify({ type: 'message', content: text }))
  } else { ElMessage.warning('客服暂时不在线') }
}

function handleSend() { const text = inputText.value.trim(); if (text) { sendMessage(text); inputText.value = '' } }

function statusBadgeClass(status) {
  const map = { 0: 'bg-orange-50 text-orange-500', 1: 'bg-blue-50 text-blue-500', 2: 'bg-purple-50 text-purple-500', 3: 'bg-green-50 text-green-500', 4: 'bg-gray-100 text-gray-400' }
  return map[status] || 'bg-gray-100 text-gray-400'
}

async function fetchOrders() {
  orderLoading.value = true
  try {
    const res = await getOrders({ page: 1, size: 50 })
    if (res.code === 200 && res.data) {
      orderList.value = (res.data.list || []).filter(o => o.status !== 4) // 过滤已取消
    }
  } catch (err) { ElMessage.error('加载订单失败') }
  finally { orderLoading.value = false }
}

function selectOrder(order) {
  showOrderPanel.value = false
  const text = `我想咨询订单 ${order.orderNo}（金额：¥${order.totalAmount?.toFixed(2)}，状态：${ORDER_STATUS_MAP[order.status] || '未知'}）`
  sendMessage(text)
}

// 展开订单面板时自动加载订单
watch(showOrderPanel, (val) => { if (val) fetchOrders() })

function scrollToBottom() { nextTick(() => { if (chatContainer.value) chatContainer.value.scrollTop = chatContainer.value.scrollHeight }) }

onMounted(() => connect())
onUnmounted(() => { if (reconnectTimer) clearTimeout(reconnectTimer); ws?.close() })
</script>

<style scoped>
.slide-up-enter-active, .slide-up-leave-active { transition: all .25s ease; }
.slide-up-enter-from, .slide-up-leave-to { opacity: 0; transform: translateY(10px); max-height: 0; }
.slide-up-enter-to, .slide-up-leave-from { opacity: 1; transform: translateY(0); max-height: 120px; }
</style>
