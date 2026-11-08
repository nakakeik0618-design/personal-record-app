// 画面に表示するときの形式（画面レイアウト 1.2節）

/** タイトルが未入力の記録は「（無題）」と表示する */
export function displayTitle(title: string | null): string {
  return title ?? '（無題）'
}

/** 2026-10-10T21:30:00 → 2026-10-10 21:30 */
export function formatDateTime(value: string): string {
  return value.slice(0, 16).replace('T', ' ')
}

/** 今日の日付（YYYY-MM-DD）。toISOString は UTC になるため使わない */
export function today(): string {
  const now = new Date()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const date = String(now.getDate()).padStart(2, '0')
  return `${now.getFullYear()}-${month}-${date}`
}
