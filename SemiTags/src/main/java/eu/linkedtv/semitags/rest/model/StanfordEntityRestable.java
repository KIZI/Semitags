package eu.linkedtv.semitags.rest.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import cz.ilasek.nlp.ner.StanfordEntity;


@XmlRootElement
public class StanfordEntityRestable {
    private final StanfordEntity stanfordEntity;
    
    public StanfordEntityRestable() {
        this(new StanfordEntity());
    }
    
    public StanfordEntityRestable(StanfordEntity stanfordEntity) {
        this.stanfordEntity = stanfordEntity;
    }
    
    @XmlElement
	public int getLength() {
        return stanfordEntity.getLength();
    }

    @XmlElement
    public String getName() {
		return stanfordEntity.getName();
	}

    @XmlElement
	public String getType() {
		return stanfordEntity.getType();
	}
	
    @XmlElement
	public String getCategory() {
	    return stanfordEntity.getCategory();
	}

    @XmlElement
	public int getStart() {
		return stanfordEntity.getStart();
	}

}