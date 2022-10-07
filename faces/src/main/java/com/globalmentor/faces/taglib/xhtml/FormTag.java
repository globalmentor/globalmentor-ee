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

import javax.faces.component.*;

import static com.globalmentor.faces.Faces.*;
import static com.globalmentor.faces.component.FacesComponents.*;
import static com.globalmentor.html.spec.HTML.*;

/**
 * A tag representing a form component.
 * @author Garret Wilson
 */
public class FormTag extends AbstractXHTMLTag {

	/** @return The type of the component to use for this tag. */
	public String getComponentType() {
		return UIForm.COMPONENT_TYPE;
	}

	/** @return The default renderer for this component. */
	public String getRendererType() {
		return FORM_RENDER_TYPE;
	}

	/** The encoding type. */
	private String enctype;

	/** @return The encoding type. */
	public String getEnctype() {
		return enctype;
	}

	/**
	 * Sets the encoding type.
	 * @param enctype The encoding type.
	 */
	public void setEnctype(final String enctype) {
		this.enctype = enctype;
	}

	/**
	 * Sets the component properties from the tag's attributes.
	 * @param component The component the properties of which should be set.
	 */
	protected void setProperties(final UIComponent component) {
		super.setProperties(component); //set the default properties
		setStringValue(component, ELEMENT_FORM_ATTRIBUTE_ENCTYPE, getEnctype());
	}

	/** Release our resources. */
	public void release() {
		super.release(); //release the default resources
		enctype = null;
	}
}
