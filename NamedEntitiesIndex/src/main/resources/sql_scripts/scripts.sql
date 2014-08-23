-- Concat mentioned paragraphs
INSERT INTO entities_mentions_concat (entity_id, mentions)
SELECT entity_id, GROUP_CONCAT(paragraph_text  SEPARATOR ' ') 
    FROM `entities_mentions` JOIN paragraphs USING (paragraph_id) 
    GROUP BY entity_id;
    
-- --------------------------------------------------------------------------------
-- SELECT vyse vytvari prilis velkou temporary tabulku, proto zkousim proceduru.
-- --------------------------------------------------------------------------------
DELIMITER $$

CREATE DEFINER=`ner`@`%` PROCEDURE `concat_mentions`()
BEGIN
    DECLARE done INT DEFAULT 0;
    DECLARE v_entity_id int(10) unsigned;
    DECLARE v_mentions text;
    DECLARE concatCursor CURSOR FOR SELECT DISTINCT entity_id FROM `entities_mentions`;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1; 

    OPEN concatCursor;
    
    REPEAT
        FETCH concatCursor INTO v_entity_id;
        IF NOT done THEN
            INSERT INTO entities_mentions_concat SELECT entity_id, GROUP_CONCAT(paragraph_text  SEPARATOR ' ') 
                FROM `entities_mentions` JOIN paragraphs USING (paragraph_id) WHERE entity_id = v_entity_id;
        END IF;
    UNTIL done END REPEAT;
END

-- CALL
USE nerde;
SET group_concat_max_len=15000000;
CALL concat_mentions();