package edu.columbia.advancedb.bing.vo;

public class AppDocument {
	private String url;
	private String title;
	private String description;
	private boolean isRelevant;
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public boolean isRelevant() {
		return isRelevant;
	}
	public void setRelevant(boolean isRelevant) {
		this.isRelevant = isRelevant;
	}
}
