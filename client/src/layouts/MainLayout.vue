<template>
  <div class="layout">
    <!-- 桌面端：固定侧边栏（可折叠） -->
    <aside v-if="isDesktop" class="layout-aside" :style="{ width: collapsed ? '64px' : '232px' }">
      <AppSidebar :collapsed="collapsed" />
    </aside>

    <div class="layout-body">
      <AppHeader
        :collapsed="collapsed"
        :is-mobile="isMobile"
        @toggle-collapse="collapsed = !collapsed"
        @toggle-menu="drawerVisible = true"
      />

      <main class="layout-main">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" :key="route.fullPath" />
          </transition>
        </router-view>
      </main>
    </div>

    <!-- 移动端：抽屉收纳侧边栏 -->
    <el-drawer
      v-model="drawerVisible"
      direction="ltr"
      size="260px"
      :with-header="false"
      class="layout-drawer"
    >
      <AppSidebar @navigate="drawerVisible = false" />
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useViewport } from '@/composables/useViewport'
import AppSidebar from '@/components/AppSidebar.vue'
import AppHeader from '@/components/AppHeader.vue'

const auth = useAuthStore()
const route = useRoute()
const { isMobile, isDesktop } = useViewport()

const collapsed = ref(false)
const drawerVisible = ref(false)

onMounted(() => {
  if (!auth.user) auth.fetchProfile()
})
</script>

<style scoped>
.layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

.layout-aside {
  height: 100%;
  flex-shrink: 0;
  transition: width 0.2s ease;
  overflow: hidden;
  background: var(--fth-sidebar-bg);
}

.layout-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.layout-main {
  flex: 1;
  overflow-y: auto;
  background: var(--fth-bg-page);
}
</style>
