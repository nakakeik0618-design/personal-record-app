import { Link, Outlet, useNavigate } from 'react-router'
import { useAuth } from '../auth/authContext'

/**
 * ログイン後の画面の共通レイアウト。共通ヘッダー（画面レイアウト 1.1節）を表示する。
 */
export function Layout() {
  const { logout } = useAuth()
  const navigate = useNavigate()

  async function handleLogout() {
    await logout()
    navigate('/login', { replace: true })
  }

  return (
    <>
      <header className="header">
        <span className="header-title">MindLog</span>
        <nav className="header-nav">
          <Link to="/">ダッシュボード</Link>
          <button type="button" onClick={handleLogout}>
            ログアウト
          </button>
        </nav>
      </header>
      <main className="main">
        <Outlet />
      </main>
    </>
  )
}
