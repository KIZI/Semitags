package cz.ilasek.namedentities.set;

import java.util.HashSet;
import java.util.Set;

public class EntitySet {
    
    private static EntitySet instance = new EntitySet();
    
    private Set<String> entities = new HashSet<String>();
    
    private EntitySet() {
        
    }
    
    public static EntitySet getInstance() {
        return instance;
    }
    
    public Set<String> getEntities() {
        return entities;
    }

    public synchronized boolean addEntity(String entitiyUri) {
        return entities.add(entitiyUri);
    }
    

}
