package com.sparkweb.validation.constraint;

import java.lang.reflect.Field;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.sparkweb.validation.ConstraintValidator;

/**
 * IPv6地址验证器，用于验证一个给定字符串是否是合法的IPv6地址
 * 
 * @author yswang
 * @version 1.0
 */
public class IPv6Validator implements ConstraintValidator<Object, CharSequence, IPv6>
{
	static final String MESSAGE = "validation.IPv6.message";
	
	public boolean isValid(Object validatedObject, CharSequence valueToValidate
			, IPv6 constraintRule, Field field)
	{
		if((valueToValidate == null) || (valueToValidate.toString().trim().length() == 0))
		{
			return true;
		}

		try
		{
			InetAddress addr = InetAddress.getByName(valueToValidate.toString().trim());
			return addr instanceof Inet6Address;
		} catch(UnknownHostException e)
		{
			return false;
		}
	}

}
