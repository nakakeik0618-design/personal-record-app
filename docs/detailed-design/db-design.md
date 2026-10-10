# MindLog DB設計書

## 0. 文書情報

| 項目 | 内容 |
|---|---|
| 文書名 | MindLog DB設計書（テーブル定義） |
| バージョン | v1.0 |
| 作成日 | 2026-10-10 |
| ステータス | 確定 |
| 関連文書 | [要件定義書](../requirements/requirements.md)、[ER図](../basic-design/er-diagram.md)、[API設計書](api-design.md) |

本書は、[ER図](../basic-design/er-diagram.md)で定めたテーブルを MySQL で作成できる形（データ型・制約・インデックス）まで具体化する。

- 本書で決定した方針は [7. DB設計で決定した事項](#7-db設計で決定した事項) にまとめる。

---

## 1. 共通方針

| 項目 | 内容 |
|---|---|
| DBMS | MySQL 8.0 |
| ストレージエンジン | InnoDB（外部キー・トランザクションに対応） |
| 文字コード | `utf8mb4`（日本語・絵文字を保存できる） |
| 照合順序 | `utf8mb4_bin`（文字を厳密に区別する。詳細は 1.1 節） |
| 命名規則 | テーブル名・カラム名ともに小文字のスネークケース（例：`target_date`）。テーブル名は複数形 |
| 主キー | `BIGINT` の自動採番（`AUTO_INCREMENT`）。モード別テーブルは `record_id` を主キー兼外部キーとする |
| 日時の型 | `DATETIME`。タイムゾーンは日本時間（Asia/Tokyo）で保存する |
| 作成日時・更新日時の設定 | アプリケーション（Spring Boot）側で設定する（詳細は 1.2 節） |

### 1.1 照合順序について

照合順序は、文字列の比較・検索で「どの文字を同じとみなすか」のルール。MySQL 8.0 の初期値 `utf8mb4_0900_ai_ci` は、濁点・半濁点の違いを無視する（「はな」で検索すると「ばな」「ぱな」も一致する）ため、タイトル検索（FR-024）で意図しない結果になる。

`utf8mb4_bin` を使用し、文字を厳密に区別する。

- 英字の大文字・小文字も区別されるため、タイトル検索で「abc」と「ABC」は別物として扱う。
- メールアドレスは、アプリケーション側で小文字に統一してから保存・照合する（`User@Example.com` と `user@example.com` を同じユーザーとして扱うため）。

### 1.2 更新日時をアプリケーション側で設定する理由

MySQL の `ON UPDATE CURRENT_TIMESTAMP` は、その行自体が更新されたときにしか日時を更新しない。記録の編集で本文（モード別テーブル）だけが変わった場合、`records.updated_at` が更新されないため、一覧の並び順（FR-018）が正しくならない。そのため、記録を保存するたびにアプリケーション側で `records.updated_at` を設定する。

---

## 2. テーブル一覧

| No. | テーブル名 | 論理名 | 概要 |
|---|---|---|---|
| 1 | users | ユーザー | ログインするユーザーの情報 |
| 2 | records | 記録 | 全モード共通の項目 |
| 3 | problem_solving_details | 問題解決の入力内容 | 問題解決モード固有の項目 |
| 4 | diary_details | 日記の入力内容 | 日記モード固有の項目 |
| 5 | daily_review_details | 1日の振り返りの入力内容 | 1日の振り返りモード固有の項目 |

---

## 3. テーブル定義

凡例：PK＝主キー、FK＝外部キー、UQ＝一意制約、NN＝NOT NULL（必須）

### 3.1 users（ユーザー）

| No. | カラム名 | 論理名 | データ型 | PK | FK | UQ | NN | 初期値 | 備考 |
|---|---|---|---|---|---|---|---|---|---|
| 1 | id | ユーザーID | BIGINT | ○ | | | ○ | 自動採番 | |
| 2 | email | メールアドレス | VARCHAR(255) | | | ○ | ○ | | 小文字に統一して保存 |
| 3 | password_hash | パスワード（ハッシュ値） | VARCHAR(255) | | | | ○ | | BCrypt でハッシュ化（NFR-001） |
| 4 | created_at | 登録日時 | DATETIME | | | | ○ | | |
| 5 | updated_at | 更新日時 | DATETIME | | | | ○ | | |

### 3.2 records（記録）

| No. | カラム名 | 論理名 | データ型 | PK | FK | UQ | NN | 初期値 | 備考 |
|---|---|---|---|---|---|---|---|---|---|
| 1 | id | 記録ID | BIGINT | ○ | | | ○ | 自動採番 | |
| 2 | user_id | ユーザーID | BIGINT | | ○ | | ○ | | users.id を参照 |
| 3 | mode | モード | VARCHAR(20) | | | | ○ | | `PROBLEM_SOLVING` / `DIARY` / `DAILY_REVIEW` のいずれか（CHECK制約） |
| 4 | title | タイトル | VARCHAR(30) | | | | | NULL | 最大30文字（要件定義書7節） |
| 5 | target_date | 対象日 | DATE | | | | ○ | | |
| 6 | created_at | 作成日時 | DATETIME | | | | ○ | | |
| 7 | updated_at | 更新日時 | DATETIME | | | | ○ | | 本文のみの編集時も更新する（1.2節） |

`VARCHAR(30)` の30はバイト数ではなく文字数のため、日本語でも30文字まで保存できる。

### 3.3 problem_solving_details（問題解決の入力内容）

| No. | カラム名 | 論理名 | データ型 | PK | FK | UQ | NN | 初期値 | 備考 |
|---|---|---|---|---|---|---|---|---|---|
| 1 | record_id | 記録ID | BIGINT | ○ | ○ | | ○ | | records.id を参照 |
| 2 | goal | 目標 | TEXT | | | | | NULL | |
| 3 | current_state | 現状 | TEXT | | | | | NULL | |
| 4 | gap | ギャップ | TEXT | | | | | NULL | |
| 5 | cause | 原因 | TEXT | | | | | NULL | |
| 6 | countermeasure | 対策 | TEXT | | | | | NULL | |

### 3.4 diary_details（日記の入力内容）

| No. | カラム名 | 論理名 | データ型 | PK | FK | UQ | NN | 初期値 | 備考 |
|---|---|---|---|---|---|---|---|---|---|
| 1 | record_id | 記録ID | BIGINT | ○ | ○ | | ○ | | records.id を参照 |
| 2 | memo | メモ | TEXT | | | | | NULL | |

### 3.5 daily_review_details（1日の振り返りの入力内容）

| No. | カラム名 | 論理名 | データ型 | PK | FK | UQ | NN | 初期値 | 備考 |
|---|---|---|---|---|---|---|---|---|---|
| 1 | record_id | 記録ID | BIGINT | ○ | ○ | | ○ | | records.id を参照 |
| 2 | events | 今日の出来事 | TEXT | | | | | NULL | |
| 3 | reflection | 反省点 | TEXT | | | | | NULL | |
| 4 | learning | 学び | TEXT | | | | | NULL | |
| 5 | insight | 気付き | TEXT | | | | | NULL | |
| 6 | tomorrow_tasks | 明日やること | TEXT | | | | | NULL | |

### 3.6 本文（TEXT型）の上限

`TEXT` 型は最大 65,535 バイトまで保存できる。`utf8mb4` は1文字最大4バイトのため、確実に保存できるのは約16,000文字。本文の各項目は**最大5,000文字**とし、超えた場合はアプリケーション側で入力エラーにする（DBの上限を超えてサーバーエラーになるのを防ぐため）。

---

## 4. 外部キー

| No. | テーブル | カラム | 参照先 | 削除時の動作 | 説明 |
|---|---|---|---|---|---|
| 1 | records | user_id | users.id | RESTRICT | 記録が残っているユーザーは削除できない（アカウント削除はスコープ外） |
| 2 | problem_solving_details | record_id | records.id | CASCADE | 記録を削除すると、対応する行も自動で削除される |
| 3 | diary_details | record_id | records.id | CASCADE | 同上 |
| 4 | daily_review_details | record_id | records.id | CASCADE | 同上 |

---

## 5. インデックス

主キー・一意制約・外部キーには MySQL が自動でインデックスを作成する。それ以外に以下を作成する。

| No. | テーブル | インデックス名 | 対象カラム | 目的 |
|---|---|---|---|---|
| 1 | records | idx_records_user_id_updated_at | user_id, updated_at | 自分の記録を更新日時の新しい順に取得する（ダッシュボード・一覧、FR-017, FR-018） |
| 2 | records | idx_records_user_id_target_date | user_id, target_date | 対象日の期間検索（FR-022） |

- タイトルの部分一致検索（`LIKE '%キーワード%'`）は、先頭が `%` のためインデックスが使われない。個人利用で記録件数が少ない想定のため、初期実装では対策しない。

---

## 6. DDL（テーブル作成SQL）

```sql
CREATE TABLE users (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    email         VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at    DATETIME     NOT NULL,
    updated_at    DATETIME     NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uq_users_email (email)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_bin;

CREATE TABLE records (
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    user_id     BIGINT      NOT NULL,
    mode        VARCHAR(20) NOT NULL,
    title       VARCHAR(30) NULL,
    target_date DATE        NOT NULL,
    created_at  DATETIME    NOT NULL,
    updated_at  DATETIME    NOT NULL,
    PRIMARY KEY (id),
    KEY idx_records_user_id_updated_at (user_id, updated_at),
    KEY idx_records_user_id_target_date (user_id, target_date),
    CONSTRAINT fk_records_user_id FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT chk_records_mode CHECK (mode IN ('PROBLEM_SOLVING', 'DIARY', 'DAILY_REVIEW'))
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_bin;

CREATE TABLE problem_solving_details (
    record_id      BIGINT NOT NULL,
    goal           TEXT   NULL,
    current_state  TEXT   NULL,
    gap            TEXT   NULL,
    cause          TEXT   NULL,
    countermeasure TEXT   NULL,
    PRIMARY KEY (record_id),
    CONSTRAINT fk_problem_solving_details_record_id FOREIGN KEY (record_id) REFERENCES records (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_bin;

CREATE TABLE diary_details (
    record_id BIGINT NOT NULL,
    memo      TEXT   NULL,
    PRIMARY KEY (record_id),
    CONSTRAINT fk_diary_details_record_id FOREIGN KEY (record_id) REFERENCES records (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_bin;

CREATE TABLE daily_review_details (
    record_id      BIGINT NOT NULL,
    events         TEXT   NULL,
    reflection     TEXT   NULL,
    learning       TEXT   NULL,
    insight        TEXT   NULL,
    tomorrow_tasks TEXT   NULL,
    PRIMARY KEY (record_id),
    CONSTRAINT fk_daily_review_details_record_id FOREIGN KEY (record_id) REFERENCES records (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_bin;
```

- 本書のDDLは設計内容の確認用とする。実際にテーブルを作成する方法（SQLファイルを手動実行する／マイグレーションツールを使う等）は環境構築で決める。

---

## 7. DB設計で決定した事項

| No. | 項目 | 決定内容 |
|---|---|---|
| 1 | 文字コード・照合順序 | `utf8mb4` ／ `utf8mb4_bin`（濁点の違いや大文字・小文字を区別する） |
| 2 | メールアドレスの大文字・小文字 | アプリケーション側で小文字に統一して保存・照合する |
| 3 | 本文（各入力項目）の上限 | 各項目最大5,000文字。超えた場合は入力エラー |
| 4 | 作成日時・更新日時の設定方法 | アプリケーション側で設定する |
| 5 | 日時のタイムゾーン | 日本時間（Asia/Tokyo） |
| 6 | パスワードのハッシュ化方式 | BCrypt（Spring Security の標準的な方式） |
| 7 | DDLの位置付け | 設計確認用とし、実際の作成方法は環境構築で決める |
