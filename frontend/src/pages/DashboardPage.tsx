import { useAuth } from '../auth/authContext'

/**
 * SC-03 ダッシュボード。
 * 「新しく記録する」「最近の記録」などは、記録機能の実装時に追加する。
 */
export function DashboardPage() {
  const { user } = useAuth()

  return (
    <>
      <h1>ダッシュボード</h1>
      <p>{user?.email} でログインしています。</p>
    </>
  )
}
