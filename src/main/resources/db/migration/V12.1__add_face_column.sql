ALTER TABLE cardimages
    ADD COLUMN face varchar(5);

CREATE INDEX idx_images_card_id_variant_face ON cardimages (card_id, variant, face);