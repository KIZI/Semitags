package cz.ilasek.namedentities.models;

public class DisambiguatedEntity {
    private static final String WIKIPEDIA_BASE_URI = "http://de.wikipedia.org/wiki/";
    private static final String DBPEDIA_BASE_URI = "http://de.dbpedia.org/resource/";

    private long entityId;
    private String name;
    private String uri;
    private int groupId;
    private float srtingScore;
    private double graphScore = 0;
    private int intScore = 0;
    private String type;

    public int getIntScore() {
        return intScore;
    }

    public void addIntScore(int intScore) {
        this.intScore += intScore;
    }

    public long getEntityId() {
        return entityId;
    }

    public void setEntityId(long entity_id) {
        this.entityId = entity_id;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getDBpediaUri() {
        return DBPEDIA_BASE_URI + uri.substring(WIKIPEDIA_BASE_URI.length());
    }

    public float getStringScore() {
        return srtingScore;
    }

    public void setStringScore(float stringScore) {
        this.srtingScore = stringScore;
    }

    public double getGraphScore() {
        return graphScore;
    }

    public void setGraphScore(double graphScore) {
        this.graphScore = graphScore;
    }

    public void addGraphScore(double graphScore) {
        this.graphScore += graphScore;
    }

    @Override
    public String toString() {
        return name + " <" + uri + "> [" + srtingScore + " - " + graphScore
                + "] (" + type + ")";
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
