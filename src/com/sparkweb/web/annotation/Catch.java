package com.sparkweb.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 异常拦截器， @Path 注解的方法执行发生异常情况下执行存在的 @Catch 方法。
 * <br> 效果类似：try{ }catch(Exception e){ }
 * <pre>
 * <code>
 * @Catch(IllegalArgumentException.class)
 * static void catchException(Throwable e) {}
 * </code>
 * </pre>
 * 
 * @author yswang
 * @version 1.0
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Catch 
{
	/**
	 * 定义拦截的异常类
	 * @return
	 */
	Class<? extends Throwable>[] value() default {};
	
	/**
	 * 优先级，存在多个 @Catch 声明的方法时，按照优先级依次执行。
	 * <br> 数值越小，级别越高
	 * @return
	 */
	int priority() default 0;
	
}
