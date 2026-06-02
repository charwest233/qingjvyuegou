<template>
  <el-breadcrumb separator="/">
    <el-breadcrumb-item :to="{ path: '/dashboard' }">
      首页
    </el-breadcrumb-item>
    <el-breadcrumb-item v-if="currentRoute.meta && currentRoute.meta.title">
      {{ currentRoute.meta.title }}
    </el-breadcrumb-item>
  </el-breadcrumb>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const currentRoute = computed(() => route)
</script>

<style scoped>
:deep(.el-breadcrumb__inner) {
  font-size: 14px;
  color: #6b7280;
}

:deep(.el-breadcrumb__inner.is-link:hover) {
  color: #171717;
}

:deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: #171717;
  font-weight: 500;
}
</style>
