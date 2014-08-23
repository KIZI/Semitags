package cz.ilasek.namedentities.models;

import java.util.LinkedList;
import java.util.List;

public class EntityInText {
    private String entityName;
    private List<SVOTriple> svoTriples = new LinkedList<SVOTriple>();

    public EntityInText(String entityName) {
        this.entityName = entityName;
    }
    
    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public List<SVOTriple> getSvoTriples() {
        return svoTriples;
    }

    public void addSvoTriple(SVOTriple svoTriple) {
        this.svoTriples.add(svoTriple);
    }

}
