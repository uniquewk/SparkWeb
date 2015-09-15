package com.sparkweb.validation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * 自定义验证器扩展，验证规则的最终执行者。
 * 
 * @author yswang
 * @version 1.0
 *
 * @param <T> 被验证的对象
 * @param <V> 待验证对象属性值
 * @param <R> 当前的验证规则
 */
public interface ConstraintValidator<T, V, R extends Annotation>
{
	/**
	 * 验证器需要实现的验证方法
	 * 
	 * @param validatedObject 被验证的实例对象
	 * @param valueToValidate 待验证的对象属性值
	 * @param constraintRule 当前对象属性上配置的验证规则
	 * @param validatedField 待验证的对象字段
	 * 
	 * @return 验证通过，返回true；否则，返回false
	 */
	public boolean isValid(T validatedObject, V valueToValidate, R constraintRule, Field validatedField);
	
}
