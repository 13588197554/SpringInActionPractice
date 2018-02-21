package com.guo.soundsystem;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Created by guo on 21/2/2018.
 */
@Configuration
public class CDConfig {
    @Bean
    public CompactDisc compactDisc () {
        return new SgtPeppers();
    }
}
