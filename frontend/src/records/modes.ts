// モードごとの表示名・説明文・入力項目（画面レイアウト 5〜6節）
import type { RecordMode } from '../api/records'

export type ModeDefinition = {
  mode: RecordMode
  /** 記録作成画面の URL に使う名前（/records/new/diary など） */
  slug: string
  label: string
  description: string
  fields: { key: string; label: string }[]
}

export const MODES: ModeDefinition[] = [
  {
    mode: 'PROBLEM_SOLVING',
    slug: 'problem-solving',
    label: '問題解決',
    description: '困っていること・悩んでいることを整理し、解決策を考える',
    fields: [
      { key: 'goal', label: '目標' },
      { key: 'currentState', label: '現状' },
      { key: 'gap', label: 'ギャップ' },
      { key: 'cause', label: '原因' },
      { key: 'countermeasure', label: '対策' },
    ],
  },
  {
    mode: 'DIARY',
    slug: 'diary',
    label: '日記',
    description: '形式を決めず自由に記録する',
    fields: [{ key: 'memo', label: 'メモ' }],
  },
  {
    mode: 'DAILY_REVIEW',
    slug: 'daily-review',
    label: '1日の振り返り',
    description: 'その日の行動を振り返り、自己フィードバックする',
    fields: [
      { key: 'events', label: '今日の出来事' },
      { key: 'reflection', label: '反省点' },
      { key: 'learning', label: '学び' },
      { key: 'insight', label: '気付き' },
      { key: 'tomorrowTasks', label: '明日やること' },
    ],
  },
]

export function findModeBySlug(slug: string | undefined): ModeDefinition | undefined {
  return MODES.find((mode) => mode.slug === slug)
}

export function findMode(mode: RecordMode): ModeDefinition {
  return MODES.find((definition) => definition.mode === mode) as ModeDefinition
}
