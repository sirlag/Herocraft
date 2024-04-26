CREATE EXTENSION pgcrypto;

DROP TABLE IF EXISTS users;
CREATE TABLE users (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    username TEXT NOT NULL UNIQUE,
    email TEXT NOT NULL UNIQUE,
    password TEXT NOT NULL
);

CREATE TABLE deck (
    id uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    hash TEXT,
    name TEXT,
    owner uuid references users(id),
    visibility INT
);