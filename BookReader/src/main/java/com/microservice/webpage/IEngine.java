package com.microservice.webpage;

import java.util.*;

import com.microservice.data.Article;

/**
 * 网页抓取接口
 *
 * @author
 *
 */
public interface IEngine {

	/**
	 * 根据关键字、页码抓取当前页码内的所有新闻
	 *
	 * @param keyword
	 *            关键字
	 * @param pagenum
	 *            页码
	 * @return 抓取的新闻列表
	 */
	public ArrayList<Article> searchByKeyword(String keyword, int pagenum);

	/**
	 * 根据关键字，抓取所有相关新闻
	 *
	 * @param keyword
	 *            关键字
	 * @return 抓取的新闻列表
	 */
	public ArrayList<Article> searchByKeyword(String keyword);

	/**
	 * 释放资源
	 */
	public void freeResource();

	/**
	 * 根据url获取新闻列表（抓取相似新闻的引擎需实现此方法，如BaiduNewsEngine,So360NewsEngine,
	 * SogouNewsEngine）
	 *
	 * @param newsUrl
	 *            相似新闻url
	 * @return 抓取的新闻列表
	 */
	public ArrayList<Article> getSimilarArticles(String newsUrl);

	/**
	 * 得到格式化的关键字列表(目前业务代码中没有用到此方法)
	 *
	 * @param keyword
	 *            关键字
	 * @return
	 */
	public ArrayList<String> getFormatKeywordList(String keyword);
}
