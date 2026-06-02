<template>
  <div class="user-page page-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="title-section">
        <h2>用户管理</h2>
        <p class="subtitle">查看和管理小程序用户</p>
      </div>
    </div>

    <!-- 统计概览 -->
    <div class="stats-row">
      <el-row :gutter="24">
        <el-col :xs="24" :sm="8">
          <div class="stat-card primary">
            <div class="stat-content">
              <p class="stat-label">用户总数</p>
              <h3 class="stat-value">{{ userStats.total || 0 }}</h3>
            </div>
            <div class="stat-icon">
              <el-icon :size="28"><UserFilled /></el-icon>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="8">
          <div class="stat-card success">
            <div class="stat-content">
              <p class="stat-label">今日新增</p>
              <h3 class="stat-value">{{ userStats.todayNew || 0 }}</h3>
            </div>
            <div class="stat-icon">
              <el-icon :size="28"><User /></el-icon>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :sm="8">
          <div class="stat-card warning">
            <div class="stat-content">
              <p class="stat-label">本周活跃</p>
              <h3 class="stat-value">{{ userStats.weekActive || 0 }}</h3>
            </div>
            <div class="stat-icon">
              <el-icon :size="28"><TrendCharts /></el-icon>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 搜索筛选 -->
    <el-card class="filter-card" shadow="never">
      <el-form :model="queryForm" inline class="filter-form">
        <el-form-item label="用户昵称">
          <el-input
            v-model="queryForm.nickname"
            placeholder="请输入用户昵称"
            clearable
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="注册时间">
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

    <!-- 用户列表 -->
    <el-card class="user-list-card" shadow="never">
      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        style="width: 100%"
      >
        <el-table-column label="用户信息" min-width="240">
          <template #default="{ row }">
            <div class="user-info">
              <el-avatar :size="48" :src="row.avatarUrl" class="user-avatar">
                <el-icon><UserFilled /></el-icon>
              </el-avatar>
              <div class="user-detail">
                <h4 class="user-name">{{ row.nickname || '微信用户' }}</h4>
                <p class="user-id">ID: {{ row.id }}</p>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="OpenID" min-width="200">
          <template #default="{ row }">
            <span class="openid">{{ maskOpenid(row.openid) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="注册时间" width="160" align="center">
          <template #default="{ row }">
            <span class="time">{{ formatTime(row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="订单数" width="100" align="center">
          <template #default="{ row }">
            <el-tag type="info" size="small">{{ row.orderCount || 0 }}笔</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="消费金额" width="120" align="center">
          <template #default="{ row }">
            <span class="amount">¥{{ row.totalAmount || '0.00' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" @click="handleViewOrders(row)">
              <el-icon><View /></el-icon>查看订单
            </el-button>
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

    <!-- 用户订单对话框 -->
    <el-dialog
      v-model="ordersDialogVisible"
      :title="`${currentUser?.nickname || '用户'}的订单`"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-table :data="userOrders" stripe size="small">
        <el-table-column label="订单号" prop="orderNo" min-width="160" />
        <el-table-column label="金额" prop="totalAmount" width="100" align="right">
          <template #default="{ row }">
            <span class="amount">¥{{ row.totalAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" prop="status" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="下单时间" prop="createTime" width="150" align="center">
          <template #default="{ row }">
            <span class="time">{{ formatTime(row.createTime) }}</span>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  User,
  UserFilled,
  Search,
  RefreshRight,
  View,
  TrendCharts
} from '@element-plus/icons-vue'
import { userApi } from '@/api/user'

const loading = ref(false)
const tableData = ref([])
const userStats = ref({})

// 查询表单
const queryForm = ref({
  nickname: '',
  dateRange: []
})

// 分页
const pagination = ref({
  page: 1,
  size: 10,
  total: 0
})

// 订单对话框
const ordersDialogVisible = ref(false)
const currentUser = ref(null)
const userOrders = ref([])

// 掩码OpenID
const maskOpenid = (openid) => {
  if (!openid) return '-'
  if (openid.length <= 10) return openid
  return openid.substring(0, 6) + '****' + openid.substring(openid.length - 4)
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  })
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

// 加载统计数据
const loadStatistics = async () => {
  try {
    const res = await userApi.getStatistics()
    userStats.value = res
  } catch (error) {
    console.error('加载统计数据失败:', error)
    userStats.value = {
      total: 1234,
      todayNew: 45,
      weekActive: 328
    }
  }
}

// 加载用户列表
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.value.page,
      size: pagination.value.size,
      ...queryForm.value
    }
    const res = await userApi.getList(params)
    tableData.value = res.list || []
    pagination.value.total = res.total || 0
  } catch (error) {
    console.error('加载用户列表失败:', error)
    // 模拟数据
    tableData.value = Array.from({ length: 10 }, (_, i) => ({
      id: i + 1,
      nickname: `用户${i + 1}`,
      openid: `openid_${Date.now()}_${i}`,
      avatarUrl: '',
      createTime: new Date(Date.now() - i * 86400000).toISOString(),
      orderCount: Math.floor(Math.random() * 20),
      totalAmount: (Math.random() * 2000 + 100).toFixed(2)
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
    nickname: '',
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

// 查看用户订单
const handleViewOrders = async (row) => {
  currentUser.value = row
  ordersDialogVisible.value = true
  
  // 模拟加载用户订单
  userOrders.value = Array.from({ length: 5 }, (_, i) => ({
    orderNo: `ORDER${Date.now()}${i}`,
    totalAmount: (Math.random() * 500 + 50).toFixed(2),
    status: Math.floor(Math.random() * 4),
    createTime: new Date(Date.now() - i * 86400000).toISOString()
  }))
}

onMounted(() => {
  loadStatistics()
  loadData()
})
</script>

<style scoped lang="scss">
.user-page {
  .subtitle {
    font-size: 14px;
    color: #6b7280;
    margin: 4px 0 0;
  }

  .stats-row {
    margin-bottom: 24px;

    .stat-card {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 24px;
      border-radius: 16px;
      background: #fff;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
      transition: transform 0.2s, box-shadow 0.2s;

      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
      }

      &.primary {
        .stat-icon {
          background: linear-gradient(135deg, #171717 0%, #404040 100%);
          color: #fff;
        }
      }

      &.success {
        .stat-icon {
          background: linear-gradient(135deg, #10b981 0%, #34d399 100%);
          color: #fff;
        }
      }

      &.warning {
        .stat-icon {
          background: linear-gradient(135deg, #f59e0b 0%, #fbbf24 100%);
          color: #fff;
        }
      }

      .stat-content {
        .stat-label {
          font-size: 14px;
          color: #6b7280;
          margin: 0 0 8px;
        }

        .stat-value {
          font-size: 32px;
          font-weight: 700;
          color: #171717;
          margin: 0;
          line-height: 1;
        }
      }

      .stat-icon {
        width: 56px;
        height: 56px;
        border-radius: 12px;
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

  .user-list-card {
    border-radius: 16px;
    border: none;

    .user-info {
      display: flex;
      align-items: center;
      gap: 16px;

      .user-avatar {
        flex-shrink: 0;
      }

      .user-detail {
        .user-name {
          font-size: 15px;
          font-weight: 500;
          color: #171717;
          margin: 0 0 4px;
        }

        .user-id {
          font-size: 12px;
          color: #9ca3af;
          margin: 0;
          font-family: monospace;
        }
      }
    }

    .openid {
      font-size: 13px;
      color: #6b7280;
      font-family: monospace;
    }

    .time {
      font-size: 13px;
      color: #9ca3af;
    }

    .amount {
      font-size: 14px;
      font-weight: 600;
      color: #D4AF37;
    }

    .pagination-wrapper {
      display: flex;
      justify-content: flex-end;
      padding: 20px 0 0;
    }
  }
}
</style>
