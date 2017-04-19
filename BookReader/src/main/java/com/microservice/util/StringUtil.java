package com.microservice.util;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;

public class StringUtil {

	public static String LINE_SEPARATOR = "\n";

	public StringUtil() {

	}

	public static Logger logger = Logger.getLogger(StringUtil.class);

	public static boolean isContentPage(String url) {
		boolean isLeagal = true;

		if (url == null)
			return false;

		if (url.endsWith("/"))
			return false;

		try {
			URL urlPage = new URL(url);

			String fileName = urlPage.getFile();

			if (fileName.length() <= 1 && (urlPage.getPath().length() <= 1)) {
				System.out.println("No pathfile exist Url:[" + url + "]");
				return false;
			} else if (fileName.matches(".*?(index|default)[^/]*\\..*?"))
				return false;
		} catch (MalformedURLException e) {
			return false;
		}

		return isLeagal;
	}

	public static String getMD5(byte[] source) {
		String s = null;

		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'a', 'b', 'c', 'd', 'e', 'f' };
		try {
			MessageDigest md = MessageDigest
					.getInstance("MD5");
			md.update(source);

			byte tmp[] = md.digest();
			char str[] = new char[16 * 2];

			int k = 0;
			for (int i = 0; i < 16; i++) {
				byte byte0 = tmp[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf];
			}
			s = new String(str);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return s;
	}

	public static String getPlainText(String strHtml) {
		String str = null;

		String regEx_html = "<[^>]+>";

		java.util.regex.Pattern pattern;
		java.util.regex.Matcher matcher;

		pattern = java.util.regex.Pattern.compile(regEx_html,
				java.util.regex.Pattern.CASE_INSENSITIVE);
		matcher = pattern.matcher(strHtml);

		str = matcher.replaceAll("");

		str = str.replaceAll("&gt;&gt;", "��");
		str = str.replaceAll("&nbsp;", "��");
		str = str.replaceAll("&mdash;", "����");

		return str;
	}

	public static String getEscapeCharacter(String str) {
		String s = new String(str);
		s = s.replaceAll("&lt;", "<");
		s = s.replaceAll("&gt;", ">");
		s = s.replaceAll("&quot;", "\"");
		s = s.replaceAll("&yen;", "��");
		s = s.replaceAll("&ldquo;", "��");
		s = s.replaceAll("&rdquo;", "��");
		s = s.replaceAll("&#12288;", " ");
		s = s.replaceAll("&nbsp;", " ");
		s = s.replaceAll("&mdash;", "����");
		s = getEscapeCharacter2Chinese(s);
		return s;
	}

	public static String getEscapeCharacter2Chinese(String str) {
		if (str == null)
			return null;

		StringBuffer buffer = new StringBuffer();
		String[] splitArr = str.split("&#");
		if (splitArr != null && splitArr.length > 0) {
			for (int i = 0; i < splitArr.length; i++) {
				String splitStr = splitArr[i];

				if (splitStr != null && !splitStr.equals("")) {
					String es = splitStr;
					int fi = splitStr.indexOf(";");
					if (fi > 0)
						es = splitStr.substring(0, fi);

					try {
						int ascii = Integer.parseInt(es);

						char ch = (char) ascii;
						buffer.append(ch);
					} catch (NumberFormatException e) {
						buffer.append(splitStr);
						continue;
					}

					if (fi > 0)
						buffer.append(splitStr.substring(fi + 1));
				}
			}

		}
		return buffer.toString();
	}

	public static String removeEscapeCharacter(String strHtml) {
		if (strHtml == null)
			return null;

		String regEx_script = "<[\\s]*?script[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?script[\\s]*?>";

		java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
				regEx_script, java.util.regex.Pattern.CASE_INSENSITIVE);
		java.util.regex.Matcher matcher = pattern.matcher(strHtml);
		strHtml = matcher.replaceAll("");

		String regEx_html = "<[^>]+>";
		pattern = java.util.regex.Pattern.compile(regEx_html,
				java.util.regex.Pattern.CASE_INSENSITIVE);
		matcher = pattern.matcher(strHtml);
		strHtml = matcher.replaceAll("");

		strHtml = strHtml.replaceAll("&#[0-9]{1,5};?", "");
		strHtml = strHtml.replaceAll("&[a-z]{1,6};?", "");
		strHtml = strHtml.replaceAll("&#[a-z]{1,5};?", "");
		strHtml = strHtml.replaceAll("&[0-9]{1,5};?", "");
		strHtml = strHtml.replaceAll("document.write.*;", "");

		strHtml = strHtml.replaceAll("\t", "");
		strHtml = strHtml.replaceAll("\n", "");
		strHtml = strHtml.replaceAll("\r", "");

		return strHtml.trim();
	}

	public static boolean isChinese(String source) {
		if (source == null)
			return false;
		for (int i = 0; i < source.length(); i++) {
			char chr = source.charAt(i);
			int value = (int) chr;
			if ((value >= 0x2E80 && value <= 0x9FFF)
					|| (value >= 0xE800 && value <= 0xE87F)
					|| (value >= 0xF900 && value <= 0xFAFF)

					|| (value >= 0xFF00 && value <= 0xFF5E)) {
				return true;
			}
		}
		return false;
	}

	public static String iso2gbk(String str) {
		String value = "";
		if (str == null || str.length() == 0) {
			return "";
		}

		try {
			value = new String(str.getBytes("ISO8859_1"), "GBK");
		} catch (Exception e) {
			return null;
		}
		return value;
	}

	public static String iso2xxx(String str, String charSet) {
		String value = "";
		if (str == null || str.length() == 0) {
			return "";
		}

		try {
			value = new String(str.getBytes("ISO8859_1"), charSet);
		} catch (Exception e) {
			return null;
		}
		return value;
	}

	public static String gbk2xxx(String str, String charSet) {
		String value = "";
		if (str == null || str.length() == 0) {
			return "";
		}

		try {
			value = new String(str.getBytes("GBK"), charSet);
		} catch (Exception e) {
			return null;
		}
		return value;
	}

	public static String xxx2xxx(String str, String fromcharSet, String charSet) {
		String value = "";
		if (str == null || str.length() == 0) {
			return "";
		}

		try {
			value = new String(str.getBytes(fromcharSet), charSet);
		} catch (Exception e) {
			return null;
		}
		return value;
	}

	public static String gbk2iso(String str) {
		String value = "";
		if (str == null || str.length() == 0) {
			return "";
		}

		try {
			value = new String(str.getBytes("GBK"), "ISO8859_1");
		} catch (Exception e) {
			return value;
		}
		return value;
	}

	public static String gbk2utf8(String strChinese) {
		String ret = null;

		char c[] = strChinese.toCharArray();
		byte[] fullByte = new byte[3 * c.length];

		for (int i = 0; i < c.length; i++) {
			int m = (int) c[i];

			String word = Integer.toBinaryString(m);

			StringBuffer sb = new StringBuffer();
			int len = 16 - word.length();

			for (int j = 0; j < len; j++) {
				sb.append("0");
			}

			sb.append(word);
			sb.insert(0, "1110");
			sb.insert(8, "10");
			sb.insert(16, "10");

			String s1 = sb.substring(0, 8);
			String s2 = sb.substring(8, 16);
			String s3 = sb.substring(16);

			byte b0 = Integer.valueOf(s1, 2).byteValue();
			byte b1 = Integer.valueOf(s2, 2).byteValue();
			byte b2 = Integer.valueOf(s3, 2).byteValue();
			byte[] bf = new byte[3];
			bf[0] = b0;
			fullByte[i * 3] = bf[0];
			bf[1] = b1;
			fullByte[i * 3 + 1] = bf[1];
			bf[2] = b2;
			fullByte[i * 3 + 2] = bf[2];
		}

		if (fullByte != null) {
			try {
				ret = new String(fullByte, "UTF-8");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return ret;
	}

	public static String toUnicode(String s) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) <= 256) {
				sb.append(s.charAt(i));
			} else {
				sb.append("$u");
				sb.append(Integer.toHexString(s.charAt(i)));
			}
		}

		return sb.toString();
	}

	public static String decodeUnicode(String dataStr) {
		int index = 0;

		if (dataStr == null)
			return null;

		if (dataStr.length() < 2)
			return dataStr;

		StringBuffer buffer = new StringBuffer();

		for (index = 0; index < dataStr.length() - 1;) {

			char charStr = dataStr.charAt(index);
			char nextChar = dataStr.charAt(index + 1);

			if (charStr == '\\' && nextChar == 'u') {
				String theStr = dataStr.substring(index + 2, index + 6);

				char letter = (char) Integer.parseInt(theStr, 16);
				buffer.append(new Character(letter).toString());

				index = index + 6;
			} else {
				buffer.append(charStr);
				index++;
			}
		}

		if (index > 0)
			buffer.append(dataStr.charAt(index));

		return buffer.toString();
	}

	public static String encodeURL(String url, String encode)
			throws UnsupportedEncodingException {
		StringBuilder sb = new StringBuilder();
		StringBuilder noAsciiPart = new StringBuilder();
		for (int i = 0; i < url.length(); i++) {
			char c = url.charAt(i);
			if (c > 255) {
				noAsciiPart.append(c);
			} else {
				if (noAsciiPart.length() != 0) {
					sb.append(URLEncoder.encode(noAsciiPart.toString(), encode));
					noAsciiPart.delete(0, noAsciiPart.length());
				}
				sb.append(c);
			}
		}
		return sb.toString();
	}

	public static int convertStr2Int(String str) {
		int intValue = 0;

		if (str == null || str.trim().equals(""))
			return intValue;

		try {
			intValue = Integer.parseInt(str.trim());
		} catch (Exception e) {
			intValue = 0;
		}
		return intValue;
	}

	public static int getValidLength(String source) {
		int validLength = 0;

		StringBuffer validChars = new StringBuffer();

		if (source == null)
			return 0;

		if (isChinese(source)) {

			for (int i = 0; i < source.length(); i++) {
				char chr = source.charAt(i);
				int value = (int) chr;
				if ((value >= 0x2E80 && value <= 0x9FFF)
						|| (value >= 0xE800 && value <= 0xE87F)
						|| (value >= 0xF900 && value <= 0xFAFF)

						|| (value >= 0xFF00 && value <= 0xFF5E)) {
					validChars.append(chr);
				}
			}

			validLength = validChars.length();
		} else {
			String terms[] = source.split("\\s");
			int len = 0;
			for (int i = 0; i < terms.length; i++) {
				String strTemp = terms[i].trim();
				if (strTemp.length() <= 1)
					continue;

				validChars.append(terms[i].trim() + " ");
				len++;
			}

			validLength = len;
		}

		return validLength;
	}

	public static String parseSqlString(String strSql) {
		if (strSql == null)
			return null;

		strSql = strSql.replaceAll("'", "''");

		return strSql;
	}

	public static boolean hasErrorGBKCode(String source) {

		int countLuanma = 0;
		if (source == null)
			return false;

		for (int i = 0; i < source.length(); i++) {
			char chr = source.charAt(i);
			int value = (int) chr;

			if ((value >= 0x2E80 && value <= 0x9FFF)

			|| (value >= 0xE800 && value <= 0xE87F)

			|| (value >= 0xF900 && value <= 0xFAFF)

			|| (value >= 0xFF00 && value <= 0xFF5E)

			|| (value >= 0x2000) && (value <= 0x206F)) {

				;
			} else {
				if (value <= 0x007f) {

					;
				} else {

					countLuanma++;
					break;
				}

			}
		}

		if (countLuanma > 0)
			return true;
		else
			return false;
	}

	public static String repaceWithLatestDay(String homeUrl, int maxBeforeDays) {
		String newHomeUrl = homeUrl;

		if (homeUrl == null)
			return null;

		int p1 = homeUrl.indexOf("yyyy");
		if (p1 == -1)
			return newHomeUrl;

		Date today = new Date();

		int counter = 0;
		while (counter < maxBeforeDays) {

			String strTodo = homeUrl.substring(p1);

			GregorianCalendar gc = new GregorianCalendar();
			gc.setTime(today);
			gc.add(Calendar.DAY_OF_MONTH, -counter);

			counter++;

			SimpleDateFormat format = new SimpleDateFormat("yyyy");
			String curYear = format.format(gc.getTime());

			format = new SimpleDateFormat("MM");
			String curMonth = format.format(gc.getTime());

			format = new SimpleDateFormat("dd");
			String curDay = format.format(gc.getTime());

			strTodo = strTodo.replaceAll("yyyy", curYear);
			strTodo = strTodo.replaceAll("MM", curMonth);
			strTodo = strTodo.replaceAll("dd", curDay);

			newHomeUrl = homeUrl.substring(0, p1) + strTodo;

			try {
				HttpGet httpGet = new HttpGet(newHomeUrl);

				CloseableHttpClient httpclient = HttpClients.createDefault();
				CloseableHttpResponse httpResponse = httpclient
						.execute(httpGet);
				int stateCode = httpResponse.getStatusLine().getStatusCode();

				if (stateCode == 200) {

					return newHomeUrl;
				}
			} catch (Exception e) {
				;
			}
		}

		return homeUrl;
	}

	public static String getStandardSQLINStr(String str) {
		String cleanstr = null;

		if (str == null)
			return null;

		String ts[] = str.split(",|��|;|��");
		if (ts == null)
			return null;

		for (String ttos : ts) {
			if (ttos == null)
				continue;

			ttos = ttos.trim();
			if (cleanstr == null)
				cleanstr = ttos;
			else
				cleanstr += "," + ttos;
		}

		return cleanstr;
	}

	public final static String getMD5Encode(String s) {
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'a', 'b', 'c', 'd', 'e', 'f' };

		try {
			byte[] strTemp = s.getBytes();
			MessageDigest mdTemp = MessageDigest.getInstance("MD5");
			mdTemp.update(strTemp);
			byte[] md = mdTemp.digest();
			int j = md.length;
			char str[] = new char[j];
			int k = 0;
			for (int i = 0; i < j / 2; i++) {
				byte byte0 = md[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf];
			}

			return new String(str);
		} catch (Exception e) {
			return null;
		}
	}

	public static String getHost(String url) {
		if (url == null)
			return null;

		String host = null;
		try {
			if (!url.startsWith("http://"))
				url = "http://" + url;

			URL u = new URL(url);
			host = u.getHost();
		} catch (Exception e) {
			System.out.println("Error while get host." + e);
		}
		return host;
	}

	public static String getDomain(String url) {
		String domain = null;

		String host = getHost(url);

		if (host == null)
			return null;

		boolean isNotDigitalHost = false;
		for (int i = 0; i < host.length(); i++) {
			char ic = host.charAt(i);
			if (!(Character.isDigit(ic) || ic == '.')) {
				isNotDigitalHost = true;
				break;
			}
		}

		if (!isNotDigitalHost)
			return host;

		String[] hostElements = host.split("\\.");

		int i = 0;
		for (i = 0; i < hostElements.length; i++) {
			String element = hostElements[i];

			if (element
					.matches("(com|net|org|gov|cn|tv|mobi|co|cc|biz|tel|info|me|edu)")) {

				if (i == hostElements.length - 1)
					break;

				int k = i + 1;
				if ((k < hostElements.length)
						&& hostElements[k]
								.matches("(com|net|org|gov|cn|tv|mobi|co|cc|biz|tel|info|me|edu)"))
					break;
			}
		}

		if (i == 0)
			return host;

		i--;

		for (; i < hostElements.length; i++) {
			String element = hostElements[i];

			if (domain == null)
				domain = element + ".";
			else {
				if (hostElements.length - 1 == i)
					domain += element;
				else
					domain += element + ".";
			}
		}

		return domain;
	}

	public static void main(String[] args) {
		System.out
				.println(getEscapeCharacter2Chinese("&#12288;<a href=\"/ilink/cnenterprise/news-event/news/news-list/HW_277456\">&gt;NBA&#21326;&nbsp;&#20026;kdgfdsg&#20113;&#25805;&#20316;&#31995;&#32479;FusionSphere&#33719;SPECvirt&#27979;&#35797;&#39640;&#20998;&#65292;&#34394;&#25311;&#21270;&#24615;&#33021;&#19994;&#30028;&#26368;&#20339;</a>"));

		System.out.println("domain=" + getDomain("http://www.23.25.com"));
		System.out.println("domain=" + getDomain("http://www.23.25.88"));
		System.out.println("domain=" + getDomain("http://11.23.25.88"));
		System.out.println("domain=" + getDomain("http://11.www.com.cn"));
		System.out.println("domain=" + getDomain("http://www.com.tv.cn"));
		System.out.println("domain="
				+ isContentPage("http://scse.buaa.edu.cn/aa"));

		System.out.println(getStandardSQLINStr("'abc ','aaa'"));
		System.out.println(getStandardSQLINStr("1;2,3"));

		System.out.println(getValidLength("����ABC"));

		String homeUrl = "http://www.hmrb.com.cn/test/hmrb/html/yyyy-MM/dd/node_1.htm";

		System.out.println(repaceWithLatestDay(homeUrl, 10));
	}
}
