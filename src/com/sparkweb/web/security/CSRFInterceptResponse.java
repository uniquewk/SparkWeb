package com.sparkweb.web.security;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

/**
 * CSRF Intercepter
 * 
 * @author yswang
 * @version 1.0
 */
public class CSRFInterceptResponse extends HttpServletResponseWrapper
{
	private HttpServletResponse response = null;
	private HttpServletRequest request = null;
	private CsrfGuard csrfGuard = null;
	
	public CSRFInterceptResponse(HttpServletResponse res, HttpServletRequest req, CsrfGuard _csrfGuard) {
		super(res);
		this.response = res;
		this.request = req;
		this.csrfGuard = _csrfGuard;
	}

	@Override
	public String encodeRedirectURL(String url)
	{
		return wrapURL(super.encodeRedirectURL(url));
	}

	@Override
	public String encodeURL(String url)
	{
		return wrapURL(super.encodeURL(url));
	}

	@Override
	public void sendRedirect(String location) throws IOException
	{
		response.sendRedirect(wrapURL(location));
	}
	
	private String wrapURL(String url)
	{
		// Remove CR and LF characters to prevent CRLF injection
		String sanitizedLocation = url.replaceAll("(\\r|\\n|%0D|%0A|%0a|%0d)", "");
		
		/** ensure token included in redirects **/
		if (!sanitizedLocation.contains("://") && csrfGuard.isProtectedPageAndMethod(sanitizedLocation, "GET")) 
		{
			/** update tokens **/
			csrfGuard.updateTokens(request);
			
			StringBuilder sb = new StringBuilder();
			
			if(sanitizedLocation.charAt(0) != '/') 
			{
				sb.append(request.getContextPath() + "/" + sanitizedLocation);
			} 
			else {
				sb.append(sanitizedLocation);
			}
			
			if (sanitizedLocation.contains("?")) 
			{
				sb.append('&');
			} else {
				sb.append('?');
			}

			// remove any query parameters from the sanitizedLocation
			String locationUri = sanitizedLocation.split("\\?", 2)[0];

			sb.append(csrfGuard.getTokenName());
			sb.append('=');
			sb.append(csrfGuard.getTokenValue(request, locationUri));
			
			return sb.toString();
		} 
		
		return sanitizedLocation;
	}
	
}
