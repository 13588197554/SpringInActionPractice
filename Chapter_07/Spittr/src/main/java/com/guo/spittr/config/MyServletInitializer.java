package com.guo.spittr.config;

import org.springframework.web.WebApplicationInitializer;

import javax.servlet.ServletRegistration.Dynamic;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

/**
 * Created by guo on 25/2/2018.
 */
public class MyServletInitializer implements WebApplicationInitializer {
    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {

    }
/*    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        javax.servlet.FilterRegistration.Dynamic filter = servletContext.addFilter("myFilter",myFilter.class);
        filter.addMappingForUrlPatterns(null,false,"/custom*//*");
    }*/
}
