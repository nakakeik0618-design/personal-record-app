import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router'
import { ApiError } from '../api/client'
import { fetchRecord, type MindRecord } from '../api/records'
import { displayTitle, formatDateTime } from '../records/format'
import { findMode } from '../records/modes'

/** SC-07 記録詳細画面 */
export function RecordDetailPage() {
  const { id } = useParams()
  const [record, setRecord] = useState<MindRecord | null>(null)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    // 画面を離れたあとに届いた結果は使わない
    let active = true
    fetchRecord(id ?? '')
      .then((result) => {
        if (active) {
          setRecord(result)
          setError(null)
        }
      })
      .catch((e: unknown) => {
        if (active) {
          setRecord(null)
          setError(e instanceof ApiError ? e.message : '通信エラーが発生しました。')
        }
      })
    return () => {
      active = false
    }
  }, [id])

  if (error) {
    return (
      <>
        <p className="message error">{error}</p>
        <div className="actions">
          <Link to="/records" className="button secondary">
            一覧へ戻る
          </Link>
        </div>
      </>
    )
  }
  if (!record) {
    return <p className="loading">読み込み中…</p>
  }

  const mode = findMode(record.mode)

  return (
    <>
      <h1>{displayTitle(record.title)}</h1>

      <dl className="record-meta">
        <dt>モード</dt>
        <dd>{mode.label}</dd>
        <dt>対象日</dt>
        <dd>{record.targetDate}</dd>
        <dt>作成日時</dt>
        <dd>{formatDateTime(record.createdAt)}</dd>
        <dt>更新日時</dt>
        <dd>{formatDateTime(record.updatedAt)}</dd>
      </dl>

      {mode.fields.map((field) => (
        <section key={field.key} className="record-field">
          <h2>{field.label}</h2>
          <p>{record.details[field.key]}</p>
        </section>
      ))}

      <div className="actions">
        <Link to="/records" className="button secondary">
          一覧へ戻る
        </Link>
      </div>
    </>
  )
}
