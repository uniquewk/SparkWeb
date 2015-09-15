package com.sparkweb.validation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;

import com.sparkweb.validation.constraint.Required;

/**
 * Field验证器上下文
 * 
 * @author yswang
 * @version 1.0
 */
public class FieldContext
{
	private Field field;
	private Method fieldMethod;
	private Annotation[] constraints = new Annotation[0];
	private Annotation requiredConstraint = null;
	
	protected FieldContext(Field field, Collection<Annotation> _constraints)
	{
		this(field, _constraints != null 
						? _constraints.toArray(new Annotation[_constraints.size()]) 
						: new Annotation[0]);
	}
	
	protected FieldContext(Field field, Annotation[] _constraints)
	{
		this.field = field;
		this.fieldMethod = getFieldMethod(this.field);
		
		if(_constraints != null)
		{
			this.constraints = _constraints;
		}
		
		for(Annotation anno : this.constraints)
		{
			if(anno.annotationType() == Required.class)
			{
				this.requiredConstraint = anno;
				break;
			}
		}
	}

	protected Field getField()
	{
		return this.field;
	}

	protected Annotation[] getConstraints()
	{
		return this.constraints;
	}
	
	protected Annotation getRequiredConstraint()
	{
		return this.requiredConstraint;
	}

	protected Method getFieldMethod()
	{
		return fieldMethod;
	}
	
	
	private static Method getFieldMethod(Field field)
	{
		if(field == null)
		{
			return null;
		}
		
		try
		{
			java.beans.PropertyDescriptor propDescr = new java.beans.PropertyDescriptor(field.getName(), field.getDeclaringClass());
			
			return propDescr.getReadMethod();
			
		} catch(java.beans.IntrospectionException e)
		{
			throw new ValidationException(e);
		}
		
		/*
		String fieldName = field.getName();
		String fieldMethodName = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
		
		Method method = null;
		if(field.getType() != boolean.class)
		{
			try
			{
				method = field.getDeclaringClass().getDeclaredMethod("get"+ fieldMethodName);
			} 
			catch(NoSuchMethodException e)
			{
				try
				{
					method = field.getDeclaringClass().getDeclaredMethod(fieldName);
				} 
				catch(NoSuchMethodException e2)
				{
				}
			}
		}
		else
		{
			try
			{
				method = field.getDeclaringClass().getDeclaredMethod("is"+ fieldMethodName);
			} 
			catch(NoSuchMethodException e)
			{
				try
				{
					method = field.getDeclaringClass().getDeclaredMethod("get" + fieldName);
				} 
				catch(NoSuchMethodException e2)
				{
				}
			}
		}
		
		return method != null && Modifier.isPublic(method.getModifiers()) ? method : null;
		*/
		
	}
}
