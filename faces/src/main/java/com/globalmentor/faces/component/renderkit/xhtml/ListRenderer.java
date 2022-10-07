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

import javax.faces.component.*;

import com.globalmentor.faces.component.UIList;

import static com.globalmentor.html.spec.HTML.*;

/**
 * The default renderer for an XHTML ordered list
 * @author Garret Wilson
 */
public class ListRenderer extends DataRenderer {

	/** The type of renderer for XHTML &lt;ol&gt;. */
	public static final String RENDERER_TYPE = UIList.COMPONENT_TYPE;

	/** The name of the XML element for the component. */
	//TODO del	private final String componentElementName;

	/** @return The name of the XML element for the component. */
	//TODO del		protected String getComponentElementName() {return componentElementName;}

	/** The name of the XML element for an item in a column. */
	private final String itemElementName;

	/** @return The name of the XML element for an item in a column. */
	protected String getItemElementName() {
		return itemElementName;
	}

	/** Default constructor. */
	public ListRenderer() {
		this(ELEMENT_OL); //default to the <ol> element for the list
	}

	/**
	 * List element name constructor with a default list item name.
	 * @param listElementName The name of the XML element for the list.
	 */
	public ListRenderer(final String listElementName) {
		this(listElementName, ELEMENT_LI); //default to the <li> element for each list item
	}

	/**
	 * List element name and item element name constructor.
	 * @param listElementName The name of the XML element for the list.
	 * @param itemElementName The name of the XML element for an item in a column.
	 */
	public ListRenderer(final String listElementName, final String itemElementName) {
		super(listElementName); //construct the parent with the name of the XML list element
		//TODO del		this.componentElementName=listElementName;
		this.itemElementName = itemElementName;
	}

	/**
	 * @return The name of the XML element for an item in a column.
	 * @param column The column for which to return the list item name.
	 */
	protected String getItemElementName(final UIColumn column) {
		return itemElementName;
	}
}
