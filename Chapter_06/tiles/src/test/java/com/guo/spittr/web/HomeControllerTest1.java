package com.guo.spittr.web;

import org.junit.Test;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created by guo on 24/2/2018.
 */
public class HomeControllerTest1  {

    @Test
    public void testHomePage() throws Exception {                          //大家在测试的时候注意静态导入的方法
        HomeController controller = new HomeController();
        MockMvc mockMvc =  standaloneSetup(controller).build();              //搭建MockMvc
       mockMvc.perform(get("/"))                                 //对“/”执行GET请求，
               .andExpect(view().name("home"));           //预期得到home视图
    }
}
