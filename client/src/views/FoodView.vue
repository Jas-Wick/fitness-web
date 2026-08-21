<template>
  <div class="page">
    <PageHeader title="饮食记录" subtitle="掌握每日营养摄入，吃出好状态">
      <template #actions>
        <el-date-picker
          v-model="range"
          type="daterange"
          value-format="YYYY-MM-DD"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          class="range-picker"
          @change="loadAll"
        />
        <el-button type="primary" @click="openCreate">
          <el-icon class="btn-icon"><Plus /></el-icon>
          记录饮食
        </el-button>
      </template>
    </PageHeader>

    <!-- 摄入统计 -->
    <div class="stat-grid">
      <StatCard
        label="摄入热量"
        :value="stat?.totalCalories"
        unit="千卡"
        icon="Odometer"
        tone="primary"
        :loading="loading.stat"
      />
      <StatCard
        label="蛋白质"
        :value="stat?.totalProtein"
        unit="g"
        icon="Chicken"
        tone="blue"
        :loading="loading.stat"
      />
      <StatCard
        label="碳水"
        :value="stat?.totalCarbs"
        unit="g"
        icon="Grape"
        tone="warning"
        :loading="loading.stat"
      />
      <StatCard
        label="脂肪"
        :value="stat?.totalFat"
        unit="g"
        icon="IceCreamRound"
        tone="purple"
        :loading="loading.stat"
      />
    </div>

    <!-- 营养构成可视化 -->
    <div class="row">
      <div class="col col-6">
        <el-card>
          <template #header>
            <div class="card-title">营养素构成</div>
          </template>
          <MacroProgress
            :total-protein="stat?.totalProtein"
            :total-carbs="stat?.totalCarbs"
            :total-fat="stat?.totalFat"
          />
        </el-card>
      </div>
      <div class="col col-6">
        <ChartCard
          ref="macroChartRef"
          title="营养占比"
          subtitle="蛋白质 / 碳水 / 脂肪（克）"
          height="260px"
          :loading="loading.stat"
          :empty="macroEmpty"
          empty-text="该时段暂无饮食记录"
          empty-icon="Bowl"
        />
      </div>
    </div>

    <!-- 餐次明细 -->
    <div class="meals">
      <div v-for="g in grouped" :key="g.value" class="meal-card" :class="{ empty: g.items.length === 0 }">
        <div class="meal-head">
          <div class="meal-name">
            <span class="meal-dot" :class="`meal-${g.value}`" />
            {{ g.label }}
          </div>
          <div class="meal-summary">
            <span>{{ g.items.length }} 项</span>
            <span class="dot-sep">·</span>
            <span>{{ groupCalories(g) }} 千卡</span>
          </div>
        </div>

        <div v-if="g.items.length" class="meal-list">
          <div v-for="item in g.items" :key="item.id" class="meal-item">
            <div class="item-main">
              <div class="item-name">{{ item.foodName }}</div>
              <div class="item-meta">
                <span class="item-cal">{{ item.calories ?? '-' }} 千卡</span>
                <span class="item-nutr">蛋白 {{ item.protein ?? '-' }}g · 碳水 {{ item.carbs ?? '-' }}g · 脂肪 {{ item.fat ?? '-' }}g</span>
              </div>
            </div>
            <div class="item-right">
              <span class="item-time">{{ formatTime(item.eatTime).slice(11) || formatTime(item.eatTime) }}</span>
              <el-button link type="primary" size="small" @click="openEdit(item)">编辑</el-button>
              <el-button link type="danger" size="small" @click="remove(item)">删除</el-button>
            </div>
          </div>
        </div>
        <div v-else class="meal-empty">
          <el-icon><Bowl /></el-icon>
          <span>本时段暂无{{ g.label }}记录</span>
        </div>
      </div>
    </div>

    <!-- 最近记录 -->
    <el-card>
      <template #header>
        <div class="card-title">全部记录</div>
      </template>
      <div class="table-scroll">
        <el-table :data="records.list" v-loading="records.loading">
          <el-table-column prop="foodName" label="食物" min-width="130" />
          <el-table-column label="餐次" width="80">
            <template #default="{ row }">
              <el-tag size="small" :type="mealTag(row.mealType)" effect="light">{{ mealName(row.mealType) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="calories" label="热量(千卡)" min-width="90">
            <template #default="{ row }">{{ row.calories ?? '-' }}</template>
          </el-table-column>
          <el-table-column prop="protein" label="蛋白(g)" min-width="80">
            <template #default="{ row }">{{ row.protein ?? '-' }}</template>
          </el-table-column>
          <el-table-column prop="carbs" label="碳水(g)" min-width="80">
            <template #default="{ row }">{{ row.carbs ?? '-' }}</template>
          </el-table-column>
          <el-table-column prop="fat" label="脂肪(g)" min-width="80">
            <template #default="{ row }">{{ row.fat ?? '-' }}</template>
          </el-table-column>
          <el-table-column label="时间" width="140">
            <template #default="{ row }">{{ formatTime(row.eatTime) }}</template>
          </el-table-column>
          <el-table-column label="操作" width="120" align="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
              <el-button link type="danger" @click="remove(row)">删除</el-button>
            </template>
          </el-table-column>
          <template #empty>
            <EmptyState description="还没有饮食记录" icon="Bowl" />
          </template>
        </el-table>
      </div>
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
      :title="editId ? '编辑记录' : '记录饮食'"
      :loading="submitting"
      confirm-text="保存"
      @confirm="submit"
    >
      <el-form :model="form" label-position="top">
        <el-form-item label="食物名称" required>
          <el-input v-model="form.foodName" maxlength="128" placeholder="如：燕麦片" />
        </el-form-item>
        <el-form-item label="餐次">
          <el-select v-model="form.mealType" style="width: 100%">
            <el-option v-for="m in MEAL_TYPES" :key="m.value" :label="m.label" :value="m.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="摄入时间" required>
          <el-date-picker v-model="form.eatTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" style="width: 100%" />
        </el-form-item>
        <div class="form-grid">
          <el-form-item label="热量(千卡)">
            <el-input-number v-model="form.calories" :min="0" :precision="2" :controls="false" class="w-full" />
          </el-form-item>
          <el-form-item label="蛋白质(g)">
            <el-input-number v-model="form.protein" :min="0" :precision="2" :controls="false" class="w-full" />
          </el-form-item>
          <el-form-item label="碳水(g)">
            <el-input-number v-model="form.carbs" :min="0" :precision="2" :controls="false" class="w-full" />
          </el-form-item>
          <el-form-item label="脂肪(g)">
            <el-input-number v-model="form.fat" :min="0" :precision="2" :controls="false" class="w-full" />
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
import MacroProgress from '@/components/MacroProgress.vue'
import { useList } from '@/composables/useList'
import { confirmDelete } from '@/composables/useConfirm'
import { formatTime, today } from '@/composables/useDate'
import { createFood, deleteFood, getFoodStat, listFoods, updateFood } from '@/api/food'
import type { FoodRecordVO, FoodRecordRequest, FoodStatVO } from '@/types'
import { CHART_COLORS, MEAL_TYPES, mealName, mealTag } from '@/constants'

const records = useList<FoodRecordVO>((p) => listFoods(p), { size: 10 })

const range = ref<string[]>([])
const stat = ref<FoodStatVO | null>(null)
const rangeFoods = ref<FoodRecordVO[]>([])
const loading = reactive({ stat: false, foods: false })
const macroChartRef = ref<InstanceType<typeof ChartCard>>()

const dialogVisible = ref(false)
const submitting = ref(false)
const editId = ref<number | null>(null)
const form = reactive<FoodRecordRequest>({ foodName: '', eatTime: '' })

const macroEmpty = computed(() => {
  const s = stat.value
  return !s || (!s.totalProtein && !s.totalCarbs && !s.totalFat)
})

const grouped = computed(() => {
  const [start, end] = range.value.length === 2 ? [range.value[0], range.value[1]] : [today(), today()]
  const map = new Map<number, FoodRecordVO[]>()
  for (const m of MEAL_TYPES) map.set(m.value, [])
  for (const f of rangeFoods.value) {
    const date = f.eatTime.slice(0, 10)
    if (date >= start && date <= end) {
      const key = f.mealType ?? 4
      map.get(key)?.push(f)
    }
  }
  return MEAL_TYPES.map((m) => ({ ...m, items: map.get(m.value) ?? [] }))
})

function groupCalories(g: { items: FoodRecordVO[] }): number {
  return g.items.reduce((sum, i) => sum + Number(i.calories ?? 0), 0)
}

async function loadStat() {
  if (range.value.length !== 2) return
  loading.stat = true
  try {
    stat.value = await getFoodStat(range.value[0], range.value[1])
    renderMacro()
  } catch {
    stat.value = null
  } finally {
    loading.stat = false
  }
}

async function loadRangeFoods() {
  loading.foods = true
  try {
    const data = await listFoods({ page: 1, size: 100 })
    rangeFoods.value = data.records
  } catch {
    rangeFoods.value = []
  } finally {
    loading.foods = false
  }
}

function renderMacro() {
  if (!stat.value) return
  macroChartRef.value?.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c}g ({d}%)' },
    legend: { bottom: 0, icon: 'circle' },
    series: [
      {
        name: '营养素(克)',
        type: 'pie',
        radius: ['52%', '72%'],
        center: ['50%', '44%'],
        avoidLabelOverlap: true,
        itemStyle: { borderRadius: 4, borderColor: '#fff', borderWidth: 2 },
        label: { show: false },
        emphasis: { label: { show: true, fontWeight: 600 } },
        data: [
          { name: '蛋白质', value: Number(stat.value.totalProtein || 0), itemStyle: { color: CHART_COLORS.blue } },
          { name: '碳水', value: Number(stat.value.totalCarbs || 0), itemStyle: { color: CHART_COLORS.amber } },
          { name: '脂肪', value: Number(stat.value.totalFat || 0), itemStyle: { color: CHART_COLORS.purple } },
        ],
      },
    ],
  })
}

function openCreate() {
  editId.value = null
  form.foodName = ''
  form.calories = undefined
  form.protein = undefined
  form.carbs = undefined
  form.fat = undefined
  form.mealType = undefined
  form.eatTime = `${today()}T08:00:00`
  form.remark = ''
  dialogVisible.value = true
}

function openEdit(row: FoodRecordVO) {
  editId.value = row.id
  form.foodName = row.foodName
  form.calories = row.calories ?? undefined
  form.protein = row.protein ?? undefined
  form.carbs = row.carbs ?? undefined
  form.fat = row.fat ?? undefined
  form.mealType = row.mealType ?? undefined
  form.eatTime = row.eatTime
  form.remark = row.remark ?? ''
  dialogVisible.value = true
}

async function submit() {
  if (!form.foodName || !form.eatTime) {
    ElMessage.warning('请填写食物名称和摄入时间')
    return
  }
  submitting.value = true
  try {
    if (editId.value) {
      await updateFood(editId.value, { ...form })
    } else {
      await createFood({ ...form })
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

async function remove(row: FoodRecordVO) {
  const ok = await confirmDelete('确定删除这条饮食记录吗？')
  if (!ok) return
  try {
    await deleteFood(row.id)
    ElMessage.success('删除成功')
    refreshAll()
  } catch {
    // 拦截器已提示
  }
}

function refreshAll() {
  records.load()
  loadStat()
  loadRangeFoods()
}

function loadAll() {
  loadStat()
  loadRangeFoods()
}

onMounted(() => {
  if (range.value.length !== 2) {
    range.value = [today(), today()]
  }
  records.load()
  loadAll()
})
</script>

<style scoped>
.range-picker {
  width: 250px;
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
.col-6 {
  grid-column: span 6;
}
@media (max-width: 1023px) {
  .col-6 {
    grid-column: span 12;
  }
}

.card-title {
  font-size: var(--fth-text-lg);
  font-weight: 600;
}

/* 餐次明细 */
.meals {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}
@media (max-width: 1023px) {
  .meals {
    grid-template-columns: 1fr;
  }
}
.meal-card {
  background: var(--fth-bg-card);
  border: 1px solid var(--fth-border);
  border-radius: var(--fth-radius);
  box-shadow: var(--fth-shadow-sm);
  padding: 16px 20px;
}
.meal-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 10px;
}
.meal-name {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
  color: var(--fth-text-primary);
}
.meal-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}
.meal-1 { background: #3b82f6; }
.meal-2 { background: #16a34a; }
.meal-3 { background: #f59e0b; }
.meal-4 { background: #8b5cf6; }
.meal-summary {
  font-size: var(--fth-text-xs);
  color: var(--fth-text-tertiary);
}
.dot-sep {
  margin: 0 6px;
}
.meal-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 0;
  border-top: 1px solid var(--fth-border-soft);
}
.item-main {
  min-width: 0;
}
.item-name {
  font-weight: 500;
  color: var(--fth-text-primary);
}
.item-meta {
  margin-top: 2px;
  font-size: var(--fth-text-xs);
  color: var(--fth-text-secondary);
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
.item-cal {
  color: var(--fth-primary-hover);
  font-weight: 500;
}
.item-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}
.item-time {
  font-size: var(--fth-text-xs);
  color: var(--fth-text-tertiary);
}
.meal-empty {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 20px 0 8px;
  color: var(--fth-text-tertiary);
  font-size: var(--fth-text-sm);
  border-top: 1px solid var(--fth-border-soft);
}

.form-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 0 16px;
}
.w-full {
  width: 100%;
}
.table-scroll {
  overflow-x: auto;
}
.table-scroll :deep(.el-table) {
  min-width: 640px;
}
.pager {
  justify-content: flex-end;
  margin-top: 12px;
}
</style>
