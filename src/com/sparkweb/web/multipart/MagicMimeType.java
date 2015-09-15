package com.sparkweb.web.multipart;

/**
 * 魔数部分文件类型定义
 * 
 * @author yswang
 * @version 1.0
 */
public enum MagicMimeType
{
	// TODO 定义魔数文件类型
	JPEG("FFD8FF"),
	JPG("FFD8FF"),
	PNG("89504E47"),
	GIF("47494638"),
	TIFF("49492A00"),
	BMP("424D"),
	ICO("00000100"),
	DWG("41433130"),
	PSD("38425053"),
	AI("25504446"),
	XML("3C3F786D6C"),
	HTML("68746D6C3E"),
	PDF("255044462D312E"),
	TXT("TODO"),
	CLASS("CAFEBABE"),
	MS97_2003("D0CF11E0"),
	DOC("D0CF11E0"),
	XLS("D0CF11E0"),
	PPT("D0CF11E0"),
	MS2007("504B0304140006"),
	DOCX("504B0304140006"),
	XLSX("504B0304140006"),
	PPTX("504B0304140006"),
	CHM("49545346"),
	EXE("4D5A"),
	COM("4D5A"),
	DLL("4D5A"),
	SYS("4D5A"),
	BAT("TODO"),
	ZIP("504B0304"),
	RAR("52617221"),
	TAR("TODO"),
	GZ("1F8B08"),
	_7Z("377ABCAF271C"),
	BZ2("425A68"),
	WAV("57415645"),
	AVI("41564920"),
	MP4("TODO"),
	SWF("TODO")
	;
	
	private String value = "";
	private MagicMimeType(String _value)
	{
		this.value = _value;
	}
	
	public String getValue()
	{
		return value;
	}
	
	public void setValue(String value)
	{
		this.value = value;
	}
	
}
