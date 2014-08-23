package cz.ilasek.namedentities.index.models;

public class EntityMentionInParagraph {
    private final String entityUri;
    private final String surfaceForm;
    private final String paragraphUri;

    public EntityMentionInParagraph(String entityUri, String surfaceForm, String paragraphUri) {
        super();
        this.entityUri = entityUri;
        this.surfaceForm = surfaceForm;
        this.paragraphUri = paragraphUri;
    }

    public String getEntityUri() {
        return entityUri;
    }

    public String getSurfaceForm() {
        return surfaceForm;
    }

    public String getParagraphUri() {
        return paragraphUri;
    }

}
