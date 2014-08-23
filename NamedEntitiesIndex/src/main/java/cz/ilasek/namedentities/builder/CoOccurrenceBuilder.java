package cz.ilasek.namedentities.builder;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.ilasek.namedentities.index.dao.redis.EntityCoOccurrencesDao;
import cz.ilasek.namedentities.index.models.EntityMentionInParagraph;
import cz.ilasek.namedentities.reader.CSVReader;
import cz.ilasek.namedentities.reader.ReaderException;

public class CoOccurrenceBuilder {
    private static final int LOG_FREQUENCY = 50000;
    
    private static final Logger logger = LoggerFactory.getLogger(CoOccurrenceBuilder.class);
    
    private final EntityCoOccurrencesDao entityCoOccurrencesDao;
    
    private final CSVReader inputReader;
    
    private final String lang;
    
    
    public CoOccurrenceBuilder(String inputFolder, EntityCoOccurrencesDao entityCoOccurrencesDao, String lang) throws IOException {
        inputReader = new CSVReader(inputFolder);
        this.entityCoOccurrencesDao = entityCoOccurrencesDao;
        this.lang = lang;
    }

    public void countCoOccurrences() {
        EntityMentionInParagraph emip = null;
        String lastParagraphUri = null;
        Set<String> entitiesInSameParagraph = new HashSet<String>();
        Set<String> alreadyIndexed = new HashSet<String>();
        
        int i = 0;
        try {
            while((emip = inputReader.readEntityMention()) != null) {
                if (emip.getParagraphUri().equals(lastParagraphUri)) {
                    entitiesInSameParagraph.add(emip.getEntityUri());
                } else {
                    if (entitiesInSameParagraph.size() > 1) {
                        alreadyIndexed.clear();
                        for (String entityAUri : entitiesInSameParagraph){
                            for (String entityBUri : entitiesInSameParagraph) {
                                if (!entityAUri.equals(entityBUri)) {
                                    String key = EntityCoOccurrencesDao.buildKey(lang, entityAUri, entityBUri);
                                    if (!alreadyIndexed.contains(key)) {
                                        entityCoOccurrencesDao.incrementCoOccurrenceCount(entityAUri, entityBUri);
                                        alreadyIndexed.add(key);
                                    }
                                }
                            }
                        }
                    }
                    
                    entitiesInSameParagraph.clear();
                    entitiesInSameParagraph.add(emip.getEntityUri());
                    lastParagraphUri = emip.getParagraphUri();
                }
                
                if ((++i % LOG_FREQUENCY) == 0) {
                    logger.info("Processed " + i + " mentions.");
                }
            }
            
            if (entitiesInSameParagraph.size() > 1) {
                alreadyIndexed.clear();
                for (String entityAUri : entitiesInSameParagraph){
                    for (String entityBUri : entitiesInSameParagraph) {
                        if (!entityAUri.equals(entityBUri)) {
                            String key = EntityCoOccurrencesDao.buildKey(lang, entityAUri, entityBUri);
                            if (!alreadyIndexed.contains(key)) {
                                entityCoOccurrencesDao.incrementCoOccurrenceCount(entityAUri, entityBUri);
                                alreadyIndexed.add(key);
                            }
                        }
                    }
                }
            }            
        } catch (ReaderException e) {
            logger.error("Problem reding entity mentions", e);
        }
    }
    
}
