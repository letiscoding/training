package com.microservice.util;

import net.sf.json.JSONArray;

import java.io.*;
import java.util.List;

/**
 * Created by lihui on 2017/3/31.
 */
public class JsonFileHelper<T> {

    public List<T> readFile(String filePath,T t)
    {
        StringBuilder sb = readFile(filePath);
        if(sb==null) return null;

        JSONArray jsonArray = JSONArray.fromObject(sb.toString());
        List<T> list = (List) JSONArray.toCollection(jsonArray,
                t.getClass());
        return list;
    }

    public StringBuilder readFile(String filePath)
    {
        StringBuilder sb = new StringBuilder();
        String encoding="utf-8";
        try {
            File file=new File(filePath);
            if(file.isFile() && file.exists()){ //判断文件是否存在
                InputStreamReader read = new InputStreamReader(
                        new FileInputStream(file),encoding);//考虑到编码格式
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt = null;

                while((lineTxt = bufferedReader.readLine()) != null){
                    sb.append(lineTxt);
                }
                read.close();
            }else{
                return null;
            }
        } catch (Exception e) {
            System.out.println("读取文件内容出错");
            return null;
        }
        return sb;
    }

    public void writeFile(String filePath,String result)
    {
        File file=new File(filePath);
        if(file.isFile() && file.exists()){
            file.deleteOnExit();
        }
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(filePath));
            bw.write(result);
            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void writeFile(String filePath,List<T> ts)
    {
        JSONArray jsonArray = JSONArray.fromObject(ts);
        writeFile(filePath,jsonArray.toString());
    }
}
