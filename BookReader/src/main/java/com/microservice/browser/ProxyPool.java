package com.microservice.browser;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

import org.apache.log4j.Logger;

public class ProxyPool {

	private static Logger logger = Logger.getLogger(ProxyPool.class);

	private static ProxyPool instance;
	private ArrayList<ProxyHost> listProsyHost;
	private int order;

	private ProxyPool() {
		listProsyHost = new ArrayList<ProxyHost>();
		order = 0;
	}

	public void init(String strPath) {
		if ((strPath == null) || (strPath.trim().length() == 0)) {
			if (listProsyHost != null)
				listProsyHost.clear();
			return;
		}
		String lowerPath = strPath.toLowerCase();
		String proxyDefine = null;

		logger.info("ProxyHost Parse : " + strPath);
		if (lowerPath.startsWith("http://")) {
			proxyDefine = getProxyHostByUrl(strPath);
		} else {
			proxyDefine = getProxyHostByFile(strPath);
		}

		listProsyHost.clear();
		parseProxyHosts(proxyDefine, listProsyHost);
	}

	public synchronized static ProxyPool getInstance() {

		if (instance == null) {
			instance = new ProxyPool();
		}
		return instance;
	}

	public synchronized ProxyHost getRandomProxyHost() {
		ProxyHost ph = null;

		if ((listProsyHost != null) && (listProsyHost.size() > 0)) {
			ph = listProsyHost.get(Math.abs(new Random().nextInt())
					% listProsyHost.size());
			logger.info("RandomProxy IP:" + ph.getIp() + " Port:"
					+ ph.getPort());
		} else {
			logger.info("there is no RandomProxy server provide");
		}

		return ph;
	}

	public synchronized ProxyHost getNextProxyHost() {
		ProxyHost ph = null;
		if ((listProsyHost != null) && (listProsyHost.size() > 0)) {
			ph = listProsyHost.get(order % listProsyHost.size());
			logger.info("NextProxy IP:" + ph.getIp() + " Port:" + ph.getPort());
			order++;
		} else {
			logger.info("there is no NextProxy server provide");
		}
		return ph;
	}

	private String getProxyHostByUrl(String urlPath) {

		String content = "";

		try {

			URL url = new URL(urlPath);

			BufferedReader br = new BufferedReader(new InputStreamReader(
					url.openStream()));

			String s = "";

			StringBuffer sb = new StringBuffer("");

			while ((s = br.readLine()) != null) {

				sb.append(s.trim() + "\n");

			}

			br.close();

			content = sb.toString();

		} catch (Exception e) {

			e.printStackTrace();

		}

		return content;
	}

	private String getProxyHostByFile(String filePath) {

		String content = "";
		try {
			File file = new File(filePath);
			FileReader fileReader = new FileReader(file);
			BufferedReader br = new BufferedReader(fileReader);

			String temp = null;
			while ((temp = br.readLine()) != null) {
				content += temp.trim() + "\n";

			}
			br.close();
			fileReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return content;
	}

	private int parseProxyHosts(String strContent, ArrayList<ProxyHost> listPH) {
		if ((strContent == null) || (strContent.length() == 0))
			return 0;

		String[] proxydefine = strContent.split("\n");

		for (String ipandport : proxydefine) {
			try {
				String[] ipport = ipandport.split(":");

				String ip = ipport[0].trim();
				if ((ip == null) || (ip.length() < 8) || ip.indexOf(".") <= 0)
					continue;

				int port = 0;
				try {
					port = Integer.parseInt(ipport[1]);
				} catch (Exception e) {
					port = 0;
				}
				if (port <= 0)
					continue;

				ProxyHost ph = new ProxyHost();
				ph.setIp(ip);
				ph.setPort(port);

				listPH.add(ph);
			} catch (Exception e) {
				logger.error("Error parseProxyHosts :" + ipandport);
				e.printStackTrace();
			}

		}
		return listPH.size();
	}

	public static void main(String[] args) {

		ProxyPool.getInstance().init(
				"http://cs.chinaii.cn/sysAdmin/proxy/weiboProxy.htm");

		for (int i = 0; i < 100; i++) {

			ProxyHost ph = ProxyPool.getInstance().getNextProxyHost();
			if (ph != null)
				System.out.println("Host:" + ph.getIp() + " Port:"
						+ ph.getPort());
		}
	}

}
