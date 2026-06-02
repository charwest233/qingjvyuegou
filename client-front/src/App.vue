<template>
  <div class="min-h-screen bg-background">
    <!-- 顶部导航栏 -->
    <NavBar v-if="!isLoginPage" />

    <!-- 主内容区 -->
    <main :class="contentClass">
      <router-view v-slot="{ Component }">
        <transition name="page" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <!-- 底部Tab栏 -->
    <TabBar v-if="!isLoginPage" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import NavBar from './components/NavBar.vue'
import TabBar from './components/TabBar.vue'

const route = useRoute()
const isLoginPage = computed(() => route.path === '/login')
const contentClass = computed(() => {
  if (isLoginPage.value) return ''
  return 'pt-16 pb-20 min-h-screen'
})
</script>
