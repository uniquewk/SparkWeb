package com.sparkweb.validation;

import java.lang.annotation.ElementType;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 所有验证规则(包括自定义d的验证规则)必须使用 @Constraint(validatedBy = {}) 
 * 约束定义该规则将通过哪些验证器进行验证。
 * <pre><code>
 * @Target({ElementType.FIELD})
 * @Retention(RetentionPolicy.RUNTIME)
 * @Documented
 * @Constraint(validatedBy = { EqualsToValidator.class })
 * public @interface EqualsTo 
 * {
 * 		public abstract String value();
 * 		public abstract String message() default EqualsToValidator.MESSAGE;
 *
 *		@Target({ FIELD })
 *		@Retention(RetentionPolicy.RUNTIME)
 *		@Documented
 *		public static @interface List 
 *		{
 *			public abstract AssertFalse[] value();
 *		}
 *	}
 * </code></pre>
 * 
 * @author yswang
 * @version 1.0
 */
@Target({ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Constraint
{
  public abstract Class<? extends ConstraintValidator<?,?,?>>[] validatedBy();
}
