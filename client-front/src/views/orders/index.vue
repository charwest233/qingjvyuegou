<template>
  <div class="page-container py-4">
    <h1 class="section-title">我的订单</h1>

    <!-- 状态Tab -->
    <div class="flex gap-2 mb-4 overflow-x-auto pb-2 scrollbar-hide">
      <button
        v-for="tab in statusTabs"
        :key="tab.value"
        class="px-4 py-2 rounded-full text-sm font-medium whitespace-nowrap transition-all duration-200 cursor-pointer"
        :class="currentStatus === tab.value
          ? 'bg-primary text-white shadow-md'
          : 'bg-white text-gray-600 hover:bg-primary/10 border border-gray-200'"
        @click="switchStatus(tab.value)"
      >
        {{ tab.label }}
      </button>
    </div>

    <!-- 订单列表 -->
    <div v-if="loading" class="space-y-4">
      <div v-for="n in 3" :key="n" class="bg-white rounded-xl overflow-hidden shadow-sm animate-pulse">
        <div class="p-4 border-b border-gray-50">
          <div class="h-4 bg-gray-200 rounded w-1/3" />
        </div>
        <div class="p-4 space-y-3">
          <div class="h-16 bg-gray-200 rounded" />
          <div class="h-16 bg-gray-200 rounded" />
        </div>
      </div>
    </div>
    <div v-else-if="orders.length === 0" class="text-center py-20">
      <ClipboardList class="w-20 h-20 mx-auto text-gray-300 mb-4" />
      <p class="text-gray-400">暂无订单</p>
      <router-link to="/" class="btn-primary inline-block mt-4">去购物</router-link>
    </div>
    <div v-else class="space-y-4">
      <OrderCard
        v-for="order in orders"
        :key="order.id"
        :order="order"
        @pay="handlePay"
        @cancel="handleCancel"
        @ship="handleConfirm"
        @reviewed="handleReviewed"
      />
    </div>

    <!-- 分页 -->
    <div v-if="totalPages > 1" class="flex justify-center mt-8">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        background
        @current-change="loadOrders"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ClipboardList } from 'lucide-vue-next'
import { ElMessage, ElMessageBox } from 'element-plus'
import OrderCard from '@/components/OrderCard.vue'
import { getOrders, payOrder, cancelOrder, confirmOrder } from '@/api/order'

const currentStatus = ref(null)
const orders = ref([])
const loading = ref(true)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const totalPages = ref(0)

const statusTabs = [
  { value: null, label: '全部' },
  { value: 0, label: '待支付' },
  { value: 1, label: '已支付' },
  { value: 2, label: '已发货' },
  { value: 3, label: '已完成' },
  { value: -1, label: '已取消' }
]

async function loadOrders() {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      status: currentStatus.value
    }
    const res = await getOrders(params)
    if (res.code === 200) {
      orders.value = res.data?.list || res.data?.records || res.data || []
      total.value = res.data?.total || 0
      totalPages.value = res.data?.pages || Math.ceil(total.value / pageSize.value) || 0
    }
  } catch (err) {
    console.error('加载订单失败:', err)
  } finally {
    loading.value = false
  }
}

function switchStatus(status) {
  currentStatus.value = status
  currentPage.value = 1
  loadOrders()
}

async function handlePay(order) {
  try {
    const res = await payOrder(order.id)
    if (res.code === 200) {
      ElMessage.success('支付成功')
      loadOrders()
    }
  } catch (err) {
    console.error('支付失败:', err)
  }
}

async function handleCancel(order) {
  try {
    await ElMessageBox.confirm('确定要取消该订单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await cancelOrder(order.id)
    if (res.code === 200) {
      ElMessage.success('订单已取消')
      loadOrders()
    }
  } catch {
    // 用户取消
  }
}

async function handleConfirm(order) {
  try {
    await ElMessageBox.confirm('确认已收到商品吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    const res = await confirmOrder(order.id)
    if (res.code === 200) {
      ElMessage.success('已确认收货')
      loadOrders()
    }
  } catch {
    // 用户取消
  }
}

function handleReviewed() {
  loadOrders()
}

function handleDeleteOrder() {
  loadOrders()
}

onMounted(() => {
  loadOrders()
})
</script>
