package cz.ilasek.namedentities.index.models;

public class EntitySurfaceFormMention {
    private final String entityUri;
    private final String surfaceForm;

    public EntitySurfaceFormMention(String entityUri, String surfaceForm) {
        this.entityUri = entityUri;
        this.surfaceForm = surfaceForm;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((entityUri == null) ? 0 : entityUri.hashCode());
        result = prime * result + ((surfaceForm == null) ? 0 : surfaceForm.hashCode());
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
        EntitySurfaceFormMention other = (EntitySurfaceFormMention) obj;
        if (entityUri == null) {
            if (other.entityUri != null)
                return false;
        } else if (!entityUri.equals(other.entityUri))
            return false;
        if (surfaceForm == null) {
            if (other.surfaceForm != null)
                return false;
        } else if (!surfaceForm.equals(other.surfaceForm))
            return false;
        return true;
    }

    public String getEntityUri() {
        return entityUri;
    }

    public String getSurfaceForm() {
        return surfaceForm;
    }
}
