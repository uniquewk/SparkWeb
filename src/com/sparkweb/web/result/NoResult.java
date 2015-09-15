package com.sparkweb.web.result;

import com.sparkweb.web.Request;
import com.sparkweb.web.Response;

public class NoResult extends Result
{
	private static final long	serialVersionUID	= 5079359141102570779L;

	@Override
	public void apply(Request request, Response response)
	{
		// ignore
	}
}
