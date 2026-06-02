<template>
  <el-container class="main-layout">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '80px' : '260px'" class="sidebar">
      <div class="logo">
        <div class="logo-icon">
          <el-icon :size="28" color="white"><ShoppingBag /></el-icon>
        </div>
        <span v-show="!isCollapse" class="logo-text">青桔悦购</span>
      </div>

      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :collapse-transition="false"
        router
        class="sidebar-menu"
      >
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <template #title>仪表盘</template>
        </el-menu-item>

        <el-menu-item index="/products">
          <el-icon><Goods /></el-icon>
          <template #title>商品管理</template>
        </el-menu-item>

        <el-menu-item index="/categories">
          <el-icon><FolderOpened /></el-icon>
          <template #title>分类管理</template>
        </el-menu-item>

        <el-menu-item index="/orders">
          <el-icon><ShoppingCart /></el-icon>
          <template #title>订单管理</template>
        </el-menu-item>

        <el-menu-item index="/users">
          <el-icon><User /></el-icon>
          <template #title>用户管理</template>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container
      class="main-container"
      :style="{ marginLeft: mainContainerMarginLeft }"
    >
      <!-- 顶部导航 -->
      <el-header class="header">
        <div class="header-left">
          <el-icon
            class="collapse-btn cursor-pointer"
            :size="20"
            style="width: 50px;"
            @click="toggleCollapse"
          >
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <breadcrumb />
        </div>

        <div class="header-right">
          <el-tooltip content="全屏" placement="bottom">
            <el-icon
              class="header-icon cursor-pointer"
              :size="20"
              @click="toggleFullscreen"
              style="width: 50px;"
            >
              <FullScreen />
            </el-icon>
          </el-tooltip>

          <el-dropdown @command="handleCommand">
            <div class="user-info cursor-pointer">
              <el-avatar :size="36" :src="adminInfo?.avatar || defaultAvatar" />
              <span class="username">{{
                adminInfo?.username || '管理员'
              }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人信息
                </el-dropdown-item>
                <el-dropdown-item command="password">
                  <el-icon><Lock /></el-icon>修改密码
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主内容区 -->
      <el-main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>

    <!-- 修改密码对话框 -->
    <el-dialog
      v-model="passwordDialogVisible"
      title="修改密码"
      width="400px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        label-width="100px"
      >
        <el-form-item label="原密码" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            show-password
            placeholder="请输入原密码"
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            show-password
            placeholder="请输入新密码"
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            show-password
            placeholder="请再次输入新密码"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button
          type="primary"
          :loading="passwordLoading"
          @click="submitPasswordChange"
        >
          确认
        </el-button>
      </template>
    </el-dialog>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Odometer,
  Goods,
  FolderOpened,
  ShoppingCart,
  User,
  Fold,
  Expand,
  FullScreen,
  ArrowDown,
  Lock,
  SwitchButton,
  ShoppingBag,
} from '@element-plus/icons-vue'
import Breadcrumb from '@/components/Breadcrumb.vue'
import { authApi } from '@/api/auth'

const route = useRoute()
const router = useRouter()

// 侧边栏折叠状态
const isCollapse = ref(false)

// 当前激活的菜单
const activeMenu = computed(() => route.path)

// 管理员信息
const adminInfo = ref(null)
const defaultAvatar =
  'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// 密码修改相关
const passwordDialogVisible = ref(false)
const passwordLoading = ref(false)
const passwordFormRef = ref(null)
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.value.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' },
  ],
}

// 计算主内容区的左外边距
const mainContainerMarginLeft = computed(() => {
  return isCollapse.value ? '80px' : '260px'
})

// 监听路由变化，确保菜单高亮正确
watch(route, (newRoute) => {
  activeMenu.value = newRoute.path
})

// 切换侧边栏
const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

// 全屏切换
const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
  } else {
    document.exitFullscreen()
  }
}

// 下拉菜单处理
const handleCommand = (command) => {
  switch (command) {
    case 'profile':
      ElMessage.info('个人信息功能开发中')
      break
    case 'password':
      passwordDialogVisible.value = true
      break
    case 'logout':
      handleLogout()
      break
  }
}

// 退出登录
const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_info')
    ElMessage.success('退出登录成功')
    router.push('/login')
  })
}

// 提交密码修改
const submitPasswordChange = async () => {
  try {
    await passwordFormRef.value.validate()
    passwordLoading.value = true

    await authApi.changePassword({
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword,
    })

    ElMessage.success('密码修改成功')
    passwordDialogVisible.value = false
    passwordForm.value = {
      oldPassword: '',
      newPassword: '',
      confirmPassword: '',
    }
  } catch (error) {
    console.error('修改密码失败:', error)
  } finally {
    passwordLoading.value = false
  }
}

// 获取管理员信息
const getAdminInfo = async () => {
  try {
    const info = localStorage.getItem('admin_info')
    if (info) {
      adminInfo.value = JSON.parse(info)
    }
  } catch (error) {
    console.error('获取管理员信息失败:', error)
  }
}

onMounted(() => {
  getAdminInfo()
})
</script>

<style scoped lang="scss">
.main-layout {
  min-height: 100vh;
}

.sidebar {
  background: linear-gradient(180deg, #0D9488 0%, #0F766E 100%);
  transition: width 0.3s ease;
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  z-index: 1000;
  overflow-x: hidden;

  .logo {
    height: 64px;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0 20px;
    border-bottom: 1px solid rgba(255, 255, 255, 0.15);

    .logo-icon {
      width: 40px;
      height: 40px;
      background: rgba(94, 234, 212, 0.2);
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
    }

    .logo-text {
      margin-left: 12px;
      font-size: 18px;
      font-weight: 600;
      color: #fff;
      white-space: nowrap;
      font-family: 'Rubik', system-ui, sans-serif;
    }
  }

  .sidebar-menu {
    background: transparent;
    border-right: none;
    padding-top: 16px;

    :deep(.el-menu-item) {
      color: rgba(255, 255, 255, 0.85);
      margin: 6px 12px;
      border-radius: 10px;
      height: 48px;
      line-height: 48px;
      font-weight: 500;

      &:hover {
        background: rgba(255, 255, 255, 0.18);
        color: #fff;
      }

      &.is-active {
        background: #fff;
        color: #0D9488;
        font-weight: 600;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);

        &::before {
          display: none;
        }

        .el-icon {
          color: #14B8A6;
        }
      }

      .el-icon {
        color: inherit;
      }
    }

    :deep(.el-sub-menu__title) {
      color: rgba(255, 255, 255, 0.85);
      margin: 6px 12px;
      border-radius: 10px;
      font-weight: 500;

      &:hover {
        background: rgba(255, 255, 255, 0.18);
        color: #fff;
      }
    }
  }
}

.main-container {
  transition: margin-left 0.3s ease;
  min-height: 100vh;
  background: #F0FDFA;
}

.header {
  height: 64px;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  position: sticky;
  top: 0;
  z-index: 100;

  .header-left {
    display: flex;
    align-items: center;
    gap: 16px;

    .collapse-btn {
      padding: 8px;
      border-radius: 6px;
      color: #6b7280;
      transition: all 0.2s;

      &:hover {
        background: #f3f4f6;
        color: #171717;
      }
    }
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 20px;

    .header-icon {
      padding: 8px;
      border-radius: 6px;
      color: #6b7280;
      transition: all 0.2s;

      &:hover {
        background: #f3f4f6;
        color: #171717;
      }
    }

    .user-info {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 6px 12px;
      border-radius: 8px;
      transition: background 0.2s;

      &:hover {
        background: #f3f4f6;
      }

      .username {
        font-size: 14px;
        font-weight: 500;
        color: #374151;
      }
    }
  }
}

.main-content {
  padding: 0;
  background: transparent;
  min-height: calc(100vh - 64px);
}

// 过渡动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

// 侧边栏折叠时的样式调整
.sidebar-menu.el-menu--collapse {
  :deep(.el-menu-item) {
    margin: 4px 0; // 移除水平外边距
    justify-content: center; // 水平居中
    padding: 0 !important; // 移除内边距，确保图标能完全居中

    .el-icon {
      margin-right: 0; // 移除图标右侧外边距
    }

    .el-tooltip__trigger { // 针对 Element Plus 的 Tooltip 触发器
      padding: 0 !important;
      justify-content: center;
    }
  }
  :deep(.el-sub-menu__title) {
    margin: 4px 0;
    justify-content: center;
    padding: 0 !important;
    .el-icon {
      margin-right: 0;
    }
  }
}
</style>
