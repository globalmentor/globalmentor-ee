package com.garretwilson.faces.component.renderkit.xhtml;

import java.io.IOException;
import java.util.*;

import javax.faces.component.*;
import javax.faces.context.*;
import javax.faces.event.*;

import com.garretwilson.util.Debug;

import static com.garretwilson.faces.component.ComponentUtilities.*;
import static com.garretwilson.faces.taglib.xhtml.XHTMLTagConstants.*;
import static com.garretwilson.text.xml.xhtml.XHTMLConstants.*;

/**Renders a <code>UICommand</code> as a button.
@author Garret Wilson
*/
public class ButtonRenderer extends AbstractXHTMLRenderer
{

	/**@return The name of the XML element for the component.*/
	protected String getComponentElementName() {return ELEMENT_BUTTON;}

	/**Begins encoding the component.
	@param context The JSF context.
	@param component The component being rendered.
	@exception IOException Thrown if there is an error writing the output.
	@exception NullPointerException Thrown if <var>context</var> or
		<var>component</var> is <code>null</code>.
	*/
	public void encodeBegin(final FacesContext context, final UIComponent component) throws IOException
	{
/*G***del
Debug.setDebug(true);
Debug.setVisible(true);
*/
		super.encodeBegin(context, component);	//do the default encoding
		if(component.isRendered())	//if the component should be rendered
		{
			final UICommand command=(UICommand)component;	//get the component as a command component

//G***del Debug.setDebug(true);
//G***del Debug.trace("**********encoding, action type is: ", command.getAction().getType(context));
//G***del Debug.trace("**********encoding, action value is: ", command.getAction().invoke(context, new Object[]{}));
			
			final ResponseWriter writer=context.getResponseWriter();	//get the response writer
			final Map attributeMap=component.getAttributes();	//get the map of attributes
			String type=(String)attributeMap.get(ELEMENT_BUTTON_ATTRIBUTE_TYPE);	//get the type attribute
//G***del Debug.trace("encoding command", command, "client id", component.getClientId(context));
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

	/**Decodes the component information.
	@param context The JSF context.
	@param component The component being decoded.
	@exception IOException Thrown if there is an error writing the output.
	@exception NullPointerException Thrown if <var>context</var> or
		<var>component</var> is <code>null</code>.
	*/
	public void decode(FacesContext context, UIComponent component)
	{
/*G***del
Debug.setDebug(true);
Debug.setVisible(true);
Debug.trace("decoding command", component, "client id", component.getClientId(context));
Debug.trace("ready to decode button!");
*/
		if(isMutable(component))	//if the component is mutable
		{
			final Map requestParameterMap=context.getExternalContext().getRequestParameterMap();	//get the request parameters
			final String clientID=component.getClientId(context);	//get the component's client ID
			final String value=(String)requestParameterMap.get(clientID);	//see if there is a value for our component
				//if our button was the one pressed (check for an image map click for this button, too)
			if(value!=null || requestParameterMap.get(clientID+".x")!=null || requestParameterMap.get(clientID+".y")!=null)
			{
				final String type=(String)component.getAttributes().get(ELEMENT_BUTTON_ATTRIBUTE_TYPE);	//get the type
				if(BUTTON_TYPE_RESET.equalsIgnoreCase(type))	//if this was the reset button
				{
					return;	//don't generate an event for the reset button
				}
				final ActionEvent actionEvent=new ActionEvent(component);	//create a new action event for our component
				component.queueEvent(actionEvent);	//queue our new action
			}
    }
	}

}
