# 第三章 高级装配

本章内容：
- Spring profile
- 条件化的bean声明
- 自动装配与歧义性
- bean的作用域
- Spring表达式语言

**本章中所介绍的技术也许你不会天天用到，但这并不意味着它们的价值会因此降低**

## 3.1环境与profile

在软件开发的时候，有一个很大的挑战就是将应用从一个环境迁移到另外一个环境。开发阶段中，某些环境相关的做法可能并不适合迁移到生产环境中，甚至即便迁移过去也无法工作。**数据库配置、加密算法以及外部系统的集成是跨环境部署**

在开发环境中，我们可能会使用切入式数据库，并预先加载测试数据。在Spring配置类中，可能会在一个带有@Bean注解的方法上使用EmbededDataBaseBuilder:

```java
@Bean(destroyMethod="shutdown")
public DataSource dataSource() {
    return new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.H2)
            .addScript("classpath:schema.sql")
            .addScript("classpath:test-data.sql")
            .build();
}
```
这会创建一个类型为javax.sql.dataSource的bean，使用EmbededDatabaseBuilder会搭建一个切入式的Hypersonic数据库，它的模式(schema)定义izaischema.sql中，测试数据则是通过test-data.sql。

当你在开发环境中集成测试或者启动应用进行手动测试的时候非常有用。每次启动它的时候，都能让数据库处于一个给定的状态

尽管创建的DataSource非常适合于开发环境中，但是对于生产环境来说，这是一个糟糕的选择。**在生产环境中你可能希望使用JNDI从容器中获取一个DataSource。**

```java
@Bean
public DataSource jndiDataSource() {
    JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();
    jndiObjectFactoryBean.setJndiName("jdbc/myDS");
    jndiObjectFactoryBean.setResourceRef(true);
    jndiObjectFactoryBean.setProxyInterface(javax.sql.DataSource.class);
    return (DataSource) jndiObjectFactoryBean.getObject();
}
```

**通过JNDI获取的DataSource能够让容器决定如何创建这个DataSource，甚至包括切换为容器管理的连接池，**即便如此，JNDI管理的Datasource更加适合与生产环境，对于简单的集成和开发测试来说，这会带来不必要的复杂性。

同时，在QA环境中，你可以选择完全不同的DataSource配置，可以配置为Commons DBCP连接池。
```java
@Bean(destroyMethod = "close")
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
```
看起来简单的DataSource实际上并不是那么简单。它表现了在不同环境中某个bean会有所不同。我们必须有一种方式来配置DataSource，使其在每种环境下都会选择最为合适的配置。

其中一种方式就是在单独的配置类(或XML)中配置每个bean，然后在构造阶段确定要使用哪一个配置编译到可部署的环境中。这种方式的问题在于要为每种环境重新构建应用，当从开发阶段迁移到QA阶段时，重新构造也算不上什么大问题。但是，**从QA阶段迁移到生产环境阶段时，重新构建可能引入BUG并且会在QA团队的成员中带来不安的情绪**

### 3.1.1 配置profile bean

Spring并不是在构造的时候做出这样的决策，而是到运行时再来确定，这样的结果就是在**同一个部署单元能够适应所有的环境，没有必要重新构建**.

Spring引入了bean profile的功能，要使用profile，你首先要将所有不同的bean定义整理到一个或多个profile之中，**在将应用部署到每个环境时，要确保对应的profile处于激活状态。**

在Java配置中，可以使用@profile注解指定某个bean属于哪一个profile。
```java
@Configuration
@Profile("dev")
public class DataSourceConfig {
    @Bean(destroyMethod="shutdown")
    public DataSource dataSource() {
        return new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .addScript("classpath:schema.sql")
                .addScript("classpath:test-data.sql")
                .build();
    }
}
```

需要注意的是@Profile注解应用到了类级别啥概念，它会告诉Spring这个配置来中的bean只有在dev profile激活时才创建。如果dev profile没有激活的话，那么带有@Bean注解的方法都会被忽略。

同时你可能需要一个适用于生产环境的配置

```java
@Configuration
@Profile("prod")
public class DataSourceConfig {
    @Bean
    public DataSource jndiDataSource() {
        JndiObjectFactoryBean jndiObjectFactoryBean = new JndiObjectFactoryBean();
        jndiObjectFactoryBean.setJndiName("jdbc/myDS");
        jndiObjectFactoryBean.setResourceRef(true);
        jndiObjectFactoryBean.setProxyInterface(javax.sql.DataSource.class);
        return (DataSource) jndiObjectFactoryBean.getObject();
    }

}
```
在Spring3.1，只能在类级别上使用@Profile注解，不过在Spring3.2开始，你也可以在方法级别上使用@Profile注解，与@Bean注解一同使用，这样的话，就能将两个bean的声明放在同一个配置类中。
```java
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

    @Bean(destroyMethod = "close")
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
```

尽管每个DataSource bean 都被声明在一个profile中，并且只能当规定的profile激活时，相应的bean才会被创建，但是可能会有其他的bean并没有声明到一个给定的profile范围内。**没有指定的profile的bean都会创建，与激活那个profile没有关系**


** 在XMl中配置profile
我们也可以通过<beans>元素的profil属性，在XML中配置profile bean。

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans
  ................................................................
    <beans profile="dev">
        <jdbc:embedded-database id="dataSource" type="H2">
            <jdbc:script location="classpath:schema.sql" />
            <jdbc:script location="classpath:test-data.sql" />
        </jdbc:embedded-database>
    </beans>

    <beans profile="qa">
        <bean id="dataSource"
              class="org.apache.commons.dbcp.BasicDataSource"
              destroy-method="close"
              p:url="jdbc:h2:tcp://dbserver/～/test"
              p:driverClassName="org.h2.Driver"
              p:username="guo"
              p:password="guo"
              p:initialSize="20"
              p:maxActive="39"/>
    </beans>

    <beans profile="prod">
        <jee:jndi-lookup id="dataSource"
                         lazy-init="true"
                         jndi-name="jdbc/myDatabase"
                         resource-ref="true"
                         proxy-interface="javax.sql.DataSource" />
    </beans>
</beans>
```
 除了所有的bean定义到同一个XML文件中，这种配置方式与定义单独的XML文件中实际效果是一样的。在运行时，只会创建一个bean，**这取决于处于激活状态的是哪一个profile**

### 3.1.2激活profile

Spring在确定哪个profile处于激活状态时，需要依赖两个独立的属性：`sring.profiles.active`和`spring.profiles.default`。如果设置了`spring.profiles.active`属性的话，那么它的值就会用来确定哪个profile是激活的。但如果没有设置`spring.profiles.active`的话，那么Spring将会查找`spring.profiles.default`的值。如果两者都没有的话，那就没有激活的profile。

有多种方式来设置这两个属性
- 作为DispatcherServlet的初始化参数  <init-param>
- 作为Web的应用上下文参数             <context-param>
- 作为JNDI条目
- 作为环境变量
- 作为JVM的系统属性
- 在集成测试类上

作者喜欢的一种方式是使用DisPatcherServlet的参数将spring.profiles.default设置为开发环境，会在Servlet上下文中进行设置。

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns="http://java.sun.com/xml/ns/javaee" xmlns:web="http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd"
	xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd"
	id="WebApp_ID" version="2.5">
	<display-name>taotao-rest</display-name>
	<welcome-file-list>
		<welcome-file>index.html</welcome-file>
	</welcome-file-list>
	<!-- 加载spring容器 -->
	<context-param>
		<param-name>contextConfigLocation</param-name>
		<param-value>classpath:spring/applicationContext*.xml</param-value>
	</context-param>
    <!--为上下文设置默认的profile-->
    <context-param>
        <param-name>spring.profiles.default</param-name>
        <param-value>dev</param-value>
    </context-param>
	<listener>
		<listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
	</listener>

	<!-- 解决post乱码 -->
	<filter>
		<filter-name>CharacterEncodingFilter</filter-name>
		<filter-class>org.springframework.web.filter.CharacterEncodingFilter</filter-class>
		<init-param>
			<param-name>encoding</param-name>
			<param-value>utf-8</param-value>
		</init-param>

	</filter>
	<filter-mapping>
		<filter-name>CharacterEncodingFilter</filter-name>
		<url-pattern>/*</url-pattern>
	</filter-mapping>

	<!-- springmvc的前端控制器 -->
	<servlet>
		<servlet-name>taotao-rest</servlet-name>
		<servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
		<!-- contextConfigLocation不是必须的， 如果不配置contextConfigLocation， springmvc的配置文件默认在：WEB-INF/servlet的name+"-servlet.xml" -->
		<init-param>
			<param-name>contextConfigLocation</param-name>
			<param-value>classpath:spring/springmvc.xml</param-value>
		</init-param>
        <!--为Servlet设置默认的profile-->
        <init-param>
            <param-name>spring.profiles.default</param-name>
            <param-value>dev</param-value>
        </init-param>
		<load-on-startup>1</load-on-startup>
	</servlet>
	<servlet-mapping>
		<servlet-name>taotao-rest</servlet-name>
		<url-pattern>/rest/*</url-pattern>
	</servlet-mapping>
</web-app>

```
按照这种方式设置spring.profiles.default,所有开发人员能从版本控制软件中获得应用的程序源码，并使用开发环境的设置(如切入式数据库)运行代码而不需要任何额外的设置。

**当应用程序部署到QA、生产、或其他环境中时，负责部署的人根据情况使用系统属性、环境变量、或JNDI设置`spring.profiles.active`即可。当设置`spring.profiles.avtive`后，至于`spring.profiles.default`设置成什么已经无所谓了：系统会优先使用`spring.profiles.active`中设置的profile**

使用profi进行测试

当运行集成测试时，通常会希望采用与生产环境相同的配置进行测试。但是，如果配置中的bean定义在了profile中，那么在测试运行时，我们就需要有一种方式来启动profile

Spring提供了`@ActiveProfiles`注解，可以用它来指定运行测试时要激活哪个profile。在集成测试时，通常想要激活的是开发环境的profile。

** 再次佩服老外**

```java
import static org.junit.Assert.*;
........................................
/**
 * Created by guo on 22/2/2018.
 */
public class DataSourceConfigTest {

    @RunWith(SpringJUnit4ClassRunner.class)
    @ContextConfiguration(classes = DataSourceConfig.class)
    @ActiveProfiles("dev")
    public static class DevDataSource{

        @Autowired
        private DataSource dataSource;

        @Test
        public void shouldBeEmbededDatasourcr() {
            JdbcTemplate jdbc = new JdbcTemplate(dataSource);
            List<String> results = jdbc.query("select id, name from Things", new RowMapper<String>() {
                @Override
                public String mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return rs.getLong("id") + ":" + rs.getString("name");
                }
            });
           assertEquals(1,results.size());
           assertEquals("1:A",results.get(0));
        }
    }


    @RunWith(SpringJUnit4ClassRunner.class)
    @ContextConfiguration(classes=DataSourceConfig.class)
    @ActiveProfiles("prod")
    public static class ProductionDataSourceTest {
        @Autowired
        private DataSource dataSource;

        @Test
        public void shouldBeEmbeddedDatasource() {
            // 应该是Null ，因为在JNDI中没有配置数据源
            assertNull(dataSource);
        }
    }
    @RunWith(SpringJUnit4ClassRunner.class)
    @ContextConfiguration("classpath:datasource-config.xml")
    @ActiveProfiles("dev")
    public static class DevDataSourceTest_XMLConfig {
        @Autowired
        private DataSource dataSource;

        @Test
        public void shouldBeEmbeddedDatasource() {
            assertNotNull(dataSource);
            JdbcTemplate jdbc = new JdbcTemplate(dataSource);
            List<String> results = jdbc.query("select id, name from Things", new RowMapper<String>() {
                @Override
                public String mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return rs.getLong("id") + ":" + rs.getString("name");
                }
            });

            assertEquals(1, results.size());
            assertEquals("1:A", results.get(0));
        }
    }

    @RunWith(SpringJUnit4ClassRunner.class)
    @ContextConfiguration("classpath:datasource-config.xml")
    @ActiveProfiles("prod")
    public static class ProductionDataSourceTest_XMLConfig {
        @Autowired(required=false)
        private DataSource dataSource;

        @Test
        public void shouldBeEmbeddedDatasource() {
            // 应该是Null ，因为在JNDI中没有配置数据源
            assertNull(dataSource);
        }
    }
}
``

在条件化创建bean，Spring的profil机制是一种很好的方法，这里的条件要基于哪个profile处于激活状态来判断。Spring 4.0中提供了一种更为通用的机制来实现条件化的bean定义，这这种机制之中，条件化 完全由你来取定，Spring 4 和@Conditional注解定义条件化的bean。

## 3.2 条件化的bean








































































-
