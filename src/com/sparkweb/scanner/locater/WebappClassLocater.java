package com.sparkweb.scanner.locater;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 扫描当前web应用下 WEB-INF/classes 和 WEB-INF/lib 下的所有类
 * 
 * @author yswang
 * @version 1.0
 */
public class WebappClassLocater implements ClassLocater
{
	private final static Log	log				= LogFactory.getLog(WebappClassLocater.class);

	private final static String	WEBAPP_CLASSES	= "/WEB-INF/classes";
	private final static String	WEBAPP_LIB		= "/WEB-INF/lib";

	private ServletContext		servletContext	= null;

	public WebappClassLocater(ServletContext servletContext) {
		this.servletContext = servletContext;

		if(this.servletContext == null)
		{
			throw new IllegalArgumentException("The ServletContext paramter can not be null!");
		}
	}

	public Set<URL> getClassLocations()
	{
		// 这里采用这种方式获取 WEBAPP的class URL，而不是采用 getClassLocations2() 中的方式，
		// 是为了解决 ClassScanner#217 第 217 行提到的 WEB-INF/classes 第一层目录问题
		
		Set<URL> classURLs = new HashSet<URL>(200);
		
		try
		{
			classURLs.add(URI.create("file:///"+ URLEncoder.encode(this.servletContext.getRealPath(WEBAPP_CLASSES), "UTF-8")).toURL());
		} catch(Throwable e)
		{
			log.warn(String.format("Could not add /WEB-INF/classes to scan locations! Caused by: %s", e.getMessage()));
		}
		
		/*try
		{
			classURLs.add(URI.create("file:///" + URLEncoder.encode(this.servletContext.getRealPath(WEBAPP_LIB), "UTF-8")).toURL());
		} catch(Throwable e)
		{
			log.warn("Could not add /WEB-INF/lib to class locations.", e);
		}*/

		// Scan WEB-INF/lib/*.jar
		Set<String> jarList = this.servletContext.getResourcePaths(WEBAPP_LIB);
		Iterator<String> it = jarList.iterator();
		while(it.hasNext())
		{
			String path = it.next();
			URL url = null;
			try
			{
				// File URLs are always faster to work with so use them
				// if available.
				
				// 这种先使用realPath.toURL的处理方式比下面的 方式2 速度快
				String realPath = this.servletContext.getRealPath(path);
				if(realPath != null)
				{
					url = (new File(realPath)).toURI().toURL();
				} 
				else {
					url = this.servletContext.getResource(path);
				}
				
				/* 方式2
				url = this.servletContext.getResource(path);
				if(url == null)
				{
					String realPath = this.servletContext.getRealPath(path);
					if(realPath != null)
					{
						url = (new File(realPath)).toURI().toURL();
					}
				}*/

				if(url != null)
				{
					classURLs.add(url);
				}

			} catch(Throwable e)
			{
				log.warn(String.format("Failed to scan JAR [%s] in WEB-INF/lib/! Caused by: %s", path, e.getMessage()));
			}
		}
		
		return classURLs;
	}
	
	/*
	public Set<URL> getClassLocations2()
	{
		// Scan WEB-INF/classes
		Set<String> classList = servletContext.getResourcePaths(WEBAPP_CLASSES);
		// Scan WEB-INF/lib
		Set<String> jarList = servletContext.getResourcePaths(WEBAPP_LIB);

		if((classList == null || classList.size() == 0) && (jarList == null || jarList.size() == 0))
		{
			return Collections.emptySet();
		}

		Set<URL> classURLs = new HashSet<URL>((classList != null ? classList.size() : 0)
				+ (jarList != null ? jarList.size() : 0));

		Set<String> dirList = new HashSet<String>();
		
		if(classList != null)
		{
			dirList.addAll(classList);
		}
		
		if(jarList != null)
		{
			dirList.addAll(jarList);
		}

		Iterator<String> it = dirList.iterator();
		
		while(it.hasNext())
		{
			String path = it.next();
			URL url = null;
			try
			{
				// File URLs are always faster to work with so use them
				// if available.
				String realPath = servletContext.getRealPath(path);
				
				if(realPath == null)
				{
					url = servletContext.getResource(path);
				}
				else
				{
					url = (new File(realPath)).toURI().toURL();
				}

				classURLs.add(url);

			} catch(Throwable e)
			{
				log.warn(String.format("Failed to scan JAR [%s] from WEB-INF/lib", url), e);
			}
		}

		return classURLs;
	}*/
}
