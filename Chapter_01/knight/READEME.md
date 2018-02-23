# 第1章 Spring之旅

本章内容：
- Spring的bean容器
- 介绍Spring的核心模块
- 更为强大的Spring生态系统
- Spring的新功能

对于Java程序员来说，这是一个很好的时代..........

Spring是Java历史中很重要的组成部分。

在诞生之初，创建Spring的主要目的是用来代替更加重量级的企业级Java技术，尤其是EJB。相对于EJB来说，Spring提供了 **更加轻量级和简单的编程模型**.它增强了简单老式的Java对象(Plain Old Java Object POJO)的功能，使其具备了之前只有EJB和其他企业级Java规范才具有的功能。

尽管J2EE能够赶上Spring的步伐，但Spring也没有停止前进(我们程序员也一样，根本停不下来...),Spring继续在其他领域发展，移动开发、社交API集成、NoSQL数据库、云计算以及大数据都是Spring正在涉足和创新的领域。Spring的前景会更加美好(Java也是，Java9的模块化，只是我们需要学习的还有很多).

**对于Java开发者来说，这是一个很好的时代**

## 1.1简化Java开发

Spring是一个开源框架，最早由[Rod Johnson](https://twitter.com/springrod)创建，Spring是为了解决企业级应用开发的复杂性而创建的，使用Spring可以让简单的JavaBean实现之前只有EJB才能完成的事，但Spring不仅仅局限于服务器端的开发，任何Java应用都能在简单性、可测试性、和松耦合等方面从Spring中受益。

**一个Sping组件可以是任何形式的POJO。所有的理念都可以追溯到Spring最恨本的使命上：简化Java开发。**

为了降低Java开发的复杂性，Spring采取了以下4种关键策略：
- 基于POJO的轻量级和最小入侵性编程；
- 通过依赖注入和面向接口实现松耦合；
- 基于切面和惯例进行声明式编程：
- 通过切面和模板减少样板代码；

### 1.1.1 激发POJO的潜力
很多框架通过强迫应用继承他们的类或实现它们的接口从而导致应用于框架绑死。

Spring避免因自身的API而弄乱你的应用代码。Spring不会强迫你实现Spring所规范的接口或继承Spring所规范的类，相反，在基于Spring的构建的应用中，它的类通常没有任何迹象表明你使用了Spring。最坏的场景是，一个类或许会使用Spring注解，但它依旧是POJO。

```java
package com.guo.spring

public class HelloWordBean {
  public String sayHello() {
    return "Hello World"
  }
}
```

可以看到，这是一个简单普通的Java类——POJO。没有任何地方表明它是一个Spring组件。Spring的非侵入式编程模型意味着这个类在Spring应用和非Spring应用中都可以发挥同样的作用。

尽管简单，但POJO一样可以拥有魔力，Spring赋予POJO魔力的方式之一就是通过DI来装配它们。

### 1.1.2 依赖注入

依赖注入现在已经演变成**一项复杂的编程技巧或设计模式的理念**

任何一个有实际意义的应用都会由两个或更多的类组成，这些类相互之间进行协作来完成特定的业务逻辑。按照传统的做法，每个对象负责管理与自己相互协作的对象的引用(即它所依赖的对象)，这就会导致高度耦合和难以测试的代码。

```java
/**
 * Created by guo on 20/2/2018.
 * damsel表示：少女
 */
public class DamselRescuingKnight implements Knight {
    private RescueDamselQuest quest;
    public DamselRescuingKnight ( RescueDamselQuest quest) {
        //与RescueDamselQuest紧耦合
        this.quest = new RescueDamselQuest();
    }
    @Override
    public void embarkOnQuest() {
         quest.embark();
    }
}
```

DamselRescueingKnight在它的构造函数中自行创建了RescueDamselQuest。这使得两者紧紧的耦合在一起。因此极大的限制了骑士执行探险的能力。在这样一个测试中 ，你必须保证当骑士embarkOnQuest方法被调用的时候，探险embark方法也要被调用。但是没有一个简单明了的方式能够测试。

耦合具有两面性：
- 紧密耦合的代码难以岑氏，难以复用，难以理解，并且在典型的表现出"打地鼠"式的BUG特性,（修复一个bug，将会出现一个或更多的bug).
- 一定的程度耦合又是必须的，完全没有耦合的代码什么都做不了。为了完成更有实际意义的功能，不同的类必须以适当的方式进行交互，总而言之，**耦合是必须的，但需要谨慎对待**

通过DI，对象的依赖关系将由系统中负责协调各对象的第三方组件在创建对象的时候设定，对象无需自行创建或管理他们的依赖关系，依赖关系将被自动注入到需要它们的对象中。

**依赖注入会将所依赖的关系自动交给目标对象，而不是让对象自己去获取依赖**

BraveKnight足够灵活可以接受任何赋予他的探险任务。
```java
/**
 * Created by guo on 20/2/2018.
 */
public class BraveKnight implements Knight {
    public Quest quest;

    public BraveKnight(Quest quest) {          //Quest被注入进来
        this.quest = quest;
    }

    @Override
    public void embarkOnQuest() {
         quest.embark();
    }
}

```

不同于之前的DamselRescuingKnight，BraveKnight没有自行创建探险任务，而是在构造的时候把探险任务作为构造参数传入。这是依赖注入的方式之一，即构造注入(constructor injection).

需要注意的是，传入的探险类型是一个Quest，也就是所有的探险任务都必须实现的一个接口。所以BraveKnight能够响应RescueDamselQuest、SlayDragonQuest、MakeRoundTableRounderQuesst **等任意的Quest实现。**

这里的要点是BraveKnight没有有特定的Quest实现发生耦合。对他来说，被要求挑战的探险任务只要实现了Quest接口，那么具体的是那种类型就无关紧要了。这就是DI带来最大的收益——松耦合。**如果一个对象只通过接口(而不是具体的实现或初始化过程)来表明依赖关系，那么这种依赖就能够在对象本身毫不情况的情况下，用不同的具体实例进行替换。

对依赖进行替换的一个最常用方法就是在测试的时候使用mock实现。

```java

import static org.mockito.Mockito.*;

public class BraveKnightTest {
    @Test
    public void knightShouldEmbarkQuest() {
        Quest mockQuest = mock(Quest.class);               //创建mock Quest
        BraveKnight knight = new BraveKnight(mockQuest);   //注入mock Quest；
        knight.embarkOnQuest();
        verify(mockQuest,times(1)).embark();
    }
}
```

可以通过mock框架Mockito去创建一个Quest接口的mock实现。通过这个mock对象，就可以创建一个新的BraveKnight实例，并通过构造器注入到这个mock Quest。当调用embarkOnQUest方法时，你可以要求Mockito框架验证Quest的mock实现的embark方法仅仅被调用了一次。

**将Quest注入到Knight中**

希望BraveKnight所进行的探险任务是杀死一只怪龙，

```java
public class SlayDragonQuest implements Quest {
    private PrintStream stream;
    public SlayDragonQuest(PrintStream stream) {
        this.stream = stream;
    }
    @Override
    public void embark() {
        stream.println("Embarking on quest to slay the dragon!!,顺便还可以学英语，一举两得。");
    }
```
SlayDragonQuest实现类Quest接口，这样它就适合注入到BraveKnight中了，与其他入门不同的是，SlayDragonQuest没有使用System.out.println();,而是在构造方法中请求一个更为通用的PrintStream。

创建应用组件之间协作的行为成为装配。Spring有多种装配Bean的方式，采用XML是一种常用的方式。
knights.xml，该文件将BraveKnight，SlayDragonQuest和PrintStream装配到一起。
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
      http://www.springframework.org/schema/beans/spring-beans.xsd">
    <bean id="knight" class="guo.knights.BraveKnight">
        <constructor-arg ref="quest"/>                        <!--注入Quest bean-->
    </bean>
    <bean id="quest" class="guo.knights.SlayDragonQuest">     <!--创建SlayDragonQuest-->
        <constructor-arg value="#{T(System).out}"/>
    </bean>
</beans>
```
在这里，BraveKnight和SlayDragonQuest被声明为Spring中的bean。就BraveKnight bean来讲，他在构造时传入对SlayDragonQuest bean的引用，将其作为构造器参数。同时，SlayDragonQuest bean 的声明使用了Spring表达式语言(Spring Expression Language)，将System.out(一个PrintStream)传入到了SlayDragonQuest的构造器中，

在SpEL中, 使用T()运算符会调用类作用域的方法和常量. 例如, 在SpEL中使用Java的Math类, 我们可以像下面的示例这样使用T()运算符：

T(java.lang.Math)

T()运算符的结果会返回一个java.lang.Math类对象.

**Spring提供了基于Java的配置可作为XML的替代方案。**

```java

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
```
不管使用的是基于XML的配置还是基于Java的配置，DI所带来的收益都是相同的。尽管BraveKnight依赖于Quest，但是它并不知道传递给它的是什么类型的Quest，与之类似，SlayDragonQuest依赖于PrintStream，但是编译时，并不知道PrintStream长啥样子。只有Spring通过他的配置，能够了解这些组成部分是如何装配起来的。这样就可以在不改变 所依赖的类的情况下，修改依赖关系。

**接下来，我们只需要装载XML配置文件，并把应用启动起来。

**Spring通过应用上下文(Application context) 装载bean的定义，并把它们组装起来。Spring应用上下文全权负责对象的创建个组装，Spring自带了多种应用上下文的实现，他们之间的主要区别仅仅在于如何加载配置。**

因为knights.xml中的bean是使用XML文件进行配置的，所以选择ClassPathXmlApplicationContext作为应用上下文相对是比较合适的。该类加载位于应用程序类路径下的一个或多个Xml配置文件。

```java
public class KnightMain {
    public static void main(String[] args) {
        ClassPathXmlApplicationContext context =
                new ClassPathXmlApplicationContext("spring/knights.xml");    //加载Sprinig应用上下文
        Knight knight = context.getBean(Knight.class);                                       //获取knight bean
        knight.embarkOnQuest();                                                              //使用knight调用方法
        context.close();                                                                     //关闭应用上下文
    }
}

输出如下：
Embarking on quest to slay the dragon!!,顺便还可以学英语，一举两得。
```
这里的main()方法基于knight.xml文件创建了spring应用上下文。随后他调用该应用上下文获取一个ID为knighht的bean。得到Knighht对象的引用后，只需要简单调用embarkOnQuest方法就可以执行所赋予的探险任务了。只有knights.xml知道哪个骑士执行力那种任务。

### 1.1.3 应用切面

DI能够让相互协作的软件组件保持松耦合，而面向切面编程(aspect-oriented programming AOP) 允许你把遍布应用各处的功能分离出来形成可重用的组件。

面向切面编程往往被定义为促使**软件系统实现关注点的分离**一项技术,系统由许多不同的组件组成，每个组件各负责一特定的功能。除了实现自身核心的功能之外，这些组件还经常承担着额外的职责。诸如**日志、事务管理、和安全**这样的系统服务经常融入到自身具有核心业务逻辑的组件中。这些**系统通常被称为横切关注点。**，因此他们会跨越系统多个组件。

如果将这些关注点分散到多个组件中去，你的代码将会带来双重的复杂性。

- 实现系统关注点功能的代码嫁给你会重复出现在多个组件中。这意味着如果你要改变这些关注点的逻辑，必须修噶各个模块中相关的实现。即使你把这些关注点抽象成一个独立的模块，其他模块只是调用方法。但方法的调用还是会重复出现在各个模块中。

- 组件会因为那些与自身核心业务无关的代码而变得混乱。一个向地址薄增加地址条目的方法应该只关注如何添加地址。而不应该关注它是不是安全的，或者是否需要事务的支持。

**AOP能使这些服务模块化，并以声明的方式将它们应用到它们要影响的组件中去**。所造成的结果就是这些组件会具有哥哥你好的内聚性并且会更加关注自身的业务，安全不需要了解涉及系统服务所带来的复杂性。总之AOP能确保POJO的简单性。

我们可以把切面想象为覆盖在很多组件之上的一个外壳。应用是由哪些实现各自业务功能模块组成的，借助AOP，**可以使用各种功能层去包裹核心业务层，,这些层以声明的方式灵活的应用到系统中，你的核心应用甚至根本不知道他们的存在。**这是一个非常强大的理念，可以将安全，事务，日志关注点与核心业务相分离。**

![](https://i.imgur.com/BtJ8w6h.jpg)

每一个人都熟知骑士所做的任何事情，这是因为咏游诗人用诗歌记载了骑士的事迹并将其进行传唱。假设我们需要使用咏游诗人这个服务类来记载骑士的所有事迹。

咏游诗人是中世界的音乐记录器
```java
/**
 * Created by guo on 20/2/2018.
 * 咏游诗人，作为骑士的一个切面
 */
public class Minstrel {
    private PrintStream stream;

    public Minstrel(PrintStream stream) {
        this.stream = stream;
    }
    public void singBeforeQuest() {
        stream.println("Fa la la ,the Knight is so brabe");      //探险之前调用
    }
    public void singAfterQuest() {
        stream.println("Tee hee hhe,the brave knight " + "did embark on a quest");   //探险之后调用
    }
}
```
Minstrel只有两个简单的方法的类，在骑士执行每一个探险任务之前，singBeforeQuest()被调用；在骑士完成探险任务之后，singAfterQuest()方法被调用。在这两种情况下，Minstrel都会通过一个PrintStream类来歌颂骑士的事迹，这个类通过构造器注入进来。


 **但利用AOP，你可以声明咏游诗人西部歌颂骑士的 探险事迹，而骑士本身不直接访问Minstrel的方法**

 要将Minstrel抽象为一个切面，你所需要做的事情就是在一个Spring配置文件中声明它，，

 ```xml
 <bean id="minstrel" class="guo.knights.Minstrel">
    <constructor-arg value="#{T(System).out}"/>                                 <!--声明Minstrel bean-->
</bean>

 <aop:config>
     <aop:aspect ref="minstrel">

        <aop:pointcut id="embark" expression="execution(* * .embarkOnQuest(..))"/>     <!--定义切点-->

         <aop:after pointcut-ref="embark" method="singBeforeQuest"/>                  <!-- 声明前置通知-->

         <aop:after pointcut-ref="embark" method="singAfterQuest"/>                    <!-- 声明后置通知-->

     </aop:aspect>
 </aop:config>
 ```
 这里使用了Spring的aop配置命名空间把Minstrel声明为一个切面。

 在这两种方式中，pointcut-ref属性都引用列名为为“embark”的切入点，该切入点实在前面的<poiontcut>元素中定义的，并配置expression属性来选择所应用的通知。表达式的语法采用的是aspectJ的切点表达式语言。

 **Minstrel仍然是一个POJO，没有任何代码表明它要被作为一个切面使用，其次最重要的是Minstrel可以被应用到BraveKnight中，而BraveKnight不需要显示的调用它，实际上，BraveKnight完全不知道MInstrel的存在**
 ```java
 public class KnightAopMain {
    public static void main(String[] args) {
        ClassPathXmlApplicationContext context =
                new ClassPathXmlApplicationContext("spring/minstrel-AOP.xml");
       Knight knight = context.getBean(Knight.class);
       //Knight knight = (Knight) context.getBean("knight");
        knight.embarkOnQuest();
        context.close();
    }
}

输出如下：

Fa la la ,the Knight is so brabe
Embarking on quest to slay the dragon!!,顺便还可以学英语，一举两得。
Tee hee hhe,the brave knight did embark on a quest
 ```

### 1.1.4 小节
作者已经为我们展示了Spring通过面向POJO编程、DI、切面、模板技术来简化Java开发中的复杂性。在这个工程中，展示了基于XML的配置文件中如何配置bean和切面，但这些文件是如何加载的呢？他们被加载到哪里呢？接下来让我们了解下Spring容器，这是应用中的所有bean所驻留的地方。
























































待续....



















































































-
