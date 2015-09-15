package com.sparkweb.exception;

/**
 * An unexpected exception
 */
public class UnexpectedException extends SparkException
{
	private static final long	serialVersionUID	= -8152097895184442546L;

	public UnexpectedException(String message) {
		super(message);
	}

	public UnexpectedException(Throwable exception) {
		super(exception);
	}

	public UnexpectedException(String message, Throwable cause) {
		super(message, cause);
	}
}
