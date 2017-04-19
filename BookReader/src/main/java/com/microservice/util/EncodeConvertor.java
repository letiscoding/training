package com.microservice.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

public class EncodeConvertor {

	private static String defaultCharset = "gbk";

	private static byte chrGBK[];

	private static byte chrBIG[];

	private static byte[] initByteArray(String filepath, int arrayLength)
			throws FileNotFoundException, IOException {
		byte bytes[] = new byte[arrayLength];
		InputStream is = null;

		try {
			is = EncodeConvertor.class.getResourceAsStream(filepath);
			if (is == null) {
				System.out.println("filepath=" + filepath
						+ ", but InputStream is null.");
				return null;
			}

			byte[] byteBuffer = new byte[100];
			int intLength = 0;

			int count = 0;// ͳ�ƶ�ȡ���ַ��ĸ���
			while ((intLength = is.read(byteBuffer)) != -1) {
				count = count + intLength;
				for (int i = 0; i < 100; i++) {
					System.arraycopy(byteBuffer, 0, bytes, (count - intLength),
							intLength);
				}
			}
		} catch (FileNotFoundException e) {
			throw e;
		} catch (IOException e) {
			throw e;
		} finally {

			if (is != null) {
				try {
					is.close();
				} catch (Exception e) {
				}
			}
		}
		return bytes;

	}

	public static String codeConvert(String text, String srcCharset,
			String desCharset) throws FileNotFoundException, IOException {
		String result = "";
		if (text == null || text.trim().equals(""))
			return result;

		if (srcCharset.equalsIgnoreCase("big5")
				&& (desCharset.equalsIgnoreCase("gbk") || desCharset
						.equalsIgnoreCase("gb2312"))) {
			result = big2Gbk(text);

		} else if (desCharset.equalsIgnoreCase("big5")
				&& (srcCharset.equalsIgnoreCase("gbk") || srcCharset
						.equalsIgnoreCase("gb2312"))) {
			result = gbk2Big(text);

		} else {
			try {
				result = new String(text.getBytes(srcCharset), desCharset);

			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public static String getHtmlCharset(byte[] htmlSourceByteArray) {

		String strHtml = new String(htmlSourceByteArray);

		String srcCharset = getCharsetFromPageSource(strHtml);

		if (srcCharset == null || srcCharset.trim().length() == 0)
			srcCharset = defaultCharset;

		return srcCharset;

	}

	public static String getCharsetFromPageSource(String strHtml) {
		String charset = null;

		int length = 2000;
		String str2Find = strHtml;
		if (strHtml.length() > length)
			str2Find = strHtml.substring(0, length);

		String strTmp = str2Find.toLowerCase();

		int iMeta = strTmp.indexOf("meta");
		if (iMeta == -1)
			return null;

		int iContent = strTmp.indexOf("content", iMeta + 1);
		if (iContent == -1)
			return null;

		int iCharset = strTmp.indexOf("charset", iContent + 1);

		if (iCharset == -1)
			return null;

		int iCharsetStart = strTmp.indexOf("=", iCharset + 1);
		if (iCharsetStart == -1)
			return null;

		int iMetaEnd = strTmp.indexOf(">", iCharsetStart + 1);
		if (iMetaEnd == -1)
			return null;

		charset = strTmp.substring(iCharsetStart + 1, iMetaEnd);
		if (charset == null)
			return null;

		charset = charset.replace("'", "");
		charset = charset.replace("\"", "");
		charset = charset.replace("/", "");
		charset = charset.replace("\\", "");

		charset = charset.replaceAll("[\\s]+", "");

		return charset;
	}

	public static String codeConvert(byte[] text, String charset)
			throws IOException {

		String result = "";

		if (charset.equalsIgnoreCase("big5")) {
			result = big2Gbk(text);
			return result;

		} else {
			try {
				result = new String(text, charset);

			} catch (Exception e) {

				result = new String(text, defaultCharset);
			}

		}

		return result;
	}

	public static String gbk2Big(String content) throws FileNotFoundException,
			IOException {
		String result = "";

		byte text[] = content.getBytes();
		result = gbk2Big(text);
		return result;
	}

	public static String gbk2Big(byte text[]) throws FileNotFoundException,
			IOException {
		if (chrBIG == null) {
			chrBIG = initByteArray("gbk-big/gbk2big.txt", 47880);
		}
		String result = "";
		int len = 0, i = 0;
		int ch1, ch2;
		len = text.length;

		byte desc[] = new byte[len];

		while (i < len) {
			ch1 = text[i];
			if (i + 1 == len) {
				desc[i] = (byte) ch1;
				break;
			}

			ch2 = text[i + 1];
			if (ch1 < 0)
				ch1 = ch1 + 256;
			if (ch2 < 0)
				ch2 = ch2 + 256;

			if (ch1 >= 0x81 && ch1 <= 0xfe)

			{
				if (((ch2 >= 0x40) && (ch2 < 0x7f))
						|| ((ch2 > 0x7f) && (ch2 <= 0xfe))) {
					int tmp = 2 * ((ch1 - 0x81) * 190 + (ch2 - 0x40) - (ch2 / 128));
					desc[i] = chrBIG[tmp];
					desc[i + 1] = chrBIG[tmp + 1];
					i = i + 2;
				} else {
					desc[i] = (byte) ch1;
					i++;
				}
			} else {
				desc[i] = (byte) ch1;
				i++;
			}
		}

		try {

			result = new String(desc, "big5");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return result;

	}

	public static String big2Gbk(String content) throws FileNotFoundException,
			IOException {
		String result = "";

		byte text[] = content.getBytes();
		result = big2Gbk(text);
		return result;
	}

	public static String big2Gbk(byte text[]) throws FileNotFoundException,
			IOException {
		if (chrGBK == null) {
			chrGBK = initByteArray("gbk-big/big2gbk.txt", 27946);
		}
		String result = "";

		int len = 0, i = 0;
		int ch1, ch2;
		len = text.length;

		byte desc[] = new byte[len];

		while (i < len) {
			ch1 = text[i];
			if (i + 1 == len) {
				desc[i] = (byte) ch1;
				break;
			}
			ch2 = text[i + 1];
			if (ch1 < 0)
				ch1 = ch1 + 256;
			if (ch2 < 0)
				ch2 = ch2 + 256;

			if (ch1 >= 0xa1 && ch1 <= 0xfe)

			{
				if (ch2 < 0xa1)
					ch2 = ch2 - 0x40;
				if (ch2 >= 0xa1)
					ch2 = ch2 - 0xa1 + 0x7e - 0x40 + 1;

				int tmp = 2 * ((ch1 - 0xa1) * 157 + ch2);

				try {
					desc[i] = chrGBK[tmp];
					desc[i + 1] = chrGBK[tmp + 1];
				} catch (Exception e) {
				}

				i = i + 2;
			} else {
				desc[i] = (byte) ch1;

				i++;
			}
		}

		try {

			result = new String(desc, "gbk");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static void main(String[] args) {

		String strContent = "fdsgfdg df><meta http-equiv='Content-Type' content =' text/html; charset = utf -8 \" / >dsgdsf";

		System.out
				.println(EncodeConvertor.getCharsetFromPageSource(strContent));

	}
}
