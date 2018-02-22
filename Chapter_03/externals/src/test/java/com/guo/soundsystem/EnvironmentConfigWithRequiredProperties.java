package com.guo.soundsystem;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * Created by guo on 23/2/2018.
 */

@Configuration
public class EnvironmentConfigWithRequiredProperties {

    @Autowired
    Environment env;

    @Bean
    public BlankDisc blankDisc() {
        return new BlankDisc(
                env.getRequiredProperty("disc.title"),
                env.getRequiredProperty("disc.artist"));
    }

}

