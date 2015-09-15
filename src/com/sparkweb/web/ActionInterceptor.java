package com.sparkweb.web;

import java.lang.annotation.Annotation;

import com.sparkweb.reflect.MethodAccess;

/**
 * 拦截器
 * 
 * @author yswang
 * @version 1.0
 */
public final class ActionInterceptor extends ActionInvoker
{
	public ActionInterceptor(MethodAccess _accessor, int _methodIndex) {
		super(_accessor, _methodIndex);
	}

	public <T extends Annotation> T getInterceptor(Class<T> interceptorClass)
	{
		return (methodAccessor.getMethods()[methodIndex]).getAnnotation(interceptorClass);
	}

}
