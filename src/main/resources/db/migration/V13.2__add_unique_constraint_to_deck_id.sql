alter table public.deck_facts_aggregate
    add constraint unq_deck_facts_aggregate_deck_id
        unique (deck_id);