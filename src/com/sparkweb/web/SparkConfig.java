package com.sparkweb.web;

import javax.servlet.ServletContext;

import com.sparkweb.web.view.ViewResolver;

/**
 * @author yswang
 * @version 1.0
 */
public final class SparkConfig
{
	private WebSettings webSettings;
	private JSONResolver jsonResolver;
	private ViewResolver viewResolver;
	private ServletContext servletContext;
	
	protected void setWebSettings(WebSettings settings)
	{
		this.webSettings = settings;
		this.jsonResolver = settings.jsonResolver();
		this.viewResolver = settings.viewResolver();
	}
	
	protected void setServletContext(ServletContext context)
	{
		this.servletContext = context;
	}
	
	public WebSettings webSettings()
	{
		return webSettings;
	}
	
	public JSONResolver jsonResolver()
	{
		if(this.jsonResolver == null)
		{
			throw new NullPointerException("The JSONResolver could not found, you must setting it!");
		}
		
		return jsonResolver;
	}
	
	public ViewResolver viewResolver()
	{
		if(this.viewResolver == null)
		{
			throw new NullPointerException("The ViewResolver could not found, you must setting it!");
		}
		
		return viewResolver;
	}

	public ServletContext servletContext()
	{
		return servletContext;
	}

	private SparkConfig() {
	}
	
	public static SparkConfig getConfig()
	{
		return SingletonHolder.INSTANCE;
	}
	
	private static class SingletonHolder
	{
		public static final SparkConfig INSTANCE = new SparkConfig();
	}
}
