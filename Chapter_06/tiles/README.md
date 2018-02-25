## 6.3 使用Apache Tiles 视图定义布局。

假设我们想为应用中的所有页面定义一个通用的头部和底部，最原始的方式就是查找每个JSP模板，并为其添加头部和底部的HTML。但是这种方法的扩展性不好，也难以维护。为每个页面添加这些元素会有一些初始成本，而后续的每次变更更会消耗类似的成本。

更好的方式是使用局部引擎，如Apache Tiles，定义适用于所有页面的通用页面布局。Spring MVC以视图解析器的形式为Apache Tiles 提供了支持，这个视图解析器能够将逻辑视图名解析为Tile定义。

### 6.3.1 配置Tiles视图解析器

需要配置一个TilesConfigurer bean，它会负责定位和加载Tile定义并协调生成的Tiles。除此之外还需要TilesViewResolver bean 将逻辑视图名解析为Tile定义。

包名不同

首先配置TilesConfigurer来解析Tile定义

```java
//Tile
 @Bean
 public TilesConfigurer tilesConfigurer() {
     TilesConfigurer tiles = new TilesConfigurer();
     tiles.setDefinitions(new String[] {
             "WEB-INF/layout/tiles.xml",
             "/WEB-INF/view/**.tiles.xml"});       //指定Tile定义的位置
     tiles.setCheckRefresh(true);                 //启用刷新功能
     return tiles;
 }

 @Bean
 public ViewResolver viewResolver() {
     return new TilesViewResolver();
 }
```

本例中，使用了Ant风格的通配符，（**）,所以TilesConfigurer会遍历“WEB-INF/”的所有子目录在查找Tile定义。

如果你更喜欢XMl配置的话，那么可以按照如下的形式配置`TilesConfigurer`和`TilesViewResolver`:
```xml
<bea id="tilesConfigurer"
      class="org.springframework.web.servlet.view.tiles3.TilesConfigurer">
    <property name="difinitions">
      <list>
        <value>WEB-INF/layout/tiles.xml</value>
        <value>/WEB-INF/view/**.tiles.xml</value>
    </property>
</bean>
<bean id="ViewResolver"
    class="org.springframework.web.servlet.view.tiles3.TilesViewResolver" />
```

`TilesConfigurer`会加载Tiles定义并与`Apache Tiles`协作，而`TilesViewResolver`会将逻辑视图名解析为引用Tiles定义的视图，它是通过查找与逻辑视图名称想匹配的Tiles定义实现该功能的。需要定义几个Tile定义以了解它是如何运转的

** 定义Tiles **

Apache Tiles 提供了一个文档类型定义(docment type definition,DTD) 用来在XML文件中指定Tile的定义。每个定义中需要包含一个<definition>元素，这个元素会有一个或多个<put-attribute>元素。

```xml
<?xml version="1.0" encoding="ISO-8859-1" ?>
<!DOCTYPE tiles-definitions PUBLIC
       "-//Apache Software Foundation//DTD Tiles Configuration 3.0//EN"
       "http://tiles.apache.org/dtds/tiles-config_3_0.dtd">
<tiles-definitions>

  <definition name="base" template="/WEB-INF/layout/page.jsp">           <!--定义base Tile-->
    <put-attribute name="header" value="/WEB-INF/layout/header.jsp" />
    <put-attribute name="footer" value="/WEB-INF/layout/footer.jsp" />     <!--设置属性-->
  </definition>

  <definition name="home" extends="base">                                   <!--扩展base Tile-->
    <put-attribute name="body" value="/WEB-INF/views/home.jsp" />
  </definition>

  <definition name="registerForm" extends="base">
    <put-attribute name="body" value="/WEB-INF/views/registerForm.jsp" />
  </definition>

  <definition name="profile" extends="base">
    <put-attribute name="body" value="/WEB-INF/views/profile.jsp" />
  </definition>

  <definition name="spittles" extends="base">
    <put-attribute name="body" value="/WEB-INF/views/spittles.jsp" />
  </definition>

  <definition name="spittle" extends="base">
    <put-attribute name="body" value="/WEB-INF/views/spittle.jsp" />
  </definition>

</tiles-definitions>
```
每个<difinition>元素定义了一个Tile，它最终引用的是一个JSP模板。对于base Tiles来讲，它引用的是一个头部JSP模板和一个底部JSP模板

baseTile所引用的page.jsp模板如下所示：
```xml
<%@ taglib uri="http://www.springframework.org/tags" prefix="s" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="t" %>
<%@ page session="false" %>
<html>
  <head>
    <title>Spittr</title>
    <link rel="stylesheet"
          type="text/css"
          href="<s:url value="/resources/style.css" />" >
  </head>
  <body>
    <div id="header">
      <t:insertAttribute name="header" />             <%--插入头部--%>
    </div>
    <div id="content">
      <t:insertAttribute name="body" />               <%--插入主题内容--%>
    </div>
    <div id="footer">
      <t:insertAttribute name="footer" />              <%--插入底部--%>
    </div>
  </body>
</html>
```

在以上代码中，重点关注的是事情就是：如何使用Tile标签库中的<t:insertAttribute>JSP标签来插入其他的模板。

属性引用的每个模板是很简单的。

```xml
<%@ taglib uri="http://www.springframework.org/tags" prefix="s" %>
<a href="<s:url value="/" />"><img
    src="<s:url value="/resources" />/images/spitter_logo_50.png"
    border="0"/></a>
```

为了完整的了解home Tile 展现如下home.jsp
```xml
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page session="false" %>
<h1>Welcome to Spitter</h1>

<a href="<c:url value="/spittles" />">Spittles</a> |
<a href="<c:url value="/spitter/register" />">Register</a>
```

这里的关键点在于通用的元素放到了page.jsp、header.jsp以及footer.jsp中，其他的Tile模板中不再包含这部分内容。这使用它们能够跨页面重用，这些元素的维护也得以简化。

![](https://i.imgur.com/0KCTGa0.jpg)

在Java Web应用领域中，JSP长期以来都是占据主导地位的方案，但是这个领域有了新的竞争者，也就是Thymeleaf。
