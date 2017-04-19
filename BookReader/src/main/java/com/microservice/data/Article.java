package com.microservice.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class Article  implements Serializable {

	private long articleID;
	private int webID;
	private String subject;
	private String digest;
	private String url;

	private String postTime;

	private Date processTime = new Date();

	private String language = "ZH";

	private String author;
	private String authorUrl;

	private String content;

	private int readerCount = 0;
	private int commentCount = 0;
	private int forwardCount;

	private String webName;

	private String webUrl;

	private String engineName;

	private int customID;

	private ArrayList<Article> similars = null;
	private String similarUrl = null;
	private String similarIDs = "";

	private int spam = 0;
	private int sentiment = 0;

	private int mediaType = 10;

	private String unicodeString;

	public String getUnicodeString() {
		return unicodeString;
	}

	public void setUnicodeString(String unicodeString) {
		this.unicodeString = unicodeString;
	}

	public int getMediaType() {
		return mediaType;
	}

	public void setMediaType(int mediaType) {
		this.mediaType = mediaType;
	}

	public void setEngineName(String engineName) {
		this.engineName = engineName;
	}

	public String getEngineName() {
		return engineName;
	}

	public void setSimilarUrl(String similarUrl) {
		this.similarUrl = similarUrl;
	}

	public String getSimilarUrl() {
		return similarUrl;
	}

	public String getSimilarIDs() {
		return similarIDs;
	}

	public void setSimilarIDs(String similarIDs) {
		this.similarIDs = similarIDs;
	}

	public int getCustomID() {
		return customID;
	}

	public void setCustomID(int customID) {
		this.customID = customID;
	}

	public int getWebID() {
		return webID;
	}

	public void setWebID(int webID) {
		this.webID = webID;
	}

	public String getDigest() {
		return digest;
	}

	public void setDigest(String digest) {
		this.digest = digest;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getWebName() {
		return webName;
	}

	public void setWebName(String from) {
		this.webName = from;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getPostTime() {
		return postTime;
	}

	public void setPostTime(String time) {
		this.postTime = time;
	}

	public Date getProcessTime() {
		return processTime;
	}

	public void setProcessTime(Date time) {
		this.processTime = time;
	}

	public String getWebUrl() {
		return webUrl;
	}

	public void setWebUrl(String siteUrl) {
		this.webUrl = siteUrl;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public long getArticleID() {
		return articleID;
	}

	public void setArticleID(long articleId) {
		this.articleID = articleId;
	}

	public int getReaderCount() {
		return readerCount;
	}

	public void setReaderCount(int readerCount) {
		this.readerCount = readerCount;
	}

	public int getCommentCount() {
		return commentCount;
	}

	public void setCommentCount(int comments) {
		this.commentCount = comments;
	}

	public int getSpam() {
		return spam;
	}

	public void setSpam(int spam) {
		this.spam = spam;
	}

	public int getSentiment() {
		return sentiment;
	}

	public void setSentiment(int sentiment) {
		this.sentiment = sentiment;
	}

	public ArrayList<Article> getSimilars() {
		return this.similars;
	}

	public void setSimilars(ArrayList<Article> similarArticles) {
		this.similars = similarArticles;
	}

	public String getAuthorUrl() {
		return authorUrl;
	}

	public void setAuthorUrl(String authorUrl) {
		this.authorUrl = authorUrl;
	}

	public int getForwardCount() {
		return forwardCount;
	}

	public void setForwardCount(int forwardCount) {
		this.forwardCount = forwardCount;
	}

	public int setSimilarArticle(Article similarArticle) {

		if (similarArticle == null)
			return 0;

		if (this.similars == null)
			this.similars = new ArrayList<Article>();

		if (similarArticle.similars != null) {

			for (Article simiAtl : similarArticle.similars) {

				this.similars.add(simiAtl);

				simiAtl.similars.add(this);
			}
		} else {

			similarArticle.similars = new ArrayList<Article>();
		}

		this.similars.add(similarArticle);

		similarArticle.similars.add(this);

		return 1;
	}
}
