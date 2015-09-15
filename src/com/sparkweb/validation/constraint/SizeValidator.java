package com.sparkweb.validation.constraint;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

import com.sparkweb.validation.ConstraintValidator;
import com.sparkweb.validation.ValidationException;

/**
 * 对象大小验证器
 * <p>如果被验证的值是字符串，则验证字符串的长度大小；</p>
 * <p>如果被验证的值是Collection类型，则验证字Collection的size()大小；</p>
 * <p>如果被验证的值是Map类型，则验证Map的size()大小；</p>
 * <p>如果被验证的值是数组类型，则验证数组的length大小；</p>
 * 
 * @author yswang
 * @version 1.0
 */
public class SizeValidator implements ConstraintValidator<Object, Object, Size>
{
	static final String MESSAGE = "validation.Size.message";
	
	public boolean isValid(Object validatedObject, Object valueToValidate
			, Size constraintRule, Field field)
	{
		if(valueToValidate == null)
		{
			return true;
		}
		
		long minValue = constraintRule.min();
		long maxValue = constraintRule.max();
		
		if(maxValue < minValue)
		{
			throw new ValidationException("@Size.max value must be not less than @Size.min value!");
		}
		
		int size = 0;
		if(valueToValidate instanceof CharSequence)
		{
			size = valueToValidate.toString().trim().length();
			return size >= minValue && size <= maxValue;
		}
		
		if(valueToValidate instanceof Collection<?>)
		{
			size = ((Collection<?>)valueToValidate).size();
			return size >= minValue && size <= maxValue;
		}
		
		if(valueToValidate instanceof Map<?, ?>)
		{
			size = ((Map<?, ?>)valueToValidate).size();
			return size >= minValue && size <= maxValue;
		}
		
		if(valueToValidate.getClass().isArray())
		{
			try
			{
				size = java.lang.reflect.Array.getLength(valueToValidate);
				return size >= minValue && size <= maxValue;
			} catch(Exception e)
			{
				throw new ValidationException(e);
			}
		}
		
		return false;
	}

}
