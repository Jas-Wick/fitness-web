<template>
  <div class="page">
    <PageHeader title="个人中心" subtitle="管理你的个人资料">
      <template #actions>
        <span class="text-tertiary">修改密码与账户信息请点击右上角用户名</span>
      </template>
    </PageHeader>

    <!-- 资料头卡 -->
    <el-card class="profile-card">
      <div class="profile-main">
        <div class="avatar-wrap">
          <el-avatar :size="72" :src="avatarUrl || undefined" class="profile-avatar">
            {{ (auth.user?.nickname || auth.user?.username || 'U')[0] }}
          </el-avatar>
          <div class="avatar-upload">
            <el-upload :show-file-list="false" accept="image/*" :http-request="doUpload">
              <el-button size="small" circle>
                <el-icon><Camera /></el-icon>
              </el-button>
            </el-upload>
          </div>
        </div>
        <div class="profile-info">
          <div class="profile-name">
            {{ auth.user?.nickname || auth.user?.username || '未设置昵称' }}
            <el-tag v-if="auth.user?.fitnessGoal" size="small" effect="light" type="warning" class="goal-tag">
              目标 · {{ auth.user.fitnessGoal }}
            </el-tag>
          </div>
          <div class="profile-meta">
            <span>@{{ auth.user?.username }}</span>
            <span class="dot-sep">·</span>
            <span>编码 {{ auth.user?.userCode }}</span>
            <span v-if="auth.user?.fitnessLevel" class="dot-sep">·</span>
            <span v-if="auth.user?.fitnessLevel">等级 {{ auth.user.fitnessLevel }}</span>
          </div>
          <div class="profile-meta text-tertiary">
            {{ auth.user?.birthDate ? `${auth.user.birthDate} 出生` : '' }}
            {{ auth.user?.age ? `· ${auth.user.age} 岁` : '' }}
          </div>
        </div>
        <div class="profile-nums">
          <div class="p-num-item">
            <div class="p-num-value">{{ profile.height ?? '-' }}</div>
            <div class="p-num-label">身高 (cm)</div>
          </div>
          <div class="p-num-divider" />
          <div class="p-num-item">
            <div class="p-num-value">{{ profile.weight ?? '-' }}</div>
            <div class="p-num-label">体重 (kg)</div>
          </div>
          <div class="p-num-divider" />
          <div class="p-num-item">
            <div class="p-num-value">{{ bmi?.bmi ?? '-' }}</div>
            <div class="p-num-label">BMI</div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 统计 -->
    <div class="stat-grid">
      <StatCard
        label="累计训练次数"
        :value="summary.count"
        unit="次"
        icon="Calendar"
        tone="primary"
        :loading="loading.summary"
      />
      <StatCard
        label="累计训练时长"
        :value="summary.duration"
        unit="分钟"
        icon="Timer"
        tone="blue"
        :loading="loading.summary"
      >
        {{ Math.round(summary.duration / 60) }} 小时
      </StatCard>
      <StatCard
        label="累计消耗热量"
        :value="summary.calories"
        unit="千卡"
        icon="Lightning"
        tone="warning"
        :loading="loading.summary"
      />
      <StatCard
        label="连续打卡"
        :value="streak?.currentStreak"
        unit="天"
        icon="AlarmClock"
        tone="purple"
        :loading="loading.summary"
      >
        最长连续 {{ streak?.longestStreak ?? '-' }} 天
      </StatCard>
    </div>

    <!-- 资料编辑 -->
    <el-card>
      <template #header>
        <div class="card-title">个人资料</div>
      </template>
      <el-form :model="form" label-position="top">
        <div class="form-grid">
          <el-form-item label="昵称">
            <el-input v-model="form.nickname" maxlength="64" placeholder="设置你的昵称" />
          </el-form-item>
          <el-form-item label="性别">
            <el-radio-group v-model="form.gender">
              <el-radio :value="1">男</el-radio>
              <el-radio :value="2">女</el-radio>
              <el-radio :value="0">保密</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="出生日期">
            <el-date-picker v-model="form.birthDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
          </el-form-item>
          <el-form-item label="健身等级">
            <el-select v-model="form.fitnessLevel" clearable style="width: 100%">
              <el-option v-for="l in FITNESS_LEVELS" :key="l" :label="l" :value="l" />
            </el-select>
          </el-form-item>
          <el-form-item label="身高 (cm)">
            <el-input-number v-model="form.height" :min="0" :precision="2" :controls="false" class="w-full" />
          </el-form-item>
          <el-form-item label="体重 (kg)">
            <el-input-number v-model="form.weight" :min="0" :precision="2" :controls="false" class="w-full" />
          </el-form-item>
          <el-form-item label="健身目标">
            <el-select v-model="form.fitnessGoal" clearable style="width: 100%">
              <el-option v-for="g in FITNESS_GOALS" :key="g" :label="g" :value="g" />
            </el-select>
          </el-form-item>
        </div>
        <el-form-item class="form-actions">
          <el-button type="primary" :loading="saving" @click="save">
            <el-icon class="btn-icon"><Check /></el-icon>
            保存资料
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { UploadRequestOptions } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import StatCard from '@/components/StatCard.vue'
import { useAuthStore } from '@/stores/auth'
import { useAuthAvatar } from '@/composables/useAuthAvatar'
import { updateProfile, uploadAvatar } from '@/api/user'
import { getBmiCurrent } from '@/api/body'
import { getStreak, listTraining } from '@/api/training'
import type { BmiVO, StreakVO, UpdateProfileRequest } from '@/types'
import { FITNESS_GOALS, FITNESS_LEVELS } from '@/constants'

const auth = useAuthStore()
const { avatarUrl } = useAuthAvatar()
const saving = ref(false)

const form = reactive<UpdateProfileRequest>({})

const streak = ref<StreakVO | null>(null)
const bmi = ref<BmiVO | null>(null)
const summary = reactive({ count: 0, duration: 0, calories: 0 })
const loading = reactive({ summary: true })

const profile = reactive({ height: undefined as number | undefined, weight: undefined as number | undefined })

function initForm() {
  const u = auth.user
  if (!u) return
  form.nickname = u.nickname ?? undefined
  form.gender = u.gender
  form.birthDate = u.birthDate ?? undefined
  form.height = u.height ?? undefined
  form.weight = u.weight ?? undefined
  form.fitnessGoal = u.fitnessGoal ?? undefined
  form.fitnessLevel = u.fitnessLevel ?? undefined
  profile.height = u.height ?? undefined
  profile.weight = u.weight ?? undefined
}

async function loadSummary() {
  loading.summary = true
  try {
    const [page, streakData, bmiData] = await Promise.all([
      listTraining({ page: 1, size: 100 }),
      getStreak(),
      getBmiCurrent(),
    ])
    streak.value = streakData
    bmi.value = bmiData
    summary.count = page.total
    summary.duration = page.records.reduce((s, r) => {
      const v = Number(r.durationValue ?? 0)
      return s + (r.durationUnit === 'HOUR' ? v * 60 : v)
    }, 0)
    summary.calories = page.records.reduce((s, r) => s + Number(r.caloriesBurned ?? 0), 0)
  } catch {
    // 拦截器已提示
  } finally {
    loading.summary = false
  }
}

async function save() {
  saving.value = true
  try {
    auth.user = await updateProfile({ ...form })
    initForm()
    ElMessage.success('资料已保存')
  } catch {
    // 拦截器已提示
  } finally {
    saving.value = false
  }
}

async function doUpload(options: UploadRequestOptions) {
  try {
    const url = await uploadAvatar(options.file)
    auth.user = { ...auth.user!, avatarUrl: url }
    ElMessage.success('头像已更新')
  } catch {
    // 拦截器已提示
  }
}

onMounted(async () => {
  if (!auth.user) {
    await auth.fetchProfile()
  }
  initForm()
  loadSummary()
})
</script>

<style scoped>
.profile-card {
  margin-bottom: 16px;
}
.profile-main {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}
.avatar-wrap {
  position: relative;
  flex-shrink: 0;
}
.profile-avatar {
  background: var(--fth-primary);
  color: #fff;
  font-size: 24px;
}
.avatar-upload {
  position: absolute;
  right: -4px;
  bottom: -4px;
}
.profile-info {
  flex: 1;
  min-width: 200px;
}
.profile-name {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 20px;
  font-weight: 700;
  color: var(--fth-text-primary);
}
.goal-tag {
  flex-shrink: 0;
}
.profile-meta {
  margin-top: 4px;
  font-size: var(--fth-text-sm);
  color: var(--fth-text-secondary);
}
.dot-sep {
  margin: 0 8px;
}
.profile-nums {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 12px 20px;
  background: var(--fth-bg-muted);
  border-radius: var(--fth-radius);
}
.p-num-item {
  text-align: center;
}
.p-num-value {
  font-size: 22px;
  font-weight: 700;
  color: var(--fth-text-primary);
  font-variant-numeric: tabular-nums;
}
.p-num-label {
  font-size: var(--fth-text-xs);
  color: var(--fth-text-tertiary);
}
.p-num-divider {
  width: 1px;
  height: 28px;
  background: var(--fth-border);
}
@media (max-width: 767px) {
  .profile-nums {
    width: 100%;
    justify-content: space-around;
  }
}

.card-title {
  font-size: var(--fth-text-lg);
  font-weight: 600;
}
.form-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 0 16px;
}
@media (max-width: 1023px) {
  .form-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
@media (max-width: 767px) {
  .form-grid {
    grid-template-columns: 1fr;
  }
}
.w-full {
  width: 100%;
}
.form-actions {
  margin-bottom: 0;
}
.btn-icon {
  margin-right: 4px;
}
</style>
