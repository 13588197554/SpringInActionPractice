package com.guo.myapp;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * Created by guo on 22/2/2018.
 */
@Configuration
public class ExplicitConfig {

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public NotePad notePad() {
        return new NotePad();
    }

    @Bean
    public UniqueThing unique() {
        return new UniqueThing();
    }

}
