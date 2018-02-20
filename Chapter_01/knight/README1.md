# 第1章 Spring之旅

本章内容：

- Spring的bean容器
- 介绍Spring的核心模块
- 更为强大的Spring生态系统
- Spring的新功能

## 1.2 容纳你的Bean

在基于Spring的应用中，你的应用对象存在于Spring容器(container)中.Spring负责创建对象，装配它，并管理它们的整个生命周期，从生存到死亡(new 到finalize())。

**首先重要的是了解容纳对象的容器。理解容器将有助于理解对象是如何创建的。**

容器是Spring框架的核心。Spring容器使用DI管理构成应用的组件，它会创建相互协作的组件爱你之间的联系。这些对象更简单干净、更容易理解，更易于重用并且易于进行单元测试。

Spring容器并不只是只有一个，Spring自带了多个容器实现，可以归纳为两种不同的类型：
- Bean工厂。由` org.springframework.beans.factory.BeanFactory`接口定义的。是最简单的容器。
- 应用上下文 由`org.springframework.context.applicationContext`接口定义的。基于BeanFactory构建，并提供应用框架级别的服务，例如：从属性文件解析文本信息以及发布应用事件给感兴趣的事件监听器。

**应用上下文比Bean工厂更受欢迎。bean工厂对于大多数应用来说太低级了。**

### 1.2.1使用应用上下文
Spring自带了多种应用上下文：

- `AnonotationConfigApplicationContext`：从一个或多个基于Java的配置文件类中加载Spring应用上下文
- `AnnotationConfigWebApplicationContext`：从一个或多个基于Java配置类加载SpringWeb应用上下文
- `ClassPathXmlApplicationContext`：从类路径下的一个或多个XML配置文件中加载上下文定义，把应用上下文的定义文件作为类资源
- `FileSystemXmlapplicationContext`：从文件系统下的一个或多个XMl配置文件中加载上下文定义
- `XmlWebapplicationContext`：从web应用下的一个或多个XML配置文件中加载上下文定义

无论是从文件系统中装配应用上下文还是从类路径下装配应用上下文，将bean加载到bean工厂的过程都是相似的。

加载一个`FileSystemXmlApplicationContext`: 在文件系统的路径下查找knight.xml

```java
ApplicationContext context =
        new FileSystemXmlApplicationContext("c:/knight.xml");
```

也可以使用`ClassPathXmlApplicationContext`: 所有的类路径下查找knight.xml
```java
ApplicationContext context =
        new ClassPathXmlApplicationCOntext("knight.xml");
```

也可以从Java配置中加载应用上下文，那么可以使用`AnnotationConfigApplicationContext`

```java
ApplicationContext context = new  AnonotationConfigApplicationContext(
          com.guo.knights.config.KnightConfig.class);
```

应用上下文准备就绪之后，我们就可以调用上下文的getBean()方法从Spring容器中获取bean。


### 1.2.2 bean的生命周期

在传统的Java应用中，bean的生命周期很简单。使用Java关键字new进行bean实例化，然后bean就可以使用了。
一旦bean不再使用，则由JCM自动进行垃圾回收。

相比之下，Spring容器中的bean声明周期就显得复杂多了。正确理解Spring bean的生命周期非常重要，因为你或许要利用Spring提供的扩展点来自定义bean的创建过程。

![](https://i.imgur.com/HyZSL8O.png)

在bean工厂执行力若干启动步骤；
- 1、Spring对bean进行实例化

- 2、Spring将值和 bean的引用注入到bean对应的属性中。
- 3、如果bean实现类beanNameAware接口，Spring将bean的ID传给setBean-Name()方法
- 4、如果bean实现类BeanFactoryAware接口，Spirng将调用setBeanFatory()方法，将BeanFactory容器实例传入。
- 5、如果bean实现类applicationContextAware接口，Spring将调用setApplicationContext()方法，将bean所在的应用上下文的引用传递进来。
- 6、如果bean实现类BeanPostProcessor接口，Spring将调用它们的postProcessBeforInitialization()方法
- 7、如果bean实现类InitializingBean接口，Spring将调用它们的afterPropertiesSet()方法，如果类似的，如果bean使用init-method声明了初始化方法，该方法也会被调用。
- 8、如果bean实现类BeanPostProcessor接口，Spring将调用它们的PostProcessAfterInitialization()方法
- 9、此时，bean已经准备就绪，可以被应用程序使用了，他们将一直驻留在应用上下文中，直到该应用上下文被销毁。
- 如果bean实现类DisPosableBean接口，Spring将调用他的destroy()接口方法。同样，如果bean使用destroy-mothod声明销毁方法，该方法也会被调用。

## 1.3俯瞰Spring风景线

Srping框架通过关注于通过DI、AOP和消除模板样式代码来简化企业级Java开发。即使这是Spring所能作的全部事情，那么Spring也值得一用，**Spring实际上的功能超乎你的想象**

### 1.3.1 Spring模块

这些模块依据其所属的功能划分为6类不同的功能，总而言之，这些模块为开发企业及应用提供了所需的一切 。但是你也不必将应用建立在整个Spring框架上，你可以自由的选择合适自身应用需求的Spring模块：当Spring不能满足需求时，完全可以考虑其他选择，事实上，**Spring甚至提供了与其他的第三方框架和类库的集成点**,这样你就不需要自己编写代码了。

![](https://i.imgur.com/hTZUmkO.png)

**Spring**核心容器

容器是Spring最核心的部分，它管理者Spring应用中bean的创建、配置、管理。在该模块中，包括了Spring bean工厂，它为Spring提供了DI的功能，甚至bean工厂，我们还会发现有多种Spring应用上下文的实现，每一种都提供了配置Spring的不同方式。

所有的Spring模块都构建于核心容器之上。当你配置应用时，其实你隐式的使用率这些类。

**Spring**AOP模块
在AOP模块中，Spring对面向切面编程提供了丰富的支持。这个模块是Spring应用系统中开发切面的基础。与DI一样，AOP可以帮助应用对象解耦，借助于AOP，可以将遍布系统的应用的关注点(例如：事务，安全，日志)从它们所应用的对象中解耦出来。

**数据访问与集成**

使用JDBC编写代码通常会导致大量的样式代码，Spring的JDBC和DAO模块抽象类这些样板代码，是我们的数据库代码变得简单明了。还可以避免因为关闭数据库资源失败而引发的问题。该模块在多种数据库服务的错误信息之上构建了一个语义丰富的异常层，以后我们再也不需要解释那些隐晦专有的SQL信息了。

Spring提供了ORM模块，Spring的ORM模块建立在DAO的支持之上，并为多个ORM框架提供了一种构建DAO的简便方式 。Spring没有尝试去创建自己的ORM解决方，而是对许多流行的ORM框架进行了集成。包括Hibernater、Java Persisternce API、Java Data Object 和mybatis。Spring的事务管理支持所有的ORM框架以及JDBC。

**Web与远程调用**
MVC(Model-View-Controller)模块是一种普遍被接受的构建Web应用的方法，它可以帮助用户将界面逻辑与应用逻辑分离，Java从来不缺少MVC框架，Apache的struts2、JSF、WebWorks都是可选的最流行的MVC框架。Spring远程调用功能集成了RMI(Remote mehtod Invocation)、Hessian、CXF。Spring还提供了暴露和使用RESTAPI的良好支持。

**Instrumentation**
Spring的Instrumentation模块提供了为`JVM`添加代理(agent)的功能.具体来讲，它为Tomcat提供了一个织入代理，能够为Tomcat传递类文件，就像这些文件时被类加载器加载的一样。

**Testing**
通过该模块，你会发现Spring为JNDI、Servlet和Portlet编写单元测试提供了一系列的mock对象事项，对于继承测试，该模块为加载Spring应用上下文中的bean集合以及与Spirng上下文中的bean进行交互提供了支持。

### 1.3.2 Spring Portfolio

如果仅仅停留在和性的Spring框架层面，我们将错过Spring Portlio所提供的巨额财富。整个Spirng Portlio包括多个构建与核心Spring框架之上的框架和类库。概括的来讲，整个Spring Portlio几乎为每一个领域的Java开发都提供了Spring编程模型
- Spring Web Flow:是建立与Spring MVC框架之上，它为基于流程的会话式Web应用(购物车、向导功能)提供了支持。

- Spirng Security:安全度与许多应用都是一个非常关键的切面。利用SpringAOP，SpringSecurity为Spring应用提供了声明式的安全机制。
- Spring Data：使得在Spring中使用任何数据库都变得非常容易。一种新的数据库种类，通常被称为NoSQL数据库，提供了使用数据的新方法，为所中数据库提供了一种自动化的Repository机制，它负责为你创建Repository的实现
- Spring Boot： Spring极大的简化了众多编程的任务，减少甚至消除了很多样板式代码。Spring Boot大量依赖于自动配置技术，它能够消除大部分Spring配置。还提供了多个Starter项目，不管你是用Maven还是Gradle，这都能减少Spring的工程构建文件的大小。

## 1.4 Spring的新功能

### 1.4.1 Spring3.1新特性
Spring 3.1带来了多项有用的新特性 和增强，其中有很多都是关于如何简化个改善配置的。除此之外，Spring3.1还提供了声明式缓存的支持以及众多针对SpringMVC的的功能增强。
- 借助于profile，就能根据应用布置在什么环境之中选择不同的数据源
- 基于Java配置，Spring3.1增加了多个enable注解，启用Spring特定功能
- 对声明式缓存的支持，能够 使用简单的注解声明缓存的边界和规则，
- 开始支持Servlet3.0，包括在基于Java的配置中 申明Servlet和Filter，而不再借助于web.xml

### 1.4.2 Spring 3.2新特性

Spring 3.2主要关注Spring MVC的一个发布版本。

Spring MVC 3.2带来如下的功能提升
- 控制器(Controller)可以使Servlet3.0异步请求，允许在一个独立的线程中处理请求，从而就爱那个Servlet线程解放出来处理更多的请求
- @Autowired、@Value、@Bean注解能够作为元注解。用于创建自定义的注解和bean的申明注解
- Spring的声明式缓存提供了对JCache0.5的支持。

### 1.4.3 Spring 4.0新特性

- Spring提供了对WebSocket编程的支持，
- 新的消息模块，
- 支持Java8的新特性，比如：Lambda，函数式，
- 为 Groovy开发的应用程序提供了更加顺畅的编程体验
- 添加了条件化创建bean的功能
- Spring4.0包含了Spring RestTemplate的一个新的异步实现。它会立即返回并且允许在操作完成后执行回调
- 添加了对多项JEE规范的支持，包括JMS 2.0 、JTA1.2 JPA 2.1

### 1.4.4 Spring 5.0新特性
- 在Spring Framework代码中使用JDK 8特性
- 响应式编程是Spring Framework 5.0最重要的功能之一
- 除了响应式特性之外，Spring 5还提供了一个函数式Web框架。
- Spring Framework 5.0 引入了对 JetBrains Kotlin 语言的支持。

## 1.5 小节
**Spring致力于简化企业级开发Java开发、促进代码的松耦合。成功的关键在于依赖注入和AOP。**

DI是组装应用对象的一种方式，借助于这种方式对象无需知道依赖来自于何处或者依赖的具体实现方式。不同于自己获取依赖对象，对象会在运行期赋予它们所依赖的对象。依赖对象通常会通过接口了解所注入的对象，这样的话就能确保低耦合。

除了DI，还简单介绍了Spring对AOP的支持，AOP可以帮助应用将散落在各处的逻辑汇集于一处——切面。当Spring装配bean的时候，这些切面能够运行期编织起来，这样就能呢个非常有效的赋予bean新功能。

**依赖注入和AOP是Spring框架最核心的部分**，只有理解了如何应用Spring是最关键的功能。你才有能力使用Spring框架的其他功能。














































-
