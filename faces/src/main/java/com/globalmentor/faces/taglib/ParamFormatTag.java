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

package com.globalmentor.faces.taglib;

import javax.faces.component.UIComponent;
import javax.faces.webapp.UIComponentTag;

import com.globalmentor.faces.component.UIParameterFormat;

import static com.globalmentor.faces.component.FacesComponentConstants.*;
import static com.globalmentor.faces.component.FacesComponents.*;

/**
 * A tag representing a definition in a list.
 * @author Garret Wilson
 */
public class ParamFormatTag extends UIComponentTag {

	/** @return <code>null</code> indicating no default renderer. */
	public String getRendererType() {
		return null;
	}

	/** @return The type of the component to use for this tag. */
	public String getComponentType() {
		return UIParameterFormat.COMPONENT_TYPE;
	}

	/** The parameter name. */
	private String name = null;

	/** @return The parameter name. */
	public String getName() {
		return name;
	}

	/**
	 * Sets parameter name.
	 * @param name The parameter name.
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/** The value of the component. */
	private String value = null;

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

	/**
	 * Sets the component properties from the tag's attributes.
	 * @param component The component the properties of which should be set.
	 */
	protected void setProperties(final UIComponent component) {
		super.setProperties(component); //set the default properties
		setStringValue(component, NAME_ATTRIBUTE, getName()); //set the namne
		setStringValue(component, VALUE_ATTRIBUTE, getValue()); //set the value
	}

	/** Release our resources. */
	public void release() {
		super.release(); //release the default resources
		name = null;
		value = null;
	}

}
