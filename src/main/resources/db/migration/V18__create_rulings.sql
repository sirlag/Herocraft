-- V18: Create rulings table for card rulings and Q&A

CREATE TABLE IF NOT EXISTS rulings (
    id UUID PRIMARY KEY,
    card_herocraft_id UUID NOT NULL,
    source TEXT NOT NULL,              -- constrained at service layer: discord | luminary | herocraft
    source_url TEXT NULL,
    published_at TIMESTAMPTZ NOT NULL,
    style TEXT NOT NULL,               -- constrained at service layer: QA | RULING
    comment TEXT NULL,
    question TEXT NULL,
    answer TEXT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_rulings_card_herocraft_id ON rulings(card_herocraft_id);
CREATE INDEX IF NOT EXISTS idx_rulings_published_at ON rulings(published_at);
