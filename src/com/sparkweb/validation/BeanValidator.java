package com.sparkweb.validation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sparkweb.i18n.ResourceBundleLocator;
import com.sparkweb.validation.constraint.Required;

/**
 * JavaBean验证器
 * 
 * @author yswang
 * @version 1.0
 */
public final class BeanValidator
{
	/**
	 * 验证JavaBean
	 * 
	 * @param bean 需要验证的JavaBean对象
	 * @param fields 需要可能指定验证的Bean字段
	 * @return 验证结果
	 */
	@SuppressWarnings("unchecked")
	public final static <T> ConstraintViolation<T>[] validate(T bean, String...fields)
	{
		if(bean == null)
		{
			throw new ValidationException("Validated JavaBean object can not be null!");
		}

		if(bean.getClass() == Object.class)
		{
			return new ConstraintViolation[0];
		}

		FieldContext[] fieldContexts = ValidationContextCache.getConstraintFieldContexts(bean.getClass());
		if(fieldContexts == null || fieldContexts.length == 0)
		{
			return new ConstraintViolation[0];
		}
		
		// 对指定的field进行提取
		FieldContext[] needFieldContexts = null;
		if(fields != null && fields.length > 0)
		{
			needFieldContexts = new FieldContext[fields.length];
			int i = 0;
			boolean fExist = false; // 检测指定的field是否存在于给定的Bean中
			
			for(String f : fields)
			{
				if(f == null || f.trim().length() == 0)
				{
					continue;
				}
				
				fExist = false;
				for(FieldContext fc : fieldContexts)
				{
					if(fc.getField().getName().equals(f))
					{
						needFieldContexts[i++] = fc;
						fExist = true;
						break;
					}
				}
				
				if(!fExist)
				{
					throw new ValidationException("The field `"+ f +"` is not exists in constraint fields of `"+ bean.getClass().getName() +"`!");
				}
			}
			
			// 指定的field都为null或空，则指定无效，验证全部
			if(i == 0)
			{
				needFieldContexts = fieldContexts;
			}
		} 
		else {
			needFieldContexts = fieldContexts;
		}

		List<ConstraintViolation<T>> violations = new ArrayList<ConstraintViolation<T>>(needFieldContexts.length);
		ConstraintViolation<T> fcv = null;
		
		for(FieldContext fc : needFieldContexts)
		{
			fcv = validateFieldContext(bean, fc);
			
			if(fcv != null)
			{
				violations.add(fcv);
			}
		}

		return violations.toArray(new ConstraintViolation[violations.size()]);
	}

	/**
	 * 验证字段值
	 * 
	 * @param bean
	 * @return
	 */
	private final static <T> ConstraintViolation<T> validateFieldContext(T bean, FieldContext fc)
	{
		if(fc == null)
		{
			return null;
		}
		
		ConstraintViolation<T> violation = null;

		Object fieldValue = getFieldValue(bean, fc.getFieldMethod());

		// 首先验证其是否是Required
		Annotation requiredConstraint = fc.getRequiredConstraint();
		if(requiredConstraint != null)
		{
			violation = validateField(bean, fc.getField(), fieldValue, requiredConstraint);
			if(violation != null)
			{
				return violation;
			}
		}

		// 验证其它规则
		for(Annotation otherConstraint : fc.getConstraints())
		{
			// 不再处理Required
			if(otherConstraint.annotationType() == Required.class)
			{
				continue;
			}

			violation = validateField(bean, fc.getField(), fieldValue, otherConstraint);
			if(violation != null)
			{
				return violation;
			}
		}

		return null;
	}

	private final static <T> ConstraintViolation<T> validateField(T bean, Field field, Object fieldValue, Annotation constraintAnno)
	{
		boolean isValid = true;
		// 获取该注解验证规则上的所有验证器，并依次进行调用验证
		ConstraintValidator[] validators = ValidationContextCache.getConstraintValidators(constraintAnno);
		if(validators == null || validators.length == 0)
		{
			return null;
		}
		
		for(ConstraintValidator validator : validators)
		{
			if(validator == null)
			{
				continue;
			}
			
			isValid &= validator.isValid(bean, fieldValue, constraintAnno, field);
			if(!isValid)
			{
				return new ConstraintViolation<T>(bean, field.getName(), fieldValue, getMessage(constraintAnno));
			}
		}

		return null;
	}

	private final static <T> Object getFieldValue(T bean, Method fieldMethod)
	{
		if(fieldMethod == null)
		{
			return null;
		}
		
		try
		{
			return fieldMethod.invoke(bean);
		} catch(Exception e)
		{
			return null;
		}
	}
	
	private final static String getMessage(Annotation constraintAnno)
	{
		String msg = invokeAnnotationMethod(constraintAnno, "message");
		if(msg == null || msg.length() == 0)
		{
			return "";
		}
		
		String _msg = ValidationResourceBundle.getBundleMessage(msg);
		if(_msg != null && _msg.trim().length() > 0)
		{
			msg = _msg;
		}
		
		String[] params = ResourceBundleLocator.parseStringParameters(msg);
		if(params == null || params.length == 0)
		{
			return msg;
		}
		
		Map<String, String> valueMap = new HashMap<String, String>(params.length);
		for(String param : params)
		{
			valueMap.put(param, invokeAnnotationMethod(constraintAnno, param));
		}
		
		return ResourceBundleLocator.formatString(msg, valueMap);
	}
	
	private final static String invokeAnnotationMethod(Annotation constraintAnno, String methodName)
	{
		Method method = null;
		try
		{
			method = constraintAnno.annotationType().getDeclaredMethod(methodName, (Class<?>[]) null);
			// 跳过java的安全检查，提升反射调用性能
			method.setAccessible(true);
			
			if(Modifier.isPublic(method.getModifiers()))
			{
				return method.invoke(constraintAnno, (Object[]) null).toString();
			}
			
		} catch(SecurityException e)
		{
			e.printStackTrace();
		} catch(NoSuchMethodException e)
		{
			e.printStackTrace();
		} catch(IllegalArgumentException e)
		{
			e.printStackTrace();
		} catch(IllegalAccessException e)
		{
			e.printStackTrace();
		} catch(InvocationTargetException e)
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	// 私有构造器，不允许外部new
	private BeanValidator() {
		throw new IllegalAccessError("New instance of BeanValidator is not allowed!");
	}
	
}
