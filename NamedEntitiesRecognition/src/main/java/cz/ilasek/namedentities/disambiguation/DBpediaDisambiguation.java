package cz.ilasek.namedentities.disambiguation;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cz.ilasek.linkeddata.endpoints.DbPediaEndpoint;
import cz.ilasek.namedentities.models.DisambiguatedEntity;

// TODO implement Disambiguation
public class DBpediaDisambiguation { //implements Disambiguation {
    private DbPediaEndpoint dbpe = new DbPediaEndpoint();
    
    public List<DisambiguatedEntity> scoreEntities(List<DisambiguatedEntity> candidates, String conText) {
        for (DisambiguatedEntity e1 : candidates) {
            for (DisambiguatedEntity e2 : candidates) {
                if (e1.getGroupId() != e2.getGroupId()) {
                    System.out.println("Find " + e1.getDBpediaUri() + " <-> " + e2.getDBpediaUri());
                    try {
                        int count = dbpe.getCount("select distinct ?p where {<" 
                                + e1.getDBpediaUri() + "> ?p <" + e2.getDBpediaUri() + ">}");
                        if (count > 0)
                            System.out.println("FOUND");
                        e1.addGraphScore(count);
                        e2.addGraphScore(count);
                    } catch (Exception e) {
                        System.err.println("Catched " + e.getMessage());
                    }
                }
            }   
        }
        
        return candidates;        
    }
}
