package com.sparkweb.web;

import java.lang.reflect.Method;

import com.sparkweb.reflect.MethodAccess;

/**
 * Action动作执行器
 * 
 * @author yswang
 * @version 1.0
 */
public class ActionInvoker
{
	protected MethodAccess	methodAccessor;
	protected int			methodIndex;
	protected String		action;
	protected Method		method;
	protected String[]		methodParamNames = new String[0];

	public ActionInvoker(MethodAccess _accessor, int _methodIndex) {
		this.methodAccessor = _accessor;
		this.methodIndex = _methodIndex;
		
		this.method = this.methodAccessor.getMethods()[this.methodIndex];
		this.action = this.methodAccessor.getDeclaringClass().getName() + '.' + method.getName();
		this.methodParamNames = this.methodAccessor.getParameterNames()[this.methodIndex];
	}
	
	public String getAction()
	{
		return this.action;
	}

	public Method getMethod()
	{
		return this.method;
	}
	
	public String[] getMethodParameterNames()
	{
		return this.methodParamNames;
	}

	public void invoke(Object... args)
	{
		this.methodAccessor.invoke(null, methodIndex, args);
	}

	@Override
	public String toString()
	{
		StringBuilder actionBuilder = new StringBuilder(200);
		actionBuilder.append(getAction());
		actionBuilder.append('(');
		
		int i = 0;
		for(Class<?> argType : this.method.getParameterTypes())
		{
			if(i > 0)
			{
				actionBuilder.append(", ");
			}
			
			actionBuilder.append(argType.getSimpleName()).append(' ').append(this.methodParamNames[i]);
			i++;
		}
		
		actionBuilder.append(')');
		
		return actionBuilder.toString();
	}
}
