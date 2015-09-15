package com.sparkweb.exception;

/**
 * @author yswang
 * @version 1.0
 */
public class SparkException extends RuntimeException
{
	private static final long	serialVersionUID	= 971598966138434361L;

	public SparkException() {
		super();
	}

	public SparkException(String desc) {
		super(desc);
	}
	
	public SparkException(Throwable cause) {
		super(cause);
	}

	public SparkException(String desc, Throwable cause) {
		super(desc, cause);
	}
	
}
