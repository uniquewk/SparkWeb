package com.sparkweb.web.view;

import com.sparkweb.web.Request;
import com.sparkweb.web.Response;

/**
 * 视图渲染解析器
 * 
 * @author yswang
 * @version 1.0
 */
public interface ViewResolver
{
	/**
	 * 配置视图文件名后缀
	 * @return
	 */
	String viewSuffix();
	
	/**
	 * 配置视图文件所在的根目录
	 * 
	 * @return
	 */
	String viewDirectory();
	
	/**
	 * 渲染视图
	 * 
	 * @param view 视图文件
	 * @param request
	 * @param response
	 */
	void render(String view, final Request request, final Response response);
}
