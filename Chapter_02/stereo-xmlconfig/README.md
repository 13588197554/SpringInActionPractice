![](https://i.imgur.com/OEBPcTK.jpg)

**不得不佩服老外**

## 2.4 通过XML装配bean
在Spring刚刚出现的时候，XMl是描述配置的主要方式。在Spring的名义下，我们创建了无数行XML代码。在一定程度上，Spring成为了XMl配置的同义词。现在需要明确的是XML不再是配置Spring的唯一可选方案。Spring现在有了强大的自动配置和基于Java的配置，Xml不应该在是你的第一选择了。

**本节的内容只是用来帮助你维护已有的XML配置，在完成新的Spring工作时，希望你会使用自动化配置和Java配置**

### 2.4.1 创建XML配置规范

在使用XMl为Spring装配bean之前你需要创建一个新的配置规范。在使用JavaConfig的时候，你需要创建一个带有@Configuration注解的类，而在XML配置中，需要创建一个XMl文件，并且要以<beans>元素为根
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:c="http://www.springframework.org/schema/c"
       xmlns:p="http://www.springframework.org/schema/p"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
		http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd">

    <!-- configuration details go here -->
</beans>
```
已经有一个合法的Spring XMl配置文件。不过它是一个没有任何用处的配置，因为它没有申明任何bean。为了给予它生命，重新创建一下CD样例，只不过这次选择XML配置，而不是使用JavaConfig和自动化装配。

### 2.4.2 声明一个简单的bean
要在基于XML的Spring中声明一个bean，需要使用Spring-beans模式中的另一个元素 <bean> 类似于JavaConfig中的@Bean注解。
```xml
<bean class="com.guo.soundsystem.SgtPeppers"/>
```

这里声明了一个简单的bean，创建这个bean的类通过class属性来指定的并且要使用全限定类名。
因为没有明确给Id，所以这个  bean江湖根据全限定名来进行命名。

尽管自动化的bean命名方式非常方便，但如果你稍后需要引用它的话，那么自动产生的名字就没有多大用处了。因此，通常来讲更好的办法就是借助于id属性。为每个bean设置一个你自己选择的名字
```xml
<bean id="compactDisc" class="com.guo.soundsystem.SgtPeppers"/>
```

第一件需要注意的事情就是你不在需要直接负责创建SgtPeppers的实例，在基于JavaConfig的配置中，我们需要这样做。当Spring发现这个<bean>元素时，它会调用SgtPeppers的默认构造器来创建bean。在Xml配置中，bean的创建显得更加被动，不过，它它没有javaConfig那样强大，在JavaConfig中，你可以通过任何可以想象到的方法来创建bean实例。

另一个需要注意的是，在这个简单的<bean>声明中，我们将bean的类型以字符串的形式设置在了 class属性中。谁能确保设置给Class属性的值是真正的类呢？Spring的XMl配置并不能从编译器的类型检查张宏收益，即便它所引用的是实际的类型，如果你重命名了会发生什么呢？

以上介绍的只是JavaConfig要优于XML配置的部分原因。在你的应用选择配置风格时，要记住XMl配置的这些缺点。

### 2.4.3 借助于构造器注入 初始化 bean
在Spring XMl配置中，只有一种声明bean的方式，使用<bean>元素并制定calss属性，Sprng会从这里获取必要的信息来创建bean。

在XMl中声明DI时，会有多种可选的配置风格和方案。具体到构造器注入，有两种基本的配置方案可供选择
- <constructor-arg>元素
- 使用Spring3.0所引入的c-命名空间

两者的区别在很大程度就是是否冗余长烦琐。<constructor-arg>元素比使用c-命名空间会更加冗长。从而导致XMl更加难以读懂。另外有些事情<constructor-arg>可以做到，但是使用c-命名空间却无法实现。

**构造器注入**

```xml
<bean id="cdPlayer" class="com.guo.soundsystem.CDPlayer">
    <constructor-arg ref="compactDisc"/>
</bean>
```

**当Spring遇到这个bean元素时，它会创建一个CDPlayer实例 。<constructor-arg>元素会告知Spring要将一个ID为compactDisc的bean引用传递到CDPlayer的构造器中**

作为替代方案，你也可以使用Spring的c-命名空间。c-命名空间实在Spring3.0中引入的，它是在XMl中更为简单的描述构造器参数的方式，要使用他的话，必须在XMl的顶部声明其模式 ，
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:c="http://www.springframework.org/schema/c"
       xmlns:p="http://www.springframework.org/schema/p"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
		http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd">
</beans>
```
在c-命名空间和模式声明之后，就可以使用它来声明构造器参数了
```xml
<bean id="compactDisc" class="com.guo.soundsystem.cdPlayer"
  c:cd-ref="compactDisc" />
```

**将字面量注入到构造器中**
迄今为止，我们所作的DI通常指的都是类型的装配---也就是**将对象的引用装配到依赖于他的其他对象之中。**
而有些时候我们需要做的只是将一个字面量值来配置对象，为了阐述这一点，假设你要创建CompactDisc的一个新实现 。

```java
/**
 * Created by guo on 21/2/2018.
 */
public class BlankDisc implements CompactDisc {
    private String title;
    private String artist;

    public BlankDisc(String title, String artist) {
        this.title = title;
        this.artist = artist;
    }

    @Override
    public void play() {
        System.out.println("Playing " + title + " by " + artist);
    }
}
```

在SgtPeppers中，唱片名称和艺术家的名字都是硬编码的。但是这个CompactDisc实现与之不同，它更加灵活。像现实中的空磁盘一样，它可以设置成你想要的艺术家和唱片名。

```xml
<bean id="compactDisc1" class="com.guo.soundsystem.BlankDisc">
    <constructor-arg value="Sgt. Peppers Lonely Hearts Club band"/>
    <constructor-arg value="The Beatles"/>
</bean>
```
使用value属性，通过该属性表明给定的值要以字面量的形式注入到构造器中。

**集合装配**

如果使用CompactDisc为真正的CD建模，那么它也应该有磁道列表的概念。
```xml
<bean id="compactDisc1" class="com.guo.soundsystem.BlankDisc">
    <constructor-arg value="Sgt. Peppers Lonely Hearts Club band"/>
    <constructor-arg value="The Beatles"/>
    <constructor-arg>
      <list>
        <value>11</value>
        <value>22</value>
        <value>33</value>
        <value>44</value>
      </list>
    </constructor-arg>
</bean>
```
其中，<list>元素是<consturctor-arg>的子元素。这表明一个包含值的列表将会传递给构造器中，其中<value>元素用来指定列表中的每一个元素。

**与之类似的，我们也可以使用<ref>元素替代<value>,实现bean引用列表的装配**

<set>和<list>元素区别不大，其中重要的不同在于当Spring创建要装配集合的时候，所创建的是java.util.Set还是java.util.List。如果是Set的话，所有重复元素会被忽略掉，存放顺序也不会得到保证。不过无论在哪中情况下，<set>和<List>都可以用来装配List、Set甚至数组。

**在装配集合方面，<consturctor-arg>比c-命名空间的属性更有优势。使用c-命名空间的属性却无法实现装配集合的功能**

与其不厌其烦的花时间讲述如何使用XML进行 构造器注入，还不如看一下如何使用XML来装配属性

### 2.4.4 设置属性

到目前为止，CDPlayer和BlanckDisc类完全是通过构造器注入的，没有使用属性的Setter方法，接下来，我们就看看如何使用Sprng XML配置实现属性注入，

**该选择构造器注入还是熟悉注入呢？作为一个通用的规则，我倾向于对强依赖使用构造器注入，而对可选性的依赖使用属性注入。

对于CDPlayer来讲，它对CompactDisc是强依赖还是可选依赖可能会有些争议。

Spring在创建bean的时候不会有任何问题，但是CDPlayTest会因为出现NullPointException而导致测试失败，因为我们并没有出入CDPlayer的compactDisc属性。不过按照下面的方式修改XML，就能解决该问题
```xml
<bean id="cdPlayer" class="com.guo.soundsystem.CDPlayer">
  <propert name="compactDisc" ref="compactDisc"/>
</bean>
```
<propert>元素为属性的Setter方法所提供的功能与<consturctor-arg>元素为构造器所提供的功能是一样的。在本例中，它它引用了ID为compactDisc的bean，(通过ref属性)，并将其注入到compactDisc属性中(通过setCompactDisc()方法)。

Spring为<consturctor-arg>元素通过了c-命名空间作为替代方案，与之类似的，Spring提供了更加简洁的p-命名空间，作为<propert>元素的替代方案。为了启用p-，必须在XML文件织哦你与其他命名空间一起对其进行声明。
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:c="http://www.springframework.org/schema/c"
       xmlns:p="http://www.springframework.org/schema/p"
       xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd
		http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd">
</beans>
```

我们可以使用p-命名空间，按照以下的方式装配compactDisc属性
```xml
<bean id="cdPlayer" class="com.guo.soundsystem.CDPlayer"
  p:compactDisc-ref="compactDisc"/>
```
首先属性的名字使用了"p："前缀，表明我们设置的是一个属性。接下来就要注入属性名。

**将字面量注入到属性中**

```xml
<bean id="compactDisc1" class="com.guo.soundsystem.BlankDisc">
    <propert value="Sgt. Peppers Lonely Hearts Club band"/>
    <propert value="The Beatles"/>
    <propert>
      <list>
        <value>11</value>
        <value>22</value>
        <value>33</value>
        <value>44</value>
      </list>
    </propert>
</bean>
```

需要注意的是**我们不能使用p-命名空间来装配集合，没有遍历的方式使用p-命名空间

util-命名空间所提供的功能之一就是<util:list>元素它会创建一个列表的bean，借助<util:list>，我们可以将磁道列表转移到BlackDisc bean之外。并将其声明到单独的bean之中。
```xml
<util:list id="trackList">
  <value>Sgt. Pepper's Lonely Hearts Club Band</value>
  <value>With a Little Help from My Friends</value>
  <value>Lucy in the Sky with Diamonds</value>
  <value>Getting Better</value>
  <value>Fixing a Hole</value>
  <value>She's Leaving Home</value>
  <value>Being for the Benefit of Mr. Kite!</value>
  <value>Within You Without You</value>
  <value>When I'm Sixty-Four</value>
  <value>Lovely Rita</value>
  <value>Good Morning Good Morning</value>
  <value>Sgt. Pepper's Lonely Hearts Club Band (Reprise)</value>
  <value>A Day in the Life</value>
</util:list>
```

在需要的时候，你可能会用到util-命名空间中的部分成员。

Spring util-命名空间中的元素

- <util:constant> :引用某个类型的public static 域，并将其暴露为bean
- util:list:创建一个java.util.List类型的bean，其中包含值和引用
- util:map :创建一个java.util.Map类型的bean，其中包含值或引用。
- util:properties :创建一个java.util.properteis类型的bean。
- util：set： 创建一个java.util.Set类型的bean，其中包含值或引用。

## 2.5导入和混合配置

待续































































-
