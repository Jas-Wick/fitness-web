import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import * as authApi from '@/api/auth'
import { getProfile } from '@/api/user'
import { getAccessToken, getRefreshToken, setTokens, clearTokens } from '@/utils/token'
import type { LoginRequest, RegisterRequest, UserVO } from '@/types'

export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref(getAccessToken())
  const refreshToken = ref(getRefreshToken())
  const user = ref<UserVO | null>(null)

  const isLoggedIn = computed(() => !!accessToken.value)

  async function login(payload: LoginRequest) {
    const data = await authApi.login(payload)
    applyLogin(data)
  }

  async function register(payload: RegisterRequest) {
    const data = await authApi.register(payload)
    applyLogin(data)
  }

  async function fetchProfile() {
    if (!accessToken.value) return
    try {
      user.value = await getProfile()
    } catch {
      // 登录态失效由请求拦截器兜底
    }
  }

  function applyLogin(data: { accessToken: string; refreshToken: string; user: UserVO }) {
    accessToken.value = data.accessToken
    refreshToken.value = data.refreshToken
    user.value = data.user
    setTokens(data.accessToken, data.refreshToken)
  }

  function logout() {
    accessToken.value = ''
    refreshToken.value = ''
    user.value = null
    clearTokens()
  }

  return {
    accessToken,
    refreshToken,
    user,
    isLoggedIn,
    login,
    register,
    fetchProfile,
    logout,
  }
})
