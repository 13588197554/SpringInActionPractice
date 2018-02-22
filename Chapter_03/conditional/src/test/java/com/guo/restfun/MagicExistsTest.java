package com.guo.restfun;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.swing.*;

import static org.junit.Assert.*;
/**
 * Created by guo on 22/2/2018.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = MagicExistsCondition.class)
public class MagicExistsTest {

    @Autowired
    private ApplicationContext context;

    /**
     * 这个测试会失败，直到你输入“magic”属性。
     * 你也可以设置一个环境变量，JVM属性，
     /*
     * This test will fail until you set a "magic" property.
     * You can set this property as an environment variable, a JVM system property, by adding a @BeforeClass
     * method and calling System.setProperty() or one of several other options.
     */
    @Test
    public void shouldNotNull() {
        assertTrue(context.containsBean("magicBean"));
    }
}
