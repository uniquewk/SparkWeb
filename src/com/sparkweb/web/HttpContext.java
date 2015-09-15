package com.sparkweb.web;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.sparkweb.exception.SparkException;
import com.sparkweb.web.multipart.MultipartHttpRequest;
import com.sparkweb.web.router.MatchedRoute;

/**
 * Http请求上下文
 * 
 * @author yswang
 * @version 1.0
 */
public final class HttpContext
{
	private static final String						TEMP_UPLOAD_PATH_ATTR_NAME	= "$__SPARKWEB_upload_tmp_path$";

	private static final String						ENCODING					= "UTF-8";

	private static final ThreadLocal<HttpContext>	CURRENT_HTTP_CONTEXT		= new ThreadLocal<HttpContext>();

	private Request									request;
	private Response								response;

	private static String							upload_tmp_path;

	static
	{
		// 初始化上传文件的临时目录
		upload_tmp_path = getWebRootPath() + "WEB-INF" + File.separator + "sparkweb_file_tmp" + File.separator;

		File uploadTmpDir = new File(upload_tmp_path);
		if(uploadTmpDir.exists())
		{
			if(!uploadTmpDir.isDirectory())
			{
				throw new SparkException("File <" + uploadTmpDir
						+ "> exists and is not a directory. Unable to create directory!");
			}
		}
		else if(!uploadTmpDir.mkdirs())
		{
			if(!uploadTmpDir.isDirectory())
			{
				throw new SparkException("Unable to create directory: " + uploadTmpDir);
			}
		}
	}

	static HttpContext init(HttpServletRequest req, HttpServletResponse res, String _encoding)
	{
		try
		{
			req.setCharacterEncoding(_encoding != null ? _encoding : ENCODING);
		} catch(UnsupportedEncodingException e)
		{
			// ignore
		}

		res.setCharacterEncoding(_encoding != null ? _encoding : ENCODING);
		// 用来防止iframe下session丢失
		res.setHeader("P3P", "CP='IDC DSP COR ADM DEVi TAIi PSA PSD IVAi CONi HIS OUR IND CNT'");

		HttpContext httpContext = new HttpContext();
		httpContext.request = new Request(autoWrapMultipartRequest(req));
		httpContext.response = new Response(res);

		CURRENT_HTTP_CONTEXT.set(httpContext);

		return httpContext;
	}

	private HttpContext() {
	}

	/**
	 * Retrieves the current HTTP RequestContext, for the current thread.
	 */
	public static HttpContext current()
	{
		HttpContext hc = CURRENT_HTTP_CONTEXT.get();
		if(hc == null)
		{
			throw new SparkException("There is no HTTP Context available from current request!");
		}

		return hc;
	}

	public Request request()
	{
		return this.request;
	}

	public Response response()
	{
		return this.response;
	}

	public void matchedRoute(MatchedRoute mRoute)
	{
		this.request.setMatchedRoute(mRoute);
	}
	
	public static String getWebRootPath()
	{
		String root = HttpContext.class.getResource("/").getFile();
		try
		{
			root = new File(root).getParentFile().getParentFile().getCanonicalPath();
			root += File.separator;
		} catch(IOException e)
		{
			throw new SparkException(e);
		}

		return root;
	}

	/**
	 * 清理 HttpContext 这个方法必须在每次请求结束后调用，否则会造成内存泄露！！
	 */
	protected void destroy()
	{
		// 当一次请求处理完成后，自动删除上传在临时目录下的文件
		String tmpPath = (String) this.request.attr(TEMP_UPLOAD_PATH_ATTR_NAME);
		if(tmpPath != null)
		{
			File tmpDir = new File(tmpPath);
			if(tmpDir != null && tmpDir.exists() && tmpDir.isDirectory())
			{
				File[] subFiles = tmpDir.listFiles();
				if(subFiles != null && subFiles.length > 0)
				{
					for(File subFile : subFiles)
					{
						subFile.delete();
					}
				}

				tmpDir.delete();
			}
		}

		this.request = null;
		this.response = null;

		CURRENT_HTTP_CONTEXT.set(null);
		CURRENT_HTTP_CONTEXT.remove();
	}

	/**
	 * 判断一个请求是否是 multipart/form-data 表单发生的请求
	 */
	public static boolean isMultipartRequest(HttpServletRequest req)
	{
		// Check the content type to make sure it's "multipart/form-data"
		// Access header two ways to work around WebSphere oddities
		String type = null;
		String type1 = req.getHeader("Content-Type");
		String type2 = req.getContentType();
		// If one value is null, choose the other value
		if(type1 == null && type2 != null)
		{
			type = type2;
		}
		else if(type2 == null && type1 != null)
		{
			type = type1;
		}
		// If neither value is null, choose the longer value
		else if(type1 != null && type2 != null)
		{
			type = (type1.length() > type2.length() ? type1 : type2);
		}

		return "POST".equals(req.getMethod().toUpperCase(Locale.US))
				&& (type != null && type.toLowerCase(Locale.US).startsWith("multipart/form-data"));
	}

	/**
	 * 自动封装multipart request请求，将自动上传请求中的文件到定义的上传文件临时目录。
	 */
	private static HttpServletRequest autoWrapMultipartRequest(HttpServletRequest req)
	{
		if(!isMultipartRequest(req) || req instanceof MultipartHttpRequest)
		{
			return req;
		}

		String savePath = upload_tmp_path + UUID.randomUUID().toString();
		File saveDir = new File(savePath);

		if(!saveDir.exists() && !saveDir.isDirectory())
		{
			if(!saveDir.mkdirs())
			{
				throw new SparkException("Failed to mkdirs<" + savePath
						+ "> for the upload file tmp diectory to save it!");
			}
		}

		// 保存当前上传文件所在的临时目录，便于当请求完成后进行删除
		req.setAttribute(TEMP_UPLOAD_PATH_ATTR_NAME, savePath);

		try
		{
			// 自动上传存在的文件
			return new MultipartHttpRequest(req, saveDir.getCanonicalPath(), ENCODING);
		} catch(IOException e)
		{
			throw new SparkException("Failed to save upload files into tmp directory: " + savePath + "! Caused by: ", e);
		}
	}

}
