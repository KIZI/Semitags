package cz.ilasek.namedentities.index;

import java.util.LinkedList;
import java.util.List;

public class Paragraph {
    private String text;
    private final List<Link> links;
    
    public Paragraph(String text) {
        setText(text);
        links = new LinkedList<Link>();
    }
    
    public void setText(String text) {
        this.text = text.replaceAll("\\{\\{.*\\}\\}", "");
    }
    
    public String getText() {
        return text;
    }

    public List<Link> getLinks() {
        return links;
    }
    
    public void addLink(Link link) {
        links.add(link);
    }
    
    public boolean hasLinks() {
        return links.size() > 0;
    }
}
