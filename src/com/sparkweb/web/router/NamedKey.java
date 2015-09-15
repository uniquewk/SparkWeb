package com.sparkweb.web.router;

/**
 * 命名参数
 * @author yswang
 */
class NamedKey
{
	private String name;
	private boolean optional;
	
	NamedKey() {}
	
	NamedKey(String name, boolean optional)
	{
		this.name = name;
		this.optional = optional;
	}

	String getName()
	{
		return name;
	}

	boolean isOptional()
	{
		return optional;
	}

	@Override
	public String toString()
	{
		return "{\"name\":\""+ getName() +"\", \"optional\":"+ isOptional() +"}";
	}
	
}
