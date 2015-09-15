package com.sparkweb.scanner.criteria;

import java.util.HashSet;
import java.util.Set;
import org.objectweb.asm.ClassReader;

import com.sparkweb.util.AnnotationReader;

/**
 * 匹配所有使用了特定类级别注解(java.lang.annotation.ElementType.TYPE)的类
 * 
 * @author yswang
 * @version 1.0
 */
public class AnnotationCriteria implements ClassCriteria
{
	private String annotation = null;
	
	private Set<Class<?>> classes = new HashSet<Class<?>>(100);
	
	public AnnotationCriteria(Class<? extends java.lang.annotation.Annotation> annotationClass)
	{
		if(annotationClass == null || !annotationClass.isAnnotation())
		{
			throw new IllegalArgumentException("The parameter(Class<?> annotationClass) must be an Annotation!");
		}
		
//		this.annotation = ("L" + annotationClass.getCanonicalName().replace(".", "/") + ";");
		this.annotation = annotationClass.getCanonicalName();
	}
	
	public boolean fitsCriteria(ClassReader classReader, String classname)
	{
		try
	    {
			// 读取类上的所有的注解
			AnnotationReader annoReader = new AnnotationReader(classReader);
			
			if(annoReader.getAnnotations().contains(this.annotation))
			{
				this.classes.add(Class.forName(classReader.getClassName().replace("/", "."), false, AnnotationCriteria.class.getClassLoader()));
	        	return true;
			}
			
			annoReader.clear();
	    }
	    catch (Exception e)
	    {
	      // ignore
	    }
		
		return false;
	}
	
	/**
	 * 获取匹配的所有类
	 * @return
	 */
	public Set<Class<?>> getClasses()
	{
		return classes;
	}
	
}
