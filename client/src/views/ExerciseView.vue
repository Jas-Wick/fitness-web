<template>
  <div class="page">
    <PageHeader title="动作教学" subtitle="标准动作图解，练得安全又到位">
      <template #actions>
        <el-input
          v-model="keyword"
          placeholder="搜索动作名称"
          clearable
          class="search-input"
          @keyup.enter="search"
          @clear="search"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="search">
          <el-icon class="btn-icon"><Search /></el-icon>
          查询
        </el-button>
      </template>
    </PageHeader>

    <el-tabs v-model="activePart" class="part-tabs" @tab-change="onTabChange">
      <el-tab-pane label="全部" name="all" />
      <el-tab-pane v-for="part in EXERCISE_BODY_PARTS" :key="part" :label="part" :name="part" />
    </el-tabs>

    <div v-loading="loading" class="card-grid">
      <div v-for="e in list" :key="e.id" class="ex-card" @click="openDetail(e)">
        <div class="ex-media">
          <ImageWithFallback :src="e.imageUrl" :alt="e.name" />
        </div>
        <div class="ex-body">
          <div class="ex-head">
            <div class="ex-name">{{ e.name }}</div>
            <el-tag size="small" effect="light" type="warning">{{ e.bodyPart }}</el-tag>
          </div>
          <div class="ex-desc">{{ e.description }}</div>
          <div class="ex-foot">
            <span class="ex-meta">
              <el-icon class="ex-meta-icon"><View /></el-icon>
              {{ e.viewCount }} 次浏览
            </span>
            <el-button link type="primary" size="small">
              查看详情<el-icon class="arrow-icon"><ArrowRight /></el-icon>
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <EmptyState v-if="!loading && list.length === 0" description="没有找到匹配的动作" icon="Files">
      <template #action>
        <el-button @click="reset">清空筛选</el-button>
      </template>
    </EmptyState>

    <el-pagination
      v-if="total > size"
      class="pager"
      layout="prev, pager, next, total"
      :total="total"
      :page-size="size"
      :current-page="page"
      @current-change="onPageChange"
    />

    <!-- 动作详情 -->
    <el-dialog v-model="detailVisible" :title="detail?.name" width="600px">
      <div v-if="detail">
        <div class="detail-part">
          <el-tag size="small" effect="light" type="warning">{{ detail.bodyPart }}</el-tag>
          <span class="detail-views">{{ detail.viewCount }} 次浏览</span>
        </div>
        <div class="detail-media">
          <ImageWithFallback v-if="detail.imageUrl" :src="detail.imageUrl" :alt="detail.name" />
        </div>
        <p v-if="detail.description" class="detail-text">{{ detail.description }}</p>
        <template v-if="detail.steps">
          <div class="section-title">
            <el-icon class="section-icon"><List /></el-icon>
            标准步骤
          </div>
          <p class="pre">{{ detail.steps }}</p>
        </template>
        <template v-if="detail.precautions">
          <div class="section-title">
            <el-icon class="section-icon"><Warning /></el-icon>
            注意事项
          </div>
          <p class="pre precautions">{{ detail.precautions }}</p>
        </template>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import PageHeader from '@/components/PageHeader.vue'
import EmptyState from '@/components/EmptyState.vue'
import ImageWithFallback from '@/components/ImageWithFallback.vue'
import { getExerciseDetail, listExercises } from '@/api/exercise'
import type { ExerciseVO } from '@/types'
import { EXERCISE_BODY_PARTS } from '@/constants'

const list = ref<ExerciseVO[]>([])
const total = ref(0)
const page = ref(1)
const size = 12
const loading = ref(false)
const keyword = ref('')
const activePart = ref('all')
const detail = ref<ExerciseVO | null>(null)
const detailVisible = ref(false)

async function load() {
  loading.value = true
  try {
    const data = await listExercises({
      page: page.value,
      size,
      keyword: keyword.value || undefined,
      bodyPart: activePart.value === 'all' ? undefined : activePart.value,
    })
    list.value = data.records
    total.value = data.total
  } catch {
    // 拦截器已提示
  } finally {
    loading.value = false
  }
}

function search() {
  page.value = 1
  load()
}

function onTabChange() {
  page.value = 1
  load()
}

function onPageChange(p: number) {
  page.value = p
  load()
}

function reset() {
  keyword.value = ''
  activePart.value = 'all'
  page.value = 1
  load()
}

async function openDetail(row: ExerciseVO) {
  try {
    detail.value = await getExerciseDetail(row.id)
    detailVisible.value = true
  } catch {
    // 拦截器已提示
  }
}

onMounted(load)
</script>

<style scoped>
.search-input {
  width: 220px;
}
.btn-icon {
  margin-right: 4px;
}

.part-tabs {
  margin-bottom: 4px;
}
.part-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
  background: var(--fth-border-soft);
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 16px;
  min-height: 120px;
}
.card-grid > * {
  height: 100%;
}
.ex-card {
  display: flex;
  flex-direction: column;
  background: var(--fth-bg-card);
  border: 1px solid var(--fth-border);
  border-radius: var(--fth-radius);
  overflow: hidden;
  cursor: pointer;
  box-shadow: var(--fth-shadow-sm);
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}
.ex-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--fth-shadow-md);
}
.ex-media {
  height: 140px;
  flex-shrink: 0;
}
.ex-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 12px 14px 10px;
  min-width: 0;
}
.ex-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 6px;
}
.ex-name {
  font-weight: 600;
  font-size: var(--fth-text-lg);
  color: var(--fth-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.ex-desc {
  color: var(--fth-text-secondary);
  font-size: var(--fth-text-sm);
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  min-height: 42px;
}
.ex-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: auto;
  padding-top: 8px;
  border-top: 1px solid var(--fth-border-soft);
}
.ex-meta {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--fth-text-tertiary);
  font-size: var(--fth-text-xs);
}
.ex-meta-icon {
  font-size: 14px;
}
.arrow-icon {
  margin-left: 2px;
  font-size: 12px;
}

.pager {
  justify-content: flex-end;
  margin-top: 16px;
}

.detail-part {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}
.detail-views {
  color: var(--fth-text-tertiary);
  font-size: var(--fth-text-xs);
}
.detail-media {
  border-radius: var(--fth-radius);
  overflow: hidden;
  margin-bottom: 12px;
  height: 220px;
}
.detail-text {
  color: var(--fth-text-primary);
  line-height: 1.7;
  margin: 0 0 8px;
}
.section-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  margin: 14px 0 6px;
  color: var(--fth-text-primary);
}
.section-icon {
  color: var(--fth-primary);
}
.pre {
  white-space: pre-wrap;
  color: var(--fth-text-secondary);
  line-height: 1.7;
  margin: 0;
  font-size: var(--fth-text-sm);
}
.pre.precautions {
  color: #b45309;
}
</style>
