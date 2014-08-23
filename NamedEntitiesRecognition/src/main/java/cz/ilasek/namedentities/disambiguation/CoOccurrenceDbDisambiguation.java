package cz.ilasek.namedentities.disambiguation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import cz.ilasek.namedentities.index.dao.sql.EntityMentionsDao;
import cz.ilasek.namedentities.models.DisambiguatedEntity;

public class CoOccurrenceDbDisambiguation extends AbstractDisambiguation {
    @Autowired
    private EntityMentionsDao entityMentionsDao;
    
    @Override
    public List<DisambiguatedEntity> scoreEntities(List<DisambiguatedEntity> candidates, String conText) {
        for (DisambiguatedEntity e1 : candidates) {
            for (DisambiguatedEntity e2 : candidates) {
                if ((e1.getGroupId() != e2.getGroupId()) && (!e1.getUri().equals(e2.getUri()))) {
//                    System.out.println("Find " + e1.getUri() + " <-> " + e2.getUri());
                    int count = entityMentionsDao.getCoOccurrenceCount(e1.getEntityId(), e2.getEntityId());
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
