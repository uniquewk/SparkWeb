package com.sparkweb.validation.constraint;

import static java.lang.annotation.ElementType.FIELD;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.sparkweb.validation.Constraint;

/**
 * 自定义正则表达式验证注解器
 * 
 * @author yswang
 * @version 1.0
 */
@Target({ FIELD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = { PatternValidator.class })
public @interface Pattern {
	
	public abstract String regexp();

	public abstract Flag[] flags() default {Flag.CASE_INSENSITIVE};

	public abstract String message() default PatternValidator.MESSAGE;

	@Target({ FIELD })
	@Retention(RetentionPolicy.RUNTIME)
	@Documented
	public static @interface List {
		public abstract Pattern[] value();
	}

	public static enum Flag
	{
		UNIX_LINES(1),

		CASE_INSENSITIVE(2),

		COMMENTS(4),

		MULTILINE(8),

		DOTALL(32),

		UNICODE_CASE(64),

		CANON_EQ(128);

		private final int	value;

		private Flag(int value) {
			this.value = value;
		}

		public int getValue()
		{
			return this.value;
		}
	}
}
