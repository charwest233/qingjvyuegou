<template>
  <div class="min-h-screen bg-background pb-20">
    <div class="sticky top-0 z-10 bg-white/80 backdrop-blur-md border-b border-gray-100">
      <div class="flex items-center gap-3 px-4 py-3">
        <button @click="$router.back()" class="p-2 hover:bg-gray-100 rounded-xl transition-colors cursor-pointer">
          <ArrowLeft class="w-5 h-5 text-text-secondary" />
        </button>
        <h1 class="text-base font-heading font-semibold">售后详情</h1>
      </div>
    </div>

    <div v-if="refund" class="max-w-lg mx-auto px-4 py-4 space-y-3">
      <div class="bg-white rounded-xl p-5 shadow-sm" :class="statusHeaderClass">
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 rounded-full flex items-center justify-center" :class="statusIconClass">
            <Clock v-if="refund.status === 0" class="w-5 h-5" />
            <PenSquare v-if="refund.status === 1" class="w-5 h-5" />
            <Truck v-if="refund.status === 2" class="w-5 h-5" />
            <Clock v-if="refund.status === 3" class="w-5 h-5" />
            <CheckCircle v-if="refund.status === 4" class="w-5 h-5" />
            <XCircle v-if="refund.status === 5" class="w-5 h-5" />
            <RotateCcw v-if="refund.status === 6" class="w-5 h-5" />
          </div>
          <div>
            <h2 class="text-base font-semibold text-text-primary">{{ statusText }}</h2>
            <p class="text-xs text-text-secondary mt-0.5">{{ statusDesc }}</p>
          </div>
        </div>
      </div>

      <div class="bg-white rounded-xl p-4 shadow-sm space-y-3">
        <h3 class="text-sm font-medium text-text-primary">售后信息</h3>
        <div class="space-y-2 text-sm">
          <div class="flex justify-between"><span class="text-text-secondary">售后单号</span><span class="text-text-primary font-mono">{{ refund.refundNo }}</span></div>
          <div class="flex justify-between"><span class="text-text-secondary">售后类型</span><span class="text-text-primary">{{ refund.type === 1 ? '仅退款' : '退货退款' }}</span></div>
          <div class="flex justify-between"><span class="text-text-secondary">退款金额</span><span class="text-functional-danger font-semibold">¥{{ refund.refundAmount?.toFixed(2) }}</span></div>
          <div class="flex justify-between"><span class="text-text-secondary">退款原因</span><span class="text-text-primary">{{ refund.reason }}</span></div>
          <div v-if="refund.description" class="flex justify-between"><span class="text-text-secondary">问题描述</span><span class="text-text-primary text-right max-w-[60%]">{{ refund.description }}</span></div>
          <div class="flex justify-between"><span class="text-text-secondary">申请时间</span><span class="text-text-primary">{{ formatTime(refund.applyTime) }}</span></div>
          <div v-if="refund.auditRemark" class="flex justify-between"><span class="text-text-secondary">审核备注</span><span class="text-text-primary text-right max-w-[60%]">{{ refund.auditRemark }}</span></div>
        </div>
      </div>

      <!-- 退货退款：状态1(待填物流)时填写物流 -->
      <div v-if="refund.status === 1 && refund.type === 2" class="bg-white rounded-xl p-4 shadow-sm space-y-3">
        <h3 class="text-sm font-medium text-text-primary">填写退货物流</h3>
        <p class="text-xs text-text-secondary">请将商品寄回后填写快递信息</p>
        <el-select v-model="expressCompany" placeholder="选择快递公司" class="w-full">
          <el-option label="顺丰速运" value="sf" />
          <el-option label="中通快递" value="zt" />
          <el-option label="圆通速递" value="yt" />
          <el-option label="韵达快递" value="yd" />
          <el-option label="申通快递" value="st" />
          <el-option label="EMS" value="ems" />
        </el-select>
        <el-input v-model="expressNo" placeholder="输入快递单号" />
        <button
          class="w-full py-2.5 rounded-xl text-sm font-medium text-white transition-colors cursor-pointer disabled:opacity-50"
          :class="logisticsSubmitting ? 'bg-gray-400' : 'bg-primary hover:bg-primary-dark'"
          :disabled="!expressCompany || !expressNo || logisticsSubmitting"
          @click="handleSubmitLogistics"
        >{{ logisticsSubmitting ? '提交中...' : '提交物流信息' }}</button>
      </div>

      <div v-if="refund.expressCompany" class="bg-white rounded-xl p-4 shadow-sm space-y-2">
        <h3 class="text-sm font-medium text-text-primary">退货物流</h3>
        <div class="text-sm text-text-secondary">{{ expressName(refund.expressCompany) }} · {{ refund.expressNo }}</div>
      </div>

      <div v-if="refund.status === 0 || refund.status === 1 || refund.status === 2" class="flex gap-3">
        <button
          class="flex-1 py-2.5 rounded-xl text-sm font-medium border border-gray-200 text-text-secondary hover:bg-gray-50 transition-colors cursor-pointer"
          @click="handleCancel"
        >撤销申请</button>
      </div>
    </div>

    <div v-else class="text-center py-20 text-sm text-text-secondary">加载中...</div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ArrowLeft, Clock, CheckCircle, XCircle, Truck, RotateCcw, PenSquare } from 'lucide-vue-next'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRefundDetail, fillReturnLogistics, cancelRefund } from '@/api/refund'

const route = useRoute()
const refund = ref(null)
const expressCompany = ref('')
const expressNo = ref('')
const logisticsSubmitting = ref(false)

const statusText = computed(() => {
  const map = { 0: '待审核', 1: '待填物流', 2: '退货中', 3: '待退款', 4: '已退款', 5: '审核驳回', 6: '已撤销' }
  return map[refund.value?.status] || '未知'
})

const statusDesc = computed(() => {
  const map = {
    0: '退款申请已提交，请耐心等待客服审核',
    1: '请将商品寄回并填写物流信息',
    2: '商品退回中，等待管理员确认收货',
    3: '管理员已确认收货，等待审核退款',
    4: '退款已完成，金额将原路返回',
    5: refund.value?.auditRemark || '售后申请未通过审核',
    6: '您已撤销该售后申请'
  }
  return map[refund.value?.status] || ''
})

const statusHeaderClass = computed(() => {
  return ({ 0: 'border-l-4 border-amber-400', 4: 'border-l-4 border-green-500', 5: 'border-l-4 border-gray-400' })[refund.value?.status] || 'border-l-4 border-primary'
})

const statusIconClass = computed(() => {
  return ({ 0: 'bg-amber-100 text-amber-500', 4: 'bg-green-100 text-green-500', 5: 'bg-gray-100 text-gray-400' })[refund.value?.status] || 'bg-primary/10 text-primary'
})

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
}

function expressName(code) {
  return ({ sf: '顺丰速运', zt: '中通快递', yt: '圆通速递', yd: '韵达快递', st: '申通快递', ems: 'EMS' })[code] || code
}

async function handleSubmitLogistics() {
  if (!refund.value) return
  logisticsSubmitting.value = true
  try {
    const res = await fillReturnLogistics(refund.value.id, expressCompany.value, expressNo.value)
    if (res.code === 200) {
      ElMessage.success('物流信息已提交')
      refund.value.status = 2
      refund.value.expressCompany = expressCompany.value
      refund.value.expressNo = expressNo.value
    }
  } catch { ElMessage.error('提交失败') }
  finally { logisticsSubmitting.value = false }
}

async function handleCancel() {
  try {
    await ElMessageBox.confirm('确定要撤销该售后申请吗？', '提示', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    const res = await cancelRefund(refund.value.id)
    if (res.code === 200) { ElMessage.success('已撤销'); refund.value.status = 6 }
  } catch { /* ignore */ }
}

onMounted(async () => {
  const id = route.params.id
  if (!id) return
  try {
    const res = await getRefundDetail(id)
    if (res.code === 200 && res.data) refund.value = res.data
  } catch { /* ignore */ }
})
</script>
