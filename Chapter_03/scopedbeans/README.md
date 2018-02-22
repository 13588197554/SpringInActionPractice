# 3.3 处理自动装配的歧义性

之前，我们已经看到了如何使用自动装配让Spirng完全负责将bean引用注入到构造函数和属性中，自动装配能够提供很大的帮助，因为它会减少装配应用程序组件时所需的显示配置的数量。

不过仅有一个bean匹配所需的结果时，自动装配才是有效的。如果不仅一个bean能够匹配结果的话，这种歧义性会阻碍Spring自动装配属性、构造器或方法参数

为了阐述自动装配的歧义性，假设我们提供@Autowired注解标注了setDessert方法
```java
@Autowired
public void setDessert(Dessert dessert) {
  this.dessert = dessert;
}
```

Dessert是一个接口，并且有三个类实现了这个接口

```java
@Component
public class Cake implements Dessert {...}

@Component
public class Cookies implements Dessert { ...}

@Component
public class IceCream implements Dessert {...}
```

因为这三个实现均使用@Component注解，在组件进行扫描的时候，能够发现他们并将其创建为Spring应用上下文里面的bean，然后，**当Spring试图自动装配的setDessert()中的Dessert参数时，它们并没有唯一、无歧义的可选值。**

Spring此时别无选择，只好宣告失败并抛出异常，更准确的将。Spring会抛出：`NoUniqueBeanDefinitionException`

当Spring发生歧义时，Spring提供了多种可选方案来解决这样的问题。你可以将可选bean的某一个设为首选(primary)的bean，或者使用限定符(qualifier)来帮助Spring将可选的bean的范围缩小到只有一个bean。

### 3.3.1 表示首选的bean

在声明bean的时候，通过将一个可选的bean设置为首选(primary)bean能够避免自动装配时的歧义性。当遇到歧义性的时候，Spring将会使用首选的bean，而不是其它可选的bean。

假设冰激凌就是你最喜欢的甜点，在Spring中，可以通过@Primary来表达最喜欢的方案。`@Primary`能够与`@Componnet`组合用在组件扫描的bean上，也可以与`@Bean`组合用在Java配置的声明中。
```java
@Component
@Primary
public class IceCream implements Dessert {...}
```
或者你通过JavaConfig显示配置地声明IceCream，

```java
@Bean
@Primary
public Dessert IceCream() {
  return new IceCream();
}
```
如果你喜欢使用XML配置bean的话，同样可以实现这样的功能。

```xml
<bean id="ceCream"
    class="com.guo.IceCream"
    primary="true"/>
```

如果你标注了两个或者多个首选bean，那么就无法工作了。

```java
@Component
@Primary
public class Cake implements Dessert { ...}
```

**就解决歧义性问题而言，限定符是一种更为强大的机制**

### 3.2.2 限定自动装配的bean

设置首选bean的局限性在于@Primary无法将可选方案的范围限定到唯一一个无歧义的选项中。它只能表示一个优先的可选方案。

Spring的限定符能够在所有可选的bean上进行缩小范围的操作，最终能够达到只有一个bean满足所规定的限制条件。如果将所有的限定符都用上后依然存在歧义性，那么你可以继续使用更多的限定符来缩小范围。

@Qualifier注解是使用限定符的主要方式。它可以与@Autowired和Inject协同使用，在注入的时候指定想要注入进去的是哪个bean。例如,我们确保要将IceCream注入到setDessert()之中。
```java
@Autowired
@Qualifier("iceCream")
public void setDessert(Dessert dessert) {
  this.dessert = dessert;
}
```

@Qualifier("iceCream")指向的是组件扫描时所创建的bean，并且这个bean是IceCream的实例。更具体一点：@Qualifier("iceCream")所引用的bean要具有String类型的“iceCream”作为限定符。没有没有则和ID一样。

基于默认的bean ID作为限定符是非常简单的，但这有可能会引入一些问题。如果你重构了IceCrean类，将其重名为“Gelato”的话，那此时会发生什么情况？如果是这样的话，bean的默认ID和默认的限定符会变为gelato，这就无法匹配setDessert()方法中的限定符，自动装配会失败。

这里的问题在于setDessert()方法上所指定的限定符与要注入的bean的名称是紧耦合的。对类名称的任意改动都会导致限定符失败。

我们可以为bean设置自己的限定符，而不是依赖于将ID作为限定符。在这里所需要做的就是在bean声明上加@Qualifier注解。

```java
@Component
@Qualifier("cold")
public class IceCream implements Dessert {...}
```
在这种情况下，cold限定符分配了IceCream bean。因为它没耦合类名，因此你可以随意重构IceCream，而不必担心会破坏自动装配。

在注入的地方，只要引用cold限定符就可以了
```java
@Autowired
@Qualifier("cold")
public void setDessert(Dessert dessert) {
  this.dessert = dessert;
}
```
值得一提的是，当通过Java配置显式定义bean的时候i@Qualifier也可以与@Bean注解一起。

```java
@Bean
@Qualifier
public Dessert dessert () {
  return new IceCream();
}
```

当使用自定义的@Qualifier值时，最佳实践是为bean选择特征性或描述性的术语，而不是使用随意的名字。

面向特性的限定符要比基于bean ID的限定符更好一些。但是如果多个bean都具备这个相同的特性的话，这种做法也会出现问题。

这里只有一个小问题：**Java不允许在同一个条目上重复出现相同类型的多个注解。如果你试图这样做的话，编译器将会出错。**

但是我们可以创建 自定义的注解，借助这样的注解来表达bean所希望限定的特性。这里需要做的就是创建一个注解，它本身要使用@Qualifier注解来标注。
```java
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Qualifier
public @interface cold {}
```

当你不想用 @Qualifier注解的时候，可以类似的创建@Sort、@Crispy和@Fruity。通过在定义时添加@Qualifier注解。它就具有了@Qualifier注解的特性。

现在我们重新看一下IceCream，并为其添加@Cold和@Creamy注解

```java
@Component
@Clod
@Creamy
public class IceCream implements　Dessert {...}
```

类似的，Popsicle类可以添加@Cold和@Fruity注解

```java
@Component
@cold
@Fruity
public class Popsicle implements Dessert {...}
```

最终，在注入点，我们使用必要的限定符注解进行任意组合，从而将可选的范围缩小到只有一个bean满足需求。

为了得到IceCream bean 和 setDessert()方法可以这样使用注解：

```java
@Autowired
@Cold
@Creamy
public void setDessert(Dessert dessert) {
  this.dessert = dessert;
}
```

通过声明自定义的限定符注解，我们可以同时使用多个限定符，不会再有Java编译器的限制或错误，与此同时，相对于原始的@Qualifier并借助于String类型来指定限定符，自定义的注解也是类型安全的。

在本节和前节中，我们讨论了几种通过自定义注解扩展Spring的方式，**为了创建自定义的条件化注解，我们建议一个新的注解并在这个注解上添加了Conditional，为了创建自定义的限定符注解，我们创建一个新的注解并在这个注解上添加了@Qualifer。这种技术可以用到很多Spring注解中，从而能够将他们组合在一起形成特定目标的自定义注解。**

## 3.4 bean的作用域

默认情况下，Spring应用上下文中所有的bean都是作为以单例(singleton)的形式创建的。也就是说，不管给定的一个bean被注入到其他bean多次，每次所注入的都是同一个bean。

在大多数情况下，单例bean是很理想的方案，初始化和垃圾回收对象实例所带来的成本只有一些小规模任务。在这些任务中，让对象保持无状态并且在应用中反复使用这些对象可能并不合理。

有时候可能发现，你所使用的类是异变的(mutable)，它们会保持一些状态，因此重复使用时不安全的。在这种情况下将class声明为单例的bean就不是什么好主意了。因为会污染对象，稍后重用的时候会出现意想不到的问题。

Spring定义了多种作用域可以基于这些作用域创建bean，包括：
- 单例(singleton):在整个应用中，只创建bean的一个实例
- 原型(prototype):每次注入或者通过Spring应用上下文获取的时候，都会创建一个新的bean实例
- 会话(Session):在Web应用中，每个会话创建一个bean实例
- 请求(Request):在Web应用中，为每个请求创建一个bean实例。

单例是默认的作用域，但是正如之前所述，对于异变的类型，这并不适合。如果要选择其他作用域，要使用@Scope注解，它可以与@Component或@Bean一起使用。

如果你使用组件扫描来发现bean和生命bean，那么你可以在bean的类上使用@Scope注解，并将其声明为原型bean

```java
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class NotePad {
}
```

在这里，使用`ConfigurableBeanFactory`类的`SCOPE_PROTOTYPE`常量设置了原型作用域。你当然可以使用@Scope("prototype"),但是使用SCOPE_PROTOTYPE常量更加安全并且不易出错。

如果你想在JavaConfig中将NotePad声明为原型bean，那么可以组合使用@Scope和@Bean来指定所需的作用域

```java
@Bean
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public NotePad notepad {
  return new NotePad();
}
```

如果你想使用XMl来配置bean的话，你可以使用bean元素的scope属性来设置作用域
```xml
<bean id="notepad"
  class="com.guo.myapp.NotePad"
  scope="prototype"
```
不管你使用哪种方式来声明作用域，每次注入或从Spirng应用上下文中检索该bean的时候，都会创建新的实例。这样导致的结果就是每次操作都能得到自己的NotePad实例。

### 3.4.1 使用会话和请求作用域

在Web应用中，如果能够实例化在会话和请求范围内共享的bean，那将是非常有价值的事。例如：在典型的电子商务中，可能会有一个bean代表用户的购物车，如果这个购物车是单例的话，那么 将导致所有的用户都会像同一个购物车中添加商品。另一方面，如果购物车是原型作用域，那么在应用中某一个地方往购物车添加商品，在应用的另外一个地方可能就不可用了。因为这里注入的是另外一个原型作用域的购物车。
就购物车bean来说，会话作用域是最为合适的，因为它与给定的用户关联性最大，要指定会话作用域，我们可以使用@Scope注解，它的使用方式和原型作用域是相同的。

```java
@Component
@Scope(value=WebApplicationContext.SCOPE_SESSION,
        proxyMode=ScopedProxyMode.INTERFACES)
public ShoppingCart cart() {...}
```

这里我们将value设置成`WebapplicationConext.SCOPE.SESSION`。这会告诉Spring为Web应用中的每个会话创建一个ShoppingCart。

需要注意跌是，@Scope同时还有另外一个ProxyMode属性，它被设置成了`ScopeProxyMode.INTERFACES`。**这个属性解决了将会话或请求作用域的bean注入到单例bean中所遇到的问题。在描述ProxyMode属性之前，我们先来看下proxyMode所解决问题的场景。

假设我们要将ShoppingCart bean 注入到单例StoreService bean的Setter方法中

```java
@Component
public class StoreService {
@Autowired
public void setShoppingCart(ShoppingCart shoppingCart) {
 this.shoppingCart = shoppingCart;
}
}
```

因为StoreService是一个单例bean，会在Spring应用上下文加载的时候创建，当它创建的时候，Spring会试图将ShoppingCart注入到SetShoppingCart方法中，但是ShoppingCart是会话作用域的，此时并不存在。直到用户进入系统，创建了会话之后，才会出现ShoppingCart实例。

另外系统中将会有多个实例：每个用户一个。**我们并不想让Spirng注入到某个固定的ShoppingCart实例到StoreService中，我们希望的是当StoreService处理购物车的时候，他所用使用的ShoppingCart实例恰好是当前会话所对应的一个。**

Spring并不会将实例的ShoppingCart bean注入到StoreService，Spring会注入一个到ShoppingCart的代理。这个代理会暴露于ShoppingCart相同的方法。所以StoreService就会认为他是一个购物车。

**但是当StoreService调用ShoppingCart的方法方法时，代理会对其进行解析，并将调用委托给会话作用域内真正的ShoppingCart。**

现在我们带着这个 作用域的理解，讨论一下ProxyMode属性，如配置所示，proxyMode属性被设置成了ScopedProxyMode.INTERFACES，这表明这个代理要实现ShoppingCart接口，并将调用委托给实现bean

如果ShoppingCart是接口，而不是类的话，这是可以的，但**如果ShoppingCart是一个具体的类的话，Spring就没有办法创建基于接口的代理了，此时，它必须使用CGLIB来生成基于类的代理。所以，如果bean类型是具体的类的话，我们必须要将ProxyMode属性设置为`ScopedProxyMOde.TARGET_CLASS`.以此来表明要以生成目标类 扩展的方法创建代理。**

尽管，我主要关注量会话作用域，但是请求作用域的bean会面临相同的装配问题，因此，请求作用域的bean应该也以作用域代理的方式进行注入

### 3.4.2 在XML中声明作用域代理

如果你需要使用XML来声明会话或请求作用域的bean，那么就不能使用@Scope注解及其ProxyMode属性了<bean>元素能够设置bean的作用域，但是该怎样设置代理模式呢？

要使用代理模式，我们需要使用Spring aop命名空间的一个新元素：

```xml
<?xml version="1.0" encoding="UTF-8"?>
<bean id="cart"
  class="com.guo.myapp.ShoppingCart"
  scope="session">
  <aop:scoped-proxy/>
</bean>
```

<aop:scoped-proxy>是与@Scope注解的proxyMode属性功能相同的SpringXML配置元素，它会告诉Spring为bean创建一个作用域代理。默认情况下，它会使用CGLIB创建目标的代理。但是我们可以将proxy-target-class的属性设置为false，进而要求它生成基于接口的代理。

```xml
<?xml version="1.0" encoding="UTF-8"?>
<bean id="cart"
  class="com.guo.myapp.ShoppingCart"
  scope="session">
  <aop:scoped-proxy proxy-target-class = "false"/>
</bean>
```

为了使用<aop:scoped-proxy>元素，必须在XML配置中声明Spring的aop命名空间：
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:aop="http://www.springframework.org/schema/aop"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd
        http://www.springframework.org/schema/aop http://www.springframework.org/schema/aop/spring-aop.xsd">
</beans>
```

Spring高级配置的另一个可选方案：Spring表达式语言(Spring Expression Language)

















































































































































-
