package com.sparkweb.web.result;

import java.util.Map;
import java.util.Map.Entry;

import com.sparkweb.web.HttpStatus;
import com.sparkweb.web.Request;
import com.sparkweb.web.Response;

/**
 * 302 Redirect
 */
public class Redirect extends Result
{
	private static final long	serialVersionUID	= 806481540661468491L;

	private String				url;
	private int					code				= HttpStatus.FOUND;

	public Redirect(String url) {
		this.url = url;
	}

	/**
	 * Redirects to a given URL with the parameters specified in a {@link Map}
	 * 
	 * @param url The URL to redirect to as a {@link String}
	 * @param parameters Parameters to be included at the end of the URL as a
	 *            HTTP GET. This is a map whose entries are written out as
	 *            key1=value1&key2=value2 etc..
	 */
	public Redirect(String url, Map<String, String[]> parameters) {
		
		StringBuffer urlSb = new StringBuffer(url);
		
		if(parameters != null && parameters.size() > 0)
		{
			char cp = '?';
			for(Entry<String, String[]> param : parameters.entrySet())
			{
				String[] vals = param.getValue();
				if(vals != null)
				{
					for(String val : vals)
					{
						if(val == null) continue;
						
						urlSb.append(cp).append(param.getKey()).append('=').append(val);
						cp = '&';
					}
				}
			}
		}

		this.url = urlSb.toString();
	}

	public Redirect(String url, boolean permanent) {
		this.url = url;
		
		if(permanent)
		{
			this.code = HttpStatus.MOVED_PERMANENTLY;
		}
	}

	public Redirect(String url, int code) {
		this.url = url;
		this.code = code;
	}

	@Override
	public void apply(Request request, Response response)
	{
		try
		{
			// do not touch any valid uri:
			// http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.30
			if(this.url.matches("^\\w+://.*"))
			{
				// ignore
			}
			/*else if(this.url.charAt(0) == '/')
			{
				this.url = String.format("http%s://%s%s%s", 
								request.secure() ? "s" : "", 
								request.domain(),
								(request.port() == 80 || request.port() == 443) ? "" : (":" + request.port()), 
										this.url
							);
			}*/
			else
			{
				if(this.url.charAt(0) == '/')
				{
					this.url = this.url.substring(1);
				}
				
				this.url = String.format("http%s://%s%s%s%s", 
								request.secure() ? "s" : "", 
								request.domain(),
								(request.port() == 80 || request.port() == 443) ? "" : (":" + request.port()), 
								request.contextPath(),
								request.contextPath().endsWith("/") ? this.url : "/" + this.url
							);
			}
			
			response.status(this.code);
			response.header("Location", this.url);
			
		} catch(Exception e)
		{
			e.printStackTrace();
			//throw new UnexpectedException(e);
		}
	}
}
