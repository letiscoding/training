package com.microservice.spider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import com.microservice.util.StringUtil;

public class SpiderConfig {

	private boolean isChinesePage = true;

	private String blackWebsiteDicUrl = "http://cs.chinaii.cn/system/IIMS_Black_Website.jsp";
	private ConcurrentHashMap<String, String> blackWebsiteMaps;

	private String sysContentRegxDicUrl = "http://cs.chinaii.cn/system/IIMS_ContentRegx_Dic.jsp";
	private ConcurrentHashMap<String, String> contentRegxMaps;

	private String siteCharsetDicUrl = "http://cs.chinaii.cn/system/IIMS_Website_Charset.jsp";
	private ConcurrentHashMap<String, String> siteCharsetMaps;

	private String UserAgent = null;
	private ArrayList<String> AllowedContentType = new ArrayList<String>();

	private ArrayList<String> additionalRequestHeaders = new ArrayList<String>();

	private ArrayList<String> contentStartTags;

	private ArrayList<String> contentEndTags;

	private ArrayList<String> invalidTags;

	private int NetConnectTimeout = 10000;
	private int NetReadTimeout = 10000;

	private int MaxPageLength2Parse = 1024 * 1024;

	private boolean includeLinkText = false;

	public SpiderConfig() {

		setSiteCharsetDicFile(null);

		setBlackWebsiteDic();
	}

	public boolean isBlacksite(String url) {
		boolean isBlack = false;

		if ((url == null) || (url.length() < 1))
			return true;

		String strHost = StringUtil.getHost(url);
		if (blackWebsiteMaps.get(strHost) != null)
			return true;

		String strDomain = StringUtil.getDomain(url);
		if (blackWebsiteMaps.get(strDomain) != null)
			return true;

		return isBlack;
	}

	public void setAsChinesePage(boolean isChinesePage) {
		this.isChinesePage = isChinesePage;
	}

	public boolean isChinesePageModel() {
		return this.isChinesePage;
	}

	public String getUserAgent() {
		return UserAgent;
	}

	public void setUserAgent(String userAgent) {
		UserAgent = userAgent;
	}

	public ArrayList<String> getAllowedContentType() {
		return AllowedContentType;
	}

	public void setAllowedContentType(ArrayList<String> allowedContentType) {
		AllowedContentType = allowedContentType;
	}

	public void addAllowedContentType(String allowedContentType) {
		if (allowedContentType != null && allowedContentType.length() > 1)
			AllowedContentType.add(allowedContentType);
	}

	public void setAdditionalRequestHeader(String headName, String value) {
		if (headName != null && headName.length() > 1 && value != null)
			this.additionalRequestHeaders.add(headName + ":" + value);
	}

	public ArrayList<String> getAdditionalRequestHeader() {
		return additionalRequestHeaders;
	}

	public boolean setBlackWebsiteDic() {
		this.blackWebsiteMaps = new ConcurrentHashMap<String, String>();

		StringBuffer blackWebsiteDicBuf = new StringBuffer(1024);

		try {
			URL url = new URL(blackWebsiteDicUrl);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));

			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				blackWebsiteDicBuf.append(inputLine);
				blackWebsiteDicBuf.append("\n");
			}

			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		String blackWebsiteDic[] = blackWebsiteDicBuf.toString().split("\\\n");
		for (int i = 0; i < blackWebsiteDic.length; i++) {
			if (blackWebsiteDic[i] == null)
				continue;

			blackWebsiteDic[i] = blackWebsiteDic[i].trim();

			if (blackWebsiteDic[i].length() < 1)
				continue;

			if (blackWebsiteDic[i].startsWith("#"))
				continue;

			this.blackWebsiteMaps.put(blackWebsiteDic[i], " ");
		}

		return true;
	}

	public boolean setContentRegxDicFile(String contentRegxDicFile) {
		this.contentRegxMaps = new ConcurrentHashMap<String, String>();

		StringBuffer contentRegexDicBuf = new StringBuffer(1024);

		if (contentRegxDicFile != null) {

			try {
				File file = new File(contentRegxDicFile);
				if (file.exists()) {
					BufferedReader br = new BufferedReader(new FileReader(file));
					String content = null;
					while ((content = br.readLine()) != null) {
						contentRegexDicBuf.append(content.trim());
						contentRegexDicBuf.append("\n");
					}
					br.close();
				}
			} catch (Exception e) {
				;
			}
		}

		try {
			URL url = new URL(sysContentRegxDicUrl);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(
					connection.getInputStream()));

			String inputLine;
			while ((inputLine = in.readLine()) != null) {
				contentRegexDicBuf.append(inputLine);
				contentRegexDicBuf.append("\n");
			}

			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		String contentRegexDic[] = contentRegexDicBuf.toString().split("\\\n");
		for (int i = 0; i < contentRegexDic.length; i++) {
			if (contentRegexDic[i] == null)
				continue;

			contentRegexDic[i] = contentRegexDic[i].trim();

			if (contentRegexDic[i].length() < 1)
				continue;

			if (contentRegexDic[i].startsWith("#"))
				continue;

			int p = contentRegexDic[i].indexOf("\t");
			if (p == -1)
				p = contentRegexDic[i].indexOf(" ");

			if (p > 0) {
				String host = contentRegexDic[i].substring(0, p);
				String contentRegex = contentRegexDic[i].substring(p + 1);

				if (host == null || contentRegex == null)
					continue;

				host = host.trim();
				if (host.length() < 1)
					continue;

				contentRegex = contentRegex.trim();
				if (contentRegex.length() < 1)
					continue;

				this.contentRegxMaps.put(host, contentRegex);
			}
		}

		return true;
	}

	public boolean setContentStartRegxTags(String contentStartRegxTagsFile) {
		boolean isOK = false;

		if (contentStartRegxTagsFile == null) {
			System.out.println("contentStartRegxTagsFile is null.");
			return false;
		}

		contentStartTags = new ArrayList<String>();

		try {
			File file = new File(contentStartRegxTagsFile);
			if (!file.exists() || file.isDirectory()) {
				System.out.println("contentStartRegxTagsFile="
						+ contentStartRegxTagsFile
						+ " NOT exists or a DIRECTORY.");
				return false;
			}

			BufferedReader br = new BufferedReader(new FileReader(file));
			String temp = null;
			while ((temp = br.readLine()) != null) {
				temp = temp.trim();

				if (temp.length() < 1)
					continue;

				if (temp.startsWith("#"))
					continue;

				contentStartTags.add(temp);
			}
			br.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return isOK;
	}

	public boolean setContentEndRegxTags(String contentEndtRegxTagsFile) {
		boolean isOK = false;

		if (contentEndtRegxTagsFile == null) {
			System.out.println("contentEndtRegxTagsFile is null.");
			return false;
		}

		contentEndTags = new ArrayList<String>();

		try {
			File file = new File(contentEndtRegxTagsFile);
			if (!file.exists() || file.isDirectory()) {
				System.out.println("contentEndtRegxTagsFile="
						+ contentEndtRegxTagsFile
						+ " NOT exists or a DIRECTORY.");
				return false;
			}

			BufferedReader br = new BufferedReader(new FileReader(file));
			String temp = null;
			while ((temp = br.readLine()) != null) {
				temp = temp.trim();

				if (temp.length() < 1)
					continue;

				if (temp.startsWith("#"))
					continue;

				contentEndTags.add(temp);
			}
			br.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return isOK;
	}

	public boolean setInvalidNodeDicFile(String invalidNodeDicFile) {
		boolean isOK = false;

		if (invalidNodeDicFile == null) {
			System.out.println("invalidNodeDicFile is null.");
			return false;
		}

		invalidTags = new ArrayList<String>();
		System.out.println("Read the invalidNode Dic...");
		try {
			File file = new File(invalidNodeDicFile);
			if (!file.exists() || file.isDirectory())
				throw new FileNotFoundException();

			BufferedReader br = new BufferedReader(new FileReader(file));
			String temp = null;
			while ((temp = br.readLine()) != null) {
				temp = temp.trim();

				if (temp.length() < 1)
					continue;

				if (temp.startsWith("#"))
					continue;

				System.out.println("\tInvalidNode tag=" + temp);
				invalidTags.add(temp);
			}
			br.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return isOK;
	}

	public ConcurrentHashMap<String, String> getContentRegxMaps() {
		return contentRegxMaps;
	}

	public ArrayList<String> getContentStartTags() {
		return contentStartTags;
	}

	public ArrayList<String> getContentEndTags() {
		return contentEndTags;
	}

	public ArrayList<String> getInvalidTags() {
		return invalidTags;
	}

	public void setNetConnectTimeout(int netConnectTimeout) {
		NetConnectTimeout = netConnectTimeout;
	}

	public int getNetConnectTimeout() {
		return NetConnectTimeout;
	}

	public void setMaxPageLength2Parse(int maxPageLength2Parse) {
		this.MaxPageLength2Parse = maxPageLength2Parse;
	}

	public int getMaxPageLength2Parse() {
		return MaxPageLength2Parse;
	}

	public void setNetReadTimeout(int netReadTimeout) {
		NetReadTimeout = netReadTimeout;
	}

	public int getNetReadTimeout() {
		return NetReadTimeout;
	}

	public boolean setSiteCharsetDicFile(String siteCharsetDicFile) {
		this.siteCharsetMaps = new ConcurrentHashMap<String, String>();

		StringBuffer siteCharsetDicBuf = new StringBuffer(1024);

		if (siteCharsetDicFile == null) {
			try {
				URL url = new URL(siteCharsetDicUrl);
				HttpURLConnection connection = (HttpURLConnection) url
						.openConnection();
				BufferedReader in = new BufferedReader(new InputStreamReader(
						connection.getInputStream()));

				String inputLine;
				while ((inputLine = in.readLine()) != null)
					siteCharsetDicBuf.append(inputLine);

				in.close();
			} catch (Exception e) {
				;
			}
		} else {

			try {
				File file = new File(siteCharsetDicFile);
				if (!file.exists() || file.isDirectory()) {
					System.out.println("siteCharsetDicFile="
							+ siteCharsetDicFile
							+ " NOT exists or a DIRECTORY.");
					return false;
				}

				BufferedReader br = new BufferedReader(new FileReader(file));
				String content = null;
				while ((content = br.readLine()) != null) {
					siteCharsetDicBuf.append(content.trim());
					siteCharsetDicBuf.append("\n");
				}
				br.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		String siteCharsetDic[] = siteCharsetDicBuf.toString().split("\\\n");
		for (int i = 0; i < siteCharsetDic.length; i++) {
			if (siteCharsetDic[i] == null || siteCharsetDic[i].length() < 1)
				continue;

			if (siteCharsetDic[i].startsWith("#"))
				continue;

			int p = siteCharsetDic[i].indexOf("\t");
			if (p == -1)
				p = siteCharsetDic[i].indexOf(" ");

			if (p > 0) {
				String host = siteCharsetDic[i].substring(0, p);
				String charset = siteCharsetDic[i].substring(p + 1);

				if (host == null || charset == null)
					continue;

				host = host.trim();
				if (host.length() < 1)
					continue;

				charset = charset.trim();
				if (charset.length() < 1)
					continue;

				this.siteCharsetMaps.put(host, charset);
			}
		}

		return true;
	}

	public String getSiteCharset(String url) {
		if (url == null)
			return null;

		String host = StringUtil.getHost(url);
		String charset = siteCharsetMaps.get(host);
		if (charset != null)
			return charset;

		String domain = StringUtil.getDomain(url);
		charset = siteCharsetMaps.get(domain);

		return charset;
	}

	public boolean includeLinkText() {
		return includeLinkText;
	}

	public void setIncludeLinkText(boolean includeLinkText) {
		this.includeLinkText = includeLinkText;
	}

	public static void main(String[] args) {
		SpiderConfig config = new SpiderConfig();

		System.out
				.println(config
						.getSiteCharset("http://news.stheadline.com/dailynews/content_hk/2014/05/06/284564.asp"));
	}

}