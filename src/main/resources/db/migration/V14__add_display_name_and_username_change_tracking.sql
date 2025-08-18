-- Add display_name and last_username_changed_at
ALTER TABLE users
  ADD COLUMN display_name VARCHAR(32),
  ADD COLUMN last_username_changed_at TIMESTAMP;

-- Backfill display_name from username
UPDATE users
SET display_name = username
WHERE display_name IS NULL;

-- Make display_name NOT NULL
ALTER TABLE users
  ALTER COLUMN display_name SET NOT NULL;

-- Case-insensitive unique index for username lookups
CREATE UNIQUE INDEX IF NOT EXISTS users_username_lower_key ON users ((lower(username)));

-- Optional: index for last_username_changed_at (commented out)
-- CREATE INDEX IF NOT EXISTS idx_users_last_username_changed_at ON users (last_username_changed_at);
