package com.sparkweb.web;

import com.sparkweb.web.view.ViewResolver;

/**
 * Sparkweb对 WebSettings提供的默认缺省配置
 * 
 * @author yswang
 * @version 1.0
 */
public class DefaultWebSettings implements WebSettings
{
	final static String ENCODING = "utf-8";
	final static String JSONP_CALLBACK_NAME = "callback";
	
	public String encoding()
	{
		return ENCODING;
	}

	public boolean caseSensitiveRouting()
	{
		return false;
	}

	public boolean strictRouting()
	{
		return false;
	}
	
	public String[] jarsToSkipWhenScanningRoutes()
	{
		return new String[0];
	}
	
	public String[] staticAssetsPath()
	{
		return new String[0];
	}
	
	public JSONResolver jsonResolver()
	{
		return null;
	}

	public ViewResolver viewResolver()
	{
		return null;
	}
	
	public String jsonpCallbackName()
	{
		return JSONP_CALLBACK_NAME;
	}
	
	public void rescueRouting(final Request request, final Response response)
	{
		
	}

	public void exception(final Throwable e, final Request request, final Response response) throws Throwable
	{
		
	}

}
