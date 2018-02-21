package com.guo.soundsystem;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Created by guo on 21/2/2018.
 */
@Configuration
public class CDPlayerConfig {
    @Bean
    public CompactDisc sgtPeppers() {
        return new SgtPeppers();
    }
    @Bean
    public CDPlayer cdPlayer(CompactDisc compactDisc) {
        return new CDPlayer(compactDisc);
    }
}
