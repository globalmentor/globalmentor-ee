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

/**Renders a <code>UIInput</code> as an XHTML input element of type <code>text</code>.
@author Garret Wilson
*/
public class InputTextRenderer extends AbstractXHTMLRenderer
{

	/**The type of XHTML input element to generate; output in the <code>type</code> attribute.*/
	private final String type;

		/**@return The type of XHTML input element to generate; output in the <code>type</code> attribute.*/
		protected String getType() {return type;}

	/**Default constructor.*/
	public InputTextRenderer()
	{
		this(INPUT_TYPE_TEXT);	//create an input text element
	}

	/**Input type constructor.
	@param type The type of XHTML input element to generate; output in the
		<code>type</code> attribute.
	*/
	public InputTextRenderer(final String type)
	{
		super(ELEMENT_INPUT);	//default to the <input> element for the element
		this.type=type;	//save the type
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
		super.encodeBegin(context, component);	//do the default encoding
		if(component.isRendered())	//if the component should be rendered
		{
			final UIInput input=(UIInput)component;	//get the component as an input component

//G***del Debug.setDebug(true);
//G***del Debug.trace("**********encoding, action type is: ", command.getAction().getType(context));
//G***del Debug.trace("**********encoding, action value is: ", command.getAction().invoke(context, new Object[]{}));
			
			final ResponseWriter writer=context.getResponseWriter();	//get the response writer
			final Map attributeMap=component.getAttributes();	//get the map of attributes
			writer.writeAttribute(ATTRIBUTE_NAME, component.getClientId(context), CLIENT_ID_ATTRIBUTE);	//write the client ID as the name
			writer.writeAttribute(ELEMENT_INPUT_ATTRIBUTE_TYPE, getType(), null);	//write the input type
			final Object renderValue=getRenderValue(input);	//get the current value to be rendered
			if(renderValue!=null)	//if there is a current value
			{
				writer.writeAttribute(ATTRIBUTE_VALUE, renderValue, ATTRIBUTE_VALUE);	//write the current value
			}
		}
	}

	/**Determines the current value to be rendered.
	This value is either the submitted value or the currently set value, in that
		order.
	@return The value to be rendered as the component's current value.
	*/
	protected Object getRenderValue(final UIInput input)
	{
		final Object submittedValue=input.getSubmittedValue();	//get the current submitted value
		if(submittedValue!=null)	//if there is a submitted value
		{
			return submittedValue;	//return the submitted value
		}
		else	//if there is not a submitted value
		{
			return input.getValue();	//return the currently set value TODO maybe eventually do formatting and conversion; see Sun HtmlBasicInputRenderer.java
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
			//TODO this code should go in a generic AbstractInputRenderer---or maybe even something more general than that
		if(isMutable(component))	//if the component is mutable
		{
			final Map requestParameterMap=context.getExternalContext().getRequestParameterMap();	//get the request parameters
			final String clientID=component.getClientId(context);	//get the component's client ID
			final Object value=requestParameterMap.get(clientID);	//see if there is a value for our component
//G***del Debug.trace("found value:", value);
				//if there is a value for our component
			if(value!=null)
			{
				final UIInput input=(UIInput)component;	//get the component as an input component
				input.setSubmittedValue(value);	//set the input's submitted value
			}
    }
	}

}
