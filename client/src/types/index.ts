/** 后端统一返回结构 Result<T> */
export interface Result<T = unknown> {
  code: number
  message: string
  data: T
}

/** 分页结果 PageResult<T> */
export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  size: number
}

/** 用户信息 */
export interface UserVO {
  id: number
  userCode: string
  username: string
  nickname: string
  avatarUrl: string | null
  gender: number
  birthDate: string | null
  age: number | null
  height: number | null
  weight: number | null
  fitnessGoal: string | null
  fitnessLevel: string | null
  role: string
  createTime: string
}

/** 登录响应 */
export interface LoginVO {
  accessToken: string
  refreshToken: string
  user: UserVO
}

/** 健身动作 */
export interface ExerciseVO {
  id: number
  name: string
  bodyPart: string
  description: string
  steps: string
  precautions: string
  imageUrl: string | null
  videoUrl: string | null
  viewCount: number
}

/** 训练动作组（子表明细） */
export interface TrainingSetVO {
  id?: number
  bodyPart: string
  exerciseName?: string
  weight?: number
  reps?: number
  sets?: number
}

/** 训练打卡记录 */
export interface TrainingRecordVO {
  id: number
  trainDate: string
  mode: number
  durationValue: number | null
  durationUnit: string | null
  caloriesBurned: number | null
  remark: string | null
  sets: TrainingSetVO[]
  createTime: string
}

/** 训练打卡请求 */
export interface TrainingRecordRequest {
  trainDate: string
  mode: number
  durationValue?: number
  durationUnit?: string
  caloriesBurned?: number
  remark?: string
  sets: TrainingSetVO[]
}

/** 连续打卡统计 */
export interface StreakVO {
  currentStreak: number
  longestStreak: number
  totalDays: number
}

/** 单日训练统计 */
export interface TrainingDailyStat {
  trainDate: string
  recordCount: number
  totalDurationMinutes: number
  totalCalories: number
}

/** 帖子 */
export interface PostVO {
  id: number
  title: string
  content: string
  postType: string
  originalPostId: number | null
  originalTitle: string | null
  originalAuthorNickname: string | null
  originalContent: string | null
  likeCount: number
  commentCount: number
  favoriteCount: number
  viewCount: number
  authorId: number
  authorNickname: string | null
  authorAvatar: string | null
  createTime: string
  liked: boolean
  favorited: boolean
}

/** 评论 */
export interface CommentVO {
  id: number
  postId: number
  parentId: number | null
  content: string
  likeCount: number
  userId: number
  userNickname: string | null
  userAvatar: string | null
  createTime: string
}

/** 帖子发布请求 */
export interface PostRequest {
  title: string
  content: string
  postType: string
}

/** 评论请求 */
export interface CommentRequest {
  postId: number
  content: string
  parentId?: number
}

/** 转发请求 */
export interface ForwardRequest {
  content?: string
}

/** 登录请求 */
export interface LoginRequest {
  username: string
  password: string
}

/** 注册请求 */
export interface RegisterRequest {
  username: string
  password: string
  nickname?: string
}

/** 饮食记录 */
export interface FoodRecordVO {
  id: number
  foodName: string
  calories: number | null
  protein: number | null
  carbs: number | null
  fat: number | null
  mealType: number | null
  eatTime: string
  remark: string | null
  createTime: string
}

/** 饮食记录请求 */
export interface FoodRecordRequest {
  foodName: string
  calories?: number
  protein?: number
  carbs?: number
  fat?: number
  mealType?: number
  eatTime: string
  remark?: string
}

/** 饮食统计 */
export interface FoodStatVO {
  totalCalories: number
  totalProtein: number
  totalCarbs: number
  totalFat: number
}

/** 身体数据 */
export interface BodyDataVO {
  id: number
  recordDate: string
  weight: number | null
  bodyFatRate: number | null
  muscleMass: number | null
  chest: number | null
  waist: number | null
  hip: number | null
  bmi: number | null
  remark: string | null
  createTime: string
}

/** 身体数据请求 */
export interface BodyDataRequest {
  recordDate: string
  weight?: number
  bodyFatRate?: number
  muscleMass?: number
  chest?: number
  waist?: number
  hip?: number
  remark?: string
}

/** BMI 结果 */
export interface BmiVO {
  bmi: number | null
  category: string
  suggestion: string
}

/** 更新资料请求 */
export interface UpdateProfileRequest {
  nickname?: string
  gender?: number
  birthDate?: string
  height?: number
  weight?: number
  fitnessGoal?: string
  fitnessLevel?: string
}

/** 修改密码请求 */
export interface ChangePasswordRequest {
  oldPassword: string
  newPassword: string
}

/** 站点统计 */
export interface SiteStatsVO {
  userCount: number
  trainingCount: number
  foodCount: number
  postCount: number
  commentCount: number
}
