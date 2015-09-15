package com.sparkweb.exception;

/**
 * @author yswang
 * @version 1.0
 */
public class CacheException extends SparkException
{
	private static final long	serialVersionUID	= -3052108459536446379L;

	public CacheException() {
		super();
	}

	public CacheException(String desc) {
		super(desc);
	}

	public CacheException(String desc, Throwable cause) {
		super(desc, cause);
	}

	public CacheException(Throwable cause) {
		super(cause);
	}
}
