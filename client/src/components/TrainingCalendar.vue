<template>
  <div class="training-calendar">
    <div class="tc-header">
      <el-button text @click="shiftMonth(-1)">
        <el-icon><ArrowLeft /></el-icon>
      </el-button>
      <div class="tc-month">{{ year }}年 {{ month }}月</div>
      <el-button text @click="shiftMonth(1)">
        <el-icon><ArrowRight /></el-icon>
      </el-button>
    </div>

    <div class="tc-weekdays">
      <div v-for="w in ['一', '二', '三', '四', '五', '六', '日']" :key="w" class="tc-weekday">
        {{ w }}
      </div>
    </div>

    <div class="tc-grid">
      <template v-for="(day, i) in cells" :key="i">
        <div v-if="day" class="tc-day" :class="{ checked: checkedSet.has(day), today: day === todayStr }" @click="$emit('select', day)">
          <span class="tc-num">{{ Number(day.slice(8)) }}</span>
          <span v-if="checkedSet.has(day)" class="tc-dot" />
        </div>
        <div v-else class="tc-day blank" />
      </template>
    </div>

    <div class="tc-legend">
      <span class="legend-item"><span class="legend-dot" /> 已打卡</span>
      <span class="legend-item"><span class="legend-ring" /> 今日</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { daysInMonth, today } from '@/composables/useDate'

const props = defineProps<{
  /** YYYY-MM，如 2026-08 */
  modelValue: string
  /** 已打卡日期 YYYY-MM-DD */
  checkedDates?: string[]
}>()

const emit = defineEmits<{
  'update:modelValue': [string]
  select: [string]
}>()

const year = computed(() => Number(props.modelValue.slice(0, 4)))
const month = computed(() => Number(props.modelValue.slice(5, 7)))
const todayStr = today()

const checkedSet = computed(() => new Set(props.checkedDates || []))

// 周一为起始的网格，不足首位用 null 占位
const cells = computed<(string | null)[]>(() => {
  const first = new Date(year.value, month.value - 1, 1)
  const startWeekday = (first.getDay() + 6) % 7
  const total = daysInMonth(year.value, month.value)
  const result: (string | null)[] = []
  for (let i = 0; i < startWeekday; i++) result.push(null)
  for (let d = 1; d <= total; d++) {
    result.push(`${year.value}-${String(month.value).padStart(2, '0')}-${String(d).padStart(2, '0')}`)
  }
  return result
})

function shiftMonth(delta: number) {
  const d = new Date(year.value, month.value - 1 + delta, 1)
  emit('update:modelValue', `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`)
}
</script>

<style scoped>
.training-calendar {
  user-select: none;
}
.tc-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.tc-month {
  font-size: var(--fth-text-lg);
  font-weight: 600;
  color: var(--fth-text-primary);
}
.tc-weekdays,
.tc-grid {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 4px;
}
.tc-weekday {
  text-align: center;
  font-size: var(--fth-text-xs);
  color: var(--fth-text-tertiary);
  padding: 4px 0;
}
.tc-day {
  position: relative;
  height: 38px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
  border-radius: var(--fth-radius-sm);
  cursor: pointer;
  transition: background 0.12s ease;
  font-size: var(--fth-text-sm);
  color: var(--fth-text-primary);
}
.tc-day:not(.checked):hover {
  background: var(--fth-bg-muted);
}
.tc-day.checked {
  background: var(--fth-primary-soft);
  color: var(--fth-primary-hover);
  font-weight: 600;
}
.tc-day.today {
  box-shadow: inset 0 0 0 1.5px var(--fth-primary);
}
.tc-day.blank {
  cursor: default;
}
.tc-num {
  line-height: 1;
  font-variant-numeric: tabular-nums;
}
.tc-dot {
  width: 5px;
  height: 5px;
  border-radius: 50%;
  background: var(--fth-primary);
}
.tc-legend {
  display: flex;
  gap: 16px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--fth-border-soft);
}
.legend-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: var(--fth-text-xs);
  color: var(--fth-text-secondary);
}
.legend-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--fth-primary);
}
.legend-ring {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  box-shadow: inset 0 0 0 1.5px var(--fth-primary);
}
</style>
