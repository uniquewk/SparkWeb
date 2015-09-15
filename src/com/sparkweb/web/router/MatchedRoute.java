package com.sparkweb.web.router;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 路由匹配命中的路由，包含了参数值
 * 
 * @author yswang
 * @version 1.0
 */
public final class MatchedRoute
{
	private Route						route			= null;

	// 路径中匹配到的命名参数，/users/:uid
	private Map<String, List<String>>	pathNamedParams	= null;
	// 路径中匹配到的纯正则参数，/users/(\\d+)
	private List<String>				pathSplatParams	= null;

	protected MatchedRoute(Route route) {
		this.route = route;
		this.pathNamedParams = new HashMap<String, List<String>>();
		this.pathSplatParams = new ArrayList<String>();
	}

	protected Route getRoute()
	{
		return this.route;
	}
	
	protected synchronized void addNamedParam(String key, String value)
	{
		if(key == null || value == null)
		{
			return;
		}

		if(this.pathNamedParams.get(key.toLowerCase()) == null)
		{
			this.pathNamedParams.put(key.toLowerCase(), new ArrayList<String>(4));
		}

		this.pathNamedParams.get(key.toLowerCase()).add(value);
	}

	protected synchronized void addSplatParam(String value)
	{
		if(value == null)
		{
			return;
		}

		this.pathSplatParams.add(value);
	}

	public String[] getPathNamedParams(String name)
	{
		List<String> vals = pathNamedParams.get(name.toLowerCase());
		return vals != null ? vals.toArray(new String[vals.size()]) : new String[0];
	}
	
	public Map<String, String[]> getPathNamedParams()
	{
		Map<String, String[]> namedParams = new HashMap<String, String[]>(pathNamedParams.size());
		for(Map.Entry<String, List<String>> param : pathNamedParams.entrySet())
		{
			namedParams.put(param.getKey(), param.getValue().toArray(new String[param.getValue().size()]));
		}
		
		return namedParams;
	}
	
	public String getPathSplatParam(int index)
	{
		if(index < 0 || index >= pathSplatParams.size())
		{
			throw new IndexOutOfBoundsException(String.format("Index: %d, Path splat params size: %d", index, pathSplatParams.size()));
		}
		
		return pathSplatParams.get(index);
	}

	public String[] getPathSplatParams()
	{
		return pathSplatParams.toArray(new String[pathSplatParams.size()]);
	}
}
