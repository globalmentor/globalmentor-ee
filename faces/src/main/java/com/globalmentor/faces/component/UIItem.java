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

package com.globalmentor.faces.component;

import javax.faces.component.UIColumn;

import com.globalmentor.java.Classes;

/**
 * A component representing an item in a list.
 * @author Garret Wilson
 * @see UIList
 */
public class UIItem extends UIColumn {

	/** The name of the component. */
	public static final String COMPONENT_TYPE = Classes.getFullName(UIItem.class, "Item");

	/** Constructor. */
	public UIItem() {
		super();
	}

}
