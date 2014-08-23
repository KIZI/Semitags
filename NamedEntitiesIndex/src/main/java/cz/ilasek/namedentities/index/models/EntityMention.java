package cz.ilasek.namedentities.index.models;

public class EntityMention {
    private long entityId;
    private String surfaceForm;
    private String paragraph;
    private long paragraphId;

    public long getEntityId() {
        return entityId;
    }

    public void setEntityId(long entityId) {
        this.entityId = entityId;
    }

    public String getSurfaceForm() {
        return surfaceForm;
    }

    public void setSurfaceForm(String surfaceForm) {
        this.surfaceForm = surfaceForm;
    }

    public String getParagraph() {
        return paragraph;
    }

    public void setParagraph(String paragraph) {
        this.paragraph = paragraph;
    }

    public long getParagraphId() {
        return paragraphId;
    }

    public void setParagraphId(long paragraphId) {
        this.paragraphId = paragraphId;
    }
}
