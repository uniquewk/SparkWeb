package com.sparkweb.validation.constraint;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.sparkweb.validation.ConstraintValidator;

/**
 * 字符串日期格式验证器，用于验证一个给定字符串是否符合给定日期格式
 * 
 * @author yswang
 * @version 1.0
 */
public class DateFormatValidator implements ConstraintValidator<Object, CharSequence, DateFormat>
{
	static final String MESSAGE = "validation.DateFormat.message";
	
	public boolean isValid(Object validatedObject, CharSequence valueToValidate
			, DateFormat constraintRule, Field field)
	{
		if(valueToValidate == null || valueToValidate.toString().trim().length() == 0)
		{
			return true;
		}
		
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(constraintRule.format());
			Date d = sdf.parse(valueToValidate.toString());
			return d != null && d.getTime() >= 0;
		} catch (Exception ex) {
			return false;
		}
	}

}
