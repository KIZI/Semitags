package cz.ilasek.namedentities.index.models;

public class EntityCoOccurrenceKey {
    private final int entityAId;
    
    private final int entityBId;

    public EntityCoOccurrenceKey(int entityAId, int entityBId) {
        super();
        if (entityAId < entityBId) {
            this.entityAId = entityAId;
            this.entityBId = entityBId;
        } else {
            this.entityAId = entityBId;
            this.entityBId = entityAId;
        }
    }

    public int getEntityAId() {
        return entityAId;
    }

    public int getEntityBId() {
        return entityBId;
    }    
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + entityAId;
        result = prime * result + entityBId;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        EntityCoOccurrenceKey other = (EntityCoOccurrenceKey) obj;
        if (entityAId != other.entityAId)
            return false;
        if (entityBId != other.entityBId)
            return false;
        return true;
    }
}
