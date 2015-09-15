package com.sparkweb.validation.constraint;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.sparkweb.validation.ConstraintValidator;

/**
 * 最大值验证器，用于验证一个给定数值是否满足最大值验证
 * 
 * @author yswang
 * @version 1.0
 */
public class MaxValidator implements ConstraintValidator<Object, Number, Max>
{
	static final String MESSAGE = "validation.Max.message";
	
	public boolean isValid(Object validatedObject, Number valueToValidate
			, Max constraintRule, Field field)
	{
		if(valueToValidate == null)
		{
			return true;
		}
		
		long maxValue = constraintRule.value();
		
		if((valueToValidate instanceof BigDecimal))
		{
			return ((BigDecimal) valueToValidate).compareTo(BigDecimal.valueOf(maxValue)) != 1;
		}
		
		if((valueToValidate instanceof BigInteger))
		{
			return ((BigInteger) valueToValidate).compareTo(BigInteger.valueOf(maxValue)) != 1;
		}

		long longValue = valueToValidate.longValue();
		
		return longValue <= maxValue;
	}

}
