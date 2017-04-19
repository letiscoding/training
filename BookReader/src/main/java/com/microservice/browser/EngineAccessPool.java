package com.microservice.browser;

import java.util.Vector;
import java.util.concurrent.Semaphore;
import org.apache.log4j.Logger;

import com.microservice.util.RegexUtil;

public class EngineAccessPool {

	private static Logger logger = Logger.getLogger(EngineAccessPool.class);

	private Vector<EngineBrowser> pool = null;

	private Semaphore pass = null;
	private int clientNum = 0;

	public EngineAccessPool() {
		this.clientNum = 0;

		pool = new Vector<EngineBrowser>();

		pass = new Semaphore(0);

	}

	public void add(EngineBrowser engineBrowser) {
		if (engineBrowser != null) {
			pool.add(engineBrowser);
			this.clientNum++;

			pass.release();
		}
	}

	public EngineBrowser get() throws InterruptedException {

		logger.debug("Try to get a pass...");
		pass.acquire();
		logger.debug("Got a pass");
		return getResource();
	}

	private EngineBrowser getResource() {
		EngineBrowser result = pool.remove(0);
		logger.debug("EngineBrowser " + result + "put out");
		return result;
	}

	public void put(EngineBrowser resource) {
		logger.debug("Released a pass");

		releaseResource(resource);

		pass.release();
	}

	private void releaseResource(EngineBrowser resource) {
		logger.debug("EngineBrowser " + resource + " return");
		pool.add(resource);
	}

	public int getClientNum() {
		return clientNum;
	}

	public static void main(String[] args) {

		EngineAccessPool engineAccessPool = new EngineAccessPool();

		EngineBrowser engineBrowser = null;

		for (int i = 0; i < 5; i++) {
			engineBrowser = new EngineBrowser("httpclient", "aa" + i, "", 10,
					null);
			engineAccessPool.add(engineBrowser);

		}

		try {

			for (int i = 0; i < 10; i++) {
				engineBrowser = engineAccessPool.get();
				byte[] pageHtml = engineBrowser
						.getPage("http://whatsmyuseragent.com/");

				if (pageHtml != null) {

					String strPageSource = new String(pageHtml, "utf-8");
					String strAA = RegexUtil.getMatchedStr(strPageSource,
							"(aa[\\d]+)", 1);
					logger.debug(strAA);
				}

				engineAccessPool.put(engineBrowser);
			}
		} catch (Exception e) {

			e.printStackTrace();
		}

	}

}
