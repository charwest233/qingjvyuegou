<template>
  <div class="min-h-screen bg-background pb-20">
    <!-- 顶部栏 -->
    <div class="sticky top-0 z-10 bg-white/80 backdrop-blur-md border-b border-gray-100">
      <div class="flex items-center gap-3 px-4 py-3">
        <button @click="$router.back()" class="p-2 hover:bg-gray-100 rounded-xl transition-colors cursor-pointer">
          <ArrowLeft class="w-5 h-5 text-text-secondary" />
        </button>
        <div>
          <h1 class="text-base font-heading font-semibold">物流追踪</h1>
          <p v-if="data" class="text-xs text-gray-400 mt-0.5">{{ data.expressCompany }} {{ data.expressNo }}</p>
        </div>
      </div>
    </div>

    <!-- 加载中 -->
    <div v-if="loading" class="flex flex-col items-center justify-center py-32">
      <Loader2 class="w-8 h-8 text-primary animate-spin mb-3" />
      <p class="text-sm text-gray-400">加载中...</p>
    </div>

    <!-- 错误提示 -->
    <div v-else-if="error" class="text-center py-16 px-4">
      <MapPin class="w-16 h-16 text-gray-200 mx-auto mb-4" />
      <p class="text-sm text-text-secondary">{{ error }}</p>
    </div>

    <!-- 物流信息 -->
    <template v-else-if="data">
      <!-- 物流状态条 -->
      <div class="mx-4 mt-4 p-4 bg-white rounded-xl shadow-sm flex items-center gap-4">
        <div class="w-12 h-12 rounded-full flex items-center justify-center"
          :class="data.status === 'delivered' ? 'bg-green-100' : 'bg-blue-100'">
          <Truck class="w-6 h-6" :class="data.status === 'delivered' ? 'text-green-500' : 'text-blue-500'" />
        </div>
        <div class="flex-1">
          <p class="text-sm font-medium text-text-primary">
            {{ data.status === 'delivered' ? '已签收' : '配送中' }}
          </p>
          <p class="text-xs text-text-secondary mt-0.5">{{ data.expressCompany }} {{ data.expressNo }}</p>
        </div>
      </div>

      <!-- ===== 高德地图区域（加分项） ===== -->
      <div class="mx-4 mt-3 bg-white rounded-xl shadow-sm overflow-hidden">
        <div class="p-4 pb-0">
          <div class="flex items-center gap-2">
            <MapPin class="w-4 h-4 text-primary" />
            <span class="text-sm font-medium text-text-primary">青桔悦购 · 全国物流网络</span>
          </div>
          <p class="text-xs text-text-secondary mt-1">高德地图 API 集成展示</p>
        </div>
        <div id="amap-container" class="w-full" style="height: 260px"></div>
      </div>

      <!-- 运输路线 -->
      <div class="mx-4 mt-3 bg-white rounded-xl shadow-sm p-4">
        <h3 class="text-sm font-medium text-text-primary mb-4">运输路线</h3>
        <div class="relative py-4">
          <div class="absolute left-6 top-8 bottom-8 w-0.5 bg-gradient-to-b from-blue-500 via-green-400 to-orange-400" />
          <div class="flex items-start gap-4 mb-6 relative">
            <div class="w-12 h-12 rounded-full bg-blue-500 flex items-center justify-center shrink-0 relative z-10 shadow-lg">
              <Warehouse class="w-6 h-6 text-white" />
            </div>
            <div class="flex-1 pt-1">
              <p class="text-sm font-medium text-text-primary">{{ routeInfo.from }}</p>
              <p class="text-xs text-gray-400 mt-0.5">发货仓库</p>
            </div>
          </div>
          <div v-for="(station, i) in routeInfo.stations" :key="i" 
            class="flex items-start gap-4 mb-6 relative">
            <div class="w-12 h-12 rounded-full bg-green-400 flex items-center justify-center shrink-0 relative z-10 shadow">
              <MapPin class="w-5 h-5 text-white" />
            </div>
            <div class="flex-1 pt-1">
              <p class="text-sm font-medium text-text-primary">{{ station.name }}</p>
              <p class="text-xs text-gray-400 mt-0.5">{{ station.desc }}</p>
            </div>
          </div>
          <div class="flex items-start gap-4 relative">
            <div class="w-12 h-12 rounded-full flex items-center justify-center shrink-0 relative z-10 shadow-lg"
              :class="data.status === 'delivered' ? 'bg-green-500' : 'bg-orange-400'">
              <Home class="w-6 h-6 text-white" />
            </div>
            <div class="flex-1 pt-1">
              <p class="text-sm font-medium text-text-primary">{{ routeInfo.to }}</p>
              <p class="text-xs text-gray-400 mt-0.5">{{ data.status === 'delivered' ? '已签收' : '配送中' }}</p>
            </div>
          </div>
        </div>
      </div>

      <!-- 物流详情 -->
      <div class="mx-4 mt-3 bg-white rounded-xl shadow-sm p-4">
        <h3 class="text-sm font-medium text-text-primary mb-4">物流详情</h3>
        <div class="relative">
          <div class="absolute left-[11px] top-2 bottom-2 w-0.5 bg-gray-100" />
          <div v-for="(step, i) in data.steps" :key="i" class="flex gap-4 pb-5 last:pb-0 relative">
            <div class="w-6 h-6 rounded-full flex items-center justify-center shrink-0 relative z-10"
              :class="i === 0 ? 'bg-primary' : 'bg-gray-200'">
              <div v-if="i === 0" class="w-2.5 h-2.5 rounded-full bg-white" />
            </div>
            <div class="flex-1 min-w-0 pt-0.5" :class="i === 0 ? '' : 'opacity-60'">
              <p class="text-sm font-medium" :class="i === 0 ? 'text-text-primary' : 'text-text-secondary'">{{ step.desc }}</p>
              <p class="text-xs text-gray-400 mt-0.5">{{ step.time }}</p>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick, computed } from 'vue'
import { useRoute } from 'vue-router'
import { ArrowLeft, Loader2, MapPin, Truck, Warehouse, Home } from 'lucide-vue-next'
import { getOrderTracking } from '@/api/tracking'
import { AMAP_KEY } from '@/config/map'

const route = useRoute()
const data = ref(null)
const loading = ref(true)
const error = ref('')

let map = null

const routeInfo = computed(() => {
  if (!data.value) return { from: '杭州发货仓', to: '收货地址', stations: [] }
  const addr = data.value.receiverAddress || ''
  let toCity = '收货地址'
  const cities = ['北京', '上海', '广州', '深圳', '杭州', '南京', '武汉', '成都', '重庆', '西安', '苏州', '天津']
  for (const city of cities) { if (addr.includes(city)) { toCity = city; break } }
  const warehouses = {
    '北京': '廊坊发货仓', '天津': '北京发货仓', '上海': '杭州发货仓', '杭州': '上海发货仓',
    '南京': '苏州发货仓', '苏州': '上海发货仓', '广州': '东莞发货仓', '深圳': '惠州发货仓',
    '武汉': '郑州发货仓', '成都': '重庆发货仓', '重庆': '成都发货仓', '西安': '郑州发货仓'
  }
  const from = warehouses[toCity] || '杭州发货仓'
  const stations = [
    { name: `${from.replace('发货仓', '')}转运中心`, desc: '快件已发出' },
    { name: `${toCity}转运中心`, desc: '快件到达分拨中心' }
  ]
  return { from, to: toCity, stations }
})

function initMap() {
  const el = document.getElementById('amap-container')
  if (!el || !window.AMap) return

  // 提取路线坐标点 → AMap.LngLat
  const routeData = data.value?.route
  let start, end, waypoints, center

  if (routeData && routeData.length >= 2) {
    const points = routeData.map(p => new AMap.LngLat(Number(p.lng), Number(p.lat)))
    start = points[0]
    end = points[points.length - 1]
    waypoints = points.slice(1, -1)
    center = new AMap.LngLat((start.lng + end.lng) / 2, (start.lat + end.lat) / 2)
  } else {
    // 无路线数据时，固定显示杭州
    start = new AMap.LngLat(120.155, 30.274)
    end = guessCityCenter(data.value?.receiverAddress || '')
    waypoints = []
    center = new AMap.LngLat((start.lng + end.lng) / 2, (start.lat + end.lat) / 2)
  }

  map = new AMap.Map('amap-container', {
    center,
    zoom: 6,
    mapStyle: 'amap://styles/light',
    features: ['bg', 'road', 'building'],
    showIndoorMap: false
  })

  // ---------- 1. 虚线路线 ----------
  const path = [start, ...waypoints, end]
  new AMap.Polyline({
    map,
    path,
    strokeColor: '#409EFF',
    strokeWeight: 4,
    strokeOpacity: 0.7,
    strokeStyle: 'dashed',
    strokeDasharray: [10, 8]
  })

  // ---------- 2. 起点标记（发货仓库） ----------
  new AMap.Marker({
    map,
    position: start,
    content: `<div style="
      padding:6px 14px;
      background:linear-gradient(135deg,#409EFF,#3b82f6);
      color:white;
      border-radius:20px;
      font-size:13px;
      font-weight:600;
      white-space:nowrap;
      box-shadow:0 3px 12px rgba(64,158,255,0.4);
      border:2px solid white;
    ">🏭 发货仓库</div>`,
    offset: new AMap.Pixel(-55, -18)
  })

  // ---------- 3. 中间途经标记 ----------
  waypoints.forEach(p => {
    new AMap.Marker({
      map,
      position: p,
      content: `<div style="
        width:12px;height:12px;
        background:#52c41a;
        border:2px solid white;
        border-radius:50%;
        box-shadow:0 2px 6px rgba(0,0,0,0.2);
      "></div>`,
      offset: new AMap.Pixel(-6, -6)
    })
  })

  // ---------- 4. 终点标记（客户地址） ----------
  new AMap.Marker({
    map,
    position: end,
    content: `<div style="
      padding:6px 14px;
      background:linear-gradient(135deg,#f56c6c,#e74c3c);
      color:white;
      border-radius:20px;
      font-size:13px;
      font-weight:600;
      white-space:nowrap;
      box-shadow:0 3px 12px rgba(245,108,108,0.4);
      border:2px solid white;
    ">📍 收货地址</div>`,
    offset: new AMap.Pixel(-55, -18)
  })

  // ---------- 5. 添加地图控件 ----------
  map.plugin(['AMap.ToolBar', 'AMap.Scale'], () => {
    map.addControl(new AMap.ToolBar())
    map.addControl(new AMap.Scale())
  })
}

/** 简易地址→坐标映射（与被调用端 guessCityCenter 保持一致） */
function guessCityCenter(address) {
  if (!address) return new AMap.LngLat(121.474, 31.230)
  if (address.includes('北京')) return new AMap.LngLat(116.397, 39.908)
  if (address.includes('上海')) return new AMap.LngLat(121.474, 31.230)
  if (address.includes('广州')) return new AMap.LngLat(113.264, 23.129)
  if (address.includes('深圳')) return new AMap.LngLat(114.057, 22.543)
  if (address.includes('杭州')) return new AMap.LngLat(120.155, 30.274)
  if (address.includes('成都')) return new AMap.LngLat(104.066, 30.572)
  if (address.includes('武汉')) return new AMap.LngLat(114.305, 30.593)
  if (address.includes('南京')) return new AMap.LngLat(118.796, 32.060)
  if (address.includes('重庆')) return new AMap.LngLat(106.551, 29.563)
  if (address.includes('西安')) return new AMap.LngLat(108.940, 34.261)
  return new AMap.LngLat(121.474, 31.230)
}

function cleanupMap() {
  if (map) { map.destroy(); map = null }
}

onMounted(async () => {
  const orderId = route.params.orderId
  if (!orderId) { error.value = '缺少订单ID'; loading.value = false; return }

  try {
    const res = await getOrderTracking(orderId)
    if (res.code === 200 && res.data) {
      data.value = res.data
    } else {
      error.value = res.message || '暂无物流信息'
    }
  } catch (e) {
    error.value = '加载物流信息失败'
  } finally {
    loading.value = false
  }

  // 地图初始化：在 DOM 完全渲染后独立加载
  await nextTick()
  await nextTick()
  
  if (!window.AMap) {
    const script = document.createElement('script')
    script.src = `https://webapi.amap.com/maps?v=2.0&key=${AMAP_KEY}`
    script.async = true
    script.defer = true
    document.head.appendChild(script)
    await new Promise((resolve) => {
      script.onload = () => {
        const check = () => { if (window.AMap) resolve(); else setTimeout(check, 100) }
        check()
      }
    })
  }

  // 等待 DOM 确保容器就绪
  for (let i = 0; i < 10; i++) {
    if (document.getElementById('amap-container')) break
    await new Promise(r => setTimeout(r, 200))
  }
  
  initMap()
})

onUnmounted(() => { cleanupMap() })
</script>
