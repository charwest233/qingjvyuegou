<template>
  <div class="min-h-screen flex items-center justify-center p-4 lg:p-8 bg-gradient-to-br from-primary/10 via-background to-primary/20">
    <!-- 主登录卡片 -->
    <div class="w-full max-w-[900px] bg-white rounded-2xl shadow-2xl overflow-hidden flex flex-col lg:flex-row">
      
      <!-- ========== 左栏：动画角色（桌面显示，移动端隐藏） ========== -->
      <div class="hidden lg:flex flex-col justify-between bg-gradient-to-br from-primary/15 via-primary/10 to-primary/5 p-10 relative overflow-hidden w-[45%]">
        <!-- 顶部品牌 -->
        <div class="relative z-10">
          <div class="flex items-center gap-3 text-lg font-semibold text-gray-800">
            <div class="w-10 h-10 rounded-xl bg-white/80 backdrop-blur-sm flex items-center justify-center shadow-lg">
              <ShoppingBag class="w-5 h-5 text-primary" />
            </div>
            <span>青桔悦购</span>
          </div>
        </div>

        <!-- 中间动画角色区域 -->
        <div class="relative z-10 flex items-end justify-center flex-1 py-8">
          <div ref="charactersContainer" class="relative" style="width: 320px; height: 260px;">
            <!-- 紫色角色 - 最底层，高矩形 -->
            <div 
              ref="purpleRef"
              class="absolute bottom-0 transition-all duration-700 ease-in-out rounded-t-lg"
              :style="{
                left: '50px',
                width: '100px',
                height: (isTyping || (password.length > 0 && !showPassword)) ? '280px' : '250px',
                backgroundColor: '#8B5CF6',
                zIndex: 1,
                transform: (password.length > 0 && showPassword)
                  ? 'skewX(0deg)'
                  : (isTyping || (password.length > 0 && !showPassword))
                    ? `skewX(${(purplePos.bodySkew || 0) - 10}deg) translateX(25px)`
                    : `skewX(${purplePos.bodySkew || 0}deg)`,
                transformOrigin: 'bottom center',
              }"
            >
              <!-- 紫色角色的眼睛 -->
              <div 
                class="absolute flex gap-3 transition-all duration-700 ease-in-out"
                :style="{
                  left: (password.length > 0 && showPassword) ? '15px' : isLookingAtEachOther ? '32px' : `${25 + purplePos.faceX}px`,
                  top: (password.length > 0 && showPassword) ? '25px' : isLookingAtEachOther ? '40px' : `${28 + purplePos.faceY}px`,
                }"
              >
                <EyeBall 
                  :size="14" 
                  :pupil-size="5" 
                  :max-distance="3" 
                  eye-color="white" 
                  pupil-color="#1f2937"
                  :is-blinking="isPurpleBlinking"
                  :force-look-x="(password.length > 0 && showPassword) ? (isPurplePeeking ? 2 : -2) : isLookingAtEachOther ? 1 : undefined"
                  :force-look-y="(password.length > 0 && showPassword) ? (isPurplePeeking ? 3 : -2) : isLookingAtEachOther ? 2 : undefined"
                />
                <EyeBall 
                  :size="14" 
                  :pupil-size="5" 
                  :max-distance="3" 
                  eye-color="white" 
                  pupil-color="#1f2937"
                  :is-blinking="isPurpleBlinking"
                  :force-look-x="(password.length > 0 && showPassword) ? (isPurplePeeking ? 2 : -2) : isLookingAtEachOther ? 1 : undefined"
                  :force-look-y="(password.length > 0 && showPassword) ? (isPurplePeeking ? 3 : -2) : isLookingAtEachOther ? 2 : undefined"
                />
              </div>
            </div>

            <!-- 黑色角色 - 中间层 -->
            <div 
              ref="blackRef"
              class="absolute bottom-0 transition-all duration-700 ease-in-out rounded-t-md"
              :style="{
                left: '140px',
                width: '70px',
                height: '200px',
                backgroundColor: '#1f2937',
                zIndex: 2,
                transform: (password.length > 0 && showPassword)
                  ? 'skewX(0deg)'
                  : isLookingAtEachOther
                    ? `skewX(${(blackPos.bodySkew || 0) * 1.5 + 6}deg) translateX(10px)`
                    : (isTyping || (password.length > 0 && !showPassword))
                      ? `skewX(${(blackPos.bodySkew || 0) * 1.5}deg)`
                      : `skewX(${blackPos.bodySkew || 0}deg)`,
                transformOrigin: 'bottom center',
              }"
            >
              <!-- 黑色角色的眼睛 -->
              <div 
                class="absolute flex gap-2 transition-all duration-700 ease-in-out"
                :style="{
                  left: (password.length > 0 && showPassword) ? '10px' : isLookingAtEachOther ? '22px' : `${16 + blackPos.faceX}px`,
                  top: (password.length > 0 && showPassword) ? '20px' : isLookingAtEachOther ? '8px' : `${22 + blackPos.faceY}px`,
                }"
              >
                <EyeBall 
                  :size="12" 
                  :pupil-size="4" 
                  :max-distance="2" 
                  eye-color="white" 
                  pupil-color="#1f2937"
                  :is-blinking="isBlackBlinking"
                  :force-look-x="(password.length > 0 && showPassword) ? -2 : isLookingAtEachOther ? 0 : undefined"
                  :force-look-y="(password.length > 0 && showPassword) ? -2 : isLookingAtEachOther ? -2 : undefined"
                />
                <EyeBall 
                  :size="12" 
                  :pupil-size="4" 
                  :max-distance="2" 
                  eye-color="white" 
                  pupil-color="#1f2937"
                  :is-blinking="isBlackBlinking"
                  :force-look-x="(password.length > 0 && showPassword) ? -2 : isLookingAtEachOther ? 0 : undefined"
                  :force-look-y="(password.length > 0 && showPassword) ? -2 : isLookingAtEachOther ? -2 : undefined"
                />
              </div>
            </div>

            <!-- 橙色角色 - 前左层 -->
            <div 
              ref="orangeRef"
              class="absolute bottom-0 transition-all duration-700 ease-in-out"
              :style="{
                left: '0px',
                width: '140px',
                height: '110px',
                zIndex: 3,
                backgroundColor: '#FB923C',
                borderRadius: '70px 70px 0 0',
                transform: (password.length > 0 && showPassword) ? 'skewX(0deg)' : `skewX(${orangePos.bodySkew || 0}deg)`,
                transformOrigin: 'bottom center',
              }"
            >
              <!-- 橙色角色的眼睛 -->
              <div 
                class="absolute flex gap-4 transition-all duration-200 ease-out"
                :style="{
                  left: (password.length > 0 && showPassword) ? '32px' : `${48 + (orangePos.faceX || 0)}px`,
                  top: (password.length > 0 && showPassword) ? '45px' : `${48 + (orangePos.faceY || 0)}px`,
                }"
              >
                <Pupil :size="8" :max-distance="3" pupil-color="#1f2937" :force-look-x="(password.length > 0 && showPassword) ? -3 : undefined" :force-look-y="(password.length > 0 && showPassword) ? -2 : undefined" />
                <Pupil :size="8" :max-distance="3" pupil-color="#1f2937" :force-look-x="(password.length > 0 && showPassword) ? -3 : undefined" :force-look-y="(password.length > 0 && showPassword) ? -2 : undefined" />
              </div>
            </div>

            <!-- 黄色角色 - 最前层 -->
            <div 
              ref="yellowRef"
              class="absolute bottom-0 transition-all duration-700 ease-in-out"
              :style="{
                left: '190px',
                width: '85px',
                height: '145px',
                backgroundColor: '#FDE047',
                borderRadius: '42px 42px 0 0',
                zIndex: 4,
                transform: (password.length > 0 && showPassword) ? 'skewX(0deg)' : `skewX(${yellowPos.bodySkew || 0}deg)`,
                transformOrigin: 'bottom center',
              }"
            >
              <!-- 黄色角色的眼睛 -->
              <div 
                class="absolute flex gap-3 transition-all duration-200 ease-out"
                :style="{
                  left: (password.length > 0 && showPassword) ? '12px' : `${28 + (yellowPos.faceX || 0)}px`,
                  top: (password.length > 0 && showPassword) ? '22px' : `${26 + (yellowPos.faceY || 0)}px`,
                }"
              >
                <Pupil :size="8" :max-distance="3" pupil-color="#1f2937" :force-look-x="(password.length > 0 && showPassword) ? -3 : undefined" :force-look-y="(password.length > 0 && showPassword) ? -2 : undefined" />
                <Pupil :size="8" :max-distance="3" pupil-color="#1f2937" :force-look-x="(password.length > 0 && showPassword) ? -3 : undefined" :force-look-y="(password.length > 0 && showPassword) ? -2 : undefined" />
              </div>
              <!-- 黄色角色的嘴巴 -->
              <div 
                class="absolute w-8 h-[3px] bg-gray-800 rounded-full transition-all duration-200 ease-out"
                :style="{
                  left: (password.length > 0 && showPassword) ? '10px' : `${26 + (yellowPos.faceX || 0)}px`,
                  top: (password.length > 0 && showPassword) ? '55px' : `${55 + (yellowPos.faceY || 0)}px`,
                }"
              />
            </div>
          </div>
        </div>

        <!-- 底部留白（去掉链接） -->
        <div class="h-8"></div>
      </div>

      <!-- ========== 右栏：登录/注册表单 ========== -->
      <div class="flex-1 flex items-center justify-center p-8 lg:p-12 bg-white">
        <div class="w-full max-w-[380px]">
          <!-- 移动端 Logo -->
          <div class="lg:hidden text-center mb-6">
            <div class="inline-flex items-center justify-center w-14 h-14 bg-gradient-to-br from-primary to-primary-light rounded-xl mb-3 shadow-lg">
              <ShoppingBag class="w-7 h-7 text-white" />
            </div>
            <h1 class="text-2xl font-heading font-bold text-gray-800">青桔悦购</h1>
            <p class="text-gray-500 text-sm mt-1">精选好物，品质生活</p>
          </div>

          <!-- 标题 -->
          <div class="text-center mb-6">
            <h2 class="text-2xl font-bold text-gray-800">{{ loginType === 'password' ? '欢迎回来' : '注册账号' }}</h2>
            <p class="text-gray-500 text-sm mt-1">{{ loginType === 'password' ? '请登录您的账号' : '创建新账号' }}</p>
          </div>

          <!-- Tab切换 -->
          <div class="flex mb-6 bg-gray-50 rounded-lg p-1">
            <button
              v-for="tab in tabs"
              :key="tab.key"
              class="flex-1 py-2 text-sm font-medium rounded-md transition-all duration-200 cursor-pointer"
              :class="loginType === tab.key
                ? 'bg-white text-primary shadow-sm'
                : 'text-gray-500 hover:text-gray-700'"
              @click="loginType = tab.key"
            >
              {{ tab.label }}
            </button>
          </div>

          <!-- 注册（手机号+验证码+密码） -->
          <form v-if="loginType === 'phone'" @submit.prevent="handlePhoneRegister" class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1.5">手机号</label>
              <div class="relative">
                <Phone class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
                <input
                  v-model="registerForm.phone"
                  type="tel"
                  maxlength="11"
                  placeholder="请输入手机号"
                  class="w-full pl-10 pr-4 py-2.5 border border-gray-200 rounded-lg focus:border-primary focus:ring-1 focus:ring-primary/20 outline-none transition-all text-sm"
                  @focus="setTyping(true)"
                  @blur="setTyping(false)"
                />
              </div>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1.5">验证码</label>
              <div class="relative flex gap-2">
                <input
                  v-model="registerForm.code"
                  type="text"
                  maxlength="6"
                  placeholder="请输入验证码"
                  class="flex-1 pl-4 pr-4 py-2.5 border border-gray-200 rounded-lg focus:border-primary focus:ring-1 focus:ring-primary/20 outline-none transition-all text-sm"
                  @focus="setTyping(true)"
                  @blur="setTyping(false)"
                />
                <button
                  type="button"
                  class="shrink-0 px-3 py-2.5 text-sm font-medium rounded-lg border border-primary text-primary
                         hover:bg-primary/5 transition-colors cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
                  :disabled="smsSending || smsCountdown > 0 || !registerForm.phone || registerForm.phone.length < 11"
                  @click="handleSendSmsCode"
                >
                  {{ smsCountdown > 0 ? `${smsCountdown}s` : (smsSending ? '发送中...' : '获取验证码') }}
                </button>
              </div>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1.5">密码</label>
              <div class="relative">
                <Lock class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
                <input
                  v-model="registerForm.password"
                  :type="showPassword ? 'text' : 'password'"
                  placeholder="请设置密码（至少6位）"
                  class="w-full pl-10 pr-10 py-2.5 border border-gray-200 rounded-lg focus:border-primary focus:ring-1 focus:ring-primary/20 outline-none transition-all text-sm"
                  @focus="setTyping(true)"
                  @blur="setTyping(false)"
                />
                <button
                  type="button"
                  class="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 cursor-pointer"
                  @click="showPassword = !showPassword"
                >
                  <Eye v-if="showPassword" class="w-5 h-5" />
                  <EyeOff v-else class="w-5 h-5" />
                </button>
              </div>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1.5">邮箱（选填）</label>
              <div class="relative">
                <MessageSquare class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
                <input
                  v-model="registerForm.email"
                  type="email"
                  placeholder="请输入邮箱（可用于登录）"
                  class="w-full pl-10 pr-4 py-2.5 border border-gray-200 rounded-lg focus:border-primary focus:ring-1 focus:ring-primary/20 outline-none transition-all text-sm"
                  @focus="setTyping(true)"
                  @blur="setTyping(false)"
                />
              </div>
            </div>
            <div class="flex items-center gap-2">
              <input id="agreement" v-model="registerForm.agreed" type="checkbox" class="w-4 h-4 text-primary rounded" />
              <label for="agreement" class="text-xs text-gray-500">
                已阅读并同意
                <a href="#" class="text-primary hover:underline">《用户协议》</a>
                和
                <a href="#" class="text-primary hover:underline">《隐私政策》</a>
              </label>
            </div>
            <button
              type="submit"
              class="w-full btn-primary !py-2.5 !text-base"
              :disabled="!registerForm.agreed"
            >
              注册并登录
            </button>
          </form>

          <!-- 密码登录 -->
          <form v-else @submit.prevent="handlePasswordLogin" class="space-y-4">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1.5">手机号 / 邮箱</label>
              <div class="relative">
                <User class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
                <input
                  v-model="loginForm.account"
                  type="text"
                  placeholder="请输入手机号或邮箱"
                  class="w-full pl-10 pr-4 py-2.5 border border-gray-200 rounded-lg focus:border-primary focus:ring-1 focus:ring-primary/20 outline-none transition-all text-sm"
                  @focus="setTyping(true)"
                  @blur="setTyping(false)"
                />
              </div>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1.5">密码</label>
              <div class="relative">
                <Lock class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
                <input
                  v-model="loginForm.password"
                  :type="showPassword ? 'text' : 'password'"
                  placeholder="请输入密码"
                  class="w-full pl-10 pr-10 py-2.5 border border-gray-200 rounded-lg focus:border-primary focus:ring-1 focus:ring-primary/20 outline-none transition-all text-sm"
                  @focus="setTyping(true)"
                  @blur="setTyping(false)"
                />
                <button
                  type="button"
                  class="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 cursor-pointer"
                  @click="showPassword = !showPassword"
                >
                  <Eye v-if="showPassword" class="w-5 h-5" />
                  <EyeOff v-else class="w-5 h-5" />
                </button>
              </div>
            </div>
            <button type="submit" class="w-full btn-primary !py-2.5 !text-base">
              登录
            </button>
          </form>

          <!-- 底部切换 -->
          <div class="text-center text-sm text-gray-400 mt-6">
            {{ loginType === 'password' ? '还没有账号？' : '已有账号？' }}
            <button
              class="text-primary hover:underline font-medium cursor-pointer"
              @click="loginType = loginType === 'password' ? 'phone' : 'password'"
            >
              {{ loginType === 'password' ? '立即注册' : '去登录' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ShoppingBag, Phone, Lock, User, MessageSquare, Eye, EyeOff } from 'lucide-vue-next'
import { ElMessage } from 'element-plus'
import { registerUser, userLogin } from '@/api/user'
import { sendSmsCode } from '@/api/sms'
import { useUserStore } from '@/stores/user'
import EyeBall from './components/EyeBall.vue'
import Pupil from './components/Pupil.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// ========== 角色动画状态 ==========
const purpleRef = ref(null)
const blackRef = ref(null)
const yellowRef = ref(null)
const orangeRef = ref(null)
const charactersContainer = ref(null)

const mouseX = ref(0)
const mouseY = ref(0)
const isPurpleBlinking = ref(false)
const isBlackBlinking = ref(false)
const isTyping = ref(false)
const isLookingAtEachOther = ref(false)
const isPurplePeeking = ref(false)

// 密码显示控制
const showPassword = ref(false)

// 角色位置计算
const purplePos = ref({ faceX: 0, faceY: 0, bodySkew: 0 })
const blackPos = ref({ faceX: 0, faceY: 0, bodySkew: 0 })
const yellowPos = ref({ faceX: 0, faceY: 0, bodySkew: 0 })
const orangePos = ref({ faceX: 0, faceY: 0, bodySkew: 0 })

// 鼠标移动监听
function handleMouseMove(e) {
  mouseX.value = e.clientX
  mouseY.value = e.clientY
  
  purplePos.value = calculatePosition(purpleRef)
  blackPos.value = calculatePosition(blackRef)
  yellowPos.value = calculatePosition(yellowRef)
  orangePos.value = calculatePosition(orangeRef)
}

// 计算角色位置和倾斜
function calculatePosition(ref) {
  if (!ref.value || !charactersContainer.value) return { faceX: 0, faceY: 0, bodySkew: 0 }
  
  const rect = ref.value.getBoundingClientRect()
  
  const centerX = rect.left + rect.width / 2
  const centerY = rect.top + rect.height / 3
  
  const deltaX = mouseX.value - centerX
  const deltaY = mouseY.value - centerY
  
  const faceX = Math.max(-10, Math.min(10, deltaX / 30))
  const faceY = Math.max(-6, Math.min(6, deltaY / 40))
  
  const bodySkew = Math.max(-4, Math.min(4, -deltaX / 180))
  
  return { faceX, faceY, bodySkew }
}

// 眨眼效果
function setupBlinking() {
  const blinkPurple = () => {
    const interval = Math.random() * 4000 + 3000
    setTimeout(() => {
      isPurpleBlinking.value = true
      setTimeout(() => {
        isPurpleBlinking.value = false
        blinkPurple()
      }, 150)
    }, interval)
  }
  blinkPurple()
  
  const blinkBlack = () => {
    const interval = Math.random() * 4000 + 3000
    setTimeout(() => {
      isBlackBlinking.value = true
      setTimeout(() => {
        isBlackBlinking.value = false
        blinkBlack()
      }, 150)
    }, interval)
  }
  blinkBlack()
}

// 输入状态控制
let typingTimeout = null
function setTyping(typing) {
  isTyping.value = typing
  
  if (typing) {
    isLookingAtEachOther.value = true
    if (typingTimeout) clearTimeout(typingTimeout)
    typingTimeout = setTimeout(() => {
      isLookingAtEachOther.value = false
    }, 800)
  }
}

// 紫色角色偷看密码
let peekTimeout = null
function setupPeeking() {
  const checkPeek = () => {
    if (password.value.length > 0 && showPassword.value) {
      const interval = Math.random() * 3000 + 2000
      peekTimeout = setTimeout(() => {
        isPurplePeeking.value = true
        setTimeout(() => {
          isPurplePeeking.value = false
          checkPeek()
        }, 800)
      }, interval)
    } else {
      isPurplePeeking.value = false
      peekTimeout = setTimeout(checkPeek, 500)
    }
  }
  checkPeek()
}

onMounted(() => {
  window.addEventListener('mousemove', handleMouseMove)
  setupBlinking()
  setupPeeking()
})

onUnmounted(() => {
  window.removeEventListener('mousemove', handleMouseMove)
  if (typingTimeout) clearTimeout(typingTimeout)
  if (peekTimeout) clearTimeout(peekTimeout)
})

// ========== 登录/注册逻辑 ==========
const loginType = ref('password')

const tabs = [
  { key: 'password', label: '登录' },
  { key: 'phone', label: '注册' }
]

const registerForm = reactive({
  phone: '',
  password: '',
  code: '',
  email: '',
  agreed: true
})

// 验证码倒计时
const smsSending = ref(false)
const smsCountdown = ref(0)
let smsTimer = null

async function handleSendSmsCode() {
  if (!registerForm.phone || registerForm.phone.length < 11) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  smsSending.value = true
  try {
    const res = await sendSmsCode(registerForm.phone)
    if (res.code === 200) {
      if (res.data) {
        registerForm.code = res.data
      }
      ElMessage.success('验证码已发送')
      smsCountdown.value = 60
      smsTimer = setInterval(() => {
        smsCountdown.value--
        if (smsCountdown.value <= 0) {
          clearInterval(smsTimer)
          smsTimer = null
        }
      }, 1000)
    } else {
      ElMessage.error(res.message || '发送失败')
    }
  } catch (err) {
    ElMessage.error('发送验证码失败')
  } finally {
    smsSending.value = false
  }
}

const loginForm = reactive({
  account: '',
  password: ''
})

// 当前活动表单的密码（用于角色动画）
const password = computed(() => {
  if (loginType.value === 'password') return loginForm.password
  return registerForm.password
})

async function handlePhoneRegister() {
  if (!registerForm.agreed) {
    ElMessage.warning('请先同意用户协议')
    return
  }
  if (!registerForm.phone || registerForm.phone.length < 11) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  if (!registerForm.code || registerForm.code.length < 4) {
    ElMessage.warning('请输入验证码')
    return
  }
  if (!registerForm.password || registerForm.password.length < 6) {
    ElMessage.warning('密码至少6位')
    return
  }
  const res = await registerUser(registerForm.phone, registerForm.password, registerForm.email || undefined, registerForm.code)
  if (res?.code === 200) {
    userStore.setLoginData(res.data)
    ElMessage.success('注册成功')
    const redirect = route.query.redirect || '/'
    router.push(redirect)
  }
}

async function handlePasswordLogin() {
  if (!loginForm.account || !loginForm.password) {
    ElMessage.warning('请输入账号和密码')
    return
  }
  const res = await userLogin(loginForm.account, loginForm.password)
  if (res?.code === 200) {
    userStore.setLoginData(res.data)
    ElMessage.success('登录成功')
    const redirect = route.query.redirect || '/'
    router.push(redirect)
  }
}
</script>
