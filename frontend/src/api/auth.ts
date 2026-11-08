// 認証API（API設計書 4節）
import { apiFetch } from './client'

/** ログイン中のユーザー（API-02, API-04 のレスポンス） */
export type User = {
  id: number
  email: string
}

export type RegisterInput = {
  email: string
  password: string
  passwordConfirm: string
}

/** API-01 ユーザー登録 */
export function register(input: RegisterInput): Promise<void> {
  return apiFetch<void>('/api/auth/register', { method: 'POST', body: input })
}

/** API-02 ログイン */
export function login(email: string, password: string): Promise<User> {
  return apiFetch<User>('/api/auth/login', { method: 'POST', body: { email, password } })
}

/** API-03 ログアウト */
export function logout(): Promise<void> {
  return apiFetch<void>('/api/auth/logout', { method: 'POST' })
}

/** API-04 ログインユーザー取得（未ログインの場合は ApiError（401）になる） */
export function fetchMe(): Promise<User> {
  return apiFetch<User>('/api/auth/me')
}
