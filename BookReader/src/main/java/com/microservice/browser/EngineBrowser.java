package com.microservice.browser;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.BasicClientConnectionManager;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.ImmediateRefreshHandler;
import com.gargoylesoftware.htmlunit.JavaScriptPage;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.ProxyConfig;
import com.gargoylesoftware.htmlunit.TextPage;
import com.gargoylesoftware.htmlunit.UnexpectedPage;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.microservice.util.IIMSUtil;



public class EngineBrowser {
	
	private static Logger logger = Logger.getLogger(EngineBrowser.class);
	

	HttpClient httpclient;
	

	private  WebClient webClient;

	String referer;

	String useragent;

	long interval = 0;

	long lastAccessTime;
	
	public EngineBrowser(String clientType,String useragent,String starturl,int nInterval,ProxyHost ph) {
		
		this.referer = "";
		this.useragent = useragent;
		
		httpclient = null;
		webClient = null;

		lastAccessTime =  0;
		interval = nInterval;
		
		if ( (clientType!=null) && (clientType.equalsIgnoreCase("htmlunit")==true) )
		{
			initHtmlUnit(ph);
			if ( (starturl!=null) && (starturl.length()>10) )
			{
				getHtmlUnitPage(starturl,"GET");
			}
		}
		else
		{
			initHttpClient(ph);
			if ( (starturl!=null) && (starturl.length()>10) )
			{
				getPage(starturl);
				
			}
		}
		lastAccessTime =  System.currentTimeMillis()/1000;

	}
	
	private void initHttpClient(ProxyHost ph)
	{

		BasicClientConnectionManager manager = new BasicClientConnectionManager();  

		httpclient = new DefaultHttpClient(manager);
		httpclient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);  
		httpclient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 20000);  

		httpclient.getParams().setParameter(org.apache.http.client.params.ClientPNames.COOKIE_POLICY, org.apache.http.client.params.CookiePolicy.BEST_MATCH);
				
		httpclient.getParams().setParameter(org.apache.http.params.CoreProtocolPNames.USER_AGENT, this.useragent);

		
		if ( (ph!=null) && (httpclient!=null) )
		{
			HttpHost proxy = new HttpHost(ph.getIp(), ph.getPort());
			httpclient.getParams().setParameter(ConnRouteParams.DEFAULT_PROXY, proxy);
		}
	}
	private void initHtmlUnit(ProxyHost ph)
	{
		webClient = new WebClient(BrowserVersion.CHROME);
		
		
		if ( (ph!=null) && (webClient!=null) )
		{
			ProxyConfig proxyConfig = new ProxyConfig();
			proxyConfig.setProxyHost(ph.getIp());
			proxyConfig.setProxyPort(ph.getPort());
			webClient.getOptions().setProxyConfig(proxyConfig);
		}
	}

	public   byte[] getPage(String searchUrl)
	 {

		checkInterval();
		
		
		
		 byte[] htmlPageBytes = null;
		 HttpGet httpGet = new HttpGet(searchUrl);

		 try{
			httpGet.setHeader("Accept", "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, application/x-silverlight, */*");
			httpGet.setHeader("Accept-Language", "zh-cn,zh;q=0.5");
			httpGet.setHeader("Referer", referer);
			httpGet.setHeader("Connection", "close");  
			
			HttpResponse response = httpclient.execute(httpGet); 
			
			if (response == null)
			{
				logger.error("response is null. url=" +  searchUrl);
				return null;
			}
    
	         if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
	         {
	           	HttpEntity entity = response.getEntity();
	

	     		htmlPageBytes = EntityUtils.toByteArray(entity);
	         }
	         else
	         {
	        	 HttpEntity entity = response.getEntity();

	             if (entity != null) {

	            	 httpGet.abort();
	             }
	        	logger.error("StatusCode:" + response.getStatusLine().getStatusCode() + searchUrl);
	         }
	         
	         logger.debug("referer:[" + httpGet.getFirstHeader("Referer").getValue() + "]"); 

	         AbstractHttpClient ahc = (AbstractHttpClient)httpclient;
	         List<Cookie> cookies = ahc.getCookieStore().getCookies();    
	         if (cookies.isEmpty()) {    
	        	 logger.debug("Cookies None");    
	         } else {    
	             for (int i = 0; i < cookies.size(); i++) {  
	            	 logger.debug("- " + cookies.get(i).toString());  
	                    
	             }    
	         }  

		} 
		catch (Exception e)
		{
			logger.error("getSearchHtml=" + searchUrl + "\tError=" + e);
		}
		finally
		{  

			httpGet.releaseConnection();
			httpclient.getConnectionManager().closeIdleConnections(2, TimeUnit.SECONDS);
		}  
		
		referer = searchUrl;
		return htmlPageBytes;
	 }
	

	public  String getHtmlUnitPage(String strUrl,String method)
	{

		checkInterval();
		

		webClient.getOptions().setRedirectEnabled(true);
		webClient.getOptions().setCssEnabled(false);
		webClient.getOptions().setActiveXNative(true);
		webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
		webClient.getOptions().setThrowExceptionOnScriptError(false);
		

		ImmediateRefreshHandler imRefreshHadler = new ImmediateRefreshHandler(); 
		webClient.setRefreshHandler(imRefreshHadler);

		webClient.getOptions().setJavaScriptEnabled(true);
		webClient.getOptions().setTimeout(25000);
		webClient.addRequestHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.22 (KHTML, like Gecko) Chrome/25.0.1364.172");
		
		webClient.getCookieManager().setCookiesEnabled(true);
		webClient.getCookieManager().toString();
		
		webClient.addRequestHeader("Referer", referer);
	
		Page page;
		String strHtml;
		try 
		{
			page = webClient.getPage(strUrl);
			try {
				Thread.sleep(8000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			if(page!=null)
			{
				
				if(page instanceof HtmlPage)
				{
					strHtml = ((HtmlPage)page).asXml();
				}
				else if(page instanceof JavaScriptPage)
				{
					strHtml =  ((JavaScriptPage)page).getContent();
				}
				else if(page instanceof TextPage)
				{
					strHtml =  ((TextPage)page).getContent();
				}
				else 
				{
					strHtml =  ((UnexpectedPage)page).getWebResponse().getContentAsString();
				}
				
				this.referer = strUrl;
				return strHtml;
				
			}
			
		} catch (Exception e) {
			logger.error("Error while process url=" + strUrl + e);
		}
		
		return "";
	}
	

	public  String getHtmlUnitPage(String strUrl,String method,String encode)
	{
		
		checkInterval();
		

		webClient.getOptions().setRedirectEnabled(true);
		webClient.getOptions().setCssEnabled(false);
		webClient.getOptions().setActiveXNative(true);
		webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
		webClient.getOptions().setThrowExceptionOnScriptError(false);
		

		ImmediateRefreshHandler imRefreshHadler = new ImmediateRefreshHandler(); 
		webClient.setRefreshHandler(imRefreshHadler);
		

		webClient.getOptions().setJavaScriptEnabled(true);
		webClient.getOptions().setTimeout(25000);
		webClient.addRequestHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.22 (KHTML, like Gecko) Chrome/25.0.1364.172");
		
		webClient.getCookieManager().setCookiesEnabled(true);
		webClient.getCookieManager().toString();
		logger.debug("getCookieManager" + webClient.getCookieManager().getCookies().toString());
		
		webClient.addRequestHeader("Referer", referer);
	

		HtmlPage page;
		String strHtml;
		try {

			page = webClient.getPage(strUrl);

			try {
				Thread.sleep(8000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			

			if(page!=null)
			{
				

				strHtml =  page.getWebResponse().getContentAsString(encode);
				
				
				this.referer = strUrl;
				return strHtml;
				
			}
		} catch (Exception e) {
			logger.error("Error while process url=" + strUrl + e);
		}
		
		return "";
	}

	
	public long getInterval() {
		return interval;
	}

	public void setInterval(int interval) {
		this.interval = interval;
	}
	
	public void checkInterval() {

		
		long nowInterval = System.currentTimeMillis()/1000 - this.lastAccessTime;
		long cha = this.interval - nowInterval;
		
		if (cha > 0)
		{
			logger.debug("EngineBrowser checkInterval ,Sleep Max " + cha  + " second");
			IIMSUtil.sleep((int)cha);
			
		}
		
		this.lastAccessTime = System.currentTimeMillis()/1000;
	}

	public HttpClient getHttpclient() {
		return httpclient;
	}

	

	public WebClient getWebClient() {
		return webClient;
	}

	

	/**
	 * @param args
	 */
	public static void main(String[] args) {

	}

}
