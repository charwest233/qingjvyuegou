<template>
  <div class="min-h-screen bg-background pb-20">
    <div class="sticky top-0 z-10 bg-white/80 backdrop-blur-md border-b border-gray-100">
      <div class="flex items-center gap-3 px-4 py-3">
        <button @click="$router.back()" class="p-2 hover:bg-gray-100 rounded-xl transition-colors cursor-pointer">
          <ArrowLeft class="w-5 h-5 text-text-secondary" />
        </button>
        <h1 class="text-base font-heading font-semibold">申请售后</h1>
      </div>
    </div>

    <div class="max-w-lg mx-auto px-4 py-4 space-y-4">
      <!-- 商品信息 -->
      <div class="bg-white rounded-xl p-4 shadow-sm">
        <h3 class="text-sm font-medium text-text-primary mb-3">选择商品</h3>
        <div
          v-for="item in order.items" :key="item.id"
          class="flex items-center gap-3 p-3 rounded-lg border cursor-pointer transition-colors"
          :class="selectedItemId === item.id ? 'border-primary bg-primary/10' : 'border-gray-100 hover:border-gray-200'"
          @click="selectItem(item)"
        >
          <div class="w-14 h-14 rounded-lg bg-gray-100 overflow-hidden shrink-0">
            <img v-if="item.productImage" :src="item.productImage" class="w-full h-full object-cover" />
            <div v-else class="w-full h-full flex items-center justify-center text-gray-300"><Package class="w-6 h-6" /></div>
          </div>
          <div class="flex-1 min-w-0">
            <p class="text-sm text-text-primary line-clamp-1">{{ item.productName }}</p>
            <p class="text-xs text-text-secondary mt-1">x{{ item.quantity }} · ¥{{ (item.price * item.quantity).toFixed(2) }}</p>
          </div>
          <div v-if="selectedItemId === item.id" class="w-5 h-5 rounded-full bg-primary text-white flex items-center justify-center shrink-0">
            <Check class="w-3 h-3" />
          </div>
        </div>
      </div>

      <!-- 售后类型 -->
      <div class="bg-white rounded-xl p-4 shadow-sm">
        <h3 class="text-sm font-medium text-text-primary mb-3">售后类型</h3>
        <div class="grid grid-cols-2 gap-3">
          <div
            class="flex flex-col items-center gap-1 p-3 rounded-lg border cursor-pointer transition-colors"
            :class="refundType === 1 ? 'border-primary bg-primary/10' : 'border-gray-100 hover:border-gray-200'"
            @click="refundType = 1"
          >
            <CircleDollarSign class="w-5 h-5" :class="refundType === 1 ? 'text-primary' : 'text-text-secondary'" />
            <span class="text-xs font-medium" :class="refundType === 1 ? 'text-primary-dark' : 'text-text-secondary'">仅退款</span>
            <span class="text-[10px] text-text-secondary">未发货订单</span>
          </div>
          <div
            class="flex flex-col items-center gap-1 p-3 rounded-lg border cursor-pointer transition-colors"
            :class="refundType === 2 ? 'border-primary bg-primary/10' : 'border-gray-100 hover:border-gray-200'"
            @click="refundType = 2"
          >
            <Truck class="w-5 h-5" :class="refundType === 2 ? 'text-primary' : 'text-text-secondary'" />
            <span class="text-xs font-medium" :class="refundType === 2 ? 'text-primary-dark' : 'text-text-secondary'">退货退款</span>
            <span class="text-[10px] text-text-secondary">已发货/已完成</span>
          </div>
        </div>
      </div>

      <!-- 退款金额 -->
      <div class="bg-white rounded-xl p-4 shadow-sm">
        <div class="flex items-center justify-between">
          <span class="text-sm text-text-primary">退款金额</span>
          <span class="text-lg font-bold text-functional-danger">¥{{ refundAmount.toFixed(2) }}</span>
        </div>
      </div>

      <!-- 退款原因 -->
      <div class="bg-white rounded-xl p-4 shadow-sm space-y-3">
        <h3 class="text-sm font-medium text-text-primary">退款原因</h3>
        <el-select v-model="reason" placeholder="请选择退款原因" class="w-full">
          <el-option label="商品与描述不符" value="商品与描述不符" />
          <el-option label="质量问题" value="质量问题" />
          <el-option label="错发/漏发" value="错发/漏发" />
          <el-option label="不想要了" value="不想要了" />
          <el-option label="其他原因" value="其他原因" />
        </el-select>
        <el-input
          v-model="description" type="textarea" :rows="3"
          placeholder="请描述具体问题（选填）" maxlength="500" show-word-limit
        />
      </div>

      <button
        class="w-full py-3 rounded-xl text-sm font-medium text-white transition-colors cursor-pointer disabled:opacity-50"
        :class="submitting ? 'bg-gray-400' : 'bg-primary hover:bg-primary-dark'"
        :disabled="!selectedItemId || !reason || submitting"
        @click="handleSubmit"
      >{{ submitting ? '提交中...' : '提交申请' }}</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft, Package, Check, CircleDollarSign, Truck } from 'lucide-vue-next'
import { ElMessage } from 'element-plus'
import { getOrderDetail } from '@/api/order'
import { applyRefund } from '@/api/refund'

const route = useRoute()
const router = useRouter()

const order = ref({ items: [] })
const selectedItemId = ref(null)
const refundType = ref(1)
const reason = ref('')
const description = ref('')
const submitting = ref(false)

const refundAmount = computed(() => {
  if (!selectedItemId.value || !order.value.items) return 0
  const item = order.value.items.find(i => i.id === selectedItemId.value)
  return item ? item.price * item.quantity : 0
})

function selectItem(item) {
  selectedItemId.value = item.id
  if (Number(order.value.status) >= 2) refundType.value = 2
}

async function handleSubmit() {
  if (!selectedItemId.value || !reason.value) return
  submitting.value = true
  try {
    const res = await applyRefund({
      orderId: order.value.id, orderItemId: selectedItemId.value,
      type: refundType.value, reason: reason.value, description: description.value
    })
    if (res.code === 200) {
      ElMessage.success('售后申请已提交，请等待审核')
      router.push({ name: 'RefundList' })
    } else ElMessage.error(res.message || '提交失败')
  } catch { ElMessage.error('提交失败') }
  finally { submitting.value = false }
}

onMounted(async () => {
  const orderId = route.params.orderId
  if (!orderId) { ElMessage.error('参数错误'); return }
  try {
    const res = await getOrderDetail(orderId)
    if (res.code === 200 && res.data) order.value = res.data
  } catch { /* ignore */ }
})
</script>
