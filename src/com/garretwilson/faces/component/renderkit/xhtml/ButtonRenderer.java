package com.garretwilson.faces.component.renderkit.xhtml;

import java.io.IOException;
import java.util.*;

import javax.faces.component.*;
import javax.faces.context.*;

import static com.garretwilson.faces.taglib.xhtml.XHTMLTagConstants.*;
import static com.garretwilson.text.xml.xhtml.XHTMLConstants.*;

/**Renders a <code>UICommand</code> as a button.
@author Garret Wilson
*/
public class ButtonRenderer extends AbstractXHTMLRenderer
{

	/**@return The name of the XML element for the component.*/
	protected String getComponentElementName() {return ELEMENT_BUTTON;}

	/**Begins encoding the list.
	@param context The JSF context.
	@param component The component being rendered.
	@exception IOException Thrown if there is an error writing the output.
	@exception NullPointerException Thrown if <var>context</var> or
		<var>component</var> is <code>null</code>.
	*/
	public void encodeBegin(final FacesContext context, final UIComponent component) throws IOException
	{
		super.encodeBegin(context, component);	//do the default encoding
		if(component.isRendered())	//if the component should be rendered
		{
			final UICommand command=(UICommand)component;	//get the component as a command component
			final ResponseWriter writer=context.getResponseWriter();	//get the response writer
			final Map attributeMap=component.getAttributes();	//get the map of attributes
			String type=(String)attributeMap.get(ELEMENT_BUTTON_ATTRIBUTE_TYPE);	//get the type attribute
			writer.writeAttribute(ATTRIBUTE_NAME, component.getClientId(context), CLIENT_ID_ATTRIBUTE);	//write the client ID as the name
			if(type==null)	//if there is no type
			{
				type=BUTTON_TYPE_SUBMIT;	//default to a submit button
				attributeMap.put(ELEMENT_BUTTON_ATTRIBUTE_TYPE, type);	//update the button type with the default				
			}
			final Object value=command.getValue();	//get the button's value
			if(value!=null)	//if a value is given
			{
				writer.writeAttribute(ATTRIBUTE_VALUE, value, ATTRIBUTE_VALUE);	//write the value
			}
			if(value!=null)	//if a value is given, write it as button content G***testing
			{
				writer.writeText(value, null);	//G***testing
			}
		}
	}

}
