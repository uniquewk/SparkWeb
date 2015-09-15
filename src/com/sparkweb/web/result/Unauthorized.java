package com.sparkweb.web.result;

import com.sparkweb.web.HttpStatus;
import com.sparkweb.web.Request;
import com.sparkweb.web.Response;

/**
 * 401 Unauthorized
 */
public class Unauthorized extends Result
{
	private static final long	serialVersionUID	= 7230216088578683395L;
	
	private String	realm = "";

	public Unauthorized(String realm) {
		super(realm);
		
		if(realm != null)
		{
			this.realm = realm;
		}
	}

	public void apply(Request request, Response response)
	{
		response.status(HttpStatus.UNAUTHORIZED);
		// TODO WWW-Authenticate realm中文乱码
		response.header("WWW-Authenticate", "Basic realm=\"" + realm + "\"");
	}
}
