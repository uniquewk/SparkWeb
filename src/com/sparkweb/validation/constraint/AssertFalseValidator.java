package com.sparkweb.validation.constraint;

import java.lang.reflect.Field;

import com.sparkweb.validation.ConstraintValidator;

/**
 * 验证值为是否Boolean.FALSE
 * @author yswang
 * @version 1.0
 */
public class AssertFalseValidator implements ConstraintValidator<Object, Boolean, AssertFalse>
{
	static final String MESSAGE = "validation.AssertFalse.message";
	
	public boolean isValid(Object validatedObject, Boolean valueToValidate
			, AssertFalse constraintRule, Field field)
	{
		return (valueToValidate == null) || !(valueToValidate.booleanValue());
	}

}
