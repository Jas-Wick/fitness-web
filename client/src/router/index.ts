import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const routes: RouteRecordRaw[] = [
  { path: '/login', name: 'login', component: () => import('@/views/LoginView.vue'), meta: { public: true, title: '登录' } },
  { path: '/register', name: 'register', component: () => import('@/views/RegisterView.vue'), meta: { public: true, title: '注册' } },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/home',
    children: [
      { path: 'home', name: 'home', component: () => import('@/views/HomeView.vue'), meta: { title: '仪表盘', group: '总览' } },
      { path: 'training', name: 'training', component: () => import('@/views/TrainingView.vue'), meta: { title: '训练打卡', group: '训练' } },
      { path: 'exercise', name: 'exercise', component: () => import('@/views/ExerciseView.vue'), meta: { title: '动作教学', group: '训练' } },
      { path: 'analysis', name: 'analysis', component: () => import('@/views/AnalysisView.vue'), meta: { title: '数据分析', group: '训练' } },
      { path: 'food', name: 'food', component: () => import('@/views/FoodView.vue'), meta: { title: '饮食记录', group: '健康管理' } },
      { path: 'body', name: 'body', component: () => import('@/views/BodyView.vue'), meta: { title: '身体数据', group: '健康管理' } },
      { path: 'community', name: 'community', component: () => import('@/views/CommunityView.vue'), meta: { title: '社区', group: '互动' } },
      { path: 'ai', name: 'ai', component: () => import('@/views/AiView.vue'), meta: { title: 'AI 健身助手', group: '互动' } },
      { path: 'profile', name: 'profile', component: () => import('@/views/ProfileView.vue'), meta: { title: '个人中心', group: '互动' } },
    ],
  },
  { path: '/:pathMatch(.*)*', name: 'not-found', component: () => import('@/views/NotFoundView.vue'), meta: { public: true, title: '页面不存在' } },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 }),
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  // 404 兜底页对所有人可见（不参与登录跳转）
  if (to.name === 'not-found') return true
  if (!to.meta.public && !auth.isLoggedIn) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.meta.public && auth.isLoggedIn) {
    return { name: 'home' }
  }
  return true
})

router.afterEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} · FitHub` : 'FitHub · 智能健身管理'
})

export default router
