package com.sparkweb.validation.constraint;

import java.lang.reflect.Field;
import java.math.BigDecimal;

import com.sparkweb.validation.ConstraintValidator;
import com.sparkweb.validation.ValidationException;

/**
 * 验证给定的 Number的构成是否符合 @Digits 的定义
 * 
 * @deprecated
 * @author yswang
 * @version 1.0
 */
public class DigitsValidatorForNumber implements ConstraintValidator<Object, Number, Digits>
{
	static final String MESSAGE = "validation.Digits.message";
	
	public boolean isValid(Object validatedObject, Number valueToValidate
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
		
	    BigDecimal bigNum;
	    if ((valueToValidate instanceof BigDecimal)) 
	    {
	      bigNum = (BigDecimal)valueToValidate;
	    }
	    else 
	    {
	      bigNum = new BigDecimal(valueToValidate.toString()).stripTrailingZeros();
	    }
	    
	    if(bigNum == null)
	    {
	    	return false;
	    }
	    
	    int integerPartLength = bigNum.precision() - bigNum.scale();
	    int fractionPartLength = bigNum.scale() < 0 ? 0 : bigNum.scale();

	    return (maxIntegerLength >= integerPartLength) && (maxFractionLength >= fractionPartLength);
	}

}
