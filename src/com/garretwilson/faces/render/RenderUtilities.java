package com.garretwilson.faces.render;

import java.io.*;
import java.util.*;

import javax.faces.component.*;
import javax.faces.context.*;

/**Utilities for rendering components.
@author Garret Wilson
*/
public class RenderUtilities
{
	
	/**Renders attributes of the component that should be passed through
		with no modification.
	@param writer The writer rendering the component.
	@param component The component being rendered.
	@param attributes The names of the attributes to render
	*/
	public static void renderPassthroughAttributes(final ResponseWriter writer, final UIComponent component, final String[] attributes) throws IOException
	{
		final Map attributeMap=component.getAttributes();	//get the map of component attributes
		for(final String attribute:attributes)	//look at each of the passed attributes
		{
			final Object value=attributeMap.get(attribute);	//get this attribute
			if(value!=null)	//if we have a value TODO check to see if we should render the value, according to the special wrapper type MIN_VALUEs
			{
				writer.writeAttribute(attribute, value, attribute);	//render the attribute
			}
    }
	}
	
}
