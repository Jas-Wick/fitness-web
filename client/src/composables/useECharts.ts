import { onBeforeUnmount, onMounted, shallowRef, type Ref } from 'vue'
import * as echarts from 'echarts'

interface UseEChartsOptions {
  /** 是否自动监听窗口与容器尺寸变化（默认 true） */
  autosize?: boolean
}

/**
 * ECharts 生命周期封装：init / setOption / resize / clear / dispose。
 * 除 window resize 外，用 ResizeObserver 监听图表容器尺寸变化——
 * 侧边栏折叠、响应式断点切换、内容回流等不触发 window resize 的场景也能正确缩放，
 * 避免 canvas 停留在旧尺寸溢出容器造成重叠。
 */
export function useECharts(elRef: Ref<HTMLElement | undefined>, options: UseEChartsOptions = {}) {
  const { autosize = true } = options
  const chart = shallowRef<echarts.ECharts | null>(null)
  let ro: ResizeObserver | null = null

  function init(): echarts.ECharts | null {
    if (!elRef.value) return null
    if (!chart.value) {
      chart.value = echarts.init(elRef.value)
    }
    return chart.value
  }

  function setOption(option: echarts.EChartsOption, opts?: { notMerge?: boolean; lazyUpdate?: boolean }) {
    const c = init()
    if (c) {
      c.setOption(option, opts)
      // 过渡动画期间容器宽度可能为 0，数据就绪后校正一次
      if (c.getWidth() === 0 || c.getHeight() === 0) {
        c.resize()
      }
    }
  }

  function resize() {
    chart.value?.resize()
  }

  function clear() {
    chart.value?.clear()
  }

  function dispose() {
    chart.value?.dispose()
    chart.value = null
  }

  function onWindowResize() {
    resize()
  }

  onMounted(() => {
    init()
    if (autosize) {
      window.addEventListener('resize', onWindowResize)
      if (elRef.value && typeof ResizeObserver !== 'undefined') {
        ro = new ResizeObserver(() => resize())
        ro.observe(elRef.value)
      }
    }
  })

  onBeforeUnmount(() => {
    if (autosize) {
      window.removeEventListener('resize', onWindowResize)
      ro?.disconnect()
      ro = null
    }
    dispose()
  })

  return { chart, init, setOption, resize, clear, dispose }
}
