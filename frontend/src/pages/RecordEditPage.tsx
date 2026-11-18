import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router'
import { ApiError } from '../api/client'
import { fetchRecord, updateRecord, type MindRecord } from '../api/records'
import { RecordForm, type RecordFormValues } from '../components/RecordForm'
import { findMode } from '../records/modes'

/** API の details（未入力は null）を、入力欄の値（未入力は空文字）に変換する */
function toFormDetails(details: MindRecord['details']): Record<string, string> {
  const values: Record<string, string> = {}
  for (const [key, value] of Object.entries(details)) {
    values[key] = value ?? ''
  }
  return values
}

/** SC-08 記録編集画面（SC-05 と同じ入力フォームを使う） */
export function RecordEditPage() {
  // URL の :id を取り出す。「= ''」は、取り出せなかったときに使う値
  // 省略しない形：
  // const params = useParams()
  // const id = params.id === undefined ? '' : params.id
  const { id = '' } = useParams()
  const navigate = useNavigate()
  const [record, setRecord] = useState<MindRecord | null>(null)
  const [error, setError] = useState<string | null>(null)

  // 画面を開いたときに API-07 で現在の値を取得する
  useEffect(() => {
    let active = true
    fetchRecord(id)
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

  async function handleSubmit(values: RecordFormValues) {
    await updateRecord(id, values)
    // 保存後は詳細画面へ。戻るボタンで編集画面に戻らないよう replace にする
    navigate(`/records/${id}`, { replace: true })
  }

  return (
    <RecordForm
      key={record.id}
      heading={`記録の編集（${mode.label}）`}
      mode={mode}
      // タイトル未入力の記録は空欄で表示する（「（無題）」は入れない）
      initialValues={{
        title: record.title ?? '',
        targetDate: record.targetDate,
        details: toFormDetails(record.details),
      }}
      cancelTo={`/records/${id}`}
      onSubmit={handleSubmit}
    />
  )
}
