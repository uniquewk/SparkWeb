package com.sparkweb.web.security;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author yswang
 * @version 1.0
 */
public final class CsrfGuard
{
	public static final String	CSRF_TOKEN_NAME	= "csrf_token";

	public final static String	PAGE_TOKENS_KEY	= "csrfguard_pages_tokens_key";

	public String getTokenName()
	{
		return CSRF_TOKEN_NAME;
	}

	public String getTokenValue(HttpServletRequest request, String uri)
	{
		String tokenValue = null;
		HttpSession session = request.getSession(false);

		if(session != null)
		{
			if(isTokenPerPageEnabled())
			{
				@SuppressWarnings("unchecked")
				Map<String, String> pageTokens = (Map<String, String>) session.getAttribute(PAGE_TOKENS_KEY);

				if(pageTokens != null)
				{
					if(isTokenPerPagePrecreate())
					{
						createPageToken(pageTokens, uri);
					}

					tokenValue = pageTokens.get(uri);
				}
			}

			if(tokenValue == null)
			{
				tokenValue = (String) session.getAttribute(CSRF_TOKEN_NAME);
			}
		}

		return tokenValue;
	}

	public void updateToken(HttpSession session)
	{
		String tokenValue = (String) session.getAttribute(CSRF_TOKEN_NAME);

		/** Generate a new token and store it in the session. **/
		if(tokenValue == null)
		{
			try
			{
				tokenValue = TokenGenerator.genRandomToken();
			} catch(Exception e)
			{
				throw new RuntimeException(String.format("unable to generate the random token - %s",
						e.getLocalizedMessage()), e);
			}

			session.setAttribute(CSRF_TOKEN_NAME, tokenValue);
		}
	}

	public void updateTokens(HttpServletRequest request)
	{
		/** cannot create sessions if response already committed **/
		HttpSession session = request.getSession(false);

		if(session != null)
		{
			/** create master token if it does not exist **/
			updateToken(session);

			/** create page specific token **/
			if(isTokenPerPageEnabled())
			{
				@SuppressWarnings("unchecked")
				Map<String, String> pageTokens = (Map<String, String>) session.getAttribute(PAGE_TOKENS_KEY);

				/** first time initialization **/
				if(pageTokens == null)
				{
					pageTokens = new HashMap<String, String>();
					session.setAttribute(PAGE_TOKENS_KEY, pageTokens);
				}

				/** create token if it does not exist **/
				if(isProtectedPageAndMethod(request))
				{
					createPageToken(pageTokens, request.getRequestURI());
				}
			}
		}
	}

	/**
	 * Create page token if it doesn't exist.
	 * 
	 * @param pageTokens A map of tokens. If token doesn't exist it will be
	 *            added.
	 * @param uri The key for the tokens.
	 */
	private void createPageToken(Map<String, String> pageTokens, String uri)
	{
		if(pageTokens == null)
		{
			return;
		}

		/** create token if it does not exist **/
		if(pageTokens.containsKey(uri))
		{
			return;
		}

		try
		{
			pageTokens.put(uri, TokenGenerator.genRandomToken());
		} catch(Exception e)
		{
			throw new RuntimeException(String.format("unable to generate the random token - %s",
					e.getLocalizedMessage()), e);
		}
	}

	public boolean isProtectedPageAndMethod(String page, String method)
	{
		return true;
	}

	public boolean isValidRequest(HttpServletRequest request, HttpServletResponse response) throws CsrfGuardException
	{
		boolean valid = !isProtectedPageAndMethod(request);
		HttpSession session = request.getSession(true);
		String tokenFromSession = (String) session.getAttribute(CSRF_TOKEN_NAME);

		/** sending request to protected resource - verify token **/
		if(tokenFromSession != null && !valid)
		{
			if(isAjaxEnabled() && isAjaxRequest(request))
			{
				verifyAjaxToken(request);
			}
			else if(isTokenPerPageEnabled())
			{
				verifyPageToken(request);
			}
			else
			{
				verifySessionToken(request);
			}

			valid = true;

			/** rotate session and page tokens **/
			if(!isAjaxRequest(request) && isRotateEnabled())
			{
				rotateTokens(request);
			}
			/** expected token in session - bad state and not valid **/
		}
		else if(tokenFromSession == null && !valid)
		{
			throw new CsrfGuardException("CsrfGuard expects the token to exist in session at this point");
		}
		else
		{
			/** unprotected page - nothing to do **/
		}

		return valid;
	}

	private void rotateTokens(HttpServletRequest request)
	{
		HttpSession session = request.getSession(true);

		/** rotate master token **/
		String tokenFromSession = null;

		try
		{
			tokenFromSession = TokenGenerator.genRandomToken();
		} catch(Exception e)
		{
			throw new RuntimeException(String.format("unable to generate the random token - %s",
					e.getLocalizedMessage()), e);
		}

		session.setAttribute(CSRF_TOKEN_NAME, tokenFromSession);

		/** rotate page token **/
		if(isTokenPerPageEnabled())
		{
			@SuppressWarnings("unchecked")
			Map<String, String> pageTokens = (Map<String, String>) session.getAttribute(CsrfGuard.PAGE_TOKENS_KEY);

			try
			{
				pageTokens.put(request.getRequestURI(), TokenGenerator.genRandomToken());
			} catch(Exception e)
			{
				throw new RuntimeException(String.format("unable to generate the random token - %s",
						e.getLocalizedMessage()), e);
			}
		}
	}

	private boolean isRotateEnabled()
	{
		return true;
	}

	private boolean isTokenPerPageEnabled()
	{
		return true;
	}

	private boolean isTokenPerPagePrecreate()
	{
		return true;
	}

	private boolean isProtectedPageAndMethod(HttpServletRequest request)
	{
		return isProtectedPageAndMethod(request.getRequestURI(), request.getMethod());
	}

	private boolean isAjaxEnabled()
	{
		return true;
	}

	private boolean isAjaxRequest(HttpServletRequest request)
	{
		return request.getHeader("X-Requested-With") != null;
	}

	private void verifyAjaxToken(HttpServletRequest request) throws CsrfGuardException
	{
		HttpSession session = request.getSession(true);
		String tokenFromSession = (String) session.getAttribute(CSRF_TOKEN_NAME);
		String tokenFromRequest = request.getHeader(getTokenName());

		if(tokenFromRequest == null)
		{
			/** FAIL: token is missing from the request **/
			throw new CsrfGuardException("required token is missing from the request");
		}
		else
		{
			// if there are two headers, then the result is comma separated
			if(!tokenFromSession.equals(tokenFromRequest))
			{
				if(tokenFromRequest.contains(","))
				{
					tokenFromRequest = tokenFromRequest.substring(0, tokenFromRequest.indexOf(',')).trim();
				}
				
				if(!tokenFromSession.equals(tokenFromRequest))
				{
					/** FAIL: the request token does not match the session token **/
					throw new CsrfGuardException("request token does not match session token");
				}
			}
		}
	}

	private void verifyPageToken(HttpServletRequest request) throws CsrfGuardException
	{
		HttpSession session = request.getSession(true);
		@SuppressWarnings("unchecked")
		Map<String, String> pageTokens = (Map<String, String>) session.getAttribute(PAGE_TOKENS_KEY);

		String tokenFromPages = (pageTokens != null ? pageTokens.get(request.getRequestURI()) : null);
		String tokenFromSession = (String) session.getAttribute(CSRF_TOKEN_NAME);
		String tokenFromRequest = request.getParameter(getTokenName());

		if(tokenFromRequest == null)
		{
			/** FAIL: token is missing from the request **/
			throw new CsrfGuardException("required token is missing from the request");
		}
		else if(tokenFromPages != null)
		{
			if(!tokenFromPages.equals(tokenFromRequest))
			{
				/** FAIL: request does not match page token **/
				throw new CsrfGuardException("request token does not match page token");
			}
		}
		else if(!tokenFromSession.equals(tokenFromRequest))
		{
			/** FAIL: the request token does not match the session token **/
			throw new CsrfGuardException("request token does not match session token");
		}
	}

	private void verifySessionToken(HttpServletRequest request) throws CsrfGuardException
	{
		HttpSession session = request.getSession(true);
		String tokenFromSession = (String) session.getAttribute(CSRF_TOKEN_NAME);
		String tokenFromRequest = request.getParameter(getTokenName());

		if(tokenFromRequest == null)
		{
			/** FAIL: token is missing from the request **/
			throw new CsrfGuardException("required token is missing from the request");
		}
		else if(!tokenFromSession.equals(tokenFromRequest))
		{
			/** FAIL: the request token does not match the session token **/
			throw new CsrfGuardException("request token does not match session token");
		}
	}

	private CsrfGuard() {
	}

	public static CsrfGuard getInstance()
	{
		return SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		public static final CsrfGuard	INSTANCE	= new CsrfGuard();
	}

	/**
	 * CSRF Token生成器
	 */
	private static class TokenGenerator
	{
		private static final SecureRandom	RANDOM	= new SecureRandom();

		/**
		 * 生成一个随机数Token
		 */
		protected static String genRandomToken()
		{
			byte[] random = new byte[16];
			RANDOM.nextBytes(random);

			StringBuilder buffer = new StringBuilder();
			for(int j = 0; j < random.length; j++)
			{
				byte b1 = (byte) ((random[j] & 0xF0) >> 4);
				byte b2 = (byte) (random[j] & 0xF);
				if(b1 < 10)
				{
					buffer.append((char) (48 + b1));
				}
				else
				{
					buffer.append((char) (65 + (b1 - 10)));
				}

				if(b2 < 10)
				{
					buffer.append((char) (48 + b2));
				}
				else
				{
					buffer.append((char) (65 + (b2 - 10)));
				}
			}

			return buffer.toString();
		}
	}

}
