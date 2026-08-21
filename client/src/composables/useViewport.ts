import { onBeforeUnmount, onMounted, ref } from 'vue'

export const TABLET_BREAKPOINT = 1024

/**
 * 视口断点监听。用于侧边栏抽屉折叠与响应式布局。
 */
export function useViewport() {
  const width = ref(window.innerWidth)
  const isMobile = ref(width.value < TABLET_BREAKPOINT)
  const isDesktop = ref(width.value >= TABLET_BREAKPOINT)

  function onResize() {
    width.value = window.innerWidth
    isMobile.value = width.value < TABLET_BREAKPOINT
    isDesktop.value = width.value >= TABLET_BREAKPOINT
  }

  onMounted(() => {
    window.addEventListener('resize', onResize)
  })

  onBeforeUnmount(() => {
    window.removeEventListener('resize', onResize)
  })

  return { width, isMobile, isDesktop }
}
