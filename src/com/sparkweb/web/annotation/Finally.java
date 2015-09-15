package com.sparkweb.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 拦截器， @Path 注解的方法被执行之后(不论执行是否出现异常)，都执行存在的 @Finally 定义的方法。
 * <br> 效果类似：try{ }finally{ }
 * 
 * @author yswang
 * @version 1.0
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Finally 
{
	/**
	 * 排除哪些方法不进行拦截
	 * @return
	 */
	String[] unless() default {};
	
	/**
	 * 指定只对哪些方法进行拦截
	 * @return
	 */
	String[] only() default {};
	
	/**
	 * 优先级，存在多个 @Finally 声明的方法时，按照优先级依次执行。
	 * <br> 数值越小，级别越高
	 * @return
	 */
	int priority() default 0;
}
