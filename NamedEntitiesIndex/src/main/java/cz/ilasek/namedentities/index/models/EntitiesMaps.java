package cz.ilasek.namedentities.index.models;

import java.util.Map;

public class EntitiesMaps {
    private final Map<String, Integer> entities;
    private final Map<Integer, String> entityIds;
    
    public EntitiesMaps(Map<String, Integer> entities, Map<Integer, String> entityIds) {
        super();
        this.entities = entities;
        this.entityIds = entityIds;
    }

    public Map<String, Integer> getEntities() {
        return entities;
    }

    public Map<Integer, String> getEntityIds() {
        return entityIds;
    }
}
