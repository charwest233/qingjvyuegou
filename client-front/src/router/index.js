import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/home/index.vue'),
    meta: { title: '首页', requiresAuth: true }
  },
  {
    path: '/product-list',
    name: 'ProductList',
    component: () => import('@/views/product-list/index.vue'),
    meta: { title: '商品列表', requiresAuth: true }
  },
  {
    path: '/product/:id',
    name: 'ProductDetail',
    component: () => import('@/views/product-detail/index.vue'),
    meta: { title: '商品详情', requiresAuth: true }
  },
  {
    path: '/cart',
    name: 'Cart',
    component: () => import('@/views/cart/index.vue'),
    meta: { title: '购物车', requiresAuth: true }
  },
  {
    path: '/checkout',
    name: 'Checkout',
    component: () => import('@/views/checkout/index.vue'),
    meta: { title: '结算', requiresAuth: true }
  },
  {
    path: '/orders',
    name: 'Orders',
    component: () => import('@/views/orders/index.vue'),
    meta: { title: '我的订单', requiresAuth: true }
  },
  {
    path: '/user',
    name: 'User',
    component: () => import('@/views/user/index.vue'),
    meta: { title: '个人中心', requiresAuth: true }
  },
  {
    path: '/address',
    name: 'Address',
    component: () => import('@/views/address/index.vue'),
    meta: { title: '收货地址', requiresAuth: true }
  },
  {
    path: '/favorites',
    name: 'Favorites',
    component: () => import('@/views/favorites/index.vue'),
    meta: { title: '我的收藏', requiresAuth: true }
  },
  {
    path: '/settings',
    name: 'Settings',
    component: () => import('@/views/settings/index.vue'),
    meta: { title: '设置', requiresAuth: true }
  },
  {
    path: '/ai-chat',
    name: 'AiChat',
    component: () => import('@/views/ai-chat/index.vue'),
    meta: { title: 'AI导购', requiresAuth: true }
  },
  {
    path: '/ai-chat/:id',
    name: 'AiChatSession',
    component: () => import('@/views/ai-chat/index.vue'),
    meta: { title: 'AI导购', requiresAuth: true }
  },
  {
    path: '/admin/reviews',
    name: 'AdminReviews',
    component: () => import('@/views/admin/reviews.vue'),
    meta: { title: '评价管理', requiresAuth: true }
  },
  {
    path: '/user/reviews',
    name: 'UserReviews',
    component: () => import('@/views/user/reviews.vue'),
    meta: { title: '我的评价', requiresAuth: true }
  },
  {
    path: '/customer-service',
    name: 'CustomerService',
    component: () => import('@/views/customer-service/index.vue'),
    meta: { title: '人工客服', requiresAuth: true }
  },
  // 售后相关
  {
    path: '/orders/refund/apply/:orderId',
    name: 'ApplyRefund',
    component: () => import('@/views/refund/ApplyRefund.vue'),
    meta: { title: '申请售后', requiresAuth: true }
  },
  {
    path: '/orders/refund/list',
    name: 'RefundList',
    component: () => import('@/views/refund/RefundList.vue'),
    meta: { title: '售后记录', requiresAuth: true }
  },
  {
    path: '/orders/refund/:id',
    name: 'RefundDetail',
    component: () => import('@/views/refund/RefundDetail.vue'),
    meta: { title: '售后详情', requiresAuth: true }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/home/index.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫 - 检查登录状态
router.beforeEach((to, from, next) => {
  document.title = `${to.meta.title} - 悦选商城`
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
  } else {
    next()
  }
})

export default router
