package com.guo.spittr.web;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by guo on 24/2/2018.
 */
public class HomeControllerTest {
    @Test
    public void testHomePage() throws Exception {
        HomeController controller = new HomeController();
        assertEquals("home",controller.home());
    }
}
