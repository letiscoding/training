package com.microservice.spider;

import com.microservice.data.Webpage;
import com.microservice.util.HttpAgent;
import com.microservice.util.RegexUtil;
import com.microservice.util.StringUtil;

public class HtmlAnalyzer {

	public static int ParseMethod_RemoveHTMLTag = 0;
	public static int ParseMethod_VIPS_DOM = 1;

	private SpiderConfig config;
	public static HtmlAnalyzer instance = null;

	public HtmlAnalyzer() {
		this.config = new SpiderConfig();
	}

	private HtmlAnalyzer(SpiderConfig config) {
		this.config = config;

		HttpAgent.NetConnectTimeout = this.config.getNetConnectTimeout();
		HttpAgent.NetReadTimeout = this.config.getNetReadTimeout();
	}

	public static HtmlAnalyzer getInstance(SpiderConfig config) {
		if (instance == null) {
			synchronized (HtmlAnalyzer.class) {
				if (instance == null) {
					instance = new HtmlAnalyzer(config);
				}
			}
		}

		return instance;
	}

	public Webpage getWebpage(String url) {
		return getWebpage(url, null, ParseMethod_RemoveHTMLTag, false);
	}

	public Webpage getWebpage(String url, String subject) {
		return getWebpage(url, subject, ParseMethod_RemoveHTMLTag, false);
	}

	public Webpage getWebpage(String url, String subject, boolean isJSEnabled) {
		return getWebpage(url, subject, ParseMethod_RemoveHTMLTag, isJSEnabled);
	}

	public Webpage getWebpage(String url, String subject, int parseMethod,
			boolean isJSEnabled) {
		Webpage webpage = new Webpage();

		if (config.isBlacksite(url))
			return null;

		HttpAgent httpAgent = new HttpAgent();
		httpAgent.setMaxLengthToParse(config.getMaxPageLength2Parse());
		if (config.getUserAgent() != null)
			httpAgent.setUserAgent(config.getUserAgent());

		if (config.getAllowedContentType() != null) {
			for (String allowedContentType : config.getAllowedContentType())
				httpAgent.addAllowedContentType(allowedContentType);
		}

		httpAgent.setAdditionalRequestHeader(config
				.getAdditionalRequestHeader());

		String defaultCharset = config.getSiteCharset(url);

		String[] charsetAndstrHtml = httpAgent.getWebpage(url, defaultCharset,
				isJSEnabled);

		if (charsetAndstrHtml == null)
			return null;

		if (charsetAndstrHtml[0] == null || charsetAndstrHtml[1] == null)
			return null;

		String strRegx = getContentRegx(url);
		if (strRegx != null) {
			webpage = getWebpageByRegx(charsetAndstrHtml[0],
					charsetAndstrHtml[1], strRegx);

			if (webpage != null) {
				webpage.setCharset(charsetAndstrHtml[0]);
				webpage.setHtmlSource(charsetAndstrHtml[1]);
				webpage.setHttpStatusCode(charsetAndstrHtml[2]);
				webpage.setUrl(charsetAndstrHtml[3]);
				webpage.setContentType(charsetAndstrHtml[4]);
				webpage.setTitle(TitleAnalyzer.getStandardTitle(TitleAnalyzer
						.getOriginTitle(charsetAndstrHtml[1])));

				return webpage;
			} else {
				System.out
						.println("Auto Parsing. ContentRegx Error=" + strRegx);
				webpage = new Webpage();
			}
		}

		webpage.setCharset(charsetAndstrHtml[0]);
		webpage.setHtmlSource(charsetAndstrHtml[1]);
		webpage.setHttpStatusCode(charsetAndstrHtml[2]);
		webpage.setUrl(charsetAndstrHtml[3]);
		webpage.setContentType(charsetAndstrHtml[4]);
		webpage.setTitle(TitleAnalyzer.getStandardTitle(TitleAnalyzer
				.getOriginTitle(charsetAndstrHtml[1])));

		String contentType = charsetAndstrHtml[4];
		if (contentType == null)
			contentType = "unknown";

		if (contentType.equalsIgnoreCase("application/msword")) {
			System.out.println("IS MS Word document, Not parse it.");
			webpage.setHtmlSource("");
			return webpage;
		} else if (contentType.equalsIgnoreCase("application/vnd.ms-excel")) {
			System.out.println("IS MS Excel document, Not parse it.");
			webpage.setHtmlSource("");
			return webpage;
		} else if (contentType.equalsIgnoreCase("application/pdf")) {
			System.out.println("IS PDF document, Not parse it.");
			webpage.setHtmlSource("");
			return webpage;
		} else {

			if (config.getAllowedContentType() != null)
				contentType = "text/";

			if (contentType.startsWith("text/")) {

				if (parseMethod == ParseMethod_VIPS_DOM) {

					webpage = HTMLExtractor.getWebpageByUrl(url,charsetAndstrHtml, config);
					if (webpage != null) {
						webpage.setContent(getCleanText(webpage.getContent()));
						webpage.setUrl(url);
					}
				} else {

					ContentParser pageParser = new ContentParser(subject,
							charsetAndstrHtml[1], this.config);
					webpage.setContent(pageParser.getCleanText());
					webpage.setPostTime(pageParser.getPublishedTime());
				}
			} else {
				System.out.println("Unknown contentType[" + contentType
						+ "], can't parse its content.");
				return webpage;
			}
		}

		return webpage;
	}

	private Webpage getWebpageByRegx(String charset, String strHtml,
			String strRegx) {
		Webpage webpage = null;

		if (strRegx == null)
			return null;

		String strTemp = null;
		try {
			strTemp = RegexUtil.getMatchedStr(strHtml, strRegx, null);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (strTemp != null)
			strHtml = strTemp;

		webpage = new Webpage();

		ContentParser pageParser = new ContentParser(null, strHtml, this.config);
		webpage.setContent(pageParser.getCleanText());
		webpage.setPostTime(pageParser.getPublishedTime());

		return webpage;

	}

	private String getContentRegx(String url) {
		String strRegx = null;

		if (this.config.getContentRegxMaps() == null)
			return null;

		String strHost = StringUtil.getHost(url);
		if (strHost != null) {
			strRegx = this.config.getContentRegxMaps().get(strHost);
			if (strRegx != null)
				return strRegx;

			String strDomain = StringUtil.getDomain(url);
			strRegx = this.config.getContentRegxMaps().get(strDomain);
		}

		return strRegx;
	}

	public static String getCleanText(String strDigest) {
		String invalidChars = "[┊｜▲★【】┆·|?+#><\\/\\-\\[\\]\\s]";

		int MinLengthParagraph = 1;

		String regChars = invalidChars;

		String digest = null;

		digest = strDigest.replaceAll("&#12288;", " ");
		digest = digest.replaceAll(regChars, " ");

		String[] splits = digest.split(" ");
		String strTemp = "";
		for (int i = 0; i < splits.length; i++) {
			String s = splits[i];
			if (s != null)
				s = s.trim();

			if (s.length() > MinLengthParagraph || s.equals("_"))
				strTemp += " " + s;
		}

		digest = strTemp.replaceAll("&gt;", "");

		return digest;
	}

	public static void main(String[] args) {

	}
}
