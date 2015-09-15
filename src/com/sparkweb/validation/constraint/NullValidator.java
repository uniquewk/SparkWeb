package com.sparkweb.validation.constraint;

import java.lang.reflect.Field;

import com.sparkweb.validation.ConstraintValidator;

/**
 * 验证一个给定值必须为Null
 * @author yswang
 * @version 1.0
 */
public class NullValidator implements ConstraintValidator<Object, Object, Null>
{
	static final String MESSAGE = "validation.Null.message";
	
	public boolean isValid(Object validatedObject, Object valueToValidate
			, Null constraintRule, Field field)
	{
		return valueToValidate == null;
	}

}
