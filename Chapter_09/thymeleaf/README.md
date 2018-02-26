## Spring Security
Spring Security 是基于Spring 应用程序提供的声明式安全保护的安全框架。Spring Sercurity 提供了完整的安全性解决方案，它能够在Web请求级别和方法调用级别处理身份认证和授权，因为是基于Spring，所以Spring Security充分利用了依赖注入(Dependency injection DI) 和面向切面的技术。

Spring Security从两个角度来解决安全性，他使用Servlet规范中的Filter保护Web请求并限制URL级别的访问。Spring Security还能够使用AOP保护方法调用——借助于对象代理和使用通知，能够取保只有具备适当权限的用户才能访问安全保护的方法。

### 1、理解Spring Security的模块

将Spring Security模块添加到应用程序的类路径下。应用程序的类路径下至少包含core和Configuration这两个模块。它经常被用于保护Web应用，添加Web模块，同时还需要JSP标签库。
### 2、过滤Web请求
Spring Security借助一系列Servlet Filter来提供各种安全性功能。

DelegatingFilterProxy是一个特殊的ServletFilter，它本身所作的工作并不多，只是将工作委托给一个Javax.servlet.Filter实现类，这个实现类作为一个<bean<>注册在Spring上下文中。

web.xml配置

```xml
<filter>
	<filter-name>springSecurityFilterChain</filter-name>
	<filter-class>org.springframework.web.filter.DelegatingFilterproxy</filter-class>
</filter>
```
DelegatingFilterproxy会将过滤逻辑委托给它。

如果你希望借助于WebApplicationInitializer以JavaConfig配置,需要一个扩展类
```java
import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;

/**
 * Created by guo on 2/26/2018.
 */
public class SecurityWebInitializer extends AbstractSecurityWebApplicationInitializer {
}
```
`AbstractSecurityWebApplicationInitializer`实现了`WebapplicationInitializer`,因此Spirng会发现他，并用它在Web容器中注册`DelegatingFilterproxy`.它不需要重载任何方法。它会拦截发往应用中的请求，并将其委托给`springSecurityFilterChain`

Spting Security依赖一系列ServletFilter来提供不同的安全特性。但是你不需要细节。当我们启用Web安全性的时候，会自动创建这些Filter。

### 3、编写简单的安全性配置。

Spring 3.2引入了新的java配置方案，完全不需要通过XML来配置安全性功能了。

```java
@Configuration
@EnableWebSecurity    //启用Web安全性
public class SecurityConfig extends WebSecurityConfigurerAdapter {
}
```
@EnableWebSecurity启用Web安全功能，但它本身并没有什么用处 ,Spring Security必须配置在一个实现类WebSecurityConfigurer的bean中。

如果你的应用碰巧是在使用Spirng MVC的话，那么就应该考虑使用`@EnableWebMvcSecurity`还能配置一个Spring MVC参数解析器。这样的话，处理器方法就能够通过带有@AuthenticationPrincipal注解的参数获取得认证用户的principal。它同时还配置一个bean，在使用Spring表单绑定标签库来定义表单时，这个bean会自动添加一个隐藏的跨站请求伪造(CSRF)token的输入流。

```java
@Configuration
@EnableWebMvcSecurity//启用Web安全性
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .anyRequest().authenticated()
                .and()
                .formLogin().and()
                .httpBasic();
    }
}
```
这个简单的默认配置指定了如何保护HTTP请求，以及客户端认证用户的方案。通过调用 authorizeRequests()和anyRequest().authenticated()就会要求所欲进入应用的Http都要进行认证，他也配置Spring Securoty支持基于表单的登录以及HTTP Basic方式的认证。

为了让Spring 满足我们应用的需求，还需要在添加一些配置。
- 配置用户储存
- 指定哪些请求需要认证，那些请求不需要认证，以及所需要的权限
- 提供一个自定义的登录页面，替代原来简单的默认登录页。

除了Spring Security的这些功能，我们可能还希望给予安全限制，有选择性在Web视图上显示特定的内容。

### 4 选择查询用户详细信息的服务。

我们所需要的是用户的存储，也就是用户名、密码以及其他信息存储的地方，在进行认证决策的时候，对其进行检索。

好消息是Spring Security非常灵活，能够给予各种数据库存储来认证用户名，它内置了多种常见的用户存储场景，如内存、关系型数据库，以及LDAP，但我们也可以编写并插入自定义的用户存储实现。

借助于Spring Security的Java配置，我们能够很容易的配置一个或多个数据库存储方案。

5、使用基于内存的用户存储

```java
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .withUser("user").password("password").roles("USER").and()
                .withUser("admin").password("password").roles("USER","ADMIN");
    }
```

通过简单那的调用`inMemoryAuthentication`就能启用内存用户村苏。但是，我们还需要一些用户，否则的话这个没用户并没有且别。需要调用weithuser为其存储添加新的用户。以及给定用户授予一个或多个角色权限的reles()方法

对于调式和开发人员来讲，基于内存的用户存储是很有用的，但对于生产级别应用来讲，这就不是最理想的状态了。

### 5、基于数据库进行认证

用户数据通常会存储在关系型数据库中，并通过JDBC进行访问。为了配置Spring Security使用以JDBC为支撑的用户存储，我们可以使用jdbcAuthentication()方法，所需的最少配置。

```java
@Autowired
DataSource dataSource;

@Override
protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.jdbcAuthentication()
						.dataSource(dataSource);
}
```

我们必须要配置的知识一个DataSource，这样的话就能 访问关系型数据库里。

尽管默认的最少配置能够让一切运转起来，但是，它对我们的数据库模式有一些要求。它预期存在某些存储用户数据的表。
```java
public static final String DEF_USERS_BY_USERNAME_QUERY =
				"select username,password,enabled " +
				"from users " +
				"where username = ?";
public static final String DEF_AUTHORITIES_BY_USERNAME_QUERY =
				"select username,authority " +
				"from authorities " +
				"where username = ?";
public static final String DEF_GROUP_AUTHORITIES_BY_USERNAME_QUERY =
				"select g.id, g.group_name, ga.authority " +
				"from groups g, group_members gm, group_authorities ga " +
				"where gm.username = ? " +
				"and g.id = ga.group_id " +
				"and g.id = gm.group_id";
```
在第一个查询中，我们获取了用户的用户名、密码以及是否启用的信息，这些信息用来进行用户认证。接下来查询查找 了用户所授予的权限，用来进行鉴权。最后一个查询中，查找了用户作为群组的成员所授予的权限。

如果你能够在数据库中定义和填充满足这些查询的表，那么基本上就不需要你在做什么额外的事情了。

```java
@Override
protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.jdbcAuthentication()
		 .dataSource(dataSource)
		 .usersByUsernameQuery(
						 "select username,password,true" +
						 "from Spitter where username=?")
						.authoritiesByUsernameQuery(
						"select username,'ROLE_USER' from Spitter where username=？");
}
```

在本例中，我们只重写了认证和基本权限的查询语句，但是通过调用`groupAuthoritiesByUsername()`方法，我们也能够将群组权限重写为自定义的查询语句。


为了解决密码明文的问题，我们借助于passwordEncode()方法指定一个密码转码器(encoder)

```java
@Override
protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.jdbcAuthentication()
		 .dataSource(dataSource)
		 .usersByUsernameQuery(
						 "select username,password,true" +
						 "from Spitter where username=?")
						.authoritiesByUsernameQuery(
						"select username,'ROLE_USER' from Spitter where username=？")
		.passwordEncoder(new StandardPasswordEncoder("53cd3t"));
}
```
passwordEncoder()方法可以接受Spring Security中passwordEncoder接口的任意实现。加密模块包含了三个这样的实现
- StandardPasswordEncoder
- NoOpPasswordEncoder
- BCryptPasswordEncoder

上述代码使用了`StandardPasswordEncoder`,但是如果内置的实现无法满足需求时，你可以提供自定义的实现 ，`passwordEncoder`接口如下：
```java
package org.springframework.security.crypto.password;
/**
 * Service interface for encoding passwords.
 */
public interface PasswordEncoder {
    /**
     * Encode the raw password.
     */
    String encode(CharSequence rawPassword);
    /**
     * Verify the encoded password obtained from storage matches the submitted raw password after it too is encoded.
     */
    boolean matches(CharSequence rawPassword, String encodedPassword);
}
```

不管使用哪一个密码转化器，都需要理解的一点是：数据库的秘密是永远不会解码的，所采取的策略与之相反。用户在登录时输入的密码会按照相同的算法进行转码，然后在于数据库中已经转码过的密码进行对比，这个对比是在`PasswordEncoder`的matches()方法中进行的。

### 6、基于LDAP进行认证

为了让Spring Security使用基于LDAP的认证，我们可以使用ldapAuthentication()方法，这个方法类似于`jdbcAuthentication()`只不过是LDAP版本

```java
@Override
protected void configure(AuthenticationManagerBuilder auth) throws Exception {
	 auth.ldapAuthentication()
					 .userSearchBase("(uid={0})")
					 .groupSearchFilter("member={0}");
}
```

配置密码比对
基于LDAP进行认证的默认策略是进行绑定操作，直接通过LDAP服务器认证用户，另一种可选的方式是进行对比，涉及到输入的 密码发送到LDAP目录上，并要求服务器将这个密码和用户的密码进行对比，因为对比是用LDAP服务器内完成的。实际的秘密能保持私密。
```java
@Override
protected void configure(AuthenticationManagerBuilder auth) throws Exception {
	 auth.ldapAuthentication()
					 .groupSearchBase("on=people")
					 .userSearchBase("(uid={0})")
					 .groupSearchFilter("member={0}")
					 .groupSearchBase("on=groups")
					 .groupSearchFilter("member={0}")
					 .passwordCompare();
}
```
如果密码被保存在不同的属性中，可以通过`passwordAttribute()`方法来声明密码属性的名称
```java
@Override
protected void configure(AuthenticationManagerBuilder auth) throws Exception {
	 auth.ldapAuthentication()
					 .groupSearchBase("on=people")
					 .userSearchBase("(uid={0})")
					 .groupSearchFilter("member={0}")
					 .groupSearchBase("on=groups")
					 .groupSearchFilter("member={0}")
					 .passwordCompare()
					 .passwordEncoder(new Md5PasswordEncoder())
					 .passwordAttribute("passcode");
}
```

为了避免这一点我们可以通过调用`passwordEncoder()`方法指定加密策略。本例中使用MD5加密，这需要LDAP服务器上密码也是MD5进行加密

```java

@Override
protected void configure(AuthenticationManagerBuilder auth) throws Exception {
	 auth.ldapAuthentication()
					 .groupSearchBase("on=people")
					 .userSearchBase("(uid={0})")
					 .groupSearchFilter("member={0}")
					 .groupSearchBase("on=groups")
					 .groupSearchFilter("member={0}")
					 .contextSource()
					 .root("dc=guo,dc=com");       //.url()
					 .ldif("classpath:users.ldif");     //这里是可以分开放的，需要定义users.ldif文件
}
```

### 7、拦截请求

在任何的应用中，并不是所有的页面都需要同等程度地保护。尽管用户基本信息页面时公开的。但是，如果当处理“/spitter/me”时，通过展现当前用户的基本信息那么就需要进行认证，从而确定要展现谁的信息。

对每个请求进行细粒度安全性控制的关键在于重载`configure(HttpSecurity)`方法。

```java
@Override
protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
						.antMatchers("spitters/me").authenticated()                  //进行认证。
						.antMatchers(HttpMethod.POST,"/spittles").authenticated()			//必须经过认证
						.anyRequest().permitAll();																	//其他所有请求都是允许的，不需要认证。
}
```

`antMatchers()`方法中设置的路径支持Ant风格的通配符。

```java
.antMatchers("spitters/**").authenticated()
```
```java
.antMatchers("spitters/**","spittles/mine").authenticated()
```
```java
.antMatchers("spitters/.*").authenticated()
```

```java
@Override
protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
						.antMatchers("spitters/me").authenticated()
						.antMatchers(HttpMethod.POST, "/spittles")
						.hasAuthority("ROLE_SPITTER")
						.anyRequest().permitAll();
}
```
要求用户不仅需要认证，还要具备ROLE_SPITTER权限。作为替代方案，还可以使用hasRole()方法，它会自动使用“ROLE_”前缀

```java
@Override
protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
						.antMatchers("spitters/me").authenticated()
						.antMatchers(HttpMethod.POST,"/spittles").hasRole("SPITTER")
						.anyRequest().permitAll();
}
```
很重要的一点是将最为具体的请求路径放到最前面，而最不具体的路径放到最后面，如果不这样做的话，那不具体的配置路径将会覆盖掉更为具体的路径配置



### 8、使用Spirng表达式进行安全保护


比SpEL更为强大的原因在于，HasRole()仅仅是Spring支持的安全相关表达式中的一种。

Spring Security支持的所有表达式。
- principal : 用户的principl对象
- permitAll :结果始终为true
- hasRole 如果用户被授予了指定的角色 结果为true
- authentication ：用户认证对象
- denyAll 结果始终为false
- 。。。。

在掌握了Spring Security 的SpEL表达式后，我们就能够不再局限于基于用户的权限进访问限制了。

### 9、强制通道的安全性

使用HTTP提交数据是一件具有风险的事情。通过HTTP发送的数据没有经过加密，黑客就有机会拦截请求并且能够看到他们想看到的信息。这就是为什么铭感的 数据要通过HTTPS来加码发送的原因。

使用HTTPS似乎很简单，你要做的事情只是在URL中的HTTP后加上一个字母“s”就可以了，是吗？ 是的，不加也可以的。哈哈哈。。。

这是真的，但这是把使用的HTTPS通道的责任放在了错误的地方。

为了保证注册表单的数据通过HTTPS传递，我们可以在配置中添加`requiresChannel()`方法

```java
@Override
protected void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
						.antMatchers("spitters/me").authenticated()
						.antMatchers(HttpMethod.POST,"/spittles").hasRole("SPITTER")
						.anyRequest().permitAll()
				.and()
						.requiresChannel()
						.antMatchers("/spitter/form").requiresSecure();   //需要HTTPS
}
```

不论何时，只要是对“/spitter/form”的请求，Spring Security 都视为需要安全通道(通过调用`requiresChannel()确定`)并自动将请求重定向到HTTPS上。

与之相反，有些页面并不需要设置通过HTTPS传递。将首页声明为始终通过HTTP传送。
```java
.antMatchers("/").requiresInecure();
```
如果通过HTTPS发送了对"/"的请求，Spring Security将会把请求重定向到不安全的HTTP通道上。


### 10、防止跨站请求伪造

如果POST的请求来源于其他站点的话，跨站请求伪造(cross-site request forgery CSRF),简单来讲，如果一个站点欺骗用户提交请求到其他服务器上的话，就会发生CSRF攻击，这可能会带来消极的后果。从Spring Security 3.2开始，默认就会启用CSRF防护。实际上，除非你 采取行为处理CSRF防护或者将这个功能禁用。否则的话，在应用提交表单的时候会遇到问题。

Spring Security 通过一个同步的token的方式来实现CSRF防护的功能。它将会拦截状态变化的请求并检查CSRF token，如果请求中不包含 CSRF token的话，或者token不能与服务器端的token相匹配，请求将会失败，并抛出CsrfException异常。

这意味着在你的应用中，所有的表单必须在一个"_csrf"域中提交token，而且这个token必须要与服务器端计算并存储的token一致。这样的话当表单提交的时候，才能匹配。

好消息是Spirng Security已经简化了将token放到请求属性中这一任务。如果你使用Thymeleaf作为页面模板的话，只要<form>标签的action属性添加了Thymeleaf命名空间前缀 。那么就会自动生成一个“_csrf”隐藏域：

```xml
<form methos="POST" th:action="@{/spittles}"
..
</form>
```
如果使用JSP作为模板的话

```xml
<input typt="hidden"
		name="${_csrf.parameterName}"
		value="${_csrf.token}"
```
处理CSRF的另一种方式就是根本不去处理它，可以在配置中通过调用csrf()和.disable()禁用Spring Security的CSRF防护功能。

```java
@Override
protected void configure(HttpSecurity http) throws Exception {
				.and()
						.csrf()
						.disable()
}
```

需要提醒的是：禁用CSRF防护功能通常来讲并不是一个好主意。

### 10、认证用户

formLogin方法启用了基本的登录页功能

```java
@Override
protected void configure(HttpSecurity http) throws Exception {
	http
		.formLogin()    启用默认的登录页
		.and()
		.authorizeRequests()
			.antMatchers("/").authenticated()
			.antMatchers("/spitter/me").authenticated()
			.antMatchers(HttpMethod.POST, "/spittles").authenticated()
			.anyRequest().permitAll();
}
```


**添加自定义的登录页面**
```xml
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:th="http://www.thymeleaf.org">
  <head>
    <title>Spitter</title>
    <link rel="stylesheet"
          type="text/css"
          th:href="@{/resources/style.css}"></link>
  </head>
  <body onload='document.f.username.focus();'>
    <div id="header" th:include="page :: header"></div>

  <div id="content">

    <a th:href="@{/spitter/register}">Register</a>

  <form name='f' th:action='@{/login}' method='POST'>
   <table>
    <tr><td>User:</td><td>
        <input type='text' name='username' value='' /></td></tr>
    <tr><td>Password:</td>
        <td><input type='password' name='password'/></td></tr>
    <tr><td colspan='2'>
    <input id="remember_me" name="remember-me" type="checkbox"/>
    <label for="remember_me" class="inline">Remember me</label></td></tr>
    <tr><td colspan='2'>
        <input name="submit" type="submit" value="Login"/></td></tr>
   </table>
  </form>
  </div>
  <div id="footer" th:include="page :: copy"></div>
  </body>
</html>

```


需要注意的是,在Thymeleaf模板中，包含了username和Password输入域，就像默认的登录页一样，它也提交到了相对于上下文的“/login”页面上，因为这是一个Thymeleaf模板，因此隐藏了"_csrf"域将自动添加到表单中。


### 11、启用HTTP Basic认证
对于应用程序的人类用户来说，基于表单的认证是比较理想的，第十六章REST API 就不合适了。

HTTP Basic 认证会直接通过HTTP请求文本身，对要访问的应用程序的用户进行认证。当在Web浏览器中使用时，他将向用户弹出一个简单的模态对话框。

如果要启用HTTP Basic认证的话，只需在configure()方法所传入的HTTPSecurity对象上调用HTTPBasic()方法既可，另外还可以调用realmName()方法指定域。

```java
@Override
protected void configure(HttpSecurity http) throws Exception {
	http
		.formLogin()
			.loginPage("/login")
		.and()
		 .httpBasic()
			 .realmName("Spittr")
		.and()
```

在configure中，通过调用add()方法来将不同的配置指令连接在一起。

**启用remember-me**

不需要每次都认证了。
```java
.and()
.rememberMe()
	.tokenRepository(new InMemoryTokenRepositoryImpl())
	.tokenValiditySeconds(2419200)
	.key("spittrKey")
.and()
```

默认情况下，这个功能通过在cookie中存储一个token完成的，这个token最多两周有效。但是，在这里我们指定这个token最多四周有效 (2,419,200秒)。

在登录表单中，增加一个简单复选框就可以完成这件事
<input id="remember_me" name="remember-me" type="checkbox"/>
 <label for="remember_me" class="inline">Remember me</label>

 在应用中，与登录通用重要的就是退出，

 **退出功能**

 退出功能是通过Servlet的Filter实现的。这个Filter会拦截针对“/logout”的请求，因此，为应用添加退出功能只需要添加如下的链接即可。
 ```xml
 <a th:href="@{/logout}">Logout</a>
 ```

 当用户点击这个链接的时候 ，会发起对“/logout”的请求，这个请求会被Spring Security的LogouFilter所处理，用户会退出应用，所有的Remember-me token都会被清除。在退出完成后，用户浏览器将会重定向到“/login？logout”，从而允许用户在此登录

 如果你希望用户被重定向到其他的页面，如应用的首页，那么可以在configure()中进行如下的配置

 ```java
 @Override
protected void configure(HttpSecurity http) throws Exception {
		http
					.formLogin()
						.loginPage("/login")
					.and()
						.logout()
					.logoutSuccessUrl("/")
 ```

 到目前为止，我们已经看到了如何在发起请求的时候保护Web应用。接下来，我们将会看一下如何添加视图级别的安全性。

### 11、保护视图

Spring Security 的JSP标签库
- <security:accesscontrollist>  如果用户通过访问控制列表授予了指定的权限，那么渲染该标签的内容
- <security:authentication>     渲染当前用户认证的详细信息
- <security:authorize>          如果用户被授予了特定的权限那么渲染该标签的内容

为了使用标签库首先需要声明

```xml
<%@ taglib  prefix="security"
		url="http://www.springframework.org.security/tags"
```


待续，，好累，大家鼓励下我好吗？点赞，评论都可以。

### 12 小节(hahaha)

对于许多应用而言，安全性都是非常重要的切面。Spirng Security 提供了一种简单、灵活且强大的机制来保护我们的应用程序。

借助于一系列Servlet Filte，Spring Security 能够控制对Web资源的访问，包括Spring MVC控制器，借助于Spring Security的Java配置模型，我们不必直接处理Filter，能够非常简洁地声明为Web安全性功能。

当认证用户时，Spring Security提供了多种选项，我们探讨了如何基于内存用户库，关系型数据库和LDAP目录服务器来配置认证功能。如果这些可选方案无法满足你的需求的话，我们还学习力如何创建和配置自定义的用户服务。

在前面的几章中，我们看到了如何将Spring运用到应用程序的前端，在接下来的章中，我们还会继续深入这个技术栈，学习Spring如何在后端发挥作用，下一章将会首先从Spring的JDBC抽象开始。

 期待》》》》。。。。
