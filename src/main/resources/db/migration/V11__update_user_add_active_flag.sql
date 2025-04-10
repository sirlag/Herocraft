ALTER TABLE users
    ADD COLUMN active BOOLEAN not null default true;

CREATE TABLE resettokens
(
    id         uuid                NOT NULL PRIMARY KEY,
    token      varchar(100) UNIQUE NOT NULL,
    user_id    uuid                NOT NULL,
    created_at timestamp           NOT NULL,
    expires_at timestamp           NOT NULL,
    CONSTRAINT fk_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE
);