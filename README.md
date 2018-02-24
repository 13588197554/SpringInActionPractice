## Spring实战

## 第一部分 Spring的核心

Spring可以做很多事情，它为企业级开发提供了丰富的功能，但是这些功能都依赖于两个核心特性：依赖注入(dependency injection DI)和面向切面编程(aspect-oriented programming AOP)

在第1章中“Spring之旅”中，将快速介绍一下Spring框架，包括Spring DI 和AOP的概况，以及它们是如何帮助读者解耦应用组件的。

在第2章“装配Bean中”，将深入探讨如何将应用中的各个组件拼装到一起，将会看到Sring所提供的自动装配、基于Java的配置以及XML配置

在第3章“高级装配”中，将会告别基础的内容，为我们展现一些最大化Spring的威力的技巧和技术，包括条件化装配、处理自动装配时的歧义性、作用域以及Spring表达式语言

在第4章“面向切面Spring”中，展示如何使用Spring的AOP特性把系统级的服务(例如安全和审计)，从他们所服务的对象中解耦出来，本章也会后面的第9章、第13章和第14章做了铺垫，这几张将会分别介绍如何将SpirngAOP用于声明式安全以及缓存。

![](https://i.imgur.com/hTZUmkO.png)

## 第2部分web中的Spring

Spring通常用来开发Web应用，因此，在第2章部分中，你将会看到如何使用Spring的MVC框架为应用程序添加Web前端。

在第2章“构建Spring Web应用”中，你将会学习到Spring MVC的基本用法，它是构建在Spirng理念之上的一个Web框架，我们将会看到如何编写处理Web请求的控制器以及如何透明的绑定请求参数和负载到业务对象上，同时它还提供了数据检验和错误处理的功能。

在第6章“渲染Web视图”中，将会基于第5章的内容继续讲解，展现了如何得到Spring MVC控制器所生成的模型数据，并将其渲染为用户浏览器中的的HTML。

在第七章"Spirng MVC的高级技术"中，将会学习到构建Web应用时的一些高级技术，包括自定义Spirng MVC配置，处理multipart文件上传、处理异常，以及flash属性跨请求传递数据。

第8章中，“使用Spring Web Flow”将会为你展示如何使用Spring Web Flow来构建会话式、基于流程的Web应用程序。

鉴于安全是很多应用程序的重要关注点，因此 第9章，“保护Web应用”将会为你介绍如何使用Spirng Security来为Web应用程序提供安全性，保护应用中的信息
