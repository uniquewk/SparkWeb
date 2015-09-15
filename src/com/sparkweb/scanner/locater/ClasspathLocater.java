package com.sparkweb.scanner.locater;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;

/**
 * 当前jvm运行环境classpath下的所有类定位器
 * 
 * @author yswang
 * @version 1.0
 */
public class ClasspathLocater implements ClassLocater
{
	public ClasspathLocater() {
	}

	public Set<URL> getClassLocations()
	{
		Set<URL> locations = new HashSet<URL>(200);
		
		ClassLoader loader = this.getClass().getClassLoader();
		ClassLoader stopLoader = ClassLoader.getSystemClassLoader().getParent();
		
		while(loader != null && loader != stopLoader)
		{
			if(loader instanceof URLClassLoader)
			{
				URL[] urls = ((URLClassLoader) loader).getURLs();
				for(int i = 0, len = urls.length; i < len; ++i)
				{
					locations.add(urls[i]);
				}
			}
			
			loader = loader.getParent();
		}
		
		return locations;
	}
	
	/*
	public Set<URL> getClassLocations2()
	{
		String classpath = System.getProperty("java.class.path");
		try
		{
			Method method = this.getClass().getClassLoader().getClass().getMethod("getClassPath", (Class<?>) null);
			if(method != null)
			{
				classpath = (String) method.invoke(this.getClass().getClassLoader(), (Object) null);
			}
		} catch(Throwable e)
		{
			// ignore
		}

		if(classpath == null)
		{
			classpath = System.getProperty("java.class.path");
		}

		String pathSeparator = System.getProperty("path.separator");
		if(pathSeparator == null)
		{
			pathSeparator = java.io.File.pathSeparator;
		}

		String[] classpaths = classpath.split(pathSeparator);

		Set<URL> locations = new HashSet<URL>(200);

		if(classpaths != null && classpaths.length > 0)
		{
			for(String path : classpaths)
			{
				try
				{
					locations.add(URI.create("file:///" + java.net.URLEncoder.encode(path, "UTF-8")).toURL());
				} catch(Throwable e)
				{
					// ignore
				}
			}
		}

		return locations;
	}*/
	
}
