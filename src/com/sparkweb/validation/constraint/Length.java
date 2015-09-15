package com.sparkweb.validation.constraint;

import static java.lang.annotation.ElementType.FIELD;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.sparkweb.validation.Constraint;

/**
 * 字符串长度验证注解器
 * 
 * @author yswang
 * @version 1.0
 */
@Target({ FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = { LengthValidator.class })
public @interface Length 
{
	public abstract String message() default LengthValidator.MESSAGE;
	
	public abstract int min() default Integer.MIN_VALUE;
	
	public abstract int max() default Integer.MAX_VALUE;
	
	// 字符占据的宽度，西欧字符占1个字节，UTF-8字符占3个字节
	public abstract int charWidth() default 1;
	

	@Target({ FIELD })
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	public static @interface List 
	{
		public abstract Length[] value();
	}
}
