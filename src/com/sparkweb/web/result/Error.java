package com.sparkweb.web.result;

import com.sparkweb.web.HttpStatus;
import com.sparkweb.web.Request;
import com.sparkweb.web.Response;

/**
 * 500 Error
 */
public class Error extends Result
{
	private static final long	serialVersionUID	= -2761418874385968359L;

	private int					status;

	public Error(String reason) {
		super(reason);
		this.status = HttpStatus.INTERNAL_ERROR;
	}

	public Error(int status, String reason) {
		super(reason);
		this.status = status;
	}

	public void apply(Request request, Response response)
	{
		response.status(status);
		
		StringBuilder errBuilder = new StringBuilder(500);
		errBuilder.append("<!DOCTYPE html><html><head><meta charset=\"utf-8\"><meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge,chrome=1\"><title>")
					.append("Occur an Error - ").append(status)
					.append("</title></head><body><div style=\"padding:30px; font-size: 16px; color: #cc0000; text-align: center;\">")
					.append(getMessage())
					.append("</div></body></html>");
		
		try
		{
			response.contentType("text/html; charset=" + getEncoding());
			response.out().write(errBuilder.toString().getBytes(getEncoding()));
			response.out().flush();
			
		} catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
