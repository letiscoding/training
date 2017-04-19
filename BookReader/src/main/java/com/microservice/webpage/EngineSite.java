package com.microservice.webpage;

/**
 * <p>
 * 网页抓取引擎相关属性，解析config.xml后，会将元素放EngineSite中。见{@link TraceWorker}中run()方法
 * <p>
 * 示例:<Engine name="baiduWebpage" maxPages="2" interval="5">
 * <![CDATA[http://www.baidu.com/s?wd=ABC&ie=gbk&rn=50]]> </Engine>
 * 
 * @author
 *
 */
public class EngineSite {

	// 名称
	private String name;

	// 网站使用语言
	private String language;

	// 开始页url
	private String startUrl;

	// 每次最多获取的页面数量
	private int maxPages;

	// 抓取网页的时间间隔，单位:秒
	private int interval;

	public EngineSite() {

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getStartUrl() {
		return startUrl;
	}

	public void setStartUrl(String startUrl) {
		this.startUrl = startUrl;
	}

	public int getMaxPages() {
		return maxPages;
	}

	public void setMaxPages(int maxPages) {
		this.maxPages = maxPages;
	}

	public int getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}

}
