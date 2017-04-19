package com.microservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by lihui on 2017/3/22.
 */
@RefreshScope
@ConfigurationProperties
@Controller
public class IndexController {
    //private final Logger logger = Logger.getLogger(getClass());
    @Autowired
    private DiscoveryClient client;

    @RequestMapping("/")
    public String index(){
        return "index";
    }

    @Value("${myname}")
    String myname="lihui";

    @RequestMapping("/myname")
    @ResponseBody
    public String myname() {
        return "My name  is: " + myname;
    }

    @RequestMapping("/client")
    @ResponseBody
    public String getClient() {
        ServiceInstance instance = client.getLocalServiceInstance();

        String temp = "/add, host:" + instance.getHost() + ", service_id:" + instance.getServiceId();
        return temp;
    }
}
