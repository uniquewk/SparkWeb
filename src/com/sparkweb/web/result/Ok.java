package com.sparkweb.web.result;

import com.sparkweb.web.HttpStatus;
import com.sparkweb.web.Request;
import com.sparkweb.web.Response;

/**
 * 200 OK
 */
public class Ok extends Result
{
	private static final long	serialVersionUID	= -1767874328766208679L;

	public Ok() {
		super("OK");
	}

	@Override
	public void apply(Request request, Response response)
	{
		response.status(HttpStatus.OK);
	}
}
