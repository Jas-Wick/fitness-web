import request from './request'
import type { ExerciseVO, PageResult } from '@/types'

export interface ExerciseQuery {
  page?: number
  size?: number
  bodyPart?: string
  keyword?: string
}

export function listExercises(params: ExerciseQuery) {
  return request.get<PageResult<ExerciseVO>>('/exercise', { params })
}

export function getExerciseDetail(id: number) {
  return request.get<ExerciseVO>(`/exercise/${id}`)
}
