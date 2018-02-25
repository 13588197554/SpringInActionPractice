## 5.3 接受请求的输入
Spring MVC 允许以多种方法将客户端中的数据传送到控制器的处理器方法中
- 查询数据(Query Parameter)
- 表单参数(Form Parameter)
- 路径变量(Path Variable)

作为开始，先来看下如何处理带有查询参数的请求，这也是客户端往服务器发送数据时，最简单和最直接的方法。

### 5.3.1 处理查询参数

在Spittr应用中，可能需要处理的一件事就是展现分页的Spittle列表，如果你想让用户每次查看某一页的Spittle历史，那么就需要提供一种方式让用户传递参数进来，进而确定展现那些Spittle列表。

为了实现这个分页功能，我们编写的处理方法要接受两个参数
- before参数 (表明结果中所有的SPittle的ID均在这个值之前)
- count参数(彪悍在结果中要包含的Spittle数量)

为了实现这个功能，我们将程序修改为spittles()方法替换为使用before参数和count参数的新spittles()方法。

首先添加一个测试，这个测试反映了xinspittles()方法的功能

```java
@Test
public void shouldShowPagedSpittles() throws Exception {
    List<Spittle> expectedSpittles = createSpittleList(50);
    SpittleRepository mockRepository = mock(SpittleRepository.class);
    when(mockRepository.findSpittles(238900, 50))
            .thenReturn(expectedSpittles);

    SpittleController controller = new SpittleController(mockRepository);
    MockMvc mockMvc = standaloneSetup(controller)
            .setSingleView(new InternalResourceView("/WEB-INF/views/spittles.jsp"))
            .build();

    mockMvc.perform(get("/spittles?max=238900&count=50"))
            .andExpect(view().name("spittles"))
            .andExpect(model().attributeExists("spittleList"))
            .andExpect(model().attribute("spittleList",
                    hasItems(expectedSpittles.toArray())));
}
```

这个测试方法关键点在于同时传入了max和count参数，它测试了这些参数存在时的处理方法，而另一个则测试了没有这些参数的情景。


在这个测试之后，我们就能确保不管控制器发生了什么样的变化，它都能够处理这两种类型的请求。

```java
@RequestMapping(method = RequestMethod.GET)
public List<Spittle> spittles(
        @RequestParam(value = "max") long max,
        @RequestParam(value = "count") int count) {
    return spittleRepository.findSpittles(max, count);
}
```

SittleController中的处理器方法同时要处理有参数和没参数的场景，那我们需要对其进行修改，让它能接受参数。同时如果这些参数在请求中不存在的话，就是用默认值Long.MAX_VALUE和20.@RequestParam注解的defaultValue属性可以完成这个任务。

```java
@RequestMapping(method=RequestMethod.GET)
public List<Spittle> spittles(
    @RequestParam(value="max", defaultValue=MAX_LONG_AS_STRING) long max,
    @RequestParam(value="count", defaultValue="20") int count) {
  return spittleRepository.findSpittles(max, count);
}
```

现在如果max如果没有参数指定的话，它将会是Long的最大值。

因为查询参数都是String 类型 ，因此defaultValue属性需要String类型，

```java
private static final String MAX_LONG_AS_STRING = long.toString(Long.MAX.VALUE)
```

**请求中的查询参数是往控制器中传递信息的常用手段。另外一种方式就是将传递的参数作为请求路径的一部分。**

### 5.3.2 通过路径参数接受输入

假设我们的应用程序需要根据给定的ID来展现某一个Spittle记录。其中一种方案就是编写处理器方法，通过使用@RequestParam注解，让它接受ID作为查询参数。

```java
@RequestMapping(value="/show",method = RequestMethod.GET)
public String showSpittle(
      @RequestParam("spittle_id") long spittleId, Model model) {
      model.addAttribute(spittleRepository.findOne(spittleId));
      return "spittle";
}
```

在理想情况下，要识别资源应用应该通过URL路径来标识，而不是通过查询参数。对“/spittles/12345”发起请求要优于对“/spittles/show?spittle_id=12345”发起的请求。前者能识别出要查询的资源，而后者描述的是带有参数的一个操作——本质上是通过HTTP发起的RPC。

既然已经以面向资源的控制器作为目标，那我们将这个需求转化为一个测试。

```java
@Test
public void testSpittle() throws Exception {
  Spittle expectedSpittle = new Spittle("Hello", new Date());
  SpittleRepository mockRepository = mock(SpittleRepository.class);
  when(mockRepository.findOne(12345)).thenReturn(expectedSpittle);

  SpittleController controller = new SpittleController(mockRepository);
  MockMvc mockMvc = standaloneSetup(controller).build();

  mockMvc.perform(get("/spittles/12345"))
    .andExpect(view().name("spittle"))                                 //断言图片的名称为spittle
    .andExpect(model().attributeExists("spittle"))                     //预期Spittle放到了模型之中
    .andExpect(model().attribute("spittle", expectedSpittle));
}
```

这个测试构建了一个mockRepository，一个控制器和MockMvc

到目前为止，我们所编写的控制器，所有的方法都映射到了静态定义好的路径上，**还需要包含变量部分**

为了实现这种路径变量，Spring MVC允许我们在@RequestMapping路径中添加占位符，占位符的名称需要({..}),路径中的其他部分要与所处理的请求完全匹配，但是占位符可是是任意的值。

```java
@RequestMapping(value="/{spittleId}",method = RequestMethod.GET)
public String showSpittle(@PathVariable("spittleId") long spittleId, Model model) {
      model.addAttribute(spittleRepository.findOne(spittleId));
      return "spittle";
}
```

@PathVariable("spittleId") 表明在请求路径中，不管占位符部分的值是什么都会传递给处理器方法的showSpittle参数中。

也可以去掉这个value的值，因为方法的参数碰巧与占位符的名称相同。
```java
@RequestMapping(value="/{spittleId}",method = RequestMethod.GET)
public String showSpittle(@PathVariable long spittleId, Model model) {
      model.addAttribute(spittleRepository.findOne(spittleId));
      return "spittle";
}
```

如果传递请求中少量的数据，那查询参数和路径变量是合适的，但通常我们还需要传递很多的数据，(表单数据)，那么查询显得有些笨拙和受限制了。

## 5.4 处理表单

Web应用的功能不局限于为用户推送内容，大多数的应用允许用户填充表单，并将数据提交回应用中，通过这种方式实现与用户的交互。

使用表单分为两个方面：展现表单以及处理用户通过表单提交的数据。在Spittr应用中，我们需要有个表单让用户进行注册，SitterController是一个新的控制器，目前只有一个请求处理的方法来展现注册表单。

```java
@Controller
@RequestMapping("/spitter")
public class SpitterController {
    //处理对“/spitter/register”
    @RequestMapping(value = "/register",method = RequestMethod.GET)
      public String showRegistrationForm() {
          return "registerForm";
      }
}
```

测试展现表单的控制器方法(老外每次都测试)

```java
@Test
public void shouldShowRegistration() throws Exception {
  SpitterController controller = new SpitterController();
  MockMvc mockMvc = standaloneSetup(controller).build();
  mockMvc.perform(get("/spitter/register"))
         .andExpect(view().name("registerForm"));
}
}
```

这个JSP必须包含一个HTML<form>标签，
```xml
<form method="POST" name="spittleForm">
  <input type="hidden" name="latitude">
  <input type="hidden" name="longitude">
  <textarea name="message" cols="80" rows="5"></textarea><br/>
  <input type="submit" value="Add" />
</form>
```
需要注意的是这里的<form>标签中并没有设置action属性。在这种情况下，当表单体提交的时，它会提交到与展现时相同的URL路径上，它会提交到“/spitter/reqister”上。

这意味着需要在服务器端编写该HTTP POST请求。

### 5.4.1 编写处理表单的处理器

当处理注册表单的POST请求时，控制器需要接受表单数据，并将表单数据保存为Spitter对象。最后为了防止重复提交(用户刷新页面)，应该将浏览器重定向到新创建用户的基本信息页面。

```java
@Test
public void shouldProcessRegistration() throws Exception {
  SpitterRepository mockRepository = mock(SpitterRepository.class);
  Spitter unsaved = new Spitter("jbauer", "24hours", "Jack", "Bauer", "jbauer@ctu.gov");
  Spitter saved = new Spitter(24L, "jbauer", "24hours", "Jack", "Bauer", "jbauer@ctu.gov");
  when(mockRepository.save(unsaved)).thenReturn(saved);

  SpitterController controller = new SpitterController(mockRepository);
  MockMvc mockMvc = standaloneSetup(controller).build();

  mockMvc.perform(post("/spitter/register")
         .param("firstName", "Jack")
         .param("lastName", "Bauer")
         .param("username", "jbauer")
         .param("password", "24hours")
         .param("email", "jbauer@ctu.gov"))
         .andExpect(redirectedUrl("/spitter/jbauer"));

  verify(mockRepository, atLeastOnce()).save(unsaved);
}
```

**希望大家也可以学会这样方式**

![](https://i.imgur.com/F2Ba7VE.jpg)

在构建完SpitterRepository的mock实现以及所要执行的控制器和MockNvc之后，shouldProcessRegistration()对“/spitter/register”发起了一个POST请求，作为请求的一部分，用户信息以参数的形式放到request中，从而模拟提交的表单。

```java
/**
 * Created by guo on 24/2/2018.
 */
@Controller
@RequestMapping("/spitter")
public class SpitterController {

    private SpitterRepository spitterRepository;

    @Autowired
    public SpitterController(SpitterRepository spitterRepository) {      //注入SpiterRepository
        this.spitterRepository = spitterRepository;
    }

    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public String showRegistrationForm() {
        return "registerForm";
    }

    @RequestMapping(value = "/register",method = RequestMethod.POST)
    public String procesRegistration(Spitter spitter) {
        spitterRepository.save(spitter);                                //保存Spitter
        return "redirect:/spitter/" + spitter.getUsername();            //重定向到基本信息页面
    }
}
```

返回一个String类型，用来指定视图。但是这个视图格式和以前有所不同。这里不仅返回了视图的名称供视图解析器查找目标视图，而且返回的值还带有重定向的格式`return "redirect:/spitter/" ` 当看到视图格式中有“redirect：”前缀时，它就知道要将其解析为重定向的规则，而不是试图的名称。在本例中，它将会重定向到基本信息的页面。

需要注意的是**除了可以“redirect”还可以识别“forward：”前缀，请求将会前(forward)往指定的URL路径，而不再是重定向。**


在SpitterController中添加一个处理器方法，用来处理对基本信息页面的请求。

```java
@RequestMapping(value = "/{username}",method = RequestMethod.GET)
public String showSpitterProfile(@PathVariable String username, Model model) {
    Spitter spitter = spitterRepository.findByUsername(username);
    model.addAttribute(spitter);
    return "profile";
}
```
spitterRepository通过用户获取一个Spitter对象，showSpitterProfile()方法得到这个对象并将其添加到模型中，然后返回profile。也就是基本信息页面的逻辑视图。

```xml
<body>
  <h1>Your Profile</h1>
  <c:out value="${spitter.username}" /><br/>
  <c:out value="${spitter.firstName}" /> <c:out value="${spitter.lastName}" /><br/>
  <c:out value="${spitter.email}" />
</body>
```
![](https://i.imgur.com/HyEhsLI.jpg)
注意：这里使用H2数据库，太有用了。
```java
@Configuration
public class DataConfig {

  @Bean
  public DataSource dataSource() {
    return new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.H2)
            .addScript("schema.sql")
            .build();
  }
  @Bean
  public JdbcOperations jdbcTemplate(DataSource dataSource) {
    return new JdbcTemplate(dataSource);
  }
}

```
如果表单中没有发送username或password，会发生什么情况呢？或者名字太长，由会怎么样？，接下来，让我们看一下为表单添加校验，而从避免数据呈现不一致性。


### 5.4.2 校验表单

如果用户在提交表单的时候，username和password为空的话，那么将会导致在新建Spitter对象中，username和password是空的String。如果不处理，将会出项安全问题。

同时我们应该阻止用户提交空的名字。限制这些输入的长度。

从Spring 3.0 开始，在Spring MVC中提供了java校验的API的支持。只需要在类路径下包含这个JavaAPI的实现即可。比如Hibernate validator.

Java校验API定义了多个注解，这些注解可以用在属性上，从而限制这些属性的值。

- @Size    :所注解的元素必须是String、集合、或数组，并且长度要符合要求
- @Null    ：所注解的值必须为Null
- @NotNull  ：所注解的元素不能为Null。
- @Max     ：所注解的必须是数字，并且值要小于等于给定制。
- @Min
- @Past   ：所注解的元素必须是一个已过期的日期
- @Future ：必须是一个将来的日期
- @Pattern：必须匹配给定的正则表达式

```java
public class Spitter {

  private Long id;

  @NotNull
  @Size(min=5, max=16)
  private String username;

  @NotNull
  @Size(min=5, max=25)
  private String password;

  @NotNull
  @Size(min=2, max=30)
  private String firstName;

  @NotNull
  @Size(min=2, max=30)
  private String lastName;

  @NotNull
  @Email
  private String email;

  忽略其他方法。
}
```

```java
@RequestMapping(value="/register", method=POST)      //老外喜欢静态导入特性
public String processRegistration(
    @Valid Spitter spitter,                          //校验Spitter输入
    Errors errors) {
  if (errors.hasErrors()) {
    return "registerForm";                           //如果校验出现错误，则重新返回表单
  }

  spitterRepository.save(spitter);
  return "redirect:/spitter/" + spitter.getUsername();
}
```

Spitter参数添加了@Valid注解，这会告诉Spring，需要确保这个对象满足校验限制。

如果表单出错的话，那么这些错误可以通过Errors进行反问。

很重要一点需要注意的是：Errors参数要紧跟在带有Valid注解参数的后面。@Valid注解所标注的就是要校验的参数。

如果没有错误的话，Spitter对象将会通过Repository进行保存，控制器会像之前那样重定向到基本信息页面。

## 5.5 小节

在本章中，我们为编写应用程序的Web部分开来一个好头，可以看到Spring有一个强大而灵活的Web框架。借助于注解，Spring MVC 提供了近似于POJO的开发模式，这使得开发处理请求的控制器变得简单，**同时也易于测试。**

当编写控制器的处理方法时，Spring MVC及其灵活。概括来讲，如果你的处理器方法需要内容的话，只需将对应的对象作为参数，而他不需要的内容，则没有必要出现在参数列表中。这样，就为请求带来了无限的可能性，同时还能保持一种简单的编程模型。

尽管本章中很多内容都是关于控制器的请求处理的，但是渲染响应也同样重要，我们通过使用JSP的方式，简单了解了如何为控制器编写视图，但是，就Spring MVC视图来说，它并不是本章所看到的简单JSP。

在接下来的第6章，我们将会更深入的学习Spring视图，包括如何在JSP中使用Spring标签库，还会学习如何借助于Apache Tiles为视图添加一致的结构。同时，还会了解Thymeleaf，这是一个很有意思的JSP替代方法，Spring为其提供了内置的支持。

真的非常期待下一章，，，，加油
