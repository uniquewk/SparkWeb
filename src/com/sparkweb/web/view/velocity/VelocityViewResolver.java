package com.sparkweb.web.view.velocity;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.tools.view.ServletUtils;

import com.sparkweb.exception.SparkException;
import com.sparkweb.web.Request;
import com.sparkweb.web.Response;
import com.sparkweb.web.SparkConfig;
import com.sparkweb.web.view.ViewResolver;

/**
 * Apache Velocity Template Engine View Resolver
 * 
 * @author yswang
 * @version 1.0
 */
public abstract class VelocityViewResolver implements ViewResolver
{
	private static final Log	log								= LogFactory.getLog(VelocityViewResolver.class);

	protected static final String VIEW_SUFFIX						= ".vm";

	/**
	 * The velocity.properties key for specifying the servlet's error template.
	 */
	protected static final String	PROPERTY_ERROR_TEMPLATE			= "tools.view.servlet.error.template";

	/**
	 * The velocity.properties key for specifying the relative directory holding
	 * layout templates.
	 */
	protected static final String	PROPERTY_LAYOUT_DIR				= "tools.view.servlet.layout.directory";

	/**
	 * The velocity.properties key for specifying the servlet's default layout
	 * template's filename.
	 */
	protected static final String	PROPERTY_DEFAULT_LAYOUT			= "tools.view.servlet.layout.default.template";

	/**
	 * The default layout directory
	 */
	protected static final String	DEFAULT_LAYOUT_DIR				= "/WEB-INF/views/layout/";

	/**
	 * The default filename for the servlet's default layout
	 */
	protected static final String	DEFAULT_DEFAULT_LAYOUT			= "default";

	/**
	 * The default error template's filename.
	 */
	protected static final String	DEFAULT_ERROR_TEMPLATE			= "error";
	
	/**
	 * The context key that will hold the content of the screen. This key
	 * ($screen_content) must be present in the layout template for the current
	 * screen to be rendered.
	 */
	protected static final String	KEY_SCREEN_CONTENT				= "screen_content";

	/**
	 * The context/parameter key used to specify an alternate layout to be used
	 * for a request instead of the default layout.
	 */
	protected static final String	KEY_LAYOUT						= "layout";

	/**
	 * The context key that holds the {@link Throwable} that broke the rendering
	 * of the requested screen.
	 */
	protected static final String	KEY_ERROR_CAUSE					= "error_cause";

	/**
	 * The context key that holds the stack trace of the error that broke the
	 * rendering of the requested screen.
	 */
	protected static final String	KEY_ERROR_STACKTRACE			= "stack_trace";

	/**
	 * The context key that holds the {@link MethodInvocationException} that
	 * broke the rendering of the requested screen. If this value is placed in
	 * the context, then $error_cause will hold the error that this invocation
	 * exception is wrapping.
	 */
	protected static final String	KEY_ERROR_INVOCATION_EXCEPTION	= "invocation_exception";

	protected String			viewDir;
	protected String			layoutDir;
	protected String			defaultLayout;
	protected String			errorTemplate;

	protected VelocityView		view;

	protected VelocityViewResolver() {
		String v_properties = velocityConfigLocation();
		String v_tools = velocityToolsConfigLocation();
		
		if(v_properties != null)
		{
			SparkConfig.getConfig().servletContext().setInitParameter(VelocityView.PROPERTIES_KEY, v_properties);
		}
		
		if(v_tools != null)
		{
			SparkConfig.getConfig().servletContext().setInitParameter(VelocityView.TOOLS_KEY, v_tools);
		}

		this.view = new VelocityView(SparkConfig.getConfig().servletContext());

		this.viewDir = viewDirectory();
		if(this.viewDir == null)
		{
			throw new NullPointerException("The VelocityViewRender `viewDirectory` must not be null!");
		}
		
		if(this.viewDir.charAt(this.viewDir.length() - 1) != '/')
		{
			this.viewDir = this.viewDir + '/';
		}
		
		// check for default template path overrides
		this.layoutDir = getVelocityProperty(PROPERTY_LAYOUT_DIR, DEFAULT_LAYOUT_DIR);
		this.defaultLayout = getVelocityProperty(PROPERTY_DEFAULT_LAYOUT, DEFAULT_DEFAULT_LAYOUT);
		this.errorTemplate = getVelocityProperty(PROPERTY_ERROR_TEMPLATE, DEFAULT_ERROR_TEMPLATE);
		if(!this.errorTemplate.endsWith(viewSuffix()))
		{
			this.errorTemplate = this.errorTemplate + viewSuffix();
		}

		// preventive error checking! directory must end in /
		if(this.layoutDir.charAt(this.layoutDir.length() - 1) != '/')
		{
			this.layoutDir += '/';
		}

		// for efficiency's sake, make defaultLayout a full path now
		this.defaultLayout = this.layoutDir + this.defaultLayout;
		if(!this.defaultLayout.endsWith(viewSuffix()))
		{
			this.defaultLayout = this.defaultLayout + viewSuffix();
		}
	}

	/**
	 * `velocity.properties` configuration location
	 * 
	 * @return
	 */
	public abstract String velocityConfigLocation();

	/**
	 * `velocity-tools.xml` configuration location
	 * 
	 * @return
	 */
	public abstract String velocityToolsConfigLocation();
	

	@Override
	public String viewSuffix()
	{
		return VIEW_SUFFIX;
	}

	/* (non-Javadoc)
	 * @see com.sparkweb.web.view.ViewRender#render(java.lang.String, com.sparkweb.web.Request, com.sparkweb.web.Response)
	 */
	public void render(String view, final Request request, final Response response)
	{
		Context context = null;
		try
		{
			if(view.charAt(0) == '/') 
			{
				view = view.substring(1);
			}
			
			if(!view.endsWith(viewSuffix()))
			{
				view = view + viewSuffix();
			}
			
			// then get a context
			context = createContext(request.servletRequest(), response.httpServletResponse());

			// call standard extension point
			fillContext(context, request.servletRequest());

			setContentType(request.servletRequest(), response.httpServletResponse());

			// get the template
			Template template = getTemplate(viewDir + view);

			// merge the template and context into the response
			mergeTemplate(template, context, response.httpServletResponse());
			
		} catch(IOException e)
		{
			error(request.servletRequest(), response.httpServletResponse(), e);
			throw new SparkException(e);
		} catch(ResourceNotFoundException e)
		{
			try
			{
				manageResourceNotFound(request.servletRequest(), response.httpServletResponse(), e);
			} catch(IOException e1)
			{
				throw new SparkException(e1);
			}
		} catch(RuntimeException e)
		{
			error(request.servletRequest(), response.httpServletResponse(), e);
			throw new SparkException(e);
		} finally
		{
			requestCleanup(request.servletRequest(), response.httpServletResponse(), context);
		}
	}

	private VelocityView getVelocityView()
	{
		return this.view;
	}

	private String getVelocityProperty(String name, String alternate)
	{
		return getVelocityView().getProperty(name, alternate);
	}

	private void setContentType(HttpServletRequest request, HttpServletResponse response)
	{
		response.setContentType(getVelocityView().getDefaultContentType());
	}

	private Context createContext(HttpServletRequest request, HttpServletResponse response)
	{
		return getVelocityView().createContext(request, response);
	}
	
	private void fillContext(Context context, HttpServletRequest request)
	{
		Object layout = request.getAttribute(KEY_LAYOUT);
		
		if(layout != null)
		{
			context.put(KEY_LAYOUT, layout.toString());
		}
	}

	private Template getTemplate(String name)
	{
		return getVelocityView().getTemplate(name);
	}
	
	private void mergeTemplate(Template template, Context context, HttpServletResponse response) throws IOException
	{
		// Render the screen_content
		StringWriter sw = new StringWriter();
		template.merge(context, sw);
		// Add the resulting content to the context
		context.put(KEY_SCREEN_CONTENT, sw.toString());

		// Check for an alternate layout
		//
		// we check after merging the screen template so the screen
		// can overrule any layout set in the request parameters
		// by doing #set( $layout = "MyLayout.vm" )
		Object obj = context.get(KEY_LAYOUT);
		String layout = (obj == null) ? null : obj.toString();
		
		if(layout == null)
		{
			// no alternate, use default
			layout = this.defaultLayout;
		}
		else
		{
			// make it a full(er) path
			layout = this.layoutDir + layout;
		}

		if(!layout.endsWith(viewSuffix()))
		{
			layout = layout + viewSuffix();
		}
		
		try
		{
			// load the layout template
			template = getTemplate(layout);
		} catch(Exception e)
		{
			log.error("Can't load layout \"" + layout + "\"", e);

			// if it was an alternate layout we couldn't get...
			if(!layout.equals(this.defaultLayout))
			{
				// try to get the default layout
				// if this also fails, let the exception go
				template = getTemplate(this.defaultLayout);
			}
		}

		// Render the layout template into the response
		outputTemplate(template, context, response);
	}

	private void outputTemplate(Template template, Context context, HttpServletResponse response) throws IOException
	{
		getVelocityView().merge(template, context, response.getWriter());
	}

	/**
	 * Overrides VelocityViewServlet to display user's custom error template
	 */
	private void error(HttpServletRequest request, HttpServletResponse response, Throwable e)
	{
		try
		{
			// get a velocity context
			Context ctx = createContext(request, response);

			Throwable cause = e;

			// if it's an MIE, i want the real cause and stack trace!
			if(cause instanceof MethodInvocationException)
			{
				// put the invocation exception in the context
				ctx.put(KEY_ERROR_INVOCATION_EXCEPTION, e);
				// get the real cause
				cause = ((MethodInvocationException) e).getWrappedThrowable();
			}

			// add the cause to the context
			ctx.put(KEY_ERROR_CAUSE, cause);

			// grab the cause's stack trace and put it in the context
			StringWriter sw = new StringWriter();
			cause.printStackTrace(new java.io.PrintWriter(sw));
			ctx.put(KEY_ERROR_STACKTRACE, sw.toString());

			// retrieve and render the error template
			Template et = getTemplate(this.errorTemplate);
			mergeTemplate(et, ctx, response);

		} catch(Exception e2)
		{
			// d'oh! log this
			log.error("Error during error template rendering", e2);
			// then punt the original to a higher authority
			innerOutputError(request, response, e);
		}
	}

	/**
	 * Invoked when there is an error thrown in any part of doRequest()
	 * processing. <br>
	 * <br>
	 * Default will send a simple HTML response indicating there was a problem.
	 * 
	 * @param request original HttpServletRequest from servlet container.
	 * @param response HttpServletResponse object from servlet container.
	 * @param e Exception that was thrown by some other part of process.
	 */
	private void innerOutputError(HttpServletRequest request, HttpServletResponse response, Throwable e)
	{
		if(!response.isCommitted())
		{
			return;
		}

		try
		{
			String path = ServletUtils.getPath(request);
			log.error("Error processing a template for path '" + path + "'", e);
			StringBuilder html = new StringBuilder();
			html.append("<!DOCTYPE html>\n");
			html.append("<html>\n");
			html.append("<head><meta charset=\"utf-8\"><title>Error</title></head>\n");
			html.append("<body>\n");
			html.append("<h2>VelocityView : Error processing a template for path '");
			html.append(path);
			html.append("'</h2>\n");

			Throwable cause = e;

			String why = cause.getMessage();
			if(why != null && why.length() > 0)
			{
				html.append(StringEscapeUtils.escapeHtml(why));
				html.append("\n<br>\n");
			}

			// TODO: add line/column/template info for parse errors et al

			// if it's an MIE, i want the real stack trace!
			if(cause instanceof MethodInvocationException)
			{
				// get the real cause
				cause = ((MethodInvocationException) cause).getWrappedThrowable();
			}

			StringWriter sw = new StringWriter();
			cause.printStackTrace(new PrintWriter(sw));

			html.append("<pre>\n");
			html.append(StringEscapeUtils.escapeHtml(sw.toString()));
			html.append("</pre>\n");
			html.append("</body>\n");
			html.append("</html>");
			response.getWriter().write(html.toString());
		} catch(Exception e2)
		{
			// clearly something is quite wrong.
			// let's log the new exception then give up and
			// throw a runtime exception that wraps the first one
			String msg = "Exception while printing error screen";
			log.error(msg, e2);
			throw new RuntimeException(msg, e);
		}
	}

	/**
	 * Manages the {@link ResourceNotFoundException} to send an HTTP 404 result
	 * when needed.
	 * 
	 * @param request The request object.
	 * @param response The response object.
	 * @param e The exception to check.
	 * @throws IOException If something goes wrong when sending the HTTP error.
	 */
	private void manageResourceNotFound(HttpServletRequest request, HttpServletResponse response,
			ResourceNotFoundException e) throws IOException
	{
		String path = ServletUtils.getPath(request);
		if(log.isDebugEnabled())
		{
			log.debug("Resource not found for path '" + path + "'", e);
		}
		String message = e.getMessage();
		if(!response.isCommitted() && path != null && message != null && message.contains("'" + path + "'"))
		{
			response.sendError(HttpServletResponse.SC_NOT_FOUND, path);
		}
		else
		{
			error(request, response, e);
			throw e;
		}
	}

	/**
	 * Cleanup routine called at the end of the request processing sequence
	 * allows a derived class to do resource cleanup or other end of process
	 * cycle tasks. This default implementation does nothing.
	 * 
	 * @param request servlet request from client
	 * @param response servlet response
	 * @param context Context that was merged with the requested template
	 */
	protected void requestCleanup(HttpServletRequest request, HttpServletResponse response, Context context)
	{

	}

}
