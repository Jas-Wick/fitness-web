import { reactive } from 'vue'
import type { PageResult } from '@/types'

interface FetcherParams {
  page: number
  size: number
}

/**
 * 通用分页列表状态。
 * 返回单个 reactive 对象（list/total/page/size/loading + load/onPageChange），
 * 模板中可直接访问 records.list / records.total 等（reactive 自动解包）。
 * 收敛 Food/Body/Exercise/Community 等页的重复分页逻辑。
 */
export function useList<T>(
  fetcher: (params: FetcherParams) => Promise<PageResult<T>>,
  options: { page?: number; size?: number } = {},
) {
  const records = reactive({
    list: [] as T[],
    total: 0,
    page: options.page ?? 1,
    size: options.size ?? 10,
    loading: false,

    async load() {
      this.loading = true
      try {
        const data = await fetcher({ page: this.page, size: this.size })
        this.list = data.records
        this.total = data.total
      } catch {
        // 错误提示由请求拦截器统一处理
      } finally {
        this.loading = false
      }
    },

    onPageChange(p: number) {
      this.page = p
      this.load()
    },
  })

  return records
}
