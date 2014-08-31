/*
 * Copyright © 1996-2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.globalmentor.faces.component.renderkit.xhtml;

import java.io.IOException;
import java.net.URI;
import java.util.*;

import javax.faces.component.*;
import javax.faces.context.*;
import javax.faces.event.*;

import com.globalmentor.faces.component.FacesComponents;
import com.globalmentor.faces.component.UIBasicCommand;
import com.globalmentor.faces.component.UIBasicForm;

import static com.globalmentor.faces.component.FacesComponents.*;
import static com.globalmentor.faces.taglib.xhtml.XHTMLTags.*;
import static com.globalmentor.javascript.JavaScript.*;
import static com.globalmentor.text.xml.xhtml.XHTML.*;

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
	protected static final boolean USE_JAVASCRIPT=true;

	/**The ID of the hidden field used to hold the button value.*/
	protected static final String HIDDEN_FIELD_ID="button";

	/**@return The name of the XML element for the component.*/
//TODO del	protected String getComponentElementName() {return ELEMENT_BUTTON;}

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
			//TODO testing	TODO try not to rely on UIBasicForm
		final UIBasicForm parentForm=(UIBasicForm)FacesComponents.getParent(component, UIForm.class);	//get the parent form, if there is one
		assert parentForm!=null : getClass().getName()+" is not enclosed in a UIForm component.";
		return parentForm.getHiddenFieldClientID(context);
/*TODO fix
		final String clientID=component.getClientId(context);	//get the component's client ID
		return clientID+NamingContainer.SEPARATOR_CHAR+HIDDEN_FIELD_ID;	//construct a client ID for the hidden field
*/
	}	
	
	/**Begins encoding the component.
	@param context The JSF context.
	@param component The component being rendered.
	@throws IOException Thrown if there is an error writing the output.
	@throws NullPointerException Thrown if <var>context</var> or
		<var>component</var> is <code>null</code>.
	*/
	public void encodeBegin(final FacesContext context, final UIComponent component) throws IOException
	{
/*TODO fix
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
//TODO del Log.trace("ready to try to encode button:", clientID);

/*TODO fix without relying on UIBasicForm
		if(USE_JAVASCRIPT) {	//if we should use JavaScript to compensate for a buggy browser
			final String hiddenFieldClientID=getHiddenFieldClientID(context, component);	//get the client ID of the hidden field
				//render the hidden field
			if(component.isRendered()) {	//if the component should be rendered
				writer.startElement(ELEMENT_INPUT, null);	//<input>
				writer.writeAttribute(ATTRIBUTE_NAME, hiddenFieldClientID, ATTRIBUTE_NAME);	//name="xxx:button"
				writer.writeAttribute(ELEMENT_INPUT_ATTRIBUTE_TYPE, INPUT_TYPE_HIDDEN, ELEMENT_INPUT_ATTRIBUTE_TYPE);	//type="hidden"
				writer.endElement(ELEMENT_INPUT);	//</input>
			}
		}
*/
		
//TODO del		renderPopupJavaScript(writer, null);	//TODO testing
		super.encodeBegin(context, component);	//do the default encoding
		if(component.isRendered()) {	//if the component should be rendered
			final UICommand command=(UICommand)component;	//get the component as a command component

//TODO del Debug.setDebug(true);
//TODO del Log.trace("**********encoding, action type is: ", command.getAction().getType(context));
//TODO del Log.trace("**********encoding, action value is: ", command.getAction().invoke(context, new Object[]{}));
			
//TODO del Log.trace("encoding command", command, "client id", clientID);
			writer.writeAttribute(ATTRIBUTE_NAME, clientID, CLIENT_ID_ATTRIBUTE);	//write the client ID as the name
			final Map attributeMap=component.getAttributes();	//get the map of attributes
			final String type;	//we'll deterine the type to generate
			if(USE_JAVASCRIPT) {	//if we should use JavaScript to compensate for a buggy browser
				type=BUTTON_TYPE_BUTTON;	//always use the button type for JavaScript form submission
			}
			else {	//if we should use normal buttons
				final String typeAttributeValue=(String)attributeMap.get(ELEMENT_BUTTON_ATTRIBUTE_TYPE);	//get the type attribute
				type=typeAttributeValue!=null ? typeAttributeValue : BUTTON_TYPE_SUBMIT;	//use the type attribute value, or default to a submit button
			}
			writer.writeAttribute(ELEMENT_BUTTON_ATTRIBUTE_TYPE, type, ELEMENT_BUTTON_ATTRIBUTE_TYPE);	//write the type
			final Object value=command.getValue();	//get the button's value
			if(value!=null) {	//if a value is given
				writer.writeAttribute(ATTRIBUTE_VALUE, value, ATTRIBUTE_VALUE);	//write the value
			}
/*TODO del
			else {	//TODO testing
				writer.writeAttribute(ATTRIBUTE_VALUE, clientID, ATTRIBUTE_VALUE);	//TODO testing
			}
*/
/*TODO del
			else {	//TODO testing
				writer.writeAttribute(ATTRIBUTE_VALUE, clientID, ATTRIBUTE_VALUE);	//TODO testing
			}
*/
/*TODO del
			if(value!=null) {	//if a value is given, write it as button content TODO testing
				writer.writeText(value, null);	//TODO testing
			}
 */
			if(USE_JAVASCRIPT) {	//if we should use JavaScript to compensate for a buggy browser
				final String hiddenFieldClientID=getHiddenFieldClientID(context, component);	//get the client ID of the hidden field
				final UIForm parentForm=(UIForm)FacesComponents.getParent(component, UIForm.class);	//get the parent form, if there is one
				assert parentForm!=null : getClass().getName()+" is not enclosed in a UIForm component.";
				final String parentFormID=parentForm.getClientId(context);	//get the ID of the parent form
				final URI popupURI;	//see if we have a popup URI
				if(command instanceof UIBasicCommand) {	//if this is an extended command component
					final UIBasicCommand basicCommand=(UIBasicCommand)command;	//cast the command to a basic command
					popupURI=basicCommand.getPopupURI();	//get the popup URI, if there is one
				}
				else {	//if this is a normal command component
					popupURI=null;	//we don't have a popup URI
				}
//TODO make sure decode knows to check if a custom value was passed
				final StringBuilder onclickJavaScript=new StringBuilder();	//we'll construct JavaScript code to be executed when the button is clicked

				final Object[] confirms=getParameterValues(component, context, UIBasicCommand.CONFIRM_PARAMETER);	//see if there are any confirmation parameters
				for(final Object confirm:confirms) {	//for each confirmation parameter
					onclickJavaScript.append(createIf(createNot(confirmLiteral(confirm.toString())), returnValue(Boolean.FALSE)));	//if(!confirm(confirmString){return false;}
//TODO del when works					onclickJavaScript.append("if(!confirm('"+confirm+"')) return false;");	//TODO fix with utilities
				}
				
//TODO fix the window name				public static String createName(final String string)
				
				if(popupURI!=null) {	//if we have a popup URI
					onclickJavaScript.append(popupWindow(popupURI, "popupwindow"));	//TODO fix the window name
				}

//TODO del				onclickJavaScript.append("if(oldsubmitcontrol!=null)");	//TODO testing
/*TODO del
				final String form=getFormVariable(parentFormID);
				final String var=getFormComponentVariable(parentFormID, "oldsubmitcontrol");
				final String hiddenField=getFormComponentVariable(parentFormID, hiddenFieldClientID);
				final String hiddenFieldValue=getPropertyVariable(hiddenField, "value");
*/

//TODO fix				final String var="window.oldsubmitcontrol";

/*TODO del
				onclickJavaScript.append("if("+form+"!=undefined)");	//TODO testing
				onclickJavaScript.append(createStatement("alert('found the form')"));	//TODO testing
*/

/*TODO del
				onclickJavaScript.append("if("+var+"==undefined)");	//TODO testing
				onclickJavaScript.append(createStatement("alert('custom form variable is defined: '+("+var+"!=undefined))"));	//TODO testing

				onclickJavaScript.append(createStatement(var+"='test'"));	//TODO testing

				onclickJavaScript.append("if("+var+"!=undefined)");	//TODO testing
				onclickJavaScript.append(createStatement("alert('custom form variable is defined: '+("+var+"!=undefined))"));	//TODO testing
*/

/*TODO fix
				onclickJavaScript.append(createStatement("alert('custom form variable is defined: '+("+var+"!=undefined))"));	//TODO testing
				onclickJavaScript.append(createStatement(var+"="+hiddenField));	//TODO testing
				onclickJavaScript.append(createStatement("alert('custom form variable is defined: '+("+var+"!=undefined))"));	//TODO testing
*/

//TODO del				onclickJavaScript.append(createStatement(var+"='testvalue'"));	//TODO testing
				
				
//TODO del				onclickJavaScript.append(createStatement("alert('before submit hidden field value: '+"+hiddenFieldValue+")"));	//TODO testing
				
				
//TODO del				onclickJavaScript.append(createStatement("alert('custom form variable is now defined')"));	//TODO testing

/*TODO fix
				onclickJavaScript.append("if("+form+"==undefined and "+var+"==undefined)");	//TODO testing
				onclickJavaScript.append(createStatement("alert('"+var+" is undefined')"));	//TODO testing
*/

/*TODO fix
				onclickJavaScript.append("var "+var+";if("+var+"==undefined)");	//TODO testing
				onclickJavaScript.append(createStatement("alert('"+var+" is undefined')"));	//TODO testing
*/
				
//TODO fix				onclickJavaScript.append(createStatement(setLiteralValue(getPropertyVariable(var, "value"), "")));	//TODO testing

//TODO fix				onclickJavaScript.append(createStatement(var+"="+getFormComponentVariable(parentFormID, hiddenFieldClientID)));	//TODO testing
				
				
				
					//document.forms['formID']['hiddenFieldClientID'].value='clientID';
				onclickJavaScript.append(createStatement(setFormComponentPropertyLiteralValue(parentFormID, hiddenFieldClientID, "value", (value!=null ? value.toString() : clientID))));
					//document.forms['formID'].submit();
				onclickJavaScript.append(createStatement(submitForm(parentFormID)));
					//document.forms['formID']['hiddenFieldClientID'].value='';
				onclickJavaScript.append(createStatement(setFormComponentPropertyLiteralValue(parentFormID, hiddenFieldClientID, "value", "")));

//TODO del				onclickJavaScript.append(createStatement("alert('aftersubmit hidden field value: '+"+hiddenFieldValue+")"));	//TODO testing

				onclickJavaScript.append(returnValue(Boolean.FALSE));	//return false
				writer.writeAttribute(ATTRIBUTE_ONCLICK, onclickJavaScript, null);	//write the JavaScript
			}
		}
	}

	/**Decodes the component information.
	@param context The JSF context.
	@param component The component being decoded.
	@throws IOException Thrown if there is an error writing the output.
	@throws NullPointerException Thrown if <var>context</var> or
		<var>component</var> is <code>null</code>.
	*/
	public void decode(final FacesContext context, UIComponent component)
	{
		if(isMutable(component)) {	//if the component is mutable
			final Map requestParameterMap=context.getExternalContext().getRequestParameterMap();	//get the request parameters
			final String clientID=component.getClientId(context);	//get the component's client ID
			final boolean isClientIDMatch;	//we'll deterine whether there is a client ID match
			if(USE_JAVASCRIPT) {	//if we should use JavaScript to compensate for a buggy browser
				final String hiddenFieldClientID=getHiddenFieldClientID(context, component);	//get the client ID of the hidden field
				final String hiddenFieldValue=(String)requestParameterMap.get(hiddenFieldClientID);	//see if there is a value for our hidden field
/*TODO del
Log.trace("hidden field value:", hiddenFieldValue);
Log.trace("expecting value of client ID:", clientID);
*/
				isClientIDMatch=clientID.equals(hiddenFieldValue);	//record whether the the hidden field value contains our client ID
			}
			else {	//if we're not using JavaScript
				final String value=(String)requestParameterMap.get(clientID);	//see if there is a value for our component
				isClientIDMatch=value!=null;	//see if there was a value returned for our client ID
			}

				//if our button was the one pressed (check for an image map click for this button, too)
//TODO fix			if(value!=null || requestParameterMap.get(clientID+".x")!=null || requestParameterMap.get(clientID+".y")!=null)
//TODO testing with IE bug
//TODO del when works if(clientID.equals(value) || clientID.equals(testValue) || requestParameterMap.get(clientID+".x")!=null || requestParameterMap.get(clientID+".y")!=null)
//TODO fix IE bug; works, but keeps value around			if(clientID.equals(value) || clientID.equals(testValue) || requestParameterMap.get(clientID+".x")!=null || requestParameterMap.get(clientID+".y")!=null)
//TODO fix			if(clientID.equals(value) || (value!=null && value.contains("value=\""+test+"\"")) || requestParameterMap.get(clientID+".x")!=null || requestParameterMap.get(clientID+".y")!=null)
//TODO fix			if(clientID.equals(value) || requestParameterMap.get(clientID+".x")!=null || requestParameterMap.get(clientID+".y")!=null)
			if(isClientIDMatch || requestParameterMap.get(clientID+".x")!=null || requestParameterMap.get(clientID+".y")!=null)
			{
				final String type=(String)component.getAttributes().get(ELEMENT_BUTTON_ATTRIBUTE_TYPE);	//get the type
				if(BUTTON_TYPE_RESET.equalsIgnoreCase(type)) {	//if this was the reset button
					return;	//don't generate an event for the reset button
				}
				final ActionEvent actionEvent=new ActionEvent(component);	//create a new action event for our component
//TODO del Log.trace("---queueing an action:", actionEvent);
				component.queueEvent(actionEvent);	//queue our new action
			}
    }
	}

}
