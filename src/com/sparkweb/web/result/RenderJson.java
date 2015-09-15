package com.sparkweb.web.result;

import com.sparkweb.web.Request;
import com.sparkweb.web.Response;
import com.sparkweb.web.SparkConfig;

/**
 * 200 OK with application/json
 */
public class RenderJson extends Result
{
	private static final long	serialVersionUID	= 8813549235036655676L;

	private String				json = "{}";

	public RenderJson(Object o)
	{
		if(SparkConfig.getConfig().jsonResolver() == null)
		{
			throw new NullPointerException("The JSONResolver must not be null, if you use `Response.json(Object)` to response current requesting!");
		}
		
		if(o != null)
		{
			this.json = SparkConfig.getConfig().jsonResolver().toJSONString(o);
		}
	}
	
	public RenderJson(CharSequence jsonString) {
		if(jsonString != null)
		{
			this.json = jsonString.toString();
		}
	}

	@Override
	public void apply(Request request, Response response)
	{
		try
		{
			response.contentType("application/json; charset=" + getEncoding());
			response.out().write(json.getBytes(getEncoding()));
		} catch(Exception e)
		{
			e.printStackTrace();
			//throw new UnexpectedException(e);
		}
	}
}
