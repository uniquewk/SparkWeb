package com.sparkweb.scanner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sparkweb.scanner.criteria.ClassCriteria;
import com.sparkweb.scanner.locater.ClassLocater;
import com.sparkweb.scanner.locater.ClasspathLocater;
import com.sparkweb.scanner.locater.WebappClassLocater;
import com.sparkweb.util.Matcher;

/**
 * Class类扫描器 用于扫描符合要求的类
 * 
 * @author yswang
 * @version 1.0
 */
public final class ClassScanner
{
	private static final Log LOG = LogFactory.getLog(ClassScanner.class);
	
	private final static String			CLASS_EXT				= ".class";
	private final static String			JAR_EXT					= ".jar";
	
	// 计数共扫描了多少个class文件
	private int counter = 1;
	
	/**
	 * 默认忽略扫描的jar包
	 */
	private static final Set<String>	DEFAULT_JARS_TO_SKIP	= new HashSet<String>();

	/**
	 * 默认忽略扫描的报名
	 */
	private static final Set<String>	DEFAULT_PKGS_TO_SKIP	= new HashSet<String>();

	/**
	 * 存放扫描到的符合匹配条件的所有类
	 */
	private Set<Class<?>>				classes					= null;

	/**
	 * 类条件规则匹配器
	 */
	private List<ClassCriteria>			classCriterias			= null;

	/**
	 * 忽略扫描的jar包，支持通配符
	 */
	private Set<String>					jarsToSkip				= null;
	private Set<String[]>				ignoredJarsTokens		= null;

	/**
	 * 忽略扫描的包，支持通配符
	 */
	private Set<String>					packagesToSkip			= null;
	private Set<String[]>				ignoredPkgsTokens		= null;

	public ClassScanner() {
		classCriterias = new ArrayList<ClassCriteria>();
		jarsToSkip = new HashSet<String>();
		packagesToSkip = new HashSet<String>();

		classes = new HashSet<Class<?>>(50);
	}

	/**
	 * 扫描当前整个classpath下的class文件
	 */
	public Set<Class<?>> scanClasspath()
	{
		initSkipFilters();
		
		long stime = System.currentTimeMillis();
		ClassLocater locater = new ClasspathLocater();
		scanLocations(locater);
		
		LOG.info(String.format("ClassScanner: --- ClassScanner total scan %d class files, cost: %d ms.", counter, System.currentTimeMillis()-stime));
		
		return classes;
	}

	/**
	 * 扫描当前web应用下的类
	 */
	public Set<Class<?>> scanWebapp(ServletContext servletContext)
	{
		initSkipFilters();
		
		long stime = System.currentTimeMillis();
		ClassLocater locater = new WebappClassLocater(servletContext);
		scanLocations(locater);
		
		LOG.info(String.format("ClassScanner: --- ClassScanner total scan %d class files, cost: %d ms.", counter, System.currentTimeMillis()-stime));
		
		return classes;
	}

	/**
	 * 注册新的类匹配器
	 * 
	 * @param criteria
	 */
	public void addClassCriteria(ClassCriteria criteria)
	{
		if(criteria == null)
		{
			return;
		}

		this.classCriterias.add(criteria);
	}

	/**
	 * 添加忽略扫描的jar包
	 * 
	 * @param jarFileName jar包名称，支持通配符
	 */
	public void addJarToSkip(String... jarFileName)
	{
		if(jarFileName != null)
		{
			for(String jarName : jarFileName)
			{
				this.jarsToSkip.add(jarName);
			}
		}
	}

	/**
	 * 添加忽略扫描的包名
	 * 
	 * @param packeagename 包名，支持通配符
	 */
	public void addPackageToSkip(String... packeageName)
	{
		if(packeageName != null)
		{
			for(String pkgName : packeageName)
			{
				this.packagesToSkip.add(pkgName);
			}
		}
	}

	private void scanLocations(ClassLocater classLocater)
	{
		Set<URL> locations = classLocater.getClassLocations();
		if(locations == null || locations.isEmpty())
		{
			return;
		}

		String jarName = null;
		for(URL url : locations)
		{
			jarName = getJarName(url);
			if(jarName != null && Matcher.matchPath(ignoredJarsTokens, jarName))
			{
				if(LOG.isDebugEnabled())
				{
					LOG.debug(String.format("ClassScanner: --- Skip to scan jar <%s>!", jarName));
				}
				continue;
			}

			try
			{
				URLConnection conn = url.openConnection();
				if(conn instanceof JarURLConnection)
				{
					scanJar((JarURLConnection) conn);
				}
				else
				{
					String urlStr = url.toString();
					if(urlStr.startsWith("file:") || urlStr.startsWith("jndi:") 
							|| urlStr.startsWith("http:") || urlStr.startsWith("https:"))
					{
						if(urlStr.endsWith(JAR_EXT))
						{
							URL jarURL = new URL("jar:" + urlStr + "!/");
							scanJar((JarURLConnection) jarURL.openConnection());
						}
						else
						{
							File f = null;
							try
							{
								f = new File(url.toURI());
								// f.getName().indexOf("$") == -1 表示不解析内部类
								if(f.isFile() && f.getName().endsWith(CLASS_EXT) && f.getName().indexOf("$") == -1)
								{
									processClassStream(new FileInputStream(f), null);
								}
								else if(f.isDirectory())
								{
									// 这里要注意下:
									// 如果是通过ClasspathLocater扫描，则对于WEB-INF/classes 是以 classes为第一层目录
									//      这里就需要 scanDirectory(f, "");
									//
									// 如果是通过WebappClassLocater扫描，则如果采用 ServletContext.getResourcePaths("/WEB-INF/classes")
									// ，对于 WEB-INF/classes 是以classes/下的子目录为第一层目录
									//      这里就需要 scanDirectory(f, f.getName() + "."); 否则会丢失第一层包目录造成
									//
									// 为了统一，WebappClassLocater 采用 ServletContext.getRealPath("/WEB-INF/classes") to URL方式
									scanDirectory(f, "");
								}
							} catch(Throwable e)
							{
								LOG.warn(String.format("ClassScanner: --- Failed to scan <%s>, and skip it! Caused by: %s", url.toString(), e.getMessage()));
								// ignore
							}
						}
					}
				}

			} catch(Throwable e1)
			{
				LOG.warn(String.format("ClassScanner: --- Failed to scan <%s>, and skip it! Caused by: %s", url.toString(), e1.getMessage()));
				// ignore
			}

		}

	}

	private void scanDirectory(File dir, String packagename)
	{
		if(Matcher.matchPath(ignoredPkgsTokens, packagename))
		{
			if(LOG.isDebugEnabled())
			{
				LOG.debug(String.format("ClassScanner: --- Skip to scan pageckage <%s>!", packagename));
			}
			return;
		}

		File file = null;
		String filename = null;

		File[] files = dir.listFiles();
		if(files == null || files.length == 0)
		{
			return;
		}
		
		final int size = files.length;
		
		for(int i = 0; i < size; ++i)
		{
			file = files[i];
			filename = file.getName();
			
			if(file.isFile())
			{
				if(filename.toLowerCase(Locale.US).endsWith(JAR_EXT))
				{
					try
					{
						scanJar(file);
					} catch(Throwable e)
					{
						LOG.warn(String.format("ClassScanner: ---  Failed to scan <%s>, and skip it! Caused by: %s", file.getPath(), e.getMessage()));
						// ignore
					} 
					continue;
				}
				
				if(!filename.toLowerCase(Locale.US).endsWith(CLASS_EXT) || filename.indexOf("$") != -1)
				{
					continue;
				}

				filename = filename.substring(0, filename.lastIndexOf("."));

				try
				{
					processClassStream(new FileInputStream(file), packagename + filename);
				} 
				// 这是要使用 Throwable 代替 ClassNotFound，因为 ClassNotFound会终止程序
				catch(Throwable e) 
				{
					LOG.warn(String.format("ClassScanner: ---  Failed to scan class <%s>, and skip it! Caused by: %s", file.getName(), e.getMessage()));
					// ignore
				}

			}
			else if(file.isDirectory())
			{
				scanDirectory(file, packagename + filename + ".");
			}
		}

	}

	/**
	 * 扫描jar File 文件
	 * @param file File对象jar文件
	 */
	private void scanJar(File file)
	{
		if(file == null || !file.exists())
		{
			return;
		}
		
		try
		{
			scanJar(new JarFile(file));
		} catch(Throwable e)
		{
			LOG.warn(String.format("ClassScanner: ---  Failed to scan jar <%s>, skip it! Caused by: %s", file.getName(), e.getMessage()));
		}
	}
	
	private void scanJar(JarURLConnection jarConn)
	{
		if(jarConn == null)
		{
			return;
		}
		
		try
		{
			scanJar(jarConn.getJarFile());
		} catch(Throwable e)
		{
			LOG.warn(String.format("ClassScanner: ---  Failed to scan jar <%s>, skip it! Caused by: %s", jarConn.toString(), e.getMessage()));
		}
	}
	
	/**
	 * 扫描jar包中的class类
	 * 
	 * @param jarConn
	 */
	private void scanJar(JarFile jarFile)
	{
		if(jarFile == null)
		{
			return;
		}
		
		JarEntry entry = null;
		String entryName = null;

		try
		{
			Enumeration<JarEntry> entries = jarFile.entries();
			while(entries.hasMoreElements())
			{
				entry = entries.nextElement();
				entryName = entry.getName();

				if(Matcher.matchPath(ignoredPkgsTokens,
						entryName.substring(0, entryName.lastIndexOf("/") + 1).replace("/", "."))
						|| !entryName.toLowerCase(Locale.US).endsWith(CLASS_EXT) || entryName.indexOf("$") != -1)
				{
					if(LOG.isDebugEnabled())
					{
						LOG.debug(String.format("ClassScanner: --- Skip to scan <%s>!", entryName));
					}
					continue;
				}

				entryName = entryName.substring(0, entryName.lastIndexOf(".")).replace('/', '.');
				try
				{
					processClassStream(jarFile.getInputStream(entry), entryName);
				} 
				// 这是要使用 Throwable 代替 ClassNotFound，因为 ClassNotFound会终止程序
				catch(Throwable e) 
				{
					LOG.warn(String.format("ClassScanner: ---  Failed to scan class <%s>, skip it! Caused by: %s", entry.getName(), e.getMessage()));
					// ignore
				}
			}

		} catch(Throwable e)
		{
			LOG.warn(String.format("ClassScanner: ---  Failed to scan jar <%s>, skip it! Caused by: %s", jarFile.getName(), e.getMessage()));
			// ignore
		} finally
		{
			if(jarFile != null)
			{
				try
				{
					jarFile.close();
				} catch(Throwable e)
				{
					// ignore
				}
			}
		}
	}

	/**
	 * 负责最终的class文件解析
	 * @param is
	 * @param classname
	 */
	private void processClassStream(InputStream is, String classname)
	{
		counter++;
		try
		{	
			org.objectweb.asm.ClassReader clazzReader = new org.objectweb.asm.ClassReader(is);
			// 进行class过滤，只保存需要的class
			for(ClassCriteria criteria : this.classCriterias)
			{
				if(criteria.fitsCriteria(clazzReader, classname))
				{
					this.classes.add(Class.forName(classname, false, ClassScanner.class.getClassLoader()));
				}
			}
			
		} catch(Throwable e)
		{
			LOG.warn(String.format("ClassScanner: ---  Failed to process class <%s>, skip it! Caused by: %s", classname, e.getMessage()));
			// ignore
		} finally
		{
			if(is != null)
			{
				try
				{
					is.close();
				} catch(IOException e)
				{
					// ignore
				}
			}
		}
	}

	private void initSkipFilters()
	{
		ignoredJarsTokens = null;
		ignoredJarsTokens = new HashSet<String[]>();
		if(jarsToSkip.isEmpty())
		{
			jarsToSkip = DEFAULT_JARS_TO_SKIP;
		}

		for(String pattern : jarsToSkip)
		{
			ignoredJarsTokens.add(Matcher.tokenizePathAsArray(pattern));
		}

		ignoredPkgsTokens = null;
		ignoredPkgsTokens = new HashSet<String[]>();

		if(packagesToSkip.isEmpty())
		{
			packagesToSkip = DEFAULT_PKGS_TO_SKIP;
		}

		for(String pkg_pattern : packagesToSkip)
		{
			ignoredPkgsTokens.add(Matcher.tokenizePathAsArray(pkg_pattern));
		}
	}

	private String getJarName(URL url)
	{
		String name = null;
		String path = url.getPath();

		if(path.endsWith("!/"))
		{
			path = path.substring(0, path.length() - 2);
		}

		if(path.endsWith(JAR_EXT))
		{
			name = path.substring(path.lastIndexOf('/') + 1);
		}

		return name;
	}

}
