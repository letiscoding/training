package com.microservice;

import com.microservice.data.BookArg;
import com.microservice.util.BookArgRepository;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lihui on 2017/3/31.
 */
public class test {
    public static void main(String[] args)
    {
        String fileName = "/E:/03study/spring%20cloud/BookReader/target/classes/\\data\\data.json";
        try {
            fileName = java.net.URLDecoder.decode(fileName,"utf-8");
            List<BookArg> list = new ArrayList<>();

            BookArg bookArg = new BookArg();
            bookArg.setWebsitName("顶点cc");
            bookArg.setSourceUrl("http://www.23us.cc");
            bookArg.setChannelUrlRegex("\\d*.html");
            bookArg.setContentRegx("<div id=\"content\"([\\s\\S]*?)</div>");
            bookArg.setContentTitleRegx("<h1>([\\s\\S]*?)</h1>");
            list.add(bookArg);
            BookArg bookArg1 = new BookArg();
            bookArg1.setWebsitName("顶点com");
            bookArg1.setSourceUrl("http://www.23us.com/html/");
            bookArg1.setChannelUrlRegex("\\d*.html");
            bookArg1.setContentRegx("<dd id=\"content\"([\\s\\S]*?)</dd>");
            bookArg1.setContentTitleRegx("<h1>([\\s\\S]*?)</h1>");
            list.add(bookArg1);
            BookArgRepository bookArgRepository = new BookArgRepository();
            bookArgRepository.writeFile(fileName,list);
            List<BookArg> result = bookArgRepository.readFile(fileName,new BookArg());
            for (BookArg item:result) {
                System.out.println(item.getWebsitName());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        catch (Exception err)
        {
            err.printStackTrace();
        }

    }

    public static StringBuilder readFile(String filePath){
        StringBuilder sb = new StringBuilder();
        try {
            String encoding="utf-8";
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


}
