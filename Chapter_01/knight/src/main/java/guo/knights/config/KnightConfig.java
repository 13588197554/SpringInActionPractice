package guo.knights.config;

import guo.knights.BraveKnight;
import guo.knights.Knight;
import guo.knights.Quest;
import guo.knights.SlayDragonQuest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by guo on 20/2/2018.
 */
@Configuration
public class KnightConfig {
    @Bean
    public Knight knight() {
        return new BraveKnight(quest());
    }
    @Bean
    public Quest quest() {
        return new SlayDragonQuest(System.out);
    }
}
