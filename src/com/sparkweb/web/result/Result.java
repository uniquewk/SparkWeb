package com.sparkweb.web.result;

import com.sparkweb.exception.FastRuntimeException;
import com.sparkweb.web.HttpContext;
import com.sparkweb.web.Request;
import com.sparkweb.web.Response;

/**
 * Result support
 */
public abstract class Result extends FastRuntimeException
{
	private static final long	serialVersionUID	= -6812643417212458152L;

	public Result() {
		super();
	}

	public Result(String description) {
		super(description);
	}

	
	public abstract void apply(final Request request, final Response response);

	/**
	 * The encoding that should be used when writing this response to the client
	 */
	protected String getEncoding()
	{
		return HttpContext.current().response().encoding();
	}
	
}
