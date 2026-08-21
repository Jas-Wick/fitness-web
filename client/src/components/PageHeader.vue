<template>
  <div class="page-header">
    <div class="ph-left">
      <h1 class="ph-title">{{ title }}</h1>
      <p v-if="subtitle" class="ph-subtitle">{{ subtitle }}</p>
    </div>
    <div v-if="$slots.actions" class="ph-actions">
      <slot name="actions" />
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  title: string
  subtitle?: string
}>()
</script>

<style scoped>
.page-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}
.ph-title {
  margin: 0;
  font-size: var(--fth-text-2xl);
  font-weight: 700;
  line-height: 1.3;
  color: var(--fth-text-primary);
}
.ph-subtitle {
  margin: 4px 0 0;
  font-size: var(--fth-text-sm);
  color: var(--fth-text-secondary);
}
.ph-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
