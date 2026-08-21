<template>
  <div class="not-found">
    <div class="code">404</div>
    <div class="title">页面不存在</div>
    <div class="desc">你访问的页面可能已被移除，或输入的地址有误。</div>
    <el-button type="primary" size="large" @click="router.push('/home')">
      <el-icon class="mr-icon"><House /></el-icon>
      返回仪表盘
    </el-button>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<style scoped>
.not-found {
  height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: var(--fth-bg-page);
}
.code {
  font-size: 96px;
  font-weight: 800;
  line-height: 1;
  color: var(--fth-primary);
  letter-spacing: 4px;
}
.title {
  margin-top: 16px;
  font-size: var(--fth-text-2xl);
  font-weight: 600;
  color: var(--fth-text-primary);
}
.desc {
  margin: 8px 0 24px;
  color: var(--fth-text-secondary);
}
.mr-icon {
  margin-right: 6px;
}
</style>
