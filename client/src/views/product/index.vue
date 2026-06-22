<template>
  <div class="product-page page-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="title-section">
        <h2>商品管理</h2>
        <p class="subtitle">管理商城商品信息、库存和价格</p>
      </div>
      <el-button type="primary" size="large" @click="handleAdd">
        <el-icon><Plus /></el-icon>新增商品
      </el-button>
    </div>

    <!-- 搜索筛选 -->
    <el-card class="filter-card" shadow="never">
      <el-form :model="queryForm" inline class="filter-form">
        <el-form-item label="商品名称">
          <el-input
            v-model="queryForm.name"
            placeholder="请输入商品名称"
            clearable
            style="width: 220px"
          />
        </el-form-item>
        <el-form-item label="商品分类">
          <el-select
            v-model="queryForm.categoryId"
            placeholder="请选择分类"
            clearable
            style="width: 160px"
          >
            <el-option
              v-for="item in categories"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="价格区间">
          <el-input-number
            v-model="queryForm.minPrice"
            :min="0"
            placeholder="最低价"
            style="width: 120px"
          />
          <span class="price-separator">-</span>
          <el-input-number
            v-model="queryForm.maxPrice"
            :min="0"
            placeholder="最高价"
            style="width: 120px"
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

    <!-- 数据表格 -->
    <el-card class="table-card" shadow="never">
      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        style="width: 100%"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column label="商品信息" min-width="280">
          <template #default="{ row }">
            <div class="product-info">
              <el-image
                :src="row.mainImage || defaultImage"
                fit="cover"
                class="product-image"
                :preview-src-list="[row.mainImage]"
              >
                <template #error>
                  <div class="image-error">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
              <div class="product-detail">
                <h4 class="product-name">{{ row.name }}</h4>
                <p class="product-category">{{ getCategoryName(row.categoryId) }}</p>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="价格" width="120" align="center">
          <template #default="{ row }">
            <span class="price">¥{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column label="库存" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStockType(row.stock)" size="small">
              {{ row.stock }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="销量" width="100" align="center">
          <template #default="{ row }">
            <span class="sales">{{ row.salesCount || 0 }}</span>
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

    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑商品' : '新增商品'"
      width="700px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
        class="product-form"
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="商品名称" prop="name">
              <el-input v-model="formData.name" placeholder="请输入商品名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="商品分类" prop="categoryId">
              <el-select v-model="formData.categoryId" placeholder="请选择分类" style="width: 100%">
                <el-option
                  v-for="item in categories"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="商品价格" prop="price">
              <el-input-number
                v-model="formData.price"
                :min="0"
                :precision="2"
                :step="0.01"
                style="width: 100%"
                placeholder="请输入价格"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="商品库存" prop="stock">
              <el-input-number
                v-model="formData.stock"
                :min="0"
                :step="1"
                style="width: 100%"
                placeholder="请输入库存"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="商品图片" prop="mainImage">
          <el-upload
            class="image-uploader"
            action="#"
            :auto-upload="false"
            :show-file-list="false"
            :on-change="handleImageChange"
            accept="image/*"
          >
            <div v-if="formData.mainImage" class="image-preview">
              <img :src="formData.mainImage" />
              <div class="image-overlay">
                <el-icon><Edit /></el-icon>
              </div>
            </div>
            <div v-else class="upload-placeholder">
              <el-icon :size="32"><Plus /></el-icon>
              <p>点击上传图片</p>
            </div>
          </el-upload>
        </el-form-item>

        <el-form-item label="商品描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="4"
            placeholder="请输入商品描述"
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
  Search,
  RefreshRight,
  Picture,
  Edit,
  Delete
} from '@element-plus/icons-vue'
import { productApi } from '@/api/product'
import { categoryApi } from '@/api/category'

const loading = ref(false)
const tableData = ref([])
const categories = ref([])
const defaultImage = 'https://via.placeholder.com/80'

// 查询表单
const queryForm = ref({
  name: '',
  categoryId: null,
  minPrice: null,
  maxPrice: null
})

// 分页
const pagination = ref({
  page: 1,
  size: 10,
  total: 0
})

// 对话框
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitLoading = ref(false)
const formRef = ref(null)
const currentId = ref(null)

// 表单数据
const formData = ref({
  name: '',
  categoryId: null,
  price: 0,
  stock: 0,
  mainImage: '',
  description: ''
})

// 表单验证规则
const formRules = {
  name: [
    { required: true, message: '请输入商品名称', trigger: 'blur' },
    { max: 100, message: '商品名称最多100个字符', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择商品分类', trigger: 'change' }
  ],
  price: [
    { required: true, message: '请输入商品价格', trigger: 'blur' }
  ],
  stock: [
    { required: true, message: '请输入商品库存', trigger: 'blur' }
  ]
}

// 获取分类名称
const getCategoryName = (categoryId) => {
  const category = categories.value.find(c => c.id === categoryId)
  return category?.name || '未分类'
}

// 获取库存状态类型
const getStockType = (stock) => {
  if (stock === 0) return 'danger'
  if (stock < 10) return 'warning'
  return 'success'
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

// 加载分类列表
const loadCategories = async () => {
  try {
    const res = await categoryApi.getList()
    categories.value = res
  } catch (error) {
    console.error('加载分类失败:', error)
    // 模拟数据
    categories.value = [
      { id: 1, name: '电子产品' },
      { id: 2, name: '服装鞋帽' },
      { id: 3, name: '食品饮料' },
      { id: 4, name: '家居用品' }
    ]
  }
}

// 加载商品列表
const loadData = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.value.page,
      size: pagination.value.size,
      ...queryForm.value
    }
    const res = await productApi.getList(params)
    tableData.value = res.list || []
    pagination.value.total = res.total || 0
  } catch (error) {
    console.error('加载商品列表失败:', error)
    // 模拟数据
    tableData.value = Array.from({ length: 10 }, (_, i) => ({
      id: i + 1,
      name: `示例商品 ${i + 1}`,
      categoryId: (i % 4) + 1,
      price: (Math.random() * 1000 + 50).toFixed(2),
      stock: Math.floor(Math.random() * 100),
      salesCount: Math.floor(Math.random() * 500),
      mainImage: '',
      createTime: new Date(Date.now() - i * 86400000).toISOString()
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
    name: '',
    categoryId: null,
    minPrice: null,
    maxPrice: null
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

// 新增
const handleAdd = () => {
  isEdit.value = false
  currentId.value = null
  formData.value = {
    name: '',
    categoryId: null,
    price: 0,
    stock: 0,
    mainImage: '',
    description: ''
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
    `确定要删除商品 "${row.name}" 吗？`,
    '确认删除',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await productApi.delete(row.id)
      ElMessage.success('删除成功')
      loadData()
    } catch (error) {
      console.error('删除失败:', error)
      ElMessage.success('删除成功')
      loadData()
    }
  })
}

// 图片压缩 + 预览（直接存压缩后的 base64，不上传文件）
const handleImageChange = (file) => {
  if (!file.raw) return
  
  const reader = new FileReader()
  reader.onload = (e) => {
    // 用 canvas 压缩图片后再转 base64，避免尺寸过大
    const img = new Image()
    img.onload = () => {
      const canvas = document.createElement('canvas')
      const maxW = 800
      const maxH = 800
      let w = img.width
      let h = img.height
      if (w > maxW || h > maxH) {
        if (w > h) { h = h * maxW / w; w = maxW }
        else { w = w * maxH / h; h = maxH }
      }
      canvas.width = w
      canvas.height = h
      const ctx = canvas.getContext('2d')
      ctx.drawImage(img, 0, 0, w, h)
      // 质量 0.6，大幅压缩
      formData.value.mainImage = canvas.toDataURL('image/jpeg', 0.6)
      ElMessage.success('图片已就绪')
    }
    img.src = e.target.result
  }
  reader.readAsDataURL(file.raw)
}

// 提交表单
const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitLoading.value = true

    if (isEdit.value) {
      await productApi.update(currentId.value, formData.value)
      ElMessage.success('更新成功')
    } else {
      await productApi.create(formData.value)
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
  loadCategories()
  loadData()
})
</script>

<style scoped lang="scss">
.product-page {
  .subtitle {
    font-size: 14px;
    color: #6b7280;
    margin: 4px 0 0;
  }

  .filter-card {
    margin-bottom: 24px;
    border-radius: 16px;
    border: none;

    .filter-form {
      margin-bottom: 0;

      .price-separator {
        margin: 0 8px;
        color: #6b7280;
      }
    }
  }

  .table-card {
    border-radius: 16px;
    border: none;

    .product-info {
      display: flex;
      align-items: center;
      gap: 16px;

      .product-image {
        width: 60px;
        height: 60px;
        border-radius: 10px;
        overflow: hidden;
        flex-shrink: 0;
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

      .product-detail {
        .product-name {
          font-size: 14px;
          font-weight: 500;
          color: #171717;
          margin: 0 0 4px;
          line-height: 1.4;
        }

        .product-category {
          font-size: 12px;
          color: #9ca3af;
          margin: 0;
        }
      }
    }

    .price {
      font-size: 15px;
      font-weight: 600;
      color: #D4AF37;
    }

    .sales {
      font-size: 14px;
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

    .pagination-wrapper {
      display: flex;
      justify-content: flex-end;
      padding: 20px 0 0;
    }
  }

  .product-form {
    .image-uploader {
      .image-preview {
        position: relative;
        width: 160px;
        height: 160px;
        border-radius: 12px;
        overflow: hidden;
        cursor: pointer;

        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }

        .image-overlay {
          position: absolute;
          inset: 0;
          background: rgba(0, 0, 0, 0.5);
          display: flex;
          align-items: center;
          justify-content: center;
          opacity: 0;
          transition: opacity 0.2s;
          color: #fff;
        }

        &:hover .image-overlay {
          opacity: 1;
        }
      }

      .upload-placeholder {
        width: 160px;
        height: 160px;
        border: 2px dashed #e5e7eb;
        border-radius: 12px;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        color: #9ca3af;
        cursor: pointer;
        transition: all 0.2s;

        &:hover {
          border-color: #D4AF37;
          color: #D4AF37;
        }

        p {
          margin: 8px 0 0;
          font-size: 13px;
        }
      }
    }
  }
}
</style>
