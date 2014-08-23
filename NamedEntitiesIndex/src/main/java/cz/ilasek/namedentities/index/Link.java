package cz.ilasek.namedentities.index;

public class Link {
    private Paragraph paragraph;
    private String target;
    private String surfaceForm;

    public Link(Paragraph paragraph, String target, String surfaceForm) {
        this.paragraph = paragraph;
        this.target = target;
        this.surfaceForm = surfaceForm;
    }
 
    
    public Paragraph getParagraph() {
        return paragraph;
    }

    public void setParagraph(Paragraph paragraph) {
        this.paragraph = paragraph;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getSurfaceForm() {
        return surfaceForm;
    }

    public void setSurfaceForm(String surfaceForm) {
        this.surfaceForm = surfaceForm;
    }
}
