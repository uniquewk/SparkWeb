package com.sparkweb.scanner.criteria;

import java.util.Set;

import org.objectweb.asm.ClassReader;

/**
 * 类匹配标准
 * 
 * @author yswang
 * @version 1.0
 */
public interface ClassCriteria
{
	/**
	 * 匹配类
	 * @deprecated 使用 fitsCriteria(ClassReader classReader, String classname) 替代
	 * @param classBytes
	 * @param classname
	 * @return
	 */
//	public boolean fitsCriteria(byte[] classBytes, String classname);
	
	/**
	 * 匹配类
	 * @param classReader
	 * @param classname
	 * @return
	 */
	public boolean fitsCriteria(ClassReader classReader, String classname);
	
	/**
	 * 获取匹配的所有类
	 * @return
	 */
	public Set<Class<?>> getClasses();
	
}
