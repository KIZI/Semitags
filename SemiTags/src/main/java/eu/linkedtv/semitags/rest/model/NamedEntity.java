package eu.linkedtv.semitags.rest.model;

import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class NamedEntity {
    private String name;
    private String wikipediaUri;
    private String dbpediaUri;
    
    @XmlElement(name="occurrence")
    private List<EntityOccurrence> occurrences = new LinkedList<EntityOccurrence>(); 
    private String type;
    private double confidence;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getWikipediaUri() {
        return wikipediaUri;
    }

    public void setWikipediaUri(String wikipediaUri) {
        this.wikipediaUri = wikipediaUri;
    }

    public String getDbpediaUri() {
        return dbpediaUri;
    }

    public void setDbpediaUri(String dbpediaUri) {
        this.dbpediaUri = dbpediaUri;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }
    
    public void addOccurrence(int start, int end) {
        EntityOccurrence entityOccurrence = new EntityOccurrence(start, end);
        if (!occurrences.contains(entityOccurrence)) {
            occurrences.add(entityOccurrence);
        }
    }
    
    public List<EntityOccurrence> getOccurrences() {
        return occurrences;
    }
}
