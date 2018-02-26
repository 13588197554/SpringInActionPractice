package com.guo.spittr.config;

import com.guo.spittr.web.WebConfig;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

import javax.servlet.Filter;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletRegistration.Dynamic;

/**
 * Created by guo on 23/2/2018.
 */
public class SpitterWebInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
    @Override
    protected String[] getServletMappings() {             //将DispatcherServlet映射到“/”
        return new String[]{"/"};
    }

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class<?> [] {RootConfig.class};
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class<?> [] { WebConfig.class};
    }

    @Override
    protected void customizeRegistration(Dynamic registration) {
        registration.setMultipartConfig(
                new MultipartConfigElement("C:\\Temp\\uploads",2097152, 4194304, 0));
    }

   /* @Override
    protected Filter[] getServletFilters() {
        return new Filter[] {new Myfilter()};
    }*/
}
