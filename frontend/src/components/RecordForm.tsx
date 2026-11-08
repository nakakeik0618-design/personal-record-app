import { useState, type FormEvent } from 'react'
import { Link } from 'react-router'
import { ApiError } from '../api/client'
import type { ModeDefinition } from '../records/modes'

export type RecordFormValues = {
  title: string
  targetDate: string
  details: Record<string, string>
}

type RecordFormProps = {
  heading: string
  mode: ModeDefinition
  initialValues: RecordFormValues
  cancelTo: string
  onSubmit: (values: RecordFormValues) => Promise<void>
}

/** 記録の入力フォーム（SC-05。入力項目はモードごとに変わる） */
export function RecordForm({ heading, mode, initialValues, cancelTo, onSubmit }: RecordFormProps) {
  const [title, setTitle] = useState(initialValues.title)
  const [targetDate, setTargetDate] = useState(initialValues.targetDate)
  const [details, setDetails] = useState(initialValues.details)
  const [message, setMessage] = useState<string | null>(null)
  const [apiError, setApiError] = useState<ApiError | null>(null)
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setMessage(null)
    setApiError(null)
    setSubmitting(true)
    try {
      await onSubmit({ title, targetDate, details })
    } catch (e) {
      if (e instanceof ApiError) {
        setApiError(e)
        setMessage(e.message)
      } else {
        setMessage('通信エラーが発生しました。')
      }
    } finally {
      setSubmitting(false)
    }
  }

  const titleError = apiError?.fieldMessage('title')
  const targetDateError = apiError?.fieldMessage('targetDate')

  return (
    <>
      <h1>{heading}</h1>

      {message && <p className="message error">{message}</p>}

      <form onSubmit={handleSubmit} className="form">
        <label>
          対象日
          <input type="date" value={targetDate} onChange={(e) => setTargetDate(e.target.value)} />
          {targetDateError && <span className="field-error">{targetDateError}</span>}
        </label>
        <label>
          タイトル
          <input type="text" value={title} onChange={(e) => setTitle(e.target.value)} />
          {titleError && <span className="field-error">{titleError}</span>}
        </label>

        {mode.fields.map((field) => {
          const fieldError = apiError?.fieldMessage(`details.${field.key}`)
          return (
            <label key={field.key}>
              {field.label}
              <textarea
                rows={5}
                value={details[field.key] ?? ''}
                onChange={(e) => setDetails({ ...details, [field.key]: e.target.value })}
              />
              {fieldError && <span className="field-error">{fieldError}</span>}
            </label>
          )
        })}

        <div className="actions split">
          <Link to={cancelTo} className="button secondary">
            キャンセル
          </Link>
          <button type="submit" disabled={submitting}>
            保存する
          </button>
        </div>
      </form>
    </>
  )
}
