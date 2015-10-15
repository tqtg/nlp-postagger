package main;

public class Observation {
	private String text;
	private String tag;
	
	public Observation() {
		this.text = "";
		this.tag = "";
	}
	
	public Observation(String text, String tag) {
		setText(text);
		setTag(tag);
	}
	
	public String getText() {
		return this.text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public String getTag() {
		return this.tag;
	}
	
	public void setTag(String tag) {
		this.tag = tag;
	}
}
