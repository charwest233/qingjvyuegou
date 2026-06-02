<template>
  <div class="dashboard-page">
    <!-- 统计卡片 -->
    <div class="stats-row">
      <el-row :gutter="24">
        <el-col :xs="24" :sm="12" :lg="6">
          <div class="stat-card">
            <div class="stat-icon bg-primary">
              <el-icon :size="24"><ShoppingCart /></el-icon>
            </div>
            <div class="stat-info">
              <p class="stat-label">今日订单</p>
              <h3 class="stat-value">{{ statistics.todayOrders || 0 }}</h3>
              <p class="stat-trend" :class="{ up: statistics.orderTrend > 0 }">
                <el-icon><Top v-if="statistics.orderTrend > 0" /><Bottom v-else /></el-icon>
                {{ Math.abs(statistics.orderTrend || 0) }}%
                <span>较昨日</span>
              </p>
            </div>
          </div>
        </el-col>
        
        <el-col :xs="24" :sm="12" :lg="6">
          <div class="stat-card">
            <div class="stat-icon bg-success">
              <el-icon :size="24"><Money /></el-icon>
            </div>
            <div class="stat-info">
              <p class="stat-label">今日销售额</p>
              <h3 class="stat-value">¥{{ formatNumber(statistics.todaySales || 0) }}</h3>
              <p class="stat-trend" :class="{ up: statistics.salesTrend > 0 }">
                <el-icon><Top v-if="statistics.salesTrend > 0" /><Bottom v-else /></el-icon>
                {{ Math.abs(statistics.salesTrend || 0) }}%
                <span>较昨日</span>
              </p>
            </div>
          </div>
        </el-col>
        
        <el-col :xs="24" :sm="12" :lg="6">
          <div class="stat-card">
            <div class="stat-icon bg-warning">
              <el-icon :size="24"><User /></el-icon>
            </div>
            <div class="stat-info">
              <p class="stat-label">新增用户</p>
              <h3 class="stat-value">{{ statistics.newUsers || 0 }}</h3>
              <p class="stat-trend" :class="{ up: statistics.userTrend > 0 }">
                <el-icon><Top v-if="statistics.userTrend > 0" /><Bottom v-else /></el-icon>
                {{ Math.abs(statistics.userTrend || 0) }}%
                <span>较昨日</span>
              </p>
            </div>
          </div>
        </el-col>
        
        <el-col :xs="24" :sm="12" :lg="6">
          <div class="stat-card">
            <div class="stat-icon bg-danger">
              <el-icon :size="24"><Goods /></el-icon>
            </div>
            <div class="stat-info">
              <p class="stat-label">商品总数</p>
              <h3 class="stat-value">{{ statistics.totalProducts || 0 }}</h3>
              <p class="stat-trend" :class="{ up: statistics.productTrend > 0 }">
                <el-icon><Top v-if="statistics.productTrend > 0" /><Bottom v-else /></el-icon>
                {{ Math.abs(statistics.productTrend || 0) }}%
                <span>较上月</span>
              </p>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 图表区域 -->
    <div class="charts-row">
      <el-row :gutter="24">
        <el-col :lg="16">
          <el-card class="chart-card">
            <template #header>
              <div class="card-header">
                <h4>销售趋势</h4>
                <el-radio-group v-model="salesDays" size="small" @change="loadSalesTrend">
                  <el-radio-button :value="7">近7天</el-radio-button>
                  <el-radio-button :value="30">近30天</el-radio-button>
                </el-radio-group>
              </div>
            </template>
            <div ref="salesChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
        
        <el-col :lg="8">
          <el-card class="chart-card">
            <template #header>
              <div class="card-header">
                <h4>订单状态分布</h4>
              </div>
            </template>
            <div ref="orderChartRef" class="chart-container pie-chart"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 数据表格区域 -->
    <div class="tables-row">
      <el-row :gutter="24">
        <el-col :lg="12">
          <el-card class="table-card">
            <template #header>
              <div class="card-header">
                <h4>热门商品 TOP10</h4>
                <el-link type="primary" @click="$router.push('/products')">查看更多</el-link>
              </div>
            </template>
            <el-table :data="hotProducts" style="width: 100%" :show-header="false">
              <el-table-column width="72" class-name="rank-col">
                <template #default="{ $index }">
                  <span class="rank-number" :class="{ top: $index < 3 }">{{ $index + 1 }}</span>
                </template>
              </el-table-column>
              <el-table-column>
                <template #default="{ row }">
                  <div class="product-cell">
                    <el-image :src="row.mainImage || defaultImage" fit="cover" class="product-thumb">
                      <template #error>
                        <div class="image-error">
                          <el-icon><Picture /></el-icon>
                        </div>
                      </template>
                    </el-image>
                    <span class="product-name">{{ row.name }}</span>
                  </div>
                </template>
              </el-table-column>
              <el-table-column align="right">
                <template #default="{ row }">
                  <span class="sales-count">销量 {{ row.salesCount || 0 }}</span>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
        
        <el-col :lg="12">
          <el-card class="table-card">
            <template #header>
              <div class="card-header">
                <h4>最新订单</h4>
                <el-link type="primary" @click="$router.push('/orders')">查看更多</el-link>
              </div>
            </template>
            <el-table :data="recentOrders" style="width: 100%">
              <el-table-column label="订单号" prop="orderNo" min-width="140">
                <template #default="{ row }">
                  <span class="order-no">{{ row.orderNo }}</span>
                </template>
              </el-table-column>
              <el-table-column label="金额" prop="totalAmount" width="100">
                <template #default="{ row }">
                  <span class="amount">¥{{ row.totalAmount }}</span>
                </template>
              </el-table-column>
              <el-table-column label="状态" prop="status" width="90">
                <template #default="{ row }">
                  <el-tag :type="getOrderStatusType(row.status)" size="small">
                    {{ getOrderStatusText(row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="时间" prop="createTime" width="150">
                <template #default="{ row }">
                  <span class="time">{{ formatTime(row.createTime) }}</span>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import {
  ShoppingCart,
  Money,
  User,
  Goods,
  Top,
  Bottom,
  Picture
} from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { dashboardApi } from '@/api/dashboard'

// 统计数据
const statistics = ref({})
const salesDays = ref(7)
const hotProducts = ref([])
const recentOrders = ref([])
const defaultImage = 'https://via.placeholder.com/40'

// 图表引用
const salesChartRef = ref(null)
const orderChartRef = ref(null)
let salesChart = null
let orderChart = null

// 格式化数字
const formatNumber = (num) => {
  return num.toLocaleString('zh-CN')
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 获取订单状态类型
const getOrderStatusType = (status) => {
  const types = {
    0: 'warning',
    1: 'success',
    2: 'primary',
    3: 'info'
  }
  return types[status] || 'info'
}

// 获取订单状态文本
const getOrderStatusText = (status) => {
  const texts = {
    0: '待支付',
    1: '已支付',
    2: '已发货',
    3: '已完成'
  }
  return texts[status] || '未知'
}

const buildOrderStatusChartData = (orderStats = {}) => {
  return [
    { value: Number(orderStats.pending || 0), name: '待支付', itemStyle: { color: '#f59e0b' } },
    { value: Number(orderStats.paid || 0), name: '已支付', itemStyle: { color: '#10b981' } },
    { value: Number(orderStats.shipped || 0), name: '已发货', itemStyle: { color: '#3b82f6' } },
    { value: Number(orderStats.completed || 0), name: '已完成', itemStyle: { color: '#6b7280' } }
  ]
}

// 初始化销售趋势图表
const initSalesChart = (data) => {
  if (!salesChartRef.value) return
  
  salesChart = echarts.init(salesChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: function(params) {
        return `${params[0].name}<br/>
                销售额: ¥${params[0].value.toLocaleString()}<br/>
                订单数: ${params[1].value}笔`
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: data.dates || [],
      axisLine: {
        lineStyle: { color: '#e5e7eb' }
      },
      axisLabel: {
        color: '#6b7280'
      }
    },
    yAxis: [
      {
        type: 'value',
        name: '销售额',
        axisLine: { show: false },
        axisTick: { show: false },
        axisLabel: {
          color: '#6b7280',
          formatter: (value) => value >= 1000 ? (value / 1000) + 'k' : value
        },
        splitLine: {
          lineStyle: { color: '#f3f4f6' }
        }
      },
      {
        type: 'value',
        name: '订单数',
        axisLine: { show: false },
        axisTick: { show: false },
        axisLabel: {
          color: '#6b7280'
        },
        splitLine: { show: false }
      }
    ],
    series: [
      {
        name: '销售额',
        type: 'bar',
        data: data.sales || [],
        itemStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#14B8A6' },
            { offset: 1, color: '#5EEAD4' }
          ]),
          borderRadius: [6, 6, 0, 0]
        },
        barWidth: '40%'
      },
      {
        name: '订单数',
        type: 'line',
        yAxisIndex: 1,
        data: data.orders || [],
        smooth: true,
        symbol: 'circle',
        symbolSize: 8,
        lineStyle: {
          color: '#0D9488',
          width: 2
        },
        itemStyle: {
          color: '#0D9488',
          borderColor: '#fff',
          borderWidth: 2
        }
      }
    ]
  }
  
  salesChart.setOption(option)
}

// 初始化订单状态饼图
const initOrderChart = (data) => {
  if (!orderChartRef.value) return
  
  orderChart = echarts.init(orderChartRef.value)
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: '5%',
      top: 'center',
      textStyle: {
        color: '#6b7280'
      }
    },
    series: [
      {
        type: 'pie',
        radius: ['45%', '70%'],
        center: ['35%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 8,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 14,
            fontWeight: 'bold'
          }
        },
        data: data || [
          { value: 0, name: '待支付', itemStyle: { color: '#f59e0b' } },
          { value: 0, name: '已支付', itemStyle: { color: '#10b981' } },
          { value: 0, name: '已发货', itemStyle: { color: '#3b82f6' } },
          { value: 0, name: '已完成', itemStyle: { color: '#6b7280' } }
        ]
      }
    ]
  }
  
  orderChart.setOption(option)
}

// 加载统计数据
const loadStatistics = async () => {
  try {
    const res = await dashboardApi.getStatistics()
    statistics.value = res
    nextTick(() => {
      initOrderChart(buildOrderStatusChartData(res?.orderStats || {}))
    })
  } catch (error) {
    console.error('加载统计数据失败:', error)
    // 接口失败时显示空态，避免使用随机模拟数据误导业务判断
    statistics.value = {
      todayOrders: 0,
      orderTrend: 0,
      todaySales: 0,
      salesTrend: 0,
      newUsers: 0,
      userTrend: 0,
      totalProducts: 0,
      productTrend: 0,
      orderStats: { pending: 0, paid: 0, shipped: 0, completed: 0 }
    }
    nextTick(() => {
      initOrderChart(buildOrderStatusChartData(statistics.value.orderStats))
    })
  }
}

// 加载销售趋势
const loadSalesTrend = async () => {
  try {
    const res = await dashboardApi.getSalesTrend(salesDays.value)
    nextTick(() => {
      initSalesChart(res)
    })
  } catch (error) {
    console.error('加载销售趋势失败:', error)
    // 保留横轴日期，数值置 0，避免随机数据
    const dates = Array.from({ length: salesDays.value }, (_, i) => {
      const d = new Date()
      d.setDate(d.getDate() - (salesDays.value - 1 - i))
      return `${d.getMonth() + 1}/${d.getDate()}`
    })
    const sales = dates.map(() => 0)
    const orders = dates.map(() => 0)
    
    nextTick(() => {
      initSalesChart({ dates, sales, orders })
    })
  }
}

// 加载热门商品
const loadHotProducts = async () => {
  try {
    const res = await dashboardApi.getHotProducts(10)
    hotProducts.value = res
  } catch (error) {
    console.error('加载热门商品失败:', error)
    hotProducts.value = []
  }
}

// 加载最新订单
const loadRecentOrders = async () => {
  try {
    const res = await dashboardApi.getRecentOrders(10)
    recentOrders.value = res
  } catch (error) {
    console.error('加载最新订单失败:', error)
    recentOrders.value = []
  }
}

// 监听窗口大小变化
const handleResize = () => {
  salesChart?.resize()
  orderChart?.resize()
}

onMounted(() => {
  loadStatistics()
  loadSalesTrend()
  loadHotProducts()
  loadRecentOrders()

  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  salesChart?.dispose()
  orderChart?.dispose()
})
</script>

<style scoped lang="scss">
.dashboard-page {
  padding: 24px;
  max-width: 1600px;
  margin: 0 auto;
}

.stats-row {
  margin-bottom: 24px;

  .stat-card {
    background: #fff;
    border-radius: 16px;
    padding: 24px;
    display: flex;
    align-items: center;
    gap: 16px;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
    transition: transform 0.2s, box-shadow 0.2s;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    }

    .stat-icon {
      width: 56px;
      height: 56px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      flex-shrink: 0;

      &.bg-primary {
        background: linear-gradient(135deg, #14B8A6 0%, #5EEAD4 100%);
      }

      &.bg-success {
        background: linear-gradient(135deg, #10b981 0%, #34d399 100%);
      }

      &.bg-warning {
        background: linear-gradient(135deg, #f59e0b 0%, #fbbf24 100%);
      }

      &.bg-danger {
        background: linear-gradient(135deg, #ef4444 0%, #f87171 100%);
      }
    }

    .stat-info {
      flex: 1;

      .stat-label {
        font-size: 14px;
        color: #6b7280;
        margin: 0 0 8px;
      }

      .stat-value {
        font-size: 28px;
        font-weight: 700;
        color: #171717;
        margin: 0 0 8px;
        line-height: 1;
      }

      .stat-trend {
        font-size: 13px;
        color: #ef4444;
        margin: 0;
        display: flex;
        align-items: center;
        gap: 4px;

        &.up {
          color: #10b981;
        }

        span {
          color: #9ca3af;
          margin-left: 4px;
        }
      }
    }
  }
}

.charts-row {
  margin-bottom: 24px;

  .chart-card {
    border-radius: 16px;
    border: none;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);

    :deep(.el-card__header) {
      padding: 20px 24px;
      border-bottom: 1px solid #f3f4f6;
    }

    :deep(.el-card__body) {
      padding: 20px 24px;
    }

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      h4 {
        font-size: 18px;
        font-weight: 600;
        color: #171717;
        margin: 0;
      }
    }

    .chart-container {
      height: 320px;

      &.pie-chart {
        height: 280px;
      }
    }
  }
}

.tables-row {
  .table-card {
    border-radius: 16px;
    border: none;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);

    :deep(.el-card__header) {
      padding: 20px 24px;
      border-bottom: 1px solid #f3f4f6;
    }

    :deep(.el-card__body) {
      padding: 0;
    }

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;

      h4 {
        font-size: 18px;
        font-weight: 600;
        color: #171717;
        margin: 0;
      }
    }

    .rank-number {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      width: 28px;
      height: 28px;
      border-radius: 6px;
      background: #f3f4f6;
      color: #6b7280;
      font-size: 14px;
      font-weight: 600;

      &.top {
        background: linear-gradient(135deg, #14B8A6 0%, #5EEAD4 100%);
        color: #fff;
      }
    }

    .product-cell {
      display: flex;
      align-items: center;
      gap: 12px;

      .product-thumb {
        width: 40px;
        height: 40px;
        border-radius: 8px;
        overflow: hidden;
        background: #f3f4f6;

        .image-error {
          width: 100%;
          height: 100%;
          display: flex;
          align-items: center;
          justify-content: center;
          color: #d1d5db;
        }
      }

      .product-name {
        font-size: 14px;
        color: #374151;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }

    .sales-count {
      font-size: 13px;
      color: #6b7280;
      font-weight: 500;
    }

    .order-no {
      font-size: 13px;
      color: #374151;
      font-family: monospace;
    }

    .amount {
      font-size: 14px;
      color: #14B8A6;
      font-weight: 600;
    }

    .time {
      font-size: 13px;
      color: #9ca3af;
    }

    :deep(.el-table) {
      .el-table__cell {
        padding: 16px 24px;
      }

      .rank-col.el-table__cell .cell {
        padding-left: 12px;
        padding-right: 12px;
      }
    }
  }
}
</style>
