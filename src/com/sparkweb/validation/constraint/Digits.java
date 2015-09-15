package com.sparkweb.validation.constraint;

import static java.lang.annotation.ElementType.FIELD;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.sparkweb.validation.Constraint;

/**
 * 验证给定的 Number 和 String的构成是否合法
 * 
 * @author yswang
 * @version 1.0
 */
@Target({ FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {DigitsValidator.class})
public @interface Digits 
{
	public abstract String message() default DigitsValidator.MESSAGE;
	
	// 数值长度
	public abstract int integer();
	// 小数长度
	public abstract int fraction();
	
	@Target({ FIELD })
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	public static @interface List 
	{
		public abstract Digits[] value();
	}
}
