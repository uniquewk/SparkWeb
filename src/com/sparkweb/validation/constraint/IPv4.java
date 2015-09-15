package com.sparkweb.validation.constraint;

import static java.lang.annotation.ElementType.FIELD;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.sparkweb.validation.Constraint;

/**
 * IPv4地址验证注解器
 * @author yswang
 * @version 1.0
 */
@Target({ FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = { IPv4Validator.class })
public @interface IPv4 
{
	public abstract String message() default IPv4Validator.MESSAGE;
	
	@Target({ FIELD })
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	public static @interface List 
	{
		public abstract IPv4[] value();
	}
}
