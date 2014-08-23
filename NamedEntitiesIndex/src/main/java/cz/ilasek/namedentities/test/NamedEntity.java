package cz.ilasek.namedentities.test;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class NamedEntity {

    private Double confidence;
    private String wikipediaUri;
    private String dbpediaUri;
    private String type;

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
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

}
