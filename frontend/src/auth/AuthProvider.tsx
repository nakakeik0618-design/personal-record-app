import { useEffect, useState, type ReactNode } from 'react'
import * as authApi from '../api/auth'
import type { User } from '../api/auth'
import { AuthContext } from './authContext'

/**
 * ログイン状態を管理し、内側のすべてのコンポーネントに渡す。
 * 画面を開いた（再読み込みした）ときに API-04 でログイン中かどうかを確認する（FR-005）。
 */
export function AuthProvider({ children }: { children: ReactNode }) {
  // useState：コンポーネントが覚えておく値。値が変わると画面が描き直される
  const [user, setUser] = useState<User | null | undefined>(undefined)

  // useEffect：画面の表示後に実行したい処理。[] を渡すと最初の1回だけ実行される
  useEffect(() => {
    authApi
      .fetchMe()
      .then(setUser)
      .catch(() => setUser(null))
  }, [])

  async function login(email: string, password: string) {
    setUser(await authApi.login(email, password))
  }

  async function logout() {
    await authApi.logout()
    setUser(null)
  }

  return <AuthContext.Provider value={{ user, login, logout }}>{children}</AuthContext.Provider>
}
