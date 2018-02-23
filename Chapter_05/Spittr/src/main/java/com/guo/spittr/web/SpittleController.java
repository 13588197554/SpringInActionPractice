package com.guo.spittr.web;

import com.guo.spittr.data.SpittleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by guo on 24/2/2018.
 */
@Controller
@RequestMapping("/spittles")
public class SpittleController {
    public SpittleRepository SpittleRepository;

    @Autowired
    public SpittleController(SpittleRepository SpittleRepository) {
        this.SpittleRepository = SpittleRepository;
    }


    @RequestMapping(method = RequestMethod.GET)
    public String spittles(Model model) {
        model.addAttribute(SpittleRepository.findSpittles(Long.MAX_VALUE,20));     // 将spittle添加到视图
        return "spittles";                                                                // 返回视图名
    }
}
