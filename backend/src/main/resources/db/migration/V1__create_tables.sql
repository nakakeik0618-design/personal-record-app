-- MindLog 初期テーブル作成（docs/detailed-design/db-design.md 6節のDDL）

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
