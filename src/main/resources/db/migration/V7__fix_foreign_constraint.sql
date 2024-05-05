ALTER TABLE deckentry
    DROP CONSTRAINT "deckentry_deck_id_fkey";

ALTER TABLE deckentry
    ADD CONSTRAINT "deckentry_deck_id"
    FOREIGN KEY (deck_id)
    REFERENCES deck(id)
    ON DELETE CASCADE;