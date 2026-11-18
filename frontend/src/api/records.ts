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

/** 一覧用の記録データ（API設計書 5.1節。details は含まない） */
export type RecordSummary = Omit<MindRecord, 'details'>

/** 記録一覧の検索条件。空文字の条件は API に送らない */
export type SearchConditions = {
  mode: RecordMode | ''
  from: string
  to: string
  title: string
}

/** API-05 最近の記録取得 */
export function fetchRecentRecords(): Promise<RecordSummary[]> {
  return apiFetch<RecordSummary[]>('/api/records/recent')
}

/** API-06 記録一覧・検索 */
export function searchRecords(conditions: SearchConditions): Promise<RecordSummary[]> {
  // URLSearchParams：?mode=DIARY&title=%E4%BB%95%E4%BA%8B のような URL の後ろの部分を作る（日本語も変換される）
  const params = new URLSearchParams()
  // 省略しない形：
  // const entries = Object.entries(conditions)  // [['mode', 'DIARY'], ['from', ''], ...]
  // for (const entry of entries) {
  //   const key = entry[0]
  //   const value = entry[1]
  //   if (value !== '') { params.append(key, value) }
  // }
  for (const [key, value] of Object.entries(conditions)) {
    if (value !== '') {
      params.append(key, value)
    }
  }
  const query = params.toString()
  return apiFetch<RecordSummary[]>(query ? `/api/records?${query}` : '/api/records')
}
