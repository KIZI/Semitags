package cz.ilasek.nlp.ner;


public class StanfordEntity {
	private String name;
	private String type;
	private String category;
	private int start;
	private int length;

	public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}
	
	public String getCategory() {
	    return category;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getStart() {
		return start;
	}

	public void setStart(int start) {
		this.start = start;
	}
	
	public void setCategory(String category) {
	    this.category = category;
	}

	@Override
	public String toString() {
		return "Standford named entity: name = " + name + " type=" + type
				+ " categgory "+ category + " start" + start + " length " + length;
	}
}