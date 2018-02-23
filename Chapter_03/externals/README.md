## 3.5 运行时注入

当讨论依赖注入的时候，我们通常讨论的是将一个bean引入到另一个bean的属性或构造器参数中。它通常来讲指的是将一个对象与另一个对象关联起来

但bean装配的另一个方面指的是将一个值注入到bean的属性或构造器参数中。

有时候硬编码是可行的，但有时候我们可能会希望避免硬编码。而是让这些值在运行时在确定，为了实现这些功能，Spring提供了运行时求值的方式：
- 属性占位符(Property placeholder)
- Spring表达式语言(SpEL)

这两种技术的用法是类似的，不过他们的目的和行为是有所差别的。

### 3.5.1 注入外部的值

在Spring中，处理外部值的最简单方式就是声明属性源，并通过Spring的Enviroment来检索属性。

一个基本的Spring配置类，他使用外部的属性来装配BlankDisc bean。

```java
@Configuration
@PropertySource("classpath:/com/guo/soundsystem/app.properties")
public class EnvironmentConfig {
    @Autowired
    private Environment env;

    @Bean
    public BlankDisc blankDisc() {
        return new BlankDisc(
                env.getProperty("disc.title"),
                env.getProperty("disc.artist"));
    }
}
```

@PropertySource 引用了类路径中一个名为app.properties的文件

这个属性文件加载到Spring的Environment中，同时blackDisc()方法中，会创建一个新的BlankDisc，它的构造参数是从属性文件中获取的，而这是通过getProperty()实现的。

**深入学习Spirng的Environment**

getProperty() 方法并不是获取属性值的唯一方法，getProperty()方法有四个重载的变种形式
```java

package org.springframework.core.env;

/**
 * Interface for resolving properties against any underlying source.
 *
 * @author Chris Beams
 * @since 3.1
 * @see Environment
 * @see PropertySourcesPropertyResolver
 */
public interface PropertyResolver {

	String getProperty(String key);

	String getProperty(String key, String defaultValue);

	<T> T getProperty(String key, Class<T> targetType);

	<T> T getProperty(String key, Class<T> targetType, T defaultValue);

```
前两种形式的getProperty()方法会返回String类型的值，但是你可以稍微对@Bean方法修改一些，这样在指定属性不存在的时候，会使用一个默认值。

```java
@Bean
public BlankDisc blankDisc() {
    return new BlankDisc(
            env.getProperty("disc.title","guo go go"),
            env.getProperty("disc.artist","UU"));
}
```

剩下的两种getProperty()方法与前面的两种非常类似，但是他们不会将所有的值都视为String类型。假设你要获取的值所代表的连接池中所维持的连接数量，如果我们从属性文件中得到的是一个String类型的值，那么在使用之前还需要将其转化为Interge类型，但是如果使用重载的形式，就能非常便利的解决这个问题。

```java
int connectionCount =
    env.getProperty("db.connection.count",Interge.class,30);
```

Environment还提供了几个与属性相关的方法，如果你在使用getProperty()方法的时候没有默认值，并且这个属性没有定义的话，获取到的值是null，如果你希望这个属性必须定义，那么可以使用getRequiredProperty()，

```java
@Bean
public BlankDisc blankDisc() {
    return new BlankDisc(
            env.getRequiredProperty("disc.title"),
            env.getRequiredProperty("disc.artist"));
}
```
在这里，如果disc.title或者disc属性没有定义的话，将会抛出`lllegalStateException`异常

如果想要检查一个元素是否存在的话，可以调用Envrionment的contaiinsProperty()方法
```java
boolean  titleeExists = env.containsProperty("disc.title");
```

如果想将属性解析为类的话，可以使用getPropertyAsClass()方法
```java
Class<CompactDisc> cdClass = env.getPropertyAsClass("disc.class",CompactDisc.class);
```

除了属性的功能外，Environment还提供 一些方法来检查哪些Profile处于激活状态
```java
public interface Environment extends PropertyResolver {

	String[] getActiveProfiles();                     //返回激活profile名称的数组

	String[] getDefaultProfiles();                    //返回默认profile名称的数组

	boolean acceptsProfiles(String... profiles);      //如果environment支持给定的profile，则返回true
}
```

直接从Environment中检索属性是非常方便的，尤其是在Java配置中装配bean的时候，但是Spring也提供了通过占位符装配属性的方式，这些占位符的值会来源于一个属性源。

Spring一直支持将属性定义到外部的属性配置文件中，并使用占位符值将其插入到Spring bean中，
```xml
<bean class="com.guo.soundsystem.BlankDisc"
      c:_0 = "${disc.title}"
      c:_1 = "${disc.artist}"/>
```

按照这种方式，XML配置没有使用任何硬编码的值，它的值是从配置文件以外的一个源中解析得到的。

如果我们依赖于组件扫描和自动装配来创建和初始化应用组件的话，那么就没有占位符的配置文件了，在这种情况下，我们可以使用@Value注解，它的使用方式与@Autowired注解非常类似。

在BlankDisc类中，构造器可以改成如下显示：
```java
public BlankDisc(
  @Value("${disc.title}")String title,
  @Value("${disc.artist}") String artist) {
    this.title = title;
    this.artist = artist;
}
```
为了使用占位符，我们必须要配置一个PropertyPlaceholderConfigurer bean， 从Spring3.1开始，推荐使用propertySourcesPlaceholderConfigurer，因为它能够基于Spirnig Environment 及其属性源来加载占位符。

```java
@Bean
public static propertyplaceholderConfigurer placeholderConfigurer() {
  return new propertyplaceholderConfigurer();
}
```

如果你想使用XML配置的话，Spring Context命名空间中的<context:propertyplaceholder>元素会为你生成
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:c="http://www.springframework.org/schema/c"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
		http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context-4.0.xsd">

    <context:property-placeholder
            location="com/soundsystem/app.properties" />
</beans>
```

**解析外部属性能够将值的处理推迟到运行时，但是它的关注点在于根据名称解析来自于Spring Environment和属性源的属性。而Spring表达式语言提供了一种更为通用的方式在运行时计算所要注入的值。**

### 3.5.2 使用Spring表达式语言进行装配

Spring 3引入了Spring表达式语言(SpringExpression Langguage SpEL),它能够以一种强大和简洁的方式将值装配到bean的属性和构造器参数中，在这个过程中所使用的表达式会在运行时计算到值。。

SpEL拥有的特性：
- 使用bean的ID来引用bean
- 调用方法和访问对象的属性
- 对值进行算术、关系、逻辑运算
- 正则表达式匹配
- 集合操作

SpEL表达式要放要`#{...}`之中这与属性占位符有些类似，属性占位符需要放到${...}之中

```xml
#{T(System).currentTimeMillis()}    //计算表达式的那一刻当前时间的毫秒值。

#{systemProperties['disc.title']}   //引用其他bean和其他bean的属性

#{3.141519}                         //表示浮点值

#{artistSelector.selectArtist()}    //除了引用bean，还可以调用方法

如果要在SpEL中访问类的作用域的方法和常量，需要依赖T()这个关键运算符。

```

在动态注入值到Spring bean时，SpEL是一种很遍历和强大的方式。

## 3.6 小节

1、学习了Spring profile，解决了Spring bean 要跨各种部署环境的通用问题。Profile bean 是在运行时条件化创建bean的一种方式，但在Spring4中提供了@Conditional注解和SpringCondition接口的实现。

2、解决两种自动装配歧义的方法，首选bean以及限定符。

3、Spring嫩那个狗让bean以单例，原型、会话、请求作用域的方式来创建。

4、简单的学习了SpEl，它能够在运行时计算要注入的bean属性的值。

依赖注入能够将组件以及协作的其他组件解耦，AOP有利于将应用组件与跨多个组件的任务进行解耦。











































-
