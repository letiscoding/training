package com.microservice.data;

public class Webpage {

	private String title = null;

	private String postTime = null;

	private String content = "";

	private String charset;

	private String contentType;

	private String htmlSource;

	private String webTitle = null;

	private WebpageType pageType = WebpageType.TEXT_PAGE;

	private String url = "";

	private String contentStartTag = "";
	private String contentEndTag = "";

	private String httpStatusCode = "200";

	public Webpage() {
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return this.title;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getContent() {
		return this.content;
	}

	public void setPostTime(String postTime) {
		this.postTime = postTime;
	}

	public String getPostTime() {
		return this.postTime;
	}

	public void setWebTitle(String webTitle) {
		this.webTitle = webTitle;
	}

	public String getWebTitle() {
		return this.webTitle;
	}

	public WebpageType getPageType() {
		return pageType;
	}

	public void setPageType(WebpageType pageType) {
		this.pageType = pageType;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getHtmlSource() {
		return htmlSource;
	}

	public void setHtmlSource(String htmlSource) {
		this.htmlSource = htmlSource;
	}

	public String getContentEndTag() {
		return contentEndTag;
	}

	public void setContentEndTag(String contentEndTag) {
		this.contentEndTag = contentEndTag;
	}

	public String getContentStartTag() {
		return contentStartTag;
	}

	public void setContentStartTag(String contentStartTag) {
		this.contentStartTag = contentStartTag;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getHttpStatusCode() {
		return httpStatusCode;
	}

	public void setHttpStatusCode(String httpStatuscode) {
		this.httpStatusCode = httpStatuscode;
	}
}
