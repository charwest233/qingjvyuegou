// ============================================
// 路由配置
// ============================================

import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: '/dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '仪表盘', icon: 'Odometer' }
      },
      {
        path: '/products',
        name: 'Products',
        component: () => import('@/views/product/index.vue'),
        meta: { title: '商品管理', icon: 'Goods' }
      },
      {
        path: '/categories',
        name: 'Categories',
        component: () => import('@/views/category/index.vue'),
        meta: { title: '分类管理', icon: 'FolderOpened' }
      },
      {
        path: '/orders',
        name: 'Orders',
        component: () => import('@/views/order/index.vue'),
        meta: { title: '订单管理', icon: 'ShoppingCart' }
      },
      {
        path: '/users',
        name: 'Users',
        component: () => import('@/views/user/index.vue'),
        meta: { title: '用户管理', icon: 'User' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('admin_token')

  // 公开页面直接放行
  if (to.meta.public) {
    // 已登录用户访问登录页，跳转到首页
    if (token && to.path === '/login') {
      next('/dashboard')
      return
    }
    next()
    return
  }

  // 需要登录的页面
  if (!token) {
    next('/login')
    return
  }

  next()
})

export default router
