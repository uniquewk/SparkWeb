package com.sparkweb.web;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Collection;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import com.sparkweb.web.result.BadRequest;
import com.sparkweb.web.result.Forbidden;
import com.sparkweb.web.result.NoResult;
import com.sparkweb.web.result.NotFound;
import com.sparkweb.web.result.Ok;
import com.sparkweb.web.result.Redirect;
import com.sparkweb.web.result.RedirectToStatic;
import com.sparkweb.web.result.RenderBinary;
import com.sparkweb.web.result.RenderHtml;
import com.sparkweb.web.result.RenderJson;
import com.sparkweb.web.result.RenderJsonp;
import com.sparkweb.web.result.RenderText;
import com.sparkweb.web.result.RenderXml;
import com.sparkweb.web.result.Unauthorized;

/**
 * HTTP 响应
 * 
 * @author yswang
 * @version 1.0
 */
public final class Response
{
	private HttpServletResponse	servletResponse;

	protected Response(HttpServletResponse res) {
		this.servletResponse = res;
	}

	/**
	 * Return a 200 OK text/plain response
	 * 
	 * @param text The response content
	 */
	public void text(CharSequence text)
	{
		throw new RenderText(text);
	}
	
	/**
	 * Return a 200 OK text/html response
	 * 
	 * @param html The response content
	 */
	public void html(CharSequence html)
	{
		throw new RenderHtml(html);
	}
	
	/**
	 * Return a 200 OK application/json response
	 * 
	 * @param jsonString The JSON data string
	 */
	public void json(CharSequence jsonString)
	{
		throw new RenderJson(jsonString);
	}
	
	/**
	 * Return a 200 OK application/json response
	 * 
	 * @param object 
	 */
	public void json(Object object)
	{
		throw new RenderJson(object);
	}
	
	/**
	 * Return a 200 OK application/json response
	 * 
	 * @param jsonString The JSON data string
	 */
	public void jsonp(CharSequence jsonString)
	{
		throw new RenderJsonp(jsonString);
	}
	
	/**
	 * Return a 200 OK text/xml response
	 * 
	 * @param xml The XML string
	 */
	public void xml(CharSequence xml)
	{
		throw new RenderXml(xml);
	}
	
	public void redirect(String url)
	{
		throw new Redirect(url);
	}
	
	public void redirect(String url, Map<String, String[]> params)
	{
		throw new Redirect(url, params);
	}
	
	public void renderView(String viewName)
	{
		SparkConfig.getConfig().viewResolver().render(viewName, HttpContext.current().request(), this);
		throw new NoResult();
	}
	
	/**
	 * Return a 200 OK application/binary response
	 * 
	 * @param is The stream to copy
	 */
	public void renderBinary(InputStream is)
	{
		throw new RenderBinary(is, null);
	}
	
	/**
	 * Return a 200 OK application/binary response with content-disposition
	 * attachment.
	 * 
	 * @param is The stream to copy
	 * @param name Name of file user is downloading.
	 */
	public void renderBinary(InputStream is, String name)
	{
		throw new RenderBinary(is, name);
	}

	/**
	 * Return a 200 OK application/binary response
	 * 
	 * @param file The file to copy
	 */
	public void renderBinary(File file)
	{
		throw new RenderBinary(file);
	}

	/**
	 * Return a 200 OK application/binary response with content-disposition
	 * attachment
	 * 
	 * @param file The file to copy
	 * @param name The attachment name
	 */
	public void renderBinary(File file, String name)
	{
		throw new RenderBinary(file, name);
	}
	
	public void render(Object o, ResponseRender responseRender)
	{
		responseRender.render(o, HttpContext.current().request(), this);
		throw new NoResult();
	}
	
	public void renderStatic(String staticResource)
	{
		throw new RedirectToStatic(staticResource);
	}
	
	public void ok()
	{
		throw new Ok();
	}
	
	public void halt(String reason)
	{
		throw new com.sparkweb.web.result.Error(reason);
	}
	
	public void halt(int status, String reason)
	{
		throw new com.sparkweb.web.result.Error(status, reason);
	}
	
	public void badRequest()
	{
		throw new BadRequest();
	}
	
	public void badRequest(String why)
	{
		throw new BadRequest(why);
	}
	
	public void forbidden()
	{
		throw new Forbidden();
	}
	
	public void forbidden(String why)
	{
		throw new Forbidden(why);
	}
	
	public void notFound()
	{
		throw new NotFound();
	}
	
	public void notFound(String why)
	{
		throw new NotFound(why);
	}
	
	public void unauthorized(String realm)
	{
		throw new Unauthorized(realm);
	}
	
	public void header(String name, String value)
	{
		this.servletResponse.addHeader(name, value);
	}
	
	public String header(String name)
	{
		return this.servletResponse.getHeader(name);
	}
	
	public String[] headers(String name)
	{
		Collection<String> _headers = this.servletResponse.getHeaders(name);
		return _headers != null ? _headers.toArray(new String[_headers.size()]) : new String[0];
	}
	
	public String[] headerNames()
	{
		Collection<String> names = this.servletResponse.getHeaderNames();
		return names != null ? names.toArray(new String[names.size()]) : new String[0];
	}
	
	public void cookie(String name, String value)
	{
		cookie(name, value, -1, false);
	}
	
	public void cookie(String name, String value, int maxAge)
	{
		cookie(name, value, maxAge, false);
	}
	
	public void cookie(String name, String value, int maxAge, boolean httpOnly)
	{
		cookie("", name, value, maxAge, httpOnly);
	}
	
	public void cookie(String path, String name, String value, int maxAge, boolean httpOnly)
	{
		Cookie cookie = new Cookie(name, value);
		cookie.setPath(path);
		cookie.setMaxAge(maxAge);
		cookie.setHttpOnly(httpOnly);
		
		cookie(cookie);
	}
	
	public void cookie(Cookie cookie)
	{
		if(cookie == null)
		{
			throw new IllegalArgumentException("Can not save null cookie!");
		}
		
		this.servletResponse.addCookie(cookie);
	}
	
	public void removeCookie(String name)
	{
		Cookie cookie = new Cookie(name, "");
		cookie.setMaxAge(0);
		
		removeCookie(cookie);
	}
	
	public void removeCookie(Cookie cookie)
	{
		if(cookie == null)
		{
			return;
		}
		
		cookie.setMaxAge(0);
		this.servletResponse.addCookie(cookie);
	}

	public int status()
	{
		return this.servletResponse.getStatus();
	}
	
	public void status(int statusCode)
	{
		this.servletResponse.setStatus(statusCode);
	}
	
	public void print(Object o) throws IOException 
	{
        this.servletResponse.getOutputStream().write(o.toString().getBytes(encoding()));
    }
	
	public Writer writer() throws IOException 
	{
		return this.servletResponse.getWriter();
	}
	
	public OutputStream out() throws IOException
	{
		return this.servletResponse.getOutputStream();
	}

	public void noCache()
	{
		this.servletResponse.setHeader("Pragma", "No-cache");
		this.servletResponse.setHeader("Cache-Control", "no-cache");
		this.servletResponse.setDateHeader("Expires", 0);
	}
	
	public String contextType()
	{
		return this.servletResponse.getContentType();
	}
	
	public void contentType(String contentType)
	{
		this.servletResponse.setContentType(contentType);
	}

	public String encoding()
	{
		return this.servletResponse.getCharacterEncoding();
	}
	
	public void encoding(String _encoding)
	{
		this.servletResponse.setCharacterEncoding(_encoding);
	}

	public void flush() throws IOException
	{
		this.servletResponse.flushBuffer();
	}
	
	public void reset()
	{
		this.servletResponse.reset();
	}
	
	public HttpServletResponse httpServletResponse()
	{
		return this.servletResponse;
	}
}
