package cz.ilasek.namedentities.disambiguation;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import cz.ilasek.linkeddata.endpoints.DbPediaEndpoint;
import cz.ilasek.namedentities.index.dao.sql.EntityMentionsDao;
import cz.ilasek.namedentities.index.models.Entity;
import cz.ilasek.namedentities.models.DisambiguatedEntity;
import cz.ilasek.nlp.ner.StanfordEntity;
import cz.ilasek.nlp.ner.StanfordTextProcessingFacade;

@Deprecated
public class Disambiguator {
    private final SpotlightDisambiguation spotNer;
    private final EntityMentionsDao entityMetionsDao;
    private StanfordTextProcessingFacade stf;
    
    public Disambiguator(SpotlightDisambiguation spotNer, EntityMentionsDao entityMetionsDao) {
        this.spotNer = spotNer;
        this.entityMetionsDao = entityMetionsDao;
        stf = new StanfordTextProcessingFacade();
    }

    public Set<String> concatEntities(List<StanfordEntity> entities) {
        Set<String> entityNames = new HashSet<String>();
        StanfordEntity prevEntity = null;
        String prevName = "";
        for (StanfordEntity entity : entities) {
            if (entity.getName().equals("40"))
                System.out.println("TYPE ..." + entity.getType() + "...");
            if (!entity.getType().equals("NUMBER") && !entity.getType().equals("DATE") && !entity.getType().equals("DURATION") 
                    && !entity.getType().equals("SET") && !entity.getType().equals("PERCENT") && !entity.getType().equals("MONEY")
                    && !entity.getType().equals("ORDINAL")) {
                if ((prevEntity != null) && (entity.getStart() == (prevEntity.getStart() + prevEntity.getLength() + 1)))
                    prevName += " " + entity.getName();
                else {
                    if (prevName.length() > 0)
                        entityNames.add(prevName);
                    prevName = entity.getName();
                }
                prevEntity = entity;
            }
        }
        entityNames.add(prevName);
        
        return entityNames;
    }
    
    public List<DisambiguatedEntity> getCandidates(Set<String> entityNames) {
        List<DisambiguatedEntity> allCandidates = new LinkedList<DisambiguatedEntity>();
        int groupId = 1;
        
        for (String entityName : entityNames) {
            List<Entity> candidates = entityMetionsDao.findCandidates(entityName);
            for (Entity candidate : candidates) {
                DisambiguatedEntity entity = new DisambiguatedEntity();
                entity.setEntityId(candidate.getId());
                entity.setUri(candidate.getUri());
                entity.setName(entityName);
                entity.setGroupId(groupId);
                allCandidates.add(entity);
            }
            groupId++;
        }
        
        return allCandidates;
    }
    
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
    
    public List<DisambiguatedEntity> getSpotDisambCandidates(Set<String> entityNames, String text) {
        List<DisambiguatedEntity> allCandidates = new LinkedList<DisambiguatedEntity>();
        int groupId = 1;
        
        for (String entityName : entityNames) {
//            System.out.println("Candidates for " + entityName);
//            System.out.println("=============================");
            for (DisambiguatedEntity entity : spotNer.disambiguateEntity(entityName, text, 5)) {
                entity.setGroupId(groupId);
                allCandidates.add(entity);
                System.out.println(entity.toString());
                //
            }
            groupId++;
        }
        
        return allCandidates;
    }
    
    public List<DisambiguatedEntity> coOccurrenceDisambiguation(List<DisambiguatedEntity> entities) {
        for (DisambiguatedEntity e1 : entities) {
            for (DisambiguatedEntity e2 : entities) {
                if ((e1.getGroupId() != e2.getGroupId()) && (!e1.getUri().equals(e2.getUri()))) {
                    System.out.println("Find " + e1.getUri() + " <-> " + e2.getUri());
                    int count = entityMetionsDao.getCoOccurrenceCount(e1.getEntityId(), e2.getEntityId());
                    if (count > 0)
                        System.out.println("FOUND");
                    
                    e1.addGraphScore(count);
                    e2.addGraphScore(count);
                }
            }   
        }
        
        return entities;
    }
    
    public List<DisambiguatedEntity> normalizedCoOccurrenceDisambiguation(List<DisambiguatedEntity> entities) {
        for (DisambiguatedEntity e1 : entities) {
            for (DisambiguatedEntity e2 : entities) {
                if ((e1.getGroupId() != e2.getGroupId()) && (!e1.getUri().equals(e2.getUri()))) {
                    System.out.println("Find " + e1.getUri() + " <-> " + e2.getUri());
                    int count = entityMetionsDao.getCoOccurrenceCount(e1.getEntityId(), e2.getEntityId());
                    int e1Count = entityMetionsDao.getOccurrenceCount(e1.getEntityId());
                    int e2Count = entityMetionsDao.getOccurrenceCount(e2.getEntityId());
                    if (count > 0) {
                        System.out.println("FOUND");
                        System.out.println("Count: " + count);
                        System.out.println("Count e1: " + e1Count);
                        System.out.println("Count e2: " + e2Count);
                    }
                    
                    if (e1Count > e2Count) {
                        e1.addGraphScore((double) count / e1Count);
                        e2.addGraphScore((double) count / e1Count);
                        System.out.println("Total: " + ((double) count / e1Count));
                    } else { 
                        e1.addGraphScore((double) count / e2Count);
                        e2.addGraphScore((double) count / e2Count);
                        System.out.println("Total: " + ((double) count / e2Count));
                    }
                }
            }   
        }
        
        return entities;
    }
    
}
