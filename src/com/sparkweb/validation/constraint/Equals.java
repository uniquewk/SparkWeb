package com.sparkweb.validation.constraint;

import static java.lang.annotation.ElementType.FIELD;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.sparkweb.validation.Constraint;

/**
 * 字符串相等验证注解器
 * 
 * @author yswang
 * @version 1.0
 */
@Target({ FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = { EqualsValidator.class })
public @interface Equals 
{
	public abstract String value();
	
	public abstract boolean caseSensitive() default true;
	
	public abstract String message() default EqualsValidator.MESSAGE;

	@Target({ FIELD })
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	public static @interface List 
	{
		public abstract Equals[] value();
	}
}
