-- V19: Backfill card.herocraft_id using ivion_uuid as the canonical herocraft identity
-- This aligns cards with rulings keyed by herocraft_id.

-- Ensure column exists (added in V15); this is idempotent backfill
UPDATE card
SET herocraft_id = ivion_uuid
WHERE herocraft_id IS NULL;
