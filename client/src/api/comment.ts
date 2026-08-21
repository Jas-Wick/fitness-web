import request from './request'
import type { CommentRequest, CommentVO, PageResult } from '@/types'

export function listComments(postId: number, page = 1, size = 50) {
  return request.get<PageResult<CommentVO>>('/comment', { params: { postId, page, size } })
}

export function createComment(data: CommentRequest) {
  return request.post<CommentVO>('/comment', data)
}
