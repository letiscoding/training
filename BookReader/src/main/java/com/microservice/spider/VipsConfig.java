package com.microservice.spider;

import java.util.ArrayList;

public class VipsConfig {

	private double linkRate = 0.80;

	private double textRate = 0.75;

	private double textRatioLink = 0.025;

	private int minTextLength = 5;

	private int minContentLength = 80;

	private ArrayList<String> indexPageTags;

	private int maxMergeTextLength = 2000;

	private int minBlockTextLength = 15;

	private int declareTextLength = 200;

	private double maxBlockRate = 0.6;

	private int firsBlockLength = 60;

	public VipsConfig() {

	}

	public ArrayList<String> getIndexPageTags() {
		return indexPageTags;
	}

	public double getLinkRate() {
		return linkRate;
	}

	public void setLinkRate(double linkRate) {
		this.linkRate = linkRate;
	}

	public double getTextRate() {
		return textRate;
	}

	public void setTextRate(double textRate) {
		this.textRate = textRate;
	}

	public int getMinTextLength() {
		return minTextLength;
	}

	public void setMinTextLength(int minTextLength) {
		this.minTextLength = minTextLength;
	}

	public int getMinContentLength() {
		return minContentLength;
	}

	public void setMinContentLength(int minContentLength) {
		this.minContentLength = minContentLength;
	}

	public int getMaxMergeTextLength() {
		return maxMergeTextLength;
	}

	public void setMaxMergeTextLength(int maxMergeTextLength) {
		this.maxMergeTextLength = maxMergeTextLength;
	}

	public int getFirsBlockLength() {
		return firsBlockLength;
	}

	public void setFirsBlockLength(int firsBlockLength) {
		this.firsBlockLength = firsBlockLength;
	}

	public int getMinBlockTextLength() {
		return minBlockTextLength;
	}

	public void setMinBlockTextLength(int minBlockTextLength) {
		this.minBlockTextLength = minBlockTextLength;
	}

	public double getMaxBlockRate() {
		return maxBlockRate;
	}

	public void setMaxBlockRate(double maxBlockRate) {
		this.maxBlockRate = maxBlockRate;
	}

	public double getTextRatioLink() {
		return textRatioLink;
	}

	public void setTextRatioLink(double textRatioLink) {
		this.textRatioLink = textRatioLink;
	}

	public int getDeclareTextLength() {
		return declareTextLength;
	}

	public void setDeclareTextLength(int declareTextLength) {
		this.declareTextLength = declareTextLength;
	}

	public static void main(String[] args) {

	}

}