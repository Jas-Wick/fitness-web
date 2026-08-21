<template>
  <div class="page">
    <PageHeader title="AI 健身助手" subtitle="结合你的真实数据，给出个性化健身建议" />

    <div class="ai-layout">
      <!-- 左侧：介绍 + 快捷操作 -->
      <div class="ai-side">
        <el-card class="ai-intro">
          <div class="ai-brand">
            <div class="ai-logo">
              <el-icon :size="20"><MagicStick /></el-icon>
            </div>
            <div>
              <div class="ai-name">AI 健身助手</div>
              <div class="ai-sub">GLM 驱动 · 数据联动</div>
            </div>
          </div>
          <div class="ai-desc">基于你的训练记录、身体数据与目标，提供训练计划、饮食分析与数据复盘建议。</div>
          <div class="ai-tags">
            <el-tag size="small" effect="light">训练计划</el-tag>
            <el-tag size="small" effect="light" type="warning">饮食分析</el-tag>
            <el-tag size="small" effect="light" type="info">数据复盘</el-tag>
          </div>
          <div class="ai-notice">
            <el-icon><InfoFilled /></el-icon>
            未配置 API Key 时 AI 服务暂不可用
          </div>
        </el-card>

        <el-card class="ai-actions">
          <div class="ai-actions-title">快捷操作</div>
          <button
            v-for="act in quickActions"
            :key="act.key"
            class="ai-action"
            :disabled="acting === act.key || sending"
            @click="runAction(act.key)"
          >
            <el-icon class="ai-action-icon"><component :is="act.icon" /></el-icon>
            <span>{{ act.label }}</span>
            <el-icon v-if="acting === act.key" class="spin"><Loading /></el-icon>
          </button>
        </el-card>
      </div>

      <!-- 右侧：聊天区 -->
      <el-card class="chat-card">
        <div ref="listRef" class="msg-list">
          <div v-if="messages.length === 0" class="chat-empty">
            <div class="chat-empty-icon">
              <el-icon :size="34"><ChatDotRound /></el-icon>
            </div>
            <div class="chat-empty-text">向 AI 助手提问你的健身问题</div>
            <div class="chat-empty-chips">
              <el-tag v-for="q in quickQuestions" :key="q" effect="plain" class="chip" @click="ask(q)">
                {{ q }}
              </el-tag>
            </div>
          </div>

          <div v-for="(m, i) in messages" :key="i" class="msg" :class="m.role">
            <div v-if="m.role === 'assistant'" class="msg-avatar">
              <el-icon :size="14"><MagicStick /></el-icon>
            </div>
            <div class="bubble">
              <MarkdownText v-if="m.role === 'assistant'" :content="m.content" />
              <span v-else class="user-text">{{ m.content }}</span>
            </div>
          </div>

          <div v-if="sending" class="msg assistant">
            <div class="msg-avatar">
              <el-icon :size="14"><MagicStick /></el-icon>
            </div>
            <div class="bubble typing">
              <span class="dot" />
              <span class="dot" />
              <span class="dot" />
            </div>
          </div>
        </div>

        <div class="chat-input">
          <el-input
            v-model="question"
            type="textarea"
            :rows="2"
            resize="none"
            placeholder="输入你的问题，回车发送（Shift+Enter 换行）..."
            :disabled="sending"
            @keydown.enter.exact.prevent="send"
          />
          <el-button type="primary" class="send-btn" :loading="sending" @click="send">
            <el-icon v-if="!sending" class="btn-icon"><Promotion /></el-icon>
            发送
          </el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, ref } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import MarkdownText from '@/components/MarkdownText.vue'
import { useAuthStore } from '@/stores/auth'
import { aiChat, aiDietAnalysis, aiTrainingPlan } from '@/api/ai'
import { getFoodStat } from '@/api/food'
import { getStreak, getTrainingCalendar, getTrainingStats, listTraining } from '@/api/training'
import type { UserVO } from '@/types'
import { today } from '@/composables/useDate'

interface Msg {
  role: 'user' | 'assistant'
  content: string
}

const auth = useAuthStore()
const messages = ref<Msg[]>([])
const question = ref('')
const sending = ref(false)
const acting = ref<string | null>(null)
const listRef = ref<HTMLElement>()

const quickActions = [
  { key: 'plan', label: '制定训练计划', icon: 'Calendar', desc: '根据身体情况生成 7 天计划' },
  { key: 'recent', label: '分析近期训练', icon: 'TrendCharts' },
  { key: 'diet', label: '今日饮食建议', icon: 'Dish' },
  { key: 'today', label: '今日训练建议', icon: 'Timer' },
  { key: 'summary', label: '查看训练总结', icon: 'Histogram' },
]

const quickQuestions = [
  '如何制定一份减脂期训练计划？',
  '今天该练什么？',
  '蛋白质每天应该摄入多少？',
]

async function scrollToBottom() {
  await nextTick()
  listRef.value?.scrollTo({ top: listRef.value.scrollHeight, behavior: 'smooth' })
}

function pushUser(text: string) {
  messages.value.push({ role: 'user', content: text })
}

function pushAssistant(text: string) {
  messages.value.push({ role: 'assistant', content: text })
}

async function send() {
  const q = question.value.trim()
  if (!q || sending.value) return
  pushUser(q)
  question.value = ''
  sending.value = true
  await scrollToBottom()
  try {
    const reply = await aiChat(q)
    pushAssistant(reply)
  } catch {
    pushAssistant('抱歉，暂时无法获取 AI 回复。请确认 API Key 已配置后重试。')
  } finally {
    sending.value = false
    await scrollToBottom()
  }
}

function ask(q: string) {
  question.value = q
  send()
}

function buildProfile(u: UserVO | null): string {
  if (!u) return '请补充身高、体重、目标等信息。'
  return [
    `身高：${u.height ?? '未知'}cm`,
    `体重：${u.weight ?? '未知'}kg`,
    `健身目标：${u.fitnessGoal ?? '未知'}`,
    `健身等级：${u.fitnessLevel ?? '未知'}`,
    `年龄：${u.age ?? '未知'}`,
  ].join('，')
}

async function recentContext(): Promise<string> {
  try {
    const [stats, streak] = await Promise.all([getTrainingStats(7), getStreak()])
    const count = stats.reduce((s, x) => s + x.recordCount, 0)
    const dur = stats.reduce((s, x) => s + x.totalDurationMinutes, 0)
    const cal = stats.reduce((s, x) => s + x.totalCalories, 0)
    return `最近一周训练 ${count} 次、共 ${dur} 分钟、消耗 ${cal} 千卡，当前连续打卡 ${streak.currentStreak} 天。`
  } catch {
    return '最近一周暂无训练数据。'
  }
}

async function todayContext(): Promise<string> {
  const t = today()
  const d = new Date()
  try {
    const dates = await getTrainingCalendar(d.getFullYear(), d.getMonth() + 1)
    if (!dates.includes(t)) return '今天还没有训练记录。'
    const page = await listTraining({ page: 1, size: 10 })
    const rec = page.records.find((r) => r.trainDate === t)
    if (!rec) return '今天还没有训练记录。'
    const types = (rec.sets ?? []).map((s) => s.bodyPart).filter(Boolean).join('、') || '未填写类型'
    const dur = rec.durationValue
    return `今天已完成训练：${types}，时长 ${dur ?? '-'}${rec.durationUnit === 'HOUR' ? ' 小时' : ' 分钟'}。`
  } catch {
    return '今天还没有训练记录。'
  }
}

async function summaryContext(): Promise<string> {
  try {
    const [stats, streak] = await Promise.all([getTrainingStats(30), getStreak()])
    const count = stats.reduce((s, x) => s + x.recordCount, 0)
    const dur = stats.reduce((s, x) => s + x.totalDurationMinutes, 0)
    const cal = stats.reduce((s, x) => s + x.totalCalories, 0)
    return `近 30 天训练 ${count} 次、共 ${dur} 分钟、消耗 ${cal} 千卡；当前连续打卡 ${streak.currentStreak} 天，累计 ${streak.totalDays} 天。`
  } catch {
    return '暂无训练数据。'
  }
}

async function runAction(key: string) {
  if (acting.value || sending.value) return
  acting.value = key

  if (key === 'plan') {
    pushUser('请根据我的身体情况生成一份 7 天训练计划。')
    try {
      const reply = await aiTrainingPlan(buildProfile(auth.user))
      pushAssistant(reply)
    } catch {
      pushAssistant('抱歉，无法生成训练计划。请确认 API Key 已配置后重试。')
    }
  } else if (key === 'recent') {
    pushUser('请分析我最近一周的训练情况，并给出改进建议。')
    try {
      const ctx = await recentContext()
      const reply = await aiChat(`请分析我最近一周的训练情况并给出可执行的改进建议。我的情况：${ctx}`)
      pushAssistant(reply)
    } catch {
      pushAssistant('抱歉，暂时无法获取分析结果。请确认 API Key 已配置后重试。')
    }
  } else if (key === 'diet') {
    const d = today()
    let summary = ''
    try {
      const stat = await getFoodStat(d, d)
      summary = `今日摄入：热量 ${stat.totalCalories} 千卡，蛋白质 ${stat.totalProtein}g，碳水 ${stat.totalCarbs}g，脂肪 ${stat.totalFat}g。`
    } catch {
      summary = '今日暂无饮食记录。'
    }
    pushUser('请分析我的饮食营养结构，并给出调整建议。')
    try {
      const reply = await aiDietAnalysis(summary)
      pushAssistant(reply)
    } catch {
      pushAssistant('抱歉，暂时无法获取饮食建议。请确认 API Key 已配置后重试。')
    }
  } else if (key === 'today') {
    pushUser('请根据我今天的情况给出训练建议。')
    try {
      const ctx = await todayContext()
      const reply = await aiChat(`请根据我今天的情况给出训练建议。我的情况：${ctx}`)
      pushAssistant(reply)
    } catch {
      pushAssistant('抱歉，暂时无法获取训练建议。请确认 API Key 已配置后重试。')
    }
  } else if (key === 'summary') {
    pushUser('请总结我近期的训练情况，并给出下一步方向。')
    try {
      const ctx = await summaryContext()
      const reply = await aiChat(`请总结我近期的训练情况，并给出下一步训练方向。我的情况：${ctx}`)
      pushAssistant(reply)
    } catch {
      pushAssistant('抱歉，暂时无法生成训练总结。请确认 API Key 已配置后重试。')
    }
  }

  acting.value = null
  await scrollToBottom()
}

onMounted(() => {
  if (!auth.user) auth.fetchProfile()
})
</script>

<style scoped>
.ai-layout {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 16px;
  align-items: start;
}
@media (max-width: 1023px) {
  .ai-layout {
    grid-template-columns: 1fr;
  }
}

.ai-side {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.ai-intro .ai-brand {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}
.ai-logo {
  width: 40px;
  height: 40px;
  border-radius: var(--fth-radius);
  background: var(--fth-primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.ai-name {
  font-size: var(--fth-text-lg);
  font-weight: 600;
  color: var(--fth-text-primary);
}
.ai-sub {
  font-size: var(--fth-text-xs);
  color: var(--fth-text-tertiary);
}
.ai-desc {
  font-size: var(--fth-text-sm);
  color: var(--fth-text-secondary);
  line-height: 1.7;
  margin-bottom: 10px;
}
.ai-tags {
  display: flex;
  gap: 6px;
  margin-bottom: 10px;
}
.ai-notice {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: var(--fth-text-xs);
  color: var(--fth-warning);
  background: var(--fth-warning-soft);
  border-radius: var(--fth-radius-sm);
  padding: 6px 10px;
}

.ai-actions-title {
  font-size: var(--fth-text-base);
  font-weight: 600;
  margin-bottom: 8px;
  color: var(--fth-text-primary);
}
.ai-action {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 10px 12px;
  margin-bottom: 6px;
  border: 1px solid var(--fth-border-soft);
  border-radius: var(--fth-radius);
  background: var(--fth-bg-card);
  color: var(--fth-text-primary);
  font-size: var(--fth-text-sm);
  cursor: pointer;
  transition: background 0.12s ease, border-color 0.12s ease;
}
.ai-action:hover:not(:disabled) {
  background: var(--fth-primary-softer);
  border-color: var(--fth-primary-light-5);
}
.ai-action:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.ai-action-icon {
  color: var(--fth-primary);
  font-size: 16px;
}
.spin {
  margin-left: auto;
  animation: spin 1s linear infinite;
}
@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* 聊天区 */
.chat-card {
  display: flex;
  flex-direction: column;
  padding-bottom: 0;
}
.msg-list {
  height: 480px;
  overflow-y: auto;
  padding: 4px 4px 12px;
}
.chat-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60px 16px;
  text-align: center;
}
.chat-empty-icon {
  color: var(--fth-text-tertiary);
  margin-bottom: 10px;
}
.chat-empty-text {
  color: var(--fth-text-secondary);
  font-size: var(--fth-text-base);
  margin-bottom: 16px;
}
.chat-empty-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  justify-content: center;
  max-width: 400px;
}
.chip {
  cursor: pointer;
  user-select: none;
  border-color: var(--fth-primary-light-5);
  color: var(--fth-primary-hover);
}
.chip:hover {
  background: var(--fth-primary-soft);
}

.msg {
  margin-bottom: 14px;
  display: flex;
  gap: 8px;
  align-items: flex-start;
}
.msg.user {
  justify-content: flex-end;
}
.msg.assistant {
  justify-content: flex-start;
}
.msg-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: var(--fth-primary-soft);
  color: var(--fth-primary-hover);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  margin-top: 2px;
}
.bubble {
  max-width: 78%;
  padding: 10px 14px;
  border-radius: var(--fth-radius-md);
  line-height: 1.65;
  font-size: var(--fth-text-base);
}
.msg.user .bubble {
  background: var(--fth-primary);
  color: #fff;
  border-bottom-right-radius: 4px;
}
.msg.assistant .bubble {
  background: var(--fth-bg-card);
  border: 1px solid var(--fth-border-soft);
  border-bottom-left-radius: 4px;
}
.user-text {
  white-space: pre-wrap;
}
.typing {
  display: flex;
  gap: 5px;
  align-items: center;
}
.typing .dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--fth-text-tertiary);
  animation: bounce 1.2s infinite ease-in-out;
}
.typing .dot:nth-child(2) {
  animation-delay: 0.15s;
}
.typing .dot:nth-child(3) {
  animation-delay: 0.3s;
}
@keyframes bounce {
  0%,
  60%,
  100% {
    transform: translateY(0);
    opacity: 0.5;
  }
  30% {
    transform: translateY(-4px);
    opacity: 1;
  }
}

.chat-input {
  display: flex;
  gap: 10px;
  align-items: flex-end;
  padding-top: 12px;
  border-top: 1px solid var(--fth-border-soft);
}
.send-btn {
  height: 56px;
}
.btn-icon {
  margin-right: 4px;
}
</style>
