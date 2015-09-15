package com.sparkweb.validation.constraint;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Date;

import com.sparkweb.validation.ConstraintValidator;
import com.sparkweb.validation.ValidationException;

/**
 * 验证 Date 或 Calendar 对象是否在当前时间之后
 * 
 * @author yswang
 * @version 1.0
 */
public class FutureValidator implements ConstraintValidator<Object, Object, Future>
{
	static final String MESSAGE = "validation.Future.message";
	
	public boolean isValid(Object validatedObject, Object valueToValidate
			, Future constraintRule, Field validatedField)
	{
		if(valueToValidate == null) 
		{
	      return true;
	    }
		
		if(Date.class.isAssignableFrom(validatedField.getType()))
		{
			return ((Date)valueToValidate).after(new Date());
		}
		
		if(Calendar.class.isAssignableFrom(validatedField.getType()))
		{
			return ((Calendar)valueToValidate).after(Calendar.getInstance());
		}
		
	    throw new ValidationException("The `@Future` constraint must be defined on type: `java.util.Date` or `java.util.Calendar`!");
	}
	
}
