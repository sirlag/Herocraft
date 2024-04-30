CREATE TABLE cardmapping (
    alias text NOT NULL,
    id uuid NOT NULL,
    primary key (alias, id)
);

INSERT INTO cardmapping(alias, id )
SELECT id, ivion_uuid as alias from card;