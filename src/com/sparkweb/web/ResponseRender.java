package com.sparkweb.web;

/**
 * @author yswang
 * @version 1.0
 */
public interface ResponseRender
{
	void render(Object o, final Request request, final Response response);
}
