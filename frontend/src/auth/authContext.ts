// ログイン状態をアプリ全体で共有するための「コンテキスト」
import { createContext, useContext } from 'react'
import type { User } from '../api/auth'

export type AuthContextValue = {
  /** ログイン中のユーザー。null は未ログイン、undefined は確認中（API-04 の結果待ち） */
  user: User | null | undefined
  login: (email: string, password: string) => Promise<void>
  logout: () => Promise<void>
}

export const AuthContext = createContext<AuthContextValue | null>(null)

/** どのコンポーネントからでもログイン状態を取り出せるようにする「フック」 */
export function useAuth(): AuthContextValue {
  const value = useContext(AuthContext)
  if (!value) {
    throw new Error('useAuth は AuthProvider の内側で使ってください')
  }
  return value
}
