package com.garretwilson.faces.component.renderkit.xhtml;

import java.io.IOException;
import java.util.*;

import javax.faces.component.*;
import javax.faces.context.*;
import javax.faces.event.*;

import com.garretwilson.faces.component.ComponentUtilities;
import com.garretwilson.text.xml.XMLUtilities;
import com.garretwilson.util.Debug;

import static com.garretwilson.faces.component.ComponentUtilities.*;
import static com.garretwilson.faces.taglib.xhtml.XHTMLTagConstants.*;
import static com.garretwilson.javascript.JavaScriptUtilities.*;
import static com.garretwilson.text.xml.xhtml.XHTMLConstants.*;

/**Renders a <code>UICommand</code> as a button.
Microsoft Internet Explorer does not correctly return values for the HTML button
	element, so this renderer uses hidden fields and Javascript to effect the same
	result when IE is the client.
@author Garret Wilson
TODO create client checking for IE
*/
public class ButtonRenderer extends AbstractXHTMLRenderer
{

	/**Whether JavaScript should be used to compensate for a buggy browser.*/
	protected final static boolean USE_JAVASCRIPT=true;

	/**The ID of the hidden field used to hold the button value.*/
	protected final static String HIDDEN_FIELD_ID="button";

	/**@return The name of the XML element for the component.*/
//G***del	protected String getComponentElementName() {return ELEMENT_BUTTON;}

	/**Default constructor.*/
	public ButtonRenderer()
	{
		super(ELEMENT_BUTTON);	//create a button element
	}

	/**Constructs a client ID for the hidden field associated with this button.
	@param context The JSF context.
	@param component The component being rendered.
	@return The client ID of the hidden field that holds this button's value.
	*/
	protected String getHiddenFieldClientID(final FacesContext context, final UIComponent component)
	{
		final String clientID=component.getClientId(context);	//get the component's client ID
		return clientID+NamingContainer.SEPARATOR_CHAR+HIDDEN_FIELD_ID;	//construct a client ID for the hidden field
	}	
	
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
		final ResponseWriter writer=context.getResponseWriter();	//get the response writer
		final String clientID=component.getClientId(context);	//get the component's client ID
		if(USE_JAVASCRIPT)	//if we should use JavaScript to compensate for a buggy browser
		{
			final String hiddenFieldClientID=getHiddenFieldClientID(context, component);	//get the client ID of the hidden field
				//render the hidden field
			if(component.isRendered())	//if the component should be rendered
			{
				writer.startElement(ELEMENT_INPUT, component);	//<input>
				writer.writeAttribute(ATTRIBUTE_NAME, hiddenFieldClientID, ATTRIBUTE_NAME);	//name="xxx:button"
				writer.writeAttribute(ELEMENT_INPUT_ATTRIBUTE_TYPE, INPUT_TYPE_HIDDEN, ELEMENT_INPUT_ATTRIBUTE_TYPE);	//type="hidden"
				writer.endElement(ELEMENT_INPUT);	//</input>
			}
		}
		super.encodeBegin(context, component);	//do the default encoding
		if(component.isRendered())	//if the component should be rendered
		{
			final UICommand command=(UICommand)component;	//get the component as a command component

//G***del Debug.setDebug(true);
//G***del Debug.trace("**********encoding, action type is: ", command.getAction().getType(context));
//G***del Debug.trace("**********encoding, action value is: ", command.getAction().invoke(context, new Object[]{}));
			
//G***del Debug.trace("encoding command", command, "client id", clientID);
			writer.writeAttribute(ATTRIBUTE_NAME, clientID, CLIENT_ID_ATTRIBUTE);	//write the client ID as the name
			final Map attributeMap=component.getAttributes();	//get the map of attributes
			final String type;	//we'll deterine the type to generate
			if(USE_JAVASCRIPT)	//if we should use JavaScript to compensate for a buggy browser
			{
				type=BUTTON_TYPE_BUTTON;	//always use the button type for JavaScript form submission
			}
			else	//if we should use normal buttons
			{
				final String typeAttributeValue=(String)attributeMap.get(ELEMENT_BUTTON_ATTRIBUTE_TYPE);	//get the type attribute
				type=typeAttributeValue!=null ? typeAttributeValue : BUTTON_TYPE_SUBMIT;	//use the type attribute value, or default to a submit button
			}
			writer.writeAttribute(ELEMENT_BUTTON_ATTRIBUTE_TYPE, type, ELEMENT_BUTTON_ATTRIBUTE_TYPE);	//write the type
			final Object value=command.getValue();	//get the button's value
			if(value!=null)	//if a value is given
			{
				writer.writeAttribute(ATTRIBUTE_VALUE, value, ATTRIBUTE_VALUE);	//write the value
			}
/*G***del
			else	//G***testing
			{
				writer.writeAttribute(ATTRIBUTE_VALUE, clientID, ATTRIBUTE_VALUE);	//G***testing
			}
*/
/*G***del
			else	//G***testing
			{
				writer.writeAttribute(ATTRIBUTE_VALUE, clientID, ATTRIBUTE_VALUE);	//G***testing
			}
*/
/*G***del
			if(value!=null)	//if a value is given, write it as button content G***testing
			{
				writer.writeText(value, null);	//G***testing
			}
 */
			if(USE_JAVASCRIPT)	//if we should use JavaScript to compensate for a buggy browser
			{
				final String hiddenFieldClientID=getHiddenFieldClientID(context, component);	//get the client ID of the hidden field
				final UIForm parentForm=(UIForm)ComponentUtilities.getParent(component, UIForm.class);	//get the parent form, if there is one
				assert parentForm!=null : getClass().getName()+" is not enclosed in a UIForm component.";
				final String parentFormID=parentForm.getClientId(context);	//get the ID of the parent form
				final String onmousedownJavaScript=
							//document.forms['formID']['hiddenFieldClientID'].value='value';
						createStatement(setFormComponentPropertyLiteralValue(parentFormID, hiddenFieldClientID, "value", (value!=null ? value.toString() : clientID)))
							//document.forms['formID'].submit();
						+createStatement(submitForm(parentFormID))
							//document.forms['formID']['hiddenFieldClientID'].value='';
						+createStatement(setFormComponentPropertyLiteralValue(parentFormID, hiddenFieldClientID, "value", ""));
				writer.writeAttribute("onmousedown", onmousedownJavaScript, null);	//TODO use a constant
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
	public void decode(final FacesContext context, UIComponent component)
	{
/*G***del
Debug.setDebug(true);
Debug.setVisible(true);
Debug.trace("decoding command", component, "client id", component.getClientId(context));
*/
		if(isMutable(component))	//if the component is mutable
		{
			final Map requestParameterMap=context.getExternalContext().getRequestParameterMap();	//get the request parameters
			final String clientID=component.getClientId(context);	//get the component's client ID
			final boolean isClientIDMatch;	//we'll deterine whether there is a client ID match
			if(USE_JAVASCRIPT)	//if we should use JavaScript to compensate for a buggy browser
			{
				final String hiddenFieldClientID=getHiddenFieldClientID(context, component);	//get the client ID of the hidden field
				final String hiddenFieldValue=(String)requestParameterMap.get(hiddenFieldClientID);	//see if there is a value for our hidden field
/*G***del when works			
Debug.trace("hidden field value:", hiddenFieldValue, "length", hiddenFieldValue.length());
Debug.trace("expecting value of client ID:", clientID, "length", clientID.length());
*/
				isClientIDMatch=clientID.equals(hiddenFieldValue);	//record whether the the hidden field value contains our client ID
			}
			else	//if we're not using JavaScript
			{
				final String value=(String)requestParameterMap.get(clientID);	//see if there is a value for our component
				isClientIDMatch=value!=null;	//see if there was a value returned for our client ID
			}

				//if our button was the one pressed (check for an image map click for this button, too)
//G***fix			if(value!=null || requestParameterMap.get(clientID+".x")!=null || requestParameterMap.get(clientID+".y")!=null)
//G***testing with IE bug
//G***del when works if(clientID.equals(value) || clientID.equals(testValue) || requestParameterMap.get(clientID+".x")!=null || requestParameterMap.get(clientID+".y")!=null)
//G***fix IE bug; works, but keeps value around			if(clientID.equals(value) || clientID.equals(testValue) || requestParameterMap.get(clientID+".x")!=null || requestParameterMap.get(clientID+".y")!=null)
//G***fix			if(clientID.equals(value) || (value!=null && value.contains("value=\""+test+"\"")) || requestParameterMap.get(clientID+".x")!=null || requestParameterMap.get(clientID+".y")!=null)
//G***fix			if(clientID.equals(value) || requestParameterMap.get(clientID+".x")!=null || requestParameterMap.get(clientID+".y")!=null)
			if(isClientIDMatch || requestParameterMap.get(clientID+".x")!=null || requestParameterMap.get(clientID+".y")!=null)
			{
				final String type=(String)component.getAttributes().get(ELEMENT_BUTTON_ATTRIBUTE_TYPE);	//get the type
				if(BUTTON_TYPE_RESET.equalsIgnoreCase(type))	//if this was the reset button
				{
					return;	//don't generate an event for the reset button
				}
				final ActionEvent actionEvent=new ActionEvent(component);	//create a new action event for our component
//G***del Debug.trace("---queueing an action!");
				component.queueEvent(actionEvent);	//queue our new action
			}
    }
	}

}
