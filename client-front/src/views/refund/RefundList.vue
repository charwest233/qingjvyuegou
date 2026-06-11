<template>
  <div class="min-h-screen bg-background pb-20">
    <div class="sticky top-0 z-10 bg-white/80 backdrop-blur-md border-b border-gray-100">
      <div class="flex items-center gap-3 px-4 py-3">
        <button @click="$router.back()" class="p-2 hover:bg-gray-100 rounded-xl transition-colors cursor-pointer">
          <ArrowLeft class="w-5 h-5 text-text-secondary" />
        </button>
        <h1 class="text-base font-heading font-semibold">售后记录</h1>
      </div>
    </div>

    <div class="bg-white border-b border-gray-100 flex">
      <button v-for="tab in tabs" :key="tab.key"
        class="flex-1 py-3 text-sm font-medium text-center transition-colors cursor-pointer"
        :class="activeTab === tab.key ? 'text-primary border-b-2 border-primary' : 'text-text-secondary hover:text-text-primary'"
        @click="activeTab = tab.key"
      >{{ tab.label }}</button>
    </div>

    <div class="px-4 py-4 space-y-3">
      <div v-if="filteredList.length === 0" class="text-center py-16">
        <PackageSearch class="w-12 h-12 text-gray-200 mx-auto mb-3" />
        <p class="text-sm text-text-secondary">暂无售后记录</p>
      </div>
      <div v-for="r in filteredList" :key="r.id"
        class="bg-white rounded-xl shadow-sm overflow-hidden cursor-pointer"
        @click="$router.push(`/orders/refund/${r.id}`)"
      >
        <div class="flex items-center justify-between px-4 py-3 border-b border-gray-50">
          <span class="text-xs text-text-secondary font-mono">{{ r.refundNo }}</span>
          <span class="text-xs px-2 py-0.5 rounded-full" :class="statusBadgeClass(r.status)">{{ statusText(r.status) }}</span>
        </div>
        <div class="flex items-center gap-3 px-4 py-3">
          <div class="w-14 h-14 rounded-lg bg-gray-100 overflow-hidden shrink-0">
            <img v-if="r.productImage" :src="r.productImage" class="w-full h-full object-cover" />
            <div v-else class="w-full h-full flex items-center justify-center text-gray-300"><Package class="w-6 h-6" /></div>
          </div>
          <div class="flex-1 min-w-0">
            <p class="text-sm text-text-primary line-clamp-1">{{ r.productName || '整单退款' }}</p>
            <p class="text-xs text-text-secondary mt-1">退款金额：<span class="text-functional-danger font-medium">¥{{ r.refundAmount?.toFixed(2) }}</span></p>
          </div>
          <ChevronRight class="w-4 h-4 text-gray-300 shrink-0" />
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ArrowLeft, PackageSearch, Package, ChevronRight } from 'lucide-vue-next'
import { getRefundList } from '@/api/refund'

const activeTab = ref('all')
const refundList = ref([])

const tabs = [
  { key: 'all', label: '全部' },
  { key: '1', label: '待填物流' },
  { key: '4', label: '已完成' },
  { key: '5', label: '已驳回' }
]

const filteredList = computed(() => {
  if (activeTab.value === 'all') return refundList.value
  return refundList.value.filter(r => r.status === Number(activeTab.value))
})

function statusText(status) {
  return ({ 0: '待审核', 1: '待填物流', 2: '退货中', 3: '待退款', 4: '已退款', 5: '已驳回', 6: '已撤销' })[status] || '未知'
}

function statusBadgeClass(status) {
  const map = {
    0: 'bg-amber-50 text-amber-500',
    1: 'bg-blue-50 text-blue-500',
    2: 'bg-primary/10 text-primary',
    3: 'bg-orange-50 text-orange-500',
    4: 'bg-green-50 text-green-500',
    5: 'bg-gray-100 text-text-secondary',
    6: 'bg-gray-100 text-text-secondary'
  }
  return map[status] || 'bg-gray-100 text-text-secondary'
}

onMounted(async () => {
  try {
    const res = await getRefundList()
    if (res.code === 200 && res.data) refundList.value = res.data
  } catch { /* ignore */ }
})
</script>
