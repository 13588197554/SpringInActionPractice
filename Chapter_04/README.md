# 面向切面的Spring

本章主要内容：
- 面向切面编程的基本原理
- 通过POJO创建切面
- 使用@Aspect注解
- 为AspectJ切面注入依赖。

软件系统中的一些功能就像我们家里的电表一样。则和谐功能需要用到应用程序的多个地方。但是我们又不想在每个点都明确调用它。日志、安全、事务管理的确很重要。但它们是否为应用对象主动参与的行为呢？如果让应用对象只关注与自己所针对的业务领域问题，而其他方面的问题由其他应用对象来处理，这样不更好吗？

在软件开发中，散布于应用中多出功能被称为横切关注点(crosscutting concern)。通常来讲横切关注点从概念上是与应用的业务逻辑分离的。但往往是耦合在一起的，**把这些横切关注点与业务逻辑相分离正是面向切面编程(AOP)所要解决的问题。**

依赖注入(DI)管理我们的应用对象，DI有助于应用对象之间解耦。而AOP可以实现横切关注点与它们所影响的对象之间的耦合。

## 4.1 什么是面向切面编程

切面能够帮我们模块化横切关注点。简而言之，横切关注点可以被描述为影响应用多处的功能。例如 安全，事务、日志等功能。

如果要重用对象的话，最常见的面向对象技术是继承、委托、组合。但是，如果整个应用中都使用相同的基类，继承往往会导致一个脆弱的对象体系。而使用委托可能需要委托对象进行复杂的调用。

切面提供了取代继承和委托的另一种可选方案。在使用面向切面编程时，我们仍然在一个地方定义通知功能，而无需修改受影响的类。**横切关注点可以被模块化为特殊的类，这些类被称为切面(aspect).** 这样做带来两个好处：每个关注点都集中到一个地方，而不是分散到多处代码中：其次，服务模块更简洁，因为它只包含了主要关注点(核心功能)的代码。而次要关注的代码被移到切面中了。

### 4.1.1 定义AOP术语

描述切面的常用术语有：通知(advice)、切点(pointcut)、(连接点)。

**通知(advice)**

通知定义了切面是什么以及何时使用。除了描述切面要完成的工作外，通知还解决了何时执行这个工作问题。它应该在某个方法被调用之前？之后？之前和之后都调用？还是只在方法抛出异常时调用？

Spring切面可以应用5中类型的通知：

- 前置通知(Before):在目标方法被调用之前调用通知功能。
- 后置通知(After):在目标方法完成之后调用通知
- 返回通知(After-returning):在目标方法成功执行之后调用通知
- 异常通知(After-throwing):在目标方法抛出异常后调用通知
- 环绕通知(Around):在被通知方法调用之前和调用之后执行自定义的行为

**连接点**

我们的应用可能有数以千计的时机应用通知，这些时机被称为连接点。连接点是在应用执行过程中能够插入的一个点。这个点可以是调用方法时，抛出异常时，甚至修改一个字段时。切面可以利用这些点插入到应用的正常流程之中，并添加新的行为。

**切点**

如果说通知定义了切面的的“什么”和“何时”，那么切点定义了“何处”。切点的定义会匹配通知所要织入的一个或多个连接点。

**切面**

切面是通知和切点的结合。通知和切点通过定义了切面的全部 内容——他是什么，在什么时候和在哪里完成其功能。

**引入**
引入允许我们向现有的类添加新的方法或者属性。

**织入**

织入是把切面应用到目标对象并创建新的代理对象的过程。切面在指定的连接点被织入到目标对象。在目标对象的生命周期里有多个点可以进行织入：
- 编译器：切面在目标类编译时被织入。Aspect的织入编译器就是以这种方式织入切面的。
- 类加载器：切面在目标类加载到JVM时被织入。需要特殊的类加载(Classloader)，它可以在目标类被引入之前增强该目标类的字节码(CGlib)
- 运行期：切面在应用运行时的某个时刻被织入。AOP会为目标对象创建一个代理对象

**通知包含了需要用于多个应用对象的横切关注点。连接点是程序执行过程中能够应用通知的所有点。切点定义了通知被应用的具体位置(在哪些连接点)，其中关键是切点定义了哪些连接点会得到通知。**


### 4.1.2 Spring对AOP的支持

并不是所有的AOP框架都是相同的，他们在连接点模型上可能有强弱之分。有些允许在字段修饰符级别的通知，而另一些只支持与方法调用相关的连接点。它们织入切面的方式和时机也有所不同。但是，无论如何，创建切点来定义切面所织入的连接点是AOP的基本功能。

Spring提供了4种类型的AOP支持：
- 基于代理的经典Spring AOP
- 纯POJO切面
- @AspectJ注解驱动的切面
- 注入式AspectJ切面

前三种都是Spirng AOP实现的变体，Spring AOP构建在动态代理基础上。因此，Spring对AOP的支持局限于方法拦截。

引入了简单的声明式AOP与基于注解的AOP之后，Spring经典的看起来就显得非常笨拙和过于复杂话，直接使用ProxyFactory bean 会让人感觉厌烦。

借助于Spring的aop命名空间，我们可以将纯POJO转为切面。

Spring借鉴了AspectJ的切面，以提供注解驱动的AOP。本质上，它依然是Spring基于代理的AOP，但是编程模型几乎与编写成熟的AspectJ注解切面完全一致。这种AOP风格的好处在于能够不使用XML来完成功能。


Spring所创建的通知都是用标准的Java类编写的，定义通知所应用的切点通常会使用注解或在Spring配置文件里采用XML来编写

通知带代理类中包裹切面，Spring在运行时把切面织入到Spring所管理的bean中。代理类封装了目标类，并拦截被通知方法的调用。再把调用转发给真正的目标bean。当代理拦截到方法调用时，在调用目标bean方法之前，会执行切面逻辑。直到应用需要被代理bean时，Spring才会创建代理对象。如果使用ApplicationContext的话，在ApplicationContext从BeanFactory中加载所有的bean的时候，Spring才会创建被代理的对象。因为Spirng运行时才创建代理对象，所以我们不需要特殊的编译器来织入Spring AOP的切面。

Spring基于动态代理，所以Spring只支持方法连接点。方便拦截可以满足大部分的需求。

## 4.2 通过切点来选择连接点

切点用于准确定位应该在什么地方应用切面的通知。通知和切点是切面最基本的元素。

Spring仅支持AspectJ切点指示器的一个子集。Spring是基于代理的，而某些切点表达式是基于代理的AOP无关的。

Spring支持的指示器，只有execution指示器是实际执行匹配的，而其他的指示器都是用来限制匹配的。这说明execution指示器是我们在编写切点定义时最主要的指示器。

### 4.2.1编写切点

为了阐述Spring中的切面，  我们需要有个主题来定义切面的切点。

```java
package com.guo.cocert;
public interface Performance {
  public void perform();
}
```
```java
execution(* concert.Performance.perform(..))
```

我们使用execution()指示器选择Performance的perform()方法，方法表达式以"*"号开始，表明了我们不关心方法返回值的类型。然后指明了全限定类名和方法名，对于方法参数列表，我们使用了两个点号(..)表明切点要选择任意的perform()方法，无论该方法的入参是什么。

现在假设我们需要配置的切点仅匹配concert包，可以使用within()指示器

```java
execution(* concert.Performance.perform(..)) && within(concert.*)
```

因为“&”在XMl中有特殊的含义，所以在Spring和XML配置中，描述切点时，可以使用and代替“&&”。

### 4.2.2 在切点中选择bean
Spring引入了一个新的bean()指示器，它允许我们在切点表达式中使用bean的ID来标识bean。bean()使用bean ID 或 bean 名称作为参数来限制切点只匹配特定的bean。

```java
execution(* concert.Performance.perform(..)) and bean("woodsotck")
```

也可以这样
```java
execution(* concert.Performance.perform(..)) and ！bean("woodsotck")
```
切面的通知会被编织到所有ID不为woodsotck的bean中。


## 4.3使用注解创建切面

使用注解来创建切面是AspectJ 5所引入的关键特性。

### 4.3.1 定义切面

如果一场演出没有观众的话，那不能称之为演出。

```java
@AspectJ
public class Audience {

}
```

Audience类使用@AspectJ注解进行了标注。该注解表明Audience不仅仅是一个POJO，还是一个切面。Audience类中的方法都是使用注解来定义切面的具体行为。


```java
@AspectJ
public class Audience {
  @Pointcut("execution(* * concern.Performance.perform(..))")
  public void performance() {};
}
```

在Autience中，performance()方法使用了@Pointcut注解。为@Pointcut注解设置的值是一个切点表达式，就像之前在通知注解上所设置的那样。

需要注意的是，除了注解和没有实际操作的performa()方法，Audience类依然是一个POJO，我们能够像使用其他的Java类那样调用它的方法，它的方法也能独立的进行单元测试。与其他Java类没有什么区别。

像其他的Java类一样，它可以装配为Spring中的bean
```java
@Bean
public Audience audience() {
  return new Audience();
}
```

如果你就此止步的话，Audience只会是Spring容器中的一个bean。即便使用了AspectJ注解，但它并不会被视为切面，这些注解不会解析，也不会创建将其转化为切面的代理。

如果你使用JavaConfig的话，可以在配置类的级别上通过使用`EnableAspectJ-AutoProxy`注解启用自动代理功能。

```java
@Configuration
@EnableAspectJAutoProxy             //启用AspectJ自动代理
@ComponentScan
public class ConcertConfig {
  @Bean
  public Audience autidence() {     //声明Audience bean
    return new Audience();
  }
}
```

假如你在Spring中使用XMl来装配bean的话，那么需要使用Spring aop命名空间中的<aop:aspect-autoproxy>元素

```xml
<?xml version="1.0" encoding="UTF-8"?>

、、、、、、、、、、、、、、、、、、、、、、、、

<context:component-scan base-package="com.guo.concert"/>
<aop:aspect-autoproxy/>
<bean class="com.guo.concert.Audience"/>
```
不管你使用JavaConfig还是XML，AspecJ自动代理都会使用@Aspect注解的bean创建一个代理。这个代理会围绕着所有该切面的切点所匹配的bean。

我们需要记住的是，Spring的AspectJ自动代理仅仅使用@AspectJ作为创建切面的指导，切面依然是基于代理的。本质上它依然是Spring基于代理的切面。

### 4.3.2 创建环绕通知

环绕通知是最为强大的通知类型，它能够让你编写的逻辑将被通知的目标方法安全包装起来，实际上就像在一个通知方法中同时编写前置通知和后置通知。
```java
@AspectJ
public class Audience {
  @Pointcut("execution(* * concern.Performance.perform(..))")
  public void performance() {};
  @Around
  public void xx(Xxx jp) {
    .......
    jp.proced()
  }
}
```
在这里，@Around注解，表明这个xx()方法会作为performance()切点的环绕通知。

这个通知所达到的效果与之前的前置通知和后置通知是一样的。

需要注意的是，别忘记调用proceed()方法，如果不调用这个方法，那么你的通知实际上会阻塞对被通知方法的调用，有意思的是，你可以不调用proceed方法，从而阻塞对被通知方法的反问，

### 4.3.4 通过注解引入新功能

一些编程语言，例如：Ruby和Groovy，有开放来的理念，它们可以不直接使用修改对象或类的定义就能够为对象或类增加新的方法。不过Java并不是动态语言，一旦编译完成了，就很难在为该类添加新的功能了。

如果切面能够为现有的方法增加额外的功能，为什么不恩那个为一个对象增加新的方法呢？利用引入AOP的概念，切面可以为Spring bean 添加新的方法。

在Spring中，注解和自动代理提供了一种便利的方式来创建切面，它非常简单，并且只设计最少的Spring配置，但是，面向注解的切面有一个明显的不足点：你必须能够为通知类添加注解，为了做到这一点，必须要有源码。

## 4.4 在XML中声明切面

之前，有这样一条原则：那就是基于注解的配置要优于Java的配置，基于Java的配置要优于XMl的配置，但是，如果你需要声明切面，但是又不能为通知类添加注解的时候 ，那么就必须转向XML配置了。

在Spring的aop命名空间中，提供了多个元素用来在XML中声明切面，

- <aop:advisor>           :定义AOP通知器
- <aop:after>             :定义AOP后置通知
- <aop:after-returning>   :定义AOP返回通知
- <aop:after-throwing>    :定义AOP异常通知
- <aop:around>            :定义AOP环绕通知
- <aop:aspect>            :定义一个切面
- <aop:aspectj-autoproxy> :启用@AspectJ注解
- <aop:before>            :定义一个AOP前置通知
- <aop:poiontcut>         :定义一个切点

### 4.4.1 声明前置通知和后置通知
我们会使用Spring aop命名空间中的一些元素，将没有注解的Aurience类转为切面

```xml
<aop:config>
    <aop:aspect ref="audience">       <!--引用audience Bean-->

        <aop:before pointcut="execution(* * concert.Performance.perform(..))" method="silenceCellIphones"/>

        <aop:before pointcut="execution(* * concert.Performance.perform(..))" method="takeSeats"/>

        <aop:after-returning pointcut="execution(* * concert.Performance.perform(..))" method="applause"/>

        <aop:after-throwing pointcut="execution(* * concert.Performance.perform(..))" method="demandRefund"/>

    </aop:aspect>
</aop:config>
```

第一需要注意的就是大多数AOP配置元素必须在<aop:config>元素的上下文中使用。

在所有的通知元素中，pointcut属性定义了通知所应用的切点，它的值是使用AspectJ切点表达式语法所定义的切点。

在基于Aspectj注解的通知中，当发现在这些类型的重复时，使用@Pointcut注解来消除这些重复的内容。

如下的XMl配置展示了如何将通用的切点表达式抽取到一个切点声明中，这样，这个声明就能在所有的通知元素中使用了

```xml
<aop:config>
    <aop:aspect ref="audience">       <!--引用audience Bean-->
        <aop:pointcut id="performance" expression="execution(* * concert.Performance.perform(..))"  />

        <aop:before pointcut="" method="silenceCellIphones"/>

        <aop:before pointcut-ref="performance" method="takeSeats"/>

        <aop:after-returning pointcut-ref="performance" method="applause"/>

        <aop:after-throwing pointcut-ref="performance" method="demandRefund"/>

    </aop:aspect>
</aop:config>
```

现在的切点是一个地方定义的，并且被多个通知元素所引用，<aop:pointcut>元素定义了一个id为performance的切点，同时修改所有的通知元素，用pointcut0ref来引用这个命名切点。

### 4.4.2 声明环绕通知

相比于前置通知和后置通知，环绕通知在这点上有明显的优势。使用环绕通知，我们可以完成前置通知和后置通知所实现的相同功能，而且只需要在一个方法中实现。因为整个通知逻辑都是在一个方法中实现的。
```xml
<aop:config>
    <aop:aspect ref="audience">       <!--引用audience Bean-->
        <aop:pointcut id="performance" expression="execution(* * concert.Performance.perform(..))"  />

        <aop:around pointcut-ref="performance" method="watchPerformance"/>

    </aop:aspect>
</aop:config>
```

像其他通知的XML元素一样，<aop:around>指定了一个切点和一个通知方法的名字。

### 4.4.3 为通知传递参数

```xml

```
区别在于切点表达式中包含了一个参数，这个参数传递到通知方法中。还有区别就是这里使用了and关键字

### 4.4.4 通过切面引入新的功能
借助于AspectJ的@DeclareParents注解为被通知的方法引入新的方法。但是AOP引入并不是Aspectj特有的。使用Spring aop命名空间中的<aop:declare-parents>元素，我们可以实现相同的功能
```xml
<aop:config>
     <aop:aspect ref="audience">       <!--引用audience Bean-->
        <aop:declare-parents types-matching="concert.Performance"
                             implement-interface="concert.Encoreable"
                             default-impl="concert.DefaoultEncoreable"

     </aop:aspect>
 </aop:config>
```

## 4.5 注入AspectJ切面

虽然Spring AOP能够满足许多应用的切面需求，但是与AspectJ相比，Spring AOP是一个功能比较弱的AOP解决方案，ASpect提供了Spring AOP 所不能支持的许多类型的切点。

Spring不能像之前那样使用<bean>声明来创建一个实例----它已经在运行时由AspectJ创建完成了，Spring需要通过工厂方法获取切面的引用。然后像<bean>元素规定的那样在该对象上执行依赖注入

## 4.6 小节(重点中的重点)

AOP是面向对象编程的一个强大补充，通过AspectJ，我们现在可以把之前分散在应用各处的行为放入可重用的模块中。我们显示地声明在何处如何应用该行为。这样有效减少了代码冗余，并让我们的类关注自身的主要功能。

Spring提供了一个AOP框架，让我们把切面插入到方法执行的周围。现在我们已经学会了如何把通知织入前置，后置和环绕方法的调用中，以及为处理异常增加自定义行为。

关于在Spirng应用中如何使用切面 ，我们可以有多种选择。通过使用@AspectJ注解和简化的配置命名空间，在Spring中装配通知和切点变得非常简单

最后，当Spring不能满足需求时，我们必须转向更为强大的AspectJ。对于这些场景，我们了解了如何使用Spring为AspectJ切面注入依赖。

此时此刻，我们已经覆盖了Spring框架的基础知识，了解到如何配置Spring容器以及如何为Spring管理的对象应用切面，这些技术为创建高内聚，低耦合的应用奠定了坚实的基础。


从下一章开始，首先看到的是如何使用Spring构建Web应用。。


期待......






















































































-
