<template>
  <el-card class="chart-card">
    <template #header>
      <div class="cc-header">
        <div class="cc-titles">
          <div class="cc-title">{{ title }}</div>
          <div v-if="subtitle" class="cc-subtitle">{{ subtitle }}</div>
        </div>
        <div v-if="$slots.extra" class="cc-extra">
          <slot name="extra" />
        </div>
      </div>
    </template>

    <div v-loading="loading" class="cc-body">
      <div ref="chartEl" class="cc-chart" :style="{ height: height }" />
      <div v-if="empty && !loading" class="cc-empty">
        <EmptyState :description="emptyText" :icon="emptyIcon" />
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useECharts } from '@/composables/useECharts'
import EmptyState from './EmptyState.vue'

withDefaults(
  defineProps<{
    title: string
    subtitle?: string
    height?: string
    loading?: boolean
    empty?: boolean
    emptyText?: string
    emptyIcon?: string
  }>(),
  {
    height: '300px',
    loading: false,
    empty: false,
    emptyText: '暂无数据，去完成一次打卡吧',
    emptyIcon: 'DataLine',
  },
)

const chartEl = ref<HTMLElement>()
const { setOption, clear, resize } = useECharts(chartEl)

defineExpose({ setOption, clear, resize })
</script>

<style scoped>
.cc-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}
.cc-title {
  font-size: var(--fth-text-lg);
  font-weight: 600;
  color: var(--fth-text-primary);
}
.cc-subtitle {
  margin-top: 2px;
  font-size: var(--fth-text-xs);
  color: var(--fth-text-tertiary);
}
.cc-body {
  position: relative;
}
.cc-chart {
  width: 100%;
}
.cc-empty {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
