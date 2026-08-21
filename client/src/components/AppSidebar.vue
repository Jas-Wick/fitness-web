<template>
  <div class="app-sidebar" :class="{ collapsed }">
    <div class="brand">
      <div class="brand-mark">F</div>
      <div v-if="!collapsed" class="brand-text">
        <div class="brand-name">FitHub</div>
        <div class="brand-sub">智能健身管理</div>
      </div>
    </div>

    <nav class="nav">
      <template v-for="group in menuGroups" :key="group.label">
        <div v-if="!collapsed" class="group-label">{{ group.label }}</div>
        <el-tooltip
          v-for="item in group.items"
          :key="item.path"
          :disabled="!collapsed"
          :content="item.title"
          placement="right"
          :show-after="200"
        >
          <router-link
            :to="item.path"
            class="nav-item"
            :class="{ active: isActive(item.path) }"
            @click="$emit('navigate')"
          >
            <el-icon class="nav-icon"><component :is="item.icon" /></el-icon>
            <span v-if="!collapsed" class="nav-label">{{ item.title }}</span>
          </router-link>
        </el-tooltip>
      </template>
    </nav>

    <div class="sidebar-footer">
      <el-tooltip :disabled="!collapsed" :content="userName" placement="right" :show-after="200">
        <router-link :to="'/profile'" class="user-card" @click="$emit('navigate')">
          <el-avatar :size="32" :src="avatarUrl || undefined" class="user-avatar">
            {{ (auth.user?.nickname || auth.user?.username || 'U')[0] }}
          </el-avatar>
          <div v-if="!collapsed" class="user-info">
            <div class="user-name">{{ userName }}</div>
            <div class="user-role">个人中心</div>
          </div>
        </router-link>
      </el-tooltip>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useAuthAvatar } from '@/composables/useAuthAvatar'

defineProps<{ collapsed?: boolean }>()
defineEmits<{ navigate: [] }>()

const route = useRoute()
const auth = useAuthStore()
const { avatarUrl } = useAuthAvatar()

const userName = computed(() => auth.user?.nickname || auth.user?.username || '未登录')

const menuGroups = [
  {
    label: '总览',
    items: [{ path: '/home', title: '仪表盘', icon: 'HomeFilled' }],
  },
  {
    label: '训练',
    items: [
      { path: '/training', title: '训练打卡', icon: 'Calendar' },
      { path: '/exercise', title: '动作教学', icon: 'Files' },
      { path: '/analysis', title: '数据分析', icon: 'TrendCharts' },
    ],
  },
  {
    label: '健康管理',
    items: [
      { path: '/food', title: '饮食记录', icon: 'Dish' },
      { path: '/body', title: '身体数据', icon: 'ScaleToOriginal' },
    ],
  },
  {
    label: '互动',
    items: [
      { path: '/community', title: '社区', icon: 'ChatDotRound' },
      { path: '/ai', title: 'AI 健身助手', icon: 'MagicStick' },
    ],
  },
]

function isActive(path: string): boolean {
  return route.path === path
}
</script>

<style scoped>
.app-sidebar {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--fth-sidebar-bg);
  color: var(--fth-sidebar-text);
  overflow-y: auto;
  overflow-x: hidden;
}

.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 64px;
  padding: 0 16px;
  flex-shrink: 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
}
.app-sidebar.collapsed .brand {
  padding: 0;
  justify-content: center;
}
.brand-mark {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: var(--fth-primary);
  color: #fff;
  font-size: 20px;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.brand-name {
  font-size: 16px;
  font-weight: 700;
  color: #fff;
  line-height: 1.2;
  letter-spacing: 0.3px;
}
.brand-sub {
  font-size: 11px;
  color: var(--fth-sidebar-text);
  line-height: 1.4;
}

.nav {
  flex: 1;
  padding: 12px 10px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.group-label {
  padding: 14px 10px 6px;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.38);
  letter-spacing: 1px;
}
.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 40px;
  padding: 0 10px;
  border-radius: var(--fth-radius);
  color: var(--fth-sidebar-text);
  text-decoration: none;
  transition: background 0.15s ease, color 0.15s ease;
  position: relative;
}
.nav-item:hover {
  background: var(--fth-sidebar-hover);
  color: var(--fth-sidebar-text-active);
}
.nav-item.active {
  background: var(--fth-sidebar-active-bg);
  color: var(--fth-sidebar-active-text);
  font-weight: 500;
}
.nav-item.active::before {
  content: '';
  position: absolute;
  left: -10px;
  top: 50%;
  transform: translateY(-50%);
  width: 3px;
  height: 20px;
  border-radius: 0 3px 3px 0;
  background: var(--fth-primary);
}
.app-sidebar.collapsed .nav-item {
  justify-content: center;
  padding: 0;
}
.nav-icon {
  font-size: 18px;
  flex-shrink: 0;
}

.sidebar-footer {
  padding: 10px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  flex-shrink: 0;
}
.user-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px;
  border-radius: var(--fth-radius);
  color: var(--fth-sidebar-text);
  text-decoration: none;
  transition: background 0.15s ease;
}
.user-card:hover {
  background: var(--fth-sidebar-hover);
  color: var(--fth-sidebar-text-active);
}
.app-sidebar.collapsed .user-card {
  justify-content: center;
  padding: 4px;
}
.user-avatar {
  background: var(--fth-primary);
  color: #fff;
  flex-shrink: 0;
}
.user-name {
  font-size: 13px;
  font-weight: 500;
  color: #fff;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.user-role {
  font-size: 11px;
  color: var(--fth-sidebar-text);
}
</style>
