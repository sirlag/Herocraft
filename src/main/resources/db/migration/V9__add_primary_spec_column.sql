alter table deck add column primary_spec text;

UPDATE
    deck
SET primary_spec = (
    select c.archetype
    from deckentry
             inner join
         card c on c.id = deckentry.card_id
    WHERE c.type = 'Ultimate' AND deckentry.deck_id = deck.id
    LIMIT 1
)
WHERE true;

