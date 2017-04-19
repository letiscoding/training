package com.microservice.data;

import java.io.Serializable;


public class MediaChannel implements Serializable
{
	private static final long serialVersionUID = 8452988627797814476L;

	//Ƶ��ID
	private int ID;
	
	//��Ƶ��������ý��
	private Website media;
	
	//Ƶ������
	private String name;

	//��Ƶ��������url
	private String homeUrl; 
		
	//վ��ҳ�����
	private String charset; 
		
	//����վ��Ӧ����վɨ��ģ��
	private ScanPanel scanPanel;
	
	//վ��״̬
	private int status; 
	
	
	/**
	 * ���캯��
	 */
	public MediaChannel() {
		scanPanel = new ScanPanel();
	}


	public Website getMedia() {
		return media;
	}

	public void setMedia(Website media) {
		this.media = media;
	}

	public String getName() {
		return name;
	}
	public void setName(String channelName) {
		this.name = channelName;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	
	public int getID() {
		return this.ID;
	}
	
	public void setID(int id) {
		this.ID = id;
	}
		
	public String getHomeUrl() {
		return homeUrl;
	}
	public void setHomeUrl(String homeUrl) {
		this.homeUrl = homeUrl;
	}
	public String getCharset() {
		return charset;
	}
	public void setCharset(String charset) {
		this.charset = charset;
	}

	public ScanPanel getScanPanel() {
		return scanPanel;
	}

	public void setScanPanel(ScanPanel scanPanel) {
		this.scanPanel = scanPanel;
	}
	
}
