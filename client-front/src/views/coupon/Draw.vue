<template>
  <div class="min-h-screen bg-background pb-20">
    <div class="sticky top-0 z-10 bg-white/80 backdrop-blur-md border-b border-gray-100">
      <div class="flex items-center gap-3 px-4 py-3">
        <button @click="$router.back()" class="p-2 hover:bg-gray-100 rounded-xl transition-colors cursor-pointer">
          <ArrowLeft class="w-5 h-5 text-text-secondary" />
        </button>
        <h1 class="text-base font-heading font-semibold">每日抽奖</h1>
      </div>
    </div>

    <div class="px-4 py-6 space-y-6">
      <!-- 抽奖转盘区域 -->
      <div class="flex flex-col items-center">
        <div class="relative w-64 h-64 mb-4">
          <div class="absolute inset-0 rounded-full bg-gradient-to-br from-primary/20 via-primary/5 to-amber-100 animate-pulse-soft" />
          <div
            class="absolute inset-4 rounded-full bg-white shadow-lg flex items-center justify-center cursor-pointer transition-transform duration-300"
            :class="spinning ? 'scale-95' : 'hover:scale-105'"
            @click="handleDraw"
          >
            <div v-if="spinning" class="text-center">
              <Loader2 class="w-10 h-10 text-primary mx-auto animate-spin" />
              <p class="text-xs text-text-secondary mt-2">开奖中...</p>
            </div>
            <div v-else class="text-center">
              <Gift class="w-12 h-12 text-primary mx-auto" />
              <p class="text-sm font-heading font-bold text-text-primary mt-2">点击抽奖</p>
              <p class="text-xs text-text-secondary mt-1">剩余 {{ remaining }} 次</p>
            </div>
          </div>
        </div>

        <!-- 奖品列表 -->
        <div class="w-full max-w-md">
          <h3 class="text-sm font-medium text-text-primary mb-3">今日奖品</h3>
          <div class="grid grid-cols-5 gap-2">
            <div v-for="p in prizes" :key="p.id"
              class="flex flex-col items-center gap-1 p-2 rounded-lg border"
              :class="p.tier <= 2 ? 'border-gray-200 bg-white' : p.tier <= 3 ? 'border-blue-200 bg-blue-50' : p.tier === 4 ? 'border-purple-200 bg-purple-50' : 'border-amber-200 bg-amber-50'"
            >
              <span class="text-lg">{{ tierIcon(p.tier) }}</span>
              <span class="text-[10px] font-medium text-center leading-tight"
                :class="p.tier <= 2 ? 'text-text-secondary' : p.tier <= 3 ? 'text-blue-600' : p.tier === 4 ? 'text-purple-600' : 'text-amber-600'"
              >{{ p.name }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 中奖弹窗 -->
      <el-dialog v-model="showResult" title="恭喜中奖！" width="320px" :close-on-click-modal="false">
        <div class="text-center py-4">
          <div class="w-16 h-16 rounded-full bg-green-100 flex items-center justify-center mx-auto mb-3">
            <Gift class="w-8 h-8 text-green-500" />
          </div>
          <p class="text-lg font-heading font-bold text-text-primary">{{ wonCoupon?.name }}</p>
          <p class="text-functional-danger text-2xl font-bold my-2">¥{{ wonCoupon?.value?.toFixed(2) }}</p>
          <p class="text-xs text-text-secondary">满¥{{ wonCoupon?.minAmount }}可用 · {{ wonCoupon?.validDays || 7 }}天有效</p>
        </div>
        <template #footer>
          <el-button type="primary" @click="showResult = false" class="w-full">收下优惠券</el-button>
        </template>
      </el-dialog>

      <!-- 未中奖提示 -->
      <el-dialog v-model="showNoWin" title="很遗憾" width="300px">
        <div class="text-center py-4">
          <div class="w-16 h-16 rounded-full bg-gray-100 flex items-center justify-center mx-auto mb-3">
            <Frown class="w-8 h-8 text-gray-400" />
          </div>
          <p class="text-text-secondary">这次没有中奖，明天继续加油！</p>
        </div>
        <template #footer>
          <el-button @click="showNoWin = false">知道了</el-button>
        </template>
      </el-dialog>

      <!-- 次数用完 -->
      <el-dialog v-model="showFull" title="提示" width="300px">
        <div class="text-center py-4">
          <p class="text-text-secondary">今日抽奖次数已用完，明天再来吧！</p>
        </div>
        <template #footer>
          <el-button @click="showFull = false">知道了</el-button>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ArrowLeft, Gift, Loader2, Frown } from 'lucide-vue-next'
import { ElMessage } from 'element-plus'
import { getDrawStatus, draw } from '@/api/coupon'

const remaining = ref(0)
const prizes = ref([])
const spinning = ref(false)
const showResult = ref(false)
const showNoWin = ref(false)
const showFull = ref(false)
const wonCoupon = ref(null)

function tierIcon(tier) {
  return ['🎯', '🎁', '🌟', '💎', '👑'][tier - 1] || '🎁'
}

async function loadStatus() {
  try {
    const res = await getDrawStatus()
    if (res.code === 200 && res.data) {
      remaining.value = res.data.remaining
      prizes.value = res.data.prizes || []
    }
  } catch { /* ignore */ }
}

async function handleDraw() {
  if (spinning.value) return
  if (remaining.value <= 0) { showFull.value = true; return }

  spinning.value = true
  try {
    const res = await draw()
    if (res.code === 200) {
      if (res.data) {
        wonCoupon.value = res.data
        setTimeout(() => { showResult.value = true }, 500)
      } else {
        setTimeout(() => { showNoWin.value = true }, 500)
      }
      remaining.value--
    } else {
      if (res.message?.includes('用完')) showFull.value = true
      else ElMessage.error(res.message || '抽奖失败')
    }
  } catch { ElMessage.error('抽奖失败') }
  finally { spinning.value = false }
}

onMounted(() => loadStatus())
</script>
