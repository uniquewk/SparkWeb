package com.sparkweb.web.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.sparkweb.web.HttpMethod;

/**
 * 类级别和方法级别的HTTP请求URL定义注解
 * <br>注意：如果类上存在该注解定义，则注解中定义的url路径将自动作为该类下
 * 所有方法级别注解中定义的url相对路径(不以任何路径符开头的路径，比如url="index")的前缀。
 * 这样可以减少重复路径前缀的定义。
 * 
 * @author yswang
 * @version 1.0
 */
@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Path 
{
	/**
	 * 定义URL访问路径，路径支持命名参数变量，正则变量
	 * <br>
	 * 普通：/users
	 * 命名参数: /user/:userid
	 * 正则： /user/(\\d+)
	 * 使用正则约束命名参数变量：/user/:userid(\\d+)
	 */
	public abstract String[] value() default {};
	
	/**
	 * 定义请求方法类型，支持HTTP主流的 method：GET,POST, HEAD, PUT, DELETE.
	 * 默认 GET
	 */
	public abstract HttpMethod[] method() default {HttpMethod.GET};
	
}
