package com.microservice.util;

import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;

/**
 * Created by lihui on 2017/3/31.
 */
@Component
public class DataConfig {

    public String getSourcePath()
    {
        String filepath = getClass().getClassLoader().getResource("application.properties").getPath();//ClassUtils.getDefaultClassLoader().getResource("/application.properties").getPath();
        try {
            filepath = java.net.URLDecoder.decode(filepath,"utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        filepath = filepath.replaceAll("application.properties","");
        return filepath;
    }
    public String getBookPath()
    {
        String filepath = getSourcePath();
        if(filepath==null) return "E:\\";

        filepath += "\\book\\";
        return filepath;
    }
    public String getDataPath()
    {
        String filepath = getSourcePath();
        if(filepath==null) return "";
        filepath += "\\data\\data.json";
        return filepath;
    }
}
