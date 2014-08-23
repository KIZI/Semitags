package cz.ilasek.namedentities.disambiguation;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import cz.ilasek.namedentities.index.dao.redis.SurfaceFormsDao;
import cz.ilasek.namedentities.models.DisambiguatedEntity;

public class RedisCandidatesGenerator extends CandidatesGenerator {
    
    private final SurfaceFormsDao surfaceFormsDao;
    
    public RedisCandidatesGenerator(String redisHost, int redisDatabase) {
        surfaceFormsDao = new SurfaceFormsDao(redisHost, redisDatabase);
    }

    @Override
    public List<DisambiguatedEntity> getCandidates(String entityName, int groupId) {
        List<DisambiguatedEntity> allCandidates = new LinkedList<DisambiguatedEntity>();
        Set<String> candidates = surfaceFormsDao.getEntities(entityName);
        
        for (String candidate : candidates) {
            DisambiguatedEntity entity = new DisambiguatedEntity();
            entity.setName(entityName);
            entity.setUri(candidate);
            entity.setGroupId(groupId);
            allCandidates.add(entity);
        }
        
        return allCandidates;
    }

}
