package cz.ilasek.namedentities.disambiguation;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import cz.ilasek.namedentities.models.DisambiguatedEntity;

public abstract class CandidatesGenerator {

    public List<DisambiguatedEntity> getCandidates(Set<String> entityNames) {
        List<DisambiguatedEntity> allCandidates = new LinkedList<DisambiguatedEntity>();
        int groupId = 1;
        
        for (String entityName : entityNames) {
            allCandidates.addAll(getCandidates(entityName, groupId));            
            groupId++;
        }
        
        return allCandidates;
    }

    public abstract List<DisambiguatedEntity> getCandidates(String entityName, int groupId);

}