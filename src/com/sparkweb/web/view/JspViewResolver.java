package com.sparkweb.web.view;

import com.sparkweb.web.Request;
import com.sparkweb.web.Response;

/**
 * JSP视图文件渲染器
 * 
 * @author yswang
 * @version 1.0
 */
public abstract class JspViewResolver implements ViewResolver
{
	private static final String VIEW_SUFFIX = ".jsp";
	
	private String viewDir;
	
	protected JspViewResolver()
	{
		this.viewDir = this.viewDirectory();
		
		if(this.viewDir == null)
		{
			throw new NullPointerException("The JspViewRender `viewDirectory` must not be null!");
		}
		
		if(this.viewDir.charAt(this.viewDir.length() - 1) != '/')
		{
			this.viewDir = this.viewDir + '/';
		}
	}
	
	public String viewSuffix()
	{
		return VIEW_SUFFIX;
	}

	public void render(String view, Request request, Response response)
	{
		if(view.charAt(0) == '/') 
		{
			view = view.substring(1);
		}
		
		if(!view.endsWith(viewSuffix()))
		{
			view = view + viewSuffix();
		}
		
		request.forward(this.viewDir + view);
	}

}
