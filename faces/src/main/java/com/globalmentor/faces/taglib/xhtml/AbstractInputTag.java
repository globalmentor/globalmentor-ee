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

package com.globalmentor.faces.taglib.xhtml;

import javax.faces.component.*;
import javax.faces.context.FacesContext;
import javax.faces.event.*;

import static com.globalmentor.faces.component.FacesComponentConstants.*;
import static com.globalmentor.faces.component.FacesComponents.*;

/**
 * An abstract base class for input component and renderer tags.
 * @author Garret Wilson
 */
public abstract class AbstractInputTag extends AbstractXHTMLTag {

	/** @return The type of the component to use for this tag. */
	public String getComponentType() {
		return UIInput.COMPONENT_TYPE;
	}

	/** The value of the component. */
	private String value;

	/** @return The value of the component. */
	public String getValue() {
		return value;
	}

	/**
	 * Sets the value of the component.
	 * @param value The value of the component.
	 */
	public void setValue(final String value) {
		this.value = value;
	}

	/** The component validator. */
	private String validator;

	/** @return The component validator. */
	public String getValidator() {
		return validator;
	}

	/**
	 * Sets the component validator.
	 * @param validator The new component validator.
	 */
	public void setValidator(final String validator) {
		this.validator = validator;
	}

	//TODO implement converter atribute

	/** Whether the input is immediate. */
	private String immediate;

	/** @return Whether the input is immediate. */
	public String getImmediate() {
		return immediate;
	}

	/**
	 * Sets whether the input is immediate.
	 * @param immediate Whether the input should be immediate.
	 */
	public void setImmediate(final String immediate) {
		this.immediate = immediate;
	}

	/** Whether the input is required. */
	private String required;

	/** @return Whether the input is required. */
	public String getRequired() {
		return required;
	}

	/**
	 * Sets whether the input is required.
	 * @param required Whether the input is required.
	 */
	public void setRequired(final String required) {
		this.required = required;
	}

	/** The component value change listener. */
	private String valueChangeListener;

	/** @return The component valuel change listener. */
	public String getValueChangeListener() {
		return valueChangeListener;
	}

	/**
	 * Sets the component value change listener.
	 * @param valueChangeListener The new component value change listener.
	 */
	public void setValueChangeListener(final String valueChangeListener) {
		this.valueChangeListener = valueChangeListener;
	}

	/**
	 * Sets the component properties from the tag's attributes.
	 * @param component The component the properties of which should be set.
	 */
	protected void setProperties(final UIComponent component) {
		super.setProperties(component); //set the default properties
		setStringValue(component, VALUE_ATTRIBUTE, getValue()); //set the value
		setMethodBindingAttribute(component, INPUT_VALIDATOR_ATTRIBUTE, getValidator(), FacesContext.class, UIComponent.class, Object.class);
		//TODO fix converter
		setBooleanValue(component, IMMEDIATE_ATTRIBUTE, getImmediate()); //set the immediate attribute
		setBooleanValue(component, INPUT_REQUIRED_ATTRIBUTE, getRequired()); //set the required attribute
		setMethodBindingAttribute(component, INPUT_VALUE_CHANGE_LISTENER_ATTRIBUTE, getValueChangeListener(), ValueChangeEvent.class);
	}

	/** Release our resources. */
	public void release() {
		super.release(); //release the default resources
		value = null;
		validator = null;
		//TODO implement converter attribute
		immediate = null;
		required = null;
		valueChangeListener = null;
	}
}
