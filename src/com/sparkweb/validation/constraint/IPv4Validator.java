package com.sparkweb.validation.constraint;

import java.lang.reflect.Field;
import java.util.regex.Pattern;

import com.sparkweb.validation.ConstraintValidator;

/**
 * IPv4地址验证器，用于验证一个给定字符串是否是合法的IPv4地址
 * @author yswang
 * @version 1.0
 */
public class IPv4Validator implements ConstraintValidator<Object, CharSequence, IPv4>
{
	static final String MESSAGE = "validation.IPv4.message";
	
	private static Pattern IP_PATTERN = Pattern.compile("^((2[0-4]\\d|25[0-5]|[01]?\\d\\d?)\\.){3}(2[0-4]\\d|25[0-5]|[01]?\\d\\d?)$", Pattern.CASE_INSENSITIVE);
	
	public boolean isValid(Object validatedObject, CharSequence valueToValidate
			, IPv4 constraintRule, Field field)
	{
		if((valueToValidate == null) || (valueToValidate.toString().trim().length() == 0))
		{
			return true;
		}
		
		return IP_PATTERN.matcher(valueToValidate.toString().trim()).matches();
	}

}
