import { BrowserRouter, Navigate, Route, Routes } from 'react-router'
import { AuthProvider } from './auth/AuthProvider'
import { Layout } from './components/Layout'
import { RequireAuth } from './components/RequireAuth'
import { DashboardPage } from './pages/DashboardPage'
import { LoginPage } from './pages/LoginPage'
import { ModeSelectPage } from './pages/ModeSelectPage'
import { RecordCreatePage } from './pages/RecordCreatePage'
import { RecordDetailPage } from './pages/RecordDetailPage'
import { RegisterPage } from './pages/RegisterPage'

/**
 * 画面のURLと表示するコンポーネントの対応（API設計書 6節）。
 * 記録一覧（/records）と記録編集（/records/:id/edit）は、それぞれの実装時に追加する。
 */
export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />

          {/* ログインが必要な画面：RequireAuth → Layout（共通ヘッダー）→ 各画面 の順に入れ子になる */}
          <Route element={<RequireAuth />}>
            <Route element={<Layout />}>
              <Route path="/" element={<DashboardPage />} />
              <Route path="/records/new" element={<ModeSelectPage />} />
              <Route path="/records/new/:modeSlug" element={<RecordCreatePage />} />
              <Route path="/records/:id" element={<RecordDetailPage />} />
            </Route>
          </Route>

          {/* 存在しないURLはダッシュボードへ */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  )
}
