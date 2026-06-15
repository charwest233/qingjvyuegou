<template>
  <div class="page-container py-4">
    <div v-if="!isLoggedIn" class="text-center py-20">
      <User class="w-20 h-20 mx-auto text-gray-300 mb-4" />
      <p class="text-gray-400 mb-4">请先登录</p>
      <router-link to="/login" class="btn-primary inline-block">去登录</router-link>
    </div>
    <template v-else>
      <!-- 用户信息头部 → 流动渐变 -->
      <div class="relative rounded-2xl p-6 mb-6 overflow-hidden">
        <!-- 流动渐变背景 -->
        <div class="absolute inset-0 bg-gradient-to-r from-primary via-cyan-400 via-emerald-300 to-primary bg-[length:400%_100%] animate-gradient-flow" />
        <!-- 光晕装饰 -->
        <div class="absolute inset-0">
          <div class="absolute top-0 right-0 w-32 h-32 bg-white/15 rounded-full -translate-y-1/2 translate-x-1/2 blur-2xl animate-pulse-slow" />
          <div class="absolute bottom-0 left-0 w-24 h-24 bg-white/10 rounded-full translate-y-1/3 -translate-x-1/3 blur-xl animate-pulse-slow" style="animation-delay: 1.5s" />
          <div class="absolute top-1/2 right-1/3 w-16 h-16 bg-white/10 rounded-full blur-xl animate-pulse-slow" style="animation-delay: 0.8s" />
        </div>
        <!-- 粒子 -->
        <div class="absolute inset-0 overflow-hidden pointer-events-none">
          <div v-for="n in 6" :key="n"
            class="absolute w-1 h-1 bg-white/60 rounded-full animate-float-particle"
            :style="{
              left: `${Math.random() * 100}%`,
              top: `${Math.random() * 100}%`,
              animationDelay: `${Math.random() * 4}s`,
              animationDuration: `${2 + Math.random() * 3}s`,
              width: `${1 + Math.random() * 2}px`,
              height: `${Math.random() * 2}px`,
              opacity: 0.3 + Math.random() * 0.5
            }"
          />
        </div>
        <div class="relative flex items-center gap-4">
          <div class="w-16 h-16 rounded-full bg-white/30 flex items-center justify-center overflow-hidden ring-2 ring-white/50">
            <img v-if="userInfo?.avatarUrl" :src="userInfo.avatarUrl" class="w-full h-full object-cover" />
            <User v-else class="w-8 h-8 text-white" />
          </div>
          <div class="text-white">
            <h2 class="text-xl font-heading font-bold">{{ userInfo?.nickname || '用户' }}</h2>
            <p class="text-sm opacity-80 mt-1">欢迎回来</p>
          </div>
        </div>
      </div>

      <!-- 订单快捷入口 -->
      <div class="bg-white rounded-2xl p-4 shadow-sm mb-4">
        <div class="flex items-center justify-between mb-4">
          <h3 class="text-sm font-medium text-gray-700">我的订单</h3>
          <router-link to="/orders" class="text-sm text-primary hover:text-primary-dark transition-colors">查看全部 →</router-link>
        </div>
        <div class="grid grid-cols-4 gap-2 text-center">
          <router-link
            v-for="tab in orderTabs"
            :key="tab.status"
            :to="{ name: 'Orders', query: { status: tab.status } }"
            class="flex flex-col items-center gap-1 p-2 rounded-lg hover:bg-gray-50 transition-colors"
          >
            <div class="relative">
              <component :is="tab.icon" class="w-6 h-6" :class="tab.color" />
              <!-- 红点角标（已完成不显示，贴近图标右下） -->
              <span
                v-if="tab.status !== 3 && orderStats[tab.statKey] > 0"
                class="absolute -top-1.5 -right-1.5 min-w-[16px] h-[16px] bg-red-500 text-white text-[10px] font-bold
                       flex items-center justify-center rounded-full px-0.5 leading-none shadow"
              >
                {{ orderStats[tab.statKey] }}
              </span>
            </div>
            <span class="text-xs text-gray-600">{{ tab.label }}</span>
          </router-link>
        </div>
      </div>

      <!-- 功能入口 -->
      <div class="grid grid-cols-2 gap-4 mb-20">
        <router-link to="/address"
          class="bg-white rounded-2xl p-5 shadow-sm card-hover cursor-pointer group flex items-center gap-4"
        >
          <div class="w-12 h-12 rounded-xl flex items-center justify-center transition-all bg-primary/10">
            <MapPin class="w-6 h-6 text-primary" />
          </div>
          <div>
            <p class="font-medium text-gray-900">收货地址</p>
            <p class="text-xs text-gray-400 mt-1">管理收货地址</p>
          </div>
        </router-link>
        <router-link to="/favorites"
          class="bg-white rounded-2xl p-5 shadow-sm card-hover cursor-pointer group flex items-center gap-4"
        >
          <div class="w-12 h-12 rounded-xl flex items-center justify-center transition-all bg-functional-danger/10">
            <Heart class="w-6 h-6 text-functional-danger" />
          </div>
          <div>
            <p class="font-medium text-gray-900">我的收藏</p>
            <p class="text-xs text-gray-400 mt-1">查看收藏的商品</p>
          </div>
        </router-link>
        <router-link to="/user/reviews"
          class="bg-white rounded-2xl p-5 shadow-sm card-hover cursor-pointer group flex items-center gap-4"
        >
          <div class="w-12 h-12 rounded-xl flex items-center justify-center transition-all bg-amber-100">
            <MessageSquareText class="w-6 h-6 text-amber-500" />
          </div>
          <div>
            <p class="font-medium text-gray-900">我的评价</p>
            <p class="text-xs text-gray-400 mt-1">查看我的商品评价</p>
          </div>
        </router-link>
        <router-link to="/orders/refund/list"
          class="bg-white rounded-2xl p-5 shadow-sm card-hover cursor-pointer group flex items-center gap-4"
        >
          <div class="w-12 h-12 rounded-xl flex items-center justify-center transition-all bg-purple-100">
            <RotateCcw class="w-6 h-6 text-purple-500" />
          </div>
          <div>
            <p class="font-medium text-gray-900">售后记录</p>
            <p class="text-xs text-gray-400 mt-1">查看退款/退货进度</p>
          </div>
        </router-link>
        <router-link to="/coupon/draw"
          class="bg-white rounded-2xl p-5 shadow-sm card-hover cursor-pointer group flex items-center gap-4"
        >
          <div class="w-12 h-12 rounded-xl flex items-center justify-center transition-all bg-amber-100">
            <Gift class="w-6 h-6 text-amber-500" />
          </div>
          <div>
            <p class="font-medium text-gray-900">每日抽奖</p>
            <p class="text-xs text-gray-400 mt-1">免费抽取优惠券</p>
          </div>
        </router-link>
        <router-link to="/coupon/my"
          class="bg-white rounded-2xl p-5 shadow-sm card-hover cursor-pointer group flex items-center gap-4"
        >
          <div class="w-12 h-12 rounded-xl flex items-center justify-center transition-all bg-teal-100">
            <Ticket class="w-6 h-6 text-teal-500" />
          </div>
          <div>
            <p class="font-medium text-gray-900">我的优惠券</p>
            <p class="text-xs text-gray-400 mt-1">查看和管理优惠券</p>
          </div>
        </router-link>
        <router-link to="/settings"
          class="bg-white rounded-2xl p-5 shadow-sm card-hover cursor-pointer group flex items-center gap-4"
        >
          <div class="w-12 h-12 rounded-xl flex items-center justify-center transition-all bg-gray-100">
            <Settings class="w-6 h-6 text-gray-600" />
          </div>
          <div>
            <p class="font-medium text-gray-900">设置</p>
            <p class="text-xs text-gray-400 mt-1">账户安全</p>
          </div>
        </router-link>
        <router-link to="/customer-service"
          class="bg-white rounded-2xl p-5 shadow-sm card-hover cursor-pointer group flex items-center gap-4"
        >
          <div class="w-12 h-12 rounded-xl flex items-center justify-center transition-all bg-blue-100">
            <Headset class="w-6 h-6 text-blue-500" />
          </div>
          <div>
            <p class="font-medium text-gray-900">人工客服</p>
            <p class="text-xs text-gray-400 mt-1">在线咨询</p>
          </div>
        </router-link>
        <button
          class="bg-white rounded-2xl p-5 shadow-sm card-hover cursor-pointer group flex items-center gap-4 text-left"
          @click="handleLogout"
        >
          <div class="w-12 h-12 rounded-xl flex items-center justify-center transition-all bg-functional-danger/5">
            <LogOut class="w-6 h-6 text-functional-danger" />
          </div>
          <div>
            <p class="font-medium text-gray-900">退出登录</p>
            <p class="text-xs text-gray-400 mt-1">退出当前账户</p>
          </div>
        </button>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { User, CreditCard, Package, Truck, Star, MapPin, Heart, MessageSquareText, Settings, LogOut, Headset, RotateCcw, Gift, Ticket } from 'lucide-vue-next'
import { getUser } from '@/utils/auth'
import { getMyOrderStats } from '@/api/order'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()
const isLoggedIn = computed(() => !!getUser())
const userInfo = computed(() => getUser())
const orderStats = ref({ pending: 0, paid: 0, shipped: 0, completed: 0 })

const orderTabs = [
  { status: 0, label: '待支付', icon: CreditCard, color: 'text-functional-warning', statKey: 'pending' },
  { status: 1, label: '已支付', icon: Package, color: 'text-functional-info', statKey: 'paid' },
  { status: 2, label: '已发货', icon: Truck, color: 'text-primary', statKey: 'shipped' },
  { status: 3, label: '已完成', icon: Star, color: 'text-functional-success', statKey: 'completed' }
]

async function loadOrderStats() {
  try {
    const res = await getMyOrderStats()
    if (res.code === 200) {
      orderStats.value = res.data || { pending: 0, paid: 0, shipped: 0, completed: 0 }
    }
  } catch (err) {
    console.error('加载订单统计失败:', err)
  }
}

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    userStore.logout()
    window.location.href = '/login'
  } catch {
    // 用户取消
  }
}

onMounted(() => {
  if (isLoggedIn.value) {
    loadOrderStats()
  }
})
</script>

<style scoped>
@keyframes gradient-flow {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}
.animate-gradient-flow {
  animation: gradient-flow 6s ease infinite;
}

@keyframes pulse-slow {
  0%, 100% { opacity: 0.4; transform: scale(1); }
  50% { opacity: 0.8; transform: scale(1.15); }
}
.animate-pulse-slow {
  animation: pulse-slow 4s ease-in-out infinite;
}

@keyframes float-particle {
  0% { transform: translateY(0) scale(1); opacity: 1; }
  50% { transform: translateY(-15px) scale(1.3); opacity: 0.8; }
  100% { transform: translateY(0) scale(1); opacity: 1; }
}
.animate-float-particle {
  animation: float-particle 3s ease-in-out infinite;
}
</style>
