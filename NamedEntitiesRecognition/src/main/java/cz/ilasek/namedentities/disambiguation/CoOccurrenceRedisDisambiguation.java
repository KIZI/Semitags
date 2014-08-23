package cz.ilasek.namedentities.disambiguation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.ilasek.namedentities.index.dao.redis.EntityCoOccurrencesDao;
import cz.ilasek.namedentities.models.DisambiguatedEntity;

public class CoOccurrenceRedisDisambiguation extends AbstractDisambiguation {
    
    private static final Logger logger = LoggerFactory.getLogger(CoOccurrenceRedisDisambiguation.class);
    
    private final EntityCoOccurrencesDao entityCoOccurrencesDao;
    
    public CoOccurrenceRedisDisambiguation(String lang, String redisHost, int redisDatabase) {
        entityCoOccurrencesDao = new EntityCoOccurrencesDao(lang, redisHost, redisDatabase);
    }
    
    @Override
    public List<DisambiguatedEntity> scoreEntities(List<DisambiguatedEntity> candidates, String conText) {
        for (DisambiguatedEntity e1 : candidates) {
            for (DisambiguatedEntity e2 : candidates) {
                if ((e1.getGroupId() != e2.getGroupId()) && (!e1.getUri().equals(e2.getUri())) 
                        && (e1.getUri().compareTo(e2.getUri()) < 0)) {
//                    System.out.println("Find " + e1.getUri() + " <-> " + e2.getUri());
                    int count = entityCoOccurrencesDao.getCoOccurrenceCount(e1.getUri(), e2.getUri());
                    if (count > 0) { 
                        logger.debug("Score for " + e1.getUri() + " .. " + e2.getUri() + " = " + count);
                    }
//                    if (count > 0)
//                        System.out.println("FOUND");
                    
                    e1.addGraphScore(count);
                    e2.addGraphScore(count);
                }
            }  
        }
        
        return candidates;
    }
    
    @Override
    public Map<Integer, DisambiguatedEntity> getBestCandidates(List<DisambiguatedEntity> allCandidates) {
        Map<Integer, DisambiguatedEntity> results = new HashMap<Integer, DisambiguatedEntity>();
        for (DisambiguatedEntity de : allCandidates) {
            double bestScore = 0;
            DisambiguatedEntity bestEntity = results.get(de.getGroupId());
            if (bestEntity != null)
                bestScore = bestEntity.getGraphScore();
            
            if (de.getGraphScore() > bestScore)
                results.put(de.getGroupId(), de);
        }
        
        return results;
    }    
}
