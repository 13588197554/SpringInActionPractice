package com.guo.spittr.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by guo on 24/2/2018.
 * 首页控制器
 */
@Controller
@RequestMapping("/")
public class HomeController {
    @RequestMapping(method = RequestMethod.GET)           //处理对“/”的Get请求
    public String home() {
        return "home";                                                //视图名为home
    }
}
