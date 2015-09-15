package com.sparkweb.validation.constraint;

import java.lang.reflect.Field;
import java.util.Calendar;

import com.sparkweb.validation.ConstraintValidator;

/**
 * 验证 Calendar 对象是否在当前时间之后
 * 
 * @deprecated
 * @author yswang
 * @version 1.0
 */
public class FutureValidatorForCalendar implements ConstraintValidator<Object, Calendar, Future>
{
	static final String MESSAGE = "validation.Future.message";
	
	public boolean isValid(Object validatedObject, Calendar valueToValidate
			, Future constraintRule, Field field)
	{
		if(valueToValidate == null) 
		{
	      return true;
	    }
		
	    return valueToValidate.after(Calendar.getInstance());
	}

}
