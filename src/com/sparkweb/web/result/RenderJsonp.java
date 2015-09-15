package com.sparkweb.web.result;

import com.sparkweb.exception.UnexpectedException;
import com.sparkweb.web.Request;
import com.sparkweb.web.Response;
import com.sparkweb.web.SparkConfig;

/**
 * jsonp
 */
public class RenderJsonp extends Result
{
	private static final long	serialVersionUID	= 8813549235036655676L;

	private static final String	JSONP_CALLBACK = "callback";
	private String				json = "{}";

	public RenderJsonp(Object o) {
		if(SparkConfig.getConfig().jsonResolver() == null)
		{
			throw new NullPointerException("The JSONResolver must not be null, if you use `Response.jsonp(Object)` to response current requesting!");
		}
		
		if(o != null)
		{
			this.json = SparkConfig.getConfig().jsonResolver().toJSONString(o);
		}
	}
	
	public RenderJsonp(CharSequence jsonString) {
		if(jsonString != null)
		{
			this.json = jsonString.toString();
		}
	}

	@Override
	public void apply(final Request request, final Response response)
	{
		String callback_name = SparkConfig.getConfig().webSettings().jsonpCallbackName();
		if(callback_name == null)
		{
			callback_name = JSONP_CALLBACK;
		}
		
		String callback = request.param(callback_name);
		
		if(callback == null || callback.trim().length() == 0)
		{
			throw new UnexpectedException("The jsonp callback function is not find!");
		}
		
		try
		{
			response.contentType("application/javascript");
			
			StringBuilder sb = new StringBuilder();
			sb.append(callback).append('(').append(this.json).append(')');
			
			response.print(sb.toString());
		} catch(Exception e)
		{
			e.printStackTrace();
			//throw new UnexpectedException(e);
		}
	}
}
