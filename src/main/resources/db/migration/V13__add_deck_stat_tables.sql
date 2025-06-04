-- Likes are only for decks that user does not manage
CREATE TABLE deck_likes
(
    id         uuid PRIMARY KEY,
    deck_id    uuid      NOT NULL,
    user_id    uuid      NOT NULL,
    timestamp  timestamp NOT NULL,
    deleted    boolean,
    deleted_at timestamp,

    constraint fk_deck_likes_deck
        FOREIGN KEY (deck_id)
            REFERENCES deck (id)
            ON DELETE CASCADE,

    constraint fk_deck_likes_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    constraint unq_deck_likes_deck_user
        UNIQUE (deck_id, user_id)
);

-- Favorites are only for decks the user does manage.
CREATE TABLE deck_favorites
(
    id         uuid PRIMARY KEY,
    deck_id    uuid      NOT NULL,
    user_id    uuid      NOT NULL,
    timestamp  timestamp NOT NULL,
    deleted    boolean,
    deleted_at timestamp,

    constraint fk_deck_favorites_deck
        FOREIGN KEY (deck_id)
            REFERENCES deck (id)
            ON DELETE CASCADE,

    constraint fk_deck_favorites_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    constraint unq_deck_favorites_deck_user
        UNIQUE (deck_id, user_id)
);

CREATE TABLE deck_views
(
    id         uuid PRIMARY KEY,
    deck_id    uuid        NOT NULL,
    -- An IPV6 Address can have at most 45 Characters
    ip         varchar(45) NOT NULL,
    timestamp  timestamp,
    user_agent text,

    constraint fk_deck_views_deck
        FOREIGN KEY (deck_id)
            REFERENCES deck (id)
            ON DELETE CASCADE
);


CREATE TABLE deck_facts_aggregate
(
    id      uuid PRIMARY KEY,
    deck_id uuid NOT NULL,
    views   int  NOT NULL,
    likes   int  NOT NULL,

    constraint fk_deck_facts_aggregate_deck
        FOREIGN KEY (deck_id)
            REFERENCES deck (id)
            ON DELETE CASCADE
)



