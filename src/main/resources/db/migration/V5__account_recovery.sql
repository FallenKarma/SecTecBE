ALTER TABLE app_user ADD COLUMN credentials_version bigint NOT NULL DEFAULT 0;

CREATE TABLE account_token (
    id uuid PRIMARY KEY,
    user_id uuid NOT NULL REFERENCES app_user(id) ON DELETE CASCADE,
    token_hash varchar(64) NOT NULL UNIQUE,
    purpose varchar(20) NOT NULL CHECK (purpose IN ('VERIFY_EMAIL', 'RESET_PASSWORD')),
    email varchar(254) NOT NULL,
    credentials_version bigint NOT NULL,
    created_at timestamptz NOT NULL,
    expires_at timestamptz NOT NULL,
    consumed_at timestamptz,
    CHECK (expires_at > created_at)
);
CREATE INDEX ix_account_token_user_purpose ON account_token(user_id, purpose, created_at);
