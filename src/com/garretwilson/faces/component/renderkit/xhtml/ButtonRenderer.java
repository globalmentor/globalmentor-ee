package com.garretwilson.faces.component.renderkit.xhtml;

import java.io.IOException;
import java.net.URI;
import java.util.*;

import javax.faces.component.*;
import javax.faces.context.*;
import javax.faces.el.ValueBinding;
import javax.faces.event.*;

import com.garretwilson.faces.component.ComponentUtilities;
import com.garretwilson.faces.component.UIBasicCommand;
import com.garretwilson.faces.component.UIBasicForm;
import com.garretwilson.util.Debug;

import static com.garretwilson.faces.component.ComponentUtilities.*;
import static com.garretwilson.faces.render.RenderUtilities.*;
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
			//G***testing	TODO try not to rely on UIBasicForm
		final UIBasicForm parentForm=(UIBasicForm)ComponentUtilities.getParent(component, UIForm.class);	//get the parent form, if there is one
		assert parentForm!=null : getClass().getName()+" is not enclosed in a UIForm component.";
		return parentForm.getHiddenFieldClientID(context);
/*G***fix
		final String clientID=component.getClientId(context);	//get the component's client ID
		return clientID+NamingContainer.SEPARATOR_CHAR+HIDDEN_FIELD_ID;	//construct a client ID for the hidden field
*/
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
//G***del Debug.setDebug(true);
//G***del Debug.setVisible(true);
/*G***fix
	if (renderedSet) {
    return (rendered);
}
ValueBinding vb = getValueBinding("rendered");
if (vb != null) {
    return (!Boolean.FALSE.equals(vb.getValue(getFacesContext())));
} else {
    return (this.rendered);
}
*/
		final ResponseWriter writer=context.getResponseWriter();	//get the response writer
		final String clientID=component.getClientId(context);	//get the component's client ID
//G***del Debug.trace("ready to try to encode button:", clientID);

/*TODO fix without relying on UIBasicForm
		if(USE_JAVASCRIPT)	//if we should use JavaScript to compensate for a buggy browser
		{
			final String hiddenFieldClientID=getHiddenFieldClientID(context, component);	//get the client ID of the hidden field
				//render the hidden field
			if(component.isRendered())	//if the component should be rendered
			{
				writer.startElement(ELEMENT_INPUT, null);	//<input>
				writer.writeAttribute(ATTRIBUTE_NAME, hiddenFieldClientID, ATTRIBUTE_NAME);	//name="xxx:button"
				writer.writeAttribute(ELEMENT_INPUT_ATTRIBUTE_TYPE, INPUT_TYPE_HIDDEN, ELEMENT_INPUT_ATTRIBUTE_TYPE);	//type="hidden"
				writer.endElement(ELEMENT_INPUT);	//</input>
			}
		}
*/
		
//G***del		renderPopupJavaScript(writer, null);	//G***testing
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
				final URI popupURI;	//see if we have a popup URI
				if(command instanceof UIBasicCommand)	//if this is an extended command component
				{
					final UIBasicCommand basicCommand=(UIBasicCommand)command;	//cast the command to a basic command
					popupURI=basicCommand.getPopupURI();	//get the popup URI, if there is one
				}
				else	//if this is a normal command component
				{
					popupURI=null;	//we don't have a popup URI
				}
//TODO make sure decode knows to check if a custom value was passed
				final StringBuilder onclickJavaScript=new StringBuilder();	//we'll construct JavaScript code to be executed when the button is clicked

				
//G***fix the window name				public static String createName(final String string)
				
				if(popupURI!=null)	//if we have a popup URI
				{
					onclickJavaScript.append(popupWindow(popupURI, "popupwindow"));	//TODO fix the window name
				}

//G***del				onclickJavaScript.append("if(oldsubmitcontrol!=null)");	//G***testing
/*G***del
				final String form=getFormVariable(parentFormID);
				final String var=getFormComponentVariable(parentFormID, "oldsubmitcontrol");
				final String hiddenField=getFormComponentVariable(parentFormID, hiddenFieldClientID);
				final String hiddenFieldValue=getPropertyVariable(hiddenField, "value");
*/

//G***fix				final String var="window.oldsubmitcontrol";

/*G***del
				onclickJavaScript.append("if("+form+"!=undefined)");	//G***testing
				onclickJavaScript.append(createStatement("alert('found the form')"));	//G***testing
*/

/*G***del
				onclickJavaScript.append("if("+var+"==undefined)");	//G***testing
				onclickJavaScript.append(createStatement("alert('custom form variable is defined: '+("+var+"!=undefined))"));	//G***testing

				onclickJavaScript.append(createStatement(var+"='test'"));	//G***testing

				onclickJavaScript.append("if("+var+"!=undefined)");	//G***testing
				onclickJavaScript.append(createStatement("alert('custom form variable is defined: '+("+var+"!=undefined))"));	//G***testing
*/

/*G***fix
				onclickJavaScript.append(createStatement("alert('custom form variable is defined: '+("+var+"!=undefined))"));	//G***testing
				onclickJavaScript.append(createStatement(var+"="+hiddenField));	//G***testing
				onclickJavaScript.append(createStatement("alert('custom form variable is defined: '+("+var+"!=undefined))"));	//G***testing
*/

//G***del				onclickJavaScript.append(createStatement(var+"='testvalue'"));	//G***testing
				
				
//G***del				onclickJavaScript.append(createStatement("alert('before submit hidden field value: '+"+hiddenFieldValue+")"));	//G***testing
				
				
//G***del				onclickJavaScript.append(createStatement("alert('custom form variable is now defined')"));	//G***testing

/*G***fix
				onclickJavaScript.append("if("+form+"==undefined and "+var+"==undefined)");	//G***testing
				onclickJavaScript.append(createStatement("alert('"+var+" is undefined')"));	//G***testing
*/

/*G***fix
				onclickJavaScript.append("var "+var+";if("+var+"==undefined)");	//G***testing
				onclickJavaScript.append(createStatement("alert('"+var+" is undefined')"));	//G***testing
*/
				
//G***fix				onclickJavaScript.append(createStatement(setLiteralValue(getPropertyVariable(var, "value"), "")));	//G***testing

//G***fix				onclickJavaScript.append(createStatement(var+"="+getFormComponentVariable(parentFormID, hiddenFieldClientID)));	//G***testing
				
				
				
					//document.forms['formID']['hiddenFieldClientID'].value='clientID';
				onclickJavaScript.append(createStatement(setFormComponentPropertyLiteralValue(parentFormID, hiddenFieldClientID, "value", (value!=null ? value.toString() : clientID))));
					//document.forms['formID'].submit();
				onclickJavaScript.append(createStatement(submitForm(parentFormID)));
					//document.forms['formID']['hiddenFieldClientID'].value='';
				onclickJavaScript.append(createStatement(setFormComponentPropertyLiteralValue(parentFormID, hiddenFieldClientID, "value", "")));

//G***del				onclickJavaScript.append(createStatement("alert('aftersubmit hidden field value: '+"+hiddenFieldValue+")"));	//G***testing

				onclickJavaScript.append("return false;");	//TODO use constants
				writer.writeAttribute(ATTRIBUTE_ONCLICK, onclickJavaScript, null);	//write the JavaScript
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
/*G***del
Debug.trace("hidden field value:", hiddenFieldValue);
Debug.trace("expecting value of client ID:", clientID);
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
