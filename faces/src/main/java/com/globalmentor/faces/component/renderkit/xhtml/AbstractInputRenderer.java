/*
 * Copyright © 1996-2008 GlobalMentor, Inc. <https://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.globalmentor.faces.component.renderkit.xhtml;

import java.io.IOException;
import java.util.*;

import javax.faces.component.*;
import javax.faces.context.*;

import static com.globalmentor.faces.component.FacesComponents.*;
import static com.globalmentor.faces.taglib.xhtml.XHTMLTags.*;
import static com.globalmentor.html.spec.HTML.*;

/**
 * Renders a <code>UIInput</code> as an XHTML input element.
 * @author Garret Wilson
 */
public abstract class AbstractInputRenderer extends AbstractXHTMLRenderer {

	/** The type of XHTML input element to generate; output in the <code>type</code> attribute. */
	private final String type;

	/** @return The type of XHTML input element to generate; output in the <code>type</code> attribute. */
	protected String getType() {
		return type;
	}

	/**
	 * Input type constructor.
	 * @param type The type of XHTML input element to generate; output in the <code>type</code> attribute.
	 */
	public AbstractInputRenderer(final String type) {
		super(ELEMENT_INPUT); //default to the <input> element for the element
		this.type = type; //save the type
	}

	/**
	 * Begins encoding the component.
	 * @param context The JSF context.
	 * @param component The component being rendered.
	 * @throws IOException Thrown if there is an error writing the output.
	 * @throws NullPointerException Thrown if <var>context</var> or <var>component</var> is <code>null</code>.
	 */
	public void encodeBegin(final FacesContext context, final UIComponent component) throws IOException {
		super.encodeBegin(context, component); //do the default encoding
		if(component.isRendered()) { //if the component should be rendered
			final UIInput input = (UIInput)component; //get the component as an input component
			final ResponseWriter writer = context.getResponseWriter(); //get the response writer
			final Map<String, Object> attributeMap = component.getAttributes(); //get the map of attributes
			writer.writeAttribute(ATTRIBUTE_NAME, component.getClientId(context), CLIENT_ID_ATTRIBUTE); //write the client ID as the name
			writer.writeAttribute(ELEMENT_INPUT_ATTRIBUTE_TYPE, getType(), null); //write the input type
			final Object renderValue = getRenderValue(input); //get the current value to be rendered
			if(renderValue != null) { //if there is a current value
				writer.writeAttribute(ATTRIBUTE_VALUE, renderValue, ATTRIBUTE_VALUE); //write the current value
			}
		}
	}

	/**
	 * Determines the value intended by the component. This value is either the submitted value or the currently set value, in that order.
	 * @param input The input with the current submitted value.
	 * @return The value intended by the component.
	 */
	protected Object getIntendedValue(final UIInput input) {
		final Object submittedValue = input.getSubmittedValue(); //get the current submitted value
		if(submittedValue != null) { //if there is a submitted value
			return submittedValue; //return the submitted value
		} else { //if there is not a submitted value
			return input.getValue(); //return the currently set value TODO maybe eventually do formatting and conversion; see Sun HtmlBasicInputRenderer.java
		}
	}

	/**
	 * Determines the current value to be rendered. This version returns the intended value.
	 * @param input The input with the current submitted value.
	 * @return The value to be rendered as the component's current value.
	 * @see #getIntendedValue(UIInput)
	 */
	protected Object getRenderValue(final UIInput input) {
		final Object submittedValue = input.getSubmittedValue(); //get the current submitted value
		if(submittedValue != null) { //if there is a submitted value
			return submittedValue; //return the submitted value
		} else { //if there is not a submitted value
			return input.getValue(); //return the currently set value TODO maybe eventually do formatting and conversion; see Sun HtmlBasicInputRenderer.java
		}
	}

	/**
	 * Decodes the component information.
	 * @param context The JSF context.
	 * @param component The component being decoded.
	 * @throws NullPointerException Thrown if <var>context</var> or <var>component</var> is <code>null</code>.
	 */
	public void decode(final FacesContext context, final UIComponent component) {
		//TODO this code should go in a generic AbstractInputRenderer---or maybe even something more general than that
		if(isMutable(component)) { //if the component is mutable
			//TODO fix; this was implemented before generics, and may depend on the ability to pass non-string objects as values; see RequestParametersFacesContextDecorator.RequestParametersExternalContextDecorator.getRequestParameterMap()
			final Map requestParameterMap = context.getExternalContext().getRequestParameterMap(); //get the request parameters
			final String clientID = component.getClientId(context); //get the component's client ID
			final Object value = requestParameterMap.get(clientID); //see if there is a value for our component
			//TODO del getLogger().trace("found value: {}", value);
			//if there is a value for our component
			if(value != null) {
				final UIInput input = (UIInput)component; //get the component as an input component
				input.setSubmittedValue(value); //set the input's submitted value
			}
		}
	}

	/**
	 * Decodes the value information, doing any transformations necessary. By default no conversion is performed.
	 * @param value The value received as a request submission.
	 * @return The decoded value, or <code>null</code> for no value.
	 */
	protected Object decodeValue(final Object value) {
		return value; //return the value with no changes
	}

}
