package com.microservice.webpage;

import com.microservice.browser.EngineBrowser;
import com.microservice.data.Article;
import com.microservice.util.RunstatReporter;
import com.microservice.util.StringUtil;
import org.apache.log4j.Logger;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.util.NodeList;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

/**
 * 网页抓取抽象类，提供通用的属性，方法
 * 
 * @author
 *
 */
public abstract class Engine implements IEngine {

	public Logger logger = Logger.getLogger(Engine.class);

	// 网页抓取引擎相关信息
	protected EngineSite engineSite;

	// 下一页url
	protected String nextPageUrl;

	// 排序类型
	protected int orderType = 0;

	// EngineBrowser提供通过HTTP方式浏览网页源代码相关方法
	protected EngineBrowser curEngineBrowser = null;

	/**
	 * 设置网页抓取引擎
	 * 
	 * @param engineSite
	 *            网页抓取引擎
	 */
	public Engine(EngineSite engineSite) {
		this.engineSite = engineSite;
	}

	/**
	 * 获取网页抓取引擎
	 * 
	 * @return 网页抓取引擎
	 */
	public EngineSite getEngineSite() {
		return engineSite;
	}

	/**
	 * 获取下一页url
	 * 
	 * @return 下一页url
	 */
	public String getNextPageUrl() {
		return nextPageUrl;
	}

	/**
	 * 设置下一页url
	 * 
	 * @param nextPageUrl
	 */
	public void setNextPageUrl(String nextPageUrl) {
		this.nextPageUrl = nextPageUrl;
	}

	/**
	 * 获取排序类型
	 * 
	 * @return 排序类型
	 */
	public int getOrderType() {
		return orderType;
	}

	/**
	 * 设置排序类型
	 * 
	 * @param orderType
	 *            排序类型
	 */
	public void setOrderType(int orderType) {
		this.orderType = orderType;
	}

	/**
	 * 检查新闻列表中新闻的日期格式是否为“yyyy/MM/dd HH:mm:ss”
	 * 
	 * @param parsedList
	 *            新闻列表
	 * @return true代表格式正确
	 */
	public boolean checkTimeFormat(ArrayList<Article> parsedList) {

		if ((parsedList == null) || (parsedList.size() == 0))
			return true;

		int badformat = 0;

		Date date = new Date();
		long curSecond = date.getTime() / 1000;

		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		for (Article oneArticle : parsedList) {
			long articleSecond = 0;
			try {
				Date t = formatter.parse(oneArticle.getPostTime());
				articleSecond = t.getTime() / 1000;
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}

			if (Math.abs(curSecond - articleSecond) < 60)
				badformat++;
		}

		if (badformat * 2 > parsedList.size()) {
			logger.fatal("TimeFormatParseError Engine:" + engineSite.getName());
			return false;
		}

		return true;
	}

	/**
	 * 根据关键字、页码抓取当前页码内的所有新闻
	 * 
	 * @param keyword
	 *            关键字
	 * @param pagenum
	 *            页码
	 * @return 抓取的新闻列表
	 */
	public ArrayList<Article> searchByKeyword(String keyword, int pageNo) {
		if (keyword == null || keyword.trim().length() == 0)
			return null;

		if (pageNo <= 0 || pageNo > engineSite.getMaxPages())
			return null;

		ArrayList<Article> articleList = analyze(keyword, pageNo);

		return articleList;

	}

	/**
	 * 根据关键字，抓取所有相关新闻
	 * 
	 * @param keyword
	 *            关键字
	 * @return 抓取的新闻列表
	 */
	public ArrayList<Article> searchByKeyword(String keyword) {
		if (keyword == null || keyword.trim().length() == 0)
			return null;

		ArrayList<Article> ArticleList = new ArrayList<Article>();

		for (int i = 1; i <= engineSite.getMaxPages(); i++) {
			ArrayList<Article> articleList = analyze(keyword, i);
			if (articleList != null)
				ArticleList.addAll(articleList);

		}

		return ArticleList;
	}

	/**
	 * 根据关键字、页码获取新闻列表<br/>
	 * 抽象方法，子类需要实现细节
	 * 
	 * @param keyword
	 *            关键字
	 * @param pageorder
	 *            页码
	 * @return 新闻列表
	 */
	public abstract ArrayList<Article> analyze(String keyword, int pageorder);

	public String formatKeyword(String keyword) {
		return keyword;
	}

	/**
	 * 目前没有业务使用此方法，出现在getFormatKeywordList()方法中
	 * 
	 * @param all
	 * @return
	 */
	public ArrayList<String> getORList(ArrayList<String[]> all) {
		ArrayList<String> list = new ArrayList<String>();
		HashSet<String> set = new HashSet<String>();

		String tmpStr = "";
		getANDTmp(tmpStr, 0, all, list, set);

		return list;
	}

	/**
	 * 目前没有业务使用此方法，出现在getORList()方法中
	 * 
	 * @param tmpStr
	 * @param nowIndex
	 * @param all
	 * @param list
	 * @param set
	 */
	private void getANDTmp(String tmpStr, int nowIndex,
			ArrayList<String[]> all, ArrayList<String> list, HashSet<String> set) {
		String[] now = all.get(nowIndex);
		int size = all.size();
		String nowKey = tmpStr;

		for (int i = 0; i < now.length; i++) {

			if (now[i] == null)
				now[i] = "";

			if (nowIndex == 0) {
				tmpStr = now[i].trim();
			} else {
				tmpStr += "+" + now[i].trim();
			}
			if (nowIndex == size - 1) {
				if (!set.contains(tmpStr)) {
					set.add(tmpStr);
					list.add(tmpStr);
				}
				tmpStr = nowKey;
			} else {
				getANDTmp(tmpStr, nowIndex + 1, all, list, set);
				tmpStr = nowKey;
			}
		}
	}

	/**
	 * 目前没有业务代码使用此方法，出现在getFormatKeywordList()方法中
	 * 
	 * @param keyword
	 * @return
	 */
	public ArrayList<String> getExcluedKeywordList(String keyword) {
		ArrayList<String> notKeywords = new ArrayList<String>();

		int lastNotIndex = -1;
		int minusIndex = -1;
		for (int i = 0; i < keyword.length(); i++) {
			minusIndex = keyword.indexOf("-", i);
			if (minusIndex > 0) {
				if (lastNotIndex != -1) {
					String strTemp = keyword.substring(lastNotIndex + 1,
							minusIndex);
					notKeywords.add(strTemp.trim());
				}
				lastNotIndex = minusIndex;

				i = minusIndex + 1;
			}
		}
		if (lastNotIndex != -1)
			notKeywords.add(keyword.substring(lastNotIndex + 1).trim());

		for (int i = 0; i < notKeywords.size(); i++) {
			String strKey = notKeywords.get(i);

			int plus = strKey.indexOf("+");
			if (plus != -1) {
				strKey = strKey.substring(0, plus);
				notKeywords.set(i, strKey.trim());
			}

			if (strKey.startsWith("(")) {
				String strNor = strKey.substring(1, strKey.length() - 1);
				String[] moreNotKeywords = strNor.split(",");
				if (moreNotKeywords.length > 0) {
					for (int k = 0; k < moreNotKeywords.length; k++)
						notKeywords.add(moreNotKeywords[k].trim());
				}
			}
		}

		for (int i = 0; i < notKeywords.size(); i++) {
			String strKey = notKeywords.get(i);

			if (strKey.startsWith("(")) {
				notKeywords.remove(i);
			}
		}

		return notKeywords;
	}

	/**
	 * 格式化关键字，目前没有业务代码使用此方法
	 */
	public ArrayList<String> getFormatKeywordList(String keyword) {
		ArrayList<String> keywordList = new ArrayList<String>();

		String formatKeyword = null;

		if (keyword == null)
			return null;

		ArrayList<String> andKeywords = new ArrayList<String>();

		int lastAndIndex = -1;

		for (int i = 0; i < keyword.length(); i++) {
			int plusIndex = keyword.indexOf("+", i);
			if (plusIndex > 0) {
				String strTemp = keyword.substring(lastAndIndex + 1, plusIndex);
				andKeywords.add(strTemp.trim());
				lastAndIndex = plusIndex;

				i = plusIndex + 1;
			}
		}

		andKeywords.add(keyword.substring(lastAndIndex + 1).trim());

		for (int i = 0; i < andKeywords.size(); i++) {
			String strKey = andKeywords.get(i);
			if (strKey.trim().length() > 1) {
				if (strKey.startsWith("\""))
					continue;

				int k1 = strKey.indexOf("-");
				if (k1 >= 0) {
					String newStr = strKey.substring(0, k1);
					for (int j = k1 + 1; j < strKey.length(); j++) {
						char a = strKey.charAt(j);
						if (a == '+') {
							newStr += strKey.substring(j);
							break;
						}
					}
					andKeywords.set(i, newStr);
				}
			}
		}

		ArrayList<String[]> all = new ArrayList<String[]>();
		for (int i = 0; i < andKeywords.size(); i++) {
			String strKey = andKeywords.get(i);
			int k1 = strKey.indexOf("(");
			int k2 = strKey.indexOf(")");
			if (k1 >= 0 && k2 > 0 && k2 > k1) {
				String sw = strKey.substring(k1 + 1, k2);
				andKeywords.set(i, sw);

				all.add(sw.split(","));
			} else
				all.add(strKey.split(","));

		}

		String strTemp = null;
		ArrayList<String> list = getORList(all);
		for (int i = 0; i < list.size(); i++) {
			if (i == 0)
				formatKeyword = "";

			strTemp = list.get(i);
			if (strTemp != null)
				strTemp = strTemp.replace("+", " ");

			if (i == 0)
				formatKeyword = strTemp;
			else
				formatKeyword += "|" + strTemp;
		}

		String[] fklist = formatKeyword.split("\\|");
		for (int i = 0; i < fklist.length; i++) {
			keywordList.add(fklist[i]);
		}

		return keywordList;
	}

	/**
	 * 根据样式名称、样式值筛选出子标签结点
	 * 
	 * @param parent
	 *            父标签结点
	 * @param classSimpleName
	 *            样式名称
	 * @param classValue
	 *            样式值
	 * @return 子标签结点
	 */
	public TagNode getChildTagNode(TagNode parent, String classSimpleName,
			String classValue) {
		TagNode child = null;

		// 获取所有子节点
		NodeList list = parent.getChildren();
		if (list == null)
			return null;

		String strClassName = null;
		for (int j = 0; j < list.size(); j++) {
			strClassName = list.elementAt(j).getClass().getSimpleName();

			// 筛选出指定样式的结点
			if (strClassName.equalsIgnoreCase(classSimpleName)) {
				child = (TagNode) list.elementAt(j);

				if (classValue == null)
					return child;

				String st = child.getAttribute("class");

				// 筛选出指定样式值的结点
				if ((st != null) && (st.trim().equalsIgnoreCase(classValue))) {

					return child;
				}
			}
		}

		return null;
	}

	/**
	 * 根据样式名称筛选出子标签结点
	 * 
	 * @param parent
	 *            父标签结点
	 * @param classSimpleName
	 * @return 子标签结点
	 */
	public TagNode getChildTagNode(TagNode parent, String classSimpleName) {
		TagNode child = null;

		// 获取所有子结点
		NodeList list = parent.getChildren();
		if (list == null)
			return null;

		String strClassName = null;
		for (int j = 0; j < list.size(); j++) {
			strClassName = list.elementAt(j).getClass().getSimpleName();

			// 筛选出指定样式的结点
			if (strClassName.equalsIgnoreCase(classSimpleName)) {
				child = (TagNode) list.elementAt(j);

				String st = child.getAttribute("class");
				if (st == null) {
					break;
				}
			}
		}

		return child;
	}

	public void freeResource() {

	}

	/**
	 * 报告异常信息
	 * 
	 * @param strErrDesc
	 * @return
	 */
	public int reportError(String strErrDesc) {
		RunstatReporter runStatUtil = new RunstatReporter();
		runStatUtil.setPara("Error", "paser item regex error");
		runStatUtil.setPara("LogDesc", strErrDesc);
		runStatUtil.report(RunstatReporter.LogType_MIDDLE,
				RunstatReporter.LogLevel_ERROR);

		return 1;
	}

	/**
	 * 判断是否是合法新闻
	 * 
	 * @param article
	 * @return
	 */
	public static boolean isLeagalNews(Article article) {
		boolean isLeagal = true;

		if (article.getUrl() == null)
			return false;

		if (article.getUrl().indexOf("weibo.com") >= 0
				|| (article.getUrl().indexOf("t.qq.com") >= 0))
			return false;

		// 判断是否是内容页面
		isLeagal = StringUtil.isContentPage(article.getUrl());

		return isLeagal;
	}

	public String getRedirectUrl(String location) {
		return location;
	}

	public static void main(String[] args) {
		String url = "http://www.wealink.com/zhiwei/view/14881603/index/default.html";
		url = "http://www.wealink.com/zhiwei/view/14881603";

		Article article = new Article();
		article.setUrl(url);

		if (Engine.isLeagalNews(article))
			System.out.println("is leagal");
		else
			System.out.println("not leagal");
	}
}
