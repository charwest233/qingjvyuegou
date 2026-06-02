<template>
  <div class="login-page">
    <div class="login-container">
      <!-- 左侧装饰区域 -->
      <div class="login-left">
        <div class="brand-section">
          <div class="brand-logo">
            <el-icon :size="48" color="#5EEAD4"><ShoppingBag /></el-icon>
          </div>
          <h1 class="brand-name">青桔悦购管理后台</h1>
          <p class="brand-desc">高效、专业、智能的电商管理系统</p>
        </div>
        
        <div class="features">
          <div class="feature-item">
            <div class="feature-icon">
              <el-icon :size="24"><DataLine /></el-icon>
            </div>
            <div class="feature-text">
              <h4>数据可视化</h4>
              <p>实时数据监控与分析</p>
            </div>
          </div>
          <div class="feature-item">
            <div class="feature-icon">
              <el-icon :size="24"><ShoppingCart /></el-icon>
            </div>
            <div class="feature-text">
              <h4>订单管理</h4>
              <p>高效处理订单流程</p>
            </div>
          </div>
          <div class="feature-item">
            <div class="feature-icon">
              <el-icon :size="24"><Goods /></el-icon>
            </div>
            <div class="feature-text">
              <h4>商品管理</h4>
              <p>轻松管理商品上下架</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧登录表单 -->
      <div class="login-right">
        <div class="login-box">
          <div class="login-header">
            <h2>欢迎回来</h2>
            <p>请使用管理员账号登录</p>
          </div>

          <el-form
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
            class="login-form"
            @keyup.enter="handleLogin"
          >
            <el-form-item prop="username">
              <el-input
                v-model="loginForm.username"
                placeholder="请输入用户名"
                :prefix-icon="User"
                size="large"
              />
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                :prefix-icon="Lock"
                show-password
                size="large"
              />
            </el-form-item>

            <div class="login-options">
              <el-checkbox v-model="rememberMe">记住我</el-checkbox>
            </div>

            <el-button
              type="primary"
              size="large"
              class="login-btn"
              :loading="loading"
              @click="handleLogin"
            >
              登 录
            </el-button>
          </el-form>

          <div class="login-footer">
            <p>默认账号: admin / 密码: 123456</p>
          </div>
        </div>
      </div>
    </div>

    <!-- 装饰背景 -->
    <div class="bg-decoration">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, ShoppingBag, DataLine, ShoppingCart, Goods } from '@element-plus/icons-vue'
import { authApi } from '@/api/auth'

const router = useRouter()

const loginForm = ref({
  username: '',
  password: ''
})

const loading = ref(false)
const rememberMe = ref(false)
const loginFormRef = ref(null)

const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ]
}

// 登录处理
const handleLogin = async () => {
  try {
    await loginFormRef.value.validate()
    loading.value = true

    const res = await authApi.login({
      username: loginForm.value.username,
      password: loginForm.value.password
    })

    // 保存登录信息
    localStorage.setItem('admin_token', res.token)
    localStorage.setItem('admin_info', JSON.stringify(res.adminInfo))

    if (rememberMe.value) {
      localStorage.setItem('admin_username', loginForm.value.username)
    } else {
      localStorage.removeItem('admin_username')
    }

    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (error) {
    console.error('登录失败:', error)
  } finally {
    loading.value = false
  }
}

// 初始化记住的用户名
onMounted(() => {
  const savedUsername = localStorage.getItem('admin_username')
  if (savedUsername) {
    loginForm.value.username = savedUsername
    rememberMe.value = true
  }
})
</script>

<style scoped lang="scss">
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #14B8A6 0%, #0D9488 100%);
  position: relative;
  overflow: hidden;
}

.bg-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;

  .circle {
    position: absolute;
    border-radius: 50%;
    background: radial-gradient(circle, rgba(94, 234, 212, 0.15) 0%, transparent 70%);

    &.circle-1 {
      width: 600px;
      height: 600px;
      top: -200px;
      right: -200px;
    }

    &.circle-2 {
      width: 400px;
      height: 400px;
      bottom: -100px;
      left: -100px;
    }

    &.circle-3 {
      width: 300px;
      height: 300px;
      top: 50%;
      left: 30%;
      background: radial-gradient(circle, rgba(94, 234, 212, 0.08) 0%, transparent 70%);
    }
  }
}

.login-container {
  display: flex;
  width: 1000px;
  height: 600px;
  background: #fff;
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 0 25px 50px -12px rgba(0, 0, 0, 0.5);
  position: relative;
  z-index: 1;
}

.login-left {
  flex: 1;
  background: linear-gradient(135deg, #171717 0%, #262626 100%);
  padding: 60px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  color: #fff;

  .brand-section {
    .brand-logo {
      width: 80px;
      height: 80px;
      background: rgba(52, 211, 153, 0.15);
      border-radius: 16px;
      display: flex;
      align-items: center;
      justify-content: center;
      margin-bottom: 24px;
    }

    .brand-name {
      font-size: 32px;
      font-weight: 700;
      margin-bottom: 12px;
      color: #fff;
    }

    .brand-desc {
      font-size: 16px;
      color: rgba(255, 255, 255, 0.6);
      margin: 0;
    }
  }

  .features {
    display: flex;
    flex-direction: column;
    gap: 24px;

    .feature-item {
      display: flex;
      align-items: center;
      gap: 16px;

      .feature-icon {
        width: 48px;
        height: 48px;
        background: rgba(94, 234, 212, 0.15);
        border-radius: 12px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #5EEAD4;
        flex-shrink: 0;
      }

      .feature-text {
        h4 {
          font-size: 16px;
          font-weight: 600;
          margin: 0 0 4px;
          color: #fff;
        }

        p {
          font-size: 14px;
          color: rgba(255, 255, 255, 0.5);
          margin: 0;
        }
      }
    }
  }
}

.login-right {
  width: 400px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: #fff;

  .login-box {
    width: 100%;
    max-width: 320px;

    .login-header {
      text-align: center;
      margin-bottom: 32px;

      h2 {
        font-size: 24px;
        font-weight: 600;
        color: #171717;
        margin: 0 0 8px;
      }

      p {
        font-size: 14px;
        color: #6b7280;
        margin: 0;
      }
    }

    .login-form {
      :deep(.el-input__wrapper) {
        background: #f9fafb;
        box-shadow: none;
        border: 1px solid #e5e7eb;
        border-radius: 10px;
        padding: 4px 12px;

        &:hover, &:focus {
          border-color: #14B8A6;
        }

        &.is-focus {
          border-color: #14B8A6;
          box-shadow: 0 0 0 3px rgba(20, 184, 166, 0.08);
        }
      }

      :deep(.el-input__inner) {
        height: 44px;
        font-size: 15px;
      }

      :deep(.el-input__icon) {
        color: #9ca3af;
      }
    }

    .login-options {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24px;

      :deep(.el-checkbox__label) {
        font-size: 14px;
        color: #6b7280;
      }
    }

    .login-btn {
      width: 100%;
      height: 48px;
      font-size: 16px;
      font-weight: 600;
      border-radius: 10px;
      background: linear-gradient(135deg, #14B8A6 0%, #0D9488 100%);
      border: none;

      &:hover {
        background: linear-gradient(135deg, #5EEAD4 0%, #14B8A6 100%);
        transform: translateY(-1px);
      }
    }

    .login-footer {
      margin-top: 24px;
      text-align: center;

      p {
        font-size: 12px;
        color: #9ca3af;
      }
    }
  }
}

// 响应式
@media (max-width: 768px) {
  .login-container {
    width: 100%;
    height: 100vh;
    border-radius: 0;
    flex-direction: column;
  }

  .login-left {
    display: none;
  }

  .login-right {
    width: 100%;
    padding: 24px;
  }
}
</style>
