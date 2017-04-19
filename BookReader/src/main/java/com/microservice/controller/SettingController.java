package com.microservice.controller;

import com.microservice.data.BookArg;
import com.microservice.data.ErrorCode;
import com.microservice.data.Result;
import com.microservice.util.BookArgRepository;
import com.microservice.util.DataConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Created by lihui on 2017/3/22.
 */
@Controller
public class SettingController {

    @Autowired
    DataConfig dataConfig;
    @Autowired
    BookArgRepository bookArgRepository;

    @RequestMapping(value = "/setting", method = RequestMethod.GET)
    public String setting(Model model){
        List<BookArg> bookArgs = bookArgRepository.readFile(dataConfig.getDataPath(),new BookArg());
        model.addAttribute("bookArgs", bookArgs);
        model.addAttribute("key","hello");
        return "setting";
    }

    @RequestMapping(value = "/save",method = RequestMethod.POST)
    @ResponseBody
    public Result<String> save(@RequestParam String all)
    {
        if(all==null || all.isEmpty()) return new Result<String>("字符串不能为空", ErrorCode.BAD_REQUEST);
        bookArgRepository.writeFile(dataConfig.getDataPath()+"bak",all);
        return new Result<String>("保存成功", ErrorCode.SUCCESS);
    }
}
