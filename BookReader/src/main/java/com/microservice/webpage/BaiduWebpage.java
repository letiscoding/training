package com.microservice.webpage;

import com.microservice.browser.EngineBrowser;
import com.microservice.browser.ProxyPool;
import com.microservice.browser.UserAgent;
import com.microservice.data.Article;
import com.microservice.spider.TitleAnalyzer;
import com.microservice.util.DateUtil;
import com.microservice.util.RegexUtil;
import com.microservice.util.StringUtil;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.Socket;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class BaiduWebpage extends Engine {

    private Logger logger = Logger.getLogger(BaiduWebpage.class);
    /**
     * 为减少统计，默认就是1天
     */
    private int dayBefore = 1;

    public BaiduWebpage(EngineSite engineSite) {
        super(engineSite);
        this.orderType = 1;
        // 添加一个统计的时间戳，统计最近多长时间的内容
        String stf = "&gpc=stf=%s,%s";
        long cur_time = System.currentTimeMillis() / 1000;
        // 默认是一天内的数据就可以了
//        if (engineSite.getStartUrl().contains("gpc") == false) {
//            engineSite.setStartUrl(engineSite.getStartUrl()
//                    + String.format(stf, String.valueOf(cur_time - 86400 * 7),
//                    String.valueOf(cur_time)) + "%7Cstftype=1&tfflag=1");
//        }
        curEngineBrowser = new EngineBrowser("httpclient",
                UserAgent.getRandomProxyHost(), "", engineSite.getInterval(),
                ProxyPool.getInstance().getRandomProxyHost());
        logger.debug("Engine=" + engineSite.getName());
    }

    /**
     * 对传递过来的搜索词进行抓取，并分析
     *
     * @param keyword   搜索关键词
     * @param pageorder 当前查询的是第几页
     * @return
     */
    @Override
    public ArrayList<Article> analyze(String keyword, int pageorder) {
        // 获取网页的正文
        String strHtml = getPage(keyword, pageorder);
        // 解析正正文的内容
        ArrayList<Article> artilceList = parse(strHtml);

        return artilceList;
    }

    /**
     *
     * @param newsUrl
     * @return
     */
    public ArrayList<Article> getSimilarArticles(String newsUrl) {
        ArrayList<Article> similarArticles = new ArrayList<Article>();

        return similarArticles;
    }

    private ArrayList<Article> parse(String strHtml) {
        if (strHtml == null)
            return null;
        ArrayList<Article> articleList = new ArrayList<Article>();
        Document doc = Jsoup.parse(strHtml, "https://www.baidu.com/");
        Elements links = doc.select("div[class^=result c-container]");
        for (Element e : links) {
            try {
                Article item = parseResultItem(e);
                if (item != null)
                    articleList.add(item);
            } catch (Exception e1) {
                logger.error("Exception :" + e1);
            }
        }

        nextPageUrl = null;
        Elements eltNextPages = doc.getElementsMatchingOwnText("下一页");
        for (Element eltNextPage : eltNextPages) {
            if (eltNextPage != null
                    && eltNextPage.attr("abs:href").length() > 7) {
                nextPageUrl = eltNextPage.attr("abs:href");
                logger.info("nextPageUrl=" + nextPageUrl);

                break;
            }
        }

        return articleList;

    }

    private Article parseResultItem(Element parent) {
        Article article = new Article();
        // 获取解析的大标题，通过大标题来解析文本的title
        Elements links = parent.select("h3.t > a");
        if ((links == null) || (links.size() == 0))
            return null;
        Element eltSubject = links.first();
        String strSubject =eltSubject.text();// TitleAnalyzer.getStandardTitle(eltSubject.text());
        article.setSubject(strSubject);
        String url = eltSubject.attr("href");

        if (url.startsWith("http://www.baidu.com"))
            url = url.substring("http://www.baidu.com".length());

        Elements digests = parent.select("div.c-abstract");
        if ((digests == null) || (digests.size() == 0))
            return null;

        Element eltDigest = digests.first();

        Elements timeNodes = eltDigest
                .select("span[class*=newTimeFactor_before_abs]");
        String strBaiduTime = null;
        Element timeNode = null;
        if ((timeNodes != null) && (timeNodes.size() > 0)) {
            timeNode = timeNodes.first();
            strBaiduTime = timeNode.text();
        } else {
            strBaiduTime = eltDigest.text();
        }

//        String baiduTimeRegex = "(\\d{4}-\\d{1,2}-\\d{1,2}|\\d+?小时前|\\d+?分钟前|\\d+?天内|\\d+?天前)";
//        try {
//            strBaiduTime = RegexUtil.getMatchedStr(strBaiduTime,
//                    baiduTimeRegex, null);
//
//            if ((strBaiduTime != null) && (strBaiduTime.indexOf("天内") > 0))
//                strBaiduTime = "12小时前";
//            String strRealTime = DateUtil.getFormattedTime(strBaiduTime);
//            article.setPostTime(strRealTime);
//
//            if ((strBaiduTime != null) || (strRealTime != null)) {
//
//                boolean isIntime = DateUtil.isInTime(article.getPostTime(),
//                        dayBefore);
//                logger.debug("Intime=" + isIntime + " strBaiduTime="
//                        + strBaiduTime + "\t" + article.getPostTime());
//                if (isIntime == false)
//                    return null;
//            }
//        } catch (Exception e) {
//            ;
//        }

        String redirectUrl = getRedirectUrlOwn(url);

        logger.error("[redirectUrl]:" + url);
        article.setUrl(redirectUrl);

//        if (!isLeagalNews(article))
//            return null;
//
//        if (timeNode != null)
//            timeNode.remove();

        String strDigest = getCleanDigest(eltDigest.text());

        strDigest = strDigest.replace("...", "");

        article.setDigest(strDigest.trim());
        logger.debug("Subject :" + article.getSubject() + "\tUrl  :["
                + article.getUrl() + "]\n\t\t\t\t\t\t\t\t" + "Digest :"
                + article.getDigest());

        article.setAuthor("");

        article.setEngineName(this.engineSite.getName());
        article.setWebName(StringUtil.getHost(article.getUrl()));

        if ((article.getWebName() == null)
                || (article.getWebName().length() < 2))
            return null;

        if ((article.getSubject() == null)
                || (article.getSubject().length() < 2))
            return null;

        if ((article.getUrl() == null) || (article.getUrl().length() < 2))
            return null;

        if (article.getPostTime() == null) {

            String dd = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
                    .format(new Date());
            article.setPostTime(dd);
        }

        article.setUnicodeString(article.getUrl());
        article.setEngineName(this.engineSite.getName());

        return article;
    }

    public String getCleanDigest(String strDigest) {
        if (strDigest == null)
            return "";

        String digest = strDigest;

        digest = digest.replaceAll("<[a-zA-Z]+[1-9]?[^><]*>", "");
        if (digest != null)
            digest = digest.replaceAll("</[a-zA-Z]+[1-9]?>", "");

        if (digest != null)
            digest = digest.replaceAll("\\{[^}]*\\}", "");

        if (digest != null)
            digest = digest.replaceAll("\\([^)]*\\)", "");

        if (digest != null)
            digest = digest.replaceAll("\\)", "");

        if (digest != null)
            digest = digest.replaceAll("\\}", "");

        if (digest == null)
            return "";

        return digest.trim();
    }

    /**
     * 获取的网页的正文信息
     *
     * @param keyword   搜索的关键词
     * @param pageorder 传入第几页的内容
     * @return 返回获取的百度网页搜索的文本正文
     */
    private String getPage(String keyword, int pageorder) {
        String strHtml = null;
        String url = null;
        try {
            if (pageorder == 1) {
                String tKeyword = URLEncoder.encode(keyword, "gb2312");
                url = this.engineSite.getStartUrl().replaceAll("ABC", tKeyword)
                        .trim();
            } else {
                if (this.nextPageUrl != null)
                    url = this.nextPageUrl.replaceAll("https", "http").trim();
            }
            if (url == null) {
                logger.debug("PageUrl is null. exit" + keyword + "["
                        + pageorder + "页]");
                return null;
            }

            logger.debug("accessUrl=" + URLDecoder.decode(url, "gb2312"));
            byte[] pageHtml = curEngineBrowser.getPage(url);
            if (pageHtml != null)
                strHtml = new String(pageHtml, "utf-8");

        } catch (Exception e) {
            logger.error(this.getClass().getName() + " Error :" + e);
        }

        return strHtml;
    }

    public String getRedirectUrlOwn(String oldRelativeUrl) {

        String strHost = "www.baidu.com";

        if (oldRelativeUrl.startsWith("/link?") == false)
            return oldRelativeUrl;

        String location = "http://" + strHost + oldRelativeUrl;

        boolean is302 = false;
        Socket socket = null;
        BufferedWriter wr = null;
        BufferedReader rd = null;
        try {

            socket = new Socket(strHost, 80);

            wr = new BufferedWriter(new OutputStreamWriter(
                    socket.getOutputStream(), "UTF8"));
            wr.write("GET " + oldRelativeUrl + " HTTP/1.0\r\n");
            wr.write("\r\n");
            wr.flush();

            rd = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));

            String line;

            while ((line = rd.readLine()) != null) {

                logger.debug(line);

                if (line.indexOf("HTTP/1.1 302") != -1)
                    is302 = true;

                if (is302 == true && line.startsWith("Location:")) {
                    location = line.substring("Location:".length()).trim();
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                wr.close();
                rd.close();
                socket.close();
            } catch (IOException e) {

                e.printStackTrace();
            }

        }

        return location;
    }

    public static void main(String[] args) {
        EngineSite engineSite = new EngineSite();
        engineSite.setName("baiduWebpage");
        engineSite
                .setStartUrl("http://www.baidu.com/s?q1=ABC&q2=&q3=&q4=&rn=50&lm=2&ct=0&ft=&q5=&q6=&tn=baiduadv");
        engineSite.setMaxPages(1);
        engineSite.setInterval(5);

        BaiduWebpage baiduNews = new BaiduWebpage(engineSite);

        String keyword = "不朽凡人 顶点";

        ArrayList<Article> allList = new ArrayList<Article>();

        ArrayList<String> keywordList = baiduNews.getFormatKeywordList(keyword);
        for (int j = 0; j < keywordList.size(); j++) {
            String formatWord = keywordList.get(j);
            System.out.println("formatWord=" + formatWord);

            for (int i = 1; i <= engineSite.getMaxPages(); i++) {
                ArrayList<Article> articleList = baiduNews.analyze(formatWord,
                        i);
                if (articleList != null) {
                    allList.addAll(articleList);
                }
            }
        }

        System.out.println(" size :" + allList.size());
        for (int i = 0; i < allList.size(); i++) {
            Article article = (Article) allList.get(i);
            System.out.println(article.getSubject() + " \tFrom="
                    + article.getWebName() + " \tTime=" + article.getPostTime()
                    + "\t Url=" + article.getUrl().trim() + "\tDigest="
                    + article.getDigest());
            if (article.getSimilars() == null)
                continue;

            for (int s = 0; s < article.getSimilars().size(); s++) {
                Article sarticle = (Article) article.getSimilars().get(s);
                System.out.println("\tSimi" + s + "\t" + sarticle.getSubject()
                        + " \tFrom=" + sarticle.getWebName() + " \tTime="
                        + sarticle.getPostTime() + "\t Url="
                        + sarticle.getUrl().trim() + "\tDigest="
                        + sarticle.getDigest());
            }
        }
    }

}
