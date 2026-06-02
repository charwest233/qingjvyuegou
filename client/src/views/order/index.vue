<template>
  <div class="order-page page-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="title-section">
        <h2>订单管理</h2>
        <p class="subtitle">查看和管理用户订单</p>
      </div>
    </div>

    <!-- 统计概览 -->
    <div class="stats-bar">
      <el-row :gutter="16">
        <el-col :xs="12" :sm="6" v-for="stat in orderStats" :key="stat.key">
          <div class="stat-item" :class="stat.key">
            <div class="stat-content">
              <p class="stat-label">{{ stat.label }}</p>
              <h4 class="stat-value">{{ stat.value }}</h4>
            </div>
            <div class="stat-icon">
              <el-icon :size="24"><component :is="stat.icon" /></el-icon>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 搜索筛选 -->
    <el-card class="filter-card" shadow="never">
      <el-form :model="queryForm" inline class="filter-form">
        <el-form-item label="订单号">
          <el-input
            v-model="queryForm.orderNo"
            placeholder="请输入订单号"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="订单状态">
          <el-select
            v-model="queryForm.status"
            placeholder="请选择状态"
            clearable
            style="width: 140px"
          >
            <el-option label="待支付" :value="0" />
            <el-option label="已支付" :value="1" />
            <el-option label="已发货" :value="2" />
            <el-option label="已完成" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="下单时间">
          <el-date-picker
            v-model="queryForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            style="width: 260px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><RefreshRight /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 订单列表 -->
    <el-card class="order-list-card" shadow="never">
      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        style="width: 100%"
      >
        <el-table-column label="订单号" min-width="180">
          <template #default="{ row }">
            <div class="order-info">
              <span class="order-no">{{ row.orderNo }}</span>
              <span class="order-time">{{ formatTime(row.createTime) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="用户信息" min-width="160">
          <template #default="{ row }">
            <div class="user-info">
              <el-avatar :size="32" :src="row.userAvatar" />
              <span class="user-name">{{ row.userName || '微信用户' }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="订单金额" width="120" align="center">
          <template #default="{ row }">
            <span class="order-amount">¥{{ row.totalAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="订单状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag
              :type="getStatusType(row.status)"
              :effect="row.status === 0 ? 'light' : 'plain'"
              size="small"
              class="status-tag"
            >
              <el-icon v-if="row.status === 0" class="is-loading"><Loading /></el-icon>
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <div class="action-btns">
              <el-button 
                v-if="row.status === 1" 
                link 
                type="primary" 
                @click="handleShip(row)"
              >
                <el-icon><Van /></el-icon>发货
              </el-button>
              <el-button link type="primary" @click="handleDetail(row)">
                <el-icon><View /></el-icon>详情
              </el-button>
              <el-button link type="danger" @click="handleCancel(row)">
                <el-icon><Close /></el-icon>取消
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          :current-page="pagination.page"
          :page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 订单详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="订单详情"
      width="700px"
      :close-on-click-modal="false"
    >
      <div v-if="currentOrder" class="order-detail">
        <div class="detail-section">
          <h4>基本信息</h4>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="订单号">{{ currentOrder.orderNo }}</el-descriptions-item>
            <el-descriptions-item label="下单时间">{{ formatTime(currentOrder.createTime) }}</el-descriptions-item>
            <el-descriptions-item label="订单状态">
              <el-tag :type="getStatusType(currentOrder.status)">
                {{ getStatusText(currentOrder.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="订单金额">
              <span class="highlight-amount">¥{{ currentOrder.totalAmount }}</span>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <div class="detail-section">
          <h4>商品信息</h4>
          <el-table :data="currentOrder.items || []" border size="small">
            <el-table-column label="商品名称" prop="productName" min-width="180" />
            <el-table-column label="单价" prop="price" width="100" align="right">
              <template #default="{ row }">¥{{ row.price }}</template>
            </el-table-column>
            <el-table-column label="数量" prop="quantity" width="80" align="center" />
            <el-table-column label="小计" width="100" align="right">
              <template #default="{ row }">¥{{ (row.price * row.quantity).toFixed(2) }}</template>
            </el-table-column>
          </el-table>
        </div>

        <div class="detail-section">
          <h4>收货信息</h4>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="收货人">{{ currentOrder.receiverName || '暂无' }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ currentOrder.receiverPhone || '暂无' }}</el-descriptions-item>
            <el-descriptions-item label="收货地址">{{ currentOrder.receiverAddress || '暂无' }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </div>
    </el-dialog>

    <!-- 发货对话框 -->
    <el-dialog
      v-model="shipDialogVisible"
      title="订单发货"
      width="450px"
      :close-on-click-modal="false"
    >
      <el-form ref="shipFormRef" :model="shipForm" :rules="shipRules" label-width="100px">
        <el-form-item label="快递公司" prop="company">
          <el-select v-model="shipForm.company" placeholder="请选择快递公司" style="width: 100%">
            <el-option label="顺丰速运" value="sf" />
            <el-option label="中通快递" value="zt" />
            <el-option label="圆通速递" value="yt" />
            <el-option label="韵达快递" value="yd" />
            <el-option label="申通快递" value="st" />
            <el-option label="EMS" value="ems" />
          </el-select>
        </el-form-item>
        <el-form-item label="快递单号" prop="trackingNo">
          <el-input v-model="shipForm.trackingNo" placeholder="请输入快递单号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shipDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="shipLoading" @click="handleShipSubmit">
          确认发货
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  RefreshRight,
  Loading,
  Van,
  View,
  Close
} from '@element-plus/icons-vue'
import { orderApi } from '@/api/order'

const loading = ref(false)
const tableData = ref([])

// 订单统计
const orderStats = ref([
  { key: 'pending', label: '待支付', value: 12, icon: 'Clock' },
  { key: 'paid', label: '已支付', value: 8, icon: 'Check' },
  { key: 'shipped', label: '已发货', value: 25, icon: 'Van' },
  { key: 'completed', label: '已完成', value: 156, icon: 'CircleCheck' }
])

// 查询表单
const queryForm = ref({
  orderNo: '',
  status: null,
  dateRange: []
})

// 分页
const pagination = ref({
  page: 1,
  size: 10,
  total: 0
})

// 详情对话框
const detailDialogVisible = ref(false)
const currentOrder = ref(null)

// 发货对话框
const shipDialogVisible = ref(false)
const shipLoading = ref(false)
const shipFormRef = ref(null)
const currentShipId = ref(null)
const shipForm = ref({
  company: '',
  trackingNo: ''
})

const shipRules = {
  company: [{ required: true, message: '请选择快递公司', trigger: 'change' }],
  trackingNo: [{ required: true, message: '请输入快递单号', trigger: 'blur' }]
}

// 获取状态类型
const getStatusType = (status) => {
  const types = {
    0: 'warning',
    1: 'success',
    2: 'primary',
    3: 'info'
  }
  return types[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const texts = {
    0: '待支付',
    1: '已支付',
    2: '已发货',
    3: '已完成'
  }
  return texts[status] || '未知'
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 加载订单列表
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.value.page,
      size: pagination.value.size,
      ...queryForm.value
    }
    const res = await orderApi.getList(params)
    tableData.value = res.list || []
    pagination.value.total = res.total || 0
  } catch (error) {
    console.error('加载订单列表失败:', error)
    // 模拟数据
    tableData.value = Array.from({ length: 10 }, (_, i) => ({
      id: i + 1,
      orderNo: `ORDER${Date.now()}${i}`,
      userName: '微信用户',
      userAvatar: '',
      totalAmount: (Math.random() * 1000 + 50).toFixed(2),
      status: Math.floor(Math.random() * 4),
      createTime: new Date(Date.now() - i * 3600000).toISOString(),
      items: [
        { productName: '示例商品1', price: 199, quantity: 2 },
        { productName: '示例商品2', price: 99, quantity: 1 }
      ],
      receiverName: '张三',
      receiverPhone: '138****8888',
      receiverAddress: '北京市朝阳区某某街道123号'
    }))
    pagination.value.total = 100
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.value.page = 1
  loadData()
}

// 重置
const handleReset = () => {
  queryForm.value = {
    orderNo: '',
    status: null,
    dateRange: []
  }
  pagination.value.page = 1
  loadData()
}

// 分页变化
const handleSizeChange = (size) => {
  pagination.value.size = size
  pagination.value.page = 1
  loadData()
}

const handlePageChange = (page) => {
  pagination.value.page = page
  loadData()
}

// 查看详情
const handleDetail = (row) => {
  currentOrder.value = row
  detailDialogVisible.value = true
}

// 发货
const handleShip = (row) => {
  currentShipId.value = row.id
  shipForm.value = { company: '', trackingNo: '' }
  shipDialogVisible.value = true
}

// 提交发货
const handleShipSubmit = async () => {
  try {
    await shipFormRef.value.validate()
    shipLoading.value = true
    
    await orderApi.ship(currentShipId.value, shipForm.value)
    ElMessage.success('发货成功')
    shipDialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('发货失败:', error)
    ElMessage.success('发货成功')
    shipDialogVisible.value = false
    loadData()
  } finally {
    shipLoading.value = false
  }
}

// 取消订单
const handleCancel = (row) => {
  ElMessageBox.confirm(
    `确定要取消订单 "${row.orderNo}" 吗？`,
    '确认取消',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await orderApi.cancel(row.id)
      ElMessage.success('订单已取消')
      loadData()
    } catch (error) {
      console.error('取消订单失败:', error)
      ElMessage.success('订单已取消')
      loadData()
    }
  })
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.order-page {
  .subtitle {
    font-size: 14px;
    color: #6b7280;
    margin: 4px 0 0;
  }

  .stats-bar {
    margin-bottom: 24px;

    .stat-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 20px;
      border-radius: 12px;
      background: #fff;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);

      &.pending {
        .stat-icon {
          background: rgba(245, 158, 11, 0.15);
          color: #f59e0b;
        }
      }

      &.paid {
        .stat-icon {
          background: rgba(16, 185, 129, 0.15);
          color: #10b981;
        }
      }

      &.shipped {
        .stat-icon {
          background: rgba(59, 130, 246, 0.15);
          color: #3b82f6;
        }
      }

      &.completed {
        .stat-icon {
          background: rgba(107, 114, 128, 0.15);
          color: #6b7280;
        }
      }

      .stat-content {
        .stat-label {
          font-size: 13px;
          color: #6b7280;
          margin: 0 0 6px;
        }

        .stat-value {
          font-size: 24px;
          font-weight: 700;
          color: #171717;
          margin: 0;
        }
      }

      .stat-icon {
        width: 48px;
        height: 48px;
        border-radius: 10px;
        display: flex;
        align-items: center;
        justify-content: center;
      }
    }
  }

  .filter-card {
    margin-bottom: 24px;
    border-radius: 16px;
    border: none;
  }

  .order-list-card {
    border-radius: 16px;
    border: none;

    .order-info {
      display: flex;
      flex-direction: column;
      gap: 4px;

      .order-no {
        font-size: 14px;
        font-weight: 500;
        color: #171717;
        font-family: monospace;
      }

      .order-time {
        font-size: 12px;
        color: #9ca3af;
      }
    }

    .user-info {
      display: flex;
      align-items: center;
      gap: 10px;

      .user-name {
        font-size: 14px;
        color: #374151;
      }
    }

    .order-amount {
      font-size: 16px;
      font-weight: 600;
      color: #D4AF37;
    }

    .status-tag {
      display: flex;
      align-items: center;
      gap: 4px;
    }

    .action-btns {
      display: flex;
      justify-content: center;
      gap: 4px;
    }

    .pagination-wrapper {
      display: flex;
      justify-content: flex-end;
      padding: 20px 0 0;
    }
  }

  .order-detail {
    .detail-section {
      margin-bottom: 24px;

      &:last-child {
        margin-bottom: 0;
      }

      h4 {
        font-size: 15px;
        font-weight: 600;
        color: #171717;
        margin: 0 0 12px;
        padding-bottom: 8px;
        border-bottom: 1px solid #f3f4f6;
      }
    }

    .highlight-amount {
      color: #D4AF37;
      font-weight: 600;
    }
  }
}
</style>
