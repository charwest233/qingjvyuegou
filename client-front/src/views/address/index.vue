<template>
  <div class="page-container py-4">
    <!-- 顶部标题 -->
    <div class="flex items-center justify-between mb-4">
      <div class="flex items-center gap-3">
        <button @click="$router.back()" class="p-2 -ml-2 hover:bg-gray-100 rounded-lg transition-colors cursor-pointer">
          <ArrowLeft class="w-5 h-5 text-gray-600" />
        </button>
        <h1 class="section-title !mb-0">收货地址</h1>
      </div>
      <button class="btn-primary !py-2 !px-4 !text-sm" @click="showAddDialog = true">
        <Plus class="w-4 h-4 inline-block mr-1" />新增
      </button>
    </div>

    <!-- 地址列表 -->
    <div v-if="loading" class="text-center py-20 text-gray-400">加载中...</div>
    <div v-else-if="addresses.length === 0" class="text-center py-20">
      <MapPin class="w-16 h-16 mx-auto text-gray-300 mb-4" />
      <p class="text-gray-400 mb-4">暂无收货地址</p>
      <button class="btn-primary inline-block" @click="showAddDialog = true">新增地址</button>
    </div>
    <div v-else class="space-y-3">
      <div v-for="addr in addresses" :key="addr.id"
        class="bg-white rounded-2xl p-4 shadow-sm"
      >
        <div class="flex items-start justify-between">
          <div class="flex-1">
            <div class="flex items-center gap-2 mb-1">
              <span class="font-medium text-gray-900">{{ addr.receiverName }}</span>
              <span class="text-sm text-gray-500">{{ addr.phone }}</span>
              <span v-if="addr.isDefault" class="text-xs bg-primary/10 text-primary px-2 py-0.5 rounded">默认</span>
            </div>
            <p class="text-sm text-gray-600 mb-3">
              {{ formatAddress(addr) }}
            </p>
            <div class="flex items-center gap-4">
              <button class="text-xs text-primary cursor-pointer hover:underline"
                :class="{ 'font-semibold': addr.isDefault }"
                @click="handleSetDefault(addr.id)">
                {{ addr.isDefault ? '已设为默认' : '设为默认' }}
              </button>
            </div>
          </div>
          <div class="flex gap-1 shrink-0">
            <button class="p-2 text-gray-400 hover:text-primary rounded-lg hover:bg-gray-50 cursor-pointer" @click="editAddress(addr)">
              <Pen class="w-4 h-4" />
            </button>
            <button class="p-2 text-gray-400 hover:text-functional-danger rounded-lg hover:bg-gray-50 cursor-pointer" @click="handleDelete(addr.id)">
              <Trash2 class="w-4 h-4" />
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- 新增/编辑对话框 -->
    <el-dialog v-model="showAddDialog" :title="editingAddress ? '编辑地址' : '新增地址'" width="92%" top="5vh" destroy-on-close>
      <el-form :model="addressForm" label-position="top" size="large">
        <el-form-item label="收货人" required>
          <el-input v-model="addressForm.receiverName" placeholder="请输入收货人姓名" />
        </el-form-item>
        <el-form-item label="手机号" required>
          <div class="flex gap-2 w-full">
            <el-select v-model="phoneType" class="!w-36" @change="onPhoneTypeChange">
              <el-option label="注册手机号" value="registered" />
              <el-option label="其他手机号" value="other" />
            </el-select>
            <el-input
              v-if="phoneType === 'other'"
              v-model="addressForm.phone"
              placeholder="请输入手机号" maxlength="11"
            />
            <el-input
              v-else
              :model-value="registeredPhone"
              disabled
              placeholder="加载中..."
              class="flex-1"
            />
          </div>
        </el-form-item>
        <el-form-item label="所在地区" required>
          <el-cascader
            v-model="selectedArea"
            :options="regionData"
            :props="{ value: 'label', label: 'label' }"
            placeholder="选择省/市/区"
            size="large"
            style="width: 100%"
            clearable
            @change="onAreaChange"
          />
        </el-form-item>
        <el-form-item label="详细地址" required>
          <el-input v-model="addressForm.detailAddress" placeholder="街道、门牌号等" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="addressForm.label" placeholder="如：家、公司、学校" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ArrowLeft, Plus, MapPin, Pen, Trash2 } from 'lucide-vue-next'
import { ElMessage } from 'element-plus'
import { regionData } from 'element-china-area-data'
import { getUserAddresses, addUserAddress, updateUserAddress, deleteUserAddress } from '@/api/user'
import { setDefaultAddress } from '@/api/user'
import { getUser } from '@/utils/auth'

const addresses = ref([])
const loading = ref(true)
const showAddDialog = ref(false)
const saving = ref(false)
const editingAddress = ref(null)

const userInfo = computed(() => getUser())
const registeredPhone = computed(() => userInfo.value?.phone || '')
const phoneType = ref('registered')

const defaultForm = () => ({
  receiverName: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detailAddress: '',
  label: ''
})

const addressForm = ref(defaultForm())
const selectedArea = ref([])

function formatAddress(addr) {
  const parts = []
  if (addr.province) parts.push(addr.province)
  if (addr.city && addr.city !== addr.province) parts.push(addr.city)
  if (addr.district) parts.push(addr.district)
  if (addr.detailAddress) parts.push(addr.detailAddress)
  return parts.join('')
}

function onAreaChange(value) {
  addressForm.value.province = value[0] || ''
  addressForm.value.city = value[1] || ''
  addressForm.value.district = value[2] || ''
}

async function loadAddresses() {
  loading.value = true
  try {
    const res = await getUserAddresses()
    if (res.code === 200) {
      addresses.value = res.data || []
    }
  } catch (err) {
    console.error('加载地址失败:', err)
  } finally {
    loading.value = false
  }
}

function onPhoneTypeChange(type) {
  if (type === 'registered') {
    addressForm.value.phone = registeredPhone.value
  } else {
    addressForm.value.phone = ''
  }
}

function editAddress(addr) {
  editingAddress.value = addr
  const isRegistered = addr.phone === registeredPhone.value
  phoneType.value = isRegistered ? 'registered' : 'other'
  addressForm.value = {
    receiverName: addr.receiverName || '',
    phone: isRegistered ? registeredPhone.value : (addr.phone || ''),
    province: addr.province || '',
    city: addr.city || '',
    district: addr.district || '',
    detailAddress: addr.detailAddress || '',
    label: addr.label || ''
  }
  selectedArea.value = [addr.province || '', addr.city || '', addr.district || ''].filter(Boolean)
  showAddDialog.value = true
}

async function handleSave() {
  const form = addressForm.value
  // 如果是选择注册手机号，自动填入
  if (phoneType.value === 'registered') {
    form.phone = registeredPhone.value
  }
  if (!form.receiverName || !form.phone || !form.detailAddress) {
    ElMessage.warning('请填写完整的地址信息')
    return
  }
  saving.value = true
  try {
    if (editingAddress.value) {
      await updateUserAddress(editingAddress.value.id, form)
      ElMessage.success('地址已更新')
    } else {
      await addUserAddress(form)
      ElMessage.success('地址已添加')
    }
    showAddDialog.value = false
    editingAddress.value = null
    phoneType.value = 'registered'
    selectedArea.value = []
    addressForm.value = defaultForm()
    await loadAddresses()
  } catch (err) {
    console.error('保存地址失败:', err)
  } finally {
    saving.value = false
  }
}

async function handleSetDefault(id) {
  try {
    await setDefaultAddress(id)
    ElMessage.success('已设为默认地址')
    await loadAddresses()
  } catch (err) {
    console.error('设置默认地址失败:', err)
  }
}

async function handleDelete(id) {
  try {
    await deleteUserAddress(id)
    ElMessage.success('地址已删除')
    await loadAddresses()
  } catch (err) {
    console.error('删除地址失败:', err)
  }
}

onMounted(() => {
  loadAddresses()
})
</script>
