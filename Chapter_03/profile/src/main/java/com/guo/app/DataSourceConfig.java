package com.guo.app;

import org.apache.commons.dbcp.BasicDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.jndi.JndiObjectFactoryBean;

import javax.sql.DataSource;

/**
 * Created by guo on 22/2/2018.
 */
@Configuration
public class DataSourceConfig {
    @Bean(destroyMethod="shutdown")
    @Profile("dev")
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .addScript("classpath:schema.sql")
                .addScript("classpath:test-data.sql")
                .build();
    }
    @Bean
    @Profile("prod")
    public DataSource jndiDataSource() {
        JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();
        jndiObjectFactoryBean.setJndiName("jdbc/myDS");
        jndiObjectFactoryBean.setResourceRef(true);
        jndiObjectFactoryBean.setProxyInterface(javax.sql.DataSource.class);
        return (DataSource) jndiObjectFactoryBean.getObject();
    }
    //@Bean(destroyMethod = "close")
    public DataSource dataSourceAO() {
        BasicDataSource   dataSource = new BasicDataSource();
        dataSource.setUrl("jdbc:h2:tcp://dbserver/～/test");
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUsername("guo");
        dataSource.setPassword("guo");
        dataSource.setInitialSize(20);
        dataSource.setMaxActive(30);
        return dataSource;
    }
}
