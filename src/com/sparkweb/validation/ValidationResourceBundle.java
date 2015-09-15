package com.sparkweb.validation;

import com.sparkweb.i18n.ResourceBundleLocator;

/**
 * 验证消息国际化处理
 * 
 * @author yswang
 * @version 1.0
 */
public final class ValidationResourceBundle
{
	private static final ResourceBundleLocator RESOURCE_BUNDLE = ResourceBundleLocator.getLocator("com.sparkweb.validation.ValidationMessages");
	
	protected static String getBundleMessage(String key)
	{
		if(key == null || key.trim().length() == 0)
		{
			return null;
		}
		
		return RESOURCE_BUNDLE.getString(key);
	}
	
}
