package com.microservice.spider;

import java.util.ArrayList;

import com.microservice.util.RegexUtil;

public class TitleAnalyzer {
	private static String illeagTagsRegex = ".*?(?i)(博客|社区|论坛|58同城|powered|最|领导者|股吧).*?";
	private static String strEndRegex = ".*?(网|频道|微博|版|页|股吧|贴吧|信息|文章|资讯)";

	public static String getOriginTitle(String htmlSource) {
		String strTitle = null;

		String strTitleRegx = "(?i)<\\s*?title\\s*?>([\\s\\S]*?)<\\s*?/\\s*?title\\s*?>";
		try {
			strTitle = RegexUtil.getMatchedStr(htmlSource, strTitleRegx, 1);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return strTitle;
	}

	public static String getStandardTitle(String title) {
		String newTitle = title;

		if (title == null || title.equals(""))
			return null;

		if (newTitle.startsWith("..."))
			newTitle = newTitle.substring(3);

		if (newTitle.endsWith("..."))
			newTitle = newTitle.substring(0, newTitle.length() - 3);

		newTitle = filterSimpleHTML(newTitle);

		newTitle = newTitle.replaceAll("\\[.*?\\]|【.*?】|", "");

		newTitle = replaceChar(newTitle, '-', '—');

		String[] titleArray = newTitle.split("_|｜|\\||–|—|‖");

		if (titleArray.length == 0)
			return title;

		ArrayList<String> validSegList = new ArrayList<String>();
		for (int i = 0; i < titleArray.length; i++) {
			String tmp = titleArray[i];

			if (tmp == null || tmp.trim().length() < 2)
				continue;

			tmp = tmp.trim();

			if (tmp.matches(illeagTagsRegex)) {

				continue;
			}

			if (tmp.matches(strEndRegex)) {

				continue;
			}

			validSegList.add(tmp);
		}

		if (validSegList == null || validSegList.size() == 0) {
			System.out.println("No subject be parsed out. Original subject="
					+ title);
			return null;
		}

		String longestSeg = getLongestSeg(validSegList);

		if (longestSeg == null || longestSeg.length() == 0) {
			longestSeg = "";
			for (int i = 0; i < titleArray.length; i++) {
				String tmp = titleArray[i];

				if (titleArray.length > 1 && (i == (titleArray.length - 1)))
					continue;

				longestSeg += tmp;
			}
		}

		return longestSeg;
	}

	public static String replaceChar(String strOld, char oldChar, char newChar) {
		String strNew = "";

		for (int i = 0; i < strOld.length(); i++) {
			char c = strOld.charAt(i);

			if (i > 0 && i < strOld.length() - 1) {
				char beforeChar = strOld.charAt(i - 1);
				char nextChar = strOld.charAt(i + 1);

				if (c == oldChar) {

					if (!isCharOrDigit(beforeChar) || !isCharOrDigit(nextChar)) {

						strNew += newChar;
						continue;
					}
				}
			}

			strNew += c;
		}

		return strNew;
	}

	public static boolean isCharOrDigit(char c) {
		if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'z')
				|| (c >= 'A' && c <= 'Z'))
			return true;
		else
			return false;
	}

	public static String filterSimpleHTML(final String s) {
		if (s == null)
			return null;

		String ns = s.trim();

		ns = ns.replaceAll("<[^>]*>", "");

		ns = ns.replaceAll("&lt;", "<");
		ns = ns.replaceAll("&gt;", ">");
		ns = ns.replaceAll("&quot;", "\"");
		ns = ns.replaceAll("&yen;", "￥");
		ns = ns.replaceAll("&ldquo;", "“");
		ns = ns.replaceAll("&rdquo;", "”");
		ns = ns.replaceAll("&#12288;", "");
		ns = ns.replaceAll("&nbsp;", "");

		ns = ns.replaceAll("&[\\S]+?;", "");

		ns = ns.trim();

		return ns;
	}

	private static String getLongestSeg(ArrayList<String> titleArray) {
		String newTitle = "";

		if (titleArray == null || titleArray.size() == 0)
			return null;

		int maxSegment = 0;
		for (int i = 0; i < titleArray.size(); i++) {
			String tmp = titleArray.get(i);

			if (tmp == null || tmp.trim().length() < 2)
				continue;

			tmp = tmp.trim();

			if (tmp.length() > maxSegment) {
				maxSegment = tmp.length();
				newTitle = tmp;
			}
		}

		return newTitle;
	}

	public static int getStringLength(String str) {
		int len = 0;
		if (str == null || str.length() == 0)
			return len;
		char chArray[] = str.toCharArray();

		int preCharType = 1;
		for (int i = 0; i < chArray.length; i++) {

			int tmp = getCharType(chArray[i]);

			if (i > 0)
				preCharType = getCharType(chArray[i - 1]);

			if (tmp == 0 && preCharType != 0) {

				len++;
			} else if (tmp == 2) {

				len++;
			}
		}
		return len;
	}

	private static int getCharType(char ch) {
		int result = 2;

		int tmp = (int) ch;
		if (((65 <= tmp) && (90 >= tmp)) || ((97 <= tmp) && (122 >= tmp))
				|| ((40 <= tmp) && (43 >= tmp)) || ((45 <= tmp) && (58 >= tmp))
				|| 64 == tmp) {

			result = 0;
		} else if (32 == tmp || 12288 == tmp) {

			result = 1;
		}
		return result;
	}

	public static void main(String[] args) {
		String title = "◤★◥乐高创新实现价值观 :: 大学生文网 :: po p p Essays & Papers of College & University tai yuan university of techology";

		title = "jjckbs@xinhuanet.com jjckb@vip.sina.com电话：";// +

		title = "实用工具&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
		title = "飞利浦[PHG]_美股实时行情_新浪财经";
		title = "【飞利浦手机大全】Philips手机报价_飞利浦手机大全图片及报价_手";
		title = "泰克推出首款面向MIPI M-PHY 3.1的测试解决方案";

		title = "A股再上演“蛇吞象”中纺投资182亿收购安信证券【中金视听】 - 中金视听 - 爆米花网 ";

		System.out.println("old=" + title);
		System.out.println("new=" + TitleAnalyzer.getStandardTitle(title));

	}
}
