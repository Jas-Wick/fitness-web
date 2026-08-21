<template>
  <div class="stat-card" v-loading="loading">
    <div v-if="icon" class="sc-icon" :class="tone">
      <el-icon :size="18"><component :is="icon" /></el-icon>
    </div>
    <div class="sc-content">
      <div class="sc-label">{{ label }}</div>
      <div class="sc-value-row">
        <span class="sc-value">{{ displayValue }}</span>
        <span v-if="unit" class="sc-unit">{{ unit }}</span>
      </div>
      <div v-if="$slots.default" class="sc-extra">
        <slot />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

type Tone = 'primary' | 'success' | 'warning' | 'danger' | 'info' | 'blue' | 'purple'

const props = withDefaults(
  defineProps<{
    label: string
    value: number | string | null | undefined
    unit?: string
    icon?: string
    tone?: Tone
    loading?: boolean
    /** 当 value 为空时的占位文案 */
    emptyText?: string
  }>(),
  {
    unit: '',
    icon: '',
    tone: 'primary',
    loading: false,
    emptyText: '-',
  },
)

const displayValue = computed(() => {
  if (props.value === null || props.value === undefined || props.value === '') {
    return props.emptyText
  }
  return String(props.value)
})
</script>

<style scoped>
.stat-card {
  background: var(--fth-bg-card);
  border: 1px solid var(--fth-border);
  border-radius: var(--fth-radius);
  padding: 18px 20px;
  display: flex;
  align-items: flex-start;
  gap: 14px;
  box-shadow: var(--fth-shadow-sm);
  transition: box-shadow 0.15s ease;
}
.stat-card:hover {
  box-shadow: var(--fth-shadow-md);
}
.sc-icon {
  width: 40px;
  height: 40px;
  border-radius: var(--fth-radius);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.sc-icon.primary { background: var(--fth-primary-soft); color: var(--fth-primary); }
.sc-icon.success { background: var(--fth-success-soft); color: var(--fth-success); }
.sc-icon.warning { background: var(--fth-warning-soft); color: var(--fth-warning); }
.sc-icon.danger  { background: var(--fth-danger-soft);  color: var(--fth-danger); }
.sc-icon.info    { background: var(--fth-info-soft);    color: var(--fth-info); }
.sc-icon.blue    { background: #dbeafe; color: #3b82f6; }
.sc-icon.purple  { background: #ede9fe; color: #8b5cf6; }

.sc-content {
  min-width: 0;
}
.sc-label {
  font-size: var(--fth-text-sm);
  color: var(--fth-text-secondary);
  margin-bottom: 2px;
}
.sc-value-row {
  display: flex;
  align-items: baseline;
  gap: 4px;
}
.sc-value {
  font-size: 26px;
  font-weight: 700;
  line-height: 1.2;
  color: var(--fth-text-primary);
  font-variant-numeric: tabular-nums;
}
.sc-unit {
  font-size: var(--fth-text-sm);
  color: var(--fth-text-tertiary);
}
.sc-extra {
  margin-top: 4px;
  font-size: var(--fth-text-xs);
  color: var(--fth-text-tertiary);
}
</style>
