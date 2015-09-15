package com.sparkweb.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 用来标记 action的某个参数值需要从 url中的动态命名参数中获取
 * 
 * @author yswang
 * @version 1.0
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface PathParam 
{
	public abstract String value() default "";
}
