-- ============================================================================
-- code-review-guru 初始化 schema（MySQL 方言；H2 以 MODE=MySQL 运行同一份）
-- ----------------------------------------------------------------------------
-- 通过 Spring Boot spring.sql.init 在启动时执行（application.yml 中配置）。
-- 幂等：全部用 CREATE TABLE IF NOT EXISTS；索引内联在表定义里（MySQL 不支持
--      CREATE INDEX IF NOT EXISTS，故不写单独的 CREATE INDEX）。
-- 字符集：表级 DEFAULT CHARSET=utf8mb4（H2 MODE=MySQL 忽略这两个表选项但语法可过）。
-- 生产部署：设 REVIEW_SQL_INIT_MODE=NEVER，由 DBA 手动执行本文件一次。
-- ============================================================================

CREATE TABLE IF NOT EXISTS review_request (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    title         VARCHAR(255) NOT NULL DEFAULT '',
    language      VARCHAR(64)  NOT NULL,
    source_code   TEXT         NOT NULL,
    source_size   INT          NOT NULL DEFAULT 0,
    reviewer_type VARCHAR(16)  NOT NULL DEFAULT 'RULE',
    model         VARCHAR(128) NULL,
    summary       VARCHAR(2000) NULL,
    score         INT          NULL,
    status        VARCHAR(16)  NOT NULL DEFAULT 'DONE',
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS review_finding (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    request_id BIGINT       NOT NULL,
    severity   VARCHAR(16)  NOT NULL,
    category   VARCHAR(64)  NULL,
    line       INT          NULL,
    title      VARCHAR(512) NOT NULL,
    detail     TEXT         NULL,
    suggestion TEXT         NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_finding_request (request_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
