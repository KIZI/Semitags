package cz.ilasek.namedentities.disambiguation;

import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import cz.ilasek.namedentities.index.dao.sql.EntityMentionsDao;
import cz.ilasek.namedentities.index.models.Entity;
import cz.ilasek.namedentities.models.DisambiguatedEntity;

public class DbCandidatesGenerator extends CandidatesGenerator {
    
    @Autowired
    private EntityMentionsDao entityMetionsDao;
    
    /* (non-Javadoc)
     * @see cz.ilasek.namedentities.disambiguation.CandidatesGenerator#getCandidates(java.lang.String, int)
     */
    @Override
    public List<DisambiguatedEntity> getCandidates(String entityName, int groupId) {
        List<DisambiguatedEntity> allCandidates = new LinkedList<DisambiguatedEntity>();
        List<Entity> candidates = entityMetionsDao.findCandidates(entityName);
        for (Entity candidate : candidates) {
            DisambiguatedEntity entity = new DisambiguatedEntity();
            entity.setEntityId(candidate.getId());
            entity.setUri(candidate.getUri());
            entity.setName(entityName);
            entity.setGroupId(groupId);
            allCandidates.add(entity);
        }
        
        return allCandidates;
    }
}
