package com.microservice.util;

import java.net.URL;
import java.util.Random;

public class IIMSUtil {

	public IIMSUtil() {

	}

	public static String getIIMSNewsUrl(String url) {
		String strUrl = url;

		try {
			URL urlOperator = new URL(url);

			String host = urlOperator.getHost();
			String path = urlOperator.getPath();
			String query = urlOperator.getQuery();
			if (query == null)
				query = "";
			else
				query = "?" + query;

			int port = urlOperator.getPort();
			if (port == -1)
				strUrl = "http://" + host + ":80" + path + query;
			else
				strUrl = "http://" + host + ":" + port + path + query;
		} catch (Exception e) {
			strUrl = url;
		}

		return strUrl;
	}

	public static int sleep(int maxSleepTime) {
		int sleepTime = 0;

		if (maxSleepTime == 0)
			return sleepTime;

		try {

			Random r = new Random();

			sleepTime = r.nextInt(maxSleepTime) + 1;

			if (sleepTime < maxSleepTime / 2)
				sleepTime += maxSleepTime / 2;

			Thread.sleep(sleepTime * 1000);

		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return sleepTime;
	}

	public static void main(String[] args) {

		System.out.println("works start");
		System.out.println("sleep=" + sleep(10));
		System.out.println("works now");

	}
}
