-- V16: Create separate table for per-face textual information for cards
-- Date: 2025-11-20 00:50 (local)

CREATE TABLE IF NOT EXISTS card_faces (
    id           uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    card_id      uuid NOT NULL REFERENCES card(id) ON DELETE CASCADE,
    face         varchar(8) NOT NULL,
    name         text NULL,
    rules_text   text NULL,
    flavor_text  text NULL,
    artist       text NULL,
    action_cost  INTEGER NULL,
    power_cost   INTEGER NULL,
    heroic       BOOLEAN NULL,
    slow         BOOLEAN NULL,
    silence      BOOLEAN NULL,
    disarm       BOOLEAN NULL,
    created_at   timestamp NOT NULL DEFAULT now()
);

-- Ensure only one row per (card, face)
CREATE UNIQUE INDEX IF NOT EXISTS unq_card_faces_card_face ON card_faces(card_id, face);
CREATE INDEX IF NOT EXISTS idx_card_faces_card_id ON card_faces(card_id);
CREATE INDEX IF NOT EXISTS idx_card_faces_face ON card_faces(face);

