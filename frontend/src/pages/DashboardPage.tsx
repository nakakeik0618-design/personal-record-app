import { useEffect, useState } from 'react'
import { Link } from 'react-router'
import { ApiError } from '../api/client'
import { fetchRecentRecords, type RecordSummary } from '../api/records'
import { RecordTable } from '../components/RecordTable'

/** SC-03 ダッシュボード */
export function DashboardPage() {
  // null は「読み込み中」
  const [records, setRecords] = useState<RecordSummary[] | null>(null)
  const [error, setError] = useState<string | null>(null)

  // 画面を開いたときに API-05 で最近の記録を取得する
  useEffect(() => {
    // 画面を離れたあとに届いた結果は使わない
    let active = true
    fetchRecentRecords()
      .then((result) => {
        if (active) {
          setRecords(result)
        }
      })
      .catch((e: unknown) => {
        if (active) {
          setError(e instanceof ApiError ? e.message : '通信エラーが発生しました。')
        }
      })
    return () => {
      active = false
    }
  }, [])

  return (
    <>
      <h1>ダッシュボード</h1>
      <div className="actions">
        <Link to="/records/new" className="button">
          ＋ 新しく記録する
        </Link>
      </div>

      <h2 className="section-heading">最近の記録</h2>
      {error && <p className="message error">{error}</p>}
      {!error && records === null && <p className="loading">読み込み中…</p>}
      {records && records.length === 0 && <p className="empty">まだ記録がありません。</p>}
      {records && records.length > 0 && <RecordTable records={records} showCreatedAt={false} />}

      <p className="more-link">
        <Link to="/records">すべての記録を見る →</Link>
      </p>
    </>
  )
}
