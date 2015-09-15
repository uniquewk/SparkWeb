package com.sparkweb.web.result;

import com.sparkweb.web.HttpStatus;
import com.sparkweb.web.Request;
import com.sparkweb.web.Response;

/**
 * 302 Redirect
 */
public class RedirectToStatic extends Result
{
	private static final long	serialVersionUID	= -4545706971730474393L;
	
	private String	file;

	public RedirectToStatic(String file) {
		this.file = file;
	}

	@Override
	public void apply(Request request, Response response)
	{
		try
		{
			response.status(HttpStatus.FOUND);
			response.header("Location", file);
		} catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
