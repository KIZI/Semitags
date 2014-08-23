package cz.ilasek.namedentities.index.models;

public class EntityOccurrence {

    private final String entityUri;
    private int occurrences = 1;
    
    public EntityOccurrence(String entityUri) {
        super();
        this.entityUri = entityUri;
    }

    public int getOccurrences() {
        return occurrences;
    }

    public void incermentOccurrences() {
        this.occurrences++;
    }

    public String getEntityUri() {
        return entityUri;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((entityUri == null) ? 0 : entityUri.hashCode());
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
        EntityOccurrence other = (EntityOccurrence) obj;
        if (entityUri == null) {
            if (other.entityUri != null)
                return false;
        } else if (!entityUri.equals(other.entityUri))
            return false;
        return true;
    }
    
}
