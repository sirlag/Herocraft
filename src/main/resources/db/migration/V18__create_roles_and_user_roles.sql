-- V18: Initial role system
-- Adds role and user_role tables to support permissions via roles.
-- Current permissions represented as boolean flags on role: admin, moderation

CREATE TABLE IF NOT EXISTS role (
    id           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    name         text NOT NULL UNIQUE,
    admin        boolean NOT NULL DEFAULT false,
    moderation   boolean NOT NULL DEFAULT false,
    created_at   timestamp NOT NULL DEFAULT now(),
    updated_at   timestamp NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS user_role (
    user_id  uuid NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id  uuid NOT NULL REFERENCES role(id) ON DELETE CASCADE,
    assigned_at timestamp NOT NULL DEFAULT now(),
    PRIMARY KEY (user_id, role_id)
);

CREATE INDEX IF NOT EXISTS idx_user_role_user ON user_role(user_id);
CREATE INDEX IF NOT EXISTS idx_user_role_role ON user_role(role_id);
