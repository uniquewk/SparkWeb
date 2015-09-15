package com.sparkweb.web.result;

import com.sparkweb.web.Request;
import com.sparkweb.web.Response;

/**
 * 200 OK with a text/xml
 */
public class RenderXml extends Result
{
	private static final long	serialVersionUID	= 3038920787343412978L;

	private String				xml				= "";

	public RenderXml(CharSequence _xml) {
		if(_xml != null)
		{
			this.xml = _xml.toString();
		}
	}

	@Override
	public void apply(Request request, Response response)
	{
		try
		{
			response.contentType("text/xml; charset=" + getEncoding());
			response.out().write(this.xml.getBytes(getEncoding()));
		} catch(Exception e)
		{
			e.printStackTrace();
			//throw new UnexpectedException(e);
		}
	}

}
