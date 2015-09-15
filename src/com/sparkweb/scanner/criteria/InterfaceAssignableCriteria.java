package com.sparkweb.scanner.criteria;

import java.util.HashSet;
import java.util.Set;
import org.objectweb.asm.ClassReader;


/**
 * 匹配所有实现了特定接口的类
 * 
 * @author yswang
 * @version 1.0
 */
public class InterfaceAssignableCriteria implements ClassCriteria
{
	private String interfaceClass = null;
	private Set<Class<?>> classes = new HashSet<Class<?>>(100);
	
	public InterfaceAssignableCriteria(Class<?> interfaceClass)
	{
		if(interfaceClass == null || !interfaceClass.isInterface())
		{
			throw new IllegalArgumentException("The parameter(Class<?> interfaceClass) must be an interface!");
		}
		
		this.interfaceClass = interfaceClass.getCanonicalName().replace(".", "/");
	}
	
	/*public boolean fitsCriteria(byte[] classBytes, String classname)
	{
		try
	    {
	      ClassReader reader = new ClassReader(classBytes);
	      
	      String[] interfaces = reader.getInterfaces();
	      for(String _interface : interfaces)
	      {
	    	  if(this.interfaceClass.equals(_interface))
	    	  {
	    		  this.classes.add(Class.forName(reader.getClassName().replace("/", "."), false, Thread.currentThread().getContextClassLoader()));
	    		  return true;
	    	  }
	      }
	      
	    } catch (Exception e)
	    {
	      // ignore
	    }
		
		return false;
	}*/
	
	public boolean fitsCriteria(ClassReader classReader, String classname)
	{
		try
	    {
	      String[] interfaces = classReader.getInterfaces();
	      for(String _interface : interfaces)
	      {
	    	  if(this.interfaceClass.equals(_interface))
	    	  {
	    		  this.classes.add(Class.forName(classReader.getClassName().replace("/", "."), false, InterfaceAssignableCriteria.class.getClassLoader()));
	    		  return true;
	    	  }
	      }
	      
	    } catch (Exception e)
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
