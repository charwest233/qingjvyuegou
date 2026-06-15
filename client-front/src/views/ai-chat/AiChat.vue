<template>
  <div class="flex flex-col h-[calc(100vh-8rem)] bg-gradient-to-b from-teal-50/30 to-white">
    <!-- 顶部栏 -->
    <div class="sticky top-0 z-10 bg-white/80 backdrop-blur-md border-b border-gray-100 px-4 py-3 flex items-center justify-between flex-shrink-0">
      <div class="flex items-center gap-3">
        <button @click="showSessionDrawer = true" class="p-2 hover:bg-teal-50 rounded-xl transition-colors">
          <Menu class="w-5 h-5 text-text-secondary" />
        </button>
        <div class="flex items-center gap-2">
          <div class="w-8 h-8 bg-gradient-to-br from-primary to-primary-light rounded-lg flex items-center justify-center">
            <Bot class="w-5 h-5 text-white" />
          </div>
          <div>
            <h1 class="text-sm font-heading font-semibold text-text-primary">AI导购</h1>
            <p class="text-xs text-text-secondary">悦选智能助手</p>
          </div>
        </div>
      </div>
      <div class="flex items-center gap-2">
        <el-select
          v-model="currentModel"
          size="small"
          class="!w-56"
          placeholder="选择模型"
          :teleported="false"
          :fit-input-width="true"
          @change="handleModelChange"
        >
          <el-option
            v-for="m in modelList"
            :key="m.id"
            :label="m.name"
            :value="m.id"
          />
        </el-select>
      </div>
    </div>

    <!-- 聊天区域 -->
    <div ref="chatContainer" class="flex-1 overflow-y-auto px-4 py-4 space-y-4">
      <!-- 欢迎页面 -->
      <div v-if="messages.length === 0 && !loadingMessages" class="flex flex-col items-center justify-center h-full text-center py-12">
        <div class="w-24 h-24 bg-gradient-to-br from-primary/20 to-primary/5 rounded-full flex items-center justify-center mb-6 animate-float">
          <Bot class="w-12 h-12 text-primary" />
        </div>
        <h2 class="text-xl font-heading font-semibold text-text-primary mb-2">嗨，我是悦选导购！</h2>
        <p class="text-sm text-text-secondary max-w-xs mb-8">我可以帮你推荐商品、对比产品、解答购物疑问</p>
        <div class="grid grid-cols-1 gap-3 w-full max-w-sm">
          <button
            v-for="suggestion in suggestions"
            :key="suggestion.text"
            class="text-left px-4 py-3 bg-white border border-gray-200 rounded-xl text-sm text-text-secondary
                   hover:border-primary hover:text-primary hover:bg-primary/5 transition-all cursor-pointer"
            @click="sendSuggestion(suggestion.text)"
          >
            {{ suggestion.text }}
          </button>
        </div>
      </div>

      <!-- 消息列表 -->
      <template v-if="loadingMessages">
        <div v-for="n in 3" :key="n" class="flex gap-3" :class="n % 2 === 0 ? 'justify-end' : ''">
          <div class="max-w-[80%]">
            <div class="h-10 bg-gray-200 rounded-xl animate-pulse" :class="n % 2 === 0 ? 'w-32' : 'w-48'" />
          </div>
        </div>
      </template>
      <template v-else>
        <div
          v-for="(msg, index) in messages"
          :key="index"
          class="flex gap-3"
          :class="msg.role === 'user' ? 'justify-end' : 'justify-start'"
        >
          <!-- AI 头像 -->
          <div v-if="msg.role === 'assistant'" class="flex-shrink-0 mt-1">
            <div class="w-8 h-8 bg-gradient-to-br from-primary to-primary-light rounded-full flex items-center justify-center">
              <Bot class="w-4 h-4 text-white" />
            </div>
          </div>

          <div class="max-w-[85%] sm:max-w-[70%]">
            <div
              class="px-4 py-3 rounded-2xl text-sm leading-relaxed whitespace-pre-wrap break-words"
              :class="msg.role === 'user'
                ? 'bg-primary text-white rounded-br-md'
                : 'bg-white border border-gray-100 shadow-sm rounded-bl-md'"
            >
              <!-- 图片消息 -->
              <img
                v-if="msg.imageUrl"
                :src="msg.imageUrl"
                class="max-w-full max-h-48 rounded-lg mb-2 object-cover"
                @click="previewImage(msg.imageUrl)"
              />
              <!-- AI 回复支持 Markdown 渲染 -->
              <div v-if="msg.role === 'assistant'" class="ai-markdown" v-html="renderMarkdown(msg.content)" />
              <div v-else>{{ msg.content }}</div>
              <!-- 打字光标 -->
              <span
                v-if="msg.role === 'assistant' && index === messages.length - 1 && isStreaming"
                class="inline-block w-2 h-4 bg-primary ml-0.5 animate-pulse rounded-sm"
              />
            </div>
            <p class="text-xs text-gray-400 mt-1 px-1">
              {{ msg.role === 'user' ? '我' : 'AI导购' }}
            </p>
          </div>
        </div>
      </template>
    </div>

    <!-- 底部输入区 -->
    <div class="flex-shrink-0 bg-white border-t border-gray-100 px-4 py-3">
      <!-- 图片预览 -->
      <div v-if="previewImageUrl" class="mb-2 flex items-center gap-2">
        <div class="relative inline-block">
          <img :src="previewImageUrl" class="h-16 w-16 rounded-lg object-cover border border-gray-200" />
          <button
            class="absolute -top-2 -right-2 w-5 h-5 bg-red-500 text-white rounded-full flex items-center justify-center text-xs cursor-pointer hover:bg-red-600 transition-colors"
            @click="clearImage"
          >
            <X class="w-3 h-3" />
          </button>
        </div>
        <span class="text-xs text-text-secondary">已添加图片</span>
      </div>

      <div class="flex items-end gap-2 bg-gray-50 rounded-2xl px-4 py-2 border border-gray-200 focus-within:border-primary focus-within:ring-2 focus-within:ring-primary/20 transition-all">
        <button
          class="p-1.5 hover:bg-gray-200 rounded-lg transition-colors cursor-pointer flex-shrink-0"
          @click="triggerImageUpload"
          :disabled="isStreaming"
        >
          <ImagePlus class="w-5 h-5 text-gray-400" />
        </button>
        <input
          ref="imageInput"
          type="file"
          accept="image/*"
          class="hidden"
          @change="handleImageSelect"
        />
        <textarea
          v-model="inputText"
          class="flex-1 bg-transparent border-none outline-none resize-none text-sm py-1 max-h-24 text-text-primary placeholder:text-gray-400"
          rows="1"
          placeholder="搜索商品、加购物车、查订单..."
          @keydown.enter.exact="handleSend"
          @input="autoResize"
        />
        <button
          class="p-2 rounded-xl transition-all flex-shrink-0 cursor-pointer"
          :class="canSend
            ? 'bg-primary text-white hover:bg-primary-dark shadow-md shadow-primary/20'
            : 'bg-gray-200 text-gray-400 cursor-not-allowed'"
          :disabled="!canSend"
          @click="handleSend"
        >
          <Send v-if="!isStreaming" class="w-4 h-4" />
          <Square v-else class="w-4 h-4" @click.stop="stopStreaming" />
        </button>
      </div>
      <p class="text-xs text-gray-400 text-center mt-2">AI回复仅供参考，具体以商品详情为准</p>
    </div>

    <!-- 会话列表抽屉 -->
    <Teleport to="body">
      <Transition name="drawer">
        <div v-if="showSessionDrawer" class="fixed inset-0 z-50 flex">
          <!-- 遮罩 -->
          <div class="absolute inset-0 bg-black/30" @click="showSessionDrawer = false" />
          <!-- 抽屉 -->
          <div class="relative w-72 max-w-[80vw] h-full bg-white shadow-xl flex flex-col">
            <div class="flex items-center justify-between p-4 border-b border-gray-100">
              <h2 class="text-sm font-heading font-semibold text-text-primary">历史对话</h2>
              <button class="p-1 hover:bg-gray-100 rounded-lg transition-colors cursor-pointer" @click="showSessionDrawer = false">
                <X class="w-5 h-5 text-gray-400" />
              </button>
            </div>
            <div class="p-3">
              <button
                class="w-full flex items-center justify-center gap-2 px-4 py-2.5 bg-primary text-white rounded-xl text-sm font-medium
                       hover:bg-primary-dark transition-all cursor-pointer shadow-md shadow-primary/20"
                @click="handleNewSession"
              >
                <Plus class="w-4 h-4" />
                新建对话
              </button>
            </div>
            <div class="flex-1 overflow-y-auto px-3 pb-4 space-y-1">
              <div v-if="sessions.length === 0 && !loadingSessions" class="text-center py-8">
                <MessageSquare class="w-10 h-10 text-gray-300 mx-auto mb-2" />
                <p class="text-xs text-gray-400">暂无历史对话</p>
              </div>
              <button
                v-for="session in sessions"
                :key="session.id"
                class="w-full text-left px-3 py-3 rounded-xl transition-all cursor-pointer group"
                :class="currentSessionId === session.id
                  ? 'bg-primary/10 border border-primary/30'
                  : 'hover:bg-gray-50 border border-transparent'"
                @click="switchSession(session.id)"
              >
                <div class="flex items-start justify-between gap-2">
                  <div class="flex-1 min-w-0">
                    <p class="text-sm font-medium text-text-primary truncate">{{ session.title }}</p>
                    <p class="text-xs text-text-secondary mt-0.5">
                      <Bot class="w-3 h-3 inline mr-1" />{{ session.model?.split('/').pop() || '未知模型' }}
                    </p>
                    <p class="text-xs text-gray-400 mt-0.5">{{ formatTime(session.createdAt) }}</p>
                  </div>
                  <button
                    class="p-1.5 opacity-0 group-hover:opacity-100 hover:bg-red-50 rounded-lg transition-all cursor-pointer flex-shrink-0"
                    @click.stop="handleDeleteSession(session.id)"
                  >
                    <Trash class="w-4 h-4 text-red-400" />
                  </button>
                </div>
              </button>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- 图片预览弹窗 -->
    <Teleport to="body">
      <Transition name="fade">
        <div
          v-if="showImagePreview"
          class="fixed inset-0 z-50 bg-black/80 flex items-center justify-center p-4"
          @click="showImagePreview = false"
        >
          <img :src="previewImageUrl" class="max-w-full max-h-full object-contain rounded-lg" />
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Bot, Menu, Plus, Send, Square, X, ImagePlus, Trash, MessageSquare
} from 'lucide-vue-next'
import { getModels, createSession, listSessions, deleteSession, listMessages, streamChat } from '@/api/ai'

const route = useRoute()
const router = useRouter()

// 状态
const inputText = ref('')
const messages = ref([])
const sessions = ref([])
const modelList = ref([])
const currentModel = ref('deepseek-ai/DeepSeek-V3')
const currentSessionId = ref(null)
const isStreaming = ref(false)
const loadingMessages = ref(false)
const loadingSessions = ref(false)
const showSessionDrawer = ref(false)
const showImagePreview = ref(false)
const previewImageUrl = ref('')
const selectedImage = ref(null)
const chatContainer = ref(null)
const imageInput = ref(null)

// SSE 流式控制
let abortController = null
let streamReader = null

const canSend = computed(() => inputText.value.trim().length > 0 && !isStreaming.value)

// 推荐问题
const suggestions = [
  { text: '帮我把草莓加到购物车' },
  { text: '看看我的购物车有什么' },
  { text: '帮我搜一下蓝牙耳机，推荐几款' },
  { text: '我想买一台性价比高的笔记本电脑' }
]

// 方法
function formatTime(time) {
  if (!time) return ''
  const d = new Date(time)
  const now = new Date()
  const isToday = d.toDateString() === now.toDateString()
  const yesterday = new Date(now)
  yesterday.setDate(yesterday.getDate() - 1)
  const isYesterday = d.toDateString() === yesterday.toDateString()

  if (isToday) return `今天 ${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`
  if (isYesterday) return `昨天 ${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`
  return `${d.getMonth() + 1}/${d.getDate()} ${d.getHours().toString().padStart(2, '0')}:${d.getMinutes().toString().padStart(2, '0')}`
}

async function loadModels() {
  try {
    const res = await getModels()
    if (res.code === 200 && res.data) {
      modelList.value = res.data
    }
  } catch (err) {
    console.error('加载模型列表失败:', err)
  }
}

async function loadSessions() {
  loadingSessions.value = true
  try {
    const res = await listSessions()
    if (res.code === 200) {
      sessions.value = res.data || []
    }
  } catch (err) {
    console.error('加载会话列表失败:', err)
  } finally {
    loadingSessions.value = false
  }
}

async function loadMessages(sessionId) {
  loadingMessages.value = true
  messages.value = []
  try {
    const res = await listMessages(sessionId)
    if (res.code === 200) {
      messages.value = res.data || []
    }
  } catch (err) {
    console.error('加载消息失败:', err)
  } finally {
    loadingMessages.value = false
  }
  await nextTick()
  scrollToBottom()
}

async function handleNewSession() {
  try {
    const res = await createSession(currentModel.value)
    if (res.code === 200 && res.data) {
      currentSessionId.value = res.data.id
      messages.value = []
      showSessionDrawer.value = false
      await loadSessions()
      await router.replace({ name: 'AiChatSession', params: { id: res.data.id } })
    }
  } catch (err) {
    console.error('创建会话失败:', err)
  }
}

async function switchSession(id) {
  currentSessionId.value = id
  showSessionDrawer.value = false
  await router.replace({ name: 'AiChatSession', params: { id } })
  await loadMessages(id)
}

async function handleDeleteSession(id) {
  try {
    await ElMessageBox.confirm('确定要删除这个对话吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await deleteSession(id)
    if (res.code === 200) {
      ElMessage.success('已删除')
      sessions.value = sessions.value.filter(s => s.id !== id)
      if (currentSessionId.value === id) {
        currentSessionId.value = null
        messages.value = []
      }
    }
  } catch (err) {
    // 用户取消
  }
}

function handleModelChange() {
  // 模型切换仅影响新创建的会话
}

function sendSuggestion(text) {
  inputText.value = text
  handleSend()
}

function triggerImageUpload() {
  imageInput.value?.click()
}

function handleImageSelect(e) {
  const file = e.target.files?.[0]
  if (file) {
    selectedImage.value = file
    previewImageUrl.value = URL.createObjectURL(file)
  }
  e.target.value = ''
}

function clearImage() {
  selectedImage.value = null
  previewImageUrl.value = ''
}

function previewImage(url) {
  previewImageUrl.value = url
  showImagePreview.value = true
}

async function handleSend() {
  const text = inputText.value.trim()
  if (!text && !selectedImage.value) return

  if (!currentSessionId.value) {
    try {
      const res = await createSession(currentModel.value)
      if (res.code === 200 && res.data) {
        currentSessionId.value = res.data.id
        await router.replace({ name: 'AiChatSession', params: { id: res.data.id } })
        await loadSessions()
      } else {
        return
      }
    } catch (err) {
      return
    }
  }

  // 添加用户消息到界面
  const userMsg = {
    id: Date.now(),
    role: 'user',
    content: text,
    imageUrl: previewImageUrl.value || null,
    createdAt: new Date().toISOString()
  }
  messages.value.push(userMsg)
  inputText.value = ''
  const imageFile = selectedImage.value
  clearImage()

  // 添加占位 AI 消息
  const aiMsg = { id: Date.now() + 1, role: 'assistant', content: '' }
  messages.value = [...messages.value, aiMsg]
  isStreaming.value = true

  await nextTick()
  scrollToBottom()

  // 发起 SSE 流式请求
  try {
    const { streamPromise, abort } = streamChat(currentSessionId.value, text, imageFile)
    abortController = abort

    const response = await streamPromise
    if (!response.ok) {
      throw new Error(`HTTP ${response.status}`)
    }

    const reader = response.body.getReader()
    streamReader = reader
    const decoder = new TextDecoder()
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''

      let currentEvent = ''
      for (const line of lines) {
        if (line.startsWith('event:')) {
          currentEvent = line.substring(6).trim()
        } else if (line.startsWith('data:')) {
          const data = line.startsWith('data: ') ? line.substring(6) : line.substring(5)
          if (currentEvent === 'message') {
            aiMsg.content += data
            // 替换数组引用触发深层响应更新
            messages.value = [...messages.value]
            scrollToBottomThrottled()
          } else if (currentEvent === 'done') {
            // 流结束
          } else if (currentEvent === 'error') {
            aiMsg.content = data || '抱歉，AI回复出错了，请稍后再试。'
            messages.value = [...messages.value]
          }
        }
      }
    }
  } catch (err) {
    if (err.name === 'AbortError') {
      // 用户手动停止
    } else {
      console.error('AI流式请求失败:', err)
      aiMsg.content = '抱歉，网络连接出错了，请稍后再试。'
      messages.value = [...messages.value]
    }
  } finally {
    isStreaming.value = false
    abortController = null
    streamReader = null
    scrollToBottom()
  }
}

function stopStreaming() {
  if (abortController) {
    abortController.abort()
  }
  isStreaming.value = false
}

/**
 * 简单 Markdown → HTML 渲染（内联实现，无需额外依赖）
 * 支持：**加粗**、*斜体*、`代码`、有序/无序列表、换行
 */
function renderMarkdown(text) {
  if (!text) return ''
  let html = text
    // 转义 HTML 特殊字符（防止 XSS）
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    // 粗体 **text**
    .replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>')
    // 斜体 *text*
    .replace(/\*(.+?)\*/g, '<em>$1</em>')
    // 行内代码 `code`
    .replace(/`(.+?)`/g, '<code class="inline-code">$1</code>')
    // 标题 ### text
    .replace(/^###\s+(.+)$/gm, '<h4 class="md-h4">$1</h4>')
    .replace(/^##\s+(.+)$/gm, '<h3 class="md-h3">$1</h3>')
    .replace(/^#\s+(.+)$/gm, '<h2 class="md-h2">$1</h2>')
    // 有序列表 1. / 2.
    .replace(/^\d+\.\s+(.+)$/gm, '<li class="md-li">$1</li>')
    // 无序列表 - / *
    .replace(/^[-*]\s+(.+)$/gm, '<li class="md-li">$1</li>')
    // 连续 <li> 包裹在 <ul> 中
    .replace(/(<li class="md-li">.*?<\/li>)\n(?=<li class="md-li">)/g, '$1')
  // 将连续 <li> 块包裹为 <ul>
  html = html.replace(/((?:<li class="md-li">.*?<\/li>)+)/g, '<ul class="md-ul">$1</ul>')
  // 普通换行 → <br>
  html = html.replace(/\n/g, '<br>')
  return html
}

function autoResize(e) {
  const el = e.target
  el.style.height = 'auto'
  el.style.height = el.scrollHeight + 'px'
}

// 节流：流式输出时最多每 60ms 滚动一次
let lastScrollTime = 0
function scrollToBottom() {
  nextTick(() => {
    if (chatContainer.value) {
      chatContainer.value.scrollTop = chatContainer.value.scrollHeight
    }
  })
}
function scrollToBottomThrottled() {
  const now = Date.now()
  if (now - lastScrollTime > 60) {
    lastScrollTime = now
    scrollToBottom()
  }
}

// 路由参数变化时加载对应会话
watch(() => route.params.id, async (newId) => {
  if (newId && newId !== currentSessionId.value) {
    currentSessionId.value = newId
    await loadMessages(newId)
  } else if (!newId) {
    currentSessionId.value = null
    messages.value = []
  }
})

onMounted(async () => {
  await loadModels()
  await loadSessions()

  // 根据 URL 参数加载会话
  const sessionId = route.params.id
  if (sessionId) {
    currentSessionId.value = sessionId
    await loadMessages(sessionId)
  } else if (sessions.value.length > 0) {
    // 无指定会话时，自动衔接最近一次对话
    const lastSession = sessions.value[0]
    currentSessionId.value = lastSession.id
    await router.replace({ name: 'AiChatSession', params: { id: lastSession.id } })
    await loadMessages(lastSession.id)
  }
})
</script>

<style scoped>
/* 自定义滚动条 */
.overflow-y-auto::-webkit-scrollbar {
  width: 4px;
}
.overflow-y-auto::-webkit-scrollbar-track {
  background: transparent;
}
.overflow-y-auto::-webkit-scrollbar-thumb {
  background: #14B8A6;
  border-radius: 2px;
}

/* 抽屉过渡动画 */
.drawer-enter-active,
.drawer-leave-active {
  transition: all 0.25s ease;
}
.drawer-enter-from,
.drawer-leave-to {
  opacity: 0;
}
.drawer-enter-from > div:last-child,
.drawer-leave-to > div:last-child {
  transform: translateX(-100%);
}
.drawer-enter-active > div:last-child,
.drawer-leave-active > div:last-child {
  transition: transform 0.25s ease;
}

/* 淡入动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* AI Markdown 渲染样式 */
.ai-markdown :deep(strong) {
  font-weight: 600;
  color: #0F172A;
}
.ai-markdown :deep(em) {
  font-style: italic;
  color: #475569;
}
.ai-markdown :deep(.inline-code) {
  background: #F1F5F9;
  color: #0EA5E9;
  padding: 1px 6px;
  border-radius: 4px;
  font-size: 0.9em;
  font-family: 'SF Mono', 'Menlo', monospace;
}
.ai-markdown :deep(.md-h2) {
  font-size: 1.1em;
  font-weight: 700;
  margin: 8px 0 4px;
  color: #0F172A;
}
.ai-markdown :deep(.md-h3) {
  font-size: 1em;
  font-weight: 600;
  margin: 6px 0 3px;
  color: #1E293B;
}
.ai-markdown :deep(.md-h4) {
  font-size: 0.95em;
  font-weight: 600;
  margin: 4px 0 2px;
  color: #334155;
}
.ai-markdown :deep(.md-ul) {
  margin: 4px 0;
  padding-left: 18px;
  list-style: none;
}
.ai-markdown :deep(.md-li) {
  position: relative;
  padding-left: 4px;
  margin: 2px 0;
  line-height: 1.5;
}
.ai-markdown :deep(.md-li::before) {
  content: '•';
  position: absolute;
  left: -14px;
  color: #14B8A6;
  font-weight: 700;
}
</style>
