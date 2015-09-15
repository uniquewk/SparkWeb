package com.sparkweb.web;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.sparkweb.binding.Binder;
import com.sparkweb.exception.SparkException;
import com.sparkweb.exception.UnexpectedException;
import com.sparkweb.web.multipart.MultipartHttpRequest;
import com.sparkweb.web.multipart.UploadedFile;
import com.sparkweb.web.router.MatchedRoute;

/**
 * HTTP 请求
 * 
 * @author yswang
 * @version 1.0
 */
public final class Request
{
	private HttpServletRequest		servletRequest	= null;
	private MatchedRoute			matchedRoute	= null;
	private Map<String, String[]>	queryParamMap	= new HashMap<String, String[]>(0);

	// private Map<String, Object> flashParams = new HashMap<String, Object>();

	protected Request(HttpServletRequest req) {
		this.servletRequest = req;
		this.queryParamMap = parseQueryString(req.getQueryString());
	}

	/**
	 * 从 HttpServletRequest请求中获取指定参数名的参数值：HttpServletRequest.getParameter(name)
	 * 
	 * @param name 参数名
	 * @return 获取到，返回参数值；否则，返回 null
	 */
	public String param(String name)
	{
		return this.servletRequest.getParameter(name);
	}
	
	public int paramInt(String name, int defaultValue)
	{
		if(name == null || name.trim().length() == 0)
		{
			return defaultValue;
		}
		
		try
		{
			return Integer.parseInt(param(name));
		} catch(NumberFormatException e)
		{
			return defaultValue;
		}
	}
	
	public long paramLong(String name, long defaultValue)
	{
		if(name == null || name.trim().length() == 0)
		{
			return defaultValue;
		}
		
		try
		{
			return Long.parseLong(param(name));
		} catch(NumberFormatException e)
		{
			return defaultValue;
		}
	}

	/**
	 * 从 HttpServletRequest
	 * 请求中获取指定参数名的参数值(可能多值)：HttpServletRequest.getParameterValues(name)
	 * 
	 * @param name 参数名
	 * @return 获取到，返回参数值；否则，返回 null
	 */
	public String[] params(String name)
	{
		return this.servletRequest.getParameterValues(name);
	}

	/**
	 * 从 HttpServletRequest 请求中获取所有参数及其值：：HttpServletRequest.getParameterMap()
	 * 
	 * @return 以 Map<String, String[]> 形式返回 HttpServletRequest中所有的参数及其参数值
	 */
	public Map<String, String[]> params()
	{
		return this.servletRequest.getParameterMap();
	}

	/**
	 * 从当前请求的URL的查询参数(?a=1&b=2)中获取指定参数名的参数值
	 * 
	 * @param name 参数名
	 * @return 如果获取到，则返回参数值；否则，返回null
	 */
	public String queryParam(String name)
	{
		String[] vals = queryParams(name);
		return vals != null && vals.length > 0 ? vals[0] : null;
	}
	
	public int queryParamInt(String name, int defaultValue)
	{
		if(name == null || name.trim().length() == 0)
		{
			return defaultValue;
		}
		
		try
		{
			return Integer.parseInt(queryParam(name));
		} catch(NumberFormatException e)
		{
			return defaultValue;
		}
	}
	
	public long queryParamLong(String name, long defaultValue)
	{
		if(name == null || name.trim().length() == 0)
		{
			return defaultValue;
		}
		
		try
		{
			return Long.parseLong(queryParam(name));
		} catch(NumberFormatException e)
		{
			return defaultValue;
		}
	}

	/**
	 * 从当前请求的URL的查询参数(?a=1&a=2&b=3)中获取指定参数名的参数值(可能多值)
	 * 
	 * @param name 参数名
	 * @return 如果获取到，则返回参数值数组；否则，返回null
	 */
	public String[] queryParams(String name)
	{
		return queryParams().get(name);
	}

	/**
	 * 从当前请求的URL的查询参数(?a=1&b=2)中获取所有参数信息
	 * 
	 * @return 以 Map<String, String[]> 形式返回
	 *         HttpServletRequest.getQueryString()中所有的参数及其参数值
	 */
	public Map<String, String[]> queryParams()
	{
		return this.queryParamMap;
	}

	public String queryString()
	{
		String queryStr = this.servletRequest.getQueryString();
		return queryStr != null ? queryStr : "";
	}

	/**
	 * 从当前请求的URL中获取定义的命名参数值 <br>
	 * 例如：@Path("/users/:userid") -- /users/12 --> req.pathParam("userid") == 12
	 * 
	 * @param name 命名参数名称
	 * @return 命名参数值，如果不存在，则返回 null
	 */
	public String pathParam(String name)
	{
		String[] vals = pathParams(name);
		return vals != null && vals.length > 0 ? vals[0] : null;
	}
	
	public int pathParamInt(String name, int defaultValue)
	{
		if(name == null || name.trim().length() == 0)
		{
			return defaultValue;
		}
		
		try
		{
			return Integer.parseInt(pathParam(name));
		} catch(NumberFormatException e)
		{
			return defaultValue;
		}
	}
	
	public long pathParamLong(String name, long defaultValue)
	{
		if(name == null || name.trim().length() == 0)
		{
			return defaultValue;
		}
		
		try
		{
			return Long.parseLong(pathParam(name));
		} catch(NumberFormatException e)
		{
			return defaultValue;
		}
	}

	/**
	 * 从当前请求的URL中获取定义的命名参数值(可能多值)
	 * 
	 * @param name 命名参数名称
	 * @return 命名参数值，如果不存在，则返回 null
	 */
	public String[] pathParams(String name)
	{
		if(this.matchedRoute == null)
		{
			return new String[0];
		}

		return this.matchedRoute.getPathNamedParams(name);
	}

	/**
	 * 从当前请求的URL中获取定义的所有命名参数值
	 * 
	 * @return 以Map<String, String[]>形式返回URL中的所有命名参数及其对应的参数值
	 */
	public Map<String, String[]> pathParams()
	{
		if(this.matchedRoute == null)
		{
			return Collections.emptyMap();
		}

		return this.matchedRoute.getPathNamedParams();
	}

	/**
	 * 从当前请求的URL中使用<b>位置索引</b>获取定义的<b>正则变量</b>参数值： <br>
	 * 例如： <br>
	 * @Path("/users/(\\d+)") -- /users/12 --> req.pathSplat(0) == 12 <br>
	 * @Path("/users/(\\d+)/(edit|delete)") --> /users/12/edit -->
	 * req.pathSplat(0) == 12, req.pathSplat(1) == edit
	 * 
	 * @param index 正则变量参数在整个URL定义中的索引位置，从0开始计数
	 * @return 参数值，如果不存在，则返回 null
	 */
	public String pathSplat(int index)
	{
		if(this.matchedRoute == null)
		{
			return null;
		}

		return this.matchedRoute.getPathSplatParam(index);
	}
	
	public int pathSplatInt(int index, int defaultValue)
	{
		try
		{
			return Integer.parseInt(pathSplat(index));
		} catch(NumberFormatException e)
		{
			return defaultValue;
		}
	}
	
	public long pathSplatLong(int index, long defaultValue)
	{
		try
		{
			return Long.parseLong(pathSplat(index));
		} catch(NumberFormatException e)
		{
			return defaultValue;
		}
	}

	/**
	 * 从当前请求的URL中使用获取定义的所有<b>正则变量</b>参数值： <br>
	 * 例如： <br>
	 * @Path("/users/(\\d+)/(edit|delete)") --> /users/12/edit -->
	 * req.pathSplats() = [12, edit]
	 * 
	 * @return 参数值，如果不存在，则返回 null
	 */
	public String[] pathSplats()
	{
		if(this.matchedRoute == null)
		{
			return new String[0];
		}

		return this.matchedRoute.getPathSplatParams();
	}

	/**
	 * 从当前URL请求中获取通过 Request.Payload 方式发送的数据
	 * 
	 * @return 以字符串形式返回 Payload 数据内容
	 */
	public String body()
	{
		try
		{
			BufferedReader reader = this.servletRequest.getReader();
			StringBuilder bdy = new StringBuilder(500);
			char[] buff = new char[1024];
			int len = -1;
			while((len = reader.read(buff)) != -1)
			{
				bdy.append(buff, 0, len);
			}

			reader.close();

			return bdy.toString();

		} catch(IOException e)
		{
			throw new SparkException(e);
		}
	}
	
	public <T> T body(Class<T> beanClass)
	{
		return SparkConfig.getConfig().jsonResolver().toObject(beanClass, body());
	}
	
	public <T> T bean(Class<T> beanClass)
	{
		return bean(beanClass, null);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T bean(Class<T> beanClass, String alias)
	{
		try
		{
			return (T) Binder.bind(beanClass, alias, params());
		} catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	public Object bean(Object beanObject)
	{
		return bean(beanObject, null);
	}
	
	public Object bean(Object beanObject, String alias)
	{
		if(beanObject == null)
		{
			return null;
		}
		
		return Binder.bind(beanObject, alias, params());
	}

	public Object attr(String name)
	{
		return this.servletRequest.getAttribute(name);
	}

	public void attr(String name, Object value)
	{
		this.servletRequest.setAttribute(name, value);
	}

	public Object flash(String name)
	{
		// TODO flash
		return null;
	}

	public void flash(String name, Object value)
	{
		// TODO flash
	}

	public void forward(String url)
	{
		try
		{
			this.servletRequest.getRequestDispatcher(url).forward(this.servletRequest,
					HttpContext.current().response().httpServletResponse());
		} 
		catch(ServletException e)
		{
			throw new UnexpectedException(e);
		} catch(IOException e)
		{
			throw new UnexpectedException(e);
		}
	}

	/**
	 * 获取所有 Cookie 信息
	 * 
	 * @return 所有 Cookie 信息
	 */
	public Cookie[] cookies()
	{
		return this.servletRequest.getCookies();
	}

	/**
	 * 获取指定 Cookie 名称的 Cookie 信息
	 * 
	 * @param cookieName Cookie名称
	 * @return 指定 Cookie 名称的 Cookie 信息
	 */
	public Cookie cookie(String cookieName)
	{
		Cookie[] cookies = cookies();

		if(cookies == null || cookies.length == 0 || cookieName == null || cookieName.trim().length() == 0)
		{
			return null;
		}

		for(Cookie _cookie : cookies)
		{
			if(_cookie.getName().equals(cookieName))
			{
				return _cookie;
			}
		}

		return null;
	}

	/**
	 * 获取指定 Cookie名称的Cookie值
	 * 
	 * @param cookieName Cookie名称
	 * @return 指定 Cookie名称的Cookie值，如果不存在，返回 null
	 */
	public String cookieValue(String cookieName)
	{
		Cookie cookie = cookie(cookieName);
		if(cookie == null)
		{
			return null;
		}

		return cookie.getValue();
	}

	public int contentLength()
	{
		return this.servletRequest.getContentLength();
	}

	public String contentType()
	{
		return this.servletRequest.getContentType();
	}

	public Enumeration<String> headers(String name)
	{
		return this.servletRequest.getHeaders(name);
	}

	public String header(String name)
	{
		return this.servletRequest.getHeader(name);
	}

	public String host()
	{
		return this.servletRequest.getHeader("host");
	}

	public String ip()
	{
		String ip = header("X-Real-IP");

		if(ip == null || ip.trim().length() == 0 || "unknown".equalsIgnoreCase(ip))
		{
			ip = header("X-Forwarded-For");
		}

		if(ip == null || ip.trim().length() == 0 || "unknown".equalsIgnoreCase(ip))
		{
			ip = header("Proxy-Client-IP");
		}

		if(ip == null || ip.trim().length() == 0 || "unknown".equalsIgnoreCase(ip))
		{
			ip = header("WL-Proxy-Client-IP");
		}

		if(ip != null && ip.trim().length() > 0)
		{
			String[] ips = ip.split(",");
			if(ips.length > 0)
			{
				for(String tmpip : ips)
				{
					if(isIPAddr(tmpip))
					{
						return tmpip.trim();
					}
				}
			}
		}

		ip = this.servletRequest.getRemoteAddr();

		return ip;
	}

	/**
	 * support raw request handed in by Jetty
	 * 
	 * @return
	 */
	public HttpServletRequest raw()
	{
		return this.servletRequest;
	}

	public String method()
	{
		String _method = header("x-http-method");
		if(_method != null && _method.trim().length() > 0)
		{
			return _method;
		}

		_method = param("x-http-method");
		if(_method != null && _method.trim().length() > 0)
		{
			return _method;
		}

		return this.servletRequest.getMethod();
	}

	public String base()
	{
		if(port() == 80 || port() == 443)
		{
			return String.format("%s://%s", secure() ? "https" : "http", domain()).intern();
		}

		return String.format("%s://%s:%s", secure() ? "https" : "http", domain(), port()).intern();
	}

	public String contextPath()
	{
		String cpath = this.servletRequest.getContextPath();
		if(cpath == null || cpath.trim().length() == 0)
		{
			return "";
		}

		return cpath;
	}

	public String servletPath()
	{
		return this.servletRequest.getServletPath();
	}

	public String rawPath()
	{
		return this.servletRequest.getRequestURI();
	}

	public String pathInfo()
	{
		return this.servletRequest.getPathInfo();
	}

	public String path()
	{
		String path = (String) this.servletRequest.getAttribute("javax.servlet.include.servlet_path");
		String info = (String) this.servletRequest.getAttribute("javax.servlet.include.path_info");
		
		if(path == null)
		{
			path = servletPath();
			info = pathInfo();
		}
		
		if(info != null)
		{
			path = path + info;
		}
		
		return path;
	}

	public String url()
	{
		return this.servletRequest.getRequestURL().toString();
	}

	public String uri()
	{
		return this.servletRequest.getRequestURI();
	}

	public String protocol()
	{
		return this.servletRequest.getProtocol();
	}

	public String scheme()
	{
		return this.servletRequest.getScheme();
	}

	public int port()
	{
		return this.servletRequest.getServerPort();
	}

	public boolean secure()
	{
		return "https".equalsIgnoreCase(scheme());
	}

	public String domain()
	{
		return this.servletRequest.getServerName();
	}

	public String userAgent()
	{
		return header("user-agent");
	}

	public String encoding()
	{
		return this.servletRequest.getCharacterEncoding();
	}

	public void encoding(String _encoding)
	{
		try
		{
			this.servletRequest.setCharacterEncoding(_encoding);
		} catch(UnsupportedEncodingException e)
		{
			throw new SparkException(e);
		}
	}

	public boolean isAjax()
	{
		return "XMLHttpRequest".equals(header("x-requested-with"));
	}

	public boolean isMultipart()
	{
		return this.servletRequest instanceof MultipartHttpRequest;
	}

	/**
	 * 获取上传的文件信息
	 */
	public UploadedFile[] files()
	{
		if(!isMultipart())
		{
			return new UploadedFile[0];
		}

		MultipartHttpRequest mreq = (MultipartHttpRequest) this.servletRequest;
		// 获取请求 Multipart/form-data 表单中所有的文件组件
		Enumeration<String> inputFileNames = mreq.getInputFileNames();

		List<UploadedFile> uploadfiles = new ArrayList<UploadedFile>();

		File upFile = null;
		String inputFileName = null;
		UploadedFile uploadFile = null;

		while(inputFileNames != null && inputFileNames.hasMoreElements())
		{
			inputFileName = inputFileNames.nextElement();
			upFile = mreq.getFile(inputFileName);
			if(upFile == null || !upFile.exists())
			{
				continue;
			}

			uploadFile = new UploadedFile(inputFileName, upFile.getParent(), mreq.getFilesystemName(inputFileName),
					mreq.getOriginalFileName(inputFileName), mreq.getContentType(inputFileName));

			uploadfiles.add(uploadFile);
		}

		return uploadfiles.toArray(new UploadedFile[uploadfiles.size()]);
	}

	public HttpSession session()
	{
		return this.servletRequest.getSession();
	}

	public HttpSession session(boolean createNew)
	{
		return this.servletRequest.getSession(createNew);
	}

	public HttpServletRequest servletRequest()
	{
		return this.servletRequest;
	}

	public ServletContext servletContext()
	{
		return this.servletRequest.getServletContext();
	}

	/**
	 * 设置当前匹配的路由
	 * 
	 * @param mRoute
	 */
	void setMatchedRoute(MatchedRoute mRoute)
	{
		this.matchedRoute = mRoute;
	}

	/**
	 * 将URL查询字符串解析为参数键值对
	 */
	private Map<String, String[]> parseQueryString(String queryStr)
	{
		if(queryStr == null || queryStr.trim().length() == 0)
		{
			return Collections.emptyMap();
		}

		String[] params = queryStr.split("&");
		if(params == null || params.length == 0)
		{
			return Collections.emptyMap();
		}

		Map<String, String[]> paramMap = new HashMap<String, String[]>(params.length);
		for(String param : params)
		{
			String[] kv = param.split("=");
			String[] v = null;

			if(!paramMap.containsKey(kv[0]))
			{
				v = new String[1];
				v[0] = decodeParam(kv.length > 1 ? kv[1] : "");
			}
			else
			{
				v = new String[paramMap.get(kv[0]).length + 1];
				System.arraycopy(paramMap.get(kv[0]), 0, v, 0, v.length - 1);
				v[v.length - 1] = decodeParam(kv.length > 1 ? kv[1] : "");
			}

			paramMap.put(kv[0], v);
		}

		return paramMap;
	}

	/**
	 * 将使用UTF-8编码的URL参数内容进行解码
	 * 
	 * @param s
	 * @return
	 */
	private String decodeParam(String s)
	{
		try
		{
			return URLDecoder.decode(s, encoding());
		} catch(UnsupportedEncodingException e)
		{
			return s;
		}
	}

	/**
	 * 判断字符串是否为ip地址格式
	 * 
	 * @param addr
	 * @return
	 */
	private static boolean isIPAddr(String addr)
	{
		if(addr == null || addr.trim().length() == 0)
		{
			return false;
		}

		String[] ips = addr.split("\\.");
		if(ips.length != 4)
		{
			return false;
		}

		try
		{
			int ipa = Integer.parseInt(ips[0]);
			int ipb = Integer.parseInt(ips[1]);
			int ipc = Integer.parseInt(ips[2]);
			int ipd = Integer.parseInt(ips[3]);

			return ipa >= 0 && ipa <= 255 && ipb >= 0 && ipb <= 255 && ipc >= 0 && ipc <= 255 && ipd >= 0 && ipd <= 255;

		} catch(NumberFormatException e)
		{
			// ignore
		}

		return false;
	}

	@Override
	public String toString()
	{
		return method() + " " + path()
				+ (queryString() != null && queryString().length() > 0 ? "?" + queryString() : "");
	}
}
