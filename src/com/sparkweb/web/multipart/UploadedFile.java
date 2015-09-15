package com.sparkweb.web.multipart;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import com.sparkweb.util.EscapeUtils;

/**
 * 上传文件对象
 * 
 * @author yswang
 * @version 1.0
 */
public final class UploadedFile
{
	private String	saveDir;
	private String	inputFileName;
	private String	fileName;
	private String	originalName;
	private String	contentType;

	public UploadedFile(String inputFileName, String saveDir, String filesystemName, String originalFileName,
			String contentType) {

		this.inputFileName = inputFileName;
		this.saveDir = saveDir;
		this.fileName = filesystemName;
		this.originalName = originalFileName;
		this.contentType = contentType;
	}

	/**
	 * 返回存储在磁盘上的文件名称（可能与原始文件命名不同，如果存在重命名定义的话）
	 * 
	 * @return
	 */
	public String getFileName()
	{
		return this.fileName;
	}

	/**
	 * 返回文件的原始文件名
	 * 
	 * @return
	 */
	public String getOriginalFileName()
	{
		return this.originalName;
	}

	/**
	 * 返回该文件对应的表单域中 &lt;input type="file" name=""/&gt;的name属性值
	 * 
	 * @return
	 */
	public String getInputFileName()
	{
		return this.inputFileName;
	}

	/**
	 * 返回文件的ContentType类型，标准的类型定义，比如：image/png等
	 * 
	 * @return
	 */
	public String getContentType()
	{
		return this.contentType;
	}

	/**
	 * 返回文件的后缀扩展名
	 * 
	 * @return
	 */
	public String getExtension()
	{
		int index = getOriginalFileName().lastIndexOf('.');
		if(index == -1)
		{
			return "";
		}

		return getOriginalFileName().substring(index + 1);
	}

	/**
	 * 获取实体文件对象
	 * 
	 * @return
	 */
	public File getFile()
	{
		if(saveDir == null || fileName == null)
		{
			return null;
		}
		else
		{
			return new File(saveDir + File.separator + fileName);
		}
	}

	public void moveToDirectory(File destDir) throws IOException
	{
		moveToDirectory(destDir, getFile().getName());
	}
	
	public void moveToDirectory(File destDir, String newFilename) throws IOException
	{
		if(destDir == null)
		{
			throw new NullPointerException("Destination directory must not be null!");
		}
		
		if(newFilename == null || newFilename.trim().length() == 0)
		{
			throw new NullPointerException("New file name must not be null!");
		}

		if(!destDir.exists())
		{
			destDir.mkdirs();
		}

		if(!destDir.exists())
		{
			throw new FileNotFoundException("Destination directory '" + destDir + "' does not exist!");
		}

		if(!destDir.isDirectory())
		{
			throw new IOException("Destination '" + destDir + "' is not a directory!");
		}

		File srcFile = getFile();
		if(srcFile == null || !srcFile.exists())
		{
			throw new FileNotFoundException("The original uploaded file does not exist!");
		}

		if(!newFilename.toLowerCase().endsWith("." + getExtension().toLowerCase()))
		{
			newFilename = newFilename + "." + getExtension();
		}
		
		File destFile = new File(destDir, newFilename);
		if(destFile.exists())
		{
			throw new IOException("Destination '" + destFile + "' already exists!");
		}

		FileInputStream fis = null;
		FileOutputStream fos = null;
		FileChannel input = null;
		FileChannel output = null;
		try
		{
			fis = new FileInputStream(srcFile);
			fos = new FileOutputStream(destFile);
			input = fis.getChannel();
			output = fos.getChannel();
			
			long size = input.size();
			long pos = 0L;
			long count = 0L;
			
			while(pos < size)
			{
				count = size - pos > 31457280L ? 31457280L : size - pos;
				pos += output.transferFrom(input, pos, count);
			}
			
		} finally
		{
			closeQuietly(output);
			closeQuietly(fos);
			closeQuietly(input);
			closeQuietly(fis);
		}

		if(srcFile.length() != destFile.length())
		{
			throw new IOException("Failed to copy full contents from '" + srcFile + "' to '" + destFile + "'!");
		}

		destFile.setLastModified(srcFile.lastModified());

		if(!srcFile.delete())
		{
			throw new IOException("Failed to delete original file '" + srcFile + "' after copy to '" + destFile + "'!");
		}
	}

	private static void closeQuietly(Closeable closeable)
	{
		try
		{
			if(closeable != null)
			{
				closeable.close();
			}
		} catch(IOException ioe)
		{
			// ignore
		}
	}

	@Override
	public String toString()
	{
		return String.format(
				"{\"fileName\":\"%s\", \"originalFileName\":\"%s\", \"contentType\":\"%s\", \"extension\":\"%s\"}",
				EscapeUtils.escapeJSON(this.fileName), EscapeUtils.escapeJSON(this.originalName), this.contentType,
				this.getExtension());
	}

}
