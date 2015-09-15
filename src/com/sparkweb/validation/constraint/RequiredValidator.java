package com.sparkweb.validation.constraint;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

import com.sparkweb.validation.ConstraintValidator;
import com.sparkweb.validation.ValidationException;

/**
 * 不能为空，对CharSequence长度，Collection、Map、Array的size进行约束
 * 
 * @author yswang
 * @version 1.0
 */
public class RequiredValidator implements ConstraintValidator<Object, Object, Required>
{
	static final String MESSAGE = "validation.Required.message";
	
	public boolean isValid(Object validatedObject, Object valueToValidate
			, Required constraintRule, Field field)
	{
		if(valueToValidate == null)
		{
			return false;
		}
		
		if(valueToValidate instanceof CharSequence)
		{
			return valueToValidate.toString().trim().length() > 0;
		}
		
		if(valueToValidate instanceof Collection<?>)
		{
			return ((Collection<?>)valueToValidate).size() > 0;
		}
		
		if(valueToValidate instanceof Map<?, ?>)
		{
			return ((Map<?, ?>)valueToValidate).size() > 0;
		}
		
		if(valueToValidate.getClass().isArray())
		{
			try
			{
				return java.lang.reflect.Array.getLength(valueToValidate) > 0;
			} catch(Exception e)
			{
				throw new ValidationException(e);
			}
		}
		
		return false;
	}

}
