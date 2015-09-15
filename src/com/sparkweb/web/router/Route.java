package com.sparkweb.web.router;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sparkweb.web.ActionInvoker;
import com.sparkweb.web.HttpMethod;

/**
 * 路由对象 <br>
 * 将定义的请求路径解析转换为路由对象 <br>
 * 例如：/question/:id --> 解析后变为： { path: "/question/:id", method: "get",
 * callback:routeCallbak, keys:[{name:"id", optional:true|false}]
 * regExp:"/^\\/question\\/(?:([^\\/]+?))\\/?$/i" }
 * 
 * @author yswang
 * @version 1.0
 */
class Route
{
	private HttpMethod			method			= HttpMethod.GET;

	private String				path			= null;

	// 路由解析匹配正则
	private Pattern				routePattern	= null;

	// 是否是动态路由，1--表示动态路由， 0--表示静态路由
	private int					routeFlag		= 0;
	
	private boolean				needCsrfCheck	= false;
	
	private ActionInvoker		actionInvoker	= null;
	
	private Class<?> 			controller		= null;

	// 存储路由中的命名参数变量
	private List<NamedKey>		keys			= new ArrayList<NamedKey>(10);


	protected Route(HttpMethod method, String path) {
		this(method, path, false, false);
	}

	protected Route(HttpMethod method, String path, boolean sensitive, boolean strict) {
		this.method = method;
		this.path = path;
		this.routePattern = PathParser.parseRoutePathRegexp(path, this.keys, sensitive, strict);

		// 通过判断正则路径中是否含有 (?:或/( 来判断是否是动态路由: 1--动态路由， 0-静态路由
		this.routeFlag = (this.routePattern.pattern().indexOf("(?:") != -1 
							|| this.routePattern.pattern().indexOf("/(") != -1) ? 1 : 0;
	}

	protected String getPath()
	{
		return this.path;
	}

	protected HttpMethod getMethod()
	{
		return this.method;
	}

	protected int getFlag()
	{
		return this.routeFlag;
	}
	
	protected void setNeedCsrfCheck(boolean need)
	{
		this.needCsrfCheck = need;
	}
	
	protected boolean isNeedCsrfCheck()
	{
		return this.needCsrfCheck;
	}
	
	protected ActionInvoker getActionInvoker()
	{
		return actionInvoker;
	}

	protected void setActionInvoker(ActionInvoker _actionInvoker)
	{
		this.actionInvoker = _actionInvoker;
	}

	protected Class<?> getController()
	{
		return controller;
	}

	protected void setController(Class<?> controller)
	{
		this.controller = controller;
	}

	/**
	 * 对真实访问的路径进行路由匹配，并获取存储路径中的相关变量参数值 <br>
	 * 比如：/question/2019202，将会匹配到定义的 /question/:id 路由，并获得 id = 2019202
	 * 
	 * @param reqUri 具体的访问路径
	 * @return 返回匹配的路由
	 */
	protected MatchedRoute match(String uri)
	{
		if(uri == null)
		{
			return null;
		}

		String url = uri.replaceAll("\\/+", "\\/");
		Matcher m = this.routePattern.matcher(url);
		if(!m.matches())
		{
			return null;
		}

		MatchedRoute mRoute = new MatchedRoute(this);

		int groupCount = m.groupCount();
		int namedKeySize = this.keys.size();

		String val = null;
		NamedKey key = null;

		for(int i = 1; i <= groupCount; ++i)
		{
			val = m.group(i);

			// 获取命名参数
			key = namedKeySize >= i ? this.keys.get(i - 1) : null;
			// 存在命名参数，说明path中当前的变量参数是通过命名参数[:name]定义的，比如：/question/:id
			if(key != null)
			{
				mRoute.addNamedParam(key.getName(), val);
			}
			// 不存在命名参数，说明path中的参数是通过正则定义的，比如：/question/(\\d+)
			else
			{
				mRoute.addSplatParam(val);
			}
		}

		return mRoute;
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append('{');
		sb.append("\"path\":\"").append(this.path).append('\"');
		sb.append(", \"method\":\"").append(this.method.name()).append('\"');
		sb.append(", \"namedkeys\":[");
		for(int i = 0, size = this.keys.size(); i < size; i++)
		{
			if(i > 0)
			{
				sb.append(',');
			}
			sb.append(this.keys.get(i).toString());
		}
		sb.append(']');
		sb.append(", \"pattern\":\"").append(this.routePattern.pattern()).append('\"');
		sb.append(", \"routeflag\":").append(routeFlag);
		sb.append(", \"csrf\":").append(needCsrfCheck);
		sb.append(", \"action\":\"").append(actionInvoker.toString()).append("\"");
		sb.append('}');

		return sb.toString();
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((method == null) ? 0 : method.hashCode());
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if(obj == null)
		{
			return false;
		}

		if(obj == this)
		{
			return true;
		}

		if(!(obj instanceof Route))
		{
			return false;
		}
		
		Route other = (Route) obj;
		if(method == null)
		{
			if(other.method != null) return false;
		}
		else if(!method.name().equalsIgnoreCase(other.method.name()))
		{
			return false;
		}
		
		if(path == null)
		{
			if(other.path != null) return false;
		}
		else if(!path.equalsIgnoreCase(other.path))
		{
			return false;
		}
		
		return true;
	}

}
