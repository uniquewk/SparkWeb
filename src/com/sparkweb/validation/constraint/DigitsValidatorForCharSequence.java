package com.sparkweb.validation.constraint;

import java.lang.reflect.Field;
import java.math.BigDecimal;

import com.sparkweb.validation.ConstraintValidator;
import com.sparkweb.validation.ValidationException;

/**
 * 验证给定的 CharSequence 的构成是否符合 @Digits 的定义
 * 
 * @deprecated
 * @author yswang
 * @version 1.0
 */
public class DigitsValidatorForCharSequence implements ConstraintValidator<Object, CharSequence, Digits>
{
	static final String	MESSAGE	= "validation.Digits.message";

	public boolean isValid(Object validatedObject, CharSequence valueToValidate
			, Digits constraintRule, Field field)
	{
		if(valueToValidate == null)
		{
			return true;
		}

		int maxIntegerLength = constraintRule.integer();
		int maxFractionLength = constraintRule.fraction();

		if(maxIntegerLength < 0)
		{
			throw new ValidationException("The @Digits(integer=) value must be greater than 0!");
		}

		if(maxFractionLength < 0)
		{
			throw new ValidationException("The @Digits(fraction=) value must be greater than 0!");
		}

		BigDecimal bigNum = getBigDecimalValue(valueToValidate);
		if(bigNum == null)
		{
			return false;
		}

		int integerPartLength = bigNum.precision() - bigNum.scale();
		int fractionPartLength = bigNum.scale() < 0 ? 0 : bigNum.scale();

		return (maxIntegerLength >= integerPartLength) && (maxFractionLength >= fractionPartLength);
	}

	private BigDecimal getBigDecimalValue(CharSequence charSequence)
	{
		BigDecimal bignum;
		try
		{
			bignum = new BigDecimal(charSequence.toString());
		} catch(NumberFormatException nfe)
		{
			return null;
		}
		return bignum;
	}

}
