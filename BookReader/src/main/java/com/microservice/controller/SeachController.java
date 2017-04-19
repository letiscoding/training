package com.microservice.controller;

import com.microservice.data.*;
import com.microservice.util.*;
import com.microservice.webpage.BaiduWebpage;
import com.microservice.webpage.EngineSite;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.lexer.Page;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

@Controller
public class SeachController {

    @Autowired
	DataConfig dataConfig;
	@Autowired
	BookArgRepository bookArgRepository;

	@RequestMapping(value = "/down",method = RequestMethod.POST)
    @ResponseBody
	public Result<String> downLoad(@RequestParam String bookName,@RequestParam String bookUrl,@RequestParam String startTitle) throws UnsupportedEncodingException {
		boolean  isBegin =false;
		BookArg bookArg = bookArgRepository.findOne(dataConfig.getDataPath(),bookUrl);
        String file = dataConfig.getBookPath()+bookName+".txt";
        try {
            HttpAgent httpAgent = new HttpAgent();
            String sourceUrl = bookUrl;
			String[] webpage = httpAgent.getWebpage(sourceUrl);

            //获取目录html
            String srchtml =webpage[1];

            //获取目录所有链接
            MediaChannel curWebsite = new MediaChannel();
            curWebsite.setCharset("utf-8");
            ArrayList<String> allLinks = getAllHyperLinks(srchtml,curWebsite,sourceUrl);

            //判断本页面中是否有frame、iframe，如有，则进行hyperlink提取
            ArrayList<String> linksInFrames = getAllLinksinFrames(srchtml,curWebsite,sourceUrl,httpAgent);
            if (linksInFrames!=null && linksInFrames.size()>0)
                allLinks.addAll(linksInFrames);

            ArrayList<String> channelUrlList = parseMediaChannel(allLinks,sourceUrl,sourceUrl+bookArg.getChannelUrlRegex());

            String[] articlePage = null;
            String sourceHtml =null;
            String strTemp = null;
            String strTitle =null;
            for(String url:channelUrlList)
            {
                articlePage = httpAgent.getWebpage(url);
                sourceHtml = articlePage[1];
                if(sourceHtml==null ||sourceHtml.equals("")) continue;
                strTitle= RegexUtil.getMatchedStr(sourceHtml, bookArg.getContentTitleRegx(), null);
                if (startTitle!=null&&startTitle.length()>0)
                {
                    if(strTitle.indexOf(startTitle) !=-1)isBegin = true;
                    if(!isBegin) continue;
                }
                strTemp = RegexUtil.getMatchedStr(sourceHtml, bookArg.getContentRegx(), null);
                strTemp = strTemp.replaceAll("(<br>)", "\r\n");
                strTemp = strTemp.replaceAll("(<br />)", "\r\n");
                strTemp = strTemp.replaceAll("(<br/>)", "\r\n");
                strTemp = strTemp.replaceAll("(&nbsp;)", " ");

                System.out.println(strTitle);
                appendMethod(file,strTitle+"\r\n");
                appendMethod(file,strTemp);
            }

        }catch (Exception err)
        {
            return  new Result<String>(err.getMessage(),ErrorCode.BAD_REQUEST);
        }
		finally {
			fileWriterClose();
		}
		return new Result<String>(file,ErrorCode.SUCCESS);
	}

    @RequestMapping("/seach/{bookName}/{pageorder}")
    @ResponseBody
    public Result<ArrayList<Article>> seach(@PathVariable String bookName,@PathVariable int pageorder)
    {
        EngineSite engineSite = new EngineSite();
        engineSite.setName("baiduWebpage");
        engineSite.setStartUrl("https://www.baidu.com/s?wd=ABC");//http://www.baidu.com/s?q1=ABC&q2=&q3=&q4=&rn=50&lm=2&ct=0&ft=&q5=&q6=&tn=baiduadv");
        engineSite.setMaxPages(1);
        engineSite.setInterval(1);
        ArrayList<Article> allList = new ArrayList<Article>();
        try {
            BaiduWebpage baiduNews = new BaiduWebpage(engineSite);
            String keyword = bookName;
            for (int i = 1; i <= engineSite.getMaxPages(); i++) {
                ArrayList<Article> articleList = baiduNews.analyze(keyword, i);
                if (articleList != null) allList.addAll(articleList);
            }
        }
        catch (Exception err)
        {
            err.printStackTrace();
            return  new Result<ArrayList<Article>>(allList, ErrorCode.BAD_REQUEST);
        }
        return  new Result<ArrayList<Article>>(allList, ErrorCode.SUCCESS);
    }

    @RequestMapping("/seach")
    public String seach(){
        return "seach";
    }
	/*
	 * 解析出频道的URL
	 */
	private static ArrayList<String> parseMediaChannel(ArrayList<String>allLinks ,String sourceUrl,String channelUrlRegex)
	{
		Page page = new Page();
		page.setBaseUrl(sourceUrl);
		
		ArrayList<String> channelUrlList=new ArrayList<String>();
		for(int i=0;i<allLinks.size();i++)
		{
			String strUrl = allLinks.get(i);
	
			String channelUrl = null;
			if (channelUrlRegex!=null && channelUrlRegex.trim().length()>1)
			{
				if (strUrl.matches(channelUrlRegex))
				{
					channelUrl = strUrl;
					System.out.println("Channel Url=" + channelUrl);
					
					//替换掉匹配出来的字符中含有的一些无用字符
					if (!(  (channelUrl.indexOf("_") != -1)	|| (channelUrl.indexOf("'") != -1) || (channelUrl.indexOf(",") != -1)))
						channelUrl = channelUrl.replaceAll("'|,|_|\\(|\\)", "");
					
					if(!channelUrl.startsWith("http://"))
					{
						//频道url路径绝对化处理
						channelUrl = page.getAbsoluteURL(channelUrl);
					}
					channelUrlList.add(channelUrl);
				}
			}
		}
		return channelUrlList;
	}
	
	//特殊媒体频道Url预处理标记:如城市信报
	protected static String Special_ChannelUrl_Flag = "&#xA;| ";
	public static ArrayList<String> getAllHyperLinks(String srchtml, MediaChannel curWebsite, String sourceUrl)
	{
		ArrayList<String> urlList=new ArrayList<String>();
		
		String charset = curWebsite.getCharset();
		if(srchtml==null || charset==null || "".equals(charset))
			return null;
		
		//create HtmlParse
		Parser parser=Parser.createParser(srchtml, charset);
		try
		{
			//create Filter
			NodeFilter linkFilter = new NodeClassFilter(LinkTag.class);
            NodeList nodeList = parser.extractAllNodesThatMatch(linkFilter);

    		Page page = new Page();
    		page.setBaseUrl(sourceUrl);

			for(int i=0;i<nodeList.size();i++)
			{
				LinkTag link= (LinkTag) nodeList.elementAt(i);
				
				if (link == null)
					continue;
				
				if (!link.isHTTPLink())
					continue;
				
				//logger.debug("Url be extracted1="+link.extractLink());

				String extractLinkUrl = link.extractLink();	
				extractLinkUrl = extractLinkUrl.replaceAll(Special_ChannelUrl_Flag, "");
				//logger.debug("Url be extracted="+extractLinkUrl);
				
				if (extractLinkUrl.endsWith("&"))
					extractLinkUrl = extractLinkUrl.substring(0, extractLinkUrl.length()-1);
				
				String linkUrl=page.getAbsoluteURL(extractLinkUrl); //Url 路径绝对化处理
				
				//如果链接为空，则去掉
				if((linkUrl == null) || linkUrl.length()<1)
					continue;
				
				//链接url中文字符处理
				try 
				{
					linkUrl = StringUtil.encodeURL(linkUrl,charset);
				} catch (UnsupportedEncodingException e) {
					continue;
				}

				
				String strTitle = link.getAttribute("title");
				String linkSubject=link.getLinkText().trim();
				if (linkSubject==null || linkSubject.length()<1)
					linkSubject = strTitle;
				
				if (linkSubject!=null && linkSubject.length()>1)
				{
					if (curWebsite.getScanPanel().isValidLinkSubject(linkSubject))
						urlList.add(linkUrl);
					else
						System.out.println("\tInValid Channel=" +linkSubject + "\t" + linkUrl);
				}
			}
		}
		catch(ParserException e)
		{
			e.printStackTrace();
		}
		
		return urlList;
	}
	
	/**
	 * 如果本页面中包含frame、iframe，则获取对应子页面中的所有hyperlinks.
	 * 
	 */
	public static ArrayList<String> getAllLinksinFrames(String strHtml, MediaChannel curWebsite, String strBaseUrl, HttpAgent httpAgent)
	{
		
		ArrayList<String> frameUrls = getAllFrameUrls(strHtml,curWebsite,strBaseUrl);
		
		if (frameUrls == null || frameUrls.size()<1)
			return null;
		
		ArrayList<String> urlList = new ArrayList<String>();
		
		for (int i=0;i<frameUrls.size();i++)
		{
			String frameUrl = frameUrls.get(i);
			
			System.out.println("\tFrameUrl=" + frameUrl);
			
			String[] framepage = null;
			String frameHtml = null;
			try 
			{
				framepage = httpAgent.getWebpage(frameUrl);
				if (framepage == null)
					continue;
				
				//没有取到charset或者没有htmlSource
				if (framepage[0]==null || framepage[1]==null)
					continue;
				
				//String charset = webpage[0];
				frameHtml = framepage[1];
			} 
			catch (Exception e) 
			{
				System.out.println("\tFrame page can't be get. Url=" + frameUrl);
				continue;
			} 
			
			if (frameHtml==null)
			{
				System.out.println("\tFrame page can't be get. Url=" + frameUrl);
				continue;
			}

			String refreshUrl2  = framepage[3];
			if(refreshUrl2==null || refreshUrl2.equals(""))
				refreshUrl2 = frameUrl;
			
			urlList.addAll(getAllHyperLinks(frameHtml,curWebsite,refreshUrl2));
			
		}
		
		return urlList;
	}
	
	/**
	 * 获取当前网页中的Frameset、iframe中的链接，如果存在的话
	 * @param
	 * @return
	 */
	public static  ArrayList<String> getAllFrameUrls(String strHtml, MediaChannel curWebsite, String strBaseUrl)
	{
		ArrayList<String> frameUrls = null;
			
		if (strHtml == null)
			return null;
 
		String charset = curWebsite.getCharset();
		OrFilter orFilter = new OrFilter();
		TagNameFilter tagFilter[] = new TagNameFilter[2];
		tagFilter[0] = new TagNameFilter("iframe");
		tagFilter[1] = new TagNameFilter("frame");
		orFilter.setPredicates(tagFilter);

		Page page = new Page();
		page.setBaseUrl(strBaseUrl);
		
		try 
		{
			Parser parser = Parser.createParser(strHtml,charset);

			// 如果是解析的parser为null,则表明该网页抓取失败
			if (parser == null)
				return null;

            NodeList nodeList = parser.extractAllNodesThatMatch(orFilter);

            if (nodeList==null)
            	return null;
            
            frameUrls = new ArrayList<String>();
			for(int i=0;i<nodeList.size();i++)
			{
				org.htmlparser.nodes.TagNode link= (org.htmlparser.nodes.TagNode) nodeList.elementAt(i);
				String frameUrl = link.getAttribute("src");
				if (frameUrl ==null)
					continue;
				
				frameUrl = page.getAbsoluteURL(frameUrl); //Url 路径绝对化处理
				
				frameUrls.add(frameUrl);
				//logger.debug("frame node. src=" + frameUrl);
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		//2. 抽取
		
		return frameUrls;
	}
	
	 FileWriter writer;
	public  FileWriter writeCreator(String fileName)
	{
		try {
			if(writer!=null) return writer;
            //打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
            writer = new FileWriter(fileName, true);
            return writer;
        } catch (IOException e) {
            e.printStackTrace();
        }
		return null;
	}
	public  boolean appendMethod(String fileName, String content) {
        try {
            //打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
            FileWriter writer = writeCreator(fileName);
            writer.write(content);
          
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return  true;
    }
	
	private  void fileWriterClose()
	{
		try {
			writer.close();
			writer= null;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
