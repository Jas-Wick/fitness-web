<template>
  <div class="page">
    <PageHeader title="身体数据" subtitle="记录身体变化，让进步看得见">
      <template #actions>
        <el-button type="primary" @click="openCreate">
          <el-icon class="btn-icon"><Plus /></el-icon>
          记录身体数据
        </el-button>
      </template>
    </PageHeader>

    <!-- 概览 -->
    <div class="stat-grid">
      <StatCard
        label="当前 BMI"
        :value="bmi?.bmi"
        icon="Odometer"
        :tone="bmiTone"
        :loading="loading.bmi"
      >
        <el-tag :type="bmiTagType" size="small" effect="light">{{ bmi?.category ?? '暂无数据' }}</el-tag>
      </StatCard>
      <StatCard
        label="当前体重"
        :value="latest?.weight"
        unit="kg"
        icon="ScaleToOriginal"
        tone="blue"
        :loading="loading.trend"
      >
        {{ latest?.recordDate ? `更新于 ${latest.recordDate}` : '尚无记录' }}
      </StatCard>
      <StatCard
        label="最新体脂率"
        :value="latest?.bodyFatRate"
        unit="%"
        icon="TrendCharts"
        tone="purple"
        :loading="loading.trend"
      >
        {{ latest?.bodyFatRate !== null && latest?.bodyFatRate !== undefined ? '体脂管理进行中' : '尚未记录' }}
      </StatCard>
      <StatCard
        label="身体记录"
        :value="records.total"
        unit="条"
        icon="Files"
        tone="warning"
        :loading="loading.trend"
      />
    </div>

    <!-- BMI 建议 -->
    <el-card v-if="bmi?.suggestion" class="bmi-tip">
      <div class="bmi-tip-icon"><el-icon><ChatLineSquare /></el-icon></div>
      <div class="bmi-tip-text">{{ bmi.suggestion }}</div>
    </el-card>

    <!-- 趋势 -->
    <ChartCard
      ref="trendChartRef"
      title="体重 / 体脂率趋势"
      subtitle="近期的身体数据变化"
      height="300px"
      :loading="loading.trend"
      :empty="trendEmpty"
      empty-text="还没有身体数据记录"
      empty-icon="ScaleToOriginal"
      class="mb-4"
    />

    <!-- 记录 -->
    <el-card>
      <template #header>
        <div class="card-title">身体记录</div>
      </template>
      <el-table :data="records.list" v-loading="records.loading">
        <el-table-column prop="recordDate" label="日期" width="120" />
        <el-table-column prop="weight" label="体重 (kg)" min-width="110">
          <template #default="{ row }">{{ row.weight ?? '-' }}</template>
        </el-table-column>
        <el-table-column prop="bodyFatRate" label="体脂率 (%)" min-width="110">
          <template #default="{ row }">{{ row.bodyFatRate ?? '-' }}</template>
        </el-table-column>
        <el-table-column prop="muscleMass" label="肌肉量 (kg)" min-width="110">
          <template #default="{ row }">{{ row.muscleMass ?? '-' }}</template>
        </el-table-column>
        <el-table-column prop="waist" label="腰围 (cm)" min-width="100">
          <template #default="{ row }">{{ row.waist ?? '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="130" align="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="remove(row)">删除</el-button>
          </template>
        </el-table-column>
        <template #empty>
          <EmptyState description="还没有身体数据" icon="ScaleToOriginal" />
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

    <!-- 新增/编辑 -->
    <FormDialog
      v-model="dialogVisible"
      :title="editId ? '编辑记录' : '记录身体数据'"
      :loading="submitting"
      confirm-text="保存"
      @confirm="submit"
    >
      <el-form :model="form" label-position="top">
        <el-form-item label="记录日期" required>
          <el-date-picker v-model="form.recordDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <div class="form-grid">
          <el-form-item label="体重(kg)">
            <el-input-number v-model="form.weight" :min="0" :precision="2" :controls="false" class="w-full" />
          </el-form-item>
          <el-form-item label="体脂率(%)">
            <el-input-number v-model="form.bodyFatRate" :min="0" :max="100" :precision="2" :controls="false" class="w-full" />
          </el-form-item>
          <el-form-item label="肌肉量(kg)">
            <el-input-number v-model="form.muscleMass" :min="0" :precision="2" :controls="false" class="w-full" />
          </el-form-item>
          <el-form-item label="胸围(cm)">
            <el-input-number v-model="form.chest" :min="0" :precision="2" :controls="false" class="w-full" />
          </el-form-item>
          <el-form-item label="腰围(cm)">
            <el-input-number v-model="form.waist" :min="0" :precision="2" :controls="false" class="w-full" />
          </el-form-item>
          <el-form-item label="臀围(cm)">
            <el-input-number v-model="form.hip" :min="0" :precision="2" :controls="false" class="w-full" />
          </el-form-item>
        </div>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" maxlength="255" />
        </el-form-item>
      </el-form>
    </FormDialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import StatCard from '@/components/StatCard.vue'
import ChartCard from '@/components/ChartCard.vue'
import EmptyState from '@/components/EmptyState.vue'
import FormDialog from '@/components/FormDialog.vue'
import { useList } from '@/composables/useList'
import { confirmDelete } from '@/composables/useConfirm'
import { today } from '@/composables/useDate'
import {
  createBody,
  deleteBody,
  getBmiCurrent,
  getBodyTrend,
  listBody,
  updateBody,
} from '@/api/body'
import type { BodyDataVO, BodyDataRequest, BmiVO } from '@/types'
import { CHART_COLORS } from '@/constants'

const records = useList<BodyDataVO>((p) => listBody({ ...p, size: 8 }), { size: 8 })

const bmi = ref<BmiVO | null>(null)
const trend = ref<BodyDataVO[]>([])
const loading = reactive({ bmi: true, trend: true })
const trendChartRef = ref<InstanceType<typeof ChartCard>>()

const dialogVisible = ref(false)
const submitting = ref(false)
const editId = ref<number | null>(null)
const form = reactive<BodyDataRequest>({ recordDate: '' })

const latest = computed(() => (trend.value.length ? trend.value[trend.value.length - 1] : null))
const trendEmpty = computed(() => trend.value.length === 0)

const bmiTagType = computed(() => {
  if (!bmi.value) return 'info'
  const c = bmi.value.category
  if (c === '偏瘦' || c === '肥胖') return 'danger'
  if (c === '超重') return 'warning'
  return 'success'
})
const bmiTone = computed(() => {
  if (!bmi.value) return 'info'
  const c = bmi.value.category
  if (c === '偏瘦' || c === '肥胖') return 'danger'
  if (c === '超重') return 'warning'
  return 'success'
})

async function loadBmi() {
  try {
    bmi.value = await getBmiCurrent()
  } catch {
    bmi.value = null
  } finally {
    loading.bmi = false
  }
}

async function loadTrend() {
  try {
    trend.value = await getBodyTrend()
  } catch {
    trend.value = []
  } finally {
    loading.trend = false
    renderTrend()
  }
}

function renderTrend() {
  const dates = trend.value.map((t) => t.recordDate.slice(5))
  trendChartRef.value?.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['体重(kg)', '体脂率(%)'], bottom: 0, icon: 'circle' },
    grid: { left: 36, right: 40, top: 16, bottom: 28 },
    xAxis: { type: 'category', data: dates, axisLine: { lineStyle: { color: '#e5e7eb' } }, axisTick: { show: false } },
    yAxis: [
      { type: 'value', name: 'kg', splitLine: { lineStyle: { color: '#f3f4f6' } } },
      { type: 'value', name: '%', splitLine: { show: false } },
    ],
    series: [
      {
        name: '体重(kg)',
        type: 'line',
        data: trend.value.map((t) => Number(t.weight ?? null)),
        smooth: true,
        symbolSize: 6,
        lineStyle: { color: CHART_COLORS.primary, width: 2 },
        itemStyle: { color: CHART_COLORS.primary },
        areaStyle: { color: 'rgba(249, 115, 22, 0.08)' },
        connectNulls: true,
      },
      {
        name: '体脂率(%)',
        type: 'line',
        yAxisIndex: 1,
        data: trend.value.map((t) => Number(t.bodyFatRate ?? null)),
        smooth: true,
        symbolSize: 6,
        lineStyle: { color: CHART_COLORS.purple, width: 2 },
        itemStyle: { color: CHART_COLORS.purple },
        connectNulls: true,
      },
    ],
  })
}

function openCreate() {
  editId.value = null
  form.recordDate = today()
  form.weight = undefined
  form.bodyFatRate = undefined
  form.muscleMass = undefined
  form.chest = undefined
  form.waist = undefined
  form.hip = undefined
  form.remark = ''
  dialogVisible.value = true
}

function openEdit(row: BodyDataVO) {
  editId.value = row.id
  form.recordDate = row.recordDate
  form.weight = row.weight ?? undefined
  form.bodyFatRate = row.bodyFatRate ?? undefined
  form.muscleMass = row.muscleMass ?? undefined
  form.chest = row.chest ?? undefined
  form.waist = row.waist ?? undefined
  form.hip = row.hip ?? undefined
  form.remark = row.remark ?? ''
  dialogVisible.value = true
}

async function submit() {
  if (!form.recordDate) {
    ElMessage.warning('请选择记录日期')
    return
  }
  submitting.value = true
  try {
    if (editId.value) {
      await updateBody(editId.value, { ...form })
    } else {
      await createBody({ ...form })
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    refreshAll()
  } catch {
    // 拦截器已提示
  } finally {
    submitting.value = false
  }
}

async function remove(row: BodyDataVO) {
  const ok = await confirmDelete('确定删除这条身体数据吗？')
  if (!ok) return
  try {
    await deleteBody(row.id)
    ElMessage.success('删除成功')
    refreshAll()
  } catch {
    // 拦截器已提示
  }
}

function refreshAll() {
  records.load()
  loadBmi()
  loadTrend()
}

onMounted(() => {
  records.load()
  loadBmi()
  loadTrend()
})
</script>

<style scoped>
.btn-icon {
  margin-right: 4px;
}
.bmi-tip {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 20px;
  margin-bottom: 16px;
  border-left: 3px solid var(--fth-primary);
}
.bmi-tip-icon {
  color: var(--fth-primary);
  font-size: 20px;
}
.bmi-tip-text {
  color: var(--fth-text-secondary);
  font-size: var(--fth-text-sm);
}

.card-title {
  font-size: var(--fth-text-lg);
  font-weight: 600;
}
.form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 0 16px;
}
.w-full {
  width: 100%;
}
.pager {
  justify-content: flex-end;
  margin-top: 8px;
}
</style>
