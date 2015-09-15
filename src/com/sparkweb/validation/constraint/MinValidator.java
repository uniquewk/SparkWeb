package com.sparkweb.validation.constraint;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.sparkweb.validation.ConstraintValidator;

/**
 * 最小值验证器，用于验证一个给定数值是否满足最小值验证
 * 
 * @author yswang
 * @version 1.0
 */
public class MinValidator implements ConstraintValidator<Object, Number, Min>
{
	static final String MESSAGE = "validation.Min.message";
	
	public boolean isValid(Object validatedObject, Number valueToValidate
			, Min constraintRule, Field field)
	{
		if(valueToValidate == null)
		{
			return true;
		}
		
		long minValue = constraintRule.value();
		
		if((valueToValidate instanceof BigDecimal))
		{
			return ((BigDecimal) valueToValidate).compareTo(BigDecimal.valueOf(minValue)) != -1;
		}
		if((valueToValidate instanceof BigInteger))
		{
			return ((BigInteger) valueToValidate).compareTo(BigInteger.valueOf(minValue)) != -1;
		}

		long longValue = valueToValidate.longValue();
		
		return longValue >= minValue;
	}

}
