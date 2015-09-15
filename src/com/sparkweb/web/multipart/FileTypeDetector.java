package com.sparkweb.web.multipart;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 使用文件起始的几个字节(魔数)来确定文件类型。
 * 
 * @author yswang
 * @version 1.0
 */
public final class FileTypeDetector
{
	public static boolean isImage(File file)
	{
		try
		{
			MagicMimeType mmt = getFileType(file);
			
			
		} catch(IOException e)
		{
			e.printStackTrace();
		}
		
		return false;
	}
	
	public static MagicMimeType getFileType(File file) throws IOException
	{
		String fileHead = getFileHeader(file);
		if(fileHead == null || fileHead.length() == 0)
		{
			return null;
		}
		
		fileHead = fileHead.toUpperCase();
		for(MagicMimeType mmt : MagicMimeType.values())
		{
			if(fileHead.startsWith(mmt.getValue()))
			{
				return mmt;
			}
		}
		
		return null;
	}
	
	private static String getFileHeader(File file) throws IOException
	{
		// 对文件的前28个字节进行读取，并将内容转换为十六进制
		byte[] bytes = new byte[28];
		InputStream is = new FileInputStream(file);
		is.read(bytes, 0, 28);
		is.close();
		
		return bytes2hex(bytes);
	}
	
	private static String bytes2hex(byte[] bytes)
	{
		if(bytes == null)
		{
			throw new IllegalArgumentException("The `bytes2hex(byte[] bytes)` method argument can not be null!");
		}
		
		if(bytes.length == 0)
		{
			return "";
		}
		
		StringBuilder hex = new StringBuilder();
		String tmp = null;
		
		for(int i = 0, len = bytes.length; i < len; i++)
		{
			tmp = Integer.toHexString(bytes[i] & 0xFF);
			if(tmp.length() == 1)
			{
				hex.append('0');
			}
			
			hex.append(tmp);
		}
		
		return hex.toString();
	}
	
	public static void main(String[] args) throws IOException
	{
		File dir = new File("e:\\magic_testsuites");
		for(File f : dir.listFiles())
		{
			System.out.println(f.getName() + " = " + getFileType(f));
		}
	}
}
