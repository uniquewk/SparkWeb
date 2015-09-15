package com.sparkweb.i18n;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Resource i18n Resolver <br>
 * Example: test/Resources.properties test/Resources_en_US.properties<br>
 * <pre>
 * <code>
 * Resource.properties:
 * 	hello=Hi,everybody. 
 * 	sum={0} + {1} = {2} 
 * 
 * ResourceBundleLocator resLocale = ResourceBundleLocator.getLocator("test.Resources");
 * resLocale.getString("hello") == Hi,everybody.  
 * resLocale.getString("sum", "1", "2", "3") == 1 + 2 = 3 
 * 
 * ResourceBundleLocator resLocale = ResourceBundleLocator.getLocator("test.Resources", Locale.US);
 * </code>
 * </pre>
 * 
 * @author yswang
 * @version 1.0
 * @since 2012-5-11
 */
public class ResourceBundleLocator
{
	private final ResourceBundle	bundle;
	private final Locale			locale;

	/**
	 * Creates a new ResourceLocaleManager for a given package. This is a
	 * private method and all access to it is arbitrated by the static
	 * getManager method call so that only one StringManager per package will be
	 * created.
	 * 
	 * @param packageResourceName Name of resources to create
	 *            ResourceLocaleManager for.
	 */
	private ResourceBundleLocator(String packageResourceName, Locale locale) {
		String bundleName = packageResourceName;
		ResourceBundle bnd = null;
		try
		{
			bnd = ResourceBundle.getBundle(bundleName, locale);
		} catch(MissingResourceException ex)
		{
			// Try from the current loader (that's the case for trusted apps)
			// Should only be required if using a TC5 style classloader
			// structure
			// where common != shared != server
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			if(cl != null)
			{
				try
				{
					bnd = ResourceBundle.getBundle(bundleName, locale, cl);
				} catch(MissingResourceException ex2)
				{
					ex2.printStackTrace();
				}
			}
		}

		bundle = bnd;
		// Get the actual locale, which may be different from the requested one
		if(bundle != null)
		{
			this.locale = bundle.getLocale();
		}
		else
		{
			this.locale = null;
		}
	}

	/**
	 * Get a string from the underlying resource bundle or return null if the
	 * String is not found.
	 * 
	 * @param key to desired resource String
	 * @return resource String matching <i>key</i> from underlying bundle or
	 *         null if not found.
	 */
	public String getString(String key)
	{
		if(key == null)
		{
			throw new IllegalArgumentException("key may not have a null value");
		}

		String str = null;

		try
		{
			// Avoid NPE if bundle is null and treat it like an MRE
			if(bundle != null)
			{
				str = bundle.getString(key);
			}
		} catch(MissingResourceException mre)
		{
			str = null;
		}

		return str;
	}

	/**
	 * Get a string from the underlying resource bundle and format it with the
	 * given set of arguments.
	 * 
	 * @param key
	 * @param args
	 */
	public String getString(final String key, final Object... args)
	{
		String value = getString(key);
		if(value == null)
		{
			value = key;
		}

		MessageFormat mf = new MessageFormat(value);
		mf.setLocale(locale);
		return mf.format(args, new StringBuffer(), null).toString();
	}

	/**
	 * Get a string from the underlying resource bundle and format it with the
	 * given Map<K,V> argument.
	 * 
	 * @param key
	 * @param paramMap
	 * @return
	 */
	public String getString(final String key, final Map<String, String> paramMap)
	{
		String str = getString(key);
		return formatString(str, paramMap, null);
	}

	/**
	 * Get the given String parameter in {..}
	 * @param str
	 * @return
	 */
	public static String[] parseStringParameters(String str)
	{
		List<String> storeKeys = new ArrayList<String>();
		formatString(str, null, storeKeys);
		
		return storeKeys.toArray(new String[storeKeys.size()]);
	}
	
	/**
	 * Identify the Locale this StringManager is associated with
	 */
	public Locale getLocale()
	{
		return locale;
	}

	// --------------------------------------------------------------
	// STATIC SUPPORT METHODS
	// --------------------------------------------------------------

	private static final Map<String, Map<Locale, ResourceBundleLocator>>	RES_LOCATORS	= new Hashtable<String, Map<Locale, ResourceBundleLocator>>();

	/**
	 * Get the ResourceLocaleManager for a particular package. If a manager for
	 * a package already exists, it will be reused, else a new
	 * ResourceLocaleManager will be created and returned.
	 * 
	 * @param packageResourceName The package name
	 */
	public static final synchronized ResourceBundleLocator getLocator(String packageResourceName)
	{
		return getLocator(packageResourceName, Locale.getDefault());
	}

	/**
	 * Get the ResourceLocaleManager for a particular package and Locale. If a
	 * manager for a package/Locale combination already exists, it will be
	 * reused, else a new ResourceLocaleManager will be created and returned.
	 * 
	 * @param packageResourceName The package name
	 * @param locale The Locale
	 */
	public static final synchronized ResourceBundleLocator getLocator(String packageResourceName, Locale locale)
	{
		Map<Locale, ResourceBundleLocator> map = RES_LOCATORS.get(packageResourceName);
		if(map == null)
		{
			map = new Hashtable<Locale, ResourceBundleLocator>();
			RES_LOCATORS.put(packageResourceName, map);
		}

		ResourceBundleLocator mgr = map.get(locale);
		if(mgr == null)
		{
			mgr = new ResourceBundleLocator(packageResourceName, locale);
			map.put(locale, mgr);
		}
		return mgr;
	}

	public static final String formatString(final String str, final Map<String, String> paramMap)
	{
		return formatString(str, paramMap, null);
	}
	
	public static final String formatString(final String formatStr, final Map<String, String> paramMap, final List<String> storeKeys)
	{
//		int maxOffset = -1;
		StringBuffer[] segments = new StringBuffer[4];
		for(int i = 0; i < segments.length; ++i)
		{
			segments[i] = new StringBuffer();
		}
		int part = 0;
//		int formatNumber = 0;
		boolean inQuote = false;
		int braceStack = 0;
//		maxOffset = -1;
		for(int i = 0; i < formatStr.length(); ++i)
		{
			char ch = formatStr.charAt(i);
			if(part == 0)
			{
				if(ch == '\'')
				{
					if(i + 1 < formatStr.length() && formatStr.charAt(i + 1) == '\'')
					{
						segments[part].append(ch); // handle doubles
						++i;
					}
					else
					{
						inQuote = !inQuote;
					}
				}
				else if(ch == '{' && !inQuote)
				{
					part = 1;
				}
				else
				{
					segments[part].append(ch);
				}
			}
			else if(inQuote)
			{ // just copy quotes in parts
				segments[part].append(ch);
				if(ch == '\'')
				{
					inQuote = false;
				}
			}
			else
			{
				switch(ch)
				{
					case ',':
						if(part < 3) part += 1;
						else segments[part].append(ch);
						break;
					case '{':
						++braceStack;
						segments[part].append(ch);
						break;
					case '}':
						if(braceStack == 0)
						{
							part = 0;
							makeFormat(segments, paramMap, storeKeys);
//							formatNumber++;
						}
						else
						{
							--braceStack;
							segments[part].append(ch);
						}
						break;
					case '\'':
						inQuote = true;
						// fall through, so we keep quotes in other parts
					default:
						segments[part].append(ch);
						break;
				}
			}
		}
		
		if(braceStack == 0 && part != 0)
		{
//			maxOffset = -1;
			throw new IllegalArgumentException("Unmatched braces in the pattern.");
		}

		return segments[0].toString();
	}
	
	private static void makeFormat(StringBuffer[] segments, Map<String, String> paramMap, List<String> storeKeys)
	{
		String param = segments[1].toString();
		String val = null;
		
		if(paramMap != null && paramMap.containsKey(param.trim()))
		{
			val = paramMap.get(param.trim());
		} else {
			val = "{"+ param +"}";
		}
		segments[0].append(val);
		segments[1].setLength(0);
		segments[2].setLength(0);
		segments[3].setLength(0);
		
		if(storeKeys != null)
		{
			storeKeys.add(param.trim());
		}
	}
	
}
