import { useEffect, useState, type ReactNode } from 'react'
import * as authApi from '../api/auth'
import type { User } from '../api/auth'
import { AuthContext } from './authContext'

/**
 * ログイン状態を管理し、内側のすべてのコンポーネントに渡す。
 * 画面を開いた（再読み込みした）ときに API-04 でログイン中かどうかを確認する（FR-005）。
 */
// 省略しない形：
// export function AuthProvider(props: { children: ReactNode }) {
//   const children = props.children
export function AuthProvider({ children }: { children: ReactNode }) {
  // useState：コンポーネントが覚えておく値。値が変わると画面が描き直される
  // 省略しない形：
  // const state = useState<User | null | undefined>(undefined)
  // const user = state[0]     // 今の値
  // const setUser = state[1]  // 値を書き換える関数
  const [user, setUser] = useState<User | null | undefined>(undefined)

  // useEffect：画面の表示後に実行したい処理。[] を渡すと最初の1回だけ実行される
  // 省略しない形：
  // useEffect(() => {
  //   authApi
  //     .fetchMe()
  //     .then((me) => setUser(me))
  //     .catch(() => setUser(null))
  // }, [])
  useEffect(() => {
    authApi
      .fetchMe()
      .then(setUser)
      .catch(() => setUser(null))
  }, [])

  // 省略しない形（戻り値の型を書く）：
  // async function login(email: string, password: string): Promise<void> {
  //   const loggedInUser = await authApi.login(email, password)
  //   setUser(loggedInUser)
  // }
  async function login(email: string, password: string) {
    setUser(await authApi.login(email, password))
  }

  async function logout() {
    await authApi.logout()
    setUser(null)
  }

  // 省略しない形：
  // return (
  //   <AuthContext.Provider value={{ user: user, login: login, logout: logout }}>
  //     {children}
  //   </AuthContext.Provider>
  // )
  return <AuthContext.Provider value={{ user, login, logout }}>{children}</AuthContext.Provider>
}
