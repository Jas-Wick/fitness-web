<template>
  <div class="page">
    <PageHeader title="社区" subtitle="分享你的健身经验，与大家一同进步">
      <template #actions>
        <el-select v-model="postType" placeholder="全部类型" clearable style="width: 150px" @change="load">
          <el-option v-for="t in POST_TYPES" :key="t" :label="t" :value="t" />
        </el-select>
        <el-button type="primary" @click="createVisible = true">
          <el-icon class="btn-icon"><EditPen /></el-icon>
          发布帖子
        </el-button>
      </template>
    </PageHeader>

    <div v-loading="loading" class="post-list">
      <el-card v-for="p in posts" :key="p.id" class="post-card">
        <div class="post-head">
          <el-avatar :size="36" :src="p.authorAvatar || undefined" class="post-avatar">
            {{ (p.authorNickname || 'U')[0] }}
          </el-avatar>
          <div class="post-author-info">
            <div class="post-author">{{ p.authorNickname || `用户${p.authorId}` }}</div>
            <div class="post-time">{{ formatTime(p.createTime) }}</div>
          </div>
          <el-tag size="small" effect="light" class="post-type">{{ p.postType }}</el-tag>
        </div>

        <!-- 转发原帖 -->
        <div v-if="p.originalPostId" class="forward-box">
          <div class="forward-author">@{{ p.originalAuthorNickname }} 的原帖</div>
          <div class="forward-title">{{ p.originalTitle }}</div>
          <div class="forward-content">{{ p.originalContent }}</div>
        </div>

        <div class="post-title">{{ p.title }}</div>
        <div class="post-content">{{ p.content }}</div>

        <div class="post-actions">
          <button class="action-btn" :class="{ active: p.liked }" @click="toggleLike(p)">
            <svg viewBox="0 0 24 24" class="heart-icon" aria-hidden="true">
              <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z" />
            </svg>
            <span>{{ p.likeCount }}</span>
          </button>
          <button class="action-btn" :class="{ active: p.favorited }" @click="toggleFavorite(p)">
            <el-icon><component :is="p.favorited ? 'StarFilled' : 'Star'" /></el-icon>
            <span>{{ p.favoriteCount }}</span>
          </button>
          <button class="action-btn" @click="openForward(p)">
            <el-icon><Share /></el-icon>
            <span>转发</span>
          </button>
          <button class="action-btn" @click="openDetail(p)">
            <el-icon><ChatDotRound /></el-icon>
            <span>{{ p.commentCount }}</span>
          </button>
        </div>
      </el-card>

      <EmptyState v-if="!loading && posts.length === 0" description="还没有帖子，来发布第一条吧" icon="ChatDotRound">
        <template #action>
          <el-button type="primary" @click="createVisible = true">发布帖子</el-button>
        </template>
      </EmptyState>
    </div>

    <el-pagination
      v-if="total > size"
      class="pager"
      layout="prev, pager, next, total"
      :total="total"
      :page-size="size"
      :current-page="page"
      @current-change="onPageChange"
    />

    <!-- 发布帖子 -->
    <FormDialog v-model="createVisible" title="发布帖子" width="540px" :loading="submitting" confirm-text="发布" @confirm="submitCreate">
      <el-form label-position="top">
        <el-form-item label="标题" required>
          <el-input v-model="createForm.title" maxlength="200" placeholder="一句话说清你的帖子主题" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="createForm.postType" style="width: 100%">
            <el-option v-for="t in POST_TYPES" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="内容" required>
          <el-input v-model="createForm.content" type="textarea" :rows="6" placeholder="分享你的经验、计划或问题..." />
        </el-form-item>
      </el-form>
    </FormDialog>

    <!-- 转发 -->
    <FormDialog v-model="forwardVisible" :title="`转发 @${forwardTarget?.authorNickname}`" width="480px" :loading="submitting" confirm-text="转发" @confirm="submitForward">
      <el-input v-model="forwardContent" type="textarea" :rows="3" placeholder="说点什么（可选）" />
    </FormDialog>

    <!-- 帖子详情 + 评论 -->
    <el-dialog v-model="detailVisible" :title="detail?.title" width="600px" append-to-body :close-on-click-modal="false">
      <div v-if="detail">
        <div class="detail-content">{{ detail.content }}</div>
        <el-divider content-position="left">
          <span class="comment-count">评论（{{ detail.commentCount }}）</span>
        </el-divider>
        <div class="comments">
          <div v-for="c in comments" :key="c.id" class="comment">
            <el-avatar :size="28" :src="c.userAvatar || undefined" class="comment-avatar">
              {{ (c.userNickname || 'U')[0] }}
            </el-avatar>
            <div class="comment-body">
              <div class="comment-author">{{ c.userNickname || `用户${c.userId}` }}</div>
              <div class="comment-text">{{ c.content }}</div>
            </div>
            <div class="comment-time">{{ formatTime(c.createTime) }}</div>
          </div>
          <EmptyState v-if="comments.length === 0" description="暂无评论，来抢沙发" icon="ChatDotRound" />
          <div v-if="comments.length < commentTotal" class="load-more">
            <el-button link type="primary" :loading="loadingComments" @click="loadMoreComments">
              加载更多评论
            </el-button>
          </div>
        </div>
        <div class="comment-input">
          <el-input
            ref="commentInputRef"
            v-model="commentContent"
            placeholder="写下你的评论..."
            maxlength="1000"
            @keyup.enter.prevent="onCommentEnter"
          />
          <el-button type="primary" @click="submitComment">评论</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import EmptyState from '@/components/EmptyState.vue'
import FormDialog from '@/components/FormDialog.vue'
import { listPosts, createPost, likePost, favoritePost, forwardPost } from '@/api/post'
import { listComments, createComment } from '@/api/comment'
import type { CommentVO, PostVO } from '@/types'
import { POST_TYPES } from '@/constants'
import { formatTime } from '@/composables/useDate'

const posts = ref<PostVO[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const postType = ref('')
const loading = ref(false)

const createVisible = ref(false)
const forwardVisible = ref(false)
const detailVisible = ref(false)
const submitting = ref(false)

const createForm = reactive({ title: '', content: '', postType: '问题交流' })
const forwardTarget = ref<PostVO | null>(null)
const forwardContent = ref('')
const detail = ref<PostVO | null>(null)
const comments = ref<CommentVO[]>([])
const commentContent = ref('')
const commentPage = ref(1)
const commentTotal = ref(0)
const loadingComments = ref(false)
const commentInputRef = ref<{ focus: () => void }>()

async function load() {
  loading.value = true
  try {
    const data = await listPosts({ page: page.value, size: size.value, postType: postType.value || undefined })
    posts.value = data.records
    total.value = data.total
  } catch {
    // 拦截器已提示
  } finally {
    loading.value = false
  }
}

function onPageChange(p: number) {
  page.value = p
  load()
}

// ── 点赞 / 收藏乐观更新：逻辑与改造前完全一致 ──
async function toggleLike(p: PostVO) {
  await likePost(p.id)
  p.liked = !p.liked
  p.likeCount += p.liked ? 1 : -1
}

async function toggleFavorite(p: PostVO) {
  await favoritePost(p.id)
  p.favorited = !p.favorited
  p.favoriteCount += p.favorited ? 1 : -1
}

function openForward(p: PostVO) {
  forwardTarget.value = p
  forwardContent.value = ''
  forwardVisible.value = true
}

async function submitForward() {
  if (!forwardTarget.value) return
  submitting.value = true
  try {
    await forwardPost(forwardTarget.value.id, { content: forwardContent.value || undefined })
    ElMessage.success('转发成功')
    forwardVisible.value = false
    load()
  } catch {
    // 拦截器已提示
  } finally {
    submitting.value = false
  }
}

async function submitCreate() {
  if (!createForm.title || !createForm.content) {
    ElMessage.warning('请填写标题和内容')
    return
  }
  submitting.value = true
  try {
    await createPost({ ...createForm })
    ElMessage.success('发布成功')
    createVisible.value = false
    createForm.title = ''
    createForm.content = ''
    load()
  } catch {
    // 拦截器已提示
  } finally {
    submitting.value = false
  }
}

async function openDetail(p: PostVO) {
  detail.value = p
  detailVisible.value = true
  await loadComments(p.id, false)
  // 弹窗打开后让评论输入框获得焦点，避免中文输入法组合键丢失
  nextTick(() => commentInputRef.value?.focus())
}

/** Enter 提交：中文输入法候选态（isComposing）不触发，避免空内容早退 */
function onCommentEnter(e: KeyboardEvent) {
  if (e.isComposing) return
  submitComment()
}

async function loadComments(postId: number, append: boolean) {
  loadingComments.value = true
  try {
    const data = await listComments(postId, append ? commentPage.value : 1, 50)
    commentPage.value = append ? commentPage.value + 1 : 2
    commentTotal.value = data.total
    comments.value = append ? [...comments.value, ...data.records] : data.records
  } catch {
    if (!append) comments.value = []
  } finally {
    loadingComments.value = false
  }
}

async function loadMoreComments() {
  if (detail.value) await loadComments(detail.value.id, true)
}

async function submitComment() {
  if (!detail.value || !commentContent.value.trim()) return
  try {
    await createComment({ postId: detail.value.id, content: commentContent.value })
    commentContent.value = ''
    comments.value = []
    await loadComments(detail.value.id, false)
    if (detail.value) detail.value.commentCount++
  } catch {
    // 拦截器已提示
  }
}

onMounted(load)
</script>

<style scoped>
.btn-icon {
  margin-right: 4px;
}

.post-list {
  min-height: 100px;
}
.post-card {
  margin-bottom: 14px;
}
.post-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}
.post-avatar {
  background: var(--fth-primary-soft);
  color: var(--fth-primary-hover);
  flex-shrink: 0;
}
.post-author-info {
  flex: 1;
  min-width: 0;
}
.post-author {
  font-weight: 600;
  color: var(--fth-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.post-time {
  font-size: var(--fth-text-xs);
  color: var(--fth-text-tertiary);
}
.post-type {
  flex-shrink: 0;
}

.forward-box {
  background: var(--fth-bg-muted);
  border-left: 3px solid var(--fth-primary);
  padding: 10px 14px;
  margin-bottom: 10px;
  border-radius: var(--fth-radius-sm);
}
.forward-author {
  font-size: var(--fth-text-xs);
  color: var(--fth-primary-hover);
}
.forward-title {
  font-weight: 600;
  margin: 4px 0;
  color: var(--fth-text-primary);
}
.forward-content {
  color: var(--fth-text-secondary);
  font-size: var(--fth-text-sm);
}

.post-title {
  font-weight: 600;
  font-size: var(--fth-text-lg);
  margin-bottom: 6px;
  color: var(--fth-text-primary);
}
.post-content {
  color: var(--fth-text-regular);
  white-space: pre-wrap;
  margin-bottom: 12px;
}

.post-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  border-top: 1px solid var(--fth-border-soft);
  padding-top: 10px;
}
.action-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 5px 12px;
  border: none;
  background: transparent;
  border-radius: var(--fth-radius-full);
  cursor: pointer;
  color: var(--fth-text-secondary);
  font-size: var(--fth-text-sm);
  transition: background 0.12s ease, color 0.12s ease;
}
.action-btn:hover {
  background: var(--fth-bg-muted);
  color: var(--fth-primary-hover);
}
.action-btn.active {
  color: var(--fth-primary-hover);
}
.heart-icon {
  width: 16px;
  height: 16px;
  fill: none;
  stroke: currentColor;
  stroke-width: 1.8;
  transition: fill 0.15s ease;
}
.action-btn.active .heart-icon {
  fill: currentColor;
}

.pager {
  justify-content: flex-end;
  margin-top: 8px;
}

.comment-count {
  font-size: var(--fth-text-sm);
  color: var(--fth-text-secondary);
}
.comments {
  max-height: 300px;
  overflow-y: auto;
  margin-bottom: 12px;
}
.comment {
  display: flex;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid var(--fth-border-soft);
}
.comment-avatar {
  background: var(--fth-bg-muted);
  color: var(--fth-text-secondary);
  flex-shrink: 0;
}
.comment-body {
  flex: 1;
  min-width: 0;
}
.comment-author {
  font-weight: 500;
  color: var(--fth-primary-hover);
  font-size: var(--fth-text-sm);
}
.comment-text {
  color: var(--fth-text-primary);
  font-size: var(--fth-text-sm);
  margin-top: 2px;
}
.comment-time {
  font-size: var(--fth-text-xs);
  color: var(--fth-text-tertiary);
  flex-shrink: 0;
}
.load-more {
  text-align: center;
  padding-top: 6px;
}
.comment-input {
  display: flex;
  gap: 8px;
}
.detail-content {
  white-space: pre-wrap;
  color: var(--fth-text-primary);
  line-height: 1.7;
}
</style>
