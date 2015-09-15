package com.sparkweb.validation.constraint;

import java.lang.reflect.Field;

import com.sparkweb.validation.ConstraintValidator;
import com.sparkweb.validation.ValidationException;

/**
 * 字符串长度验证器，用于验证一个给定字符串的长度是否符合要求
 * 
 * @author yswang
 * @version 1.0
 */
public class LengthValidator implements ConstraintValidator<Object, CharSequence, Length>
{
	static final String MESSAGE = "validation.Length.message";
	
	public boolean isValid(Object validatedObject, CharSequence valueToValidate
			, Length constraintRule, Field field)
	{
		if(valueToValidate == null)
		{
			return true;
		}
		
		int min = constraintRule.min();
		int max = constraintRule.max();
		
		/*if(min < 0)
		{
			throw new ValidationException("@Length.min value must be not less than 0!");
		}
		
		if(max < 0)
		{
			throw new ValidationException("@Length.max value must be not less than 0!");
		}*/
		
		if(max < min)
		{
			throw new ValidationException("@Length.max value must be not less than @Length.min value!");
		}
		
		int length = valueToValidate.toString().length();
		return (length >= min) && (length <= max);
	}

}
