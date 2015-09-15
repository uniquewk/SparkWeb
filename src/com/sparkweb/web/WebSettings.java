package com.sparkweb.web;

import com.sparkweb.web.view.ViewResolver;

/**
 * 基于SparkWeb的Java Web项目需要创建一个实现 <b>SparkwebSetting</b> 的实现类，并实现相关的方法，
 * 并且该实现类上要使用 @SparkwebSetting 注解标注，便于 SparkWeb 发现
 * 该类是对整个Web项目进行全局的设置。
 * 
 * @author yswang
 * @version 1.0
 */
public interface WebSettings
{
	/**
	 * 设置请求和响应编码
	 * 
	 * @return
	 */
	String encoding();
	
	/**
	 * 静态文件(js,css,image等)的访问前缀定义。
	 * <br> 如果一个资源请求路径是以该前缀路径开头的，则认为该请求是一个静态资源文件的请求。
	 * 
	 * @return
	 */
	String[] staticAssetsPath();
	
	/**
	 * 视图渲染器
	 * 
	 * @return
	 */
	ViewResolver viewResolver();
	
	/**
	 * JSON数据结构处理器
	 * 
	 * @return
	 */
	JSONResolver jsonResolver();
	
	/**
	 * 定义jsonp的callback回调函数名，默认 `callback`
	 * 
	 * @return
	 */
	String jsonpCallbackName();
	
	/**
	 * 配置Sparkweb进行路由定义扫描时跳过哪些jar包的扫描。
	 * <br> 这有助于加快路由扫描。
	 * 
	 * @return
	 */
	String[] jarsToSkipWhenScanningRoutes();
	
	/**
	 * 是否开启请求路由匹配区分大小写。<br>
	 * 如果开启大小写区分：/user != /User；否则：/user == /User
	 * 
	 * @return
	 */
	boolean caseSensitiveRouting();
	
	/**
	 * 是否开启请求路由匹配使用严格模式.<br>
	 * 如果开启严格模式：/user != /user/； 否则：/user == /user/
	 * 
	 * @return
	 */
	boolean strictRouting();
	
	/**
	 * 当请求没有任何路由匹配命中时，会调用该方法由开发者进行可能的其它处理。
	 * 
	 * @param request 当前请求对象
	 * @param response 当前请求的响应对象
	 */
	void rescueRouting(final Request request, final Response response);
	
	/**
	 * 当请求处理产生的异常没有被内部捕获时，会将异常信息传递到这里进行处理。
	 * 
	 * @param e 抛出的异常
	 * @param request 当前请求对象
	 * @param response 当前请求的响应对象
	 */
	void exception(final Throwable e, final Request request, final Response response) throws Throwable;
	
}
