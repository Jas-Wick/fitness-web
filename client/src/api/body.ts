import request from './request'
import type { BodyDataVO, BodyDataRequest, BmiVO, PageResult } from '@/types'

export function listBody(params: { page?: number; size?: number }) {
  return request.get<PageResult<BodyDataVO>>('/body', { params })
}

export function createBody(data: BodyDataRequest) {
  return request.post<BodyDataVO>('/body', data)
}

export function updateBody(id: number, data: BodyDataRequest) {
  return request.put<BodyDataVO>(`/body/${id}`, data)
}

export function deleteBody(id: number) {
  return request.delete<void>(`/body/${id}`)
}

export function getBodyTrend(start?: string, end?: string) {
  return request.get<BodyDataVO[]>('/body/trend', { params: { start, end } })
}

export function getBmiCurrent() {
  return request.get<BmiVO>('/bmi/current')
}
