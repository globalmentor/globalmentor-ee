package com.garretwilson.faces.component.renderkit.xhtml;

import java.io.IOException;

import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

import com.garretwilson.text.xml.xhtml.XHTMLConstants;

/**Encapsulates basic XHTML rendering functionality.
<p>Inspired by <code>com.sun.faces.renderkit.html_basic.HtmlBasicRenderer.java</code>.</p>
@author Garret Wilson
*/
public class AbstractXHTMLRenderer extends Renderer implements XHTMLConstants
{

	
	/**@return <code>true</code> if the component has an <code>id</code> attribute that isn't automatically generated.*/ 
	protected boolean hasCustomID(final UIComponent component)
	{
		final String id=component.getId();	//get the component's ID
		return id!=null && !id.startsWith(UIViewRoot.UNIQUE_ID_PREFIX);	//see if there is an ID that wasn't generated
	}


	/**Writes the component ID, if there is a custom ID.
	@param context The faces context.
	@param writer The output writer.
	@param component The component being rendered.
	@see #hasCustomID(UIComponent)
	*/
	protected void writeIDAttribute(final FacesContext context, ResponseWriter writer, UIComponent component)
	{
		if(hasCustomID(component))	//if this component has a custom ID
		{
			try
			{
				writer.writeAttribute(ATTRIBUTE_ID, component.getClientId(context), ATTRIBUTE_ID);	//write the ID
       }
			catch(IOException ioException)
			{
				throw new AssertionError(ioException);
			}
    }
	}

	/**Encodes a component and its children, either by delegating to the component
	 	if that component can render its children, or by recursively rendering that
	 	component's children manually.
	<p>If the component is not rendered, no action is taken.</p>
	@param context The Faces context.
	@param component The component the children of which to register.
	*/
	protected static void encodeTree(final FacesContext context, final UIComponent component) throws IOException
	{
		if(component.isRendered())	//if the component is rendered
		{
			component.encodeBegin(context);	//begin the component
			if(component.getRendersChildren())	//if the component renders its children
			{
				component.encodeChildren(context);	//ask the component to render its children
			}
			else	//if the component can't render its own children, we'll have to do it instead
			{
				encodeDescendants(context, component);	//encode the component's children ourselves
			}
			component.encodeEnd(context);	//end the component
    }
	}

	/**Manually encode a component's children. Each child will be encoded either
	 	by delegating to the child component if that component can render its
	 	children, or by recursively rendering that component's children manually.
	<p>If the component is not rendered, no action is taken.</p>
	@param context The Faces context.
	@param component The component the children of which to register.
	*/
	protected static void encodeDescendants(final FacesContext context, final UIComponent component) throws IOException
	{
		for(Object child:component.getChildren())	//look at each of the component's children
		{
			encodeTree(context, (UIComponent)child);	//recursively encode this child and its descendants
    }
	}

}
