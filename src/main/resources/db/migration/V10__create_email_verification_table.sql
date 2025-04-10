CREATE TABLE verification (
    id uuid NOT NULL PRIMARY KEY,
    token varchar(100) UNIQUE NOT NULL,
    user_id uuid NOT NULL,
    created_at timestamp NOT NULL,
    expires_at timestamp NOT NULL,
    CONSTRAINT fk_user
        FOREIGN KEY(user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

ALTER TABLE users
    ADD COLUMN created_at timestamp NOT NULL DEFAULT now(),
    ADD COLUMN last_modified timestamp NOT NULL DEFAULT now(),
    ADD COLUMN verified bool NOT NULL DEFAULT false,
    ADD COLUMN verified_at  timestamp NULL;
