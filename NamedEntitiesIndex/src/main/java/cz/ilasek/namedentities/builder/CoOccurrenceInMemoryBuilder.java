package cz.ilasek.namedentities.builder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.Ostermiller.util.CSVPrinter;

import cz.ilasek.namedentities.index.dao.redis.EntityCoOccurrencesDao;
import cz.ilasek.namedentities.index.dao.sql.LanguageDaoManager.Language;
import cz.ilasek.namedentities.index.models.EntitiesMaps;
import cz.ilasek.namedentities.index.models.EntityCoOccurrenceKey;
import cz.ilasek.namedentities.index.models.EntityMentionInParagraph;
import cz.ilasek.namedentities.reader.CSVReader;
import cz.ilasek.namedentities.reader.ReaderException;

public class CoOccurrenceInMemoryBuilder {
    private static final String COOCCURRENCE_CSV_FILE = "entity_cooccurrence.csv";
    
    private static final int LOG_FREQUENCY = 50000;
    
    private static final Logger logger = LoggerFactory.getLogger(CoOccurrenceInMemoryBuilder.class);
    
    private final CSVReader inputReader;
    
    private final CSVPrinter cooccurrencePrinter;
    
    private final String lang;
    
    public CoOccurrenceInMemoryBuilder(String inputFolder, String outputFolder, Language language) throws IOException {
        inputReader = new CSVReader(inputFolder);
        cooccurrencePrinter = new CSVPrinter(new FileOutputStream(new File(outputFolder + "/" + COOCCURRENCE_CSV_FILE)));
        cooccurrencePrinter.writeln(new String[]{"entitiesKey", "coOccurrences"});        
        lang = language.getLang();
    }

    public void countCoOccurrences() {
        logger.info("Reading entities map");
        EntitiesMaps entitiesMaps = mapEntities();
        Map<String, Integer> entitiesMap = entitiesMaps.getEntities();
        EntityMentionInParagraph emip = null;
        String lastParagraphUri = null;
        Map<EntityCoOccurrenceKey, Integer> coOccurrences = new HashMap<EntityCoOccurrenceKey, Integer>();
        Set<Integer> entitiesInSameParagraph = new HashSet<Integer>();
        Set<EntityCoOccurrenceKey> alreadyIndexed = new HashSet<EntityCoOccurrenceKey>();
        
        int i = 0;
        try {
            while((emip = inputReader.readEntityMention()) != null) {
                int entityId = entitiesMap.get(emip.getEntityUri());

                if (emip.getParagraphUri().equals(lastParagraphUri)) {
                    if (entitiesInSameParagraph.size() <= 20) {
                        entitiesInSameParagraph.add(entityId);
                    }
                } else {
                    if (entitiesInSameParagraph.size() <= 20) {
                        storeCoOccurrences(coOccurrences, entitiesInSameParagraph, alreadyIndexed);
                    } else {
                        logger.info("Skipping paragraph " + emip.getParagraphUri() + " too many entities...");
                    }
                    
                    entitiesInSameParagraph.clear();
                    entitiesInSameParagraph.add(entityId);
                    lastParagraphUri = emip.getParagraphUri();
                }
                
                if ((++i % LOG_FREQUENCY) == 0) {
                    logger.info("Processed " + i + " mentions.");
                }
            }
            
            logger.info("Storing last paragraph of cooccurrences");
            storeCoOccurrences(coOccurrences, entitiesInSameParagraph, alreadyIndexed);
            logger.info("Entity cooccurrences indexed");
            
            persistCoOccurrences(coOccurrences, entitiesMaps.getEntityIds());
            logger.info("Entity cooccurrences persisted");
        } catch (ReaderException e) {
            logger.error("Problem reading entity mentions", e);
        }
    }

    private void persistCoOccurrences(Map<EntityCoOccurrenceKey, Integer> coOccurrences, Map<Integer, String> entitiesIds) {
        int i = 0;
        
        logger.info("Persisting " + coOccurrences.size() + " cooccurrences");
        
        for (Entry<EntityCoOccurrenceKey, Integer> coOccurrence : coOccurrences.entrySet()) {
            
            String key = EntityCoOccurrencesDao.buildKey(
                    lang, 
                    entitiesIds.get(coOccurrence.getKey().getEntityAId()), 
                    entitiesIds.get(coOccurrence.getKey().getEntityBId()));
            
            try {
                cooccurrencePrinter.writeln(new String[]{ key,  coOccurrence.getValue().toString()});
            } catch (IOException e) {
                logger.error("Problem writing cooccurrence " + key + " to output file", e);
            }
            
            if ((++i % LOG_FREQUENCY) == 0) {
                logger.info("Processed " + i + " cooccurrences.");
            }
        }        
    }
    
    private void storeCoOccurrences(Map<EntityCoOccurrenceKey, Integer> coOccurrences,
            Set<Integer> entitiesInSameParagraph, Set<EntityCoOccurrenceKey> alreadyIndexed) {
        if (entitiesInSameParagraph.size() > 1) {
            alreadyIndexed.clear();
            for (Integer entityAId : entitiesInSameParagraph){
                for (Integer entityBId : entitiesInSameParagraph) {
                    if (!entityAId.equals(entityBId)) {
                        EntityCoOccurrenceKey coOccurrenceKey = new EntityCoOccurrenceKey(entityAId, entityBId);
                        if (!alreadyIndexed.contains(coOccurrenceKey)) {
                            Integer coOccurrenceCount = coOccurrences.get(coOccurrenceKey);
                            if (coOccurrenceCount == null) {
                                coOccurrenceCount = 0;
                            }
                            
                            coOccurrences.put(coOccurrenceKey, coOccurrenceCount + 1);
                            alreadyIndexed.add(coOccurrenceKey);
                        }
                    }
                }
            }
        }
    }
    
    private EntitiesMaps mapEntities() {
        Map<String, Integer> entities = new HashMap<String, Integer>();
        Map<Integer, String> entityIds = new HashMap<Integer, String>();
        int entityId = 1;
        String entity;
        
        List<String> entitiesFromFile = new ArrayList<String>();
        
        try {
            logger.info("Reading entities from file");
            while ((entity = inputReader.readEntity()) != null) {
                entitiesFromFile.add(entity);
            }
            
            logger.info("Sorting entities from file");
            Collections.sort(entitiesFromFile);
            
            logger.info("Indexing entities from file");
            for (String entityUri : entitiesFromFile) {
                entityIds.put(entityId, entityUri);
                entities.put(entityUri, entityId++);
            }
            
            entitiesFromFile = null;
        } catch (ReaderException e) {
            logger.error("Problem reading entities", e);
        }
        
        return new EntitiesMaps(entities, entityIds);
    }
    
    public void close() {
        try {
            cooccurrencePrinter.close();
        } catch (IOException e) {
            logger.error("Problem closing cooccurrence printer", e);
        }
    }
    
}
