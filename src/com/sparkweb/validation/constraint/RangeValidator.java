package com.sparkweb.validation.constraint;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.sparkweb.validation.ConstraintValidator;
import com.sparkweb.validation.ValidationException;

/**
 * 数值范围验证器，用于验证一个给定数值是否满足设定的范围
 * @author yswang
 * @version 1.0
 */
public class RangeValidator implements ConstraintValidator<Object, Number, Range>
{
	static final String MESSAGE = "validation.Range.message";
	
	public boolean isValid(Object validatedObject, Number valueToValidate
			, Range constraintRule, Field field)
	{
		if(valueToValidate == null)
		{
			return true;
		}
		
		long minValue = constraintRule.min();
		long maxValue = constraintRule.max();
		
		if(maxValue < minValue)
		{
			throw new ValidationException("@Range.max value must be not less than @Range.min value!");
		}
		
		if((valueToValidate instanceof BigDecimal))
		{
			return ((BigDecimal) valueToValidate).compareTo(BigDecimal.valueOf(maxValue)) != 1
					&& ((BigDecimal) valueToValidate).compareTo(BigDecimal.valueOf(minValue)) != -1;
		}
		
		if((valueToValidate instanceof BigInteger))
		{
			return ((BigInteger) valueToValidate).compareTo(BigInteger.valueOf(maxValue)) != 1
					&& ((BigInteger) valueToValidate).compareTo(BigInteger.valueOf(minValue)) != -1;
		}

		long longValue = valueToValidate.longValue();
		
		return longValue <= maxValue && longValue >= minValue;
	}

}
