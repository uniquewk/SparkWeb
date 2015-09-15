package com.sparkweb.validation;

import com.sparkweb.util.EscapeUtils;

/**
 * 验证结果信息
 * 
 * @author yswang
 * @version 1.0
 *
 * @param <T>
 */
public class ConstraintViolation<T>
{
	private T rootBean;
	private String fieldName;
	private Object invalidValue;
	private String message;
	
	public ConstraintViolation(final T bean, final String fieldName, final Object invalidValue, final String message)
	{
		this.rootBean = bean;
		this.fieldName = fieldName;
		this.invalidValue = invalidValue;
		this.message = message;
	}

	public T getRootBean()
	{
		return rootBean;
	}

	public String getFieldName()
	{
		return fieldName;
	}

	public String getMessage()
	{
		return message;
	}

	public Object getInvalidValue()
	{
		return invalidValue;
	}
	
	@Override
	public String toString()
	{
		return String.format("{\"%s\":\"%s\"}", getFieldName(), EscapeUtils.escapeJSON(getMessage()));
	}
}
