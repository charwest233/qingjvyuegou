<template>
  <div class="page-container py-4">
    <h1 class="section-title">确认订单</h1>

    <!-- 收货地址 -->
    <section class="bg-white rounded-2xl p-4 shadow-sm mb-4">
      <div class="flex items-center justify-between mb-3">
        <h3 class="text-sm font-medium text-gray-700">收货地址</h3>
        <button
          class="text-sm text-primary hover:text-primary-dark transition-colors cursor-pointer"
          @click="showAddressPicker = true"
        >
          {{ selectedAddress ? '切换' : '选择地址' }}
        </button>
      </div>
      <div v-if="selectedAddress" class="flex items-start gap-3">
        <MapPin class="w-5 h-5 text-primary mt-0.5 shrink-0" />
        <div>
          <div class="flex items-center gap-2">
            <span class="font-medium text-gray-900">{{ selectedAddress.receiverName }}</span>
            <span class="text-sm text-gray-500">{{ selectedAddress.phone }}</span>
            <span v-if="selectedAddress.isDefault" class="text-xs bg-primary/10 text-primary px-2 py-0.5 rounded">默认</span>
          </div>
          <p class="text-sm text-gray-600 mt-1">
            {{ selectedAddress.province }}{{ selectedAddress.city }}{{ selectedAddress.district }}{{ selectedAddress.detailAddress }}
          </p>
        </div>
      </div>
      <div v-else class="text-sm text-gray-400 py-2">请选择收货地址</div>
    </section>

    <!-- 商品清单 -->
    <section class="bg-white rounded-2xl p-4 shadow-sm mb-4">
      <h3 class="text-sm font-medium text-gray-700 mb-3">商品清单</h3>
      <div v-for="item in cartItems" :key="item.productId || item.product_id" class="flex items-center gap-3 py-2">
        <div class="w-16 h-16 bg-gray-50 rounded-lg overflow-hidden shrink-0">
          <img
            v-if="item.productImage"
            :src="item.productImage"
            :alt="item.productName"
            class="w-full h-full object-cover"
          />
          <div v-else class="w-full h-full flex items-center justify-center text-gray-300">
            <Package class="w-6 h-6" />
          </div>
        </div>
        <div class="flex-1 min-w-0">
          <p class="text-sm text-gray-900 line-clamp-1">{{ item.productName }}</p>
          <p class="text-xs text-gray-400 mt-1">x{{ item.quantity }}</p>
        </div>
        <p class="text-sm font-medium text-gray-900">¥{{ formatPrice(item.price * item.quantity) }}</p>
      </div>
    </section>

    <!-- 订单汇总 -->
    <section class="bg-white rounded-2xl p-4 shadow-sm mb-4">
      <div class="space-y-2 text-sm">
        <div class="flex justify-between text-gray-500">
          <span>商品总额</span>
          <span>¥{{ formatPrice(totalAmount) }}</span>
        </div>
        <div class="flex justify-between text-gray-500">
          <span>运费</span>
          <span class="text-functional-success">免运费</span>
        </div>
        <div class="flex justify-between text-base font-bold pt-2 border-t border-gray-100">
          <span>实付金额</span>
          <span class="text-functional-danger text-xl">¥{{ formatPrice(totalAmount) }}</span>
        </div>
      </div>
    </section>

    <!-- 提交订单 -->
    <div class="fixed bottom-16 left-0 right-0 bg-white border-t border-gray-100 p-4 z-40">
      <div class="max-w-7xl mx-auto">
        <button
          class="w-full btn-cta !py-3 !text-base"
          :class="!canSubmit ? 'opacity-50 cursor-not-allowed' : ''"
          :disabled="!canSubmit"
          @click="submitOrder"
        >
          提交订单
        </button>
      </div>
    </div>
    <div class="h-20" />

    <!-- 地址选择弹窗 -->
    <AddressPicker
      v-model="showAddressPicker"
      @select="onAddressSelect"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { MapPin, Package } from 'lucide-vue-next'
import { ElMessage } from 'element-plus'
import AddressPicker from '@/components/AddressPicker.vue'
import { useCartStore } from '@/stores/cart'
import { createOrder } from '@/api/order'
import { getUserAddresses } from '@/api/user'
import { formatPrice } from '@/utils/format'

const router = useRouter()
const cartStore = useCartStore()

const showAddressPicker = ref(false)
const selectedAddress = ref(null)
const submitting = ref(false)

const cartItems = computed(() => {
  return cartStore.items.filter(item => item.selected === 1)
})

const totalAmount = computed(() => cartStore.selectedTotal)

const canSubmit = computed(() => {
  return selectedAddress.value && cartItems.value.length > 0 && !submitting.value
})

function onAddressSelect(addr) {
  selectedAddress.value = addr
}

async function submitOrder() {
  if (!canSubmit.value) return

  submitting.value = true
  try {
    const orderData = {
      addressId: selectedAddress.value.id,
      items: cartItems.value.map(item => ({
        productId: item.productId || item.product_id,
        quantity: item.quantity
      }))
    }
    const res = await createOrder(orderData)
    if (res.code === 200) {
      ElMessage.success('订单提交成功')
      // 清空已购买的购物车项
      await cartStore.loadCart()
      router.push({ name: 'Orders' })
    }
  } catch (err) {
    console.error('提交订单失败:', err)
  } finally {
    submitting.value = false
  }
}

async function loadDefaultAddress() {
  try {
    const res = await getUserAddresses()
    if (res.code === 200 && res.data?.length) {
      const defaultAddr = res.data.find(a => a.isDefault === 1)
      if (defaultAddr) {
        selectedAddress.value = defaultAddr
      }
      // 若没有默认地址，selectedAddress 保持 null，显示"请选择收货地址"
    }
  } catch (err) {
    console.error('加载默认地址失败:', err)
  }
}

onMounted(() => {
  if (cartStore.items.length === 0) {
    cartStore.loadCart()
  }
  // 自动加载默认地址（若有则自动填充，没有就显示"请选择收货地址"）
  loadDefaultAddress()
})
</script>
