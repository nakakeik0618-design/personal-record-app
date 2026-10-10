# MindLog

## プロジェクト概要

個人の思考・記録（出来事、反省点、学び、気付き、悩みなど）を整理・言語化するためのWebアプリケーション。「問題解決」「日記」「1日の振り返り」の3つのモードから選んで記録する。

**このプロジェクトの目的はアプリの完成そのものではなく、実務に近い開発工程を経験すること。** 要件定義 → 基本設計 → 詳細設計 → DB設計 → 環境構築 → 実装 → テスト → GitHub管理 → レビュー → 改善、という流れを一つずつ踏んで進める。

## ドキュメント

- 要件定義書: `docs/requirements/requirements.md`（要件ID: FR-xxx / NFR-xxxで管理）
- 基本設計書: `docs/basic-design/`（画面遷移図 `screen-transition.md`（画面ID: SC-xx）、ER図 `er-diagram.md`、画面レイアウト `screen-layout.md`）
- 詳細設計書: `docs/detailed-design/`（API設計 `api-design.md`（API ID: API-xx）、DB設計 `db-design.md`）
- プロジェクト概要: `docs/README.md`

## 技術スタック（確定）

| 領域 | 技術 |
|---|---|
| バックエンド | Java, Spring Boot, Spring Security, Spring Data JPA |
| DB | MySQL 8.4（Docker で起動、ポート 3307） |
| フロントエンド | HTML, CSS, JavaScript, React |
| ビルドツール | Maven |
| バージョン管理 | GitHub |

画面遷移・ER図・画面レイアウトは基本設計書、API設計（認証はセッション方式＋CSRF対策）、DBの物理設計（型・制約・インデックス、utf8mb4_bin）は詳細設計書で確定済み。

## 現在のフェーズ

要件定義：完了。基本設計：完了（画面遷移図・ER図・画面レイアウト）。詳細設計：完了（API設計）。DB設計：完了。次は環境構築。

## Gitワークフロー

このプロジェクトではGitHubの実務的な使い方を学ぶことも目的の一つのため、以下のルールを徹底する。

### ブランチ戦略

- `main`ブランチには直接コミットしない。
- 作業ごとに`main`からブランチを切り、GitHub上でPull Requestを作成してから`main`にマージする。
- ブランチ名は用途がわかる形にする（例：`feature/login-screen`, `docs/basic-design`, `fix/xxx`）。

### コミットメッセージ（Conventional Commits）

`<type>: <日本語での変更内容>` の形式で書く。

| type | 用途 |
|---|---|
| `feat` | 新機能の追加 |
| `fix` | バグ修正 |
| `docs` | ドキュメントのみの変更（設計書、README等） |
| `refactor` | 挙動を変えないコードの整理 |
| `test` | テストの追加・修正 |
| `chore` | ビルド設定など、上記に当てはまらない雑務的変更 |

例：`docs: 要件定義書を作成`、`feat: ログイン機能を実装`

### 基本的な作業の流れ

1. `git checkout -b feature/xxx`（mainから新しいブランチを作成）
2. 変更作業を行う
3. `git status` で変更内容を確認
4. `git add <ファイル名>`（`-A`や`.`ではなく、変更したファイルを個別に指定する）
5. `git commit -m "type: 変更内容"`
6. `git push -u origin feature/xxx`
7. GitHub上でPull Requestを作成
8. 内容を確認し、`main`にマージ
9. 不要になったブランチを削除

## Claude Codeとの協働方針

- DB選定・フレームワーク選定・画面設計など、要件定義や設計上の判断はユーザー自身が行う。Claude Codeは提案・選択肢の提示・設計レビューの相手として振る舞い、明示的な指示なく仕様を勝手に確定させない。
- React実装フェーズでは、書かれたコードの構文・意図が学べるよう説明を添える（ユーザーはReact未学習）。
