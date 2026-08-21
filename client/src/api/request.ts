import axios, {
  type AxiosInstance,
  type AxiosRequestConfig,
  type InternalAxiosRequestConfig,
} from 'axios'
import { ElMessage } from 'element-plus'
import { getAccessToken, getRefreshToken, setTokens, clearTokens } from '@/utils/token'
import type { LoginVO, Result } from '@/types'

const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15000,
})

service.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = getAccessToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

let refreshPromise: Promise<string | null> | null = null

/** 用原生 axios 调用 refresh，避免进入拦截器造成递归 */
function doRefresh(): Promise<string | null> {
  if (!refreshPromise) {
    const refreshToken = getRefreshToken()
    if (!refreshToken) {
      return Promise.resolve(null)
    }
    refreshPromise = axios
      .post<Result<LoginVO>>(`${service.defaults.baseURL}/auth/refresh`, { refreshToken })
      .then((res) => {
        const body = res.data
        if (body.code === 200) {
          setTokens(body.data.accessToken, body.data.refreshToken)
          return body.data.accessToken
        }
        clearTokens()
        return null
      })
      .catch(() => {
        clearTokens()
        return null
      })
      .finally(() => {
        refreshPromise = null
      })
  }
  return refreshPromise
}

service.interceptors.response.use(
  (response): any => {
    const body = response.data as Result<unknown>
    if (body && typeof body.code === 'number' && body.code !== 200) {
      return handleError(body, response.config)
    }
    return body.data
  },
  async (error) => {
    // 认证失败现在返回 HTTP 401/403 + JSON body，统一从这里处理
    const body = error.response?.data as Result<unknown> | undefined
    const config = error.config
    if (body && typeof body.code === 'number' && config) {
      if (body.code === 401) {
        return handleError(body, config)
      }
      ElMessage.error(body.message || '请求失败')
      return Promise.reject(new Error(body.message || 'request error'))
    }
    ElMessage.error('网络错误，请稍后重试')
    return Promise.reject(error)
  },
)

async function handleError(body: Result<unknown>, config: AxiosRequestConfig | undefined) {
  // 401：尝试刷新 access token 后重放一次
  if (body.code === 401 && config && !String(config.url).includes('/auth/refresh')) {
    const newToken = await doRefresh()
    if (newToken) {
      return service.request(config)
    }
    clearTokens()
    ElMessage.error('登录已过期，请重新登录')
    // 动态 import 避免 router → stores/auth → api/auth → request 的循环依赖
    const { default: router } = await import('@/router')
    router.push({ name: 'login', query: { redirect: location.pathname + location.search } })
    return Promise.reject(new Error('unauthorized'))
  }
  ElMessage.error(body.message || '请求失败')
  return Promise.reject(new Error(body.message || 'request error'))
}

/** 类型化请求包装：拦截器已解包 Result，直接返回 data */
const request = {
  get<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return service.get(url, config) as unknown as Promise<T>
  },
  post<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
    return service.post(url, data, config) as unknown as Promise<T>
  },
  put<T>(url: string, data?: unknown, config?: AxiosRequestConfig): Promise<T> {
    return service.put(url, data, config) as unknown as Promise<T>
  },
  delete<T>(url: string, config?: AxiosRequestConfig): Promise<T> {
    return service.delete(url, config) as unknown as Promise<T>
  },
}

export default request
