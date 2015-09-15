package com.sparkweb.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * 一些额外的特殊字符转义
 * 
 * @author yswang
 * @version 1.0
 */
public class EscapeUtils
{
	/**
	 * 转义JSON值中的特殊字符
	 * @param str
	 * @return
	 */
	public static String escapeJSON(String str)
	{
		if(str == null || str.trim().length() == 0)
		{
			return "";
		}

		int len = str.length();
		int i = 0;
		StringBuilder builder = new StringBuilder(len);
		for(; i < len; ++i)
		{
			char ch = str.charAt(i);
			switch(ch)
			{
				case '\"':
					builder.append("\\\"");
					break;
				case '\\':
					builder.append("\\\\");
					break;
				case '/':
					builder.append("\\/");
					break;
				case '\b':
					builder.append("\\b");
					break;
				case '\t':
					builder.append("\\t");
					break;
				case '\n':
					builder.append("\\n");
					break;
				case '\f':
					builder.append("\\f");
					break;
				case '\r':
					builder.append("\\r");
					break;
				default:
					builder.append(ch);
					break;
			}
		}
		
		return builder.toString();
	}
	
	/**
	 * 转义URL地址
	 * @param url
	 * @return
	 */
	public static String escapeURL(String url)
	{
		if(url == null || url.trim().length() == 0)
		{
			return "";
		}

		String result = null;
		try
		{
			result = URLEncoder.encode(url, "UTF-8");
		} catch(UnsupportedEncodingException ex)
		{
			throw new RuntimeException("UTF-8 not supported", ex);
		}

		return result;
	}

	/**
	 * 转义正则表达式字符串中的特殊字符
	 * @param regexStr
	 * @return
	 */
	public static String escapeRegex(String regexStr)
	{
		if(regexStr == null || regexStr.trim().length() == 0)
		{
			return "";
		}

		int len = regexStr.length();
		int i = 0;
		StringBuilder builder = new StringBuilder(len);
		for(; i < len; ++i)
		{
			char ch = regexStr.charAt(i);
			switch(ch)
			{
				case '.':
					builder.append("\\.");
					break;
				case '\\':
					builder.append("\\\\");
					break;
				case '?':
					builder.append("\\?");
					break;
				case '*':
					builder.append("\\*");
					break;
				case '+':
					builder.append("\\+");
					break;
				case '&':
					builder.append("\\&");
					break;
				case ':':
					builder.append("\\:");
					break;
				case '{':
					builder.append("\\{");
					break;
				case '}':
					builder.append("\\}");
					break;
				case '[':
					builder.append("\\[");
					break;
				case ']':
					builder.append("\\]");
					break;
				case '(':
					builder.append("\\(");
					break;
				case ')':
					builder.append("\\)");
					break;
				case '^':
					builder.append("\\^");
					break;
				case '$':
					builder.append("\\$");
					break;
				default:
					builder.append(ch);
					break;
			}
		}
		
		return builder.toString();
	}
	
}
