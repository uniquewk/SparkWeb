package com.sparkweb.validation.constraint;

import java.lang.reflect.Field;
import java.net.IDN;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sparkweb.validation.ConstraintValidator;

/**
 * Email验证器，用于验证一个给定字符串是否是合法的Email格式
 * 
 * @author yswang
 * @version 1.0
 */
public class EmailValidator implements ConstraintValidator<Object, CharSequence, Email>
{
	static final String MESSAGE = "validation.Email.message";
	
	private static String	ATOM			= "[a-z0-9!#$%&'*+/=?^_`{|}~-]";
	private static String	DOMAIN			= ATOM + "+(\\." + ATOM + "+)*";
	private static String	IP_DOMAIN		= "\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\]";

	private Pattern			localPattern	= Pattern.compile(ATOM + "+(\\." + ATOM + "+)*", 2);
	private Pattern			domainPattern	= Pattern.compile(DOMAIN + "|" + IP_DOMAIN, 2);

	public boolean isValid(Object validatedObject, CharSequence valueToValidate
			, Email constraintRule, Field field)
	{
		if((valueToValidate == null) || (valueToValidate.toString().trim().length() == 0))
		{
			return true;
		}

		String[] emailParts = valueToValidate.toString().split("@");
		if(emailParts.length != 2)
		{
			return false;
		}

		if((emailParts[0].endsWith(".")) || (emailParts[1].endsWith(".")))
		{
			return false;
		}

		if(!matchPart(emailParts[0], this.localPattern))
		{
			return false;
		}

		return matchPart(emailParts[1], this.domainPattern);

	}

	private boolean matchPart(String part, Pattern pattern)
	{
		try
		{
			part = IDN.toASCII(part);
		} catch(IllegalArgumentException e)
		{
			return false;
		}
		Matcher matcher = pattern.matcher(part);
		return matcher.matches();
	}
}
