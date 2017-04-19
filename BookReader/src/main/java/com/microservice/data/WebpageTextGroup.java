package com.microservice.data;

public class WebpageTextGroup {

	private String keywords;

	private String description;

	private String title;

	private String linkText;

	private String normalText;

	public WebpageTextGroup() {
		keywords = "";
		description = "";
		title = "";
		linkText = "";
		normalText = "";
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public String getLinkText() {
		return linkText;
	}

	public void setLinkText(String linkText) {
		this.linkText = linkText;
	}

	public String getNormalText() {
		return normalText;
	}

	public void setNormalText(String normalText) {
		this.normalText = normalText;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

}
