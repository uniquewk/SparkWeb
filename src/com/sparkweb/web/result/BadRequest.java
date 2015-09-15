package com.sparkweb.web.result;

import java.io.IOException;

import com.sparkweb.web.HttpStatus;
import com.sparkweb.web.Request;
import com.sparkweb.web.Response;

/**
 * 400 Bad Request
 */
public class BadRequest extends Result
{
	private static final long	serialVersionUID	= -5153732267294216340L;

	public BadRequest() {
		super();
	}
	
	public BadRequest(String why) {
		super(why);
	}
	
	@Override
	public void apply(Request request, Response response)
	{
		try
		{
			response.httpServletResponse().sendError(HttpStatus.BAD_REQUEST, getMessage());
		} catch(IOException e)
		{
			e.printStackTrace();
		}
	}

}
