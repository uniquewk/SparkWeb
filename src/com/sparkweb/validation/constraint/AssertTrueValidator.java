package com.sparkweb.validation.constraint;

import java.lang.reflect.Field;

import com.sparkweb.validation.ConstraintValidator;

/**
 * 验证值为是否Boolean.TRUE
 * @author yswang
 * @version 1.0
 */
public class AssertTrueValidator implements ConstraintValidator<Object, Boolean, AssertTrue>
{
	static final String MESSAGE = "validation.AssertTrue.message";
	
	public boolean isValid(Object validatedObject, Boolean valueToValidate
			, AssertTrue constraintRule, Field field)
	{
		return (valueToValidate == null) || (valueToValidate.booleanValue());
	}

}
