<template>
  <div class="macro-progress">
    <div v-for="row in rows" :key="row.label" class="macro-row">
      <div class="macro-head">
        <span class="macro-label">
          <span class="dot" :style="{ background: row.color }" />
          {{ row.label }}
        </span>
        <span class="macro-value">
          {{ row.value }}<span class="macro-unit"> g</span>
          <span class="macro-pct">{{ row.pct }}%</span>
        </span>
      </div>
      <el-progress
        :percentage="row.pct"
        :color="row.color"
        :show-text="false"
        :stroke-width="8"
        class="macro-bar"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  totalProtein?: number | null
  totalCarbs?: number | null
  totalFat?: number | null
}>()

const rows = computed(() => {
  const protein = Number(props.totalProtein || 0)
  const carbs = Number(props.totalCarbs || 0)
  const fat = Number(props.totalFat || 0)
  const total = protein + carbs + fat
  const pct = (v: number) => (total > 0 ? Math.round((v / total) * 100) : 0)
  return [
    { label: '蛋白质', value: protein, pct: pct(protein), color: '#3b82f6' },
    { label: '碳水', value: carbs, pct: pct(carbs), color: '#f59e0b' },
    { label: '脂肪', value: fat, pct: pct(fat), color: '#8b5cf6' },
  ]
})
</script>

<style scoped>
.macro-progress {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.macro-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
}
.macro-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: var(--fth-text-sm);
  color: var(--fth-text-secondary);
}
.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}
.macro-value {
  font-size: var(--fth-text-sm);
  font-weight: 600;
  color: var(--fth-text-primary);
  font-variant-numeric: tabular-nums;
}
.macro-unit {
  font-weight: 400;
  color: var(--fth-text-tertiary);
  margin-left: 1px;
}
.macro-pct {
  margin-left: 8px;
  font-weight: 400;
  color: var(--fth-text-tertiary);
}
.macro-bar :deep(.el-progress-bar__outer) {
  background: var(--fth-bg-muted);
}
</style>
