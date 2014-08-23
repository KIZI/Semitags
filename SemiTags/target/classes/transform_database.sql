-- Scripts for computation of aggregated information after indexing

-- Filter out unimportant mentions
INSERT INTO ner.nl_entities_mentions_filter 
    SELECT entity_id, surface_form_id, COUNT(*) AS occurrences 
        FROM ner.nl_entities_mentions 
        GROUP BY entity_id, surface_form_id 
        HAVING occurrences > 5;