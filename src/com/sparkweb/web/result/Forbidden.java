package com.sparkweb.web.result;

import java.io.IOException;

import com.sparkweb.web.HttpStatus;
import com.sparkweb.web.Request;
import com.sparkweb.web.Response;

/**
 * 403 Forbidden
 */
public class Forbidden extends Result
{
	private static final long	serialVersionUID	= 7224814444100054705L;

	public Forbidden() {
		super();
	}
	
	/**
	 * @param why a description of the forbidden
	 */
	public Forbidden(String reason) {
		super(reason);
	}
	
	@Override
	public void apply(Request request, Response response)
	{
		try
		{
			response.httpServletResponse().sendError(HttpStatus.FORBIDDEN, getMessage());
		} catch(IOException e)
		{
			e.printStackTrace();
		}
	}

}
