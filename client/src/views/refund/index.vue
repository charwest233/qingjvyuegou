<template>
  <div class="refund-page page-container">
    <div class="page-header">
      <div class="title-section">
        <h2>售后管理</h2>
        <p class="subtitle">处理用户退款/退货申请</p>
      </div>
    </div>

    <el-card class="filter-card" shadow="never">
      <div class="flex items-center gap-3">
        <span class="text-sm text-gray-600">状态筛选：</span>
        <el-radio-group v-model="statusFilter" @change="loadData">
          <el-radio-button label="">全部</el-radio-button>
          <el-radio-button label="0">待审核</el-radio-button>
          <el-radio-button label="2">退货中</el-radio-button>
          <el-radio-button label="3">待退款</el-radio-button>
          <el-radio-button label="4">已退款</el-radio-button>
          <el-radio-button label="5">已驳回</el-radio-button>
        </el-radio-group>
      </div>
    </el-card>

    <el-card class="refund-list-card" shadow="never">
      <el-table v-loading="loading" :data="tableData" stripe style="width: 100%">
        <el-table-column label="售后单号" width="180">
          <template #default="{ row }">
            <span class="font-mono text-xs">{{ row.refundNo }}</span>
          </template>
        </el-table-column>
        <el-table-column label="商品" min-width="200">
          <template #default="{ row }">
            <div class="flex items-center gap-2">
              <div class="prod-img shrink-0">
                <img v-if="row.productImage" :src="row.productImage" />
                <span v-else class="no-img">无图</span>
              </div>
              <div class="text-xs text-gray-700 line-clamp-2">{{ row.productName || '整单退款' }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="退款金额" width="110" align="center">
          <template #default="{ row }">¥{{ row.refundAmount?.toFixed(2) }}</template>
        </el-table-column>
        <el-table-column label="售后类型" width="90" align="center">
          <template #default="{ row }">{{ row.type === 1 ? '仅退款' : '退货退款' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="申请时间" width="150" align="center">
          <template #default="{ row }">{{ formatTime(row.applyTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <div class="action-btns">
              <!-- 仅退款待审核 → 通过/驳回 -->
              <template v-if="row.status === 0 && row.type === 1">
                <el-button link type="success" @click="handleAudit(row, true)">通过</el-button>
                <el-button link type="danger" @click="handleAudit(row, false)">驳回</el-button>
              </template>
              <!-- 退货中 → 确认收货 -->
              <el-button v-if="row.status === 2" link type="primary" @click="handleConfirm(row)">确认收货</el-button>
              <!-- 待退款 → 审核通过/驳回 -->
              <template v-if="row.status === 3">
                <el-button link type="success" @click="handleAudit(row, true)">通过</el-button>
                <el-button link type="danger" @click="handleAudit(row, false)">驳回</el-button>
              </template>
              <el-button link type="info" @click="handleDetail(row)">详情</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="auditVisible" :title="auditApproved ? '审核通过' : '审核驳回'" width="450px">
      <el-input v-if="!auditApproved" v-model="auditRemark" type="textarea" :rows="3" placeholder="请输入驳回原因（选填）" />
      <p v-else class="text-sm text-gray-600">确认{{ auditApproved ? '通过' : '驳回' }}该售后申请？</p>
      <template #footer>
        <el-button @click="auditVisible = false">取消</el-button>
        <el-button type="primary" :loading="auditLoading" @click="submitAudit">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { refundAdminApi } from '@/api/refund'

const loading = ref(false)
const tableData = ref([])
const statusFilter = ref('')
const auditVisible = ref(false)
const auditLoading = ref(false)
const auditApproved = ref(false)
const auditRemark = ref('')
const currentRefund = ref(null)

function statusText(status) {
  return ({ 0: '待审核', 1: '待填物流', 2: '退货中', 3: '待退款', 4: '已退款', 5: '已驳回', 6: '已撤销' })[status] || '未知'
}

function statusTagType(status) {
  return ({ 0: 'warning', 1: '', 2: 'primary', 3: 'danger', 4: 'success', 5: 'info', 6: 'info' })[status] || 'info'
}

function formatTime(t) {
  if (!t) return ''
  const d = new Date(t)
  return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')} ${String(d.getHours()).padStart(2,'0')}:${String(d.getMinutes()).padStart(2,'0')}`
}

async function loadData() {
  loading.value = true
  try {
    const res = await refundAdminApi.getList(statusFilter.value || null)
    if (Array.isArray(res)) tableData.value = res
  } catch { tableData.value = [] }
  finally { loading.value = false }
}

function handleAudit(row, approved) {
  currentRefund.value = row
  auditApproved.value = approved
  auditRemark.value = ''
  auditVisible.value = true
}

async function submitAudit() {
  if (!currentRefund.value) return
  auditLoading.value = true
  try {
    await refundAdminApi.audit(currentRefund.value.id, auditApproved.value, auditRemark.value)
    ElMessage.success(auditApproved.value ? '审核通过' : '已驳回')
    auditVisible.value = false
    loadData()
  } catch { ElMessage.error('操作失败') }
  finally { auditLoading.value = false }
}

async function handleConfirm(row) {
  try {
    await ElMessageBox.confirm('确认收到退货？确认后进入待退款审核。', '提示', { confirmButtonText: '确认', cancelButtonText: '取消', type: 'warning' })
    await refundAdminApi.confirmReceipt(row.id)
    ElMessage.success('已确认收货')
    loadData()
  } catch { /* ignore */ }
}

function handleDetail(row) {
  ElMessage.info(`售后单 ${row.refundNo} — ${statusText(row.status)}`)
}

onMounted(() => loadData())
</script>

<style scoped>
.refund-page { padding: 20px; }
.page-header { margin-bottom: 20px; }
.page-header h2 { font-size: 20px; font-weight: 600; color: #1e293b; margin: 0; }
.subtitle { font-size: 13px; color: #94a3b8; margin-top: 4px; }
.filter-card { margin-bottom: 16px; }
.action-btns { display: flex; gap: 4px; justify-content: center; }
.prod-img { width: 44px; height: 44px; border-radius: 6px; overflow: hidden; background: #f3f4f6; display: inline-flex; align-items: center; justify-content: center; }
.prod-img img { width: 100%; height: 100%; object-fit: cover; display: block; }
.prod-img .no-img { font-size: 11px; color: #9ca3af; }
</style>
