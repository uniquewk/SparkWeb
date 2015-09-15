package com.sparkweb.scanner.criteria;

import java.util.HashSet;
import java.util.Set;
import org.objectweb.asm.ClassReader;

/**
 * 匹配所有继承了给定父类的所有子类
 * 
 * @author yswang
 * @version 1.0
 */
public class SuperClassCriteria implements ClassCriteria
{
	private String superClass = null;
	private Set<Class<?>> classes = new HashSet<Class<?>>(100);
	
	
	public SuperClassCriteria(Class<?> superClass)
	{
		if(superClass == null)
		{
			throw new IllegalArgumentException("The parameter(Class<?> superClass) must be a Class!");
		}
		
		this.superClass = superClass.getCanonicalName().replace(".", "/");
	}

	public boolean fitsCriteria(ClassReader classReader, String classname)
	{
		try
	    {
			String superName = classReader.getSuperName();
			if(this.superClass.equals(superName))
			{
				 this.classes.add(Class.forName(classReader.getClassName().replace("/", "."), false, SuperClassCriteria.class.getClassLoader()));
				 return true;
			}
	    } catch (Exception e)
	    {
	      // ignore
	    }
		
		return false;
	}

	public Set<Class<?>> getClasses()
	{
		return this.classes;
	}

}
