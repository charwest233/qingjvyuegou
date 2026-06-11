<template>
  <div class="cs-page">
    <!-- 左侧会话列表 -->
    <div class="session-panel">
      <div class="panel-header">
        <el-icon :size="20"><ChatDotRound /></el-icon>
        <span>在线客服</span>
        <el-tag :type="wsConnected ? 'success' : 'danger'" size="small" class="ml-auto">
          {{ wsConnected ? '已连接' : '未连接' }}
        </el-tag>
      </div>

      <div class="session-list">
        <div
          v-for="s in sessions"
          :key="s.sessionId"
          class="session-item"
          :class="{ active: currentSession === s.sessionId }"
          @click="selectSession(s.sessionId, s.nickname)"
        >
          <div class="session-avatar">
            <el-avatar :size="36" icon="User" />
          </div>
          <div class="session-info">
            <div class="session-name">{{ s.nickname }}</div>
            <div class="session-last">{{ s.lastMsg }}</div>
          </div>
          <div class="session-time">{{ s.time }}</div>
          <el-badge v-if="s.unread" :value="s.unread" class="session-badge" />
        </div>
        <el-empty v-if="sessions.length === 0" description="暂无咨询" :image-size="60" />
      </div>
    </div>

    <!-- 右侧聊天区域 -->
    <div class="chat-panel">
      <template v-if="currentSession">
        <div class="chat-header">
          <el-avatar :size="32" icon="User" />
          <span>{{ currentNickname }}</span>
        </div>

        <div class="chat-messages" ref="msgContainer">
          <div
            v-for="(msg, i) in messages"
            :key="i"
            class="msg-row"
            :class="msg.senderType === 2 ? 'sent' : 'received'"
          >
            <!-- 用户头像（接收到的消息左侧） -->
            <el-avatar v-if="msg.senderType === 1" :size="32" icon="User" class="msg-avatar" />
            <div v-else class="msg-avatar-placeholder" />

            <div class="msg-bubble" :class="msg.senderType === 2 ? 'sent' : 'received'">
              <div v-if="msg.senderType === 1" class="msg-sender">{{ msg.userNickname || '用户' }}</div>
              <div class="msg-text">{{ msg.content }}</div>
              <div class="msg-time">{{ fmtTime(msg.createdAt) }}</div>
            </div>

            <!-- 客服头像（发送的消息右侧） -->
            <el-avatar v-if="msg.senderType === 2" :size="32" class="msg-avatar agent-avatar">
              <el-icon><User /></el-icon>
            </el-avatar>
            <div v-else class="msg-avatar-placeholder" />
          </div>
          <el-empty v-if="messages.length === 0" description="暂无消息" :image-size="40" />
        </div>

        <div class="chat-input">
          <el-input
            v-model="inputText"
            type="textarea"
            :rows="2"
            placeholder="输入回复内容..."
            resize="none"
            @keydown.enter.exact="sendReply"
          />
          <el-button type="primary" @click="sendReply" :disabled="!inputText.trim() || !wsConnected">
            发送
          </el-button>
        </div>
      </template>
      <div v-else class="chat-empty">
        <el-empty description="选择左侧会话开始回复" :image-size="80" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { ChatDotRound, User } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { csApi } from '@/api/customer-service'

const wsConnected = ref(false)
const currentSession = ref('')
const currentNickname = ref('')
const inputText = ref('')
const messages = ref([])
const sessions = ref([])
const msgContainer = ref(null)
let ws = null

function fmtTime(t) {
  if (!t) return ''
  const d = new Date(t)
  return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

/** 从 DB 拉会话列表 */
async function loadSessions() {
  try {
    const sessions = await csApi.getSessions()
    if (Array.isArray(sessions)) {
      sessions.value = sessions
        .filter(s => /^\d+$/.test(s))
        .map(s => ({ sessionId: s, nickname: '用户', lastMsg: '', time: '', unread: 0 }))
    }
  } catch (e) { /* ignore */ }
}

async function loadMessages(sessionId) {
  try {
    const msgList = await csApi.getMessages(sessionId)
    if (Array.isArray(msgList)) {
      messages.value = msgList
      scrollBottom()
    }
  } catch (e) { messages.value = [] }
}

function selectSession(sessionId, nickname) {
  currentSession.value = sessionId
  currentNickname.value = nickname
  messages.value = []  // 切换会话先清空
  loadMessages(sessionId)
  const s = sessions.value.find(x => x.sessionId === sessionId)
  if (s) s.unread = 0
}

function connectWs() {
  ws = new WebSocket('ws://localhost:8080/ws/customer-service/agent_admin')

  ws.onopen = () => {
    wsConnected.value = true
  }

  ws.onmessage = (e) => {
    try {
      const data = JSON.parse(e.data)
      if (data.type === 'message') {
        const sid = data.sessionId
        updateSession(sid, data.nickname || '用户', data.content)
        // 当前选中的会话有新消息 → 直接追加到本地消息列表
        if (currentSession.value === sid) {
          messages.value.push({
            senderType: 1,
            content: data.content,
            userNickname: data.nickname || '用户',
            createdAt: new Date(data.time || Date.now()).toISOString()
          })
          scrollBottom()
        }
      } else if (data.type === 'system') {
        // 新用户接入 → 直接创建会话条目
        updateSession(data.sessionId, data.nickname || '用户', data.content)
      }
    } catch (ex) { /* ignore */ }
  }

  ws.onclose = () => { wsConnected.value = false; setTimeout(connectWs, 3000) }
  ws.onerror = (err) => { console.error('WS error:', err) }
}

function updateSession(sid, nickname, lastMsg) {
  const exist = sessions.value.find(s => s.sessionId === sid)
  if (exist) {
    exist.lastMsg = lastMsg
    exist.time = fmtTime(new Date())
    if (nickname && nickname !== '用户') exist.nickname = nickname
    if (currentSession.value !== sid) exist.unread = (exist.unread || 0) + 1
  } else {
    sessions.value.unshift({
      sessionId: sid,
      nickname,
      lastMsg,
      time: fmtTime(new Date()),
      unread: currentSession.value === sid ? 0 : 1
    })
  }
}

function sendReply() {
  const text = inputText.value.trim()
  if (!text || !ws || ws.readyState !== WebSocket.OPEN) {
    ElMessage.warning('连接已断开')
    return
  }
  ws.send(JSON.stringify({ type: 'message', content: text, targetUser: currentSession.value }))
  messages.value.push({ senderType: 2, content: text, createdAt: new Date().toISOString() })
  inputText.value = ''
  scrollBottom()
}

function scrollBottom() {
  nextTick(() => {
    if (msgContainer.value) msgContainer.value.scrollTop = msgContainer.value.scrollHeight
  })
}

onMounted(() => { connectWs(); loadSessions() })
onUnmounted(() => ws?.close())
</script>

<style scoped lang="scss">
.cs-page { display: flex; height: calc(100vh - 130px); background: #f5f7fa; border-radius: 8px; overflow: hidden; }

.session-panel { width: 300px; background: #fff; border-right: 1px solid #e4e7ed; display: flex; flex-direction: column; }
.panel-header { display: flex; align-items: center; gap: 8px; padding: 16px; border-bottom: 1px solid #e4e7ed; font-weight: 600; font-size: 15px; color: #303133; }
.session-list { flex: 1; overflow-y: auto; }
.session-item { display: flex; align-items: center; gap: 10px; padding: 12px 16px; cursor: pointer; border-bottom: 1px solid #f0f0f0; transition: background .15s; position: relative;
  &:hover { background: #f5f7fa; }
  &.active { background: #ecf5ff; }
  .session-info { flex: 1; min-width: 0;
    .session-name { font-size: 14px; color: #303133; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
    .session-last { font-size: 12px; color: #909399; margin-top: 2px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
  }
  .session-time { font-size: 11px; color: #c0c4cc; flex-shrink: 0; }
  .session-badge { position: absolute; right: 12px; top: 8px; }
}

.chat-panel { flex: 1; display: flex; flex-direction: column; background: #fff; }
.chat-header { display: flex; align-items: center; gap: 10px; padding: 14px 20px; border-bottom: 1px solid #e4e7ed; font-weight: 600; font-size: 15px; color: #303133; }
.chat-messages { flex: 1; overflow-y: auto; padding: 16px 20px; background: #fafafa; }

/* 消息行 */
.msg-row { display: flex; align-items: flex-start; gap: 8px; margin-bottom: 16px;
  &.received { justify-content: flex-start; }   /* 用户消息在左 */
  &.sent { justify-content: flex-end; }          /* 客服消息在右 */
}
.msg-avatar { flex-shrink: 0; }
.msg-avatar-placeholder { width: 32px; flex-shrink: 0; }
.agent-avatar { background: var(--el-color-primary); color: #fff; }

/* 气泡 */
.msg-bubble { max-width: 55%; padding: 10px 14px; border-radius: 12px; font-size: 14px; line-height: 1.5;
  &.received { background: #fff; border: 1px solid #e4e7ed; border-top-left-radius: 4px; }
  &.sent { background: var(--el-color-primary-light-9); border-top-right-radius: 4px; }
  .msg-sender { font-size: 12px; color: #909399; margin-bottom: 2px; }
  .msg-text { color: #303133; word-break: break-word; }
  .msg-time { font-size: 11px; color: #c0c4cc; margin-top: 4px; text-align: right; }
}

.chat-empty { flex: 1; display: flex; align-items: center; justify-content: center; }
.chat-input { display: flex; gap: 12px; padding: 16px 20px; border-top: 1px solid #e4e7ed; align-items: flex-end; }
</style>
