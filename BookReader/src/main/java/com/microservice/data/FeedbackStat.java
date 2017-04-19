package com.microservice.data;

import java.io.Serializable;
import java.util.HashMap;



public class FeedbackStat implements Serializable
{
	private static final long serialVersionUID = 5278229281482759825L;

	//���ĵ�ת��ָ��
	private int weight;
	
	//�����ܵ�ת�ش���
	private int tracedCount;
	
	//���ĵĲ���ת����վ�����Բ�ͬwebsiteIDΪ���㵥λ��΢���˻���΢����Ϊ������ת��վ�㣩
	private int tracedsiteCount;
	
	//ԭ��������ת����Ѷ���й��Ķ�����ת�������������͵�������
	private int readCount;
	private int forwardCount;
	private int commentCount;
	private int agreeCount;
	
	//ԭ��������ת����Ѷ���۵����ͳ��
	private int positiveCount;
	private int negativeCount;
	private int neuterCount;

	//��վ����ͳ��Map��<areaCode,Count>
	private HashMap<String,Integer> areaStatMap = new HashMap<String,Integer>();
	
	//��վ�Զ������ͳ��Map��<CategoryID,Count>
	private HashMap<Integer,Integer> categoryStatMap = new HashMap<Integer,Integer>();
	
	//��վ��ҵͳ��Map��<InduetryID,Count>
	private HashMap<Integer,Integer> industryStatMap = new HashMap<Integer,Integer>();
		
	//��վý�����͵�ͳ��Map��<MediaType,Count>
	private HashMap<Integer,Integer> mediaTypeStatMap = new HashMap<Integer,Integer>();
		
	public FeedbackStat() {
		
	}

	
	public int getWeight() {
		return weight;
	}


	public void setWeight(int weight) {
		this.weight = weight;
	}


	public int getTracedCount() {
		return tracedCount;
	}


	public void setTracedCount(int tracedCount) {
		this.tracedCount = tracedCount;
	}


	public int getTracedsiteCount() {
		return tracedsiteCount;
	}


	public void setTracedsiteCount(int tracedsiteCount) {
		this.tracedsiteCount = tracedsiteCount;
	}


	public int getReadCount() {
		return readCount;
	}


	public void setReadCount(int readCount) {
		this.readCount = readCount;
	}


	public int getForwardCount() {
		return forwardCount;
	}


	public void setForwardCount(int forwardCount) {
		this.forwardCount = forwardCount;
	}


	public int getCommentCount() {
		return commentCount;
	}


	public void setCommentCount(int commentCount) {
		this.commentCount = commentCount;
	}


	public int getAgreeCount() {
		return agreeCount;
	}


	public void setAgreeCount(int agreeCount) {
		this.agreeCount = agreeCount;
	}


	public int getPositiveCount() {
		return positiveCount;
	}


	public void setPositiveCount(int positiveCount) {
		this.positiveCount = positiveCount;
	}


	public int getNegativeCount() {
		return negativeCount;
	}


	public void setNegativeCount(int negativeCount) {
		this.negativeCount = negativeCount;
	}


	public int getNeuterCount() {
		return neuterCount;
	}


	public void setNeuterCount(int neuterCount) {
		this.neuterCount = neuterCount;
	}


	public HashMap<String, Integer> getAreaStatMap() {
		return areaStatMap;
	}


	public void setAreaStatMap(HashMap<String, Integer> areaStatMap) {
		this.areaStatMap = areaStatMap;
	}


	public HashMap<Integer, Integer> getCategoryStatMap() {
		return categoryStatMap;
	}


	public void setCategoryStatMap(HashMap<Integer, Integer> categoryStatMap) {
		this.categoryStatMap = categoryStatMap;
	}


	public HashMap<Integer, Integer> getIndustryStatMap() {
		return industryStatMap;
	}


	public void setIndustryStatMap(HashMap<Integer, Integer> industryStatMap) {
		this.industryStatMap = industryStatMap;
	}


	public HashMap<Integer, Integer> getMediaTypeStatMap() {
		return mediaTypeStatMap;
	}


	public void setMediaTypeStatMap(HashMap<Integer, Integer> mediaTypeStatMap) {
		this.mediaTypeStatMap = mediaTypeStatMap;
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		

	}

}
