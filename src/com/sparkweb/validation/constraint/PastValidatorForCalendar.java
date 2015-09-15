package com.sparkweb.validation.constraint;

import java.lang.reflect.Field;
import java.util.Calendar;

import com.sparkweb.validation.ConstraintValidator;

/**
 * 验证 Calendar 对象是否在当前时间之前
 * 
 * @deprecated
 * @author yswang
 * @version 1.0
 */
public class PastValidatorForCalendar implements ConstraintValidator<Object, Calendar, Past>
{
	static final String MESSAGE = "validation.Past.message";
	
	public boolean isValid(Object validatedObject, Calendar valueToValidate
			, Past constraintRule, Field field)
	{
		if(valueToValidate == null) 
		{
	      return true;
	    }
		
	    return valueToValidate.before(Calendar.getInstance());
	}

}
