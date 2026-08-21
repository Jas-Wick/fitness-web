<template>
  <div class="page">
    <PageHeader title="训练打卡" subtitle="记录每一次训练，见证持续进步">
      <template #actions>
        <el-segmented v-model="mode" :options="modeOptions" />
      </template>
    </PageHeader>

    <!-- 概览统计 -->
    <div class="stat-grid">
      <StatCard
        label="今日打卡"
        :value="todayChecked ? '已完成' : '待打卡'"
        :icon="todayChecked ? 'CircleCheck' : 'Calendar'"
        :tone="todayChecked ? 'success' : 'info'"
        :loading="loading.calendar"
      >
        {{ todayChecked ? '坚持就是胜利' : '去完成今天的训练吧' }}
      </StatCard>
      <StatCard
        label="当前连续打卡"
        :value="streak?.currentStreak"
        unit="天"
        icon="AlarmClock"
        tone="primary"
        :loading="loading.streak"
      />
      <StatCard
        label="累计打卡"
        :value="streak?.totalDays"
        unit="天"
        icon="Histogram"
        tone="blue"
        :loading="loading.streak"
      />
      <StatCard
        label="本月打卡"
        :value="checkedDates.length"
        unit="天"
        icon="Grid"
        tone="purple"
        :loading="loading.calendar"
      >
        最长连续 {{ streak?.longestStreak ?? '-' }} 天
      </StatCard>
    </div>

    <div class="row">
      <!-- 打卡表单 -->
      <div class="col col-7">
        <el-card class="form-card">
          <template #header>
            <div class="card-title">
              <el-icon class="title-icon"><EditPen /></el-icon>
              训练打卡
            </div>
          </template>

          <el-form label-position="top">
            <el-form-item label="训练日期" required>
              <el-date-picker
                v-model="form.trainDate"
                type="date"
                value-format="YYYY-MM-DD"
                placeholder="选择日期"
                style="width: 100%"
              />
            </el-form-item>

            <!-- 基础版：训练类型多选 -->
            <el-form-item v-if="mode === 1" label="训练类型" required>
              <div class="type-grid">
                <el-checkbox v-for="t in TRAINING_TYPES" :key="t" v-model="basicTypes" :value="t" :label="t" class="type-check" />
              </div>
            </el-form-item>

            <!-- 进阶版：动作组列表 -->
            <div v-else class="sets-block">
              <div class="sets-title">训练动作（部位 / 动作 / 重量 / 次数 / 组数）</div>
              <div v-for="(s, i) in advancedSets" :key="i" class="set-row">
                <el-select v-model="s.bodyPart" placeholder="部位" class="set-part">
                  <el-option v-for="t in TRAINING_TYPES" :key="t" :label="t" :value="t" />
                </el-select>
                <el-input v-model="s.exerciseName" placeholder="动作名称" class="set-name" />
                <el-input-number v-model="s.weight" :min="0" :controls="false" placeholder="重量kg" class="set-num" />
                <el-input-number v-model="s.reps" :min="0" :controls="false" placeholder="次数" class="set-num" />
                <el-input-number v-model="s.sets" :min="0" :controls="false" placeholder="组数" class="set-num" />
                <el-button link type="danger" @click="advancedSets.splice(i, 1)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
              <el-button text type="primary" @click="addSet">
                <el-icon class="btn-icon"><Plus /></el-icon>添加动作
              </el-button>
            </div>

            <div class="form-duo">
              <el-form-item label="训练时长" class="duo-item">
                <div class="duration">
                  <el-input-number v-model="form.durationValue" :min="0" :controls="false" placeholder="时长" class="dur-num" />
                  <el-select v-model="form.durationUnit" class="dur-unit">
                    <el-option label="分钟" value="MINUTE" />
                    <el-option label="小时" value="HOUR" />
                  </el-select>
                </div>
              </el-form-item>
              <el-form-item v-if="mode === 2" label="消耗热量（千卡）" class="duo-item">
                <el-input-number v-model="form.caloriesBurned" :min="0" :controls="false" placeholder="千卡" class="w-full" />
              </el-form-item>
            </div>

            <el-form-item label="备注">
              <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="训练感受、备注..." />
            </el-form-item>

            <el-form-item class="form-actions">
              <el-button type="primary" size="large" :loading="saving" @click="onSubmit">
                <el-icon class="btn-icon"><CircleCheck /></el-icon>
                提交打卡
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </div>

      <!-- 打卡日历 -->
      <div class="col col-5">
        <el-card class="calendar-card" v-loading="loading.calendar">
          <template #header>
            <div class="card-title">月度打卡</div>
          </template>
          <TrainingCalendar
            v-model="calendarMonth"
            :checked-dates="checkedDates"
            @select="onSelectDay"
          />
        </el-card>
      </div>
    </div>

    <!-- 训练趋势 -->
    <div class="row">
      <div class="col col-6">
        <ChartCard
          ref="countChartRef"
          title="训练次数趋势"
          subtitle="近 30 天每日训练次数"
          height="260px"
          :loading="loading.stats"
          :empty="statsEmpty"
        />
      </div>
      <div class="col col-6">
        <ChartCard
          ref="trendChartRef"
          title="时长 / 热量趋势"
          subtitle="近 30 天训练时长与消耗热量"
          height="260px"
          :loading="loading.stats"
          :empty="statsEmpty"
        />
      </div>
    </div>

    <!-- 最近记录 -->
    <el-card>
      <template #header>
        <div class="card-title">最近记录</div>
      </template>
      <el-table :data="records.list" v-loading="records.loading">
        <el-table-column prop="trainDate" label="日期" width="120" />
        <el-table-column label="模式" width="90">
          <template #default="{ row }">
            <el-tag :type="row.mode === 2 ? 'primary' : 'info'" size="small" effect="light">
              {{ TRAINING_MODE[row.mode] || '-' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="训练类型" min-width="160">
          <template #default="{ row }">
            <el-tag v-for="s in row.sets" :key="s.id" size="small" effect="plain" class="tag-gap">
              {{ s.bodyPart }}
            </el-tag>
            <span v-if="!row.sets?.length" class="text-tertiary">-</span>
          </template>
        </el-table-column>
        <el-table-column label="时长" width="110">
          <template #default="{ row }">
            {{ formatDuration(row) }}
          </template>
        </el-table-column>
        <el-table-column prop="caloriesBurned" label="热量（千卡）" width="110">
          <template #default="{ row }">{{ row.caloriesBurned ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="80" align="right">
          <template #default="{ row }">
            <el-button link type="danger" @click="removeRecord(row)">
              <el-icon><Delete /></el-icon>
            </el-button>
          </template>
        </el-table-column>
        <template #empty>
          <EmptyState description="还没有训练记录" icon="Calendar" />
        </template>
      </el-table>
      <el-pagination
        class="pager"
        layout="prev, pager, next, total"
        :total="records.total"
        :page-size="records.size"
        :current-page="records.page"
        @current-change="records.onPageChange"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import StatCard from '@/components/StatCard.vue'
import ChartCard from '@/components/ChartCard.vue'
import EmptyState from '@/components/EmptyState.vue'
import TrainingCalendar from '@/components/TrainingCalendar.vue'
import { useList } from '@/composables/useList'
import { confirmDelete } from '@/composables/useConfirm'
import { today as todayStr } from '@/composables/useDate'
import {
  createTraining,
  deleteTraining,
  getStreak,
  getTrainingCalendar,
  getTrainingStats,
  listTraining,
} from '@/api/training'
import type { StreakVO, TrainingDailyStat, TrainingRecordVO, TrainingSetVO } from '@/types'
import { CHART_COLORS, TRAINING_MODE, TRAINING_TYPES } from '@/constants'

const modeOptions = [
  { label: '基础版', value: 1 },
  { label: '进阶版', value: 2 },
]

const mode = ref(1)
const saving = ref(false)

const form = reactive({
  trainDate: '',
  durationValue: undefined as number | undefined,
  durationUnit: 'MINUTE',
  caloriesBurned: undefined as number | undefined,
  remark: '',
})

const basicTypes = ref<string[]>([])
const advancedSets = ref<TrainingSetVO[]>([])

const streak = ref<StreakVO | null>(null)
const stats = ref<TrainingDailyStat[]>([])
const checkedDates = ref<string[]>([])
const calendarMonth = ref('')
const loading = reactive({ streak: true, calendar: true, stats: true })

const countChartRef = ref<InstanceType<typeof ChartCard>>()
const trendChartRef = ref<InstanceType<typeof ChartCard>>()

const today = todayStr()
const todayChecked = computed(() => checkedDates.value.includes(today))
const statsEmpty = computed(() => stats.value.every((s) => s.recordCount === 0))

const records = useList<TrainingRecordVO>((p) => listTraining(p), { size: 10 })

function addSet() {
  advancedSets.value.push({ bodyPart: '', exerciseName: '', weight: undefined, reps: undefined, sets: undefined })
}

function formatDuration(r: TrainingRecordVO): string {
  const v = r.durationValue
  if (v === null || v === undefined) return '-'
  return r.durationUnit === 'HOUR' ? `${v} 小时` : `${v} 分钟`
}

async function loadStreak() {
  try {
    streak.value = await getStreak()
  } catch {
    streak.value = null
  } finally {
    loading.streak = false
  }
}

async function loadCalendar() {
  if (!calendarMonth.value) return
  loading.calendar = true
  const [y, m] = calendarMonth.value.split('-').map(Number)
  try {
    checkedDates.value = await getTrainingCalendar(y, m)
  } catch {
    checkedDates.value = []
  } finally {
    loading.calendar = false
  }
}

async function loadStats() {
  try {
    stats.value = await getTrainingStats(30)
  } catch {
    stats.value = []
  } finally {
    loading.stats = false
    renderCharts()
  }
}

function renderCharts() {
  const dates = stats.value.map((s) => s.trainDate.slice(5))
  countChartRef.value?.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 32, right: 16, top: 16, bottom: 26 },
    xAxis: { type: 'category', data: dates, axisLine: { lineStyle: { color: '#e5e7eb' } }, axisTick: { show: false } },
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

  const calories = stats.value.map((s) => s.totalCalories)
  const durations = stats.value.map((s) => Math.round(s.totalDurationMinutes))
  trendChartRef.value?.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['消耗热量(千卡)', '训练时长(分钟)'], bottom: 0, icon: 'circle' },
    grid: { left: 40, right: 40, top: 16, bottom: 30 },
    xAxis: { type: 'category', data: dates, axisLine: { lineStyle: { color: '#e5e7eb' } }, axisTick: { show: false } },
    yAxis: [
      { type: 'value', name: '千卡', splitLine: { lineStyle: { color: '#f3f4f6' } } },
      { type: 'value', name: '分钟', splitLine: { show: false } },
    ],
    series: [
      { name: '消耗热量(千卡)', type: 'line', data: calories, smooth: true, symbolSize: 4, lineStyle: { width: 2, color: CHART_COLORS.amber }, itemStyle: { color: CHART_COLORS.amber }, connectNulls: true },
      { name: '训练时长(分钟)', type: 'line', yAxisIndex: 1, data: durations, smooth: true, symbolSize: 4, lineStyle: { width: 2, color: CHART_COLORS.blue }, itemStyle: { color: CHART_COLORS.blue }, connectNulls: true },
    ],
  })
}

function onSelectDay(date: string) {
  form.trainDate = date
}

async function onSubmit() {
  // ── 提交 payload 构造逻辑与改造前完全一致 ──
  if (!form.trainDate) {
    ElMessage.warning('请选择训练日期')
    return
  }
  const sets: TrainingSetVO[] = mode.value === 1
    ? basicTypes.value.map((t) => ({ bodyPart: t }))
    : advancedSets.value.filter((s) => s.bodyPart)
  if (sets.length === 0) {
    ElMessage.warning(mode.value === 1 ? '请至少选择一个训练类型' : '请至少添加一个动作')
    return
  }
  saving.value = true
  try {
    await createTraining({
      trainDate: form.trainDate,
      mode: mode.value,
      durationValue: form.durationValue,
      durationUnit: form.durationUnit,
      caloriesBurned: mode.value === 2 ? form.caloriesBurned : undefined,
      remark: form.remark || undefined,
      sets,
    })
    ElMessage.success('打卡成功')
    form.trainDate = todayStr()
    form.durationValue = undefined
    form.caloriesBurned = undefined
    form.remark = ''
    basicTypes.value = []
    advancedSets.value = []
    refreshAll()
  } catch {
    // 拦截器已提示
  } finally {
    saving.value = false
  }
}

async function removeRecord(row: TrainingRecordVO) {
  const ok = await confirmDelete('确定删除这条训练记录吗？删除后相关统计会同步更新。')
  if (!ok) return
  try {
    await deleteTraining(row.id)
    ElMessage.success('删除成功')
    refreshAll()
  } catch {
    // 拦截器已提示
  }
}

function refreshAll() {
  records.load()
  loadStreak()
  loadCalendar()
  loadStats()
}

onMounted(() => {
  form.trainDate = todayStr()
  const d = new Date()
  calendarMonth.value = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
  records.load()
  loadStreak()
  loadCalendar()
  loadStats()
})
</script>

<style scoped>
.row {
  display: grid;
  grid-template-columns: repeat(12, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}
.col-7 { grid-column: span 7; }
.col-6 { grid-column: span 6; }
.col-5 { grid-column: span 5; }
@media (max-width: 1023px) {
  .col-7,
  .col-6,
  .col-5 {
    grid-column: span 12;
  }
}

.card-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: var(--fth-text-lg);
  font-weight: 600;
}
.title-icon {
  color: var(--fth-primary);
}

/* 训练类型选择 */
.type-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 4px 20px;
}
.type-check {
  margin-right: 0;
}

/* 进阶版动作组 */
.sets-block {
  margin-bottom: 18px;
}
.sets-title {
  color: var(--fth-text-secondary);
  font-size: var(--fth-text-sm);
  margin-bottom: 8px;
}
.set-row {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 8px;
  flex-wrap: wrap;
}
.set-part { width: 84px; }
.set-name { width: 150px; }
.set-num { width: 92px; }
.btn-icon {
  margin-right: 4px;
}

.form-duo {
  display: flex;
  gap: 16px;
}
.duo-item {
  flex: 1;
  min-width: 0;
}
.duration {
  display: flex;
  gap: 8px;
}
.dur-num { flex: 1; }
.dur-unit { width: 100px; }
.w-full { width: 100%; }
.form-actions {
  margin-bottom: 0;
}

.tag-gap {
  margin-right: 4px;
}
.pager {
  justify-content: flex-end;
  margin-top: 12px;
}
</style>
