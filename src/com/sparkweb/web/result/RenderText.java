package com.sparkweb.web.result;

import com.sparkweb.web.Request;
import com.sparkweb.web.Response;

/**
 * 200 OK with a text/plain
 */
public class RenderText extends Result
{
	private static final long	serialVersionUID	= 3038920787343412978L;

	private String				text				= "";

	public RenderText(CharSequence _text) {
		if(_text != null)
		{
			this.text = _text.toString();
		}
	}

	@Override
	public void apply(Request request, Response response)
	{
		try
		{
			response.contentType("text/plain; charset=" + getEncoding());
			response.out().write(this.text.getBytes(getEncoding()));
		} catch(Exception e)
		{
			e.printStackTrace();
			//throw new UnexpectedException(e);
		}
	}

}
