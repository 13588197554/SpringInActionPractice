package com.guo.myapp;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;

/**
 * Created by guo on 22/2/2018.
 */
@Configuration
@ComponentScan(excludeFilters = {@Filter(type = FilterType.ANNOTATION,value = Configuration.class)})
public class ComponentScannedConfig {
}
