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






































































-
