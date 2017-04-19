package com.microservice.spider;

import java.util.ArrayList;

import com.microservice.util.DateUtil;
import com.microservice.util.RegexUtil;

public class ContentParser {
	private SpiderConfig htmlAnalyzerConfig;
	private String subject;
	private String htmlSource;

	private static String MySpecifiedSplliter = "iiSPT";
	private static String MyCRLF = "myCRLF";

	private String strCleanHtml = null;

	public ContentParser(String subject, String strHtml,
			SpiderConfig htmlAnalyzerConfig) {
		this.subject = subject;
		this.htmlSource = strHtml;
		this.htmlAnalyzerConfig = htmlAnalyzerConfig;

		if (this.subject != null && this.subject.trim().length() > 0) {

			if (this.subject.endsWith("...")) {
				this.subject = this.subject.substring(0,
						this.subject.length() - 3).trim();

			}
		} else
			this.subject = null;

		this.strCleanHtml = getTextWithoutHtml();

	}

	public String getTextWithoutHtml() {
		String strTextWithoutHtml = this.htmlSource;

		String strBodyRegx = "((?i)<\\s*?body[\\s\\S]*?>)";
		String strBody = null;
		try {
			strBody = RegexUtil.getMatchedStr(strTextWithoutHtml, strBodyRegx,
					1);
			if (strBody != null) {
				int b1 = strTextWithoutHtml.indexOf(strBody);
				if (b1 > 0)
					strTextWithoutHtml = strTextWithoutHtml.substring(b1
							+ strBody.length());
			}
		} catch (Exception e) {
			;
		}

		strTextWithoutHtml = removeHtmlBlock(strTextWithoutHtml);

		strTextWithoutHtml = removeItemHtmlTags(strTextWithoutHtml);

		return strTextWithoutHtml;
	}

	public String getPublishedTime() {
		return getPublishedTime(null);
	}

	public String getPublishedTime(ArrayList<String> timeRegexList) {
		String strTime = null;

		if (this.strCleanHtml == null)
			return null;

		String str2Find = null;

		if (timeRegexList != null) {
			for (String strRegex : timeRegexList) {
				if (strRegex == null || strRegex.trim().length() == 0)
					continue;

				try {
					str2Find = RegexUtil.getMatchedStr(this.htmlSource,
							strRegex, 1);
				} catch (Exception e) {
					;
				}

				if (str2Find != null) {

					break;
				}
			}
		}

		if (str2Find == null) {
			str2Find = this.strCleanHtml;

			String strSubject = this.subject;
			if (strSubject != null) {
				strSubject = strSubject.toLowerCase();
				int p1 = this.strCleanHtml.indexOf(strSubject);
				if (p1 > 0)
					str2Find = this.strCleanHtml.substring(p1
							+ strSubject.length());
			}
		}

		strTime = DateUtil.getFormattedTime(str2Find);

		return strTime;
	}

	public String getCleanText() {
		if (this.strCleanHtml == null)
			return null;

		String strNoHtmlTags = this.strCleanHtml;

		String strSubject = this.subject;
		boolean hasFoundSubject = false;
		if (strSubject != null) {
			strSubject = strSubject.toLowerCase();
			int p1 = strNoHtmlTags.indexOf(strSubject);
			if (p1 > 0) {
				strNoHtmlTags = strNoHtmlTags.substring(p1
						+ strSubject.length());
				hasFoundSubject = true;

			}
		}

		int minBlockLength = 2;
		if (hasFoundSubject)
			minBlockLength = 1;

		boolean findStartPos = false;
		String strTemp = null;

		StringBuffer bufContent = new StringBuffer();
		String[] cntBlocks = strNoHtmlTags.split(MySpecifiedSplliter + "|　");
		for (int i = 0; i < cntBlocks.length; i++) {
			String strBlock = cntBlocks[i].trim();

			if (strBlock.equals(MyCRLF)) {
				bufContent.append(strBlock + " ");
				continue;
			}

			String strValidBlock = strBlock;
			if (htmlAnalyzerConfig.isChinesePageModel())
				strValidBlock = strBlock.replaceAll("[^\u4e00-\u9fa5]", "");

			if (strValidBlock.length() >= minBlockLength) {

				strBlock = strBlock.replaceAll(MyCRLF, "\n");
				strBlock = strBlock.replaceAll("[\\n]{2,}", "\n");

				if (strBlock.length() <= 10) {
					if (strBlock
							.matches(".*?(网|刊|社区|频道|论坛|博客|报社|报|报纸|版|版面|客户端|导航|网站|文章|资讯|博文|帖子|新闻|下载)")) {

						continue;
					}
				}

				if (strBlock.length() <= 5) {
					if (strTemp == null)
						strTemp = strBlock;
					else
						strTemp += " " + strBlock;

					continue;
				}

				if (strTemp != null) {

					if (!findStartPos && isContentStartBlock(strTemp)) {
						findStartPos = true;
						bufContent.delete(0, bufContent.length());

					} else if (isContentEndBlock(strTemp)) {

						break;
					} else if (HTMLExtractor.isInvalidNode(strTemp,
							htmlAnalyzerConfig)) {

					} else {
						bufContent.append(strTemp + " ");
					}

					strTemp = null;
				}

				if (!findStartPos && isContentStartBlock(strBlock)) {
					findStartPos = true;
					bufContent.delete(0, bufContent.length());

					continue;
				}

				if (isContentEndBlock(strBlock)) {

					break;
				} else if (HTMLExtractor.isInvalidNode(strBlock,
						htmlAnalyzerConfig)) {

					continue;
				}

				bufContent.append(strBlock + " ");
			}
		}

		String strContent = bufContent.toString();

		strContent = strContent.replaceAll(MyCRLF, "\n");
		strContent = strContent.replaceAll(" *?[\\n]{1,} *?", "\n");
		strContent = strContent.replaceAll("[\\n]{2,}", "\n").trim();

		return strContent;

	}

	public boolean isContentStartBlock(String content) {
		boolean isDeclared = false;

		if (content == null || content.length() < 1)
			return false;

		ArrayList<String> contentStartTags = htmlAnalyzerConfig
				.getContentStartTags();
		if (contentStartTags == null || contentStartTags.size() < 1)
			return false;

		for (int i = 0; i < contentStartTags.size(); i++) {
			String tk = contentStartTags.get(i);

			if (content.matches(tk)) {
				isDeclared = true;

				break;
			}
		}

		return isDeclared;
	}

	public boolean isContentEndBlock(String content) {
		boolean isDeclared = false;

		if (content == null || content.length() < 1)
			return false;

		ArrayList<String> contentEndTags = htmlAnalyzerConfig
				.getContentEndTags();
		if (contentEndTags == null || contentEndTags.size() < 1)
			return false;

		for (int i = 0; i < contentEndTags.size(); i++) {
			String tk = contentEndTags.get(i);

			if (content.matches(tk)) {
				isDeclared = true;

				break;
			}
		}

		return isDeclared;
	}

	public String removeItemHtmlTags(String strHtml) {

		String content = strHtml;

		if (content == null)
			return null;

		if (!htmlAnalyzerConfig.includeLinkText())
			content = content.replaceAll(
					"(?i)<\\s*?a\\s+?[^>]*?>[\\s\\S]*?<\\s*?/\\s*?a\\s*?>",
					MySpecifiedSplliter);

		content = content.replaceAll("(?i)<\\s*?br\\s*?(/|)\\s*?>", MyCRLF
				+ MySpecifiedSplliter);
		content = content.replaceAll("(?i)<\\s*?p\\s*?(/|)\\s*?>", MyCRLF
				+ MySpecifiedSplliter);
		content = content.replaceAll("(?i)<\\s*?(/|)\\s*?p\\s*?>", MyCRLF
				+ MySpecifiedSplliter);
		content = content.replaceAll("(\\r|\\n)", MyCRLF + MySpecifiedSplliter);

		content = content.replaceAll("<[\\s\\S]*?>", MySpecifiedSplliter);

		content = content.replaceAll("[\\s|　]+", " ");

		content = processEscapedChars(content);

		content = content.trim();

		return content;
	}

	public static String processEscapedChars(String str2beTransferred) {
		String textBeTransferred = str2beTransferred;

		textBeTransferred = textBeTransferred.replaceAll("&lt;", "<");
		textBeTransferred = textBeTransferred.replaceAll("&gt;", ">");
		textBeTransferred = textBeTransferred.replaceAll("&quot;", "\"");
		textBeTransferred = textBeTransferred.replaceAll("&yen;", "￥");
		textBeTransferred = textBeTransferred.replaceAll("&ldquo;", "“");
		textBeTransferred = textBeTransferred.replaceAll("&rdquo;", "”");

		textBeTransferred = textBeTransferred.replaceAll("&#12288;", "");

		textBeTransferred = textBeTransferred.replaceAll("&[^#][\\S]+?;", "");

		return textBeTransferred;
	}

	private String removeHtmlBlock(String strPage) {
		String strRemoved = strPage;

		if (strPage == null)
			return null;

		strRemoved = strRemoved.replaceAll("<!--[\\s\\S]*?-->",
				MySpecifiedSplliter);

		strRemoved = strRemoved
				.replaceAll(
						"(?i)<\\s*?script[\\s\\S]*?>[\\s\\S]*?<\\s*?/\\s*?script\\s*?>",
						MySpecifiedSplliter);
		strRemoved = strRemoved
				.replaceAll(
						"(?i)<\\s*?style[\\s\\S]*?>[\\s\\S]*?<[\\s\\S]*?/\\s*?style\\s*?>",
						MySpecifiedSplliter);

		strRemoved = strRemoved.replaceAll(
				"(?i)<object[\\s\\S]*?>[\\s\\S]*?<\\s*?/\\s*?object\\s*?>",
				MySpecifiedSplliter);

		strRemoved = strRemoved.replaceAll(
				"(?i)<select[\\s\\S]*?>[\\s\\S]*?<\\s*?/\\s*?select\\s*?>",
				MySpecifiedSplliter);

		return strRemoved;
	}

	public static void main(String[] args) {

		String subject = "贵州证实fef安顺市长涉嫌违纪 一r./456,=-报社记者牵涉其中";
		String content = "<div id=\"Article\">哈哈哈<BODY >"
				+ "<img src=\"dfdf20121231\"><h1 >贵州证实安顺市长涉嫌违纪 一报社记者牵涉其中 <br />"
				+ "< a sdfd df d javacsriot:=\"2011-12-01 aa.htm\">AAAAAAAAAAAAAA</a><span >&nbsp;>2013-07月03 &nbsp; 19时04分</span&nbsp;&nbsp;&nbsp;来源：&nbsp;&nbsp;&nbsp;来源：中国新闻网&nbsp;&nbsp;&nbsp;评论：(<a href=\"#comment_iframe\" id=\"comment\">0</a>)条评论 </span></h1>"
				+ "<div class=\"content\">"
				+ "<p>　　前晚，由贵州日报报业集团主办的金黔在线网站发布消息称，记者从贵州省纪委获悉，安顺市市长王术君涉嫌违纪，目前正接受组织调查，网上举报人陆炫杰也正在配合调查";

		SpiderConfig htmlAnalyzerConfig = new SpiderConfig();
		htmlAnalyzerConfig.setNetConnectTimeout(100000);
		htmlAnalyzerConfig.setNetReadTimeout(100000);
		htmlAnalyzerConfig.setIncludeLinkText(false);
		ContentParser parser = new ContentParser(subject, content,
				htmlAnalyzerConfig);

		System.out.println(parser.strCleanHtml);
		System.out.println(parser.getPublishedTime());
		System.out.println(parser.getCleanText());

		System.out.println("Not中文:"
				+ subject.replaceAll("[^a-zA-Z\u4e00-\u9fa5]", ""));

	}
}