package com.sparkweb.web.result;

import com.sparkweb.web.Request;
import com.sparkweb.web.Response;

/**
 * 200 OK with a text/html
 */
public class RenderHtml extends Result
{
	private static final long	serialVersionUID	= 7541599543370494219L;
	
	private String html = "";

	public RenderHtml(CharSequence _html) {
		if(_html != null)
		{
			this.html = _html.toString();
		}
	}

	@Override
	public void apply(Request request, Response response)
	{
		try
		{
			response.contentType("text/html; charset=" + getEncoding());
			response.out().write(this.html.getBytes(getEncoding()));
		} catch(Exception e)
		{
			e.printStackTrace();
			//throw new UnexpectedException(e);
		}
	}

}
