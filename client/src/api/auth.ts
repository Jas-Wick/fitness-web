import request from './request'
import type { LoginRequest, RegisterRequest, LoginVO } from '@/types'

export function login(data: LoginRequest) {
  return request.post<LoginVO>('/auth/login', data)
}

export function register(data: RegisterRequest) {
  return request.post<LoginVO>('/auth/register', data)
}
