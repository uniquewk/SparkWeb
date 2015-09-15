package com.sparkweb.validation.constraint;

import java.lang.reflect.Field;
import java.util.Date;

import com.sparkweb.validation.ConstraintValidator;

/**
 * 验证 Date 对象是否在当前时间之前
 * 
 * @deprecated
 * @author yswang
 * @version 1.0
 */
public class PastValidatorForDate implements ConstraintValidator<Object, Date, Past>
{
	static final String MESSAGE = "validation.Past.message";
	
	public boolean isValid(Object validatedObject, Date valueToValidate
			, Past constraintRule, Field field)
	{
		if(valueToValidate == null) 
		{
	      return true;
	    }
		
	    return valueToValidate.before(new Date());
	}

}
