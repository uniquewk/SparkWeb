# Sparkweb MVC 框架介绍 #

## 一、什么是 Sparkweb ?
Sparkweb 是一个超轻量级的简易高效的 Java WEB 开发框架，其设计思想结合了目前主流的 Spring、Struts2、Playframework、Nodejs-Expressjs、Ruby On Rails 等框架的优秀地方，完美支持 RESTful设计。


## 二、Sparkweb 框架定位
> 简易：没有过多的封装，接近底层的接口使用方式；没有XML等配置文件，采用少量的注解方式进行。
> 高效：由于无过多封装、接近底层接口使用，同时采用 ASM 来完成反射调用，使得处理过程非常的高效。、
> 实用：没有加入过多的花哨的东西，一切都从WEB开发的实用角度出发。


## 三、Sparkweb 框架的组成
Sparkweb框架包括：基础的MVC、国际化、JavaBean验证器、缓存 和 可选的 ORM 实现。 


## 四、Sparkweb 框架的基本概念
1. **控制器（Controller）：**
	Sparkweb的核心功能，控制器用于接收请求，执行响应；控制器由类级注解 `@Controller` 标记，控制器中定义N个由注解 `@Path` 定义的路由处理接口。
	eg:
	``
		
		@Controller
		public class IndexController 
		{
			@Path("/index")
			static void index(final Request req, final Response res)
			{
				//...
			}
			
			// ...
		}
	``
	
2. **路由（Router）：**
	Sparkweb的核心功能，Sparkweb通过注解 `@Path` 来定义WEB系统中设计的所有请求URL路由格式（支持 RESTful格式），`@Path` 标记的方法必须是**静态方法**，同时提供 `(final com.sparkweb.web.Request req, final com.sparkweb.web.Response res)` **请求对象** 和 **响应对象** 2个参数。
	eg:
	``
	
		@Path("/index")
		static void index(final Request req, final Response res)
		{
			res.renderView('index');
		}
		
		@Path(value="/save", method=HttpMethod.POST)
		static void save(final Request req, final Response res)
		{
			res.json("{\"result\": 0}");
		}
	``
	
	路由的详细介绍，请参见 “Router.md” 说明。

3. **模型（Model)：**
	数据实体，用于视图页面渲染提供数据。

4. **视图（View）：**
	Sparkweb 支持多种视图解析，默认提供 jsp 和  apache velocity 模板引擎视图的支持；同时内置了 `text`、`html`、`xml`、`json`、`jsonp`、`binary`的直接输出。
	开发者可以通过实现接口：`com.sparkweb.web.view.ViewResolver` 来定义自己的视图解析器。

5. **拦截器(Interceptor)：**
	Sparkweb提供了：`@Before`、`@After`、`@Catch`、`@Finally` 四种拦截器，用于在路由处理业务接口执行 **前**、 **后**、 **发生异常时** 和 **最后** 四个阶段进行特殊的处理。拦截器的详细介绍，请参加 “Interceptor.md” 说明。


## 五、Sparkweb 如何使用？
Sparkweb使用非常的简单：

1. 新建一个 Java Web 项目，将 Sparkweb的最新jar包(sparkweb-version.jar，依赖：asm-4.x.jar、commons-logging-1.x.jar) 放到 /WEB-INF/lib/ 目录下；

2. 在 web.xml 文件中加入Sparkweb分发过滤器（如果使用的是Tomcat7等支持Servlet3.0容器，则可以省略该步骤）：
``

	<filter>
		<filter-name>SparkwebDispatcher</filter-name>
		<filter-class>com.sparkweb.web.RouteDispatcher</filter-class>
	</filter>
	<filter-mapping>
		<filter-name>SparkwebDispatcher</filter-name>
		<url-pattern>/*</url-pattern>
		<dispatcher>REQUEST</dispatcher>
		<dispatcher>FORWARD</dispatcher>
		<dispatcher>ERROR</dispatcher>
	</filter-mapping>
``

3. 创建一个项目全局Sparkweb配置类，该类可以继承 `com.sparkweb.web.DefaultWebSettings` 或 实现接口 `com.sparkweb.web.WebSettings`，并且使用类注解 `@SparkwebSetting` 进行标注。该类用于配置 Sparkweb框架 需要的一些参数（更多参数请参考接口： com.sparkweb.web.WebSettings），示例代码如下：
``

	/**
	 * Sparkweb的全局配置，
	 * 更多的配置项请参考  com.sparkweb.web.WebSettings 接口。
	 */
	@SparkwebSetting
	public class DemoSparkwebSetting extends DefaultWebSettings
	{
		public DemoSparkwebSetting() {
		}
		
		/**
		 * 设置系统的默认请求和响应的编码
		 */
		@Override
		public String encoding()
		{
			return "utf-8";
		}
		
		/**
		 * 设置系统的静态资源(.css,.js,.png等)前缀路径，
		 * Sparkweb 当发现这些前缀路径资源请求时，会将请求交给J2EE容器处理。
		 * eg: <link rel="stylesheet" href="/assets/css/style.css" />
		 */
		@Override
		public String[] staticAssetsPath()
		{
			return new String[] {
				"/assets/*",
				"/uploads/*"
			};
		}
	
		/**
		 * 设置系统处理JSON数据格式采用的JSON处理器，
		 * Sparkweb 没有提供任何的默认JSON处理器，需要由开发者自己提供（实现接口：com.sparkweb.web.JSONResolver）。
		 */
		@Override
		public JSONResolver jsonResolver()
		{
			return new FastJsonResolver();
		}
	
		/**
		 * 设置系统的默认视图解析处理器。
		 * Sparkweb 默认提供了 apache velocity 模板引擎实现。
		 * 开发者可以通过实现接口：com.sparkweb.web.view.ViewRender 定义自己的视图处理器。
		 */
		@Override
		public ViewResolver viewResolver()
		{
			return new VelocityViewResolver() {
				/**
				 * 可以重新设置模板文件的后缀名，默认：.vm
				 */
				@Override
				public String viewSuffix()
				{
					return ".html";
				}
	
				/**
				 * 设置视图文件所在的webapp目录
				 */
				@Override
				public String viewDirectory()
				{
					return "/WEB-INF/views/";
				}
				
				/**
				 * 设置 velocity 配置文件所在的位置，
				 * Sparkweb 默认的是 /WEB-INF/velocity.properties。
				 */
				@Override
				public String velocityConfigLocation()
				{
					return "/WEB-INF/conf/velocity.properties";
				}
				
				/**
				 * 设置 velocity-tools 配置文件所在的位置，
				 * Sparkweb 默认的是 /WEB-INF/velocity-tools.xml。
				 */
				@Override
				public String velocityToolsConfigLocation()
				{
					return "/WEB-INF/conf/velocity-tools.xml";
				}
			};
		}
	}
``

4. 创建一个基本的首页 Controller 和 首页试图文件：
``

	IndexController.java:
	@Controller
	public class IndexController
	{
		@Path("/")
		static void index(final Request req, final Response res)
		{
			res.renderView("index");
		}
	}
	
	
	/WEB-INF/views/index.html:
	<!DOCTYPE html>
	<html>
	<head>
	    <meta charset="utf-8">
	    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
	    <title>Sparkweb Examples</title>
	</head>
	<body>
	    <h1> Hello world, Sparkweb! </h1>
	</body>
	</html>
``

5. 启动 Tomcat，浏览器访问： http://ip:port/xxx，你将会看到  Hello world, Sparkweb! 输出。


