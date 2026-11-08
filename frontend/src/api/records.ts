// 記録API（API設計書 5節）
import { apiFetch } from './client'

export type RecordMode = 'PROBLEM_SOLVING' | 'DIARY' | 'DAILY_REVIEW'

/** モード別の入力内容（未入力の項目は null） */
export type RecordDetails = Record<string, string | null>

/** 記録データ（API設計書 5.1節） */
export type MindRecord = {
  id: number
  mode: RecordMode
  title: string | null
  targetDate: string
  createdAt: string
  updatedAt: string
  details: RecordDetails
}

export type CreateRecordInput = {
  mode: RecordMode
  title: string
  targetDate: string
  details: Record<string, string>
}

/** API-07 記録詳細取得 */
export function fetchRecord(id: string): Promise<MindRecord> {
  return apiFetch<MindRecord>(`/api/records/${encodeURIComponent(id)}`)
}

/** API-08 記録作成 */
export function createRecord(input: CreateRecordInput): Promise<MindRecord> {
  return apiFetch<MindRecord>('/api/records', { method: 'POST', body: input })
}
