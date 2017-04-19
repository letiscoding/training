package com.microservice.util;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcherInput;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

public class RegexUtil {
	static Logger logger = Logger.getLogger(RegexUtil.class);

	public static String TAG_REGEX_SPLIT = "\\|\\|";

	private static Pattern getPattern(final String findStr) throws Exception {
		PatternCompiler compiler = new Perl5Compiler();
		return compiler.compile(findStr, Perl5Compiler.CASE_INSENSITIVE_MASK
				| Perl5Compiler.SINGLELINE_MASK);
	}

	public static String getMatchedStr(final String source,
			final String patten, final int index) throws Exception {
		MatchResult result = getMatchResult(source, patten);
		if (result != null)
			return result.group(index);
		return null;
	}

	public static String getMatchedStr(final String source,
			final String patten, final String replace) throws Exception {
		MatchResult match = getMatchResult(source, patten);
		if (match == null)
			return null;

		String result = match.group(1);

		if (replace != null && !replace.trim().equals(""))
			return replaceGroup(match, replace);

		return result;
	}

	public static String getAllMatchedStr(final String source,
			final String patten, final String replace, final String split)
			throws Exception {

		List<MatchResult> list = getAllMatchResult(source, patten);
		if (list == null || list.size() == 0)
			return null;

		StringBuffer buf = new StringBuffer();
		MatchResult match = null;
		String result = null;

		for (int j = 0; j < list.size(); j++) {
			match = list.get(j);
			result = match.group(1);

			if (replace != null && !replace.trim().equals(""))
				result = replaceGroup(match, replace);
			if (j > 0)
				buf.append(split);
			buf.append(result);
		}
		return buf.toString();
	}

	public static String replaceGroup(final MatchResult match,
			final String replace) {
		if (match == null)
			return null;
		if (replace == null || !replace.contains("^"))
			return replace;
		String result = replace;
		int count = match.groups();
		for (int i = 1; i < count; i++)
			result = result.replace("^" + i, match.group(i));
		return result;
	}

	public static boolean isMatch(final String source, final String patten)
			throws Exception {
		if (patten == null || source == null)
			return false;
		boolean flag = true;
		String p = patten;
		if (p.startsWith("!!")) {
			p = p.substring(2);
			flag = false;
		}
		MatchResult result = getMatchResult(source, p);
		return flag ? result != null : result == null;
	}

	private static MatchResult getMatchResult(final String source,
			final String patten) {
		if (source == null || patten == null || source.equals(""))
			return null;

		String pattens[] = patten.split(TAG_REGEX_SPLIT);
		Perl5Matcher matcher = null;
		Pattern pattern = null;
		String temp = null;
		PatternCompiler compiler = new Perl5Compiler();
		for (int i = 0; i < pattens.length; i++) {
			temp = pattens[i];
			if (temp == null || temp.equals(""))
				continue;
			matcher = new Perl5Matcher();

			try {
				pattern = compiler.compile(temp,
						Perl5Compiler.CASE_INSENSITIVE_MASK
								| Perl5Compiler.SINGLELINE_MASK);
				if (matcher.contains(source, pattern)) {
					return matcher.getMatch();
				}
			} catch (MalformedPatternException e) {
				logger.error("Regex error." + e);
			}

		}
		return null;
	}

	public static List<MatchResult> getAllMatchResult(final String source,
			final String patten) throws Exception {
		if (source == null || patten == null || source.equals(""))
			return null;

		List<MatchResult> list = new ArrayList<MatchResult>();
		String pattens[] = patten.split(TAG_REGEX_SPLIT);
		PatternMatcherInput input = new PatternMatcherInput(source);
		Perl5Matcher matcher = null;
		Pattern pattern = null;
		String temp;
		PatternCompiler compiler = new Perl5Compiler();
		for (int i = 0; i < pattens.length; i++) {
			temp = pattens[i];
			if (temp == null || temp.equals(""))
				continue;
			pattern = compiler.compile(temp,
					Perl5Compiler.CASE_INSENSITIVE_MASK
							| Perl5Compiler.SINGLELINE_MASK);
			matcher = new Perl5Matcher();

			while (matcher.contains(input, pattern)) {
				list.add(matcher.getMatch());
			}
		}
		return list;
	}

	public static String replaceAll(final String source, final String patten,
			final String replace) throws Exception {
		if (source == null)
			return null;
		if (patten == null || replace == null || patten.equals(""))
			return null;
		Perl5Matcher matcher = new Perl5Matcher();
		Pattern pattern = getPattern(patten);
		String value = source;
		StringBuffer buf = new StringBuffer(100);
		MatchResult result = null;
		while (matcher.contains(value, pattern)) {
			result = matcher.getMatch();
			buf.delete(0, buf.length());
			int begin = result.beginOffset(0);
			int end = result.endOffset(0);
			buf.append(value.substring(0, begin));
			buf.append(replaceGroup(result, replace));
			buf.append(value.substring(end));
			value = buf.toString();
		}
		return value;
	}

	public static String replaceFirst(final String source, final String patten,
			final String replace) throws Exception {
		if (source == null)
			return null;
		if (patten == null || replace == null || patten.equals(""))
			return null;
		Perl5Matcher matcher = new Perl5Matcher();
		Pattern pattern = getPattern(patten);
		StringBuffer buf = new StringBuffer(100);
		if (matcher.contains(source, pattern)) {
			MatchResult result = matcher.getMatch();
			buf.delete(0, buf.length());
			int begin = result.beginOffset(0);
			int end = result.endOffset(0);
			buf.append(source.substring(0, begin));
			buf.append(replace);
			buf.append(source.substring(end));
			return buf.toString();
		}
		return source;
	}

	public static List<String> getAllMatched(final String source,
			final String patten, final int index) throws Exception {
		List<String> list = new ArrayList<String>();
		if (source == null || patten == null || source.equals("")
				|| patten.equals(""))
			return list;
		Perl5Matcher matcher = new Perl5Matcher();
		Pattern pattern = getPattern(patten);
		PatternMatcherInput input = new PatternMatcherInput(source);
		while (matcher.contains(input, pattern)) {
			list.add(matcher.getMatch().group(index));
		}
		return list;
	}

	public static String getBetween(final String source,
			final String beginPatten, final String endPatten) throws Exception {
		if (source == null || source.equals(""))
			return null;
		int beginPos = 0;
		int endPos = -1;

		MatchResult resultBegin = null;

		if (beginPatten != null && !beginPatten.equals("")) {
			resultBegin = getMatchResult(source, beginPatten);
			if (resultBegin == null)
				throw new Exception("beginPatten not found," + beginPatten);
			String begin = resultBegin.group(0);
			beginPos = resultBegin.beginOffset(0) + begin.length();
		}

		if (endPatten != null && !endPatten.equals("")) {
			String tail = source;
			if (resultBegin != null)
				tail = source.substring(beginPos);
			MatchResult resultEnd = getMatchResult(tail, endPatten);
			if (resultEnd == null)
				throw new Exception("endPatten not found," + endPatten);
			endPos = beginPos + resultEnd.beginOffset(0);

		}

		if (beginPos >= 0 && endPos >= 0) {
			return source.substring(beginPos, endPos);
		} else if (beginPos > 0)
			return source.substring(beginPos);
		else if (beginPos == 0)
			return source;
		else {
			return null;
		}
	}

	public static String filterSimpleHTML(final String s) {
		if (s == null)
			return null;

		String ns = s.trim();

		ns = ns.replaceAll("<[^>]*>", "");

		ns = ns.replaceAll("&lt;", "<");
		ns = ns.replaceAll("&gt;", ">");

		ns = ns.replaceAll("&quot;", "\"");

		ns = ns.replaceAll("&yen;", "��");

		ns = ns.replaceAll("&ldquo;", "��");
		ns = ns.replaceAll("&rdquo;", "��");

		ns = ns.replaceAll("&brvbar;", "|");
		ns = ns.replaceAll("&nbsp;", "");

		ns = ns.replaceAll("&[^#][\\S]+?;", "");

		ns = ns.trim();

		return ns;
	}

	public static String filterHTML(final String s) {
		return filterHTML(s, null);
	}

	public static String filterHTML(final String s, final String reserve) {
		if (s == null)
			return null;

		String v = s.replaceAll("<!--[\\s\\S]*?-->", " ");

		v = v.replaceAll(
				"(?i)<\\s*?script[\\s\\S]*?>[\\s\\S]*?<\\s*?/\\s*?script\\s*?>",
				" ");
		v = v.replaceAll(
				"(?i)<\\s*?style[\\s\\S]*?>[\\s\\S]*?<[\\s\\S]*?/\\s*?style\\s*?>",
				" ");

		v = v.replaceAll(
				"(?i)<object[\\s\\S]*?>[\\s\\S]*?<\\s*?/\\s*?object\\s*?>", " ");

		v = v.replaceAll(
				"(?i)<select[\\s\\S]*?>[\\s\\S]*?<\\s*?/\\s*?select\\s*?>", " ");

		v = v.replaceAll("(?i)<\\s*?br\\s*?(/|)\\s*?>", " ");
		v = v.replaceAll("(?i)<\\s*?p\\s*?(/|)\\s*?>", " ");
		v = v.replaceAll("(?i)<\\s*?(/|)\\s*?p\\s*?>", " ");

		v = v.replaceAll("<[^>]*>", "");

		String[] reserves = null;
		if (reserve != null && !reserve.trim().equals("")) {
			StringTokenizer st = new StringTokenizer(reserve, ",");
			int total = st.countTokens();
			reserves = new String[total];
			for (int i = 0; i < total; i++) {
				reserves[i] = st.nextToken().trim();
			}
		}

		if (reserves != null) {
			for (int i = 0; i < reserves.length; i++) {
				String tag = reserves[i].toLowerCase();
				if (tag.equals("img"))
					continue;
				if (!tag.equals("")) {
					v = v.replaceAll("<\\s*" + tag.toUpperCase() + ".*?>",
							"=====" + tag + "=====");
					v = v.replaceAll("<\\s*" + tag + ".*?>", "=====" + tag
							+ "=====");
					v = v.replaceAll("</\\s*" + tag.toUpperCase() + ".*?>",
							"/////" + tag + "/////");
					v = v.replaceAll("</\\s*" + tag + ".*?>", "/////" + tag
							+ "/////");
				}
			}
		}

		v = removeSpecifiedChars(v);

		if (reserves != null && v != null) {
			for (int i = 0; i < reserves.length; i++) {
				String tag = reserves[i].toLowerCase();
				if (tag.equals("img"))
					continue;
				if (!tag.equals("")) {
					v = v.replace("=====" + tag + "=====", "<" + tag + ">");
					v = v.replace("/////" + tag + "/////", "</" + tag + ">");
				}
			}

			v = trimTag(v, reserves);
		}
		return v;
	}

	private static String trimTag(final String s, final String[] tags) {
		if (tags == null || tags.length == 0)
			return s;
		if (s == null)
			return null;
		boolean handled = false;
		int len1 = 0;
		int len2 = 0;
		String temp = null;
		String result = s;
		String tag = null;

		while (true) {
			handled = false;
			for (int i = 0; i < tags.length; i++) {
				tag = "<" + tags[i] + ">";
				len2 = tag.length();

				len1 = result.length();

				if (len1 == len2 && result.equalsIgnoreCase(tag))
					return null;

				else if (len1 > len2) {

					temp = result.substring(0, len2);
					if (temp.equalsIgnoreCase(tag)) {
						handled = true;
						result = result.substring(len2);
						result = result.trim();
					}
				}

				len1 = result.length();

				if (len1 == len2 && result.equalsIgnoreCase(tag))
					return null;

				else if (len1 > len2) {

					temp = result.substring(len1 - len2);
					if (temp.equalsIgnoreCase(tag)) {
						handled = true;
						result = result.substring(0, len1 - len2);
						result = result.trim();
					}
				}
			}

			if (!handled)
				break;
		}
		return result;
	}

	public static String filterUrl(final String s) {
		if (s == null)
			return null;
		String v = s;
		v = v.replaceAll("&nbsp;", " ");
		v = v.replaceAll("&amp;", "&");
		v = v.replaceAll("&quot;", "\"");
		v = v.replaceAll("&apos;", "'");
		v = v.replaceAll("&gt;", ">");
		v = v.replaceAll("&lt;", "<");

		v = v.trim();
		return v;
	}

	public static String fiterBadImg(final String s) {
		if (s == null)
			return null;
		String v = s;
		Parser parser;
		NodeList nodelist;
		parser = Parser.createParser(s, "gbk");
		NodeFilter imgfilter = new NodeClassFilter(
				ImageTag.class);
		OrFilter lastFilter = new OrFilter();
		lastFilter.setPredicates(new NodeFilter[] { imgfilter });
		try {
			nodelist = parser.parse(lastFilter);
		} catch (ParserException e) {
			e.printStackTrace();
			return s;
		}
		Node[] nodes = nodelist.toNodeArray();

		for (int i = 0; i < nodes.length; i++) {
			Node node = nodes[i];
			if (node instanceof ImageTag) {
				ImageTag imgTag = (ImageTag) node;
				String src = imgTag.getAttribute("src");

				if (src.indexOf("photo.sina.com.cn") > 0
						|| src.indexOf("bbs.163.com") > 0
						|| src.indexOf("imgcache.qq.com") > 0
						|| src.indexOf("www.sinaimg.cn") > 0) {
					logger.debug("repalce img-------------------"
							+ imgTag.toHtml());
					v = v.replace(imgTag.toHtml(), "");
				}
			}
		}
		v = v.trim();
		return v;
	}

	public static String removeSpecifiedChars(final String s) {
		if (s == null)
			return null;

		String v = s;

		v = v.replaceAll("<li.*?>", " ");
		v = v.replaceAll("<in.*?>", " ");
		v = v.replaceAll("<if.*?>", " ");
		v = v.replaceAll("<[^i][\\s\\S]*?>", " ");

		v = v.replaceAll("��+", " ");
		v = v.replaceAll("\\s+", " ");
		v = v.replaceAll("��", ",");

		v = v.replaceAll("^[��*| *]*", " ");
		v = v.replaceAll("[��*| *]*$", " ");

		v = v.replaceAll("&[\\S]+?;", " ");

		v = v.trim();

		return v;
	}

	public static void main(String[] args) {
		String strHtml = "i like frame page.<iframe width=\"390\"  height=\"120\" src= \" http://www.mof.gov.cn/xinwen/index_3389.htm \" frameborder=\"false\" marginheight=\"0\" > </iframe>\ncx";
		strHtml += "�м�����\n<iframe height=\"120\" src= \"http://bbb.mof.gov.cn/index_3389.htm\" > </iframe>\ncx";
		strHtml += "\ni don't like page too";

		System.out.println("strHtml=" + strHtml);

		System.out.println("filterHtml="
				+ filterSimpleHTML("��ҳ &gt; ���� &gt; ȯ������ &gt; ����"));

		String strRegx = "[.\n]*<iframe.*src=([\\s\\S]*?)>.*</iframe>[.\n]*";

		String iframes[] = strHtml.split("<iframe ");
		if (iframes != null) {
			for (int i = 0; i < iframes.length; i++) {
				if (iframes[i] == null || iframes[i].indexOf("</iframe") == -1)
					continue;

				String strTodo = "<iframe " + iframes[i];

				try {
					MatchResult aaa = RegexUtil
							.getMatchResult(strTodo, strRegx);

					System.out.println("groups=" + aaa.groups());
					System.out.println("\tgroup0=" + aaa.group(0));

					String strUrl = aaa.group(1);
					if (strUrl == null)
						continue;

					strUrl = strUrl.trim();
					if (strUrl.startsWith("\""))
						strUrl = strUrl.substring(1).trim();

					int pos = strUrl.indexOf(" ");
					if (pos > 0)
						strUrl = strUrl.substring(0, pos);

					if (strUrl.endsWith("\""))
						strUrl = strUrl.substring(0, strUrl.length() - 1)
								.trim();

					System.out.println("\tgroup1=" + strUrl);

				} catch (Exception e) {
					;
				}
			}
		}

	}
}
