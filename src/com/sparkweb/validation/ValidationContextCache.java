package com.sparkweb.validation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * 验证器缓存容器，用于缓存进行验证的JavaBean验证规则信息和验证规则器；
 * 高效执行验证。
 * 
 * @author yswang
 * @verion 1.0
 */
public final class ValidationContextCache
{
	/**
	 * 默认缓存JavaBean验证规则大小
	 */
	private static final int BEAN_CACHE_LIMIT = 1000;
	
	/**
	 * 默认缓存验证注解
	 */
	private static final int CONSTRAINT_CACHE_LIMIT = 100;

	/**
	 * 采用LRU策略缓存JavaBean验证规则
	 */
	private static final LinkedHashMap<String, FieldContext[]> CACHE_VALIDATEBEAN = 
			new LinkedHashMap<String, FieldContext[]>(BEAN_CACHE_LIMIT, 0.75f, true) {
				private static final long serialVersionUID = 1L;
				@Override
				protected boolean removeEldestEntry(Map.Entry<String, FieldContext[]> eldest) {
					return size() > BEAN_CACHE_LIMIT;
				}
			};
	
	/**
	 * 采用LRU策略缓存每个验证注解对应的ConstraintValidator
	 */
	private static final LinkedHashMap<Class<? extends Annotation>, ConstraintValidator<?,?,?>[]> CACHE_VALIDATOR = 
			new LinkedHashMap<Class<? extends Annotation>, ConstraintValidator<?,?,?>[]>(CONSTRAINT_CACHE_LIMIT, 0.75f, true) {
				private static final long serialVersionUID = 1L;
				@Override
				protected boolean removeEldestEntry(Map.Entry<Class<? extends Annotation>, ConstraintValidator<?,?,?>[]> eldest) {
					return size() > CONSTRAINT_CACHE_LIMIT;
				}
			};		
	
	/**
	 * 获取JavaBean Class中定义的字段验证器
	 * @param beanClass
	 * @return
	 */
	protected static FieldContext[] getConstraintFieldContexts(Class<?> beanClass)
	{
		synchronized(CACHE_VALIDATEBEAN)
		{
			String beanName = beanClass.getCanonicalName();
			
			FieldContext[] fieldContexts = CACHE_VALIDATEBEAN.get(beanName);
			
			if(fieldContexts == null)
			{
				Field[] fields = beanClass.getDeclaredFields();
				if(fields.length == 0)
				{
					// 不是有效的JavaBean，不做缓存
					return new FieldContext[0];
				}
				
				// 存储JavaBean中所有的字段的验证环境
				Set<FieldContext> beanFieldContexts = new LinkedHashSet<FieldContext>();
				Annotation[] annos;
				Set<Annotation> fieldRuleAnnos;
				
				for(Field field : fields)
				{
					annos = field.getDeclaredAnnotations();
					if(annos == null || annos.length == 0)
					{
						continue;
					}
					
					// 存储每个Field上的验证注解
					fieldRuleAnnos = new LinkedHashSet<Annotation>();
					for(Annotation anno : annos)
					{
						// 所有的验证器注解必须有 @Constraint(validatedBy={})注解；
						// 否则，则不是验证器
						if(anno.annotationType().getAnnotation(Constraint.class) == null)
						{
							continue;
						}
						
						registerConstraintValidator(anno);
						fieldRuleAnnos.add(anno);
					}
					
					// 只保存含有验证器的Field
					if(fieldRuleAnnos.size() > 0)
					{
						beanFieldContexts.add(new FieldContext(field, fieldRuleAnnos));
					}
				}
				
				fieldContexts = beanFieldContexts.toArray(new FieldContext[beanFieldContexts.size()]);
				// 不含有任何验证器的JavaBean不缓存
				if(fieldContexts.length > 0)
				{
					CACHE_VALIDATEBEAN.put(beanName, fieldContexts);
				}
			}
			
			return fieldContexts;
		}
	}
	
	
	/**
	 * 获取验证注解上定义的验证器ConstraintValidator
	 * 
	 * @param constraintAnno
	 * @return
	 */
	protected static ConstraintValidator<?,?,?>[] getConstraintValidators(Annotation constraintAnno)
	{
		return CACHE_VALIDATOR.get(constraintAnno.annotationType());
	}
	
	
	private static void registerConstraintValidator(Annotation constraintAnno)
	{
		if(CACHE_VALIDATOR.containsKey(constraintAnno.annotationType()))
		{
			return;
		}
		
		Constraint constraint = constraintAnno.annotationType().getAnnotation(Constraint.class);
		Class<? extends ConstraintValidator<?,?,?>>[] validatedBys = constraint.validatedBy();
		// 对于像：@Feture, @Past, @Digits 等因为字段类型的不同，
		// 需要选择不同类型的验证器，不会默认存在一个验证器。
		if(validatedBys == null || validatedBys.length == 0)
		{
			return;
		}
		
		ConstraintValidator<?,?,?>[] validators = new ConstraintValidator<?,?,?>[validatedBys.length];
		
		try
		{
			for(int i = 0, len = validatedBys.length; i < len; ++i)
			{
				validators[i] = validatedBys[i].newInstance();
			}
			
			CACHE_VALIDATOR.put(constraintAnno.annotationType(), validators);
		} catch(Exception e)
		{
			throw new ValidationException(e);
		}
	}
	
}
