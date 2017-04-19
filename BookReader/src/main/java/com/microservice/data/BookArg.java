package com.microservice.data;

import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * Created by lihui on 2016/6/8.
 */
@Component
public class BookArg  implements Serializable {

    private String channelUrlRegex ="";
    private String contentRegx="<dd id=\"contents\"([\\s\\S]*?)</dd>";
    private String contentTitleRegx="<h1>([\\s\\S]*?)</h1>";
    private String startTitle;
    private String sourceUrl;
    private String websitName;

    public BookArg()
    {

    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
    }

    public String getChannelUrlRegex() {
        return channelUrlRegex;
    }

    public void setChannelUrlRegex(String channelUrlRegex) {
        if(channelUrlRegex==null||channelUrlRegex.equals(""))
        {
            this.channelUrlRegex=this.getSourceUrl()+"\\d*.html";
        }
        else {
            this.channelUrlRegex = channelUrlRegex;
        }
    }

    public String getContentRegx() {
        return contentRegx;
    }

    public void setContentRegx(String contentRegx) {
        this.contentRegx = contentRegx;
    }

    public String getStartTitle() {
        try {
            startTitle = new String(startTitle.getBytes("UTF-8"));
        }
        catch (Exception err)
        {
            startTitle ="";
        }

        return startTitle;
    }

    public void setStartTitle(String startTitle) {
        this.startTitle = startTitle==null?"":startTitle;
    }





    @Override
    public String toString() {
        return "ContentTitle:"+this.getContentTitleRegx()+";\n" +
                "\r"+"StartTitle:"+this.getStartTitle()+";\n" +
                "\r"+"ChannelUrlRegex:"+this.getChannelUrlRegex()+";\n" +
                "\r"+"SourceUrl:"+this.getSourceUrl()+";\n" +
                "\r"+"ContentRegx:"+this.getContentRegx()+";\n";
    }


    public String getContentTitleRegx() {
        return contentTitleRegx;
    }

    public void setContentTitleRegx(String contentTitleRegx) {
        this.contentTitleRegx = contentTitleRegx;
    }

    public String getWebsitName() {
        return websitName;
    }

    public void setWebsitName(String websitName) {
        this.websitName = websitName;
    }
}
