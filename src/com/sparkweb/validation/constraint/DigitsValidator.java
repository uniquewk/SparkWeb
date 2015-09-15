package com.sparkweb.validation.constraint;

import java.lang.reflect.Field;
import java.math.BigDecimal;

import com.sparkweb.validation.ConstraintValidator;
import com.sparkweb.validation.ValidationException;

/**
 * 验证给定的 CharSequence 的构成是否符合 @Digits 的定义
 * 
 * @author yswang
 * @version 1.0
 */
public class DigitsValidator implements ConstraintValidator<Object, Object, Digits>
{
	static final String	MESSAGE	= "validation.Digits.message";

	public boolean isValid(Object validatedObject, Object valueToValidate, Digits constraintRule, Field validatedField)
	{
		if(valueToValidate == null)
		{
			return true;
		}

		int maxIntegerLength = constraintRule.integer();
		int maxFractionLength = constraintRule.fraction();

		if(maxIntegerLength < 0)
		{
			throw new ValidationException("The @Digits(integer=) value must be >= 0!");
		}

		if(maxFractionLength < 0)
		{
			throw new ValidationException("The @Digits(fraction=) value must be >= 0!");
		}

		BigDecimal bigNum = null;
		
		if(validatedField.getType().isPrimitive())
		{
			// int, long ,float, double等基本数据类型
			bigNum = getBigDecimalValue(String.valueOf(valueToValidate));
		}
		else if(Number.class.isAssignableFrom(validatedField.getType()))
		{
			if((valueToValidate instanceof BigDecimal))
			{
				bigNum = (BigDecimal) valueToValidate;
			}
			else
			{
				bigNum = new BigDecimal(valueToValidate.toString()).stripTrailingZeros();
			}
		}
		else if(CharSequence.class.isAssignableFrom(validatedField.getType()))
		{
			bigNum = getBigDecimalValue(valueToValidate.toString());
		}
		
		if(bigNum == null)
		{
			return false;
		}

		int integerPartLength = bigNum.precision() - bigNum.scale();
		int fractionPartLength = bigNum.scale() < 0 ? 0 : bigNum.scale();

		return (maxIntegerLength >= integerPartLength) && (maxFractionLength >= fractionPartLength);
	}

	private BigDecimal getBigDecimalValue(String val)
	{
		BigDecimal bignum;
		try
		{
			bignum = new BigDecimal(val);
		} catch(NumberFormatException nfe)
		{
			return null;
		}
		return bignum;
	}

}
