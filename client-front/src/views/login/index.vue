<template>
  <div class="min-h-screen bg-gradient-to-br from-primary/5 via-background to-primary/10 flex items-center justify-center p-4">
    <div class="w-full max-w-md">
      <!-- Logo区域 -->
      <div class="text-center mb-8 animate-fade-in">
        <div class="inline-flex items-center justify-center w-16 h-16 bg-gradient-to-br from-primary to-primary-light rounded-2xl mb-4 shadow-lg">
          <ShoppingBag class="w-8 h-8 text-white" />
        </div>
        <h1 class="text-3xl font-heading font-bold text-primary">悦选商城</h1>
        <p class="text-text-secondary mt-2">精选好物，品质生活</p>
      </div>

      <!-- 登录/注册卡片 -->
      <div class="bg-white rounded-2xl shadow-xl p-8 animate-slide-up">
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
            <label class="block text-sm font-medium text-gray-700 mb-1">手机号</label>
            <div class="relative">
              <Phone class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
              <input
                v-model="registerForm.phone"
                type="tel"
                maxlength="11"
                placeholder="请输入手机号"
                class="w-full pl-10 pr-4 py-3 border border-gray-200 rounded-lg focus:border-primary focus:ring-1 focus:ring-primary/20 outline-none transition-all"
              />
            </div>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">验证码</label>
            <div class="relative flex gap-2">
              <input
                v-model="registerForm.code"
                type="text"
                maxlength="6"
                placeholder="请输入验证码"
                class="flex-1 pl-4 pr-4 py-3 border border-gray-200 rounded-lg focus:border-primary focus:ring-1 focus:ring-primary/20 outline-none transition-all"
              />
              <button
                type="button"
                class="shrink-0 px-4 py-3 text-sm font-medium rounded-lg border border-primary text-primary
                       hover:bg-primary/5 transition-colors cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
                :disabled="smsSending || smsCountdown > 0 || !registerForm.phone || registerForm.phone.length < 11"
                @click="handleSendSmsCode"
              >
                {{ smsCountdown > 0 ? `${smsCountdown}s` : (smsSending ? '发送中...' : '获取验证码') }}
              </button>
            </div>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">密码</label>
            <div class="relative">
              <Lock class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
              <input
                v-model="registerForm.password"
                type="password"
                placeholder="请设置密码（至少6位）"
                class="w-full pl-10 pr-4 py-3 border border-gray-200 rounded-lg focus:border-primary focus:ring-1 focus:ring-primary/20 outline-none transition-all"
              />
            </div>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">邮箱（选填）</label>
            <div class="relative">
              <MessageSquare class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
              <input
                v-model="registerForm.email"
                type="email"
                placeholder="请输入邮箱（可用于登录）"
                class="w-full pl-10 pr-4 py-3 border border-gray-200 rounded-lg focus:border-primary focus:ring-1 focus:ring-primary/20 outline-none transition-all"
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
            class="w-full btn-primary !py-3 !text-base"
            :disabled="!registerForm.agreed"
          >
            注册并登录
          </button>
        </form>

        <!-- 密码登录 -->
        <form v-else @submit.prevent="handlePasswordLogin" class="space-y-4">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">手机号 / 邮箱</label>
            <div class="relative">
              <User class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
              <input
                v-model="loginForm.account"
                type="text"
                placeholder="请输入手机号或邮箱"
                class="w-full pl-10 pr-4 py-3 border border-gray-200 rounded-lg focus:border-primary focus:ring-1 focus:ring-primary/20 outline-none transition-all"
              />
            </div>
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">密码</label>
            <div class="relative">
              <Lock class="absolute left-3 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
              <input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                class="w-full pl-10 pr-4 py-3 border border-gray-200 rounded-lg focus:border-primary focus:ring-1 focus:ring-primary/20 outline-none transition-all"
              />
            </div>
          </div>
          <button type="submit" class="w-full btn-primary !py-3 !text-base">
            登录
          </button>
        </form>


      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ShoppingBag, Phone, Lock, User, MessageSquare } from 'lucide-vue-next'
import { ElMessage } from 'element-plus'
import { registerUser, userLogin } from '@/api/user'
import { sendSmsCode } from '@/api/sms'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

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
      // 开发环境自动填入验证码
      if (res.data) {
        registerForm.code = res.data
      }
      ElMessage.success('验证码已发送')
      // 开始倒计时
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
