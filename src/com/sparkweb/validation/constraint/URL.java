package com.sparkweb.validation.constraint;

import static java.lang.annotation.ElementType.FIELD;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.sparkweb.validation.Constraint;

/**
 * URL地址格式验证注解器
 * @author yswang
 * @version 1.0
 */
@Target({ FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = { URLValidator.class })
public @interface URL 
{
	public abstract String message() default URLValidator.MESSAGE;
	
	@Target({ FIELD })
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	public static @interface List 
	{
		public abstract URL[] value();
	}
}
