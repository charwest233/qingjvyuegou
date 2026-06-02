<template>
  <div class="error-page">
    <div class="error-content">
      <div class="error-code">404</div>
      <h1 class="error-title">页面未找到</h1>
      <p class="error-desc">抱歉，您访问的页面不存在或已被移除</p>
      <div class="error-actions">
        <el-button type="primary" size="large" @click="goHome">
          <el-icon><HomeFilled /></el-icon>返回首页
        </el-button>
        <el-button size="large" @click="goBack">
          <el-icon><ArrowLeft /></el-icon>返回上一页
        </el-button>
      </div>
    </div>
    <div class="error-decoration">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { HomeFilled, ArrowLeft } from '@element-plus/icons-vue'

const router = useRouter()

const goHome = () => {
  router.push('/dashboard')
}

const goBack = () => {
  router.back()
}
</script>

<style scoped lang="scss">
.error-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
  position: relative;
  overflow: hidden;
}

.error-content {
  text-align: center;
  z-index: 1;
  padding: 40px;

  .error-code {
    font-size: 160px;
    font-weight: 800;
    background: linear-gradient(135deg, #D4AF37 0%, #B8960C 50%, #D4AF37 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    line-height: 1;
    margin-bottom: 24px;
    text-shadow: none;
  }

  .error-title {
    font-size: 32px;
    font-weight: 600;
    color: #171717;
    margin: 0 0 12px;
  }

  .error-desc {
    font-size: 16px;
    color: #6b7280;
    margin: 0 0 32px;
  }

  .error-actions {
    display: flex;
    gap: 16px;
    justify-content: center;

    .el-button {
      display: flex;
      align-items: center;
      gap: 8px;
    }
  }
}

.error-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;

  .circle {
    position: absolute;
    border-radius: 50%;
    background: linear-gradient(135deg, rgba(212, 175, 55, 0.1) 0%, transparent 70%);

    &.circle-1 {
      width: 400px;
      height: 400px;
      top: -100px;
      right: -100px;
    }

    &.circle-2 {
      width: 300px;
      height: 300px;
      bottom: -50px;
      left: -50px;
    }

    &.circle-3 {
      width: 200px;
      height: 200px;
      top: 50%;
      left: 20%;
      background: linear-gradient(135deg, rgba(23, 23, 23, 0.05) 0%, transparent 70%);
    }
  }
}

@media (max-width: 768px) {
  .error-content {
    .error-code {
      font-size: 100px;
    }

    .error-title {
      font-size: 24px;
    }

    .error-actions {
      flex-direction: column;
    }
  }
}
</style>
