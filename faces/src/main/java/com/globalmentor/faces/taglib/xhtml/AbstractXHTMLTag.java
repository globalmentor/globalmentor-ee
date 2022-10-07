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
import javax.faces.webapp.UIComponentTag;

import static com.globalmentor.faces.component.FacesComponents.*;
import static com.globalmentor.faces.taglib.xhtml.XHTMLTags.*;

/**
 * The abstract base class for XHTML tags.
 * @author Garret Wilson
 */
public abstract class AbstractXHTMLTag extends UIComponentTag {

	/** The style class. */
	private String styleClass;

	/** @return The style class. */
	public String getStyleClass() {
		return styleClass;
	}

	/**
	 * Sets the style class.
	 * @param styleClass The style class.
	 */
	public void setStyleClass(final String styleClass) {
		this.styleClass = styleClass;
	}

	/**
	 * Sets the component properties from the tag's attributes.
	 * @param component The component the properties of which should be set.
	 */
	protected void setProperties(final UIComponent component) {
		super.setProperties(component); //set the default properties
		setStringValue(component, STYLE_CLASS_ATTRIBUTE, getStyleClass()); //store the style class
	}

	/** Release our resources. */
	public void release() {
		super.release(); //release the default resources
		styleClass = null;
	}
}
