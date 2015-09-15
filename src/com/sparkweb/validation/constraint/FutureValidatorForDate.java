package com.sparkweb.validation.constraint;

import java.lang.reflect.Field;
import java.util.Date;

import com.sparkweb.validation.ConstraintValidator;

/**
 * 验证 Date 对象是否在当前时间之后
 * 
 * @deprecated
 * @author yswang
 * @version 1.0
 */
public class FutureValidatorForDate implements ConstraintValidator<Object, Date, Future>
{
	static final String MESSAGE = "validation.Future.message";
	
	public boolean isValid(Object validatedObject, Date valueToValidate
			, Future constraintRule, Field field)
	{
		if(valueToValidate == null) 
		{
	      return true;
	    }
		
	    return valueToValidate.after(new Date());
	}

}
