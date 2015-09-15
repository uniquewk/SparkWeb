package com.sparkweb.web.router;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sparkweb.binding.Binder;
import com.sparkweb.binding.ParamNode;
import com.sparkweb.reflect.MethodAccess;
import com.sparkweb.scanner.ClassScanner;
import com.sparkweb.scanner.criteria.AnnotationCriteria;
import com.sparkweb.scanner.criteria.ClassCriteria;
import com.sparkweb.util.Matcher;
import com.sparkweb.web.ActionInterceptor;
import com.sparkweb.web.ActionInvoker;
import com.sparkweb.web.HttpContext;
import com.sparkweb.web.HttpMethod;
import com.sparkweb.web.Request;
import com.sparkweb.web.Response;
import com.sparkweb.web.annotation.After;
import com.sparkweb.web.annotation.Before;
import com.sparkweb.web.annotation.CSRF;
import com.sparkweb.web.annotation.Catch;
import com.sparkweb.web.annotation.Controller;
import com.sparkweb.web.annotation.Finally;
import com.sparkweb.web.annotation.Path;
import com.sparkweb.web.annotation.PathParam;
import com.sparkweb.web.annotation.RequestBody;
import com.sparkweb.web.annotation.With;
import com.sparkweb.web.result.NoResult;
import com.sparkweb.web.result.Result;
import com.sparkweb.web.security.CsrfGuard;

/**
 * 路由器，负责扫描所有配置的路由信息并进行解析、初始化 和 路由分发。
 *
 * @author yswang
 * @version 1.0
 */
public final class RouteHandler
{
	private static final Log log = LogFactory.getLog(RouteHandler.class);
	
	private static final String ROUTE_CFG = "com/sparkweb/web/router/route.properties";
	
	private static Properties routes_cfg = new Properties();
	
	static {
		InputStream is = null;
		try {
			is = Thread.currentThread().getContextClassLoader().getResourceAsStream(ROUTE_CFG);
			routes_cfg.load(is);
		} catch(IOException e) {
			//ignore
		} finally {
			if(is != null) {
				try {
					is.close();
				} catch(IOException e) {
					// ignore
				}
			}
		}
	}
	
	/**
	 * 存放所有的HTTP method: GET, POST, HEAD, PUT, DELETE, TRACE, CONNECT, OPTIONS 定义的路由规则
	 */
	private Map<String, List<Route>> routes_map = new HashMap<String, List<Route>>(8);
	
	/**
	 * 存储所有@Before等拦截器信息（包括通过 @With 引入的）
	 */
	private Map<String, List<ActionInterceptor>> before_interceptors = new HashMap<String, List<ActionInterceptor>>(20);
	private Map<String, List<ActionInterceptor>> after_interceptors = new HashMap<String, List<ActionInterceptor>>(20);
	private Map<String, List<ActionInterceptor>> finally_interceptors = new HashMap<String, List<ActionInterceptor>>(20);
	private Map<String, List<ActionInterceptor>> catch_interceptors = new HashMap<String, List<ActionInterceptor>>(20);
	
	/**
	 * 是否是大小写区分
	 */
	private boolean caseSensitive = false;
	
	/**
	 * 是否是严格匹配模式，即：路径结尾是否允许含有反斜杠 /。
	 * <br>在严格模式下， /users != /users/
	 * <br>在非严格模式下，/users == /users/
	 */
	private boolean strict = false;
	
	private boolean hasLoaded = false;
	private boolean hasNormalized = false;


	public RouteHandler() 
	{
		this(false, false);
	}
	
	public RouteHandler(boolean caseSensitive, boolean strict) 
	{
		this.caseSensitive = caseSensitive;
		this.strict = strict;
	}
	
	/**
	 * 从当前webapp的classpath环境下加载配置的路由信息
	 * 
	 * @param servletContext
	 * @param jarsToSkip
	 */
	public synchronized void loadRoutes(ServletContext servletContext, String[] jarsToSkip)
	{
		if(hasLoaded)
		{
			return;
		}
		
		hasLoaded = true;
		
		ClassScanner scanner = new ClassScanner();
		scanner.addJarToSkip(jarsToSkip);
		
		if(routes_cfg.getProperty("webspark.routes.scanner.jarsToSkip") != null)
		{
			scanner.addJarToSkip(routes_cfg.getProperty("webspark.routes.scanner.jarsToSkip").split(","));
		}
		
		if(routes_cfg.getProperty("webspark.routes.scanner.packagesToSkip") != null)
		{
			scanner.addPackageToSkip(routes_cfg.getProperty("webspark.routes.scanner.packagesToSkip").split(","));
		}
		
		// 扫描所有使用 com.webspark.web.annotation.Controller 注解定义的类
		ClassCriteria classCriteria = new AnnotationCriteria(Controller.class);
		scanner.addClassCriteria(classCriteria);
		// 如果 ServletContext 存在，则从当前WEBAPP的WEB-INF/lib下进行扫描，提高扫描速度；否则，从全局 Classpath下扫描
		Set<Class<?>> classes = servletContext != null ? scanner.scanWebapp(servletContext) 
														: scanner.scanClasspath();
		
		parseRoutes(classes);
		
	}
	
	/**
	 * 从扫描到的类中解析路由信息
	 */
	private void parseRoutes(Set<Class<?>> classes)
	{
		if(classes == null || classes.size() == 0)
		{
			log.warn("Cannot find any Path route defined!");
			return;
		}
		
		for(Class<?> cls : classes)
		{
			if(!cls.isAnnotationPresent(Controller.class))
			{
				continue;
			}
			
			try
			{
				// 使用 ASM 进行高效反射调用，ASM的反射invoke性能是Java默认反射处理性能的5倍多
				// 每个类只进行一次asm的解析
				MethodAccess mcc = MethodAccess.get(cls);
				
				// 解析路由
				parseClassRoutes(mcc);
				
				// 解析拦截器
				parseClassInterceptors(mcc, cls);
				parseWithInterceptors(cls, cls);
				
			} catch(Exception e)
			{
				log.error(String.format("Class<%s> not found!", cls.getName()), e);
			} 
		}
		
		// 优化
		this.normalize();
	}
	
	/**
	 * 解析Controller Class 中定义的所有路由信息
	 * 
	 * @param mAcc
	 */
	private void parseClassRoutes(MethodAccess mAcc)
	{
		// 解析定义在类上的 @Path 注解
		// 定义在类上的  @Path 注解只有value属性定义有效(用于作为该类下所有方法定义的url相对路径的前缀)，
		// method 属性定义忽略。
		Path classPathRoute = mAcc.getDeclaringClass().getAnnotation(Path.class);
		
		// 可能定义在Class @Path 上的基础路由前缀路径
		String[] routeBaseUrls = {"/"};
		if(classPathRoute != null && classPathRoute.value().length > 0)
		{
			routeBaseUrls = classPathRoute.value();
		}
		
		// normalize base urls
		for(int i = 0, len = routeBaseUrls.length; i < len; i++)
		{
			if(routeBaseUrls[i].charAt(0) != '/')
			{
				routeBaseUrls[i] = '/' + routeBaseUrls[i];
			}
		}
		
		// Find @Path method
		Method[] methods = mAcc.getMethods();
		if(methods == null || methods.length == 0)
		{
			return;
		}
		
		Method _method = null;
		for(int i = 0, len = methods.length; i < len; i++)
		{
			_method = methods[i];
			if(!_method.isAnnotationPresent(Path.class))
			{
				continue;
			}
			
			// @Path 标注的方法必须是静态的
			checkMethodStatic(_method);
			
			Path routePath = _method.getAnnotation(Path.class);
			boolean needCsrfCheck = _method.isAnnotationPresent(CSRF.class);
			
			for(HttpMethod httpMethod : routePath.method())
			{
				String[] routePaths = routePath.value();
				if(routePaths.length == 0)
				{
					routePaths = new String[]{"/"};
				}
				
				for(String path : routePaths)
				{
					if(path.charAt(0) != '/')
					{
						path = '/' + path;
					}
					
					for(String baseUrl : routeBaseUrls)
					{
						Route route = new Route(httpMethod, PathParser.normalizePath(baseUrl + path), this.caseSensitive, this.strict);
						route.setController(mAcc.getDeclaringClass());
						route.setActionInvoker(new ActionInvoker(mAcc, i));
						route.setNeedCsrfCheck(needCsrfCheck);
						
						// register route
						registerRoute(route);
					}
				}
			}
		}
		
	}
	
	/**
	 * 解析出类中定义的 @Before, @After, @Finally 等拦截器
	 * 
	 * @param mAcc 包含拦截器的Class MethodAccess
	 * @param controllerClass 这些拦截器应该所属的 Controller
	 */
	private void parseClassInterceptors(MethodAccess mAcc, Class<?> controllerClass)
	{
		Method[] methods = mAcc.getMethods();
		if(methods == null || methods.length == 0)
		{
			return;
		}
		
		String controller = controllerClass.getName();
		
		if(before_interceptors.get(controller) == null)
		{
			before_interceptors.put(controller, new ArrayList<ActionInterceptor>());
		}
		
		if(after_interceptors.get(controller) == null)
		{
			after_interceptors.put(controller, new ArrayList<ActionInterceptor>());
		}
		
		if(finally_interceptors.get(controller) == null)
		{
			finally_interceptors.put(controller, new ArrayList<ActionInterceptor>());
		}
		
		if(catch_interceptors.get(controller) == null)
		{
			catch_interceptors.put(controller, new ArrayList<ActionInterceptor>());
		}
		
		Method _method;
		for(int i = 0, len = methods.length; i < len; i++)
		{
			_method = methods[i];
			
			if(_method.isAnnotationPresent(Before.class))
			{
				// 拦截器方法必须是静态的
				checkMethodStatic(_method);
				before_interceptors.get(controller).add(new ActionInterceptor(mAcc, i));
			}
			
			if(_method.isAnnotationPresent(After.class))
			{
				checkMethodStatic(_method);
				after_interceptors.get(controller).add(new ActionInterceptor(mAcc, i));
			}
			
			if(_method.isAnnotationPresent(Finally.class))
			{
				checkMethodStatic(_method);
				finally_interceptors.get(controller).add(new ActionInterceptor(mAcc, i));
			}
			
			if(_method.isAnnotationPresent(Catch.class))
			{
				checkMethodStatic(_method);
				catch_interceptors.get(controller).add(new ActionInterceptor(mAcc, i));
			}
			
		}
	}
	
	/**
	 * 解析类上通过 @With 引入的拦截器信息
	 * 
	 * @param cls
	 */
	private void parseWithInterceptors(Class<?> cls, Class<?> controllerClass)
	{
		if(!cls.isAnnotationPresent(With.class))
		{
			return;
		}
		
		With with = cls.getAnnotation(With.class);
		if(with.value().length == 0)
		{
			return;
		}
		
		for(Class<?> withClass : with.value())
		{
			// 获取withClass中定义的 @Before等拦截器
			parseClassInterceptors(MethodAccess.get(withClass), controllerClass);
			
			// 递归解析 @With
			parseWithInterceptors(withClass, controllerClass);
		}
	}
	
	
	/**
	 * 检测给定的方法必须是静态方法
	 */
	private static void checkMethodStatic(Method method)
	{
		// @Path 定义的方法必须是 static 类型的
		if(!Modifier.isStatic(method.getModifiers()))
		{
			throw new RouteException(String.format("The method <%s.%s()> must be a static method!", 
					method.getDeclaringClass().getName(), method.getName()));
		}
	}
	
	/**
	 * 存储路由信息
	 * 
	 * @param method
	 * @param path
	 * @param callback
	 * @return
	 */
	private synchronized void registerRoute(Route route)
	{
		String _method = route.getMethod().name().toUpperCase(Locale.US);

		if(this.routes_map.get(_method) == null)
		{
			this.routes_map.put(_method, new ArrayList<Route>(100));
		}
		
		if(!this.routes_map.get(_method).contains(route))
		{
			this.routes_map.get(_method).add(route);
			
			log.info(route);
		}
	}

	/**
	 * 路由分发
	 * @return
	 */
	public boolean dispatch() throws Throwable
	{
		MatchedRoute matchedRoute = matchRequest(HttpContext.current().request());
		
		if(matchedRoute == null)
		{
			return false;
		}
		
		HttpContext.current().matchedRoute(matchedRoute);
		
		processRoute(matchedRoute.getRoute());
		
		return true;
	}
	
	private void processRoute(final Route route) throws Throwable
	{
		Request request = HttpContext.current().request();
		Response response = HttpContext.current().response();
		
		try
		{
			if(route.isNeedCsrfCheck())
			{
				CsrfGuard.getInstance().isValidRequest(request.servletRequest(), response.httpServletResponse());
			}
			
			Result actionResult = null;
			//String cacheKey = null;
			
			// @Before interceptors
			// may throw Result or Exception
			handleBefores(route);
			
			// Route action invoke
			ActionInvoker actionInvoker = route.getActionInvoker();
			
			// Check the cache (only for GET or HEAD)
			// TODO Cache for Result supporting...
            /*if(("GET".equals(request.method()) || "HEAD".equals(request.method())) 
            		&& actionInvoker.getMethod().isAnnotationPresent(CacheFor.class)) 
            {
                cacheKey = actionInvoker.getMethod().getAnnotation(CacheFor.class).key();
                
                if(cacheKey == null || cacheKey.trim().length() == 0) 
                {
                    cacheKey = "urlcache:" + request.url() + request.queryString();
                }
                
                actionResult = (Result) CacheManager.get(cacheKey);
            }*/
			
			if(actionResult == null)
			{
				Class<?>[] argsType = actionInvoker.getMethod().getParameterTypes();
				Object[] args = null;
				
				if(argsType != null && argsType.length > 0)
				{
					args = new Object[argsType.length];
					
					for(int i = 0, len = argsType.length; i < len; i++)
					{
						Class<?> argType = argsType[i];
						String argName = actionInvoker.getMethodParameterNames()[i];
						
						if(argType == Request.class)
						{
							args[i] = request;
							continue;
						}
						else if(argType == Response.class)
						{
							args[i] = response;
							continue;
						}
						else 
						{
							Map<String, String[]> params = new HashMap<String, String[]>();
							Annotation[] paramAnnos = actionInvoker.getMethod().getParameterAnnotations()[i];
							
							if(paramAnnos != null && paramAnnos.length > 0)
							{
								if(paramAnnos[0].annotationType() == PathParam.class)
								{
									PathParam pathParam = (PathParam)paramAnnos[0];
									params.put(argName, request.pathParams(pathParam.value().trim().length() == 0 
																			? argName 
																			: pathParam.value().trim()));
								} 
								else if(paramAnnos[0].annotationType() == RequestBody.class)
								{
									if(request.header("Content-Type").indexOf("application/x-www-form-urlencoded") == -1)
									{
										if(CharSequence.class.isAssignableFrom(argType))
										{
											args[i] = request.body();
										}
										else if(request.header("Content-Type").indexOf("application/json") != -1)
										{
											args[i] = request.body(argType);
										}
										else
										{
											args[i] = null;
										}
									}
									else
									{
										args[i] = null;
									}
									
									continue;
								}
							} 
							else if(CharSequence.class.isAssignableFrom(argType)) //argType == String.class
				            {
								args[i] = request.param(argName);
								continue;
				            }
							else if(Number.class.isAssignableFrom(argType) || argType.isPrimitive()) 
				            {
				                params.put(argName, request.params(argName));
				            }
				            else 
				            {
				                params.putAll(request.params());
				            }
				            
							args[i] = Binder.bind(
								 			ParamNode.convert(params),
								 			argName,
					                        argType,
					                        actionInvoker.getMethod().getGenericParameterTypes()[i],
					                        paramAnnos,
					                        new Binder.MethodAndParamInfo(null, actionInvoker.getMethod(), i + 1)
										);
						}
					}
				}
				
				try
				{
					// Invoke the action
					// may throw Result or Exception
					actionInvoker.invoke(args);
				} 
				catch(Result result) 
				{
					actionResult = result;
					
					// TODO Cache for Result supporting...
					// Cache it if needed
                    /*if(cacheKey != null) 
                    {
                       Cache.set(cacheKey, actionResult, actionInvoker.getMethod().getAnnotation(CacheFor.class).region());
                    }*/
				}
				catch(Exception ex)
				{
					if(ex instanceof Result)
					{
						actionResult = (Result) ex;
						
						// TODO Cache for Result supporting...
						// Cache it if needed
	                    /*if(cacheKey != null) 
	                    {
	                       Cache.set(cacheKey, actionResult, actionInvoker.getMethod().getAnnotation(CacheFor.class).region());
	                    }*/
					}
					else 
					{
						handleCatches(ex, route);
					}
				}
			}
			
			// @After interceptors
			handleAfters(route);
			
			if(actionResult != null)
			{
				throw actionResult;
			}
			
			throw new NoResult();
		}
		catch(Result result) 
		{
			if(!(result instanceof NoResult))
			{
				// invoke action
				result.apply(request, response);
				// @Finally
				handleFinallies(null, route);
			}
		}
		catch(Throwable e)
		{
			handleFinallies(e, route);
		}
	}
	
	private void handleBefores(final Route route) throws Throwable
	{
		List<ActionInterceptor> befores = before_interceptors.get(route.getController().getName());
		if(befores == null || befores.isEmpty())
		{
			return;
		}
		
		for(ActionInterceptor interceptor : befores)
		{
			Before before = interceptor.getInterceptor(Before.class);
			if(skipInterceptor(route, before.only(), before.unless()))
			{
				continue;
			}
			
			Class<?>[] argsType = interceptor.getMethod().getParameterTypes();
			Object[] args = null;
			
			if(argsType != null && argsType.length > 0)
			{
				args = new Object[argsType.length];
				for(int i = 0, len = argsType.length; i < len; i++)
				{
					if(argsType[i] == Request.class)
					{
						args[i] = HttpContext.current().request();
					}
					else if(argsType[i] == Response.class)
					{
						args[i] = HttpContext.current().response();
					}
					else {
						args[i] = null;
					}
				}
			}
			
			interceptor.invoke(args != null ? args : new Object[0]);
		}
	}
	
	private void handleAfters(final Route route) throws Throwable
	{
		List<ActionInterceptor> afters = after_interceptors.get(route.getController().getName());
		if(afters == null || afters.isEmpty())
		{
			return;
		}
		
		for(ActionInterceptor interceptor : afters)
		{
			After after = interceptor.getInterceptor(After.class);
			if(skipInterceptor(route, after.only(), after.unless()))
			{
				continue;
			}
			
			Class<?>[] argsType = interceptor.getMethod().getParameterTypes();
			Object[] args = null;
			
			if(argsType != null && argsType.length > 0)
			{
				args = new Object[argsType.length];
				for(int i = 0, len = argsType.length; i < len; i++)
				{
					if(argsType[i] == Request.class)
					{
						args[i] = HttpContext.current().request();
					}
					else if(argsType[i] == Response.class)
					{
						args[i] = HttpContext.current().response();
					}
					else {
						args[i] = null;
					}
				}
			}
			
			interceptor.invoke(args != null ? args : new Object[0]);
		}
	}
	
	private void handleFinallies(Throwable e, final Route route) throws Throwable
	{
		List<ActionInterceptor> finallies = finally_interceptors.get(route.getController().getName());
		if(finallies == null || finallies.isEmpty())
		{
			return;
		}
		
		for(ActionInterceptor interceptor : finallies)
		{
			Finally _finally = interceptor.getInterceptor(Finally.class);
			if(skipInterceptor(route, _finally.only(), _finally.unless()))
			{
				continue;
			}
			
			Class<?>[] argsType = interceptor.getMethod().getParameterTypes();
			Object[] args = null;
			//invoking @Finally method with caughtException as parameter
			if(argsType != null && argsType.length > 0)
			{
				args = new Object[argsType.length];
				for(int i = 0, len = argsType.length; i < len; i++)
				{
					if(Throwable.class.isAssignableFrom(argsType[i]))
					{
						args[i] = e;
					}
					else if(argsType[i] == Request.class)
					{
						args[i] = HttpContext.current().request();
					}
					else if(argsType[i] == Response.class)
					{
						args[i] = HttpContext.current().response();
					}
					else {
						args[i] = null;
					}
				}
			}
			
			interceptor.invoke(args != null ? args : new Object[0]);
		}
			
	}
	
	private void handleCatches(Throwable e, final Route route) throws Throwable
	{
		List<ActionInterceptor> catches = catch_interceptors.get(route.getController().getName());
		if(catches == null || catches.isEmpty())
		{
			throw e;
		}
		
		for(ActionInterceptor interceptor : catches)
		{
			Catch aCatch = interceptor.getInterceptor(Catch.class);
			Class<?>[] exceptions = aCatch.value();
			
            if(exceptions.length == 0) 
            {
                exceptions = new Class[]{Exception.class};
            }
            
            for(Class<?> exception : exceptions) 
            {
                if(exception.isInstance(e)) 
                {
                	Class<?>[] argsType = interceptor.getMethod().getParameterTypes();
    				Object[] args = null;
    				
    				if(argsType != null && argsType.length > 0)
    				{
    					args = new Object[argsType.length];
    					
    					for(int i = 0, len = argsType.length; i < len; i++)
    					{
    						if(Throwable.class.isAssignableFrom(argsType[i]))
    						{
    							args[i] = e;
    						}
    						else if(argsType[i] == Request.class)
    						{
    							args[i] = HttpContext.current().request();
    						}
    						else if(argsType[i] == Response.class)
    						{
    							args[i] = HttpContext.current().response();
    						}
    						else {
    							args[i] = null;
    						}
    					}
    				}
                	
                	interceptor.invoke(args != null ? args : new Object[0]);
                    return;
                }
            }
		}
		
		throw e;
	}
	
	/**
	 * 判断一个路由是否需要跳过相应的拦截器规则
	 */
	private static boolean skipInterceptor(Route route, String[] only, String[] unless)
	{
		boolean skip = false;
		
		if(only != null && only.length > 0)
		{
			for(String on : only)
			{
				if(on == null || on.trim().length() == 0)
				{
					continue;
				}
				
				// action = ControllerClassName.MethodName
				String action = route.getActionInvoker().getAction();
				
				// match Class.Method --> test.TestController.testAPI
				if(!on.contains(".") && !on.contains("/"))
				{
					on = route.getActionInvoker().getMethod().getDeclaringClass().getName() + '.' + on;
				}
				// match request path --> /api/* :: /api/users
				else if(on.contains("/"))
				{
					if(on.charAt(0) != '/')
					{
						on = '/' + on;
					}
					
					action = HttpContext.current().request().path();
				}
				
				if(Matcher.match(on, action, false))
				{
					skip = false;
					break;
				} 
				else {
					skip = true;
				}
			}
		}
		
		if(unless != null && unless.length > 0)
		{	
			for(String un : unless)
			{
				if(un == null || un.trim().length() == 0)
				{
					continue;
				}
				
				// action = ControllerClassName.MethodName
				String action = route.getActionInvoker().getAction();
				
				// unmatch Class.Method --> test.TestController.testAPI
				if(!un.contains(".") && !un.contains("/"))
				{
					un = route.getActionInvoker().getMethod().getDeclaringClass().getName() + '.' + un;
				}
				// unmatch request path --> /api/* :: /api/users
				else if(un.contains("/"))
				{
					if(un.charAt(0) != '/')
					{
						un = '/' + un;
					}
					
					action = HttpContext.current().request().path();
				}
				
				if(Matcher.match(un, action, false))
				{
					skip = true;
					break;
				} 
			}
		}
		
		return skip;
	}
	
	/**
	 * 匹配请求
	 * 
	 * @param request
	 * @return
	 */
	private MatchedRoute matchRequest(Request request)
	{
		String _method = request.method().toUpperCase(Locale.US);
		List<Route> routes = this.routes_map.get(_method);
		
		if(routes == null || routes.isEmpty())
		{
			return null;
		}
		
		String reqPath = request.path();
		MatchedRoute mRoute = null;
		
		for(int i = 0, size = routes.size(); i < size; ++i)
		{
			mRoute = routes.get(i).match(reqPath);
			if(mRoute != null)
			{
				return mRoute;
			}
		}
		
		return null;
	}
	
	/**
	 * Normalize
	 */
	private synchronized void normalize()
	{
		if(hasNormalized)
		{
			return;
		}
		
		hasNormalized = true;
		
		/**
		 * 对初始化的路由列表进行按照是否是静态路由排序，以保证所有的静态路由在前面，动态路由在后面。
		 * <br>这样处理主要是解决静态路由和动态路由定义歧义时优先匹配静态路由。
		 * <br>比如：<code>get("/user/new")</code> 和  <code>get("/user/:userid")</code>，
		 * 当访问 <code>/user/new/</code> 时，应该优先匹配 <code>/user/new</code>；
		 * 当访问 <code>/user/1001</code> 时，则和 <code>/user/new</code> 不匹配，而和后面的 <code>/user/:userid</code> 匹配。
		 */
		for(Map.Entry<String, List<Route>> en : this.routes_map.entrySet())
		{
			Collections.sort(en.getValue(), new Comparator<Route>() {
				public int compare(Route route1, Route route2)
				{
					// 使用 regExpFlag进行升序排序，flag = 1 | 0 （1-动态路由，0-静态路由）
					return route1.getFlag() - route2.getFlag();
				}
			});
		}
		
		// Sort Interceptors priority
		for(Map.Entry<String, List<ActionInterceptor>> interceptors : this.before_interceptors.entrySet())
		{
			Collections.sort(interceptors.getValue(), new Comparator<ActionInterceptor>() {
	            public int compare(ActionInterceptor ai1, ActionInterceptor ai2) {
	                Before before1 = ai1.getInterceptor(Before.class);
	                Before before2 = ai2.getInterceptor(Before.class);
	                return before1.priority() - before2.priority();
	            }
	        });
		}
		
		for(Map.Entry<String, List<ActionInterceptor>> interceptors : this.after_interceptors.entrySet())
		{
			Collections.sort(interceptors.getValue(), new Comparator<ActionInterceptor>() {
	            public int compare(ActionInterceptor ai1, ActionInterceptor ai2) {
	            	After after1 = ai1.getInterceptor(After.class);
	            	After after2 = ai2.getInterceptor(After.class);
	                return after1.priority() - after2.priority();
	            }
	        });
		}
		
		for(Map.Entry<String, List<ActionInterceptor>> interceptors : this.finally_interceptors.entrySet())
		{
			Collections.sort(interceptors.getValue(), new Comparator<ActionInterceptor>() {
	            public int compare(ActionInterceptor ai1, ActionInterceptor ai2) {
	            	Finally finally1 = ai1.getInterceptor(Finally.class);
	            	Finally finally2 = ai2.getInterceptor(Finally.class);
	                return finally1.priority() - finally2.priority();
	            }
	        });
		}
		
		for(Map.Entry<String, List<ActionInterceptor>> interceptors : this.catch_interceptors.entrySet())
		{
			Collections.sort(interceptors.getValue(), new Comparator<ActionInterceptor>() {
	            public int compare(ActionInterceptor ai1, ActionInterceptor ai2) {
	            	Catch catch1 = ai1.getInterceptor(Catch.class);
	            	Catch catch2 = ai2.getInterceptor(Catch.class);
	                return catch1.priority() - catch2.priority();
	            }
	        });
		}
		
	}
	
	/**
	 * 清理路由器
	 */
	public void recycle()
	{
		this.routes_map.clear();
		this.routes_map = null;
		
		this.before_interceptors.clear();
		this.before_interceptors = null;
		
		this.after_interceptors.clear();
		this.after_interceptors = null;
		
		this.finally_interceptors.clear();
		this.finally_interceptors = null;
		
		this.catch_interceptors.clear();
		this.catch_interceptors = null;
	}
}
