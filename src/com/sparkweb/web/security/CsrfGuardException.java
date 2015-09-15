package com.sparkweb.web.security;

import com.sparkweb.exception.SparkException;

/**
 * @author yswang
 * @version 1.0
 */
public class CsrfGuardException extends SparkException
{
	private static final long	serialVersionUID	= -1023553417281256739L;

	public CsrfGuardException() {
		super();
	}

	public CsrfGuardException(String message, Throwable cause) {
		super(message, cause);
	}

	public CsrfGuardException(String message) {
		super(message);
	}

	public CsrfGuardException(Throwable cause) {
		super(cause);
	}
	
}
