<template>
  <div class="page">
    <PageHeader title="数据分析" subtitle="用数据复盘训练，找到进步的方向" />

    <!-- 近 30 天概览 -->
    <div class="stat-grid">
      <StatCard
        label="近 30 天训练次数"
        :value="summary.count"
        unit="次"
        icon="Calendar"
        tone="primary"
        :loading="loading"
      />
      <StatCard
        label="近 30 天训练时长"
        :value="summary.duration"
        unit="分钟"
        icon="Timer"
        tone="blue"
        :loading="loading"
      >
        {{ Math.round(summary.duration / 60) }} 小时
      </StatCard>
      <StatCard
        label="近 30 天消耗热量"
        :value="summary.calories"
        unit="千卡"
        icon="Lightning"
        tone="warning"
        :loading="loading"
      />
      <StatCard
        label="累计打卡"
        :value="streak?.totalDays"
        unit="天"
        icon="Histogram"
        tone="purple"
        :loading="loading"
      >
        最长连续 {{ streak?.longestStreak ?? '-' }} 天
      </StatCard>
    </div>

    <div class="grid">
      <ChartCard
        ref="countChartRef"
        title="训练次数趋势"
        subtitle="近 30 天每日训练次数"
        height="280px"
        :loading="loading"
        :empty="empty"
      />
      <ChartCard
        ref="durationChartRef"
        title="训练时长趋势"
        subtitle="近 30 天每日训练时长（分钟）"
        height="280px"
        :loading="loading"
        :empty="empty"
      />
      <ChartCard
        ref="calorieChartRef"
        title="热量消耗趋势"
        subtitle="近 30 天每日消耗热量（千卡）"
        height="280px"
        :loading="loading"
        :empty="empty"
      />
      <ChartCard
        ref="typeChartRef"
        title="训练类型分布"
        subtitle="各部位训练占比（按动作记录）"
        height="280px"
        :loading="loading"
        :empty="typeEmpty"
        empty-text="暂无训练动作记录"
        empty-icon="PieChart"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import StatCard from '@/components/StatCard.vue'
import ChartCard from '@/components/ChartCard.vue'
import { getStreak, getTrainingStats, listTraining } from '@/api/training'
import type { StreakVO, TrainingDailyStat, TrainingRecordVO } from '@/types'
import { CHART_COLORS } from '@/constants'

const stats = ref<TrainingDailyStat[]>([])
const streak = ref<StreakVO | null>(null)
const trainingList = ref<TrainingRecordVO[]>([])
const loading = ref(true)

const countChartRef = ref<InstanceType<typeof ChartCard>>()
const durationChartRef = ref<InstanceType<typeof ChartCard>>()
const calorieChartRef = ref<InstanceType<typeof ChartCard>>()
const typeChartRef = ref<InstanceType<typeof ChartCard>>()

const summary = reactive({ count: 0, duration: 0, calories: 0 })
const empty = computed(() => stats.value.every((s) => s.recordCount === 0))
const typeEmpty = computed(() => trainingList.value.length === 0)

const PART_COLORS = [CHART_COLORS.primary, CHART_COLORS.blue, CHART_COLORS.purple, CHART_COLORS.amber, CHART_COLORS.green, CHART_COLORS.red]

async function loadAll() {
  loading.value = true
  try {
    const [s, streakData, page] = await Promise.all([
      getTrainingStats(30),
      getStreak(),
      listTraining({ page: 1, size: 100 }),
    ])
    stats.value = s
    streak.value = streakData
    trainingList.value = page.records

    summary.count = s.reduce((sum, x) => sum + x.recordCount, 0)
    summary.duration = s.reduce((sum, x) => sum + x.totalDurationMinutes, 0)
    summary.calories = s.reduce((sum, x) => sum + x.totalCalories, 0)

    renderCharts()
  } catch {
    // 拦截器已提示
  } finally {
    loading.value = false
  }
}

function renderCharts() {
  const dates = stats.value.map((s) => s.trainDate.slice(5))
  const axis = {
    axisLine: { lineStyle: { color: '#e5e7eb' } },
    axisTick: { show: false },
  }

  countChartRef.value?.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 32, right: 16, top: 16, bottom: 26 },
    xAxis: { type: 'category', data: dates, ...axis },
    yAxis: { type: 'value', minInterval: 1, splitLine: { lineStyle: { color: '#f3f4f6' } } },
    series: [
      {
        name: '训练次数',
        type: 'bar',
        data: stats.value.map((s) => s.recordCount),
        itemStyle: { color: CHART_COLORS.primary, borderRadius: [3, 3, 0, 0] },
        barMaxWidth: 16,
      },
    ],
  })

  durationChartRef.value?.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 16, top: 16, bottom: 26 },
    xAxis: { type: 'category', data: dates, ...axis },
    yAxis: { type: 'value', name: '分钟', splitLine: { lineStyle: { color: '#f3f4f6' } } },
    series: [
      {
        name: '训练时长(分钟)',
        type: 'line',
        data: stats.value.map((s) => Math.round(s.totalDurationMinutes)),
        smooth: true,
        symbolSize: 5,
        lineStyle: { color: CHART_COLORS.blue, width: 2 },
        itemStyle: { color: CHART_COLORS.blue },
        areaStyle: { color: 'rgba(59, 130, 246, 0.08)' },
      },
    ],
  })

  calorieChartRef.value?.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 48, right: 16, top: 16, bottom: 26 },
    xAxis: { type: 'category', data: dates, ...axis },
    yAxis: { type: 'value', name: '千卡', splitLine: { lineStyle: { color: '#f3f4f6' } } },
    series: [
      {
        name: '消耗热量(千卡)',
        type: 'line',
        data: stats.value.map((s) => s.totalCalories),
        smooth: true,
        symbolSize: 5,
        lineStyle: { color: CHART_COLORS.amber, width: 2 },
        itemStyle: { color: CHART_COLORS.amber },
        areaStyle: { color: 'rgba(245, 158, 11, 0.08)' },
      },
    ],
  })

  // 训练类型分布：聚合 listTraining 中所有 sets.bodyPart
  const partMap = new Map<string, number>()
  for (const r of trainingList.value) {
    for (const s of r.sets ?? []) {
      if (s.bodyPart) partMap.set(s.bodyPart, (partMap.get(s.bodyPart) ?? 0) + 1)
    }
  }
  const entries = [...partMap.entries()]
  const data = entries.map(([name, value], i) => ({
    name,
    value,
    itemStyle: { color: PART_COLORS[i % PART_COLORS.length] },
  }))
  typeChartRef.value?.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} 次 ({d}%)' },
    legend: { bottom: 0, icon: 'circle' },
    series: [
      {
        name: '训练部位',
        type: 'pie',
        radius: ['48%', '70%'],
        center: ['50%', '44%'],
        itemStyle: { borderRadius: 4, borderColor: '#fff', borderWidth: 2 },
        label: { show: false },
        emphasis: { label: { show: true, fontWeight: 600 } },
        data,
      },
    ],
  })
}

onMounted(loadAll)
</script>

<style scoped>
.grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}
@media (max-width: 1023px) {
  .grid {
    grid-template-columns: 1fr;
  }
}
</style>
