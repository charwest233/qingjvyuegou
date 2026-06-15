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

    <!-- 优惠券 -->
    <section class="bg-white rounded-2xl p-4 shadow-sm mb-4">
      <div class="flex items-center justify-between mb-2">
        <h3 class="text-sm font-medium text-gray-700">优惠券</h3>
        <button v-if="couponList.length > 0"
          class="text-xs text-primary hover:text-primary-dark transition-colors cursor-pointer"
          @click="showCouponPicker = !showCouponPicker"
        >{{ selectedCoupon ? '更换' : '选择优惠券' }}</button>
      </div>
      <div v-if="selectedCoupon" class="flex items-center gap-2 px-3 py-2 rounded-lg bg-green-50 border border-green-200">
        <Gift class="w-4 h-4 text-green-500 shrink-0" />
        <span class="text-sm text-green-700">{{ selectedCoupon.name }}（-¥{{ selectedCoupon.value?.toFixed(0) }}）</span>
        <button class="ml-auto text-xs text-gray-400 hover:text-functional-danger cursor-pointer" @click="selectedCoupon = null">取消</button>
      </div>
      <div v-else class="text-xs text-gray-400">暂无可用优惠券</div>

      <!-- 优惠券选择面板 -->
      <div v-if="showCouponPicker && couponList.length > 0" class="mt-3 space-y-2 max-h-48 overflow-y-auto">
        <div v-for="c in couponList" :key="c.id"
          class="flex items-center gap-3 px-3 py-2 rounded-lg border cursor-pointer transition-colors"
          :class="selectedCoupon?.id === c.id ? 'border-primary bg-primary/5' : 'border-gray-100 hover:border-gray-200'"
          @click="selectCoupon(c)"
        >
          <div class="w-10 h-10 rounded-lg bg-gradient-to-br from-primary to-primary-dark flex items-center justify-center text-white text-sm font-bold shrink-0">
            ¥{{ c.value?.toFixed(0) }}
          </div>
          <div class="flex-1 min-w-0">
            <p class="text-sm font-medium text-gray-800">{{ c.name }}</p>
            <p class="text-xs text-gray-400">满¥{{ c.minAmount }}可用 · {{ formatDate(c.expiresAt) }}到期</p>
          </div>
          <div v-if="selectedCoupon?.id === c.id" class="w-5 h-5 rounded-full bg-primary text-white flex items-center justify-center shrink-0">
            <Check class="w-3 h-3" />
          </div>
        </div>
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
        <div v-if="selectedCoupon" class="flex justify-between text-functional-success text-sm">
          <span>优惠券抵扣</span>
          <span>-¥{{ formatPrice(discountAmount) }}</span>
        </div>
        <div class="flex justify-between text-base font-bold pt-2 border-t border-gray-100">
          <span>实付金额</span>
          <span class="text-functional-danger text-xl">¥{{ formatPrice(payAmount) }}</span>
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
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { MapPin, Package, Gift, Check } from 'lucide-vue-next'
import { ElMessage } from 'element-plus'
import AddressPicker from '@/components/AddressPicker.vue'
import { useCartStore } from '@/stores/cart'
import { createOrder } from '@/api/order'
import { getUserAddresses } from '@/api/user'
import { getProductDetail } from '@/api/product'
import { getAvailableCoupons } from '@/api/coupon'
import { formatPrice } from '@/utils/format'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()

const showAddressPicker = ref(false)
const selectedAddress = ref(null)
const submitting = ref(false)

// 直接购买模式（不经过购物车）
const isDirectBuy = computed(() => route.query.directBuy === '1')
const directBuyItem = ref(null)

const cartItems = computed(() => {
  if (isDirectBuy.value && directBuyItem.value) {
    return [directBuyItem.value]
  }
  return cartStore.items.filter(item => item.selected === 1)
})

const totalAmount = computed(() => {
  return cartItems.value.reduce((sum, item) => sum + item.price * item.quantity, 0)
})

const canSubmit = computed(() => {
  return selectedAddress.value && cartItems.value.length > 0 && !submitting.value
})

// ===== 优惠券 =====
const couponList = ref([])
const selectedCoupon = ref(null)
const showCouponPicker = ref(false)

const discountAmount = computed(() => {
  if (!selectedCoupon.value) return 0
  return Math.min(selectedCoupon.value.value, totalAmount.value)
})

const payAmount = computed(() => {
  return Math.max(0, totalAmount.value - discountAmount.value)
})

function formatDate(t) {
  if (!t) return ''
  const d = new Date(t)
  return `${d.getMonth()+1}月${d.getDate()}日`
}

async function loadCoupons() {
  try {
    const res = await getAvailableCoupons(totalAmount.value)
    if (res.code === 200 && res.data) {
      couponList.value = res.data
    }
  } catch { /* ignore */ }
}

// 金额变化时重新加载可用优惠券（避免刚加载时 totalAmount=0 导致无券）
watch(totalAmount, (val) => {
  if (val > 0) loadCoupons()
})

function selectCoupon(c) {
  selectedCoupon.value = c
  showCouponPicker.value = false
}

function onAddressSelect(addr) {
  selectedAddress.value = addr
}

async function submitOrder() {
  if (!canSubmit.value) return

  submitting.value = true
  try {
    const orderData = {
      addressId: selectedAddress.value.id,
      couponId: selectedCoupon.value?.id || null,
      items: cartItems.value.map(item => ({
        productId: item.productId || item.product_id,
        quantity: item.quantity
      }))
    }
    const res = await createOrder(orderData)
    if (res.code === 200) {
      ElMessage.success('订单提交成功')
      if (!isDirectBuy.value) {
        // 购物车模式：重新加载购物车（后端已清除已购商品）
        await cartStore.loadCart()
      }
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
    }
  } catch (err) {
    console.error('加载默认地址失败:', err)
  }
}

async function loadDirectBuyProduct() {
  const productId = route.query.productId
  const quantity = parseInt(route.query.quantity) || 1
  if (!productId) return
  try {
    const res = await getProductDetail(productId)
    if (res.code === 200 && res.data) {
      directBuyItem.value = {
        productId: res.data.id,
        product_id: res.data.id,
        productName: res.data.name,
        productImage: res.data.mainImage,
        price: res.data.price,
        quantity: quantity
      }
    }
  } catch (err) {
    console.error('加载直接购买商品失败:', err)
  }
}

onMounted(() => {
  if (isDirectBuy.value) {
    loadDirectBuyProduct()
  } else {
    if (cartStore.items.length === 0) {
      cartStore.loadCart()
    }
  }
  loadDefaultAddress()
  loadCoupons()
})
</script>
