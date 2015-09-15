package com.sparkweb.web.router;

import com.sparkweb.exception.SparkException;

/**
 * 路由异常
 * @author yswang
 * @version 1.0
 */
public class RouteException extends SparkException
{
	private static final long	serialVersionUID	= 1L;

	public RouteException() {
	}

	public RouteException(String message) {
		super(message);
	}

	public RouteException(Throwable cause) {
		super(cause);
	}

	public RouteException(String message, Throwable cause) {
		super(message, cause);
	}

}
