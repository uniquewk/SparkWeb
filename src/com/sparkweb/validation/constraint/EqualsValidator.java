package com.sparkweb.validation.constraint;

import java.lang.reflect.Field;

import com.sparkweb.validation.ConstraintValidator;

/**
 * 字符串相等验证器
 * 
 * @author yswang
 * @version 1.0
 */
public class EqualsValidator implements ConstraintValidator<Object, CharSequence, Equals>
{
	static final String MESSAGE = "validation.Equals.message";
	
	public boolean isValid(Object validatedObject, CharSequence valueToValidate
			, Equals constraintRule, Field field)
	{
		if(valueToValidate == null)
		{
			return true;
		}
		
		// 区分大小写
		if(constraintRule.caseSensitive())
		{
			return valueToValidate.toString().equals(constraintRule.value());
		}
		else 
		{
			return valueToValidate.toString().equalsIgnoreCase(constraintRule.value());
		}
		
	}

}
