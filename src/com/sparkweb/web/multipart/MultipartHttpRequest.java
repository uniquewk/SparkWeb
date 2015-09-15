package com.sparkweb.web.multipart;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * Multipart/form-data请求封装，自动上传文件
 * 
 * @author yswang
 * @version 1.0
 */
@SuppressWarnings("unchecked")
public class MultipartHttpRequest extends HttpServletRequestWrapper
{
	private static final int						MAX_POST_SIZE	= 100 * 1024 * 1024;

	private com.oreilly.servlet.MultipartRequest	cos_mreq;

	public MultipartHttpRequest(HttpServletRequest request, String saveDirectory, String encoding) throws IOException {
		this(request, saveDirectory, MAX_POST_SIZE, encoding);
	}

	public MultipartHttpRequest(HttpServletRequest request, String saveDirectory, int maxPostSize, String encoding)
			throws IOException {
		super(request);

		this.cos_mreq = new com.oreilly.servlet.MultipartRequest(request, saveDirectory, maxPostSize, encoding,
				new DefaultFileRenamePolicy());
	}

	@Override
	public String getParameter(String name)
	{
		String val = super.getParameter(name);
		if(val == null)
		{
			val = this.cos_mreq.getParameter(name);
		}

		return val;
	}

	@Override
	public String[] getParameterValues(String name)
	{
		String[] vals = super.getParameterValues(name);
		if(vals == null)
		{
			vals = this.cos_mreq.getParameterValues(name);
		}

		return vals;
	}

	@Override
	public Enumeration<String> getParameterNames()
	{
		return this.cos_mreq.getParameterNames();
	}

	@Override
	public Map<String, String[]> getParameterMap()
	{
		Map<String, String[]> map = new HashMap<String, String[]>();
		Enumeration<String> enumm = getParameterNames();
		while(enumm.hasMoreElements())
		{
			String name = enumm.nextElement();
			map.put(name, getParameterValues(name));
		}

		return map;
	}

	/**
	 * 获取表单域中所有的 &lt;input type="file" name="xx"/&gt; 的域名称
	 * 
	 * @return
	 */
	public Enumeration<String> getInputFileNames()
	{
		return this.cos_mreq.getFileNames();
	}

	/**
	 * 根据 &lt;input type="file" name="xx"/&gt; 的域名称获取对应的上传保存后的文件名称
	 * 
	 * @param inputFileName
	 * @return
	 */
	public String getFilesystemName(String inputFileName)
	{
		return this.cos_mreq.getFilesystemName(inputFileName);
	}

	/**
	 * 根据 &lt;input type="file" name="xx"/&gt; 的域名称获取对应的上传文件的原始文件名
	 * 
	 * @param inputFileName
	 * @return
	 */
	public String getOriginalFileName(String inputFileName)
	{
		return this.cos_mreq.getOriginalFileName(inputFileName);
	}

	/**
	 * 根据 &lt;input type="file" name="xx"/&gt; 的域名称获取对应的上传文件的ContentType类型
	 * 
	 * @param inputFileName
	 * @return
	 */
	public String getContentType(String inputFileName)
	{
		return this.cos_mreq.getContentType(inputFileName);
	}

	/**
	 * 根据 &lt;input type="file" name="xx"/&gt; 的域名称获取对应的上传文件的文件实体对象
	 * 
	 * @param name
	 * @return
	 */
	public File getFile(String inputFileName)
	{
		return this.cos_mreq.getFile(inputFileName);
	}

}
