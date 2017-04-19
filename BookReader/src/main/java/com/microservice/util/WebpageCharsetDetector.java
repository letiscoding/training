package com.microservice.util;

import org.mozilla.intl.chardet.*;

public class WebpageCharsetDetector {

	private boolean found = false;
	private String detectedCharset = null;

	public WebpageCharsetDetector() {

	}

	public String getDetectedCharset(byte[] htmlSourceByteArray) {
		String strCharset = null;

		if (htmlSourceByteArray == null)
			return null;

		int lang = nsPSMDetector.CHINESE;
		nsDetector det = new nsDetector(lang);

		det.Init(new nsICharsetDetectionObserver() {
			public void Notify(String charset) {
				found = true;
				detectedCharset = charset;

			}
		});

		byte[] buf = new byte[1024];
		boolean done = false;
		boolean isAscii = true;
		int i = 0;
		while (true) {
			if ((i * 1024 + 1024) > htmlSourceByteArray.length)
				break;

			if (1024 > buf.length)
				break;

			System.arraycopy(htmlSourceByteArray, i * 1024, buf, 0, 1024);

			int len = buf.length;

			if (isAscii)
				isAscii = det.isAscii(buf, len);

			if (!isAscii && !done)
				done = det.DoIt(buf, len, false);

			i++;

			if (len < 1024 || done)
				break;
		}

		det.DataEnd();

		if (isAscii) {

			detectedCharset = "ASCII";
			found = true;
		}

		if (found)
			strCharset = detectedCharset;

		return strCharset;
	}

	public static void main(String[] args) {
		WebpageCharsetDetector charsetDetector = new WebpageCharsetDetector();

		String url = "http://huanbao.bjx.com.cn/";

		url = "http://news.stheadline.com/dailynews/content_hk/2014/05/06/284564.asp";

		long start = System.currentTimeMillis();
		HttpAgent httpAgent = new HttpAgent();
		byte[][] htmlAndContentType = httpAgent.getPage(url);

		if (htmlAndContentType != null) {
			String responseCharset = null;
			String contentType = new String(htmlAndContentType[1]);
			System.out.println("contentType=" + contentType);

			int p = contentType.indexOf("charset=");
			if (p > 0) {
				responseCharset = contentType.substring(p + 8).trim();
				responseCharset = responseCharset.replaceAll("\"", "");
				responseCharset = responseCharset.replaceAll(";", "");
			}

			System.out.println("time1=" + (System.currentTimeMillis() - start));

			long start2 = System.currentTimeMillis();
			System.out
					.println("ResponseHeader charset="
							+ responseCharset
							+ "\tDetected Charset="
							+ charsetDetector
									.getDetectedCharset(htmlAndContentType[0]));
			long end = System.currentTimeMillis();

			System.out.println("time=" + (end - start2));

		}
	}
}
