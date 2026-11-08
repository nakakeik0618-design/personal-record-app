import { Navigate, Outlet } from 'react-router'
import { useAuth } from '../auth/authContext'

/**
 * ログインが必要な画面（SC-03〜SC-08）の手前に置く。
 * 未ログインならログイン画面へ移動する（FR-005）。
 */
export function RequireAuth() {
  const { user } = useAuth()

  if (user === undefined) {
    return <p className="loading">読み込み中…</p>
  }
  if (user === null) {
    // replace：ブラウザの「戻る」でこの画面に戻らないようにする
    return <Navigate to="/login" replace />
  }
  // Outlet：この下に入れ子で定義した画面（Route）がここに表示される
  return <Outlet />
}
