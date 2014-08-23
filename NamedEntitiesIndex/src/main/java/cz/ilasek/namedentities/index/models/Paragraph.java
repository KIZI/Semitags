package cz.ilasek.namedentities.index.models;


public class Paragraph {
    private final String paragraphUri;
    private final String text;

    public Paragraph(String paragraphUri, String text) {
        super();
        this.paragraphUri = paragraphUri;
        this.text = text;
    }
    
    public String getParagraphUri() {
        return paragraphUri;
    }
    
    public String getArticleId() {
        int separatorIndex = paragraphUri.lastIndexOf("#");
        
        return paragraphUri.substring(0, separatorIndex);
    }

    public String getText() {
        return text;
    }
}
