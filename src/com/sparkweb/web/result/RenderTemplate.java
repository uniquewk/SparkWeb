package com.sparkweb.web.result;

import java.util.Map;

import com.sparkweb.web.Request;
import com.sparkweb.web.Response;

/**
 * @author yswang
 * @version 1.0
 */
public class RenderTemplate extends Result
{
	private static final long	serialVersionUID	= 8181400755327300516L;

	private String				content = "";
	
	public RenderTemplate(String template, Map<String, Object> templateData)
	{
		// TODO
	}

	@Override
	public void apply(Request request, Response response)
	{
		try
		{
			response.contentType("text/html; charset=" + getEncoding());
			response.out().write(this.content.getBytes(getEncoding()));
		} catch(Exception e)
		{
			e.printStackTrace();
			//throw new UnexpectedException(e);
		}
	}

}
