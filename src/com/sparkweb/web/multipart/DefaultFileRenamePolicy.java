package com.sparkweb.web.multipart;

import java.io.File;
import java.util.UUID;

/**
 * 上传文件重命名规则
 * 
 * @author yswang
 * @version 1.0
 */
public class DefaultFileRenamePolicy implements com.oreilly.servlet.multipart.FileRenamePolicy
{
	public File rename(File uploadFile)
	{
		String name = uploadFile.getName();

		int dot = name.lastIndexOf(".");
		String ext = dot != -1 ? name.substring(dot) : "";
		
		return new File(uploadFile.getParent(), UUID.randomUUID().toString() + ext);
	}

}
