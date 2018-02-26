# Spring MVC 高级的技术

本章内容：
- Spring MVC配置的替代方案
- 处理文件上传
- 在控制器中处理异常
- 使用flash属性

稍等还没结束

在很多方面，Spirng MVC(整个Spirng也是如此)，也有还没结束这样的感觉。

在第五章，我们学习了Sprng MVC的基础知识，以及如何编写控制器来处理各种请求，基于这些知识。我们在第六章学习了如何创建JSP和Thymeleaf视图，这些视图会将模型数据展示给用户。你可能认为我们已经掌握了Spring MVC的全部知识，但是，稍等！还没结束。

在本章中，我们将会看到如何编写控制器处理文件上传，如何处理控制器所抛出的异常，以及如何在模型中传递数据，使其能够在重定向(redirect)之后依然存活。


但，首先我要兑现一个承诺。在第5章中，我们快速展现了如何通过`AbstractAnnotationConfigDispatcherServletInitializer`搭建Spring MVC，当时，我们承诺会为读者展现其他的配置方案。所以，在介绍文件上传和异常处理之前，我们花时间探讨一下如何使用其他方式来搭建`DispatcherServlet`和`ContextLoaderListener`

## 7.1 Spring MVC 配置的替代方案

尽管对很多Spring应用来说，这是一种安全的假设，但是并不一定能满足我们的要求。除了`DispatcherServlet`以外，我们还可能需要额外的`DispatcherServlet`和`Filter`，我们可能还需要对`DispatcherServlet`本身做一些额外的配置：或者，如果我们需要将应用部署到Servlet3.0之前的容器中，那么还需要将`DispatcherServlet`配置到传统的web.xml中。

### 7.1.1 自定义DispatcherServlet配置
```java
public class SpitterWebInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

  @Override
  protected Class<?>[] getRootConfigClasses() {
    return new Class<?>[] { RootConfig.class };
  }

  @Override
  protected Class<?>[] getServletConfigClasses() {
    return new Class<?>[] { WebConfig.class };
  }

  @Override
  protected String[] getServletMappings() {
    return new String[] { "/" };
  }

}
```

`AbstractAnnotationConfigDispatcherServletInitializer`所完成的事其实比看上去要多，在`SpitterWebInitializer`中我们所编写的三个方法仅仅是必须要重载的三个抽象方法，但实际上还有更多的方法可以进行重载，从而实现额外的配置。

此类的方法之一就是`customizeRegistration()`.在`AbstractAnnotationConfigDispatcherServletInitializer`将`DispatcherServlet`注册到Servlet容器中就会调用`customizeRegistration()`,并将Servlet注册后得到的`Registration.Dynamic`传递进来，通过重载`customizeRegistration()`方法，我们就可以对`DispatcherServlet`进行额外的配置。

在本章稍后，我们将会看到如何在Spirng MVC中处理multiparty请求和文件上传。如果计划使用Servlet3.0对multiparty配置的支持，那么我们需要使用`DispatcherServlet`的registration来启用multilpart请求。我们可以重载`customizeRegistration()`方法来设置MultipartConfigElement，
```java
@Override
protected void customizeRegistration(Dynamic registration) {
    registration.setMultipartConfig(
            new MultipartConfigElement("C:\\Temp"));   //设置上传文件目录
}
```

借助`customizeRegistration()`方法中的`ServletRegistration.Dynamic`我们能够完成更多的任务，
- 包括通过调用`setLoadOnstartup()`设置load-on-startup 优先级，
- 通过`setInitParameter()`设置初始化参数，
- 通过调用`setMultipartConfig()`配置Servlet3.0对multipart的支持，


### 7.1.2  添加其他的Servlet和Filter

按照`AbstractAnnotationConfigDispatcherServletInitializer`的定义，它会创建`DispatcherServlet`和`ContextLoaderListener`.但是如果你想要注册其他的Servlet、Filter、Listener的话，那该怎么办？

基于Java的初始化器(initializer)的一个好处在于我们可以定义任意数量的初始化类。如果我们想要往Web容器中注册其他组件的话，只需要创建一个 新的初始化类就可以了，最简单的方式就是实现Spring的`WebApplicationInitializer`并注册一个Servlet。

```java
public class MyServletInitializer implements WebApplicationInitializer {
    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
      Dynamic  myServlet = servletContext.addServlet("myServlet",myServlet.class);
      myServlet.addMapping("/custom/**");
    }
}
```

以上程序相当基础的Servlet注册初始化器类，它注册了一个Servlet并将其映射到了一个路径上，我们也可以通过这种方式来手动注册`DispatcherServlet`.(但是没必要，因为`AbstractAnnotationConfigDispatcherServletInitializer`没用太多代码就将这项任务完成得很漂亮)

类似的，我们还可以创建新的`WebApplicationInitializer`来实现注册Listener和 Filter，

```java
@Override
public void onStartup(ServletContext servletContext) throws ServletException {
    javax.servlet.FilterRegistration.Dynamic filter = servletContext.addFilter("myFilter",myFilter.class);
    filter.addMappingForUrlPatterns(null,false,"/custom/*");
}
```


如果你将应用部署到Servlet3.0的容器中，那么`WebApplicationInitializer`提供了一种通用的方法，实现在Java中注册Servlet和Filter、Listener，如果你只是注册Filter，并且该Filter只会映射到`DispatcherServlet`上的话，那么`AbstractAnnotationConfigDispatcherServletInitializer`还有一种快捷的方式。

为了注册Filter并将其映射到`DispatcherServlet`,所需要做的仅仅是重载`AbstractAnnotationConfigDispatcherServletInitializer`的getServletFilter()方法。

```java
@Override
protected Filter[] getServletFilters() {
    return new Filter[] {new Myfilter()};
}
```

这个方法返回一个javax.servlet.filter数组。在这里没有必要声明它的映射路径，getServletFilter()方法返回所有Filter都会被映射到`DispatcherServlet`上。

如果要将应用部署到Servlet3.0上，那么Spring容器提供了多种注册方式，而不必创建web.xml文件，但是，如果你不想采取上述方案的话，也是可以的，假设你将应用部署到不支持Servlet3.0的容器中(或者你只希望使用web.xml),那么我们完全可以按照传统的方式，通过web.xml配置Spirng MVC，

### 7.1.3 在web.xml中声明DispatcherServlet

在典型的Spirng MVC应用中，我们会需要`DispatcherServlet`和`ContextLoaderListener`.`AbstractAnnotationConfigDispatcherServletInitializer`会自动注册它们，但如果需要在web.xml中注册的话，那就需要我们自己动手来完成了。

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xmlns="http://java.sun.com/xml/ns/javaee" xmlns:web="http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd"
	xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd"
	id="taotao" version="2.5">
	<display-name>appServlet</display-name>
	<welcome-file-list>
		<welcome-file>index.html</welcome-file>
	</welcome-file-list>
	<!-- 加载spring容器 -->
	<context-param>
		<param-name>contextConfigLocation</param-name>
		<param-value>classpath:spring/applicationContext-*.xml</param-value>
	</context-param>
	<listener>
		<listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
	</listener>
	<!-- 解决post乱码 -->
	<filter>
		<filter-name>CharacterEncodingFilter</filter-name>
		<filter-class>org.springframework.web.filter.CharacterEncodingFilter</filter-class>
		<init-param>
			<param-name>encoding</param-name>
			<param-value>utf-8</param-value>
		</init-param>
	</filter>
	<filter-mapping>
		<filter-name>CharacterEncodingFilter</filter-name>
		<url-pattern>/*</url-pattern>
	</filter-mapping>
	<!-- springmvc的前端控制器 -->
	<servlet>
		<servlet-name>appServlet</servlet-name>
		<servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
    <!-- contextConfigLocation不是必须的， 如果不配置contextConfigLocation， springmvc的配置文件默认在：WEB-INF/servlet的name+"-servlet.xml" -->
		<init-param>
			<param-name>contextConfigLocation</param-name>
			<param-value>classpath:spring/springmvc.xml</param-value>
		</init-param>
		<load-on-startup>1</load-on-startup>
	</servlet>
	<servlet-mapping>
		<servlet-name>appServlet</servlet-name>
		<url-pattern>/</url-pattern>
	</servlet-mapping>
</web-app>
```

`ContextLoaderListener`和`DispatcherServlet`各自都会加载一个Spirng应用上下文。上下文`ContextLoaderLocation`指定了一个XMl文件的地址，这个文件定义了根据应用上下文，它会被`ContextLoaderListener`加载。根上下文会从"/WEB-INF/spring/applicationContext-*.xml"中**加载bean的定义**

`DispatcherServlet`会根据Servlet的名字找到一个文件，并基于该文件加载应用上下文。

如果你希望指定`DispatcherServlet`配置文件的话，那么可以在Servlet指定一个`ContextLoaderLocation`初始化参数。

```xml
<!-- springmvc的前端控制器 -->
<servlet>
  <servlet-name>appServlet</servlet-name>
  <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
  <!-- contextConfigLocation不是必须的， 如果不配置contextConfigLocation， springmvc的配置文件默认在：WEB-INF/servlet的name+"-servlet.xml" -->
  <init-param>
    <param-name>contextConfigLocation</param-name>
    <param-value>classpath:spring/springmvc.xml</param-value>
  </init-param>
  <load-on-startup>1</load-on-startup>
</servlet>
<servlet-mapping>
  <servlet-name>appServlet</servlet-name>
  <url-pattern>/</url-pattern>
</servlet-mapping>
```

现在我们已经看到了如何以多种不同的方式来搭建Spring MVC，那么接下来我们看一下如何使用Spring MVC来处理文件上传。

## 7.2 处理multipart形式的数据

在Web应用中，允许用户上传内容是很常见的需求，在Facebook和Flickr这样的网站中，允许用户会上传图片和视频，并与家人朋友分享。还有一些服务器中允许用户上传照片，然后按照传统的方式将其打印在纸上，或者咖啡杯上。

Spittr应用有两个地方需要文件上传。当新用户注册的时候，我们希望能够上传一张照片，从而与他的个人信息相关联。当用户提交新的Spittle时，除了文本信息外，他们可能还会上传一张照片。

一般表单提交所形成的请求结果是非常简单的，就是以"&"符号分割的多个name-value对，尽管这种方式简单，并且对于典型的基于文本的表单提交也足够满足需求，但是对于二进制数据，就显得力不从心了。与之不同的是multipart格式的数据会将一个表单拆分为多个部分(part),每个部分对应一个输入域。在一般的表单输入域中，它所对应的部分中会放置文本数据，但是如果是上传文件的话，它所对应的部分可以是二进制。

Content-Type 他表它的类型。尽管multipart请求看起来很复杂，但是在SpringMVC中处理它却很容易，在编写控制器方法处理文件上传之前，我们必须配置一个multipart解析器，通过它来告诉`DispatcherServlet`该如何读取multipart请求。



### 7.2.1 配置multipart解析器

`DispatcherServlet`并没有实现爱你任何解析multipart请求数据的功能。它将该任务委托给了Spring中的MultipartResolver策略接口的实现，通过这个实现类来解析multipart请求中的内容。从Spirng 3.1开始，Spirng内置了两个MultipartResolver的实现供我们选择。

- CommonsMultipartResolver：使用Jakarta Commons FileUpload解析multipart请求。
- StandardServletMultipartResolver：依赖于Servlet3.0对multipart请求的支持。

一般来讲`StandardServletMultipartResolver`可能会是优选方案，他使用Servlet所提供的功能支持。并不需要依赖任何其他的项目。如果，我们需要将项目部署到Sevrvlet3.0之前的容器中，或者还没有使用Spring 3.1 或者更高的版本，那么可能就需要 `CommonsMultipartResolver`了。

使用Servlet3.0解析multipart

兼容Servlet3.0的`StandardServletMultipartResolver`没有构造器参数，也没有要设置的参数，这样，在Spring应用上下文中，将其声明为bean就会非常简单。
```java
@Bean
public MultipartResolver multipartResolver() throws IOException {
    return new StandardServletMultipartResolver();
}
```

如果我们采用Servlet初始化类的方式来配置`DispatcherServlet`的话，这个初始化类应该已经实现了`WebApplicationInitializer`,那么我们可以在`ServletRegistration`上调用`setMultipartConfig()`方法，传入一个`MultipartConfigElement`实例，具体的配置如下：

```java
@Override
protected void customizeRegistration(Dynamic registration) {
    registration.setMultipartConfig(
            new MultipartConfigElement("C:\\Temp"));
}
}
```
通过重载`customizeRegistration()`方法(它会得到一个Dynamic作为参数)类配置multipart的具体细节。

到目前为止，我们所使用的是只有一个参数的MultipartConfigElemenet构造器，这个参数指定的是文件系统中一个绝对目录，上传文件将会临时写入该目录，但是，我们还可以通过其他的构造器来限制上传文件的大小，除了临时路径的位置，其他的构造器可以接受的参数如下：

- 上传文件的最大容量(以字节为单位)。默认是没有限制的
- 整个multipart请求的容量。不会关心有多少个part以及每个part的大小。默认是没有限制的。
- 在上传的过程中，如果文件大小达到了一个指定最大容量，将会写入到临时文件路径中，默认值为0，也就是所有上传的文件都会写入磁盘上。

例如，假设我们想要限制文件的大小不超过2MB，整个请求不超过4MB，而且所有的文件都要写入磁盘，
```java
@Override
protected void customizeRegistration(Dynamic registration) {
    registration.setMultipartConfig(
            new MultipartConfigElement("C:\\Temp\\uploads",2097152, 4194304, 0));
}
```

如果使用更为传统的web.xml来配置MultipartConfigResolver的话，那么可以使用<servlet>中的<multipart-config>元素
```xml
<servlet>
  <servlet-name>appServlet</servlet-name>
  <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
  <load-on-startup>1<load-on-startup>
  <multipart-config>
    <location>C:\\Temp\\uploads<location>
    <max-file-size>2097152</max-file-size>
    <max-request-size>4194304 </max-request-size>
  </multipart-config>
</servlet>
```

**配置Jakarta Commons FileUpload Multipart解析器**

Spring内置了CommonsMultipartResolver，可以作为`StandardServletMultipartResolver`的替代方案。

### 7.2.2 处理multipart请求。

已经配置好multipart请求的处理器，那么接下来我们就编写控制器方法来接收上传的文件。要实现这一点，最常见的方法就是在某个控制器方法参数上添加@RequestPart注解。

```xml
<form method="POST" th:object="${spitter}" enctype="multipart/form-data">
  <label>Profile Picture</label>:
    <input type="file"
           name="profilePicture"
           accept="image/jpeg,image/png,image/gif" /><br/>
  <input type="submit" value="Register" />
</form>
```

<form>标签现在将enctype属性设置为`multipart/form-data`,这会告诉浏览器以multipart数据的形式提交表单，而不是以表单数据的形式进行提交。还添加了一个新的< input>域其type为file。 accept属性用来将文件类型限制为jpeg,png,gif格式的。根据name属性，图片数据将会发送到multipart请求中的profilePicture之中。

现在我们需要修改`processRegistration()`方法，使其能够接受上传的图片。其中一种方式就是添加btye数组，并为其添加@RequestPart注解。
```java
@RequestMapping(value="/register", method=POST)
public String processRegistration(
    @RequestPart(value = "profilePictures") byte[] profilePicture,
    @Valid Spitter spitter,
    Errors errors) {
```

当表单提交的时候，profilePicture属性将会给定一个byte数组，这个数组中包含了请求中对应的part的数据(通过@RequestPart指定的)。如果没有选择文件，那么这个数据为空(而不是null)，获取到图片数据后，`processRegistration()`方法剩下的任务就是将文件保存到某个地方。

接受MultipartFile

使用上传文件的原始byte比较简单，但是功能有限。因此，Spring提供了MultipartFile接口，它为处理multipart数据提供了内容更为丰富的对象。

```java
package org.springframework.web.multipart;
/**
 * A representation of an uploaded file received in a multipart request.
 * @author Juergen Hoeller
 * @author Trevor D. Cook
 */
public interface MultipartFile {
	/**
	 * Return the name of the parameter in the multipart form.
	 * @return the name of the parameter (never {@code null} or empty)
	 */
	String getName();
	/**
	 * Return the original filename in the client's filesystem.
	 */
	String getOriginalFilename();
	/**
	 * Return the content type of the file.
	 */
	String getContentType();
	/**
	 * Return whether the uploaded file is empty, that is, either no file has
	 * been chosen in the multipart form or the chosen file has no content.
	 */
	boolean isEmpty();
	/**
	 * Return the size of the file in bytes.
	 */
	long getSize();
	/**
	 * Return the contents of the file as an array of bytes.
	 */
	byte[] getBytes() throws IOException;
	/**
	 * Return an InputStream to read the contents of the file from.
	 */
	InputStream getInputStream() throws IOException;
	/**
	 * Transfer the received file to the given destination file.
	 */
	void transferTo(File dest) throws IOException, IllegalStateException;
}
```

可以看到，MultipartFile提供了获取文件上传文件byte的方式，还能获取原始的文件名，大小以及内容类型、还提供了一个InputStream用来将文件数据以流的方式进行读取。

除此之外，还提供了一个transferTo()方法，它能够帮助我们将上传文件写入到文件系统中。作为样例，我们在可以在`processRegistration()`方法中添加如下的几行代码，从而将上传的图片文件写入到文件系统中
```java
profilePicture.transferTo(
        new File("date/spittr" + profilePicture.getOriginalFilename()));
```
将文件保存到本地文件系统中是非常简单的，但是这需要我们对这些文件进行管理。我们需要确保有足够的空间，确保当出现硬件故障时，文件进行了备份，还需要在集群的多个服务器之间处理这些图片文件的同步。

以Part的形式接受上传的文件

Spring MVC接受javax.servlet.http.Part作为控制器方法的参数，如果使用part来替换MultiFile的话，那么processRegistration()方法签名会变成如下的形式。

```java
@RequestMapping(value="/register", method=POST)
public String processRegistration(
    @RequestPart(value="profilePictures", required=false) Part fileBytes,
    RedirectAttributes redirectAttributes,
    @Valid Spitter spitter,
    Errors errors) throws IOException {
  if (errors.hasErrors()) {
    return "registerForm";
  }
```
Part接口

```xml
package javax.servlet.http;
public interface Part {
  public InputStream getInputStream() throws IOException;

  public String getContentType();

  public String getName();

  public String getSubmittedFileName();

  public long getSize();

  public void write(String fileName) throws IOException;

  public void delete() throws IOException;

  public String getHeader(String name);

  public Collection<String> getHeaders(String name);

  public Collection<String> getHeaderNames();
}
```

很多情况下，Part方法的名称与MultiPartFile方法的名称是完全相同的。有一些比较类似，但是稍有差别。
比如`getSubmittedFileName()`方法对应`getOriginalFilename()`.类似的，write()方法对应于transfer()方法，借助该方法我们能够将上传的文件写入文件系统中。

![](https://i.imgur.com/1aQj7PW.jpg)

值得一提的是，如果没有在编写控制器方法的时候，通过Part参数的形式接受文件上传，那就没必要配置`MultipartResolver`了。只有使用`MultipartFile`的时候 ，我们才需要`MultipartResolver`.
