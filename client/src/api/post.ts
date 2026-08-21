import request from './request'
import type { ForwardRequest, PageResult, PostRequest, PostVO } from '@/types'

export function listPosts(params: { page?: number; size?: number; postType?: string }) {
  return request.get<PageResult<PostVO>>('/post', { params })
}

export function createPost(data: PostRequest) {
  return request.post<PostVO>('/post', data)
}

export function likePost(id: number) {
  return request.post<void>(`/post/${id}/like`)
}

export function favoritePost(id: number) {
  return request.post<void>(`/post/${id}/favorite`)
}

export function forwardPost(id: number, data: ForwardRequest) {
  return request.post<PostVO>(`/post/${id}/forward`, data)
}
