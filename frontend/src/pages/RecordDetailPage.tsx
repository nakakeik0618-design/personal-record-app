import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router'
import { ApiError } from '../api/client'
import { deleteRecord, fetchRecord, type MindRecord } from '../api/records'
import { displayTitle, formatDateTime } from '../records/format'
import { findMode } from '../records/modes'

/** SC-07 記録詳細画面 */
export function RecordDetailPage() {
  const { id } = useParams()
  const [record, setRecord] = useState<MindRecord | null>(null)
  const [error, setError] = useState<string | null>(null)
  // 削除確認ダイアログを表示しているか
  const [confirming, setConfirming] = useState(false)
  const [deleting, setDeleting] = useState(false)
  const [deleteError, setDeleteError] = useState<string | null>(null)
  const navigate = useNavigate()

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

  // ダイアログの「削除」：API-10 で削除し、記録一覧へ移動する
  async function handleDelete() {
    setDeleting(true)
    setDeleteError(null)
    try {
      await deleteRecord(id ?? '')
      navigate('/records', { replace: true })
    } catch (e) {
      setDeleteError(e instanceof ApiError ? e.message : '通信エラーが発生しました。')
      setDeleting(false)
    }
  }

  function closeDialog() {
    setConfirming(false)
    setDeleteError(null)
  }

  return (
    <>
      <div className="detail-heading">
        <h1>{displayTitle(record.title)}</h1>
        <div className="detail-buttons">
          <Link to={`/records/${record.id}/edit`} className="button secondary">
            編集
          </Link>
          <button type="button" className="danger" onClick={() => setConfirming(true)}>
            削除
          </button>
        </div>
      </div>

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

      {/* 削除確認ダイアログ（画面レイアウト 8.1節）。confirming が true のときだけ表示する */}
      {confirming && (
        <div className="dialog-backdrop">
          <div className="dialog" role="dialog" aria-modal="true" aria-labelledby="delete-dialog-message">
            <p id="delete-dialog-message">この記録を削除しますか？</p>
            {deleteError && <p className="message error">{deleteError}</p>}
            <div className="dialog-buttons">
              <button type="button" className="secondary" onClick={closeDialog} disabled={deleting}>
                キャンセル
              </button>
              <button type="button" className="danger" onClick={handleDelete} disabled={deleting}>
                削除
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  )
}
