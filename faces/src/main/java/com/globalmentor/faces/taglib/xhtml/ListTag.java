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

package com.globalmentor.faces.taglib.xhtml;

import javax.faces.component.UIComponent;

import com.globalmentor.faces.component.*;
import com.globalmentor.faces.component.renderkit.xhtml.ListRenderer;

import static com.globalmentor.faces.component.FacesComponentConstants.*;
import static com.globalmentor.faces.component.FacesComponents.*;

/**
 * A tag representing a list component.
 * @author Garret Wilson
 */
public class ListTag extends AbstractXHTMLTag {

	/** @return The type of the component to use for this tag. */
	public String getComponentType() {
		return UIList.COMPONENT_TYPE;
	}

	/** @return The default renderer for this component. */
	public String getRendererType() {
		return ListRenderer.RENDERER_TYPE;
	}

	/** The value of the list data. */
	private String value;

	/** @return The value of the list data. */
	public String getValue() {
		return value;
	}

	/**
	 * Sets the value of the list data.
	 * @param value The value of the list data.
	 */
	public void setValue(final String value) {
		this.value = value;
	}

	/** The variable used for iterating through the list rows. */
	private String var;

	/** @return The variable used for iterating through the list rows. */
	public String getVar() {
		return var;
	}

	/**
	 * Sets The variable used for iterating through the list rows.
	 * @param var The variable used for iterating through the list rows.
	 */
	public void setVar(final String var) {
		this.var = var;
	}

	/**
	 * Sets the component properties from the tag's attributes.
	 * @param component The component the properties of which should be set.
	 */
	protected void setProperties(final UIComponent component) {
		super.setProperties(component); //set the default properties
		/*TODO fix if needed
		if (value != null)
		{
		if (isValueReference(value))
		{
		viewer.setValueBinding("rows", app.createValueBinding(rows));
		}
		else
		{
		viewer.setRows(Integer.parseInt(rows));
		}
		}      
		*/
		setStringValue(component, UIBasicData.VAR_ATTRIBUTE, getVar()); //store the variable name
		setStringValue(component, VALUE_ATTRIBUTE, getValue()); //set the value
	}

	/** Release our resources. */
	public void release() {
		super.release(); //release the default resources
		value = null;
		var = null;
	}
}
