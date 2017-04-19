package com.microservice.data;

import java.io.Serializable;




public class Website implements Serializable
{
	private static final long serialVersionUID = -1377829636588862064L;

	//��վID
	private int ID;

	//�������û�AdminID
	private int adminID;
	
	//ý�����ͣ�0-website 1-epaper 2-App 3-bbs 4-Weixin 5-Weibo 6-API 
	private int mediaType;
	
	//վ����ҳ����url
	private String homeUrl; 
	
	//վ������
	private String webName;
		
	//��վ���ڵķ���ID
	private int categoryID;
	private String categoryName;
	
	//��վ�����������
	private String areaCode;
		
	//��վȨ��
	private int weight;
	
	//��Դʶ��������Զ���,����tracedsite
	private String fromTags;
	
	//ԭ��ý�壺����ת��������Դ��ʶ��������ת��������Դ�г�������Ϊ����ת������
	private String ignoreTracedTags;
	
	//ԭ��ý�壺���ñ�ǩ��������¼���ӱ�ֽ�İ������ƵĻ�ȡ�����壬���߼�¼΢���˻��Ĺ����ŵȡ����ݲ�ͬӦ�ÿ���չ
	private String ReservedTag;
	
	//վ��״̬
	private int status; 
	
	
	/**
	 * ���캯��
	 */
	public Website() {
		
	}

	public int getAdminID() {
		return adminID;
	}
	
	public void setAdminID(int adminID) {
		this.adminID = adminID;
	}
	
	public String getWebName() {
		return webName;
	}
	public void setWebName(String webname) {
		this.webName = webname;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	
	public int getWebID() {
		return this.ID;
	}
	
	public void setWebID(int id) {
		this.ID = id;
	}
	
	public int getMediaType() {
		return mediaType;
	}
	
	public void setMediaType(int mediaType) {
		this.mediaType = mediaType;
	}
	
	public String getHomeUrl() {
		return homeUrl;
	}
	public void setHomeUrl(String homeUrl) {
		this.homeUrl = homeUrl;
	}
		
	public int getCategoryID() {
		return categoryID;
	}


	public void setCategoryID(int categoryID) {
		this.categoryID = categoryID;
	}

	
	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public String getAreaCode() {
		return areaCode;
	}

	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
	}	
	
	public int getWeight() {
		return weight;
	}


	public void setWeight(int weight) {
		this.weight = weight;
	}

	public String getFromTags() {
		return fromTags;
	}

	public void setFromTags(String fromTags) {
		this.fromTags = fromTags;
	}

	public String getIgnoreTracedTags() {
		return ignoreTracedTags;
	}

	public void setIgnoreTracedTags(String ignoreTracedTags) {
		this.ignoreTracedTags = ignoreTracedTags;
	}

	public String getReservedTag() {
		return ReservedTag;
	}

	public void setReservedTag(String reservedTag) {
		ReservedTag = reservedTag;
	}
	
}
