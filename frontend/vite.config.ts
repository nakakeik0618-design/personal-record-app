import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    // 開発時は /api へのリクエストを Spring Boot（ポート 8080）へ転送する（API設計書 1.4節）
    proxy: {
      '/api': 'http://localhost:8080',
    },
  },
})
