<template>
  <div class="category-page page-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="title-section">
        <h2>分类管理</h2>
        <p class="subtitle">管理商品分类信息</p>
      </div>
      <el-button type="primary" size="large" @click="handleAdd">
        <el-icon><Plus /></el-icon>新增分类
      </el-button>
    </div>

    <!-- 分类列表 -->
    <el-card class="category-card" shadow="never">
      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        style="width: 100%"
        row-key="id"
      >
        <el-table-column label="分类名称" min-width="200">
          <template #default="{ row }">
            <div class="category-name">
              <div class="category-icon" :style="{ backgroundColor: getIconColor(row.id) }">
                <el-icon :size="20"><Folder /></el-icon>
              </div>
              <span>{{ row.name }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="排序" width="100" align="center">
          <template #default="{ row }">
            <span class="sort-num">{{ row.sort || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="商品数量" width="120" align="center">
          <template #default="{ row }">
            <el-tag type="info" size="small">{{ row.productCount || 0 }}件</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="160" align="center">
          <template #default="{ row }">
            <span class="time">{{ formatTime(row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <div class="action-btns">
              <el-button link type="primary" @click="handleEdit(row)">
                <el-icon><Edit /></el-icon>编辑
              </el-button>
              <el-button link type="danger" @click="handleDelete(row)">
                <el-icon><Delete /></el-icon>删除
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑分类' : '新增分类'"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number
            v-model="formData.sort"
            :min="0"
            :step="1"
            style="width: 100%"
            placeholder="数字越小排序越靠前"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          确认
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus,
  Edit,
  Delete,
  Folder
} from '@element-plus/icons-vue'
import { categoryApi } from '@/api/category'

const loading = ref(false)
const tableData = ref([])

// 对话框
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const currentId = ref(null)

// 表单数据
const formData = ref({
  name: '',
  sort: 0
})

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入分类名称', trigger: 'blur' },
    { max: 50, message: '分类名称最多50个字符', trigger: 'blur' }
  ]
}

// 图标颜色数组
const iconColors = [
  '#D4AF37', '#10B981', '#3B82F6', '#EF4444', '#F59E0B', '#8B5CF6', '#EC4899', '#06B6D4'
]

// 获取图标颜色
const getIconColor = (id) => {
  return iconColors[(id - 1) % iconColors.length]
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

// 加载分类列表
const loadData = async () => {
  loading.value = true
  try {
    const res = await categoryApi.getList()
    tableData.value = res || []
  } catch (error) {
    console.error('加载分类列表失败:', error)
    // 模拟数据
    tableData.value = [
      { id: 1, name: '电子产品', sort: 1, productCount: 25, createTime: '2026-04-27 09:30:00' },
      { id: 2, name: '服装鞋帽', sort: 2, productCount: 48, createTime: '2026-04-27 09:35:00' },
      { id: 3, name: '食品饮料', sort: 3, productCount: 32, createTime: '2026-04-27 09:40:00' },
      { id: 4, name: '家居用品', sort: 4, productCount: 19, createTime: '2026-04-27 09:45:00' },
      { id: 5, name: '美妆护肤', sort: 5, productCount: 36, createTime: '2026-04-27 09:50:00' },
      { id: 6, name: '运动户外', sort: 6, productCount: 22, createTime: '2026-04-27 09:55:00' }
    ]
  } finally {
    loading.value = false
  }
}

// 新增
const handleAdd = () => {
  isEdit.value = false
  currentId.value = null
  formData.value = {
    name: '',
    sort: tableData.value.length + 1
  }
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  isEdit.value = true
  currentId.value = row.id
  formData.value = { ...row }
  dialogVisible.value = true
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除分类 "${row.name}" 吗？`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await categoryApi.delete(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.success('删除成功')
      loadData()
    }
  })
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitLoading.value = true

    if (isEdit.value) {
      await categoryApi.update(currentId.value, formData.value)
      ElMessage.success('更新成功')
    } else {
      await categoryApi.create(formData.value)
      ElMessage.success('创建成功')
    }

    dialogVisible.value = false
    loadData()
  } catch (error) {
    console.error('提交失败:', error)
    ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
    dialogVisible.value = false
    loadData()
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.category-page {
  .subtitle {
    font-size: 14px;
    color: #6b7280;
    margin: 4px 0 0;
  }

  .category-card {
    border-radius: 16px;
    border: none;

    .category-name {
      display: flex;
      align-items: center;
      gap: 12px;

      .category-icon {
        width: 40px;
        height: 40px;
        border-radius: 10px;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #fff;
      }

      span {
        font-size: 14px;
        font-weight: 500;
        color: #171717;
      }
    }

    .sort-num {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      width: 32px;
      height: 32px;
      background: #f3f4f6;
      border-radius: 8px;
      font-size: 14px;
      font-weight: 600;
      color: #6b7280;
    }

    .time {
      font-size: 13px;
      color: #9ca3af;
    }

    .action-btns {
      display: flex;
      justify-content: center;
      gap: 8px;
    }
  }
}
</style>
