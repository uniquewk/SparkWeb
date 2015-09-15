package com.sparkweb.web.result;

import java.io.IOException;

import com.sparkweb.web.HttpStatus;
import com.sparkweb.web.Request;
import com.sparkweb.web.Response;

/**
 * 404 not found
 */
public class NotFound extends Result
{
	private static final long	serialVersionUID	= -4863797975148079048L;

	public NotFound() {
		super();
	}
	
	/**
	 * @param why a description of the problem
	 */
	public NotFound(String why) {
		super(why);
	}

	/**
	 * @param method routed method
	 * @param path routed path
	 */
	public NotFound(String method, String path) {
		super(method + " " + path);
	}

	@Override
	public void apply(Request request, Response response)
	{
		try
		{
			response.httpServletResponse().sendError(HttpStatus.NOT_FOUND, getMessage());
		} catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
}
