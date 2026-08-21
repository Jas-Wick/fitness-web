/**
 * 日期格式化工具
 * 收敛各视图重复的 today()/formatTime() 实现。
 */

/** 本地时区的 YYYY-MM-DD（避免 toISOString 的 UTC 偏移） */
export function today(): string {
  const d = new Date()
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

/** YYYY-MM-DDTHH:mm:ss → YYYY-MM-DD HH:mm（截断分钟） */
export function formatTime(t: string | null | undefined): string {
  return t ? t.replace('T', ' ').slice(0, 16) : ''
}

/** YYYY-MM-DD → YYYY-MM-DD HH:mm */
export function formatDateTime(t: string | null | undefined): string {
  return t ? t.replace('T', ' ').slice(0, 16) : ''
}

/** YYYY-MM → 'YYYY-MM' */
export function monthKey(year: number, month: number): string {
  return `${year}-${String(month).padStart(2, '0')}`
}

/** 给定年月返回天数 */
export function daysInMonth(year: number, month: number): number {
  return new Date(year, month, 0).getDate()
}

export function isSameDay(a: string, b: string): boolean {
  return a === b
}

/** 本周的起始日期（周一） */
export function weekStart(d = new Date()): Date {
  const copy = new Date(d)
  const day = copy.getDay() || 7 // 周日=0 → 7
  copy.setDate(copy.getDate() - day + 1)
  return copy
}

export function dateToStr(d: Date): string {
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
}

/** 当天时段问候 */
export function greeting(): string {
  const h = new Date().getHours()
  if (h < 6) return '夜深了'
  if (h < 9) return '早上好'
  if (h < 12) return '上午好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
}

/** 中文长日期：2026年8月20日 */
export function zhDate(d = new Date()): string {
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日`
}
