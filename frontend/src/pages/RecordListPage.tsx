import { useEffect, useState, type FormEvent } from 'react'
import { Link } from 'react-router'
import { ApiError } from '../api/client'
import { searchRecords, type RecordMode, type RecordSummary, type SearchConditions } from '../api/records'
import { RecordTable } from '../components/RecordTable'
import { MODES } from '../records/modes'

const EMPTY_CONDITIONS: SearchConditions = { mode: '', from: '', to: '', title: '' }

/** 日付の条件をチェックする（FR-022, FR-023）。問題なければ null */
function validateDates({ from, to }: SearchConditions): string | null {
  if ((from === '') !== (to === '')) {
    return '開始日と終了日は両方指定してください。'
  }
  // YYYY-MM-DD 形式の文字列は、文字列のまま比べても日付の前後と一致する
  if (from !== '' && from > to) {
    return '開始日は終了日以前の日付を指定してください。'
  }
  return null
}

/** SC-06 記録一覧画面 */
export function RecordListPage() {
  // 入力中の検索条件
  const [conditions, setConditions] = useState<SearchConditions>(EMPTY_CONDITIONS)
  // null は「読み込み中」
  const [records, setRecords] = useState<RecordSummary[] | null>(null)
  const [error, setError] = useState<string | null>(null)

  async function search(target: SearchConditions) {
    const dateError = validateDates(target)
    if (dateError) {
      // エラーのときは API を呼ばない（API設計書 API-06）
      setError(dateError)
      return
    }
    setError(null)
    setRecords(null)
    try {
      setRecords(await searchRecords(target))
    } catch (e) {
      setRecords([])
      setError(e instanceof ApiError ? e.message : '通信エラーが発生しました。')
    }
  }

  // 画面を開いたときは条件なしで全件を表示する
  useEffect(() => {
    let active = true
    searchRecords(EMPTY_CONDITIONS)
      .then((result) => {
        if (active) {
          setRecords(result)
        }
      })
      .catch((e: unknown) => {
        if (active) {
          setRecords([])
          setError(e instanceof ApiError ? e.message : '通信エラーが発生しました。')
        }
      })
    return () => {
      active = false
    }
  }, [])

  function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    search(conditions)
  }

  function handleClear() {
    setConditions(EMPTY_CONDITIONS)
    search(EMPTY_CONDITIONS)
  }

  /**
   * 検索条件の1項目だけを書き換える。
   * 省略しない形：
   * const next = Object.assign({}, conditions)  // conditions をコピー
   * next[key] = value                           // 1項目だけ上書き
   * setConditions(next)
   */
  function update(key: keyof SearchConditions, value: string) {
    setConditions({ ...conditions, [key]: value })
  }

  return (
    <>
      <h1>記録一覧</h1>

      <form onSubmit={handleSubmit} className="search-form">
        <label>
          モード
          <select value={conditions.mode} onChange={(e) => update('mode', e.target.value as RecordMode | '')}>
            <option value="">指定なし</option>
            {MODES.map((mode) => (
              <option key={mode.mode} value={mode.mode}>
                {mode.label}
              </option>
            ))}
          </select>
        </label>
        <div className="search-dates">
          <label>
            対象日（開始日）
            <input type="date" value={conditions.from} onChange={(e) => update('from', e.target.value)} />
          </label>
          <span className="search-dates-separator">〜</span>
          <label>
            対象日（終了日）
            <input type="date" value={conditions.to} onChange={(e) => update('to', e.target.value)} />
          </label>
        </div>
        <label>
          タイトル
          <input type="text" value={conditions.title} onChange={(e) => update('title', e.target.value)} />
        </label>
        <div className="search-buttons">
          {/* type="button"：押してもフォームの送信（検索）をしないボタン */}
          <button type="button" className="secondary" onClick={handleClear}>
            クリア
          </button>
          <button type="submit">検索</button>
        </div>
      </form>

      {error && <p className="message error">{error}</p>}
      {!error && records === null && <p className="loading">読み込み中…</p>}
      {records && records.length === 0 && !error && <p className="empty">該当する記録がありません。</p>}
      {/* showCreatedAt だけ書くのは showCreatedAt={true} の省略形 */}
      {records && records.length > 0 && <RecordTable records={records} showCreatedAt />}

      <div className="actions">
        <Link to="/" className="button secondary">
          ダッシュボードへ
        </Link>
      </div>
    </>
  )
}
