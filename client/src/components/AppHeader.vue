<template>
  <div class="app-header">
    <div class="header-left">
      <el-button v-if="isMobile" text class="menu-btn" @click="$emit('toggle-menu')">
        <el-icon :size="20"><Menu /></el-icon>
      </el-button>
      <el-button v-else text class="menu-btn" @click="$emit('toggle-collapse')">
        <el-icon :size="20"><component :is="collapsed ? 'Expand' : 'Fold'" /></el-icon>
      </el-button>

      <div class="breadcrumb">
        <span v-if="route.meta.group" class="crumb-group">{{ route.meta.group }}</span>
        <template v-if="route.meta.group">
          <span class="crumb-sep">/</span>
        </template>
        <span class="crumb-current">{{ route.meta.title || '' }}</span>
      </div>
    </div>

    <div class="header-right">
      <el-tooltip content="通知中心（即将上线）" placement="bottom">
        <el-button text class="icon-btn">
          <el-icon :size="18"><Bell /></el-icon>
          <span class="badge-dot" />
        </el-button>
      </el-tooltip>

      <el-dropdown trigger="click" @command="onCommand">
        <div class="user-dropdown">
          <el-avatar :size="30" :src="avatarUrl || undefined" class="header-avatar">
            {{ (auth.user?.nickname || auth.user?.username || 'U')[0] }}
          </el-avatar>
          <span class="user-name">{{ auth.user?.nickname || auth.user?.username || '未登录' }}</span>
          <el-icon class="chevron"><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">
              <el-icon><User /></el-icon>个人中心
            </el-dropdown-item>
            <el-dropdown-item command="change-password">
              <el-icon><Lock /></el-icon>修改密码
            </el-dropdown-item>
            <el-dropdown-item command="account">
              <el-icon><InfoFilled /></el-icon>账户信息
            </el-dropdown-item>
            <el-dropdown-item command="logout" divided>
              <el-icon><SwitchButton /></el-icon>退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>

    <ChangePasswordDialog v-model="pwdVisible" />
    <AccountInfoDialog v-model="acctVisible" />
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useAuthAvatar } from '@/composables/useAuthAvatar'
import ChangePasswordDialog from './ChangePasswordDialog.vue'
import AccountInfoDialog from './AccountInfoDialog.vue'

defineProps<{ collapsed?: boolean; isMobile?: boolean }>()
defineEmits<{ 'toggle-collapse': []; 'toggle-menu': [] }>()

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const { avatarUrl } = useAuthAvatar()

const pwdVisible = ref(false)
const acctVisible = ref(false)

function onCommand(command: string) {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'change-password') {
    pwdVisible.value = true
  } else if (command === 'account') {
    acctVisible.value = true
  } else if (command === 'logout') {
    auth.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.app-header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  background: #fff;
  border-bottom: 1px solid var(--fth-border);
  position: sticky;
  top: 0;
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}
.menu-btn {
  padding: 6px;
}
.breadcrumb {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: var(--fth-text-base);
  white-space: nowrap;
  overflow: hidden;
}
.crumb-group {
  color: var(--fth-text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
}
.crumb-sep {
  color: var(--fth-text-tertiary);
  flex-shrink: 0;
}
.crumb-current {
  font-weight: 600;
  color: var(--fth-text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}
.icon-btn {
  position: relative;
  padding: 6px;
  color: var(--fth-text-secondary);
}
.badge-dot {
  position: absolute;
  top: 6px;
  right: 6px;
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--fth-primary);
  border: 2px solid #fff;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: var(--fth-radius);
  transition: background 0.15s ease;
  outline: none;
}
.user-dropdown:hover {
  background: var(--fth-bg-muted);
}
.header-avatar {
  background: var(--fth-primary);
  color: #fff;
}
.user-name {
  font-size: var(--fth-text-base);
  color: var(--fth-text-primary);
  font-weight: 500;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.chevron {
  color: var(--fth-text-tertiary);
  font-size: 12px;
}
</style>
