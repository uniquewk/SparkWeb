package com.sparkweb.web.view.velocity.directive;

import java.io.IOException;
import java.io.Writer;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.parser.node.Node;

import com.sparkweb.web.view.velocity.directive.OverrideDirective.OverrideNodeWrapper;

/**
 * #override("block_name") 
 * 	#super() 
 * #end
 * 
 * @author yswang
 * @version 1.0
 */
public class SuperDirective extends org.apache.velocity.runtime.directive.Directive
{
	private static final String	NAME	= "super";

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public int getType()
	{
		return LINE;
	}

	@Override
	public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException,
			ResourceNotFoundException, ParseErrorException, MethodInvocationException
	{
		OverrideNodeWrapper current = (OverrideNodeWrapper) context.get(Utils.OVERRIDE_CURRENT_NODE);
		if(current == null)
		{
			throw new ParseErrorException("#super() directive must be child of #override() directive");
		}
		
		OverrideNodeWrapper parent = current.parentNode;
		if(parent == null)
		{
			throw new ParseErrorException("not found parent block for #super() ");
		}
		
		return parent.render(context, writer);
	}

}
