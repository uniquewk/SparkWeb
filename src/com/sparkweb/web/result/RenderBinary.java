package com.sparkweb.web.result;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import com.sparkweb.exception.UnexpectedException;
import com.sparkweb.util.MimeTypes;
import com.sparkweb.web.Request;
import com.sparkweb.web.Response;

/**
 * 200 OK with application/octet-stream
 */
public class RenderBinary extends Result
{
	private static final long	serialVersionUID			= -4110634480472241004L;

	private static final String	INLINE_DISPOSITION_TYPE		= "inline";
	private static final String	ATTACHMENT_DISPOSITION_TYPE	= "attachment";

	private boolean						inline						= false;
	private long						length						= 0;
	private File						file;
	private BufferedInputStream			bis;
	private String						name;
	private String						contentType;

	/**
	 * send a binary stream as the response
	 * 
	 * @param is the stream to read from
	 * @param name the name to use as Content-Diposition attachement filename
	 */
	public RenderBinary(InputStream is, String name) {
		this(is, name, false);
	}

	public RenderBinary(InputStream is, String name, long length) {
		this(is, name, length, false);
	}

	/**
	 * send a binary stream as the response
	 * 
	 * @param is the stream to read from
	 * @param name the name to use as Content-Diposition attachement filename
	 * @param inline true to set the response Content-Disposition to inline
	 */
	public RenderBinary(InputStream is, String name, boolean inline) {
		this(is, name, null, inline);
	}

	/**
	 * send a binary stream as the response
	 * 
	 * @param is the stream to read from
	 * @param name the name to use as Content-Diposition attachement filename
	 * @param inline true to set the response Content-Disposition to inline
	 */
	public RenderBinary(InputStream is, String name, String contentType, boolean inline) {
		this(is, name, 0, contentType, false);
	}

	public RenderBinary(InputStream is, String name, long length, boolean inline) {
		this(is, name, 0, null, false);
	}
	
	public RenderBinary(InputStream is, String name, long length, String contentType, boolean inline) {
		this.bis = new BufferedInputStream(is);
		this.name = name;
		this.contentType = contentType;
		this.inline = inline;
		this.length = length;
	}

	/**
	 * Send a file as the response. Content-disposion is set to attachment, name
	 * is taken from file's name
	 * 
	 * @param file readable file to send back
	 */
	public RenderBinary(File file) {
		this(file, file.getName(), false);
	}
	
	/**
	 * Send a file as the response. Content-disposion is set to attachment.
	 * 
	 * @param file readable file to send back
	 * @param name a name to use as Content-disposion's filename
	 */
	public RenderBinary(File file, String name) {
		this(file, name, false);
	}

	/**
	 * Send a file as the response. Content-disposion is set to attachment, name
	 * is taken from file's name
	 * 
	 * @param file readable file to send back
	 */
	public RenderBinary(File file, String name, boolean inline) {
		this.file = file;
		this.name = name;
		this.inline = inline;
		
		if(file == null)
		{
			throw new NullPointerException("File must not be null!");
		}
		
		if(!file.exists())
		{
			throw new UnexpectedException("File '"+ file.toString() +"' does not exist!");
		}
	}

	@Override
	public void apply(Request request, Response response)
	{
		try
		{
			if(name != null)
			{
				response.contentType(MimeTypes.getContentType(name));
			}
			
			if(contentType != null)
			{
				response.contentType(contentType);
			}
			
			String dispositionType = null;
			if(inline)
			{
				dispositionType = INLINE_DISPOSITION_TYPE;
			}
			else
			{
				dispositionType = ATTACHMENT_DISPOSITION_TYPE;
			}
			
			if(!headerContains(response.headerNames(), "Content-Disposition"))
			{
				if(name == null)
				{
					response.header("Content-Disposition", dispositionType);
				}
				else
				{
					if(canAsciiEncode(name))
					{
						String contentDisposition = "%s; filename=\"%s\"";
						response.header("Content-Disposition", String.format(contentDisposition, dispositionType, name));
					}
					else
					{
						final String encoding = getEncoding();
						String contentDisposition = "%1$s; filename*=" + encoding + "''%2$s; filename=\"%2$s\"";
						response.header("Content-Disposition",
								String.format(contentDisposition, dispositionType, URLEncoder.encode(name, encoding)));
					}
				}
			}
			
			if(file != null)
			{
				if(!file.exists())
				{
					throw new UnexpectedException("Your file does not exists (" + file + ")!");
				}
				
				if(!file.canRead())
				{
					throw new UnexpectedException("Can't read your file (" + file + ")!");
				}
				
				if(!file.isFile())
				{
					throw new UnexpectedException("Your file is not a real file (" + file + ")!");
				}
				
				bis = new BufferedInputStream(new FileInputStream(file));
				length = file.length();
			}
			
			if(length != 0)
			{
				response.header("Content-Length", String.valueOf(length));
			}
			
			if(bis != null)
			{
				BufferedOutputStream bos = new BufferedOutputStream(response.out());
				try
				{
					byte[] buffer = new byte[8192];
					int bytesRead = 0;
					while((bytesRead = bis.read(buffer, 0, buffer.length)) != -1)
					{
						bos.write(buffer, 0, bytesRead);
					}
					bos.flush();
					
				} finally {
					try {
						bis.close();
						bis = null;
					} catch(IOException e) {
						// ignore
					}
					
					try {
						if(bos != null) {
							bos.close();
							bos = null;
						}
					} catch(IOException e) {
						// ignore
					}
				}
			} 
			else {
				throw new UnexpectedException("Could not found any File or Stream to render binary!");
			}
			
		} catch(Exception e) {
			throw new UnexpectedException(e);
		}
	}

	private static boolean headerContains(String[] headers, String header)
	{
		if(headers == null || headers.length == 0)
		{
			return false;
		}
		
		for(String _header : headers)
		{
			if(_header.equalsIgnoreCase(header))
			{
				return true;
			}
		}
		
		return false;
	}
	
	private static boolean canAsciiEncode(String string)
	{
		CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder();
		return asciiEncoder.canEncode(string);
	}
	
}
