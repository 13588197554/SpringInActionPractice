package com.guo.spittr.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;


/**
 * Created by guo on 23/2/2018.
 */
@Configuration
@ComponentScan(basePackages = {"com.guo.spittr"},
    excludeFilters = {
        @Filter(type = FilterType.ANNOTATION,value = EnableWebMvc.class)})
public class RootConfig {

}
