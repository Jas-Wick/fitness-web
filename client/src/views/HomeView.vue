<template>
  <div class="page">
    <!-- 欢迎区 -->
    <div class="welcome">
      <div class="welcome-main">
        <div class="welcome-greet">{{ greeting() }}，{{ displayName }} 👋</div>
        <div class="welcome-sub">
          {{ zhDate() }}
          <span v-if="auth.user?.fitnessGoal" class="welcome-goal">
            <el-icon><Aim /></el-icon>
            当前目标 · {{ auth.user.fitnessGoal }}
          </span>
        </div>
      </div>
      <div class="welcome-actions">
        <el-button type="primary" size="large" @click="router.push('/training')">
          <el-icon class="btn-icon"><Calendar /></el-icon>
          今日训练
        </el-button>
      </div>
    </div>

    <!-- 核心数据卡 -->
    <div class="stat-grid">
      <StatCard
        label="连续打卡"
        :value="streak?.currentStreak"
        unit="天"
        icon="AlarmClock"
        tone="primary"
        :loading="loading.streak"
      >
        最长连续 {{ streak?.longestStreak ?? '-' }} 天
      </StatCard>
      <StatCard
        label="本周训练"
        :value="week.count"
        unit="次"
        icon="Calendar"
        tone="blue"
        :loading="loading.streak"
      >
        坚持就是胜利
      </StatCard>
      <StatCard
        label="本周训练时长"
        :value="week.duration"
        unit="分钟"
        icon="Timer"
        tone="purple"
        :loading="loading.stats"
      >
        {{ Math.round(week.duration / 60) }} 小时
      </StatCard>
      <StatCard
        label="本周消耗热量"
        :value="week.calories"
        unit="千卡"
        icon="Lightning"
        tone="warning"
        :loading="loading.stats"
      >
        {{ week.count }} 次训练
      </StatCard>
    </div>

    <!-- 今日训练 + 本周趋势 -->
    <div class="row">
      <div class="col col-4">
        <el-card class="today-card">
          <template #header>
            <div class="card-title">今日训练</div>
          </template>
          <div v-if="todayRecord" class="today-body">
            <div class="today-done">
              <div class="today-types">
                <el-tag v-for="t in todayTypes" :key="t" size="small" class="type-tag">{{ t }}</el-tag>
              </div>
              <div class="today-meta">
                <span>{{ formatDuration(todayRecord) }}</span>
                <span class="dot-sep">·</span>
                <span>{{ todayRecord.caloriesBurned ?? '-' }} 千卡</span>
              </div>
            </div>
            <div class="today-foot">
              <el-button type="primary" plain size="small" @click="router.push('/training')">
                查看打卡详情
              </el-button>
            </div>
          </div>
          <div v-else class="today-empty">
            <el-icon :size="34" class="today-empty-icon"><Calendar /></el-icon>
            <div class="today-empty-text">今天还没有训练记录</div>
            <el-button type="primary" size="small" @click="router.push('/training')">
              快速开始
            </el-button>
          </div>
        </el-card>
      </div>

      <div class="col col-8">
        <ChartCard
          ref="weekChartRef"
          title="本周训练趋势"
          subtitle="近 7 天每日训练次数"
          height="240px"
          :loading="loading.stats"
          :empty="weekChartEmpty"
        />
      </div>
    </div>

    <!-- 体重趋势 + 今日饮食 -->
    <div class="row">
      <div class="col col-8">
        <ChartCard
          ref="weightChartRef"
          title="体重趋势"
          subtitle="近期体重变化（kg）"
          height="240px"
          :loading="loading.weight"
          :empty="weightChartEmpty"
        />
      </div>

      <div class="col col-4">
        <el-card class="diet-card">
          <template #header>
            <div class="card-title">今日饮食</div>
          </template>
          <div class="diet-calories">
            <span class="diet-cal-value">{{ foodStat?.totalCalories ?? 0 }}</span>
            <span class="diet-cal-unit">千卡</span>
            <span class="diet-cal-tip">今日摄入</span>
          </div>
          <el-divider class="diet-divider" />
          <MacroProgress
            :total-protein="foodStat?.totalProtein"
            :total-carbs="foodStat?.totalCarbs"
            :total-fat="foodStat?.totalFat"
          />
          <div class="diet-foot">
            <el-button text type="primary" @click="router.push('/food')">记录饮食 →</el-button>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 最近训练记录 + AI 建议 -->
    <div class="row">
      <div class="col col-8">
        <el-card>
          <template #header>
            <div class="card-title">最近训练记录</div>
          </template>
          <div v-if="recentRecords.length" class="recent-list">
            <div v-for="r in recentRecords" :key="r.id" class="recent-item" @click="router.push('/training')">
              <div class="recent-date">
                <div class="recent-day">{{ Number(r.trainDate.slice(8)) }}</div>
                <div class="recent-mon">{{ r.trainDate.slice(5, 7) }}月</div>
              </div>
              <div class="recent-info">
                <div class="recent-types">
                  <el-tag size="small" :type="r.mode === 2 ? 'primary' : 'info'" effect="light">
                    {{ TRAINING_MODE[r.mode] || '训练' }}
                  </el-tag>
                  <span v-for="t in recordTypes(r)" :key="t" class="recent-type-text">{{ t }}</span>
                </div>
                <div class="recent-meta">{{ formatTime(r.trainDate) }}</div>
              </div>
              <div class="recent-stats">
                <span>{{ formatDuration(r) }}</span>
                <span class="dot-sep">·</span>
                <span>{{ r.caloriesBurned ?? '-' }} 千卡</span>
              </div>
            </div>
          </div>
          <div v-else-if="loading.records">
            <el-skeleton :rows="3" animated />
          </div>
          <div v-else>
            <EmptyState description="还没有训练记录" icon="Calendar">
              <template #action>
                <el-button type="primary" size="small" @click="router.push('/training')">去打卡</el-button>
              </template>
            </EmptyState>
          </div>
        </el-card>
      </div>

      <div class="col col-4">
        <el-card class="ai-card">
          <template #header>
            <div class="card-title">
              <el-icon class="ai-title-icon"><MagicStick /></el-icon>
              AI 健身建议
            </div>
          </template>
          <div v-if="aiLoading" class="ai-loading">
            <el-skeleton :rows="4" animated />
          </div>
          <template v-else-if="aiAdvice">
            <MarkdownText :content="aiAdvice" />
            <div class="ai-foot">
              <el-button text type="primary" @click="router.push('/ai')">与 AI 继续对话 →</el-button>
            </div>
          </template>
          <div v-else class="ai-empty">
            <el-icon :size="30" class="ai-empty-icon"><MagicStick /></el-icon>
            <div class="ai-empty-text">暂无法获取 AI 建议</div>
            <el-button text type="primary" @click="router.push('/ai')">前往 AI 健身助手</el-button>
          </div>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { getStreak, getTrainingCalendar, getTrainingStats, listTraining } from '@/api/training'
import { getBodyTrend } from '@/api/body'
import { getFoodStat } from '@/api/food'
import { aiChat } from '@/api/ai'
import type { BodyDataVO, FoodStatVO, StreakVO, TrainingDailyStat, TrainingRecordVO } from '@/types'
import { CHART_COLORS, TRAINING_MODE } from '@/constants'
import { greeting, today, zhDate, formatTime } from '@/composables/useDate'
import StatCard from '@/components/StatCard.vue'
import ChartCard from '@/components/ChartCard.vue'
import EmptyState from '@/components/EmptyState.vue'
import MacroProgress from '@/components/MacroProgress.vue'
import MarkdownText from '@/components/MarkdownText.vue'

const auth = useAuthStore()
const router = useRouter()

const displayName = computed(() => auth.user?.nickname || auth.user?.username || '朋友')

const loading = reactive({
  streak: true,
  stats: true,
  weight: true,
  records: true,
})
const streak = ref<StreakVO | null>(null)
const week = ref({ count: 0, duration: 0, calories: 0 })
const weekStats = ref<TrainingDailyStat[]>([])
const todayRecord = ref<TrainingRecordVO | null>(null)
const recentRecords = ref<TrainingRecordVO[]>([])
const weightData = ref<BodyDataVO[]>([])
const foodStat = ref<FoodStatVO | null>(null)
const aiAdvice = ref('')
const aiLoading = ref(false)

const weekChartRef = ref<InstanceType<typeof ChartCard>>()
const weightChartRef = ref<InstanceType<typeof ChartCard>>()

const todayTypes = computed(() => todayRecord.value?.sets?.map((s) => s.bodyPart).filter(Boolean) ?? [])
const weekChartEmpty = computed(() => weekStats.value.every((s) => s.recordCount === 0))
const weightChartEmpty = computed(() => weightData.value.length === 0)

function recordTypes(r: TrainingRecordVO): string[] {
  return r.sets?.map((s) => s.bodyPart).filter(Boolean) ?? []
}

function formatDuration(r: TrainingRecordVO): string {
  const v = r.durationValue
  if (v === null || v === undefined) return '-'
  return r.durationUnit === 'HOUR' ? `${v} 小时` : `${v} 分钟`
}

function renderWeekChart() {
  const dates = weekStats.value.map((s) => s.trainDate.slice(5))
  weekChartRef.value?.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 32, right: 16, top: 16, bottom: 26 },
    xAxis: { type: 'category', data: dates, axisLine: { lineStyle: { color: '#e5e7eb' } }, axisTick: { show: false } },
    yAxis: { type: 'value', minInterval: 1, splitLine: { lineStyle: { color: '#f3f4f6' } } },
    series: [
      {
        name: '训练次数',
        type: 'bar',
        data: weekStats.value.map((s) => s.recordCount),
        itemStyle: { color: CHART_COLORS.primary, borderRadius: [4, 4, 0, 0] },
        barMaxWidth: 24,
      },
    ],
  })
}

function renderWeightChart() {
  const dates = weightData.value.map((d) => d.recordDate.slice(5))
  weightChartRef.value?.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['体重(kg)'], bottom: 0, icon: 'circle' },
    grid: { left: 36, right: 36, top: 16, bottom: 28 },
    xAxis: { type: 'category', data: dates, axisLine: { lineStyle: { color: '#e5e7eb' } }, axisTick: { show: false } },
    yAxis: [
      { type: 'value', name: 'kg', splitLine: { lineStyle: { color: '#f3f4f6' } } },
      {
        type: 'value',
        name: '%',
        splitLine: { show: false },
        axisLabel: { formatter: '{value}%' },
      },
    ],
    series: [
      {
        name: '体重(kg)',
        type: 'line',
        data: weightData.value.map((d) => Number(d.weight ?? null)),
        smooth: true,
        symbolSize: 6,
        lineStyle: { color: CHART_COLORS.primary, width: 2 },
        itemStyle: { color: CHART_COLORS.primary },
        areaStyle: { color: 'rgba(249, 115, 22, 0.08)' },
      },
      {
        name: '体脂率(%)',
        type: 'line',
        yAxisIndex: 1,
        data: weightData.value.map((d) => Number(d.bodyFatRate ?? null)),
        smooth: true,
        symbolSize: 6,
        lineStyle: { color: CHART_COLORS.purple, width: 2 },
        itemStyle: { color: CHART_COLORS.purple },
        connectNulls: true,
      },
    ],
  })
}

async function loadAll() {
  const t = today()
  const d = new Date()
  const monthStr = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`

  // 连续打卡
  try {
    streak.value = await getStreak()
  } catch {
    streak.value = null
  } finally {
    loading.streak = false
  }

  // 本周统计 + 趋势
  try {
    weekStats.value = await getTrainingStats(7)
    const count = weekStats.value.reduce((s, x) => s + x.recordCount, 0)
    const duration = weekStats.value.reduce((s, x) => s + x.totalDurationMinutes, 0)
    const calories = weekStats.value.reduce((s, x) => s + x.totalCalories, 0)
    week.value = { count, duration, calories }
  } catch {
    weekStats.value = []
  } finally {
    loading.stats = false
    renderWeekChart()
  }

  // 体重趋势
  try {
    weightData.value = await getBodyTrend()
  } catch {
    weightData.value = []
  } finally {
    loading.weight = false
    renderWeightChart()
  }

  // 今日训练 + 最近记录（并行）
  try {
    const [calendar, page] = await Promise.all([
      getTrainingCalendar(d.getFullYear(), d.getMonth() + 1),
      listTraining({ page: 1, size: 10 }),
    ])
    recentRecords.value = page.records
    if (calendar.includes(t)) {
      todayRecord.value = page.records.find((r) => r.trainDate === t) ?? null
    }
  } catch {
    recentRecords.value = []
  } finally {
    loading.records = false
  }

  // 今日饮食
  try {
    foodStat.value = await getFoodStat(t, t)
  } catch {
    foodStat.value = null
  }

  // AI 建议（真实统计拼 prompt，失败优雅降级）
  await loadAdvice()
}

async function loadAdvice() {
  if (aiAdvice.value || aiLoading.value) return
  aiLoading.value = true
  try {
    const summary = [
      streak.value ? `当前连续打卡 ${streak.value.currentStreak} 天` : '',
      week.value.count ? `本周已训练 ${week.value.count} 次、${week.value.duration} 分钟` : '本周还没有训练',
      auth.user?.fitnessGoal ? `当前目标是${auth.user.fitnessGoal}` : '',
    ].filter(Boolean).join('，')
    const reply = await aiChat(`请根据我的真实情况给出一条简短（120 字以内）可执行的健身建议。我的情况：${summary}。`)
    aiAdvice.value = reply.trim()
  } catch {
    aiAdvice.value = ''
  } finally {
    aiLoading.value = false
  }
}

onMounted(() => {
  if (!auth.user) auth.fetchProfile()
  loadAll()
})
</script>

<style scoped>
.welcome {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
  background: var(--fth-sidebar-bg);
  border-radius: var(--fth-radius-md);
  padding: 24px 28px;
  margin-bottom: 20px;
}
.welcome-greet {
  font-size: var(--fth-text-3xl);
  font-weight: 700;
  color: #fff;
}
.welcome-sub {
  margin-top: 6px;
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: var(--fth-text-sm);
  color: var(--fth-sidebar-text);
}
.welcome-goal {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  color: var(--fth-sidebar-active-text);
}
.btn-icon {
  margin-right: 4px;
}

.row {
  display: grid;
  grid-template-columns: repeat(12, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}
.col-4 { grid-column: span 4; }
.col-8 { grid-column: span 8; }
@media (max-width: 1023px) {
  .col-4,
  .col-8 {
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

/* 今日训练 */
.today-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px 0;
  gap: 10px;
}
.today-empty-icon {
  color: var(--fth-text-tertiary);
}
.today-empty-text {
  color: var(--fth-text-secondary);
  font-size: var(--fth-text-sm);
}
.today-types {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 8px;
}
.type-tag {
  background: var(--fth-primary-soft);
  color: var(--fth-primary-hover);
  border: none;
}
.today-meta {
  font-size: var(--fth-text-sm);
  color: var(--fth-text-secondary);
}
/* 侧边卡片内部弹性列，footer 贴底，保证同行卡片等高时内容不散开 */
.today-card :deep(.el-card__body),
.diet-card :deep(.el-card__body),
.ai-card :deep(.el-card__body) {
  display: flex;
  flex-direction: column;
}
.today-foot {
  margin-top: auto;
  padding-top: 16px;
}
.dot-sep {
  margin: 0 6px;
  color: var(--fth-text-tertiary);
}

/* 今日饮食 */
.diet-calories {
  display: flex;
  align-items: baseline;
  gap: 6px;
}
.diet-cal-value {
  font-size: 34px;
  font-weight: 700;
  color: var(--fth-primary);
  font-variant-numeric: tabular-nums;
}
.diet-cal-unit {
  font-size: var(--fth-text-sm);
  color: var(--fth-text-secondary);
}
.diet-cal-tip {
  margin-left: auto;
  font-size: var(--fth-text-xs);
  color: var(--fth-text-tertiary);
}
.diet-divider {
  margin: 12px 0;
}
.diet-foot {
  margin-top: auto;
  padding-top: 12px;
  text-align: right;
}

/* 最近记录 */
.recent-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 4px;
  border-bottom: 1px solid var(--fth-border-soft);
  cursor: pointer;
  border-radius: var(--fth-radius-sm);
  transition: background 0.12s ease;
}
.recent-item:last-child {
  border-bottom: none;
}
.recent-item:hover {
  background: var(--fth-primary-softer);
}
.recent-date {
  width: 42px;
  height: 42px;
  border-radius: var(--fth-radius-sm);
  background: var(--fth-bg-muted);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.recent-day {
  font-size: var(--fth-text-lg);
  font-weight: 700;
  line-height: 1.1;
  color: var(--fth-text-primary);
}
.recent-mon {
  font-size: var(--fth-text-xs);
  color: var(--fth-text-tertiary);
}
.recent-info {
  flex: 1;
  min-width: 0;
}
.recent-types {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}
.recent-type-text {
  font-size: var(--fth-text-sm);
  color: var(--fth-text-primary);
}
.recent-meta {
  margin-top: 2px;
  font-size: var(--fth-text-xs);
  color: var(--fth-text-tertiary);
}
.recent-stats {
  font-size: var(--fth-text-sm);
  color: var(--fth-text-secondary);
  white-space: nowrap;
}

/* AI 建议 */
.ai-title-icon {
  color: var(--fth-primary);
}
.ai-foot {
  margin-top: auto;
  padding-top: 14px;
}
.ai-loading {
  padding: 4px 0;
}
.ai-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 24px 0;
  gap: 8px;
}
.ai-empty-icon {
  color: var(--fth-text-tertiary);
}
.ai-empty-text {
  color: var(--fth-text-secondary);
  font-size: var(--fth-text-sm);
}
</style>
