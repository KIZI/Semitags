package cz.ilasek.namedentities.disambiguation;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import cz.ilasek.namedentities.models.DisambiguatedEntity;

public interface Disambiguation {
    /**
     * Assigns scores to candidates.
     * 
     * @param candidates
     * @param conText
     * @return List of scred entities.
     */
    public List<DisambiguatedEntity> scoreEntities(List<DisambiguatedEntity> candidates, String conText);
   
    /**
     * Returns best candidates from the list of scored entities.
     * 
     * @param allCandidates
     * @return Map - key is group Id and value is the particular entity
     */
    public Map<Integer, DisambiguatedEntity> getBestCandidates(List<DisambiguatedEntity> allCandidates);
    
    public Collection<DisambiguatedEntity> listDisambiguatedEntities(List<DisambiguatedEntity> candidates, String conText);
}
