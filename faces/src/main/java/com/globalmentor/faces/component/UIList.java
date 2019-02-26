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

package com.globalmentor.faces.component;

import com.globalmentor.faces.component.renderkit.xhtml.ListRenderer;
import com.globalmentor.java.Classes;

/**
 * A component containing an ordered list.
 * @author Garret Wilson
 */
public class UIList extends UIBasicData {
	public static final String COMPONENT_TYPE = Classes.getFullName(UIList.class, "List"); //TODO we probably don't even need a special list type

	/** Constructor. */
	public UIList() {
		super(); //construct the parent class
		setRendererType(ListRenderer.RENDERER_TYPE); //default to an XHTML &lt;dl&/gt; renderer
	}
}
