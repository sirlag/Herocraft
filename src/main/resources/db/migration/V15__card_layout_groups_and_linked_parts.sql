-- V15: Add layout/grouping columns to card and create linked parts relation table
-- Date: 2025-11-19 22:16 (local)

-- 1) Add new optional columns to card for layout and identity groupings
ALTER TABLE card
    ADD COLUMN IF NOT EXISTS layout              varchar(20),
    ADD COLUMN IF NOT EXISTS herocraft_id        uuid,
    ADD COLUMN IF NOT EXISTS print_variant_group_id uuid;

-- Helpful indexes for grouping lookups
CREATE INDEX IF NOT EXISTS idx_card_herocraft_id ON card (herocraft_id);
CREATE INDEX IF NOT EXISTS idx_card_print_variant_group_id ON card (print_variant_group_id);
CREATE INDEX IF NOT EXISTS idx_card_layout ON card (layout);

-- 2) Create a relation table for linked parts (e.g., tokens, transforms)
--    We use a VARCHAR with a CHECK constraint rather than a PostgreSQL ENUM for simpler migrations.
CREATE TABLE IF NOT EXISTS card_linked_parts (
    id               uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    card_id          uuid NOT NULL REFERENCES card(id) ON DELETE CASCADE,
    related_card_id  uuid NOT NULL REFERENCES card(id) ON DELETE CASCADE,
    relation         varchar(32) NOT NULL,
    created_at       timestamp NOT NULL DEFAULT now(),

    CONSTRAINT chk_card_linked_parts_relation CHECK (relation IN (
        'TOKEN', 'GENERATED_BY', 'TRANSFORMS_FROM', 'TRANSFORMS_INTO'
    )),
    CONSTRAINT unq_card_linked UNIQUE (card_id, related_card_id, relation)
);

-- Supporting indexes to speed up reverse lookups and joins
CREATE INDEX IF NOT EXISTS idx_card_linked_parts_card_id ON card_linked_parts (card_id);
CREATE INDEX IF NOT EXISTS idx_card_linked_parts_related_card_id ON card_linked_parts (related_card_id);
CREATE INDEX IF NOT EXISTS idx_card_linked_parts_relation ON card_linked_parts (relation);
