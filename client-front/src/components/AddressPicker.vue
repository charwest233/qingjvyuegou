<template>
  <el-dialog
    v-model="visible"
    title="选择收货地址"
    width="400px"
    :close-on-click-modal="false"
  >
    <div class="space-y-3">
      <div
        v-for="addr in addresses"
        :key="addr.id"
        class="p-4 border-2 rounded-xl cursor-pointer transition-all duration-200"
        :class="selectedId === addr.id
          ? 'border-primary bg-primary/5'
          : 'border-gray-100 hover:border-primary/30'"
        @click="selectedId = addr.id"
      >
        <div class="flex items-start justify-between">
          <div>
            <div class="flex items-center gap-2">
              <span class="font-medium text-gray-900">{{ addr.receiverName }}</span>
              <span class="text-sm text-gray-500">{{ maskPhone(addr.phone) }}</span>
              <span v-if="addr.isDefault" class="text-xs bg-primary/10 text-primary px-2 py-0.5 rounded">默认</span>
            </div>
            <p class="text-sm text-gray-600 mt-1">
              {{ addr.province }}{{ addr.city }}{{ addr.district }}{{ addr.detailAddress }}
            </p>
          </div>
          <div v-if="selectedId === addr.id" class="text-primary shrink-0">
            <CheckCircle class="w-5 h-5" />
          </div>
        </div>
      </div>

      <!-- 新增地址按钮 -->
      <button
        class="w-full py-3 border-2 border-dashed border-gray-200 rounded-xl text-gray-400
               hover:border-primary hover:text-primary transition-all cursor-pointer"
        @click="showAddDialog = true"
      >
        <Plus class="w-5 h-5 mx-auto" />
        <span class="text-sm mt-1">新增地址</span>
      </button>
    </div>

    <template #footer>
      <button class="btn-outline text-sm !px-6" @click="visible = false">取消</button>
      <button class="btn-primary text-sm !px-6" @click="handleConfirm">确认</button>
    </template>
  </el-dialog>

  <!-- 新增地址弹窗 -->
  <el-dialog v-model="showAddDialog" title="新增地址" width="400px">
    <el-form :model="newAddress" label-width="80px">
      <el-form-item label="收货人">
        <el-input v-model="newAddress.receiverName" placeholder="请输入收货人姓名" />
      </el-form-item>
      <el-form-item label="手机号">
        <el-input v-model="newAddress.phone" placeholder="请输入手机号" maxlength="11" />
      </el-form-item>
      <el-form-item label="所在地区">
        <el-cascader
          v-model="newAddress.region"
          :options="regionOptions"
          :props="{ value: 'label', label: 'label' }"
          placeholder="请选择省/市/区"
          class="w-full"
        />
      </el-form-item>
      <el-form-item label="详细地址">
        <el-input v-model="newAddress.detailAddress" type="textarea" :rows="2" placeholder="街道/门牌号" />
      </el-form-item>
      <el-form-item label="标签">
        <el-radio-group v-model="newAddress.label">
          <el-radio value="家">家</el-radio>
          <el-radio value="公司">公司</el-radio>
          <el-radio value="学校">学校</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item>
        <el-checkbox v-model="newAddress.isDefault">设为默认地址</el-checkbox>
      </el-form-item>
    </el-form>
    <template #footer>
      <button class="btn-outline text-sm !px-6" @click="showAddDialog = false">取消</button>
      <button class="btn-primary text-sm !px-6" @click="handleAdd">保存</button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import { Plus, CheckCircle } from 'lucide-vue-next'
import { ElMessage } from 'element-plus'
import { maskPhone } from '@/utils/format'
import { getUserAddresses, addUserAddress } from '@/api/user'
import { regionData } from 'element-china-area-data'

const props = defineProps({
  modelValue: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue', 'select'])

const visible = ref(false)
const showAddDialog = ref(false)
const addresses = ref([])
const selectedId = ref(null)
const newAddress = ref({
  receiverName: '',
  phone: '',
  region: [],
  detailAddress: '',
  label: '家',
  isDefault: false
})

// 使用 element-china-area-data 提供的完整全国省市区数据
const regionOptions = regionData

watch(() => props.modelValue, (val) => {
  visible.value = val
  if (val) {
    loadAddresses()
  }
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

async function loadAddresses() {
  try {
    const res = await getUserAddresses()
    if (res.code === 200) {
      addresses.value = res.data || []
      const defaultAddr = addresses.value.find(a => a.isDefault)
      if (defaultAddr) selectedId.value = defaultAddr.id
    }
  } catch (err) {
    console.error('加载地址失败:', err)
  }
}

function handleConfirm() {
  const addr = addresses.value.find(a => a.id === selectedId.value)
  if (addr) {
    emit('select', addr)
    visible.value = false
  }
}

async function handleAdd() {
  try {
    const addr = {
      receiverName: newAddress.value.receiverName,
      phone: newAddress.value.phone,
      province: newAddress.value.region[0] || '',
      city: newAddress.value.region[1] || '',
      district: newAddress.value.region[2] || '',
      detailAddress: newAddress.value.detailAddress,
      label: newAddress.value.label,
      isDefault: newAddress.value.isDefault ? 1 : 0
    }
    const res = await addUserAddress(addr)
    if (res.code === 200) {
      showAddDialog.value = false
      loadAddresses()
      ElMessage.success('地址添加成功')
    }
  } catch (err) {
    console.error('添加地址失败:', err)
  }
}
</script>
