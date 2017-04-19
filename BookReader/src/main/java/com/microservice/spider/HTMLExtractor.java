package com.microservice.spider;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.htmlparser.Parser;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.tags.CompositeTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import com.microservice.data.*;


public class HTMLExtractor {
	private static VipsConfig vipsConfig = new VipsConfig();

	private static String[] htmlContainerTags = { "body", "table", "tr", "th",
			"td", "div", "span" };

	public static String removeTag(String srcHtml) {
		StringBuffer sbuf = new StringBuffer();
		boolean inTag = false;
		boolean inBody = true;
		boolean inScript = false;
		boolean inStyle = false;
		boolean inComment = false;
		boolean inHref = false;

		int length = srcHtml.length();

		char oldch = ' ';
		for (int i = 0; i < length; i++) {
			char ch = srcHtml.charAt(i);
			if (ch == '<') {

				i++;

				for (; srcHtml.charAt(i) == ' ' || srcHtml.charAt(i) == '\t'
						|| srcHtml.charAt(i) == '\r'
						|| srcHtml.charAt(i) == '\n'; i++)
					;
				inTag = true;
				StringBuffer sbuf_1 = new StringBuffer(32);
				for (; i < length; i++) {
					char ch_1 = srcHtml.charAt(i);
					if (ch_1 == ' ' || ch_1 == '\t') {
						break;
					} else if (ch_1 == '>') {
						inTag = false;
						break;
					} else {
					}

					sbuf_1.append(ch_1);
					if (sbuf_1.toString().equals("!--"))
						break;
				}

				if (sbuf_1.toString().toUpperCase().equals("SCRIPT")) {
					if (!inScript) {
						inScript = true;
					}
				} else if (sbuf_1.toString().toUpperCase().equals("STYLE")) {
					if (!inScript) {
						inStyle = true;
					}
				} else if (sbuf_1.toString().toUpperCase().equals("A")) {
					if (!inScript) {
						inHref = true;
					}
				}

				else if (sbuf_1.toString().toUpperCase().equals("!--")) {
					inComment = true;
				}

				else if (sbuf_1.toString().toUpperCase().equals("/SCRIPT")) {
					if (inScript) {
						inScript = false;
					}
				} else if (sbuf_1.toString().toUpperCase().equals("/STYLE")) {
					if (!inScript) {
						inStyle = false;
					}
				} else if (sbuf_1.toString().toUpperCase().equals("/A")) {
					if (!inScript) {
						inHref = false;
					}
				}

				if (inComment) {
					char[] temp = new char[3];
					int j = 0;
					for (; i < length; i++) {
						char t = srcHtml.charAt(i);
						if (t == ' ' || t == '\t' || t == '\r' || t == '\n')
							continue;
						temp[j] = t;
						if (temp[j] == '>' && temp[(j + 3 - 1) % 3] == '-'
								&& temp[(j + 3 - 2) % 3] == '-') {
							inComment = false;
							break;
						}
						j = (j + 1) % 3;
					}
				}

			} else if (ch == '>') {
				inTag = false;
			} else {

				if (!inTag && inBody && !inScript && !inStyle && !inComment
						&& !inHref) {
					if (ch == '\t' || ch == '\r' || ch == '\n') {
						ch = ' ';
					}
					if (oldch == ' ' && ch != ' ') {
						sbuf.append(ch);
						oldch = ch;
					} else if (oldch != ' ' && ch == ' ') {
						sbuf.append(ch);
						oldch = ch;
					} else if (oldch != ' ' && ch != ' ') {
						sbuf.append(ch);
						oldch = ch;
					} else {
					}

				}
			}

		}
		return sbuf.toString();
	}

	public static String removeTagExceptImg(String srcHtml) {
		StringBuffer sbuf = new StringBuffer();
		boolean inTag = false;
		boolean inBody = true;
		boolean inScript = false;
		boolean inStyle = false;
		boolean inComment = false;
		boolean inHref = false;
		boolean inImg = false;
		boolean inReturn = false;

		int length = srcHtml.length();

		char oldch = ' ';
		for (int i = 0; i < length; i++) {
			char ch = srcHtml.charAt(i);
			if (ch == '<') {

				i++;

				for (; srcHtml.charAt(i) == ' ' || srcHtml.charAt(i) == '\t'
						|| srcHtml.charAt(i) == '\r'
						|| srcHtml.charAt(i) == '\n'; i++)
					;
				inTag = true;
				StringBuffer sbuf_1 = new StringBuffer(32);
				for (; i < length; i++) {
					char ch_1 = srcHtml.charAt(i);
					if (ch_1 == ' ' || ch_1 == '\t') {
						break;
					} else if (ch_1 == '>') {
						inTag = false;
						if (inImg || inReturn) {
							sbuf.append(">");
							inImg = false;
							inReturn = false;
						}
						break;
					} else {
					}

					sbuf_1.append(ch_1);
					if (sbuf_1.toString().equals("!--"))
						break;
				}

				if (sbuf_1.toString().toUpperCase().equals("SCRIPT")) {
					if (!inScript) {
						inScript = true;
					}
				} else if (sbuf_1.toString().toUpperCase().equals("IMG")) {
					if (!inScript) {
						inImg = true;
						sbuf.append("<img");
					}
				} else if (sbuf_1.toString().toUpperCase().equals("P")) {
					if (!inScript) {
						inReturn = true;
						sbuf.append("<p");
						if (!inTag) {
							sbuf.append(">");
							inReturn = false;
						}
					}
				} else if (sbuf_1.toString().toUpperCase().equals("/P")) {
					if (!inScript) {
						inReturn = true;
						sbuf.append("</p");
						if (!inTag) {
							sbuf.append(">");
							inReturn = false;
						}
					}
				} else if (sbuf_1.toString().toUpperCase().equals("BR")) {
					if (!inScript) {
						inReturn = true;
						sbuf.append("<br");
						if (!inTag) {
							sbuf.append(">");
							inReturn = false;
						}
					}
				} else if (sbuf_1.toString().toUpperCase().equals("STYLE")) {
					if (!inScript) {
						inStyle = true;
					}
				} else if (sbuf_1.toString().toUpperCase().equals("A")) {
					if (!inScript) {
						inHref = true;
					}
				}

				else if (sbuf_1.toString().toUpperCase().equals("!--")) {
					inComment = true;
				}

				else if (sbuf_1.toString().toUpperCase().equals("/SCRIPT")) {
					if (inScript) {
						inScript = false;
					}
				} else if (sbuf_1.toString().toUpperCase().equals("/STYLE")) {
					if (!inScript) {
						inStyle = false;
					}
				} else if (sbuf_1.toString().toUpperCase().equals("/A")) {
					if (!inScript) {
						inHref = false;
					}
				}

				if (inComment) {
					char[] temp = new char[3];
					int j = 0;
					for (; i < length; i++) {
						char t = srcHtml.charAt(i);
						if (t == ' ' || t == '\t' || t == '\r' || t == '\n')
							continue;
						temp[j] = t;
						if (temp[j] == '>' && temp[(j + 3 - 1) % 3] == '-'
								&& temp[(j + 3 - 2) % 3] == '-') {
							inComment = false;
							break;
						}
						j = (j + 1) % 3;
					}
				}

			} else if (ch == '>') {
				inTag = false;
				if (inImg || inReturn) {
					sbuf.append(">");
					inImg = false;
					inReturn = false;
				}
			} else {

				if ((!inTag && inBody && !inScript && !inStyle && !inComment && !inHref)
						|| (inTag && inImg)) {
					if (ch == '\t' || ch == '\r' || ch == '\n') {
						ch = ' ';
					}
					if (oldch == ' ' && ch != ' ') {
						sbuf.append(ch);
						oldch = ch;
					} else if (oldch != ' ' && ch == ' ') {
						sbuf.append(ch);
						oldch = ch;
					} else if (oldch != ' ' && ch != ' ') {
						sbuf.append(ch);
						oldch = ch;
					} else {
					}

				}
			}

		}
		return sbuf.toString();
	}

	public static Webpage getWebpageByUrl(String url, String[] charsetAndHtml,
			SpiderConfig htmlAnalyzerConfig) {
		Webpage webpage = null;

		if (charsetAndHtml == null)
			return null;

		String charset = charsetAndHtml[0];
		String strHtml = charsetAndHtml[1];

		if (charset == null)
			charset = "gbk";

		if (strHtml == null)
			return null;

		strHtml = removeScriptTag(strHtml);

		WebpageType webpageType = WebpageType.TEXT_PAGE;

		WebpageTextGroup wtg = HTMLExtractor.getLinkContText(strHtml);

		if (HTMLExtractor.isIndexPageByUrl(url, htmlAnalyzerConfig)) {
			webpageType = WebpageType.INDEX_PAGE;
		} else {

			int lenLink = TitleAnalyzer.getStringLength(wtg.getLinkText());

			int lenCont = TitleAnalyzer.getStringLength(wtg.getNormalText());

			double linkRate = 0d;

			if (lenLink > 0 || lenCont > 0)
				linkRate = Math.round(lenLink * 1000 / (lenLink + lenCont)) / 1000d;

			if (linkRate > vipsConfig.getLinkRate()) {
				webpageType = WebpageType.INDEX_PAGE;
			}
		}

		if (webpageType == WebpageType.INDEX_PAGE) {

			String cleanNormalText = getCleanNormalText(wtg.getNormalText(),
					htmlAnalyzerConfig);

			webpage = new Webpage();
			if (htmlAnalyzerConfig.includeLinkText())
				webpage.setContent(wtg.getLinkText() + "    " + cleanNormalText);
			else
				webpage.setContent(cleanNormalText);
		} else {

			webpage = getWebpageByHtml(strHtml, charset, htmlAnalyzerConfig);
		}

		if (webpage != null) {

			webpage.setPageType(webpageType);
			webpage.setCharset(charset);

		}

		return webpage;
	}

	public static Webpage getWebpageByHtml(String strHtml, String charset,
			SpiderConfig htmlAnalyzerConfig) {
		Webpage webpage = null;

		if (strHtml == null || strHtml.length() == 0)
			return webpage;

		OrFilter orFilter = new OrFilter();
		int tags = htmlContainerTags.length;

		TagNameFilter tagFilter[] = new TagNameFilter[tags];
		for (int i = 0; i < tags; i++)
			tagFilter[i] = new TagNameFilter(htmlContainerTags[i]);

		orFilter.setPredicates(tagFilter);

		String content = "";

		if (!htmlAnalyzerConfig.includeLinkText())
			strHtml = strHtml.replaceAll(
					"<(?i)a\\s.*?href[^>]*?>[\\s\\S]*?<\\s*?/\\s*?a\\s*?>", "");

		ArrayList<ContainerNode> containerNodeList = new ArrayList<ContainerNode>();
		try {

			Parser parser = Parser.createParser(strHtml, charset);

			if (parser != null) {

				NodeList nodesList = parser.extractAllNodesThatMatch(orFilter);

				if (nodesList != null && nodesList.size() > 0) {

					ContainerNode curContainerNode = null;
					String preName = "";
					String curName = "";

					for (int i = 0; i < nodesList.size(); i++) {

						if (i > 0) {
							TagNode tmpNode = (TagNode) nodesList
									.elementAt(i - 1);
							String nodeName = tmpNode.getTagName();
							preName = nodeName;
						}

						CompositeTag tmpNode = (CompositeTag) nodesList
								.elementAt(i);

						curName = tmpNode.getTagName();

						if (curName.equalsIgnoreCase("span")) {

							if (!preName.equalsIgnoreCase("span")) {

								if (preName.equalsIgnoreCase("div")) {

									if (HTMLExtractor.isContentNode(
											curContainerNode,
											vipsConfig.getLinkRate())) {
										containerNodeList.add(curContainerNode);
										curContainerNode = null;
									}
								}

								curContainerNode = new ContainerNode();
								curContainerNode.addCompositeTagNode(tmpNode);
							} else {

								if (curContainerNode == null)
									curContainerNode = new ContainerNode();
								curContainerNode.addCompositeTagNode(tmpNode);
							}
						}

						else if (curName.equalsIgnoreCase("div")) {

							if (!preName.equalsIgnoreCase("div")) {

								if (preName.equalsIgnoreCase("span")) {

									if (HTMLExtractor.isContentNode(
											curContainerNode,
											vipsConfig.getLinkRate())) {
										containerNodeList.add(curContainerNode);
										curContainerNode = null;
									}
								}

								curContainerNode = new ContainerNode();
								curContainerNode.addCompositeTagNode(tmpNode);
							} else {

								if (curContainerNode == null)
									curContainerNode = new ContainerNode();
								curContainerNode.addCompositeTagNode(tmpNode);
							}
						}

						else {

							if (preName.equalsIgnoreCase("span")
									|| preName.equalsIgnoreCase("div")) {
								if (HTMLExtractor.isContentNode(
										curContainerNode,
										vipsConfig.getLinkRate())) {
									containerNodeList.add(curContainerNode);
									curContainerNode = null;
								}
							}

							curContainerNode = new ContainerNode();

							curContainerNode.addCompositeTagNode(tmpNode);

							if (HTMLExtractor.isContentNode(curContainerNode,
									vipsConfig.getLinkRate())) {
								containerNodeList.add(curContainerNode);
								curContainerNode = null;
							}
						}
					}

					if (curContainerNode != null
							&& HTMLExtractor.isContentNode(curContainerNode,
									vipsConfig.getLinkRate())) {
						containerNodeList.add(curContainerNode);
						curContainerNode = null;
					}

					content = HTMLExtractor.getContentFromNodelist(
							containerNodeList, htmlAnalyzerConfig);

				}

				if (content != null && content.trim().length() > 1) {
					int contentLength = content.length();

					if (htmlAnalyzerConfig.includeLinkText()) {
						if (contentLength < vipsConfig.getMinTextLength()) {

							String tmpContent = HTMLExtractor
									.getHtmlLinkContent(nodesList);

							if (Math.round(contentLength * 10000
									/ (tmpContent.length() + 1)) / 10000d < vipsConfig
									.getTextRatioLink()) {
								System.out
										.println("Content length less than 80, and includeLIntext=true. url="
												+ strHtml);
								content = tmpContent + "  " + content;
							}
						}
					}
				} else {

					content = removeTag(strHtml);

					content = getCleanNormalText(content, htmlAnalyzerConfig);

					System.out
							.println("Parse Webpage failed, get content by removeTag="
									+ content);
				}
			}

			webpage = new Webpage();
			webpage.setContent(content);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return webpage;
	}

	static boolean isContentNode(ContainerNode curNode, double rate) {
		boolean bln = false;
		if (curNode == null)
			return false;
		int wordCount = curNode.wordCount;
		int lwordCount = curNode.lwordCount;

		double temp = (lwordCount + 5) * 1d / (wordCount + lwordCount);

		if (wordCount > 25) {

			if (temp < 0.20) {
				bln = true;
			}
		} else if (wordCount >= 15 && wordCount <= 25) {

			if (temp < 0.34) {
				bln = true;
			}
		}
		return bln;
	}

	private static String getCleanNormalText(String content,
			SpiderConfig htmlAnalyzerConfig) {
		String cleanContent = "";

		if (content == null || content.length() < 1)
			return "";

		String[] strSegments = content.split("_");
		if (strSegments != null && strSegments.length > 0) {
			for (int i = 0; i < strSegments.length; i++) {
				if (isInvalidNode(strSegments[i], htmlAnalyzerConfig))
					continue;
				else
					cleanContent += "_" + strSegments[i];
			}
		}

		return cleanContent;
	}

	public static boolean isInvalidNode(String content,
			SpiderConfig htmlAnalyzerConfig) {
		boolean isDeclared = false;

		if (content == null || content.trim().length() < 1)
			return false;

		if (content
				.matches("(\\s|:|,|\\.|\\/|\\\\|@|>|<|\\(|\\)|\\+|!|\\[|\\])+?"))
			return true;

		ArrayList<String> invalidTags = htmlAnalyzerConfig.getInvalidTags();
		if (invalidTags == null || invalidTags.size() < 1)
			return false;

		for (int i = 0; i < invalidTags.size(); i++) {
			String tk = invalidTags.get(i);

			if (content.matches(tk)) {
				isDeclared = true;

				break;
			}
		}

		return isDeclared;
	}

	private static boolean isIndexUrl(String fileName,
			SpiderConfig htmlAnalyzerConfig) {
		boolean isIndexPage = false;

		if (fileName == null || fileName.trim().length() < 1)
			return false;

		ArrayList<String> indexPageTags = vipsConfig.getIndexPageTags();
		if (indexPageTags == null || indexPageTags.size() < 1)
			return false;

		for (int i = 0; i < indexPageTags.size(); i++) {

			if (fileName.startsWith(indexPageTags.get(i))) {
				isIndexPage = true;
				break;
			}
		}

		return isIndexPage;
	}

	public static boolean isIndexPageByUrl(String strUrl,
			SpiderConfig htmlAnalyzerConfig) {
		boolean isIndexpage = false;

		if (strUrl == null || strUrl.trim().equals(""))
			throw new RuntimeException("URL is null, please input URL!");

		try {

			URL url = new URL(strUrl);

			String path = url.getPath();
			String query = url.getQuery();

			if (path.length() < 2) {
				isIndexpage = true;
			} else {

				int index = path.lastIndexOf("/");

				int index_point = path.lastIndexOf(".");
				int len = path.length();

				if (index_point == -1 || index_point == len - 1
						|| index == len - 1 || index > index_point) {

					if (query == null)
						isIndexpage = true;
				} else {

					String fileName = path.substring(index + 1, index_point)
							.toLowerCase();

					isIndexpage = isIndexUrl(fileName, htmlAnalyzerConfig);
				}
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}

		return isIndexpage;
	}

	static String getContentFromNodelist(ArrayList<ContainerNode> conList,
			SpiderConfig htmlAnalyzerConfig) {
		String content = "";
		int contentLength = 0;

		int size = conList.size();
		if (size == 0)
			return "";

		int length = 0;

		ArrayList<ContainerNode> tmpList = new ArrayList<ContainerNode>();

		int[] blockLength = new int[size];

		int count = 0;

		for (int i = 0; i < size; i++) {
			ContainerNode tmpNode = conList.get(i);

			length = tmpNode.wordCount;

			if (length >= vipsConfig.getMaxMergeTextLength())
				return tmpNode.content;

			if (length > vipsConfig.getMinBlockTextLength()) {

				if (length < vipsConfig.getDeclareTextLength()
						&& isInvalidNode(tmpNode.content, htmlAnalyzerConfig)) {

				} else {
					tmpList.add(tmpNode);
					blockLength[count] = length;
					count++;
				}
			}
		}

		long maxCount = Math.round(count * vipsConfig.getMaxBlockRate() + 0.4);

		for (int i = 0; i < maxCount; i++) {
			int tmpCount = 0;
			int index = 0;
			if (contentLength > vipsConfig.getMaxMergeTextLength()) {
				return content;
			}
			for (int j = 0; j < count; j++) {

				if (i == 0 && blockLength[0] > vipsConfig.getFirsBlockLength()) {
					tmpCount = blockLength[0];
					index = 0;
				} else if (tmpCount < blockLength[j]) {
					tmpCount = blockLength[j];
					index = j;
				}
			}

			String cleanContentText = getCleanNormalText(
					tmpList.get(index).content, htmlAnalyzerConfig);
			if (cleanContentText != null
					&& cleanContentText.trim().length() > 1) {

				if (!content.equals(""))
					content = content + "\n_" + cleanContentText;
				else
					content = cleanContentText;
			}

			contentLength = contentLength + tmpList.get(index).wordCount;

			blockLength[index] = 0;

		}

		return content;
	}

	private static String getHtmlLinkContent(NodeList nodeList) {
		String linkContent = "";
		int size = nodeList.size();

		for (int i = 0; i < size; i++) {
			CompositeTag tmpNode = null;
			try {
				tmpNode = (CompositeTag) nodeList.elementAt(i);
			} catch (Exception e) {

				continue;
			}

			try {

				NodeList linkNodes = tmpNode.searchFor(LinkTag.class, false);

				if (linkNodes != null) {

					for (int j = 0; j < linkNodes.size(); j++) {
						String linkText = ((LinkTag) linkNodes.elementAt(j))
								.getLinkText();

						if (linkText != null) {
							linkText = linkText.trim();
							if (!linkText.equals(""))
								linkContent = linkContent + " " + linkText;
						}
					}
				}
			} catch (Exception e) {

			}
		}
		return linkContent;
	}

	public static WebpageTextGroup getLinkContText(String srcHtml) {
		WebpageTextGroup wtg = new WebpageTextGroup();

		StringBuffer sbufTitl = new StringBuffer();
		String strKeyW = "";
		String strDesc = "";

		StringBuffer sbufLink = new StringBuffer();
		StringBuffer sbufContent = new StringBuffer();

		boolean inTag = false;
		boolean inScript = false;
		boolean inStyle = false;
		boolean inComment = false;
		boolean inHref = false;
		boolean inTitle = false;

		boolean inSelect = false;
		boolean inTextarea = false;

		int length = srcHtml.length();

		char oldchL = ' ';
		char blank = '。';
		char oldchC = ' ';

		for (int i = 0; i < length; i++) {
			char ch = srcHtml.charAt(i);
			if (ch == '<') {
				i++;

				for (; i < length; i++) {
					if (srcHtml.charAt(i) == ' ' || srcHtml.charAt(i) == '\t'
							|| srcHtml.charAt(i) == '\r'
							|| srcHtml.charAt(i) == '\n') {
					} else
						break;
				}

				inTag = true;
				StringBuffer sbuf_1 = new StringBuffer(32);
				for (; i < length; i++) {
					char ch_1 = srcHtml.charAt(i);
					if (ch_1 == ' ' || ch_1 == '\t') {
						break;
					} else if (ch_1 == '>') {
						inTag = false;
						break;
					} else {
					}
					sbuf_1.append(ch_1);
					if (sbuf_1.toString().equals("!--"))
						break;
				}

				if (sbuf_1.toString().toUpperCase().equals("SCRIPT")) {
					if (!inScript) {
						inScript = true;
					}
				} else if (sbuf_1.toString().toUpperCase().equals("TITLE")) {
					if (!inScript) {
						inTitle = true;

					}
				} else if (sbuf_1.toString().toUpperCase().equals("META")) {

					if (!inScript) {
						StringBuffer sbuf_Meta = new StringBuffer(32);
						for (; i < length; i++) {
							char ch_Meta = srcHtml.charAt(i);
							if (ch_Meta == '>') {
								inTag = false;
								break;
							} else {
								sbuf_Meta.append(ch_Meta);
							}
						}
						String strTmp = sbuf_Meta.toString().toLowerCase();

						int a = strTmp.indexOf(" content");
						int n = strTmp.indexOf(" name");

						if (a > -1 && n > -1) {

							a = strTmp.indexOf("=", a);
							n = strTmp.indexOf("=", n);
							if (a > -1 && n > -1) {

								int b = strTmp.indexOf("keywords");

								if (b > n) {

									a = strTmp.indexOf("\"", a);
									int a2 = strTmp.indexOf("\"", a + 1);
									if (a > -1 && a2 > a) {
										strKeyW = (sbuf_Meta.toString())
												.substring(a + 1, a2);
									}
								}

								int c = strTmp.indexOf("description");

								if (c > n) {

									a = strTmp.indexOf("\"", a);
									int a2 = strTmp.indexOf("\"", a + 1);
									if (a > -1 && a2 > a) {
										strDesc = (sbuf_Meta.toString())
												.substring(a + 1, a2);
									}
								}

							}
						}
					}
				} else if (sbuf_1.toString().toUpperCase().equals("STYLE")) {
					if (!inScript) {
						inStyle = true;
					}
				} else if (sbuf_1.toString().toUpperCase().equals("SELECT")) {
					if (!inScript) {
						inSelect = true;
					}
				} else if (sbuf_1.toString().toUpperCase().equals("TEXTAREA")) {
					if (!inScript) {
						inTextarea = true;
					}
				} else if (sbuf_1.toString().toUpperCase().equals("A")) {

					if (!inScript) {
						StringBuffer sbuf_2 = new StringBuffer(32);
						for (; i < length; i++) {
							char ch_2 = srcHtml.charAt(i);
							if (ch_2 == ' ' || ch_2 == '\t') {

							} else if (ch_2 == '>') {
								inTag = false;
								break;
							} else {
								sbuf_2.append(ch_2);
							}
						}

						if (sbuf_2.toString().toUpperCase().indexOf("HREF") != -1) {
							inHref = true;
							blank = '。';

						}
					}
				} else if (sbuf_1.toString().toUpperCase().equals("!--")) {
					inComment = true;
				} else if (sbuf_1.toString().toUpperCase().equals("/SCRIPT")) {
					if (inScript) {
						inScript = false;
					}
				} else if (sbuf_1.toString().toUpperCase().equals("/TITLE")) {
					if (!inScript) {
						inTitle = false;
					}
				} else if (sbuf_1.toString().toUpperCase().equals("/STYLE")) {
					if (!inScript) {
						inStyle = false;
					}
				} else if (sbuf_1.toString().toUpperCase().equals("/SELECT")) {
					if (!inScript) {
						inSelect = false;
					}
				} else if (sbuf_1.toString().toUpperCase().equals("/TEXTAREA")) {
					if (!inScript) {
						inTextarea = false;
					}
				} else if (sbuf_1.toString().toUpperCase().equals("/A")) {
					if (!inScript) {
						inHref = false;
						blank = ' ';
					}
				}

				if (inComment) {
					char[] temp = new char[3];
					int j = 0;
					for (; i < length; i++) {
						char t = srcHtml.charAt(i);
						if (t == ' ' || t == '\t' || t == '\r' || t == '\n')
							continue;
						temp[j] = t;
						if (temp[j] == '>' && temp[(j + 3 - 1) % 3] == '-'
								&& temp[(j + 3 - 2) % 3] == '-') {
							inComment = false;
							break;
						}
						j = (j + 1) % 3;
					}
				}
			} else if (ch == '>') {
				inTag = false;
			} else {

				if (!inTag && !inScript && !inSelect && !inTextarea && !inTitle
						&& !inStyle && !inComment && !inHref) {
					if (ch == '\t' || ch == '\r' || ch == '\n') {
						ch = ' ';
					}
					if (oldchC == ' ' && ch != ' ') {
						sbufContent.append(ch);
						oldchC = ch;
					} else if (oldchC != ' ' && ch == ' ') {
						sbufContent.append(ch);
						oldchC = ch;
					} else if (oldchC != ' ' && ch != ' ') {
						sbufContent.append(ch);
						oldchC = ch;
					} else {
					}
				}

				if (!inTag && !inScript && !inSelect && !inTextarea && !inTitle
						&& !inStyle && !inComment && inHref) {
					if (ch == '\t' || ch == '\r' || ch == '\n') {
						ch = ' ';
					}
					if (blank == '。') {
						sbufLink.append(' ');
						blank = ' ';
					}

					if (oldchL == ' ' && ch != ' ') {
						sbufLink.append(ch);
						oldchL = ch;
					} else if (oldchL != ' ' && ch == ' ') {
						sbufLink.append(ch);
						oldchL = ch;
					} else if (oldchL != ' ' && ch != ' ') {
						sbufLink.append(ch);
						oldchL = ch;
					} else {
					}
				}

				if (!inTag && inTitle && !inScript && !inComment) {
					if (ch == '\t' || ch == '\r' || ch == '\n') {
						ch = ' ';
					}
					if (oldchC == ' ' && ch != ' ') {
						sbufTitl.append(ch);
						oldchL = ch;
					} else if (oldchC != ' ' && ch == ' ') {
						sbufTitl.append(ch);
						oldchL = ch;
					} else if (oldchC != ' ' && ch != ' ') {
						sbufTitl.append(ch);
						oldchL = ch;
					} else {
					}
				}
			}
		}

		wtg.setLinkText(ContainerNode.filterInvalidHtmlBlank(sbufLink
				.toString()));
		wtg.setNormalText(ContainerNode.filterInvalidHtmlBlank(sbufContent
				.toString()));
		wtg.setTitle(sbufTitl.toString());
		wtg.setKeywords(strKeyW);
		wtg.setDescription(strDesc);

		return wtg;
	}

	public static String removeScriptTag(String srcHtml) {

		boolean inScript = false;
		boolean inComment = false;

		int length = srcHtml.length();

		StringBuffer sbuf = new StringBuffer();
		String tagName = "";

		for (int i = 0; i < length; i++) {
			char ch = srcHtml.charAt(i);
			char ch_1 = ' ';

			if (ch == '<') {
				i++;

				for (; i < length; i++) {
					if (srcHtml.charAt(i) == ' ' || srcHtml.charAt(i) == '\t'
							|| srcHtml.charAt(i) == '\r'
							|| srcHtml.charAt(i) == '\n') {
					} else
						break;
				}

				StringBuffer sbuf_1 = new StringBuffer(32);
				for (; i < length; i++) {
					ch_1 = srcHtml.charAt(i);
					if (ch_1 == ' ' || ch_1 == '\t') {
						break;
					} else if (ch_1 == '>') {
						break;
					} else {
					}
					sbuf_1.append(ch_1);
					if (sbuf_1.toString().equals("!--"))
						break;
				}
				tagName = sbuf_1.toString().toUpperCase();

				if (tagName.equals("P")) {

					for (; i < length; i++) {
						char t = srcHtml.charAt(i);
						if (t == '>') {
							sbuf.append("_");
							break;
						} else {
						}
					}
				} else if (tagName.equals("/P")) {
				}

				else if (tagName.equals("SCRIPT")) {
					if (!inScript) {
						inScript = true;
					}
				} else if (tagName.equals("!--")) {
					inComment = true;

					if (inComment) {
						char[] temp = new char[3];
						int j = 0;
						for (; i < length; i++) {
							char t = srcHtml.charAt(i);
							if (t == ' ' || t == '\t' || t == '\r' || t == '\n')
								continue;
							temp[j] = t;
							if (temp[j] == '>' && temp[(j + 3 - 1) % 3] == '-'
									&& temp[(j + 3 - 2) % 3] == '-') {
								inComment = false;
								break;
							}
							j = (j + 1) % 3;
						}
					}
				} else if (tagName.equals("/SCRIPT")) {
					if (inScript) {
						inScript = false;
					}
				} else if (!inScript) {
					sbuf.append("<" + sbuf_1.toString() + ch_1);
				}

			} else if (ch == '>') {
				if (!inScript) {
					sbuf.append(ch);
				}
			} else if (!inScript) {
				sbuf.append(ch);
			}
		}

		return sbuf.toString();
	}

	public static void main(String[] args) {

	}

}
