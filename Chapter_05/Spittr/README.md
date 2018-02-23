# 第5章 构建Spring Web应用

本章内容：
- 映射请求到Spring控制器
- 透明的绑定表单数据
- 校验表单提交

系统面临的挑战：状态管理、工作流、以及验证都是需要解决的重要特性。HTTP协议的无状态决定了这些问题都不是那么容易解决。

Spring的Web框架就是为了帮你解决这些关注点而设计的。Spring MVC基于模型-视图-控制器(Model-View-Controller MVC)模式实现的，他能够帮你构建向Spring框架那样灵活和松耦合的Web应用程序。

在本章中，将会介绍Spring MVC Web框架，并使用新的Spring MVC注解来构建处理各种Web请求、参数、和表单输入的控制器。

## 5.1 Spring MVC起步

Spring将请求在调度Servlet、处理器映射(Handler Mappering)、控制器以及视图解析器(View resolver)之间移动，每一个Spring MVC中的组件都有特定的目的，并且也没那么复杂。

让我们看一下，请求是如何从客户端发起，经过Spring MVC中的组件，最终返回到客户端

### 5.1.1 跟踪Spring MVC

每当用户在Web浏览器中点击链接或提交表单的时候，请求就开始工作了。请求是一个十分繁忙的家伙，从离开浏览器开始到获取响应返回，它会经历很多站，在每站都会留下一些信息，同时也会带上一些信息。

![](https://i.imgur.com/5cHxVCF.png)

Spring工作流程描述[原文在这里](http://blog.csdn.net/zuoluoboy/article/details/19766131)

- 1. 用户向服务器发送请求，请求被Spring 前端控制Servelt DispatcherServlet捕获；

- 2. `DispatcherServlet`对请求URL进行解析，得到请求资源标识符（URI）。然后根据该URI，调用HandlerMapping获得该Handler配置的所有相关的对象（包括Handler对象以及Handler对象对应的拦截器），最后以`HandlerExecutionChain`对象的形式返回；

- 3. `DispatcherServlet` 根据获得的Handler，选择一个合适的HandlerAdapter。（附注：如果成功获得HandlerAdapter后，此时将开始执行拦截器的preHandler(...)方法）
-  4.  提取Request中的模型数据，填充Handler入参，开始执行Handler（Controller)。 在填充Handler的入参过程中，根据你的配置，Spring将帮你做一些额外的工作：
    - HttpMessageConveter： 将请求消息（如Json、xml等数据）转换成一个对象，将对象转换为指定的响应信息
    -  数据转换：对请求消息进行数据转换。如String转换成Integer、Double等
    -  数据根式化：对请求消息进行数据格式化。 如将字符串转换成格式化数字或格式化日期等
    -  数据验证： 验证数据的有效性（长度、格式等），验证结果存储到BindingResult或Error中
- 5.  Handler执行完成后，向DispatcherServlet 返回一个ModelAndView对象；
- 6.  根据返回的ModelAndView，选择一个适合的ViewResolver（必须是已经注册到Spring容器中的ViewResolver)返回给DispatcherServlet ；
- 7. ViewResolver 结合Model和View，来渲染视图
- 8. 将渲染结果返回给客户端。

[图片参考这里](http://blog.csdn.net/zuoluoboy/article/details/19766131)

![](https://i.imgur.com/DaqEiyL.png)


Spring工作流程描述
- 为什么Spring只使用一个Servlet(DispatcherServlet)来处理所有请求？
- 详细见J2EE设计模式-前端控制模式
- Spring为什么要结合使用`HandlerMapping`以及`HandlerAdapter`来处理Handler?
- 符合面向对象中的单一职责原则，代码架构清晰，便于维护，最重要的是代码可复用性高。如`HandlerAdapter`可能会被用于处理多种Handler。

----
1、请求旅程的第一站是Spring的`DispatcherServlet`。与大多数基于Java的Web框架一样，Spring MVC所有的请求都会通过一个前端控制器(front contrller)Servlet.前端控制器是常用Web应用程序模式。在这里一个单实例的Servlet将请求委托给应用的其他组件来执行实际的处理。在Spring MVC中，DisPatcherServlet就是前端控制器。


2、DisPactcher的任务是将请求发送Spring MVC控制器(controller).控制器是一个用于处理请求的Spring组件。在典型的应用中可能会有多个控制器，`DispatcherServlet`需要知道应该将请求发送给那个哪个控制器。所以Dispactcher以会查询一个或 多个处理器映射(Handler mapping),来确定请求的下一站在哪里。处理映射器根据请求携带的 URL信息来进行决策。

3、一旦选择了合适的控制器，`DispatcherServlet`会将请求发送给选中的控制器。到了控制器，请求会卸下其负载(用户提交的信息)并耐心等待控制器处理这些信息。(实际上，设计良好的控制器 本身只是处理很少，甚至不处理工作，而是将业务逻辑委托给一个或多个服务器对象进行处理)

4、控制器在完成处理逻辑后，通常会产生一些信息。这些 信息需要返回给 用户，并在浏览器上显示。这些信息被称为模型(Model),不过仅仅给用户返回原始的信息是不够的----这些信息需要以用户友好的方式进行格式化，一般会是HTML。所以，信息需要发送一个视图(View),通常会是JSP。

5、 控制器做的最后一件事就是将模型打包，并且表示出用于渲染输出的视图名。它接下来会将请求连同模型和视图发送回DispatcherServlet。

6、这样，*控制器就不会与特定的视图相耦合**传递给控制器的视图名并不直接表示某个特定的jsp。实际上，它甚至并不能确定视图就是JSP。相反，它仅仅传递了一个逻辑名称，这个名字将会用来查找产生结果的真正视图。DispatcherServlet将会使用视图解析器(View resolver),来将逻辑视图名称匹配为一个特定的视图实现，他可能也可能不是JSP

7、虽然DispatcherServlet已经知道了哪个驶入渲染结果、那请求的任务基本上也就完成了，它的最后一站是试图的实现。在这里它交付给模型数据。请求的任务就结束了。视图将使用模型数据渲染输出。这个输出通过响应对象传递给客户端(不会像听上去那样硬编码)

可以看到，请求要经过很多步骤，最终才能形成返回给客户端的响应，大多数的 步骤都是在Spirng框架内部完成的。


### 5.1.2 搭建Spring MVC

借助于最近几个Spring新特性的功能增强，开始使用SpringMVC变得非常简单了。使用最简单的方式配置Spring MVC；所要实现的功能仅限于运行我们所创建的控制器。

**配置DisPatcherServlet**

`DispatcherServlet`是Spirng MVC的核心，在这里请求会第一次接触到框架，它要负责将请求路由到其他组件之中。

按照传统的方式，像DispatcherServlet这样的Servlet会配置在web.xml中。这个文件会放到应用的war包中。当然这是配置`DispatcherServlet`方法之一。借助于Servlet 3规范和Spring 3.1 的功能增强，这种方式已经不是唯一的方案来。

我们会使用Java将DispatcherServlet配置在Servlet容器中。而不会在使用web.xml文件

```java
public class SpitterWebInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
    @Override
    protected String[] getServletMappings() {             //将DispatcherServlet映射到“/”
        return new String[]{"/"};
    }

    @Override
    protected Class<?>[] getRootConfigClasses() {
        return new Class<?> [] {RootConfig.class};
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class<?> [] { WebConfig.class};
    }
}
```

我们只需要知道扩展`AbstractAnnotationConfigDispatcherServletInitializer`的任意类都会自动的配置Dispatcherservlet和Spring应用上下文，Spirng的应用上下文会位于应用程序的Servlet上下文之中


在Servlet3.0环境中，容器会在类路径中 查找实现`javax.servlet.ServletContainerInitialzer`接口的类，如果能发现的话，就会用它来配置Servlet容器。


Spring提供了这个接口的实现名为`SpringServletContainnerInitialzer`,这个类反过来又会查找实现`WebApplicationInitialzer`的类，并将配置的任务交给他们来完成。Spring 3.2引入了一个遍历的`WebApplicationInitialzer`基础实现也就是`AbstractAnnotationConfigDispatcherServletInitializer`因为我们的`Spittr-WebApplicationInitialzer`扩展了`AbstractAnnotationConfigDispatcherServletInitializer`,(同时也就实现了`WebApplicationInitialzer`),因此当部署Servlet3.0容器的时候，容器会自动发现它，并用它来配置Servlet上下文


第一个方法`getServletMappings()`,它会将一个或多个路径映射到`DispatcherServlet`上，在本示例中，它映射的是“/”，表示它是应用默认的Servlet，它会处理应用的所有请求。


为了理解其他两个方法，我们首先需要理解`DispatcherServlet`和一个Servlet监听器(也就是ContextLoaderListener)的关系。

当`DispatcherServlet`启动的时候，它会创建应用上下文，并加载配置文件或配置类中声明的bean。在上面那个程序中的`getServletConfigClasses()`方法中，我们要求DispatcherServlet加载应用上下文时，使用定义在WebConfig配置类(使用Java配置)中的bean

但在Spring Web应用中，通常还会有另外一个应用上下文。另外这个就是由`ContextLoaderListener`创建.

我们希望`DispatcherServlet`加载包含Web组件的bean，如控制器，视图解析器，以及处理器映射，而`ContextLoaderListener`要加载应用中的其他bean。这些bean通常 是驱动应用后端的中间层和数据层组件。

实际上`AbstractAnnotationConfigDispatcherServletInitializer`会同时创建`DispatcherServlet`和`ContextLoaderListener`。`getServletConfigClasses()`方法会返回带有`@Configuration`注解的类将会用来定义DispatcherSerle应用上下文中的bean，`getRootConfigClasses()`会返回带有`@Configuration`注解的类将会用来配置`ContextLoaderListener`创建的应用上下文。

如果有必要两个可以同时存在，wex.xml和 `AbstractAnnotationConfigDispatcherServletInitializer`,但其实没有必要。

如果按照这种方式配置DispatcherServlet，而不是使用Web.xml的话，那么唯一的问题在于它能部署到支持Servlet3.0的服务器上才可以正常工作，如Tomcat7或更高版本，Servlet3.0规范在2009年12月份就发布了，

如果没有支持Servlet3.0，那别无选择了，只能使用web.xml配置类。


**启用Spring MVC**

我们有多种方式来启动DispatcherServlet，与之类似，启用Spring MVC组件的方式也不止一种，以前Spring是XMl进行配置的，你可以选择<mvc:annotation-driver>启用注解驱动的Spring MVC。

在第七章的时候会介绍<mvc:annotaion-driver>,现在会让Spring MVC搭建的过程尽可能简单，并基于Java进行配置。

我们所能创建最简单的Spring MVC配置就是一个带有@EnableWebMvc注解的类
```java
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@EnableWebMvc
public class WebConfig {
}
```
这可以运行起来，它的确能够启用Spring MVC，但还有不少问题要解决。

1、没有配置视图解析器，如果这样的话，Spring默认会使用BeanNameView-Resolver，这个视图解析器会查找ID与视图名称匹配的bean，并且查找的bean要实现View接口，它以这样的方式来解析视图。

2、没有启用组件扫描。这样的结果就是，Spirng只能找到显示声明在配置类中的控制器。

3、这样配置的话，DispatcherServlet会映射为默认的Servlet，所以他会处理所有的请求，包括对静态资源的请求，如图片 和样式表(在大多数情况下，这可能并不是你想要的结果)。

因此我们需要在WebConfig这个最小的Spring MVC配置上再加一些内容，从而让他变得真正实用。

```java
@Configuration
@EnableWebMvc                           //启用Spring MVC
@ComponentScan("com.guo.spittr.web")    //启用组件扫描
public class WebConfig extends WebMvcConfigurerAdapter {
    @Bean
    public ViewResolver viewResolver () {
        InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        //配置JSP视图解析器
        resolver.setPrefix("/WEB-INF/views/");
        resolver.setSuffix(".jsp");
        resolver.setExposeContextBeansAsAttributes(true);
        return resolver;
    }

    @Override
    //我们要求DispatcherServlet将静态资源的请求转发到Servlet容器中默认的Servlet上，
    //而不是使用DispatcherServlet本来来处理此类请求。
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        //配置静态资源的处理
        configurer.enable();
    }
}
```

第一件需要注意的是WebConfig现在添加了@ComponentScan注解，此时将会扫描`com.guo.spittr.web`包来查找组件。稍后你会看到，我们编写的控制器将会带有@Controller注解，这会使其成为组件扫描时的候选bean。因此，我们不需要在配置类中显示声明任何的控制器。

接下来，我们添加了一个ViewResolver bean，更具体的将是`InternalResourceViewResolver`。将会在第6章更为详细的讨论视图解析器。我们只需要知道他会去查找jsp文件，在查找的时候，它会在视图名称上加一个特定的前缀和后缀。(例如：名为home的视图会被解析为/WEB-INF/views/home.jsp)

最后新的WebConfig类还扩展里`WebMvcConfigurerAdapter`并重写了其`configureDefaultServletHandling()`方法,通过调用`DefaultServletHandlerConfigurer`的enable()方法，我们要求DispatcherServlet将静态资源的请求转发到Servlet容器中默认的Servlet上，而不是使用DispatcherServlet本来来处理此类请求。

WebConfig已经就绪，那么RootConfig呢？因为本章聚焦于Web开发，而Web相关的配置通过DisPatcherServlet创建的应用上下文都已经配好了，因此现在的RootConfig相对很简单：

```java
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
/**
 * Created by guo on 23/2/2018.
 */
@Configuration
@ComponentScan(basePackages = {"com.guo.spittr"},
    excludeFilters = {
        @Filter(type = FilterType.ANNOTATION,value = EnableWebMvc.class)})
public class RootConfig {

}
```

唯一需要注意的是RootConfig使用了@ComponentScan注解，这样的话，我们就有很多机会用非Web的组件来完善RootConfig。

### 5.1.3 Spittr应用简介

为了实现在线社交的功能，我们将要构造一个简单的微博(microblogging)应用，在很多方面，我们所构建的应用于最早的微博应用Twitter很类似，在这个过程中，我们会添加一些小的变化。当然我们使用Spirng技术来构建这个应用。

因为从Twitter借鉴了灵感并通过Spring来进行实现，所以它就有了一个名字：Spitter。

Spittr应用有两个基本的领域概念：Spitter(应用的用户)和Spittle(用户发布的简短状态更新)。当我们在书中完善Spittr应用的功能时，将会介绍这两个概念。在本章中，我们会构建应用的Web层，创建展现Spittle的控制器以及处理用户注册为Spitter的表单。

舞台已经搭建完成了，我们已经配置了DispatcherServlet，启用了基本的Spring MVC组件，并确定了目标应用。让我们进入本章的核心内容：使用Spring MVC 控制器处理Web请求。

## 5.2 编写 基本的控制器

在SpringMVC中，控制器只是在方法上添加了@RequestMapping注解的类，这个注解声明了他们所要处理的请求。

开始的时候，我们尽可能简单，假设控制器类要处理对/的请求，并对渲染应用的首页。

```java
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Created by guo on 24/2/2018.
 * 首页控制器
 */
@Controller
public class HomeController {
    @RequestMapping(value = "/",method = RequestMethod.GET)           //处理对“/”的Get请求
    public String home() {
        return "home";                                                //视图名为home
    }
}
```
**写完测试了下，好使，**

![](https://i.imgur.com/zxY3mlt.jpg)

你可能注意到第一件事就是HomeController带有@Controller注解，很显然这个注解是用来声明控制器的，但实际上这个注解对Spirng MVC 本身影响不大。

@Controller是一个构造型(stereotype)的注解。它基于@Component注解。在这里，它的目的就是辅助实现组件扫描。因为homeController带有@Controller注解，因此组件扫描器会自动去找到HomeController，并将其声明为Spring应用上下文中的bean。
```java
Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Controller {
	String value() default "";
}
```
其实你可以让HomeController带有@Component注解，它所实现的效果是一样的。但是在表意性上可能差一些，无法确定HomeController是什么组件类型。

HomeController唯一的一个方法，也就是Home方法，带有@RequestMapping注解，他的Value属性指定了这个方法所要处理的请求路径，method属性细化了它所能处理的HTTP方法，在本例中，当收到对‘/’的HTTP GET请求时，就会调用home方法。

home()方法其实并没有做太多的事情，它返回一个String类型的“home”，这个String将会被Spring MVC 解读为要渲染的视图名称。DispatcherServlet会要求视图解析器将这个逻辑名称解析为实际的视图。

鉴于我们配置`InternalResourceViewResolver`的方式，视图名“home”将会被解析为“/WEB-INF/views/home.jsp”

Spittr应用的首页，定义为一个简单的JSP
```xml
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="false" %>
<html>
  <head>
    <title>Spitter</title>
    <link rel="stylesheet"
          type="text/css"
          href="<c:url value="/resources/style.css" />" >
  </head>
  <body>
    <h1>Welcome to Spitter</h1>
    <a href="<c:url value="/spittles" />">Spittles</a> |
    <a href="<c:url value="/spitter/register" />">Register</a>
  </body>
</html>
```

**测试控制器最直接的办法可能是构建并部署应用，然后通过浏览器对其进行访问，但是自动化测试可能会给你更快的反馈和更一致的独立结果，所以，让我们编写一个针对HomeController的测试**


### 5.2.1 测试控制器

编写一个简单的类来测试HomoController。
```java
import static org.junit.Assert.*;
import org.junit.Test;

public class HomeControllerTest {
    @Test
    public void testHomePage() throws Exception {
        HomeController controller = new HomeController();
        assertEquals("home",controller.home());
    }
}
```

在测试中会直接调用home()方法，并断言返回包含 "home"值的String类型。它完全没有站在Spring MVC控制器的视角进行测试。这个测试没有断言当接收到针对“/”的GET请求时会调用home()方法。因为它返回的值就是“home”，所以没有真正判断home是试图的名称。

不过从Spring 3.2开始，我们可以按照控制器的方式进行测试Spring MVC中的控制器了。而不仅仅是POJO进行测试。Spring现在包含了一种mock Spirng MVC 并针对控制器执行 HTTP请求的机制。这样的话，在测试控制器的时候，就没有必要在启动Web服务器和Web浏览器了。


为了阐述如何测试Spirng MVC 容器，我们重写了HomeControllerTest并使用Spring MVC 中新的测试特性。

```java
import org.junit.Test;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Created by guo on 24/2/2018.
 */
public class HomeControllerTest1  {
    @Test                                                             //大家在测试的时候注意静态导入的方法
    public void testHomePage() throws Exception {
        HomeController controller = new HomeController();
        MockMvc mockMvc =  standaloneSetup(controller).build();       //搭建MockMvc
       mockMvc.perform(get("/"))                                      //对“/”执行GET请求，
               .andExpect(view().name("home"));                       //预期得到home视图
    }
}
```

这次我们不是直接调用home方法并测试它的返回值，而是发起了对"/"的请求，并断言结果视图的名称为home，它首先传递一个HomeController实例到MockMvcBuilders.strandaloneSetup()并调用build()来构建MockMvc实例，然后它使用MockMvc实例执行针对“/”的GET请求，并设置 期望得到的视图名称。

### 5.2.2 定义类级别的请求处理。

现在，已经为HomeController编写了测试，那么我们可以做一些重构。并通过测试来保证不会对功能造成什么破坏。我们可以做的就是拆分@RequestMapping，并将其路径映射部分放到类级别上

```java
@Controller
@RequestMapping("/")
public class HomeController {
    @RequestMapping(method = RequestMethod.GET)           //处理对“/”的Get请求
    public String home() {
        return "home";                                    //视图名为home
    }
}
```

在这个新版本的HomeController中，路径被转移到类级别的@RequestMapping上，而HTTP方法依然映射在方法级别上。当控制器在类级别上添加@RequestMapping注解时，这个注解会应用到控制器的所有处理器方法上，处理器方法上的@RequestMapping注解会对类级别上的@RequestMapping的声明进行补充。

就HomeController而言，这里只有一个控制器方法，与类级别的@RequestMapping合并之后，这个方法的@RequestMapping表明home()将会处理对 “/”路径的GET请求。

有了测试，所以可以确保在这个过程中，没有对原有的功能造成破坏。

当我们修改@RequestMapping时，还可以对HomeController做另一个变更。@RequestMapping的value接受一个String类型的数组。到目前为止，我们给它设置的都是一个String类型的‘/’。但是，我们还可以将它映射到对“/Homepage”的请求，只需要将类级别的@RequestMapping改动下

```java
@Controller
@RequestMapping({"/","/Homepage"})
public class HomeController {
  ...
}
```
现在，HomeController的home()方法可以被映射到对“/”和“/homepage”的GET请求上。

### 5.2.3 传递模型数据到视图中

到目前为止，就编写超级简单的控制器来说，HomeController已经是一个不错的样例了，但是大多数的控制器并不是那么简单。在Spring应用中，我们需要有一个页面展示最近提交的Spittle列表。因此，我们需要有一个新的方法来处理这个页面。

首先需要定义一个数据访问的Repository，为了实现解耦以及避免陷入数据库访问的细节中，我们将Repository定义为一个接口，并在稍后实现它(第十章)，此时，我们只需要一个能够获取Spittle列表的Repository，
```java
package com.guo.spittr.data;
import com.guo.spittr.Spittle;
import java.util.List;
/**
 * Created by guo on 24/2/2018.
 */
public interface SpittleRepository {
    List<Spittle> finfSpittles(long max, int count);
}
```
findSpittles()方法接受两个参数，其中max参数代表所返回的Spittle中，Spittle ID属性的最大值，而count参数表明要返回多少个Spittle对象，为了获得最新的20个Spittle对象，我们可以这样调用方法。

```java
List<Spittle> recent = SpittleRepository.findSpittles(long.MAX_VALUE(),20)
```

它的属性包括消息内容，时间戳，以及Spittle发布时对应的经纬度。

```java
public class Spittle {

  private final Long id;
  private final String message;
  private final Date time;
  private Double latitude;
  private Double longitude;

  public Spittle(String message, Date time) {
    this(null, message, time, null, null);
  }

  public Spittle(Long id, String message, Date time, Double longitude, Double latitude) {
    this.id = id;
    this.message = message;
    this.time = time;
    this.longitude = longitude;
    this.latitude = latitude;
  }

  //Getter和Setter略

  @Override
public boolean equals(Object that) {
  return EqualsBuilder.reflectionEquals(this, that, "id", "time");
}

@Override
public int hashCode() {
  return HashCodeBuilder.reflectionHashCode(this, "id", "time");
}
```

需要注意的是，我们使用Apache Common Lang包来实现equals()和hashCode()方法，这些方法除了常规的作用以外，当我们为控制器的处理器方法编写测试时，它们也是有用的。

既然我们说到了测试，那么我们继续讨论这个话题，并为新的控制器方法编写测试，

```java
@Test
 public void houldShowRecentSpittles() throws Exception {
   List<Spittle> expectedSpittles = createSpittleList(20);
   SpittleRepository mockRepository = mock(SpittleRepository.class);
   when(mockRepository.findSpittles(Long.MAX_VALUE, 20))
       .thenReturn(expectedSpittles);

   SpittleController controller = new SpittleController(mockRepository);
   MockMvc mockMvc = standaloneSetup(controller)
       .setSingleView(new InternalResourceView("/WEB-INF/views/spittles.jsp"))
       .build();

   mockMvc.perform(get("/spittles"))
      .andExpect(view().name("spittles"))
      .andExpect(model().attributeExists("spittleList"))
      .andExpect(model().attribute("spittleList",
                 hasItems(expectedSpittles.toArray())));
 }
/.................佩服老外，测试代码一大堆，省略了好多，好好研究下，..................../
 private List<Spittle> createSpittleList(int count) {
   List<Spittle> spittles = new ArrayList<Spittle>();
   for (int i=0; i < count; i++) {
     spittles.add(new Spittle("Spittle " + i, new Date()));
   }
   return spittles;
 }
}
```
测试首先会创建SpittleRepository接口的mock实现，这个实现会从他的findSpittles()方法中返回20个Spittle对象，然后将这个Repository注入到一个新的SpittleController实例中，然后创建MockMvc并使用这个控制器。

需要注意的是这个测试在MockMvc构造器上调用了setSingleView().这样的话，mock框架就不用解析控制器中的视图名了。在很多场景中，其实没必要这么做，但是对于这个控制器方法，视图和请求路径非常相似，这样按照默认的驶入解析规则，MockMvc就会发生失败，因为无法区分视图路径和控制器的路径，在这个测试中，构建InternalResourceViewResolver时所设置的路径是无关紧要的，但我们将其设置为`InternalResourceViewResolver`一致。

这个测试对“/spittles”发起Get请求，然后断言视图的名称为spittles并且模型中包含名为spittleList的属性，在spittleList中包含预期的内容。

当然如果此时运行测试的话，它将会失败。他不是运行失败，而是编译的时候就失败，这是因为我们还没编写SpittleController。


```java
@Controller
@RequestMapping("/spittles")
public class SpittleController {
    private SpittleRepository spittleRepository;

    @Autowired
    public  SpittleController(SpittleRepository spittleRepository) {              //注入SpittleRepository
        this.spittleRepository = spittleRepository;
    }
    @RequestMapping(method = RequestMethod.GET)
    public String spittles(Model model) {
        model.addAttribute(spittleRepository.findSpittles(Long.MAX_VALUE,20));     // 将spittle添加到视图
        return "spittles";                                                          // 返回视图名
    }
}
```
我们可以看到SpittleController有一个构造器，这个构造器使用@Autowired注解，用来注入SpittleRepository。这个SpittleRepository随后又在spittls()方法中，用来获取最新的spittle列表。

需要注意的是我们在spittles()方法中给定了一个Model作为参数。这样，spittles()方法就可以将Repository中获取到的Spittle列表填充到模型中，Model实际上就是一个Map(也就是key-value的集合)它会传递给视图，这样数据就能渲染到客户端了。当调用addAttribute()方法并且指定key的时候，那么key会根据值的对象类型来推断确定。

sittles()方法最后一件事是返回spittles作为视图的名字，这个视图会渲染模型。


如果你希望显示模型的key的话，也可以指定，
```java
@RequestMapping(method = RequestMethod.GET)
public String spittles(Model model) {
    model.addAttribute("spittleList",
        spittleRepository.findSpittles(Long.MAX_VALUE,20));     // 将spittle添加到视图
    return "spittles";                                          // 返回视图名
}
```

如果你希望使用非Spring类型的话，那么可以使用java.util.Map来代替Model
```java
@RequestMapping(method = RequestMethod.GET)
public String spittles(Map model) {
    model.addAttribute("spittleList",
        spittleRepository.findSpittles(Long.MAX_VALUE,20));     // 将spittle添加到视图
    return "spittles";                                          // 返回视图名
}
```

既然我们现在提到了各种可替代方案，那下面还有另外一种方式来编写spittles()方法

```java
@RequestMapping(method = RequestMethod.GET)
public List<String> spittles() {
  return spittleRepository.findSpittles(Long.MAX_VALUE,20));
}
```
这个并没有返回值，也没有显示的设定模型，这个方法返回的是Spittle列表。。当处理器方法像这样返回对象或集合时，这个值会放到模型中，模型的key会根据其类型推断得出。在本示例中也就是(spittleList)

逻辑视图的名称也会根据请求的路径推断得出。因为这个方法处理针对“/spittles”的GET请求，因此视图的名称将会是spittles，（去掉开头的线。）

不管使用哪种方式来编写spittles()方法，所达成的结果都是相同的。模型会存储一个Spittle列表，ket为spittleList，然后这个列表会发送到名为spittles的视图中。视图的jsp会是“/WEB-INF/views/spittles.jsp”

现在数据已经放到了模型中，在JSP中该如何访问它呢？实际上，当视图是JSP的时候，模型数据会作为请求属性放入到请求之中(Request) ,因此在spittles.jsp文件中可以使用JSTL(JavaServer Pages Standard Tag Library) 的<c:forEach>标签渲染spittle列表。

```xml
<c:forEach items="${spittleList}" var="spittle" >
  <li id="spittle_<c:out value="spittle.id"/>">
    <div class="spittleMessage"><c:out value="${spittle.message}" /></div>
    <div>
      <span class="spittleTime"><c:out value="${spittle.time}" /></span>
      <span class="spittleLocation">(<c:out value="${spittle.latitude}" />, <c:out value="${spittle.longitude}" />)</span>
    </div>
  </li>
</c:forEach>
```

尽管SpittleController很简单，但是它依然比homeController更进一步，不过，SpittleController和HomeController都没有处理任何形式的输入。现在，让我们扩展SpittleContorller，让它从客户端接受一些输入。

## 5.3 接受请求的输入

待续。。。  早安。。。。
