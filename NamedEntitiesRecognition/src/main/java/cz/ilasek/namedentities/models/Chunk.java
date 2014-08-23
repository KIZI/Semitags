package cz.ilasek.namedentities.models;

public class Chunk {
    private String chunk;
    private String type;

    public String getChunk() {
        return chunk;
    }

    public void setChunk(String chunk) {
        this.chunk = chunk;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    public void addChunkPart(String chunkPart) {
        chunk += " " + chunkPart;
    }
}
