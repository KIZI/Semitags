package cz.ilasek.namedentities.disambiguation;

import java.util.Collection;
import java.util.List;

import cz.ilasek.namedentities.models.DisambiguatedEntity;

public abstract class AbstractDisambiguation implements Disambiguation {
    
    public Collection<DisambiguatedEntity> listDisambiguatedEntities(List<DisambiguatedEntity> candidates, String conText) {
        return getBestCandidates(scoreEntities(candidates, conText)).values();
    }

}
