import request from './request'
import type { ChangePasswordRequest, UpdateProfileRequest, UserVO } from '@/types'

export function getProfile() {
  return request.get<UserVO>('/user/profile')
}

export function updateProfile(data: UpdateProfileRequest) {
  return request.put<UserVO>('/user/profile', data)
}

export function changePassword(data: ChangePasswordRequest) {
  return request.put<void>('/user/password', data)
}

export function uploadAvatar(file: File) {
  const form = new FormData()
  form.append('file', file)
  return request.post<string>('/user/avatar', form)
}
