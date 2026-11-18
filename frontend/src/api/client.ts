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
  // 省略しない形：
  // fieldMessage(field: string): string | undefined {
  //   const found = this.body.errors.find((error) => error.field === field)
  //   if (found === undefined) {
  //     return undefined
  //   }
  //   return found.message
  // }
  fieldMessage(field: string): string | undefined {
    return this.body.errors.find((error) => error.field === field)?.message
  }
}

const CSRF_COOKIE = 'XSRF-TOKEN'
const CSRF_HEADER = 'X-XSRF-TOKEN'

/** Cookie から CSRF トークンを読み出す（API設計書 1.3節） */
// document.cookie は 'XSRF-TOKEN=abc123; JSESSIONID=xyz' のような1本の文字列
// 省略しない形：
// function readCsrfToken(): string | undefined {
//   const cookies = document.cookie.split('; ')  // ['XSRF-TOKEN=abc123', 'JSESSIONID=xyz']
//   const found = cookies.find((cookie) => cookie.startsWith(CSRF_COOKIE + '='))  // 'XSRF-TOKEN=abc123'
//   if (found === undefined) {
//     return undefined
//   }
//   return found.split('=')[1]  // 'abc123'
// }
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
// options の「= {}」：引数を渡さなかったときは空のオブジェクト {} を使う
export async function apiFetch<T>(path: string, options: { method?: string; body?: unknown } = {}): Promise<T> {
  // 省略しない形：
  // let method: string
  // if (options.method === undefined || options.method === null) {
  //   method = 'GET'
  // } else {
  //   method = options.method
  // }
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

  // 省略しない形：
  // let requestBody: string | undefined
  // if (options.body === undefined) {
  //   requestBody = undefined
  // } else {
  //   requestBody = JSON.stringify(options.body)
  // }
  // const response = await fetch(path, {
  //   method: method,
  //   headers: headers,
  //   body: requestBody,
  // })
  const response = await fetch(path, {
    method,
    headers,
    body: options.body === undefined ? undefined : JSON.stringify(options.body),
  })

  const text = await response.text()
  const data: unknown = text ? JSON.parse(text) : undefined

  // 省略しない形：
  // if (!response.ok) {
  //   let body: ErrorResponse
  //   if (data === undefined) {
  //     body = { message: 'エラーが発生しました。', errors: [] }
  //   } else {
  //     body = data as ErrorResponse
  //   }
  //   throw new ApiError(response.status, body)
  // }
  if (!response.ok) {
    const body = (data as ErrorResponse | undefined) ?? { message: 'エラーが発生しました。', errors: [] }
    throw new ApiError(response.status, body)
  }
  return data as T
}
