package com.sparkweb.web;

/**
 * JSON数据结构处理器
 * 
 * @author yswang
 * @version 1.0
 */
public interface JSONResolver
{
	String toJSONString(Object object);
	
	<T> T toObject(Class<T> clazz, String jsonString);
}
