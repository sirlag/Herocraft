CREATE TABLE cardimages
(
    image_id   uuid PRIMARY KEY,
    card_id    uuid      NOT NULL,
    variant    text      NOT NULL,
    uri        text      NOT NULL,
    mime_type  varchar(100),
    byte_size  int       NOT NULL,
    created_at timestamp NOT NULL,
    updated_at timestamp NOT NULL,

    constraint fk_card
        FOREIGN KEY (card_id)
            REFERENCES card (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_images_card_id ON cardimages (card_id);