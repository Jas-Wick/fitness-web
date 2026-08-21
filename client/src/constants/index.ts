/**
 * FitHub 业务常量
 * 收敛散落在各视图中的硬编码数组与映射。
 * 注意：TRAINING_TYPES（训练集存储的短名）与 EXERCISE_BODY_PARTS（动作库筛选全名）
 * 是两个不同域，值不同且不得互相替换，否则会污染存量/增量数据。
 */

/** 训练打卡可选的训练类型（短名，写入 training_set.bodyPart，勿改值） */
export const TRAINING_TYPES = ['胸', '肩', '背', '腿', '手臂', '核心', '有氧', '其他'] as const

/** 动作库筛选部位（全名，与 exercise 表 bodyPart 列一致） */
export const EXERCISE_BODY_PARTS = ['胸部', '背部', '肩部', '腿部', '手臂', '核心'] as const

/** 训练模式 */
export const TRAINING_MODE: Record<number, string> = {
  1: '基础',
  2: '进阶',
}

/** 餐次 */
export const MEAL_TYPES = [
  { value: 1, label: '早餐' },
  { value: 2, label: '午餐' },
  { value: 3, label: '晚餐' },
  { value: 4, label: '加餐' },
] as const

export function mealName(v: number | null | undefined): string {
  return MEAL_TYPES.find((m) => m.value === v)?.label ?? '未分类'
}

export type MealTagType = 'primary' | 'success' | 'warning' | 'info'
export function mealTag(v: number | null | undefined): MealTagType {
  if (v === 1) return 'primary'
  if (v === 2) return 'success'
  if (v === 3) return 'warning'
  return 'info'
}

/** 健身目标 / 等级（个人资料） */
export const FITNESS_GOALS = ['增肌', '减脂', '塑形', '增强体能'] as const
export const FITNESS_LEVELS = ['初级', '中级', '高级'] as const

/** 社区帖子类型 */
export const POST_TYPES = ['健身经验', '饮食分享', '训练计划', '问题交流'] as const

/** 图表色板（与 tokens.css 的 --fth-chart-* 对齐） */
export const CHART_COLORS = {
  primary: '#f97316',
  blue: '#3b82f6',
  purple: '#8b5cf6',
  amber: '#f59e0b',
  green: '#16a34a',
  red: '#ef4444',
} as const
