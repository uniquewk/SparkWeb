package com.sparkweb.validation;

/**
 * @author yswang
 * @version 1.0
 */
public class ValidationException extends RuntimeException
{
	private static final long	serialVersionUID	= 4132257386103313235L;

	public ValidationException(String message) {
		super(message);
	}

	public ValidationException() {
	}

	public ValidationException(String message, Throwable cause) {
		super(message, cause);
	}

	public ValidationException(Throwable cause) {
		super(cause);
	}
}
