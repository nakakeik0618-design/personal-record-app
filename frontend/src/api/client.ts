// バックエンドの API を呼び出すための共通処理（API設計書 1〜2節）

/** エラーレスポンスの形（API設計書 2.2節） */
export type ErrorResponse = {
  message: string
  errors: { field: string; message: string }[]
}

/** API がエラー（4xx / 5xx）を返したときに投げる例外 */
export class ApiError extends Error {
  readonly status: number
  readonly body: ErrorResponse

  constructor(status: number, body: ErrorResponse) {
    super(body.message)
    this.status = status
    this.body = body
  }

  /** 指定した項目のエラーメッセージ（なければ undefined） */
  fieldMessage(field: string): string | undefined {
    return this.body.errors.find((error) => error.field === field)?.message
  }
}

const CSRF_COOKIE = 'XSRF-TOKEN'
const CSRF_HEADER = 'X-XSRF-TOKEN'

/** Cookie から CSRF トークンを読み出す（API設計書 1.3節） */
function readCsrfToken(): string | undefined {
  return document.cookie
    .split('; ')
    .find((cookie) => cookie.startsWith(`${CSRF_COOKIE}=`))
    ?.split('=')[1]
}

/**
 * CSRF トークンをまだ持っていなければ、GET リクエストを1回送って Cookie で受け取る。
 * （サーバーはどのリクエストにもトークンの Cookie を付けて返す）
 */
async function ensureCsrfToken(): Promise<string | undefined> {
  if (!readCsrfToken()) {
    await fetch('/api/auth/me')
  }
  return readCsrfToken()
}

/**
 * API を呼び出し、レスポンスの JSON を返す。
 * - `<T>` は「返ってくるデータの型」を呼び出す側で指定するための書き方（ジェネリクス）
 * - 本文がないレスポンス（201 / 204）の場合は undefined を返す
 */
export async function apiFetch<T>(path: string, options: { method?: string; body?: unknown } = {}): Promise<T> {
  const method = options.method ?? 'GET'
  const headers: Record<string, string> = {}

  if (options.body !== undefined) {
    headers['Content-Type'] = 'application/json'
  }
  // 更新系のリクエストには CSRF トークンを付ける
  if (method !== 'GET') {
    const token = await ensureCsrfToken()
    if (token) {
      headers[CSRF_HEADER] = token
    }
  }

  const response = await fetch(path, {
    method,
    headers,
    body: options.body === undefined ? undefined : JSON.stringify(options.body),
  })

  const text = await response.text()
  const data: unknown = text ? JSON.parse(text) : undefined

  if (!response.ok) {
    const body = (data as ErrorResponse | undefined) ?? { message: 'エラーが発生しました。', errors: [] }
    throw new ApiError(response.status, body)
  }
  return data as T
}
