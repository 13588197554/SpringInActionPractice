# 装配Bean

本章内容：
- 声明bean
- 构造器注入和Setter方法注入
- 装配bean
- 控制bean的创建和销毁

任何一个成功的应用都是由多个为了实现某一个业务而相互协作的组件构成的，这些组件之间必须彼此了解，并且相互协作来完成任务。例如：在一个在线购物系统中，订单管理组件需要和产品管理组件以及信用卡认证组件之间谢灶，这些组件或许还需要访问数据组件协作，从数据库中读取数据以及把数据写入数据库。

创建应用对象之间的关联关系的传统做法(通过构造器或者查找)通常会导致结构复杂的代码，这些代码很难被复用也很难进行单元测试。最好的情况是这写组件之间高度耦合，难以复用和测试。

在Spring中，**对象无需自己查找或创建与其所关联的其他对象。相反，容器负责把需要相互协作的对象引用赋予各个对象。** 一个订单管理组件需要信用卡认证组件，但它不需要自己创建信用卡认证组件，容器会主动赋予它一个人在组件。

创建应用对象之间的哦协作关系的行为通常为装配(waring),这就是依赖注入的本质，

## 2.1 Spring配置的可选方案
Spring容器负责创建应用程序中的bean，并通过DI来写作这些对象之间的关系。你只需要告诉Spring要创建那些bean，并且如何组织在一起。当描述bean如何进行装配时，Spring具有非常大的灵活性，它提供了三种主要的装配机制：

- XML中进行显示配置
- 在Java中进行显示配置
- 隐式的bean发现机制自动装配

Spring的配置风格是可以互相搭配的，所以你可以选择使用XML装配一些bean，使用Spring基于Java的配置来装配一些bean，而让剩余的bean让Spring自动去发现。

建议使用：自动装配机制，显示配置越少越好。当你必须显示配置时，推荐使用类型安全并且比XML更加强大的JavaConfig。最后只有当你想要使用便利的XML命名空间，并且在JavaConfig中没有同样的实现时，才应用使用XML

## 2.2 自动化装配Bean

Spring从两个角度来实现自动化装配：

- 组件扫描(component scanning)：Spring会自动发现应用上下文中所创建的bean
- 自动装配(autowiring):Spirng自动满足bean之间的依赖

组件扫描和自动装配组合在一起就能发挥出强大的威力，他们能够将你的显示配置降低到最少。

## 2.2.1 创建被发现的bean

CD为我们阐述了DI是如何运行提供了一个很好的样例，如果你不讲CD插入(注入)到CD播放器中，那么CD播放器其实没有太大的用处。CD播放器依赖于CD才能完成他的使命


```java
public interface CompactDisc {
    void play();
}
```
CompactDisc的具体内容不重要，重要的是你将其定义为一个接口，作为接口，它定义了CD播放器对一盘CD所能进行的操作。**它将CD播放器的任意实现与CD本身耦合降低到了最小的程度**

```java
/**
 * Created by guo on 21/2/2018.
 */
@Component    //表明该类会作为组件类
public class SgtPeppers implements CompactDisc {
    private String title = "sgt. pepper lonely Hearts Club Band";
    private String artist = "The Beatles";
    @Override
    public void play() {
        System.out.println("Playing" + title + "by" + artist);
    }
}
```
SgtPeppers类上使用了@Component注解，这个简单的注解表明该类会作为组件类，并告知Spring要为这个类创建bean。没有必要显示配置他。不过组件扫描是不开启的，需要显示配置一下Spring，从而命令它去寻找带有@Component注解的类，并为其创建bean。

```java
@Configuration
@ComponentScan
public class CDPlayerConfig {
}
```

类CDPlayConfig通过Java代码定义了Spring的装配规则。@ComPonentScan注解可以能够在Spring中启动组件扫描。如果没有其他配置，@ComponentScan默认会扫描与配置类相同的包。Spring将会扫描这个包以及这个包下所有的子包。查找带有@Component注解的类。这样就能发现CompactDisc，并且会在Spring中自动创建一个bean。


使用XMl来启用组件扫描的话，可以使用Spring context命名空间的<context:component-scan>元素，
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
.....
    <context:component-scan base-package="com.guo.soundsystem" />

</beans>
```

尽管我们可以通过XMl的方案来启用组件扫描，但是在后面的讨论中，更多的还是会使用基于Java的配置。

为了测试组件扫描功能 创建一个简单的JUnit测试，它会创建Spring上下文，并判断CompactDisc是不是真的创建出来了。
```java
package com.guo.soundsystem;

import com.guo.soundsystem.CDPlayerConfig;
import com.guo.soundsystem.CompactDisc;

import static org.junit.Assert.*;     //这里使用了静态导入
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by guo on 21/2/2018.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CDPlayConfig.class)
public class CDPlayTest {
    @Autowired
    private CompactDisc cd;

    @Test
    public void  cdShouldNotBeNull() {
        assertNotNull(cd);
    }
}

```

CDPlayerTest使用了Spring的SpringJUnitClassRunner，以便在测试开始的时候自动创建Spring的上下文。注解@ContextConfiguration会告诉它需要在CDPlayerConfig中加载配置.带有@Autowired注解，以便将CompactDisc bean注入到测试代码中，最后断言cd属性不为null，就意味着Spirng能够发现CompactDisc类，自动在Spring应用上下文中将其创建为bean并将其注入到测试代码中。，

### 2.2.2 为组件扫描的bean命名

Spring应用上下文中所有bean都会给定一个ID。尽管没有明确为SgtPeppers bean 设置ID，但Spring会根据类名为其指定一个ID，这个ID所给定的ID为sgtPeppers，也就是将类名的第一个字母变为小写。

如果想设置不同的ID，所需要做的就是将期望的ID作为值传递给@Component注解

```java
@COmponent("lonelyHeartsClub")
public class SgtPeppers implements CompactDisc {
  ...
}
```

还有另外一种为bean命名的方式，这种方式不使用@Component注解，而是使用Java依赖注入规范(Java Dependency Injection) 中所提供的@Named注解为bean设置ID：

```java
import javax.inject.Named
@Named("lonelyHeartsClub")
public class SgtPeppers implements CompactDisc {
  ...
}
```
Spring支持将@named作为@Component注解的替代方案。两者之间有一些细微的差异，但是在大多数场景中，他们是可以互相替换的。

《Spring 实战》作者更喜欢@Component注解，而对于@Named。。。感觉名字起的很不好

### 2.2.3 设置组件扫描的基础包

到目前为止，我们没有为@ComponentScan设置任何属性，这意味着，按照默认规则，它会以配置类所在的包作为基础包(base package) 来 扫描组件，有一个原因会促使我们明确的设置基础包，那就是我们要将配置类放在单独的包中，使其与其他应用代码区分开来。

为了指定不同的基础包，你所需要做的就是在@ComponentScan的Value属性中指明包的名称
```java
@Configuration
@Componentscan("com.guo.soundsystem")
public class CDPlayerConfig{}
```

如果你想更加清晰的表明你所设置的是基础包，那么你可以通过basePackages属性来进行设置
```java
@Configuration
@Componentscan(basePackages="com.guo.soundsystem")
public class CDPlayerConfig{}
```

@basePackages属性使用的是复数形式，以为这可以设置多个基础包，只需要将basePackages属性设置为要扫描包的一个数组就可以
```java
@Configuration
@Componentscan(basePackages={"com.guo.soundsystem","com.guo.video"})
public class CDPlayerConfig{}
```

在上面所有的例子中，所设置的基础包都是以String类型表示的，**作者认为这是可以的，但是这种方式是类型不安全的，如果你要重构代码的话，那么你所指定的基础包可能就会出现错误。**

除了将包设置为简单的String类型之外，@ComponentScan还提供了另外一种方式，那就是将其**设置为包中所包含的类或接口**

```java
@Configuration
@Componentscan(basePackageClasses={CDPlayer.class,DVDPlayer.class})
public class CDPlayerConfig{}
```
尽管在样例中，为basePackageClasses设置的是组件类，但是可以考虑在包中创建一个用来扫描的空标记接口。通过标记接口的方式，你依然能够保持对重构友好的接口的引用，但是可以避免引用任何实际的应用程序代码。

在你的应用程序中，如果所有的对象都是独立的，彼此之间没有任何依赖，就像SgtPeppers bean 这样，那么你需要的可能就是组件扫描而已。但是很多对象会依赖其他的对象才能完成任务。这样的话我们就需要有一种方式能够将组件扫描到的bean和他们的依赖装配在一起。**自动装配**

### 2.2.4 通过为bean添加注解实现自动装配

自动装配就是让Spring自动满足bean依赖的一种方法，在满足的依赖的过程中，会在Spring应用上下文寻找匹配某个bean需求的其他bean。为了声明要进行自动装配，我们可以将可以借助于Spring提供的@Autowired注解。

```java
@Component
public class CDPlayer implements MediaPlayer {
    private CompactDisc cd;
    @Autowired
    public  CDPlayer(CompactDisc cd) {
        this.cd = cd;
    }
    @Override
    public void play() {
        cd.play();
    }
}
```
在构造器上添加了@Autowired注解，这表明当Spring创建CDPlayer bean时，会通过这个构造器来实例化并且传入一个可设置给CompactDisc类型的bean

@Autowired注解不仅可以用于构造器上，还能用在属性Setter方法上，

```java
@Autowired
public void setCompactDisc(CompactDisc cd)｛
  this.cd = cd;
｝
```

在Spring初始化bean之后，它会尽可能去满足bean的依赖，依赖是通过带有@Autowired注解的方法进行生命的，

实际上，Setter方法并没有什么特殊之处。@Autowired可以用在类的任何方法上，
```java
@Autowired
public void insertDisc(CompactDisc cd)｛
  this.cd = cd;
｝
```
不管是构造器、Setter方法还是其他方法，Spring购汇尝试满足方法参数上所申明的依赖。假如有且只有一个满足需求依赖的话，那么这个bean将会被装配进来，

如果没有匹配的bean，那么在应用上下文创建的时候，Spring会抛出一个异常，为了避免异常的出现，你可以将@Autowired的required属性设置为false

```java
@Autowired(required=false)
public  CDPlayer(CompactDisc cd) {
    this.cd = cd;
}
```
将required设置为false时，Spring会尝试执行自动装配，但如果没有匹配的bean，Spring将会让这个bean处于未装配的状态，需要谨慎对待，**如果你的代码中没有进行null检查的话，这个处于未装配的属性可能会出现NullPointerException。**

**如果有多个bean满足依赖关系时，Spirng将会抛出异常，表明没有明确指定要选择安格bean进行装配。**

@ Autowired是Spring特有的注解，如果你不喜欢在代码中使用@Autowired，那么你可以考虑使用@Inject

```java
@Named
public class CDPlayer implements MediaPlayer {
    private CompactDisc cd;
    @Inject
    public  CDPlayer(CompactDisc cd) {
        this.cd = cd;
    }
    @Override
    public void play() {
        cd.play();
    }
}
```

@Inject注解来源于Java依赖注入规范，该规范同时还为我们定义了@Named注解，在Spring自动装配中，Spirng同时支持@Inject和@Autowired主机，作者没有特别的偏向性。根据自己的情况，选择合适自己的。

### 2.2.5 验证自动装配

```java
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = CDPlayerConfig.class)
public class CDPlayerTest {
    @Rule
    public final StandardOutputStreamLog log = new StandardOutputStreamLog();
    @Autowired
    private MediaPlayer player;
    @Autowired
    private CompactDisc cd;

    @Test
    public void  cdShouldNotBeNull() {
        assertNotNull(cd);
    }

    @Test
    public void play() {
        player.play();
        assertEquals(
                "Playing Sgt. Pepper's Lonely Hearts Club Band by The Beatles\n",
                log.getLog());
    }
}
```

在测试代码中使用System.out.println()是稍微有些棘手的是，该样例中使用了StandardOutputStreamLog，这是来源于System Rules 库的一个JUnit规则，该规则能够基于控制台的输出编写断言。
















































































































-
