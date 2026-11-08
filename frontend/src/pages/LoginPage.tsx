import { useState, type FormEvent } from 'react'
import { Link, Navigate, useLocation, useNavigate } from 'react-router'
import { ApiError } from '../api/client'
import { useAuth } from '../auth/authContext'

/** SC-01 ログイン画面 */
export function LoginPage() {
  const { user, login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  // 新規登録画面から移ってきた場合は、登録完了のメッセージを出す（FR-002）
  const registered = (location.state as { registered?: boolean } | null)?.registered === true

  // すでにログイン中ならダッシュボードへ
  if (user) {
    return <Navigate to="/" replace />
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    // フォーム送信時のページ再読み込み（ブラウザの標準動作）を止める
    event.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      await login(email, password)
      navigate('/', { replace: true })
    } catch (e) {
      setError(e instanceof ApiError ? e.message : '通信エラーが発生しました。')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="auth-page">
      <h1>MindLog</h1>

      {registered && !error && <p className="message success">ユーザー登録が完了しました。ログインしてください。</p>}
      {error && <p className="message error">{error}</p>}

      <form onSubmit={handleSubmit} className="form">
        <label>
          メールアドレス
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} autoComplete="email" />
        </label>
        <label>
          パスワード
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete="current-password"
          />
        </label>
        <button type="submit" disabled={submitting}>
          ログイン
        </button>
      </form>

      <p className="auth-link">
        アカウントをお持ちでない方は <Link to="/register">新規登録</Link>
      </p>
    </div>
  )
}
