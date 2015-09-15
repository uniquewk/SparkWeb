package com.sparkweb.validation.constraint;

import static java.lang.annotation.ElementType.FIELD;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.sparkweb.validation.Constraint;

/**
 * 对CharSequence长度，Collection、Map、Array的size进行约束
 * @author yswang
 * @version 1.0
 */
@Target({ FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = {SizeValidator.class })
public @interface Size 
{
	public abstract String message() default SizeValidator.MESSAGE;
	
	public abstract long min() default Long.MIN_VALUE;
	
	public abstract long max() default Long.MAX_VALUE;
	
	@Target({ FIELD })
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	public static @interface List 
	{
		public abstract Size[] value();
	}
}
