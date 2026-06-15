<template>
  <div class="min-h-screen bg-background pb-20">
    <div class="sticky top-0 z-10 bg-white/80 backdrop-blur-md border-b border-gray-100">
      <div class="flex items-center gap-3 px-4 py-3">
        <button @click="$router.back()" class="p-2 hover:bg-gray-100 rounded-xl transition-colors cursor-pointer">
          <ArrowLeft class="w-5 h-5 text-text-secondary" />
        </button>
        <h1 class="text-base font-heading font-semibold">我的优惠券</h1>
      </div>
    </div>

    <div class="px-4 py-4 space-y-3">
      <div v-if="loading" class="text-center py-16 text-sm text-text-secondary">加载中...</div>

      <div v-else-if="list.length === 0" class="text-center py-16">
        <Ticket class="w-12 h-12 text-gray-200 mx-auto mb-3" />
        <p class="text-sm text-text-secondary">暂无优惠券</p>
        <router-link to="/coupon/draw" class="inline-block mt-4 text-sm text-primary font-medium">去抽奖 →</router-link>
      </div>

      <div v-for="c in list" :key="c.id"
        class="bg-white rounded-xl overflow-hidden shadow-sm flex"
      >
        <!-- 左侧面值 -->
        <div class="w-28 bg-gradient-to-br from-primary to-primary-dark p-4 flex flex-col items-center justify-center text-white relative overflow-hidden">
          <div class="absolute -right-4 -top-4 w-16 h-16 bg-white/10 rounded-full" />
          <div class="absolute -left-4 -bottom-4 w-12 h-12 bg-white/10 rounded-full" />
          <span class="text-2xl font-bold font-heading">¥{{ c.value?.toFixed(0) }}</span>
          <span class="text-[10px] opacity-80 mt-1">满{{ c.minAmount }}可用</span>
        </div>
        <!-- 右侧信息 -->
        <div class="flex-1 p-3 flex flex-col justify-between">
          <div>
            <p class="text-sm font-medium text-text-primary">{{ c.name }}</p>
            <p class="text-xs text-text-secondary mt-1">有效期至 {{ formatTime(c.expiresAt) }}</p>
          </div>
          <div class="flex items-center gap-2 mt-2">
            <span class="text-[10px] px-1.5 py-0.5 rounded-full"
              :class="statusBadge(c.status)"
            >{{ c.status === 0 ? '未使用' : c.status === 1 ? '已使用' : '已过期' }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ArrowLeft, Ticket } from 'lucide-vue-next'
import { getMyCoupons } from '@/api/coupon'

const list = ref([])
const loading = ref(true)

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`
}

function statusBadge(status) {
  return ({ 0: 'bg-green-50 text-green-600', 1: 'bg-gray-100 text-gray-400', 2: 'bg-gray-100 text-gray-400' })[status] || ''
}

onMounted(async () => {
  loading.value = true
  try {
    const res = await getMyCoupons()
    if (res.code === 200 && res.data) list.value = res.data
  } catch { /* ignore */ }
  finally { loading.value = false }
})
</script>
