package com.sparkweb.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Java Annotation注解读取
 * @author yswang
 * @version 1.0
 */
public class AnnotationReader
{
	private List<String> classAnnos;
	private Map<String, List<String>> fieldAnnos;
	private Map<String, List<String>> methodAnnos;
	private Map<String, List<String>> methodParamAnnos;
	
	public AnnotationReader(Class<?> cls) throws IOException
	{
		InputStream is = null;
		try
		{
			is = Thread.currentThread().getContextClassLoader().getResourceAsStream(cls.getCanonicalName().replace('.', '/') + ".class");
			reader(new ClassReader(is));
		} 
		finally {
			if(is != null)
			{
				is.close();
				is = null;
			}
		}

	}
	
	public AnnotationReader(ClassReader reader) 
	{
		reader(reader);
	}
	
	public void reader(ClassReader reader)
	{
		classAnnos = new ArrayList<String>(4);
		fieldAnnos = new HashMap<String, List<String>>();
		methodAnnos = new HashMap<String, List<String>>();
		methodParamAnnos = new HashMap<String, List<String>>();
		
		final ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
		reader.accept(new ClassVisitor(Opcodes.ASM4, cw) {
			/**
			 * 扫描类注解
			 */
			@Override
			public AnnotationVisitor visitAnnotation(String desc, boolean visible)
			{
				classAnnos.add(parseAnno(desc));
				return null;
			}

			/**
			 * 扫描方法注解
			 */
			@Override
			public MethodVisitor visitMethod(final int access, final String name, final String desc, 
					final String signature, final String[] exceptions)
			{
				MethodVisitor v = cw.visitMethod(access, name, desc, signature, exceptions);
			
				return new MethodVisitor(Opcodes.ASM4, v) {
					/**
					 * 扫描方法注解
					 */
					@Override
					public AnnotationVisitor visitAnnotation(String annoDesc, boolean visible)
					{
						List<String> annos = methodAnnos.get(name);
						if(annos == null)
						{
							annos = new ArrayList<String>(6);
							methodAnnos.put(name, annos);
						}
						
						methodAnnos.get(name).add(parseAnno(annoDesc));
						return null;
					}

					/**
					 * 扫描方法参数注解
					 */
					@Override
					public AnnotationVisitor visitParameterAnnotation(int paramterIndex, String annoDesc, boolean visible)
					{
						String key = name + "#" + paramterIndex;
						List<String> annos = methodParamAnnos.get(key);
						if(annos == null)
						{
							annos = new ArrayList<String>();
							methodParamAnnos.put(key, annos);
						}
						
						methodParamAnnos.get(key).add(parseAnno(annoDesc));
						return null;
					}
					
				};
			}

			/**
			 * 扫描字段上的注解
			 */
			@Override
			public FieldVisitor visitField(final int access, final String name, final String desc, 
					final String signature, final Object value)
			{
				FieldVisitor v = cw.visitField(access, name, desc, signature, value);
				return new FieldVisitor(Opcodes.ASM4, v) {
					@Override
					public AnnotationVisitor visitAnnotation(final String annoDesc, final boolean visible)
					{
						List<String> annos = fieldAnnos.get(name);
						if(annos == null)
						{
							annos = new ArrayList<String>(6);
							fieldAnnos.put(name, annos);
						}
						
						fieldAnnos.get(name).add(parseAnno(annoDesc));
						return null;
					}
					
				};
			}
			
		}, ClassReader.SKIP_DEBUG);
	}

	/**
	 * 将asm读取的注解类转换为标准的java注解类.<br>
	 * Ljava/lang/annotation/Annotation; --> java.lang.annotation.Annotation
	 * @param anno 待转换的asm读取的注解类（格式：Lxx/xx/Xxx;）
	 * @return
	 */
	private static String parseAnno(String anno)
	{
		if(anno == null || anno.trim().length() == 0)
		{
			return anno;
		}
		
		return anno.substring(1, anno.length()-1).replace('/', '.');
	}
	
	/**
	 * 获取所有注解（包括类注解，字段注解，方法注解，方法参数注解）
	 * @return
	 */
	public List<String> getAnnotations()
	{
		List<String> annos = new ArrayList<String>();
		annos.addAll(this.classAnnos);
		
		for(Iterator<List<String>> it = this.fieldAnnos.values().iterator(); it.hasNext();)
		{
			for(String fAnno : it.next())
			{
				if(!annos.contains(fAnno))
				{
					annos.add(fAnno);
				}
			}
		}
		
		for(Iterator<List<String>> it = this.methodAnnos.values().iterator(); it.hasNext();)
		{
			for(String mAnno : it.next())
			{
				if(!annos.contains(mAnno))
				{
					annos.add(mAnno);
				}
			}
		}
		
		for(Iterator<List<String>> it = this.methodParamAnnos.values().iterator(); it.hasNext();)
		{
			for(String mpAnno : it.next())
			{
				if(!annos.contains(mpAnno))
				{
					annos.add(mpAnno);
				}
			}
		}
		
		return annos;
	}
	
	
	/**
	 * 获取类上的注解信息
	 * @return
	 */
	public List<String> getClassAnnotations()
	{
		return this.classAnnos;
	}
	
	/**
	 * 获取字段上的注解信息
	 * @return
	 */
	public Map<String, List<String>> getFieldAnnotations()
	{
		return this.fieldAnnos;
	}
	
	/**
	 * 获取方法注解
	 * @return
	 */
	public Map<String, List<String>> getMethodAnnotations()
	{
		return this.methodAnnos;
	}
	
	public Map<String, List<String>> getMethodParameterAnnotations()
	{
		return this.methodParamAnnos;
	}
	
	public void clear()
	{
		this.classAnnos.clear();
		this.classAnnos = null;
		this.fieldAnnos.clear();
		this.fieldAnnos = null;
		this.methodAnnos.clear();
		this.methodAnnos = null;
		this.methodParamAnnos.clear();
		this.methodParamAnnos = null;
	}
}
