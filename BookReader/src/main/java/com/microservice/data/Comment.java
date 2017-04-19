package com.microservice.data;

import java.text.SimpleDateFormat;
import java.util.Date;


public class Comment {

	// CommentID
	private String id;
	// 本评论所依附的ArticleID
	private long articleID;

	// comment information
	// 作者名
	private String authorName;
	private String authorUrl;
	private String areaName;
	private String areaCode;

	private String content;

	private Date postTime;
	private String processTime;

	private int sentiment;
	private int agreeCount;

	// 2014010712 便于时间段查询
	private long indexDateHour;

	private boolean isHot = false;

	// 转换date
	private static SimpleDateFormat timeformatter = new SimpleDateFormat(
			"yyyy/MM/dd HH:mm:ss");

	/**
	 * 构造方法
	 */
	public Comment() {
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getAgreeCount() {
		return agreeCount;
	}

	public void setAgreeCount(int agreeCount) {
		this.agreeCount = agreeCount;
	}

	public String getID() {
		return id;
	}

	public void setID(String id) {
		this.id = id;
	}

	public String getProcessTime() {
		return processTime;
	}

	public void setProcessTime(String processTime) {
		this.processTime = processTime;
	}

	public Date getPostTime() {
		return postTime;
	}

	public String getFormatedPostTime() {

		String strTime = null;
		try {
			strTime = Comment.timeformatter.format(postTime);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return strTime;
	}

	public void setPostTime(Date postTime) {
		this.postTime = postTime;
	}

	public String getAuthorUrl() {
		return authorUrl;
	}

	public String getAuthorName() {
		return authorName;
	}

	public void setAuthorName(String authorName) {
		this.authorName = authorName;
	}

	public void setAuthorUrl(String authorurl) {
		this.authorUrl = authorurl;
	}

	public String getAreaName() {
		return areaName;
	}

	public void setAreaName(String areaName) {
		this.areaName = areaName;
	}

	public String getAreaCode() {
		return areaCode;
	}

	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}

	public long getArticleID() {
		return articleID;
	}

	public void setArticleID(long articleID) {
		this.articleID = articleID;
	}

	public int getSentiment() {
		return sentiment;
	}

	public void setSentiment(int sentiment) {
		this.sentiment = sentiment;
	}

	public long getIndexDateHour() {
		return indexDateHour;
	}

	public void setIndexDateHour(long indexDateHour) {
		this.indexDateHour = indexDateHour;
	}

	public boolean isHot() {
		return isHot;
	}

	public void setAsHot(boolean hot) {
		this.isHot = hot;
	}
}
