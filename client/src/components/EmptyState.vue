<template>
  <div class="empty-state">
    <div class="es-icon">
      <el-icon :size="34"><component :is="icon" /></el-icon>
    </div>
    <div class="es-desc">{{ description }}</div>
    <div v-if="$slots.action" class="es-action">
      <slot name="action" />
    </div>
  </div>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    description?: string
    icon?: string
  }>(),
  {
    description: '暂无数据',
    icon: 'Inbox',
  },
)
</script>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 16px;
  text-align: center;
}
.es-icon {
  color: var(--fth-text-tertiary);
  margin-bottom: 8px;
}
.es-desc {
  color: var(--fth-text-secondary);
  font-size: var(--fth-text-base);
}
.es-action {
  margin-top: 12px;
}
</style>
