## 6.4 使用Thymeleaf

尽管JSP已经存在了很长时间，并且在JavaWeb服务器中无处不在，但是，它却存在一些缺陷。JSP最明显的问题在于它看起来像HTML或XML，但它其实不是。大多数的JSP模板都是采用HTML的形式，但是又参杂了各种JSP标签库的标签，使其变得混乱。这些标签库能够以很便利的方式为JSP带来动态渲染的强大功能，但是它也摧毁了我们想维持一个格式良好的文档的可能性，作为一个极端的样例，如下的JSP标签甚至作为HTML参数的值
```xml
<input type="text" value="<c:out value="${thing.name}"/>"/>
```
标签库和JSP缺乏良好的格式的一个副作用就是它很少能够与其产生的HTML类似。 因为JSP并不是真正的HTML，很多浏览器和编辑器展现的效果都很难在审美上接近模板最终所渲染出来的效果。

同时JSP规范与Servlet规范紧密耦合在一起。这意味着它只能用在基于Servlet的Web应用之中，JSP模板不能呢个作为通用的模板(如格式化Email)，也不能用于非Servlet的Web应用。

最新的挑战者是Thymaleaf，它展现了一些切实的承诺，是一项很令人兴奋的可选方案，Thymeleaf模板是原生的，不依赖于标签库，它能在接受原始HTML的地方进行编辑和渲染。因为它没有和Servlet规范耦合，因此Thymeleaf模板能够进入JSP所无法涉及的领域。让我们看下如何在Spring MVC中使用Thymeleaf。

### 6.4.1 配置Thymeleaf视图解析器

为了要在Sping中使用Thymeleaf，我们需要配置三个启用Thymeleaf与Spirng集成的bean
- ThymeleafViewResolver：将逻辑视图名称解析为Thymeleaf模板视图
- SpringTemplateEngine：处理模板并渲染结果
- TempalateResolver：加载Thymeleaf模板


```java

@Bean
public ViewResolver viewResolver(SpringTemplateEngine templateEngine) {
		ThymeleafViewResolver viewResolver = new ThymeleafViewResolver();
		viewResolver.setTemplateEngine(templateEngine);
		return viewResolver;
}
@Bean
public TemplateEngine templateEngine(TemplateResolver templateResolver) {
		SpringTemplateEngine templateEngine = new SpringTemplateEngine();
		templateEngine.setTemplateResolver(templateResolver);
		return templateEngine;
}

@Bean
public TemplateResolver templateResolver() {                   //模板解析器
		TemplateResolver templateResolver = new ServletContextTemplateResolver();
		templateResolver.setPrefix("/WEB-INF/templates");
		templateResolver.setSuffix(".html");
		templateResolver.setTemplateMode("THML5");
		return templateResolver;
}
```

使用XMl来配置bean

```xml
<bean id="viewResolver" class="org.thymeleaf.spring4.view.ThymeleafViewResolver"
	p:templateEngine-ref="templateEngine" />
<bean id="templateEngine" class="org.thymeleaf.spring4.SpringTemplateEngine"
	P:templateResolver-ref="templateResolver" />
<bean id="templateResolver" class="org.thymeleaf.templateresolver.ServletContextTemplateResolver"
	p:prefix="WEB-INF/templates/"
	p:suffix=".html"
	p:templateMode="HTML5"
```

不管是使用哪种方式，Thymeleaf已经准备好了，它可以将响应中的模板渲染到Spring MVC控制器所处理的请求中。

ThymeleafViewResolver是Spirng MVC中ViewResolver的一个实现类。像其他视图解析器一样，会接受一个逻辑视图名称，并将其解析为视图。不过在该场景下 ，视图会是一个Thymeleaf模板。

需要注意的是：`ThymeleafViewResolver` bean中注入了一个对`SpringTemplateEngine`bean 的引用。`SpringTemplateEngine`会在Spring中启用Thymeleaf引擎，用来解析模板并基于这些模板渲染结果。

`TemplateResolver`会最终定位和查找模板。与之前配置的`InternalResourceVIewResolver`类似，他使用了prefix 和 suffix属性。它的templateMode属性被设置为HTML5，这表明我们预期要解析的模板会渲染成HTML5输出。

### 6.4.2 定义Thymeleaf模板
Thymeleaf在很大程度上就是HTML文件，与JSP不同，他没有什么特殊的标签或标签库。Thymeleaf之所以能够发挥作用，是因为它通过自定义的命名空间，为标准的HTML标签集合添加Thymeleaf属性。

```html
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:th="http://www.thymeleaf.org">            <!--声明Thymeleaf命名空间-->
  <head>
    <title>Spitter</title>
    <link rel="stylesheet"
          type="text/css"
          th:href="@{/resources/style.css}"></link>    <!--到样式表的th:href链接-->
  </head>
  <body>
      <h1>Welcome to Spitter</h1>
      <a th:href="@{/spittles}">Spittles</a> |          <!--到页面的th:herf链接-->
      <a th:href="@{/spitter/register}">Register</a>
  </body>
</html>
```

首页模板相对简单，只是用来th:href属性，特殊之处在于可以包含Thymeleaf表达式，用来动态计算值。在本例中，使用th:href属性的三个地方都是用到了“@{}”表达式，用来计算相对于URL的路径，(在JSP中，会使用JSTL的<c:url>标签或Spring<s:url>标签类似)。

这意味着Thymeleaf模板与JSP不同，**它能按照原始的方式进行编辑甚至渲染，而不必经过任何类型的处理器**,当然我们需要Thymeleaf来处理模板，并渲染得到最终期望的输出。

**借助Thymeleaf实现表单绑定**

表单绑定是Spring MVC的一项重要特性。它能够将表单提交的数据填充到命令对象中，并将其传递给控制器，而在展现表单的时候，表单中也会填充命令对象中的值。

使用Thymeleaf的Spring方言，参考如下的Thymeleaf模板片段

```xml
	<label th:class="${#fields.hasErrors('firstName')}? 'error'">First Name</label>:
		<input type="text" th:field="*{firstName}"
					 th:class="${#fields.hasErrors('firstName')}? 'error'" /><br/>
```

th:class属性会渲染为一个class属性，他的值是根据给给定的表达式计算得到的。在上面的这两个th:class属性中，它会直接检查firstName域有没有校验错误，如果有的话，class属性在渲染时的值为error，如果这个域没有错误的话，将不会渲染class属性。

<input.>标签使用了th:field属性，用来引用后端对象的firstName域，

完整版如下：
```xml
<form method="POST" th:object="${spitter}">
	<div class="errors" th:if="${#fields.hasErrors('*')}">
		<ul>
			<li th:each="err : ${#fields.errors('*')}"
					th:text="${err}">Input is incorrect</li>
		</ul>
	</div>
	<label th:class="${#fields.hasErrors('firstName')}? 'error'">First Name</label>:
		<input type="text" th:field="*{firstName}"
					 th:class="${#fields.hasErrors('firstName')}? 'error'" /><br/>

	<label th:class="${#fields.hasErrors('lastName')}? 'error'">Last Name</label>:
		<input type="text" th:field="*{lastName}"
					 th:class="${#fields.hasErrors('lastName')}? 'error'" /><br/>

	<label th:class="${#fields.hasErrors('email')}? 'error'">Email</label>:
		<input type="text" th:field="*{email}"
					 th:class="${#fields.hasErrors('email')}? 'error'" /><br/>

	<label th:class="${#fields.hasErrors('username')}? 'error'">Username</label>:
		<input type="text" th:field="*{username}"
					 th:class="${#fields.hasErrors('username')}? 'error'" /><br/>

	<label th:class="${#fields.hasErrors('password')}? 'error'">Password</label>:
		<input type="password" th:field="*{password}"
					 th:class="${#fields.hasErrors('password')}? 'error'" /><br/>
	<input type="submit" value="Register" />
</form>
```
需要注意的是我们在表单的顶部也使用了Thymeleaf，它会用来渲染所有的错误。<div>元素使用th:if属性来检查是否有校验错误,如果有的话，会渲染<div>否则的话不会渲染。
```xml
<div class="errors" th:if="${#fields.hasErrors('*')}">
	<ul>
		<li th:each="err : ${#fields.errors('*')}"
				th:text="${err}">Input is incorrect</li>
	</ul>
</div>
```
在<div>中 会使用一个无序的列表来展现每项错误。<li>标签上的th:each属性将会通知Thymeleaf为每项错误都渲染一个<li>,在每次迭代中会将当前错误设置到一个名为err的变量中。

<li>标签还有一个th:text属性，这个命令会通知Thymeleaf计算某一个表达式并将它的值渲染为<li>标签的内容体。实际上的效果就是每项错误对应一个<li>元素，并展现错误文本。

"${}"表达式是变量表达式，**一般来讲，他们是对象图导航语言(Object-Grapg Navigation language OGNL)表达式，但是在使用Spirng的时候，他们是SpEL表达式，在${Spitter}这里 例子中，它会解析为ket为spitter的model属性。**

而对于`*{}`表达式，他们是选择表达式。**变量表达式是基于整个SpEl上下文计算的，而选择表达式是基于某一个选中对象计算的。**在本例的表单中，选中对象就是<form>标签中的th:object属性所设置的对象:模型中的Spitter对象。因此，“*{firsrName}”表达式就会计算为Spitter对象的firstName属性

作者没有细将，我把代码片段贴出来，

```xml
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:th="http://www.thymeleaf.org">
  <head>
    <title>Spitter</title>
    <link rel="stylesheet" type="text/css"
          th:href="@{/resources/style.css}"></link>
  </head>
  <body>
    <div id="header" th:include="page :: header"></div>

    <div id="content">
      <form method="POST" th:object="${spitter}">
      </form>
    </div>
    <div id="footer" th:include="page :: copy"></div>
  </body>
</html>
```

```xml
<html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:th="http://www.thymeleaf.org">

  <body>

    <div th:fragment="header">
      <a th:href="@{/}">
        <img th:src="@{/resources/images/spitter_logo_50.png}" border="0" /></a>
    </div>

    <div>Content goes here</div>

    <div th:fragment="copy">Copyright &copy; Craig Walls</div>
  </body>

</html>
```
**注意标签th:include 、th:fragment 、th:src 、**

## 6.5 小节(我喜欢的)

处理请求只是Spirng MVC功能的一部分。如果控制器所产生的结果想让人看到，那么 他们产生的模型数据就要渲染到视图中，并展现到用户的Web浏览器中。Spring的视图渲染是很灵活的，并提供了多个内置的可选方案，包括传统的 JavaServer Page以及流行的Apache Tiles布局引擎。

在本章节中，我们首先快速了解了一下Spring所提供的视图解析器和视图解析器可选方案。还深入学习了如何在Spring MVC中使用JSP 和ApacheTiles。

还看到了如何使用Thymeleaf作为Spirng MVC应用的视图层，它被视为JSP的替代方案。Thymeleaf是一项很有吸引力的技术，**因为它能创建原始的模板，这些模板是纯HTML，能像静态HTML那样以原始的方式编写和预览，并且能够在运行时渲染动态模型数据。** 除此之外，**Thymeleaf是与Servlet没有耦合关系的，这样它就能够用在JSP所不能使用的领域中。**

Spittr应用的视图定义完成之后，**我们已经具有了一个虽然微小但是可部署且具有一定功能的Sprinig MVC Web应用。还有一些其他特性需要更新进来，如数据持久化和安全性**，我们会在合适的时候关注这些特性。但现在，这个应用变得有模有样了。

在深入学习应用的技术栈之前，在下一章我们将继续讨论Spring MVC，学习这个框架中一些更为有用和高级的功能。

期待。。
