package com.microservice.util;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.ImmediateRefreshHandler;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.microservice.browser.EngineBrowser;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.GzipDecompressingEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class HttpAgent {
	private CloseableHttpClient defaultHttpClient = HttpClients.createDefault();

	private static final int MAX_REFRESH_PAGE_LENGTH = 2048;
	private static final int MAX_Redirect_Times = 3;

	public static int NetConnectTimeout = 100000;
	public static int NetReadTimeout = 100000;

	private int MaxLengthToParse = 1024 * 1024;

	private ArrayList<String> allowedContentTypes = new ArrayList<String>();

	private ArrayList<String> additionalRequestHeaders = new ArrayList<String>();

	private String userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/27.0.1453.116 Safari/537.36";

	public HttpAgent() {
		allowedContentTypes.add("text/");
		allowedContentTypes.add("application/xhtml+xml");
	}

	public ArrayList<String> getAdditionalRequestHeaders() {
		return additionalRequestHeaders;
	}

	public void setAdditionalRequestHeader(
			ArrayList<String> additionalRequestHeaders) {
		this.additionalRequestHeaders = additionalRequestHeaders;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		if (userAgent != null && userAgent.length() > 10)
			this.userAgent = userAgent;
	}

	public void setMaxLengthToParse(int maxLength) {
		this.MaxLengthToParse = maxLength;
	}

	public void addAllowedContentType(String allowedContentType) {
		if (allowedContentType != null && allowedContentType.length() > 1)
			allowedContentTypes.add(allowedContentType);
	}

	public static String encodeUrl(String url) {
		if (url == null)
			return null;

		url = url.trim();

		url = url.replaceAll("&amp;", "&");
		url = url.replaceAll("\\{", "%7B");
		url = url.replaceAll("\\}", "%7D");
		url = url.replaceAll("\\[", "%5B");
		url = url.replaceAll("\\]", "%5D");
		url = url.replaceAll("\\|", "%7C");

		url = url.replaceAll("\\\\", "/");

		if (!url.matches("[\\u4e00-\\u9fa5\\uF900-\\uFA2D]")) {
			StringBuffer bf = new StringBuffer();
			char[] array = url.toCharArray();
			for (Character c : array) {
				if (c.toString().matches("[\\u4e00-\\u9fa5\\uF900-\\uFA2D]")) {
					try {
						bf.append(URLEncoder.encode(c.toString(), "utf-8"));
					} catch (Exception e) {
						;
					}
				} else {
					bf.append(c);
				}
			}

			url = bf.toString();
		}

		url = url.replace(" ", "%20");

		return url;
	}

	public String[] getWebpage(String url) {
		return getWebpage(url, null, false);
	}

	public String[] getWebpage(String url, String defaultCharset) {
		return getWebpage(url, defaultCharset, false);
	}

	public String[] getWebpage(String url, String defaultCharset,
			boolean isJSEnabled) {
		String html = null;
		String charset = null;
		String autocharset = null;
		String headercharset = null;
		String metacharset = null;

		String contentType = null;

		if (url == null)
			return null;

		if (isJSEnabled)
			return getPageSourceByHtmlUnit(url, defaultCharset);

		String strRefreshedUrl = url;
		String httpStatusCode = "200";

		try {

			url = encodeUrl(url);

			final ExecutorService executor = Executors
					.newSingleThreadExecutor();

			UrlCrawlerThread netThread = new UrlCrawlerThread(url);
			Future<Object> taskFuture = executor.submit(netThread);

			int MaxWaitTime = 2 * (HttpAgent.NetConnectTimeout + HttpAgent.NetReadTimeout);

			byte[][] htmlAndContentType = null;

			try {

				htmlAndContentType = (byte[][]) taskFuture.get(MaxWaitTime,
						TimeUnit.MILLISECONDS);
			} catch (Exception e) {
				System.out
						.println("[httpAgent.getWebpage]Error while get the url."
								+ e + url);
			} finally {
				taskFuture.cancel(true);
				executor.shutdownNow();
			}

			if (htmlAndContentType == null)
				return null;

			if (htmlAndContentType[2] == null || htmlAndContentType[3] == null)
				return null;

			httpStatusCode = new String(htmlAndContentType[2]);
			strRefreshedUrl = new String(htmlAndContentType[3]);

			if (htmlAndContentType[0] == null || htmlAndContentType[1] == null)
				return new String[] { null, null, httpStatusCode,
						strRefreshedUrl, null };

			if (defaultCharset == null) {

				try {
					WebpageCharsetDetector charsetDetector = new com.microservice.util.WebpageCharsetDetector();
					autocharset = charsetDetector
							.getDetectedCharset(htmlAndContentType[0]);
					if (autocharset != null) {
						autocharset = autocharset.toUpperCase();
						autocharset = autocharset.replace("-", "");
					}

				} catch (Exception e) {
					e.printStackTrace();
				}

				if (htmlAndContentType[1] != null) {

					contentType = new String(htmlAndContentType[1]);

					String tmpcontentType = contentType.toLowerCase();

					int p1 = tmpcontentType.indexOf(";");
					if (p1 > 0) {

						int p2 = tmpcontentType.indexOf("charset=");
						if (p2 > 0) {
							headercharset = tmpcontentType.substring(p2 + 8)
									.trim();
							headercharset = headercharset.replaceAll("\"", "");
							headercharset = headercharset.replaceAll(";", "");

							headercharset = headercharset.toUpperCase();
							headercharset = headercharset.replace("-", "");

						}
					}
				}

				if ((autocharset != null)
						&& ((autocharset.equalsIgnoreCase("UTF8") == true)
								|| (autocharset.equalsIgnoreCase("GB2312") == true)
								|| (autocharset.equalsIgnoreCase("GB18030") == true) || (autocharset
								.equalsIgnoreCase("GBK") == true))) {
					charset = autocharset;
				} else {
					if (headercharset != null) {
						charset = headercharset;
					} else {

						if (htmlAndContentType[0] != null) {
							metacharset = EncodeConvertor
									.getHtmlCharset(htmlAndContentType[0]);
							metacharset = metacharset.toUpperCase();
							metacharset = metacharset.replace("-", "");
							charset = metacharset;

						}
					}
				}

				if ((headercharset == null) && (metacharset == null)) {
					charset = autocharset;
				}

			} else {
				charset = defaultCharset;

			}

			if (charset == null)
				return null;

			try {
				new String("charset".getBytes(), charset);
			} catch (Exception e) {
				System.out.println("ERROR: illeagal Charset.=" + charset);
				return null;
			}

			html = EncodeConvertor.codeConvert(htmlAndContentType[0], charset);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return new String[] { charset, html, "200", strRefreshedUrl,
				contentType };
	}

	public String[] getPageSourceByHtmlUnit(String url, String defaultCharset) {
		String sourceXML = null;

		String charset = "utf-8";

		try {
			final WebClient webClient = new WebClient(BrowserVersion.FIREFOX_24);

			webClient.getOptions().setRedirectEnabled(true);
			webClient.getOptions().setActiveXNative(true);
			webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
			webClient.getOptions().setThrowExceptionOnScriptError(false);

			ImmediateRefreshHandler imRefreshHadler = new ImmediateRefreshHandler();

			webClient.setRefreshHandler(imRefreshHadler);

			webClient.getOptions().setJavaScriptEnabled(true);
			webClient.getOptions().setTimeout(10000);
			webClient.addRequestHeader("User-Agent", userAgent);

			webClient.getCookieManager().setCookiesEnabled(true);

			final HtmlPage page = webClient.getPage(url);
			if (page == null)
				return null;

			sourceXML = page.asXml();

			String regEx_script = "<[\\s]*?script[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?script[\\s]*?>";

			java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
					regEx_script, java.util.regex.Pattern.CASE_INSENSITIVE);
			java.util.regex.Matcher matcher = pattern.matcher(sourceXML);
			sourceXML = matcher.replaceAll("");

			if (sourceXML != null) {

				charset = EncodeConvertor.getCharsetFromPageSource(sourceXML);

			}
		} catch (Exception e) {
			;
		}

		return new String[] { charset, sourceXML, "200", url, "text/html" };
	}

	public byte[][] getPage(String strUrl) {

		byte[][] htmlAndContentType = new byte[][] { null, null,
				"400".getBytes(), strUrl.getBytes() };
		;

		try {
			htmlAndContentType = getPageGet(strUrl, MAX_Redirect_Times);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return htmlAndContentType;
	}

	public byte[][] postPage(String strUrl) {

		byte[][] htmlAndContentType = null;

		String hostUrl = StringUtil.getHost(strUrl);

		if (hostUrl == null)
			return new byte[][] { null, null, "400".getBytes() };

		int posWenhao = strUrl.indexOf("?");
		String postUrl = strUrl.substring(0, posWenhao);
		String parm = strUrl.substring(posWenhao + 1);
		String[] s = parm.split("&");

		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		for (int i = 0; i < s.length; i++) {
			String[] s1 = s[i].split("=");
			if (s1.length != 2)
				continue;

			params.add(new BasicNameValuePair(s1[0], s1[1]));
		}

		HttpPost httpost = new HttpPost(postUrl);
		HttpResponse response;

		try {
			httpost.setEntity(new UrlEncodedFormEntity(params));

			httpost.setHeader("User-Agent",
					"Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.1.2)");

			httpost.setHeader("Accept-Language", "zh-cn,zh;q=0.5");
			httpost.setHeader("Accept-Charset", "GB2312,utf-8;q=0.7,*;q=0.7");
			httpost.setHeader("Accept",
					"text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			httpost.setHeader("Host", hostUrl);

			response = defaultHttpClient.execute(httpost);

			htmlAndContentType = getResponse(postUrl, response, 1);

		} catch (Exception e) {
			e.printStackTrace();
			return new byte[][] { null, null, "400".getBytes(),
					strUrl.getBytes() };
		} finally {
			httpost.abort();
		}

		return htmlAndContentType;
	}

	private synchronized byte[][] getPageGet(String strUrl, int refreshTimes) {

		byte[][] htmlAndContentType = null;

		String hostUrl = StringUtil.getHost(strUrl);
		if (hostUrl == null)
			return new byte[][] { null, null, "400".getBytes(),
					strUrl.getBytes() };

		HttpGet httpget = null;

		try {
			httpget = new HttpGet(strUrl);

			httpget.setHeader("User-Agent", userAgent);

			httpget.setHeader("Accept-Language", "zh-CN,zh;q=0.8");
			httpget.setHeader("Accept-Charset", "GB2312,utf-8;q=0.7,*;q=0.7");
			httpget.setHeader("Accept",
					"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
			httpget.setHeader("Accept-Encoding", "gzip, deflate, sdch");

			httpget.setHeader("Host", hostUrl);
			httpget.setHeader("Referer", strUrl);
			httpget.setHeader("Connection", "keep-alive");

			for (String additionHeader : additionalRequestHeaders) {
				int p = additionHeader.indexOf(":");
				if (p > 0) {
					String strHead = additionHeader.substring(0, p);
					String strValue = additionHeader.substring(p + 1);

					httpget.setHeader(strHead, strValue);
				}
			}

			RequestConfig requestConfig = RequestConfig.custom()
					.setConnectTimeout(NetConnectTimeout)
					.setRedirectsEnabled(false)
					.setCookieSpec(CookieSpecs.BEST_MATCH).build();

			httpget.setConfig(requestConfig);

			HttpResponse response = defaultHttpClient.execute(httpget);

			htmlAndContentType = getResponse(strUrl, response, refreshTimes);

		} catch (Exception e) {
			System.out.println("Network error." + e);
			return new byte[][] { null, null, "400".getBytes(),
					strUrl.getBytes() };
		} finally {
			if (httpget != null)
				httpget.abort();
		}

		return htmlAndContentType;
	}

	public static String getRefreshUrl(String strContent) {
		String strRefreshUrl = null;

		if (strContent == null)
			return null;

		if (strContent.length() > MAX_REFRESH_PAGE_LENGTH)
			return null;

		String frontContent = strContent;
		if (strContent.length() > 1024)
			frontContent = strContent.substring(0, 1024);

		int r1 = frontContent.indexOf("location.replace");
		if (r1 > 0) {
			int r11 = frontContent.indexOf("(", r1);
			int r12 = frontContent.indexOf(")", r11);
			if (r11 > 0 && r12 > r11) {
				strRefreshUrl = frontContent.substring(r11 + 1, r12).trim();

				if ((strRefreshUrl.startsWith("\"") || strRefreshUrl
						.startsWith("'"))
						&& (strRefreshUrl.endsWith("\"") || strRefreshUrl
								.endsWith("'")))
					strRefreshUrl = strRefreshUrl.substring(1,
							strRefreshUrl.length() - 1);
			}

			if (strRefreshUrl != null)
				return strRefreshUrl;
		}

		int r2 = frontContent.indexOf("window.location");
		int r3 = frontContent.indexOf("location.href");
		int r4 = frontContent.indexOf("location");
		if (r3 > 0 || r2 > 0 || r4 > 0) {
			int pstart = (r2 > r3 ? r2 : r3);
			pstart = (pstart > r4 ? pstart : r4);

			int r31 = frontContent.indexOf("=", pstart);
			if (r31 > 0) {
				strRefreshUrl = frontContent.substring(r31 + 1);

				int sp1 = strRefreshUrl.indexOf("'");
				if (sp1 == -1)
					sp1 = strRefreshUrl.indexOf("\"");

				if (sp1 >= 0)
					strRefreshUrl = strRefreshUrl.substring(sp1 + 1);

				int r32 = strRefreshUrl.indexOf("'");
				if (r32 == -1)
					r32 = strRefreshUrl.indexOf("\"");

				if (r32 > 0)
					strRefreshUrl = strRefreshUrl.substring(0, r32).trim();
			}

			if (strRefreshUrl != null)
				return strRefreshUrl;
		}

		String strRefreshRegx = "(?i)<\\s*?meta\\s*?http-equiv=[\"]*?refresh[\"]*?.+?url\\s*?=([\\s\\S]*?)[\"]*?\\s*?[/]*?\\s*?>";
		try {
			strRefreshUrl = RegexUtil.getMatchedStr(frontContent,
					strRefreshRegx, 1);
		} catch (Exception e) {
			;
		}

		if (strRefreshUrl == null)
			return null;

		strRefreshUrl = strRefreshUrl.trim();
		if (strRefreshUrl.startsWith("'"))
			strRefreshUrl = strRefreshUrl.substring(1);

		if (strRefreshUrl.endsWith("'"))
			strRefreshUrl = strRefreshUrl.substring(0,
					strRefreshUrl.length() - 1);

		return strRefreshUrl;
	}

	private byte[][] getResponse(String strUrl, HttpResponse response,
			int refreshTimes) {

		byte[][] htmlAndContentType = null;
		String contentType = "";

		boolean isAllowedContentType = false;

		boolean isXCacheError = false;

		Header headers[] = response.getAllHeaders();
		int ii = 0;
		while (ii < headers.length) {

			if (headers[ii].getName().equalsIgnoreCase("X-Cache")) {
				if (headers[ii].getValue().equalsIgnoreCase("Error"))
					isXCacheError = true;
			}

			if (headers[ii].getName().equalsIgnoreCase("Content-Type")) {
				contentType = headers[ii].getValue();

				if (!isAllowedContentType) {
					for (int i = 0; i < allowedContentTypes.size(); i++) {
						if (contentType.startsWith(allowedContentTypes.get(i))) {
							isAllowedContentType = true;
							break;
						}
					}
				}
			}

			if (headers[ii].getName().equalsIgnoreCase("Content-Length")) {
				String strLength = headers[ii].getValue();
				try {
					if (strLength != null) {
						int len = Integer.parseInt(strLength);
						if (len > this.MaxLengthToParse) {
							System.out.println("It's too long(longger than "
									+ this.MaxLengthToParse + ").ignore it."
									+ len + "\t" + strUrl);
							return new byte[][] { null, contentType.getBytes(),
									"200".getBytes(), strUrl.getBytes() };
						}
					}
				} catch (Exception e) {
					;
				}
			}

			ii++;
		}

		if (!isAllowedContentType) {
			System.out.println(contentType + " is not Allowed, ignore it."
					+ strUrl);
			return new byte[][] { null, contentType.getBytes(),
					"200".getBytes(), strUrl.getBytes() };
		}

		int statusCode = response.getStatusLine().getStatusCode();

		if ((statusCode == HttpStatus.SC_MOVED_PERMANENTLY)
				|| (statusCode == HttpStatus.SC_MOVED_TEMPORARILY)
				|| (statusCode == HttpStatus.SC_SEE_OTHER)
				|| (statusCode == HttpStatus.SC_TEMPORARY_REDIRECT)) {

			String newUrl = response.getLastHeader("Location").getValue();
			if (newUrl != null) {

				URL baseUrl;
				URL absUrl = null;
				try {
					baseUrl = new URL(strUrl);
					absUrl = new URL(baseUrl, newUrl);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}

				HttpEntity entity = response.getEntity();
				if (entity != null) {
					try {
						InputStream instream = entity.getContent();
						instream.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				refreshTimes--;
				return getPageGet(absUrl.toString(), refreshTimes);
			}
		} else if (response.getStatusLine().getStatusCode() == 200
				|| (isXCacheError && response.getStatusLine().getStatusCode() == 521)) {

			HttpEntity entity = response.getEntity();

			if (entity != null) {

				byte[] htmlSourceBytes = null;
				try {
					InputStream instream = null;
					Header header = entity.getContentEncoding();
					if (header != null
							&& header.getValue().equalsIgnoreCase("gzip")) {

						GzipDecompressingEntity gzipEntity = new GzipDecompressingEntity(
								entity);
						instream = gzipEntity.getContent();
					} else {
						instream = entity.getContent();
					}

					ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
					byte[] buff = new byte[100];
					int rc = 0;
					int totalBytes = 0;
					while ((rc = instream.read(buff, 0, 100)) > 0) {
						totalBytes += rc;

						if (totalBytes > this.MaxLengthToParse) {
							System.out
									.println("Read to find It's too long(longger than "
											+ this.MaxLengthToParse
											+ ").ignore it." + "\t" + strUrl);
							break;
						}

						swapStream.write(buff, 0, rc);
					}

					htmlSourceBytes = swapStream.toByteArray();

				} catch (Exception e) {
					;
				}

				if (htmlSourceBytes == null)
					return new byte[][] { null, null, "200".getBytes(),
							strUrl.getBytes() };

				htmlAndContentType = new byte[][] { htmlSourceBytes,
						contentType.getBytes(), "200".getBytes(),
						strUrl.getBytes() };

				if (isXCacheError
						&& response.getStatusLine().getStatusCode() == 521) {

					System.out
							.println("isXCacheError and status=521." + strUrl);
					return getXCachePage(strUrl, contentType, htmlSourceBytes);
				}

				if (refreshTimes > 0) {
					String refreshUrl = null;
					try {
						refreshUrl = getRefreshUrl(new String(
								htmlAndContentType[0], "gbk"));
					} catch (Exception e) {
						;
					}

					if (refreshUrl != null) {

						URL baseUrl, absUrl = null;
						try {
							baseUrl = new URL(strUrl);
							absUrl = new URL(baseUrl, refreshUrl);
						} catch (MalformedURLException e) {
							e.printStackTrace();
						}

						refreshUrl = absUrl.toString();

						refreshTimes--;
						return getPageGet(refreshUrl, refreshTimes);
					}
				}
			}
		} else {
			String strStatusCode = response.getStatusLine().getStatusCode()
					+ "";
			return new byte[][] { null, null, strStatusCode.getBytes(),
					strUrl.getBytes() };
		}

		return htmlAndContentType;
	}

	public byte[][] getXCachePage(String strUrl, String contentType,
			byte[] gxcacheJScode) {
		byte[][] htmlAndContentType = new byte[][] { null,
				contentType.getBytes(), "500".getBytes(), strUrl.getBytes() };
		;

		try {
			EngineBrowser browser = new EngineBrowser("htmlunit",
					this.userAgent, strUrl, 1, null);
			String page = browser.getHtmlUnitPage(strUrl, "get");

			htmlAndContentType = new byte[][] { page.getBytes(),
					contentType.getBytes(), "200".getBytes(), strUrl.getBytes() };

		} catch (Exception e) {
			;
		}

		return htmlAndContentType;
	}

	public String getLastmodifiedTime(String strUrl) {
		String lastmodifiedTime = null;

		String hostUrl = StringUtil.getHost(strUrl);
		if (hostUrl == null)
			return null;

		HttpGet httpget = null;

		try {
			httpget = new HttpGet(strUrl);

			httpget.setHeader("User-Agent", userAgent);

			httpget.setHeader("Accept-Language", "zh-CN,zh;q=0.8");
			httpget.setHeader("Accept-Charset", "GB2312,utf-8;q=0.7,*;q=0.7");
			httpget.setHeader("Accept",
					"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
			httpget.setHeader("Accept-Encoding", "gzip, deflate, sdch");

			httpget.setHeader("Host", hostUrl);
			httpget.setHeader("Referer", strUrl);
			httpget.setHeader("Connection", "keep-alive");

			for (String additionHeader : additionalRequestHeaders) {
				int p = additionHeader.indexOf(":");
				if (p > 0) {
					String strHead = additionHeader.substring(0, p);
					String strValue = additionHeader.substring(p + 1);

					httpget.setHeader(strHead, strValue);
				}
			}

			RequestConfig requestConfig = RequestConfig.custom()
					.setConnectTimeout(NetConnectTimeout)
					.setRedirectsEnabled(false)
					.setCookieSpec(CookieSpecs.BEST_MATCH).build();

			httpget.setConfig(requestConfig);

			HttpResponse response = defaultHttpClient.execute(httpget);

			Header headers[] = response.getAllHeaders();
			if (headers == null || headers.length == 0)
				return null;

			for (Header header : headers) {

				if (header.getName().equalsIgnoreCase("Last-Modified")) {
					lastmodifiedTime = header.getValue();
					break;
				}
			}
		} catch (Exception e) {
			System.out.println("Network error." + e);
			return null;
		} finally {
			if (httpget != null)
				httpget.abort();
		}

		return lastmodifiedTime;
	}

	class UrlCrawlerThread implements Callable<Object> {
		private String url;
		private byte[][] htmlAndContentType = null;

		public UrlCrawlerThread(String url) {
			this.url = url;
		}

		@Override
		public byte[][] call() throws Exception {
			try {
				htmlAndContentType = getPage(url);
				;
			} catch (Exception e) {
				System.out.println("Error in NetCrawler Thread=" + e);
				e.printStackTrace();
			}

			return htmlAndContentType;
		}
	}

	public  void main(String[] args) {
		String strContent = "<META HTTP-EQUIV=\"REFRESH\" CONTENT=\"0; URL=html/2013-07/15/node_1009.htm\">";
		strContent = "<meta http-equiv=refresh content=0;url=/gb/node2/node802/node149949/node445655/index.html>";

		System.out.println(HttpAgent.getRefreshUrl(strContent));

		String url = "http://www.moe.gov.cn/hah��ABC{nba}";
		url = "http://www.p2peye.com/forum-44-5.html";
		url = "http://www.p2peye.com/thread-461702-1-1.html";

		HttpAgent agent = new HttpAgent();
		String[] page = agent.getWebpage(url, "gbk", false);
		System.out.println("charset=" + page[0]);
		System.out.println("html=" + page[1]);

		System.out.println(encodeUrl(url));
	}
}
