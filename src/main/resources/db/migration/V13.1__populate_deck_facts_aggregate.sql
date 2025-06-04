INSERT INTO deck_facts_aggregate (id, deck_id, views, likes)
SELECT gen_random_uuid() AS id,      -- Generate a new UUID for the aggregate row
       d.id              AS deck_id, -- Get the deck ID from the deck table
       0                 AS views,   -- Set views to 0
       0                 AS likes    -- Set likes to 0
FROM public.deck d; -- Select from the deck table
