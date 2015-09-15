package com.sparkweb.validation.constraint;

import static java.lang.annotation.ElementType.FIELD;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.sparkweb.validation.Constraint;

/**
 * 验证 Date 和 Calendar 对象是否在当前时间之后
 * 
 * @author yswang
 * @version 1.0
 */
@Target({ FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {FutureValidator.class})
public @interface Future 
{
	public abstract String message() default FutureValidator.MESSAGE;

	@Target({ FIELD })
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	public static @interface List 
	{
		public abstract Future[] value();
	}
}
