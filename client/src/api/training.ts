import request from './request'
import type { PageResult, StreakVO, TrainingDailyStat, TrainingRecordRequest, TrainingRecordVO } from '@/types'

export function listTraining(params: { page?: number; size?: number }) {
  return request.get<PageResult<TrainingRecordVO>>('/training', { params })
}

export function createTraining(data: TrainingRecordRequest) {
  return request.post<TrainingRecordVO>('/training', data)
}

export function updateTraining(id: number, data: TrainingRecordRequest) {
  return request.put<TrainingRecordVO>(`/training/${id}`, data)
}

export function deleteTraining(id: number) {
  return request.delete<void>(`/training/${id}`)
}

export function getTraining(id: number) {
  return request.get<TrainingRecordVO>(`/training/${id}`)
}

export function getStreak() {
  return request.get<StreakVO>('/training/streak')
}

/** 某月已打卡日期（后端返回 YYYY-MM-DD 字符串数组） */
export function getTrainingCalendar(year: number, month: number) {
  return request.get<string[]>('/training/calendar', { params: { year, month } })
}

export function getTrainingStats(days = 30) {
  return request.get<TrainingDailyStat[]>('/training/stats', { params: { days } })
}
