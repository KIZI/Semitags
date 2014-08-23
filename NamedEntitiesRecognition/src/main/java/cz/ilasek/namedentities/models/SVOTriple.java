package cz.ilasek.namedentities.models;

public class SVOTriple {
    private Chunk subject;
    private Chunk verb;
    private Chunk object;

    public Chunk getSubject() {
        return subject;
    }

    public void setSubject(Chunk subject) {
        this.subject = subject;
    }

    public Chunk getVerb() {
        return verb;
    }

    public void setVerb(Chunk verb) {
        this.verb = verb;
    }

    public Chunk getObject() {
        return object;
    }

    public void setObject(Chunk object) {
        this.object = object;
    }
    
    public String getSubjectStr() {
        if (subject != null)
            return subject.getChunk();
        else 
            return "";
    }
    
    public String getObjectStr() {
        if (object != null)
            return object.getChunk();
        else 
            return "";
    }
    
    public String getVerbStr() {
        if (verb != null)
            return verb.getChunk();
        else 
            return "";
    }
    
    public String toString() {
        String subj = (subject == null) ? "***" : subject.getChunk();
        String vrb = (verb == null) ? "***" : verb.getChunk();
        String obj = (object == null) ? "***" : object.getChunk();
        
        return subj + " - " + vrb + " - " + obj;
    }
    
}
