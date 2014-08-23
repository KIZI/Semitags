package cz.ilasek.namedentities.disambiguation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import cz.ilasek.namedentities.models.DisambiguatedEntity;

//TODO implement Disambiguation
public class RelatednessDisambiguation { //implements Disambiguation {
    
    public List<DisambiguatedEntity> scoreEntities(List<DisambiguatedEntity> candidates, String conText) {
        List<DisambiguatedEntity> nonAmbiguousCandidates = extractNonAmbiguousCandidates(candidates);
        
        // TODO compute relatedness to non ambiguous
        
        return candidates;
    }

    private List<DisambiguatedEntity> extractNonAmbiguousCandidates(List<DisambiguatedEntity> candidates) {
        Map<Integer, Integer> groupCounts = new HashMap<Integer, Integer>();
        for (DisambiguatedEntity candidate : candidates) {
            Integer groupId = candidate.getGroupId();
            
            if (groupCounts.containsKey(groupId))
                groupCounts.put(groupId, groupCounts.get(groupId) + 1);
            else
                groupCounts.put(groupId, 1);
        }
        
        List<DisambiguatedEntity> nonAmbiguousCandidates = new LinkedList<DisambiguatedEntity>();
        
        for (DisambiguatedEntity candidate : candidates) {
            if (groupCounts.get(candidate.getGroupId()) == 1)
                nonAmbiguousCandidates.add(candidate);
        }
        
        return nonAmbiguousCandidates;
    }
    
}
