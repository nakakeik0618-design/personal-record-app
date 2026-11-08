import { Link } from 'react-router'

/**
 * SC-03 ダッシュボード。
 * 「最近の記録」「すべての記録を見る」は、記録一覧の実装時に追加する。
 */
export function DashboardPage() {
  return (
    <>
      <h1>ダッシュボード</h1>
      <div className="actions">
        <Link to="/records/new" className="button">
          ＋ 新しく記録する
        </Link>
      </div>
    </>
  )
}
