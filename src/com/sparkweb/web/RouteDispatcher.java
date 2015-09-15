package com.sparkweb.web;

import java.io.IOException;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sparkweb.reflect.ConstructorAccess;
import com.sparkweb.scanner.ClassScanner;
import com.sparkweb.scanner.criteria.AnnotationCriteria;
import com.sparkweb.scanner.criteria.ClassCriteria;
import com.sparkweb.util.Matcher;
import com.sparkweb.web.annotation.SparkwebSetting;
import com.sparkweb.web.result.NoResult;
import com.sparkweb.web.result.Result;
import com.sparkweb.web.router.RouteHandler;

/**
 * -- 最重要的核心类 -- 路由拦截分发器
 * 
 * @author yswang
 * @version 1.0
 */
//@WebFilter(filterName="SparkwebDispatcher", urlPatterns="/*", dispatcherTypes={DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.ERROR})
public final class RouteDispatcher implements Filter
{
	private static final Log	log				= LogFactory.getLog(RouteDispatcher.class);

	private ServletContext		servletContext	= null;
	private WebSettings			webSetting		= new DefaultWebSettings();
	private RouteHandler		routeHandler	= null;
	
	public void init(FilterConfig filterConfig) throws ServletException
	{
		servletContext = filterConfig.getServletContext();
		SparkConfig.getConfig().setServletContext(servletContext);
		
		log.info("-------------- Initializing WebSpark Route Dispatcher --------------");

		// Load customize sparkweb setting
		ClassScanner scanner = new ClassScanner();
		// Scan @SparkwebSetting annotated setting Class
		ClassCriteria webSettingCriteria = new AnnotationCriteria(SparkwebSetting.class);
		scanner.addClassCriteria(webSettingCriteria);
		Set<Class<?>> settingClazz = scanner.scanWebapp(servletContext);
		if(settingClazz != null && !settingClazz.isEmpty())
		{
			for(Class<?> clazz : settingClazz)
			{
				if(clazz != null && WebSettings.class.isAssignableFrom(clazz))
				{
					webSetting = (WebSettings) ConstructorAccess.get(clazz).newInstance();
					break;
				}
			}
		}
		
		SparkConfig.getConfig().setWebSettings(webSetting);
		
		long stime = System.currentTimeMillis();

		routeHandler = new RouteHandler(webSetting.caseSensitiveRouting(), webSetting.strictRouting());
		// Scan all routes that defined and load them on webapp startup
		routeHandler.loadRoutes(servletContext, webSetting.jarsToSkipWhenScanningRoutes());

		log.info(String.format("------WebSpark parse routes cost: %d ms.", System.currentTimeMillis() - stime));
		
		// Initialize the cache system.
		// TODO Cache supporting...
		/*if(SparkConfig.getConfig().cacheProvider() != null)
		{
			CacheManager.init();
		}*/
	}

	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException,
			ServletException
	{
		if(req instanceof HttpServletRequest && res instanceof HttpServletResponse)
		{
			HttpServletRequest httpRequest = (HttpServletRequest) req;
			
			String reqPath = httpRequest.getServletPath();
			if(httpRequest.getPathInfo() != null)
			{
				reqPath = reqPath + httpRequest.getPathInfo();
			}
			
			// static resources
			String[] staticAssetsPath = webSetting.staticAssetsPath();
			if(staticAssetsPath != null && staticAssetsPath.length > 0)
			{
				for(String staticPath : staticAssetsPath)
				{
					if(Matcher.match(staticPath, reqPath, false))
					{
						chain.doFilter(req, res);
						return;
					}
				}
			}
			
			HttpContext httpContext = HttpContext.init(httpRequest, (HttpServletResponse) res, webSetting.encoding());
	
			try
			{
				// route dispathcing...
				boolean dispatched = routeHandler.dispatch();
				if(!dispatched)
				{
					webSetting.rescueRouting(httpContext.request(), httpContext.response());
					
					chain.doFilter(req, res);
				}
			}
			// may throw `Result` exception from `rescueRouting(Request, Response)`
			catch(Result result)
			{
				if(!(result instanceof NoResult))
				{
					result.apply(httpContext.request(), httpContext.response());
				}
			}
			// uncatched exception by any `@Catch`
			catch(Throwable e)
			{
				try
				{
					webSetting.exception(e, httpContext.request(), httpContext.response());
				} 
				catch(Result result)
				{
					if(!(result instanceof NoResult))
					{
						result.apply(httpContext.request(), httpContext.response());
					}
				} 
				catch(Throwable ex) 
				{
					throw new RuntimeException(ex);
				}
			}
			finally
			{
				// destroy the `ThreadLocal<HttpContext>` after the current request ended
				if(httpContext != null)
				{
					httpContext.destroy();
				}
			}
		} 
		else 
		{
			chain.doFilter(req, res);
		}
	}
	
	
	public void destroy()
	{
		this.servletContext = null;
		this.webSetting = null;
		this.routeHandler = null;
		
	}

}
