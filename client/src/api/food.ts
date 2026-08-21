import request from './request'
import type { FoodRecordVO, FoodRecordRequest, FoodStatVO, PageResult } from '@/types'

export function listFoods(params: { page?: number; size?: number }) {
  return request.get<PageResult<FoodRecordVO>>('/food', { params })
}

export function createFood(data: FoodRecordRequest) {
  return request.post<FoodRecordVO>('/food', data)
}

export function updateFood(id: number, data: FoodRecordRequest) {
  return request.put<FoodRecordVO>(`/food/${id}`, data)
}

export function deleteFood(id: number) {
  return request.delete<void>(`/food/${id}`)
}

export function getFoodStat(start: string, end: string) {
  return request.get<FoodStatVO>('/food/stat', { params: { start, end } })
}
