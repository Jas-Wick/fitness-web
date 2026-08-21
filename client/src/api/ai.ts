import request from './request'

export function aiChat(question: string) {
  return request.post<string>('/ai/chat', { question })
}

export function aiTrainingPlan(userProfile: string) {
  return request.post<string>('/ai/training-plan', { userProfile })
}

export function aiDietAnalysis(dietRecords: string) {
  return request.post<string>('/ai/diet-analysis', { dietRecords })
}
