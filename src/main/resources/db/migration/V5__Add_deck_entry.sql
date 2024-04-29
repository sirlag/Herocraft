create table deckEntry (
    deck_id uuid REFERENCES deck(id) NOT NULL ,
    card_id uuid REFERENCES card(id) NOT NULL,
    count int NOT NULL
)