package com.sparkweb.validation.constraint;

import java.lang.reflect.Field;

import com.sparkweb.validation.ConstraintValidator;

/**
 * 验证一个给定值不能为Null
 * @author yswang
 * @version 1.0
 */
public class NotNullValidator implements ConstraintValidator<Object, Object, NotNull>
{
	static final String MESSAGE = "validation.NotNull.message";
	
	public boolean isValid(Object validatedObject, Object valueToValidate
			, NotNull constraintRule, Field field)
	{
		return valueToValidate != null;
	}

}
