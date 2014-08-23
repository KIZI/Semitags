package eu.linkedtv.semitags.rest.model;

import javax.xml.bind.annotation.XmlAttribute;


public class EntityOccurrence {
    private int start;
    private int end;
    
    public EntityOccurrence() {
        
    }
    
    public EntityOccurrence(int start, int end) {
        this.start = start;
        this.end = end;
    }

    @XmlAttribute(name="start")
    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    @XmlAttribute(name="end")
    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + end;
        result = prime * result + start;
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
        if (end != other.end)
            return false;
        if (start != other.start)
            return false;
        return true;
    }   
}
