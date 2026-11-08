import { Link } from 'react-router'
import { MODES } from '../records/modes'

/** SC-04 モード選択画面 */
export function ModeSelectPage() {
  return (
    <>
      <h1>記録のモードを選んでください</h1>

      <div className="mode-list">
        {MODES.map((mode) => (
          <Link key={mode.slug} to={`/records/new/${mode.slug}`} className="mode-card">
            <span className="mode-card-label">{mode.label}</span>
            <span className="mode-card-description">{mode.description}</span>
          </Link>
        ))}
      </div>

      <div className="actions">
        <Link to="/" className="button secondary">
          戻る
        </Link>
      </div>
    </>
  )
}
