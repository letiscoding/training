package com.microservice.util;

import com.microservice.data.BookArg;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by lihui on 2017/3/31.
 */
@Component
public class BookArgRepository extends JsonFileHelper<BookArg> {

    public BookArg findOne(String dataPath, String bookUrl) {
        List<BookArg> bookArgs = this.readFile(dataPath,new BookArg());
        for (BookArg bookArg:bookArgs) {
            if(bookUrl.indexOf(bookArg.getSourceUrl())!=-1) return bookArg;
        }
        return null;
    }
}
