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
 * ��ҳץȡ�����࣬�ṩͨ�õ����ԣ�����
 * 
 * @author
 *
 */
public abstract class Engine implements IEngine {

	public Logger logger = Logger.getLogger(Engine.class);

	// ��ҳץȡ���������Ϣ
	protected EngineSite engineSite;

	// ��һҳurl
	protected String nextPageUrl;

	// ��������
	protected int orderType = 0;

	// EngineBrowser�ṩͨ��HTTP��ʽ�����ҳԴ������ط���
	protected EngineBrowser curEngineBrowser = null;

	/**
	 * ������ҳץȡ����
	 * 
	 * @param engineSite
	 *            ��ҳץȡ����
	 */
	public Engine(EngineSite engineSite) {
		this.engineSite = engineSite;
	}

	/**
	 * ��ȡ��ҳץȡ����
	 * 
	 * @return ��ҳץȡ����
	 */
	public EngineSite getEngineSite() {
		return engineSite;
	}

	/**
	 * ��ȡ��һҳurl
	 * 
	 * @return ��һҳurl
	 */
	public String getNextPageUrl() {
		return nextPageUrl;
	}

	/**
	 * ������һҳurl
	 * 
	 * @param nextPageUrl
	 */
	public void setNextPageUrl(String nextPageUrl) {
		this.nextPageUrl = nextPageUrl;
	}

	/**
	 * ��ȡ��������
	 * 
	 * @return ��������
	 */
	public int getOrderType() {
		return orderType;
	}

	/**
	 * ������������
	 * 
	 * @param orderType
	 *            ��������
	 */
	public void setOrderType(int orderType) {
		this.orderType = orderType;
	}

	/**
	 * ��������б������ŵ����ڸ�ʽ�Ƿ�Ϊ��yyyy/MM/dd HH:mm:ss��
	 * 
	 * @param parsedList
	 *            �����б�
	 * @return true�����ʽ��ȷ
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
	 * ���ݹؼ��֡�ҳ��ץȡ��ǰҳ���ڵ���������
	 * 
	 * @param keyword
	 *            �ؼ���
	 * @param pagenum
	 *            ҳ��
	 * @return ץȡ�������б�
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
	 * ���ݹؼ��֣�ץȡ�����������
	 * 
	 * @param keyword
	 *            �ؼ���
	 * @return ץȡ�������б�
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
	 * ���ݹؼ��֡�ҳ���ȡ�����б�<br/>
	 * ���󷽷���������Ҫʵ��ϸ��
	 * 
	 * @param keyword
	 *            �ؼ���
	 * @param pageorder
	 *            ҳ��
	 * @return �����б�
	 */
	public abstract ArrayList<Article> analyze(String keyword, int pageorder);

	public String formatKeyword(String keyword) {
		return keyword;
	}

	/**
	 * Ŀǰû��ҵ��ʹ�ô˷�����������getFormatKeywordList()������
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
	 * Ŀǰû��ҵ��ʹ�ô˷�����������getORList()������
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
	 * Ŀǰû��ҵ�����ʹ�ô˷�����������getFormatKeywordList()������
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
	 * ��ʽ���ؼ��֣�Ŀǰû��ҵ�����ʹ�ô˷���
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
	 * ������ʽ���ơ���ʽֵɸѡ���ӱ�ǩ���
	 * 
	 * @param parent
	 *            ����ǩ���
	 * @param classSimpleName
	 *            ��ʽ����
	 * @param classValue
	 *            ��ʽֵ
	 * @return �ӱ�ǩ���
	 */
	public TagNode getChildTagNode(TagNode parent, String classSimpleName,
			String classValue) {
		TagNode child = null;

		// ��ȡ�����ӽڵ�
		NodeList list = parent.getChildren();
		if (list == null)
			return null;

		String strClassName = null;
		for (int j = 0; j < list.size(); j++) {
			strClassName = list.elementAt(j).getClass().getSimpleName();

			// ɸѡ��ָ����ʽ�Ľ��
			if (strClassName.equalsIgnoreCase(classSimpleName)) {
				child = (TagNode) list.elementAt(j);

				if (classValue == null)
					return child;

				String st = child.getAttribute("class");

				// ɸѡ��ָ����ʽֵ�Ľ��
				if ((st != null) && (st.trim().equalsIgnoreCase(classValue))) {

					return child;
				}
			}
		}

		return null;
	}

	/**
	 * ������ʽ����ɸѡ���ӱ�ǩ���
	 * 
	 * @param parent
	 *            ����ǩ���
	 * @param classSimpleName
	 * @return �ӱ�ǩ���
	 */
	public TagNode getChildTagNode(TagNode parent, String classSimpleName) {
		TagNode child = null;

		// ��ȡ�����ӽ��
		NodeList list = parent.getChildren();
		if (list == null)
			return null;

		String strClassName = null;
		for (int j = 0; j < list.size(); j++) {
			strClassName = list.elementAt(j).getClass().getSimpleName();

			// ɸѡ��ָ����ʽ�Ľ��
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
	 * �����쳣��Ϣ
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
	 * �ж��Ƿ��ǺϷ�����
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

		// �ж��Ƿ�������ҳ��
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
