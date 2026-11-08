import { useState, type FormEvent } from 'react'
import { Link, Navigate, useNavigate } from 'react-router'
import { register } from '../api/auth'
import { ApiError } from '../api/client'
import { useAuth } from '../auth/authContext'

/** 項目名 → エラーメッセージ（例：{ email: 'メールアドレスの形式が…' }） */
type FieldErrors = Partial<Record<'email' | 'password' | 'passwordConfirm', string>>

/** SC-02 新規登録画面 */
export function RegisterPage() {
  const { user } = useAuth()
  const navigate = useNavigate()

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [passwordConfirm, setPasswordConfirm] = useState('')
  const [fieldErrors, setFieldErrors] = useState<FieldErrors>({})
  const [error, setError] = useState<string | null>(null)
  const [submitting, setSubmitting] = useState(false)

  if (user) {
    return <Navigate to="/" replace />
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setFieldErrors({})
    setError(null)
    setSubmitting(true)
    try {
      await register({ email, password, passwordConfirm })
      // 登録成功：ログイン画面へ移り、登録完了メッセージを出す（FR-002）
      navigate('/login', { state: { registered: true } })
    } catch (e) {
      if (e instanceof ApiError && e.body.errors.length > 0) {
        // 各入力欄の直下にエラーを出す（画面レイアウト 10節 No.5）
        setFieldErrors({
          email: e.fieldMessage('email'),
          password: e.fieldMessage('password'),
          passwordConfirm: e.fieldMessage('passwordConfirm'),
        })
      } else {
        setError(e instanceof ApiError ? e.message : '通信エラーが発生しました。')
      }
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <div className="auth-page">
      <h1>新規ユーザー登録</h1>

      {error && <p className="message error">{error}</p>}

      <form onSubmit={handleSubmit} className="form">
        <label>
          メールアドレス
          <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} autoComplete="email" />
          {fieldErrors.email && <span className="field-error">{fieldErrors.email}</span>}
        </label>
        <label>
          パスワード
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete="new-password"
          />
          <span className="hint">8文字以上、英字と数字を含めてください</span>
          {fieldErrors.password && <span className="field-error">{fieldErrors.password}</span>}
        </label>
        <label>
          パスワード（確認）
          <input
            type="password"
            value={passwordConfirm}
            onChange={(e) => setPasswordConfirm(e.target.value)}
            autoComplete="new-password"
          />
          {fieldErrors.passwordConfirm && <span className="field-error">{fieldErrors.passwordConfirm}</span>}
        </label>
        <button type="submit" disabled={submitting}>
          登録する
        </button>
      </form>

      <p className="auth-link">
        <Link to="/login">ログイン画面へ戻る</Link>
      </p>
    </div>
  )
}
