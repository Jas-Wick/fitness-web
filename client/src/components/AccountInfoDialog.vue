<template>
  <el-dialog
    :model-value="modelValue"
    title="账户信息"
    width="400px"
    append-to-body
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <div class="acct-head">
      <el-avatar :size="48" :src="avatarUrl || undefined" class="acct-avatar">
        {{ (auth.user?.nickname || auth.user?.username || 'U')[0] }}
      </el-avatar>
      <div>
        <div class="acct-name">{{ auth.user?.nickname || auth.user?.username || '未设置昵称' }}</div>
        <div class="acct-sub">FitHub 用户</div>
      </div>
    </div>
    <div class="acct-row">
      <span class="acct-label">用户名</span>
      <span>{{ auth.user?.username }}</span>
    </div>
    <div class="acct-row">
      <span class="acct-label">用户编码</span>
      <span>{{ auth.user?.userCode }}</span>
    </div>
    <div class="acct-row">
      <span class="acct-label">注册时间</span>
      <span>{{ formatTime(auth.user?.createTime) }}</span>
    </div>
    <template #footer>
      <el-button @click="$emit('update:modelValue', false)">关闭</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { useAuthStore } from '@/stores/auth'
import { useAuthAvatar } from '@/composables/useAuthAvatar'
import { formatTime } from '@/composables/useDate'

defineProps<{ modelValue: boolean }>()
defineEmits<{ 'update:modelValue': [boolean] }>()

const auth = useAuthStore()
const { avatarUrl } = useAuthAvatar()
</script>

<style scoped>
.acct-head {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--fth-border-soft);
}
.acct-avatar {
  background: var(--fth-primary);
  color: #fff;
  flex-shrink: 0;
}
.acct-name {
  font-size: var(--fth-text-lg);
  font-weight: 600;
  color: var(--fth-text-primary);
}
.acct-sub {
  font-size: var(--fth-text-xs);
  color: var(--fth-text-tertiary);
}
.acct-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid var(--fth-border-soft);
  font-size: var(--fth-text-sm);
  color: var(--fth-text-primary);
}
.acct-row:last-child {
  border-bottom: none;
}
.acct-label {
  color: var(--fth-text-tertiary);
}
</style>
