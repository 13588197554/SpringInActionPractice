package com.guo.soundsystem;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;

/**
 * Created by guo on 21/2/2018.
 */
@Configuration
@Import({CDPlayerConfig.class,CDConfig.class})
@ImportResource("classpath:cd-config.xml")
public class SoundSystemConfig {
}
