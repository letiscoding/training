package com.microservice.util;

import org.apache.log4j.Logger;

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RunstatReporter {

	private static Logger logger = Logger.getLogger(RunstatReporter.class
			.getName());

	public String appName = "";
	public String reportURL = "";
	public String localIP = "";

	public String runingLogID = null;

	public static final String LogType_START = "1";
	public static final String LogType_MIDDLE = "2";
	public static final String LogType_END = "3";

	public static final String LogLevel_INFO = "1";
	public static final String LogLevel_ERROR = "2";
	public static final String LogLevel_FATAL = "3";

	private RunStatMsg runStatMsg = null;

	private ConcurrentHashMap<String, EngineStat> engineCountMap = new ConcurrentHashMap<String, EngineStat>();

	private static ConcurrentHashMap<String, Integer> logKeyMap = new ConcurrentHashMap<String, Integer>();

	public RunstatReporter() {
		runStatMsg = new RunStatMsg();
	}

	public void addLogKeyStat(String logKey, int count) {
		Integer logCount = logKeyMap.get(logKey);
		if (logCount == null) {
			logKeyMap.put(logKey, count);
		} else {
			logCount += count;
			logKeyMap.replace(logKey, logCount);
		}
	}

	public int getLogKeyStat(String key) {
		Integer v = logKeyMap.get(key);

		if (v == null)
			return 0;
		else
			return v.intValue();

	}

	public String getEngineRunningStat() {
		StringBuffer countBuffer = new StringBuffer();

		Enumeration<String> eum = engineCountMap.keys();
		while (eum.hasMoreElements()) {
			String key = eum.nextElement();
			EngineStat stat = engineCountMap.get(key);

			countBuffer.append(key + ":" + " " + stat.newCount + "/"
					+ stat.totalCount + ",");
		}

		return countBuffer.toString();
	}

	public synchronized void setStatCount(String engineName, int totalCount,
			int newCount) {
		EngineStat curEngineStat = engineCountMap.get(engineName);
		if (curEngineStat == null) {
			curEngineStat = new EngineStat();
			curEngineStat.engineName = engineName;
			curEngineStat.totalCount = totalCount;
			curEngineStat.newCount = newCount;

			engineCountMap.put(engineName, curEngineStat);
		} else {
			curEngineStat.totalCount += totalCount;
			curEngineStat.newCount += newCount;
		}
	}

	public HashMap<String, EngineStat> getEngineCountMap() {
		HashMap<String, EngineStat> engineMap = new HashMap<String, EngineStat>();

		Enumeration<String> eum = engineCountMap.keys();
		while (eum.hasMoreElements()) {
			String key = eum.nextElement();
			EngineStat stat = engineCountMap.get(key);

			engineMap.put(key, stat);
		}

		return engineMap;
	}

	public int setPara(String strKey, String strValue) {

		if ((strKey != null) && (strKey.length() > 0)) {

			runStatMsg.setPara(strKey, strValue);

		}
		return 0;

	}

	public int report(String logType, String logLevel) {
		int result = 0;

		if (appName == null || reportURL == null || appName.length() < 2)
			return -1;

		if (getRuningLogID() == null)
			return -1;

		if (logType.equalsIgnoreCase("1"))
			result = reportStart();

		if (logType.equalsIgnoreCase("2"))
			result = reportMiddle(logLevel);

		if (logType.equalsIgnoreCase("3"))
			result = reportEnd();

		runStatMsg.removeAllPara();

		return result;
	}

	private int reportStart() {

		RunStatMsg msg = runStatMsg;

		msg.setPara("RuningLogID", runingLogID);

		msg.setPara("AppName", appName);
		if ((localIP == null) || (localIP.trim().length() == 0))
			msg.setPara("IP", getLocalIP());
		else
			msg.setPara("IP", localIP);

		msg.setPara("LogType", RunstatReporter.LogType_START);
		msg.setPara("LogLevel", RunstatReporter.LogLevel_INFO);

		msg.setPara("LogDesc", "START");
		msg.setPara("LogTime", getNowDateSS());
		msg.setPara(
				"Module",
				Thread.currentThread().getStackTrace()[3].getClassName()
						+ "."
						+ Thread.currentThread().getStackTrace()[3]
								.getMethodName() + "()");

		postServer("json=" + msg.toString());
		logger.debug(msg.toString());
		return 1;

	}

	private int reportMiddle(String strLevel) {
		RunStatMsg msg = runStatMsg;

		if (getRuningLogID() == null)
			return -1;

		msg.setPara("RuningLogID", runingLogID);

		msg.setPara("AppName", appName);
		if ((localIP == null) || (localIP.trim().length() == 0))
			msg.setPara("IP", getLocalIP());
		else
			msg.setPara("IP", localIP);

		msg.setPara("LogType", RunstatReporter.LogType_MIDDLE);
		msg.setPara("LogLevel", strLevel);

		msg.setPara("LogDesc", "END");
		msg.setPara("LogTime", getNowDateSS());
		msg.setPara(
				"Module",
				Thread.currentThread().getStackTrace()[3].getClassName()
						+ "."
						+ Thread.currentThread().getStackTrace()[3]
								.getMethodName() + "()");

		postServer("json=" + msg.toString());
		logger.debug(msg.toString());

		return 1;

	}

	private int reportEnd() {
		RunStatMsg msg = runStatMsg;

		msg.setPara("RuningLogID", runingLogID);

		msg.setPara("AppName", appName);
		if ((localIP == null) || (localIP.trim().length() == 0))
			msg.setPara("IP", getLocalIP());
		else
			msg.setPara("IP", localIP);

		if (!this.engineCountMap.isEmpty())
			msg.setPara("EngineStat", toEngineStatJson());

		msg.setPara("LogType", RunstatReporter.LogType_END);
		msg.setPara("LogLevel", RunstatReporter.LogLevel_INFO);

		msg.setPara("LogDesc", "END");
		msg.setPara("LogTime", getNowDateSS());
		msg.setPara(
				"Module",
				Thread.currentThread().getStackTrace()[3].getClassName()
						+ "."
						+ Thread.currentThread().getStackTrace()[3]
								.getMethodName() + "()");

		postServer("json=" + msg.toString());

		return 1;

	}

	private String getRuningLogID() {
		if (runingLogID == null) {
			String strHash = appName + System.currentTimeMillis()
					+ Thread.currentThread().getId();
			runingLogID = "" + Math.abs(strHash.hashCode());

		}

		return runingLogID;
	}

	private String getNowDateSS() {
		SimpleDateFormat d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dd = d.format(new Date());
		return dd;
	}

	private int postServer(String strJson) {
		URL url = null;
		HttpURLConnection httpurlconnection = null;
		try {
			url = new URL(reportURL);

			httpurlconnection = (HttpURLConnection) url.openConnection();
			httpurlconnection.setConnectTimeout(3000);
			httpurlconnection.setDoOutput(true);
			httpurlconnection.setRequestMethod("POST");
			httpurlconnection.getOutputStream().write(strJson.getBytes());
			httpurlconnection.getOutputStream().flush();
			httpurlconnection.getOutputStream().close();
			int code = httpurlconnection.getResponseCode();
			logger.debug("getResponseCode= " + code);
		} catch (Exception e) {

			logger.error("Error while connecting to the statServer." + e);
		} finally {
			if (httpurlconnection != null)
				httpurlconnection.disconnect();
		}
		return 1;
	}

	public static String getLocalIP() {
		ArrayList<String> ips = new ArrayList<String>();

		String ip = "";

		try {
			Enumeration<?> e1 = (Enumeration<?>) java.net.NetworkInterface
					.getNetworkInterfaces();
			while (e1.hasMoreElements()) {
				java.net.NetworkInterface ni = (java.net.NetworkInterface) e1
						.nextElement();

				if ((!ni.getName().startsWith("eth"))
						&& (!ni.getName().startsWith("em")))
					continue;

				Enumeration<?> e2 = ni.getInetAddresses();
				while (e2.hasMoreElements()) {
					java.net.InetAddress ia = (java.net.InetAddress) e2
							.nextElement();
					if (ia instanceof java.net.Inet6Address)
						continue;

					ip = ia.getHostAddress();

					ips.add(ip);

				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		for (int i = 0; i < ips.size(); i++) {
			String strIP = ips.get(i);

			if (strIP.startsWith("192") || strIP.startsWith("172")
					|| strIP.startsWith("127") || strIP.startsWith("10."))
				continue;
			else {
				ip = strIP;
				break;
			}
		}

		return ip;
	}

	private String toEngineStatJson() {
		StringBuffer strJson = new StringBuffer(1024);

		strJson.append("{");
		strJson.append("EngineStatMap:");

		strJson.append("[");
		Enumeration<String> eum = this.engineCountMap.keys();
		while (eum.hasMoreElements()) {
			String key = eum.nextElement();
			EngineStat curEngineStat = this.engineCountMap.get(key);
			strJson.append("{");
			strJson.append(curEngineStat.toJson());
			strJson.append("}");
			if (eum.hasMoreElements() == true) {
				strJson.append(",");
			}

		}
		strJson.append("]");

		strJson.append("}");

		return strJson.toString();
	}

	public class EngineStat {

		private String engineName;

		private int totalCount;

		private int newCount;

		public String getEngineName() {
			return engineName;
		}

		public void setEngineName(String engineName) {
			this.engineName = engineName;
		}

		public int getTotalCount() {
			return totalCount;
		}

		public void setTotalCount(int totalCount) {
			this.totalCount = totalCount;
		}

		public int getNewCount() {
			return newCount;
		}

		public void setNewCount(int newCount) {
			this.newCount = newCount;
		}

		public String toJson() {
			StringBuffer strJson = new StringBuffer(128);

			strJson.append("engineStat:{");
			strJson.append("engineName:\"");
			strJson.append(this.engineName);
			strJson.append("\",");
			strJson.append("totalCount:\"");
			strJson.append(this.totalCount);
			strJson.append("\",");
			strJson.append("newCount:\"");
			strJson.append(this.newCount);
			strJson.append("\"}");

			return strJson.toString();
		}
	}

	class RunStatMsg {
		public String type = "json";
		public String version = "1.0";

		HashMap<String, String> ParaMap = null;

		public RunStatMsg() {
			ParaMap = new HashMap<String, String>();
			ParaMap.put("version", version);
			ParaMap.put("type", type);

		}

		public int setPara(String strKey, String strValue) {

			if ((ParaMap != null) && (ParaMap.get(strKey) == null)) {

				ParaMap.put(strKey, strValue);
			}
			return 0;

		}

		public int removeAllPara() {

			if (ParaMap != null)
				ParaMap.clear();

			return 0;
		}

		public String toString() {
			StringBuffer bufJson = new StringBuffer();

			bufJson.append("{");

			Iterator<String> it = ParaMap.keySet().iterator();

			while (it.hasNext()) {
				String k = it.next();
				String v = ParaMap.get(k);

				bufJson.append(k);

				if (v.startsWith("{") || v.endsWith("}")) {
					bufJson.append(":");
					bufJson.append(v);
					if (it.hasNext())
						bufJson.append(",");
				} else {
					bufJson.append(":\"");
					bufJson.append(v);
					if (it.hasNext())
						bufJson.append("\",");
					else
						bufJson.append("\"");
				}

			}

			bufJson.append("}");
			return bufJson.toString();
		}
	}

	public static void main(String[] args) {

		System.out.println("getLocalIP=" + getLocalIP());

		RunstatReporter runStatUtil = new RunstatReporter();

		runStatUtil.appName = "FatalReport";
		runStatUtil.reportURL = "http://112.84.182.51/sysAdmin/sysLog/AddSysLogs.jsp";

		runStatUtil.report(RunstatReporter.LogType_START,
				RunstatReporter.LogLevel_INFO);

		runStatUtil.setPara("Error", "����");
		runStatUtil.report(RunstatReporter.LogType_MIDDLE,
				RunstatReporter.LogLevel_ERROR);

		runStatUtil.setPara("INFO", "hehe");
		runStatUtil.report(RunstatReporter.LogType_MIDDLE,
				RunstatReporter.LogLevel_INFO);

		runStatUtil.setPara("FATAL", "û��������");
		runStatUtil.report(RunstatReporter.LogType_MIDDLE,
				RunstatReporter.LogLevel_FATAL);

		runStatUtil.setPara("StatNum", "58");
		runStatUtil.setPara("LogDesc", "������58��");
		runStatUtil.report(RunstatReporter.LogType_END,
				RunstatReporter.LogLevel_INFO);

	}

}
