package com.sparkweb.validation.constraint;

import java.lang.reflect.Field;
import java.util.regex.PatternSyntaxException;

import com.sparkweb.validation.ConstraintValidator;
import com.sparkweb.validation.ValidationException;

/**
 * 自定义正则表达式验证器，用于验证给定值是否匹配定义的正则表达式
 * 
 * @author yswang
 * @version 1.0
 */
public class PatternValidator implements ConstraintValidator<Object, CharSequence, Pattern>
{
	static final String MESSAGE = "validation.Pattern.message";
	
	public boolean isValid(Object validatedObject, CharSequence valueToValidate
			, Pattern constraintRule, Field field)
	{
		if(valueToValidate == null)
		{
			return true;
		}
		
		Pattern.Flag[] flags = constraintRule.flags();
		int intFlag = 0;
		for(Pattern.Flag flag : flags)
		{
			intFlag |= flag.getValue();
		}

		java.util.regex.Pattern pattern = null;
		try
		{
			pattern = java.util.regex.Pattern.compile(constraintRule.regexp(), intFlag);
			return pattern.matcher(valueToValidate.toString().trim()).matches();
		} catch(PatternSyntaxException e)
		{
			throw new ValidationException(e);
		}
	}

}
