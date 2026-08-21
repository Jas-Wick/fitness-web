import { onBeforeUnmount, ref, watch } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { getAccessToken } from '@/utils/token'

/**
 * 解析当前登录用户头像为可展示的 URL。
 * 头像存于受保护的 /uploads/** 下，<img> 无法携带 token；
 * 这里用带 Authorization 的请求获取 blob URL 展示，避免公开头像资源。
 */
export function useAuthAvatar() {
  const auth = useAuthStore()
  const avatarUrl = ref('')
  let objectUrl: string | null = null

  async function resolve() {
    if (objectUrl) {
      URL.revokeObjectURL(objectUrl)
      objectUrl = null
    }
    const raw = auth.user?.avatarUrl || ''
    if (!raw) {
      avatarUrl.value = ''
      return
    }
    // 非受保护的绝对地址（如外部 URL）直接使用
    if (!raw.startsWith('/uploads/')) {
      avatarUrl.value = raw
      return
    }
    try {
      const r = await fetch(raw, {
        headers: { Authorization: `Bearer ${getAccessToken()}` },
      })
      if (r.ok) {
        const blob = await r.blob()
        objectUrl = URL.createObjectURL(blob)
        avatarUrl.value = objectUrl
      } else {
        avatarUrl.value = ''
      }
    } catch {
      avatarUrl.value = ''
    }
  }

  watch(
    () => auth.user?.avatarUrl,
    () => {
      resolve()
    },
    { immediate: true },
  )

  onBeforeUnmount(() => {
    if (objectUrl) URL.revokeObjectURL(objectUrl)
  })

  return { avatarUrl }
}
