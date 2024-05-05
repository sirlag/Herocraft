ALTER TABLE deck
    ADD COLUMN created timestamp default now(),
    ADD COLUMN last_modified timestamp default now();
