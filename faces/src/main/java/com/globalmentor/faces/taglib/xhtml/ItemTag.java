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

import com.globalmentor.faces.component.UIItem;

/**
 * TODO comment
 * 
 * @author Garret Wilson
 *
 */
public class ItemTag extends AbstractXHTMLTag {

	public String getRendererType() {
		return null;
	}

	/** @return The type of the component to use for this tag. */
	public String getComponentType() {
		return UIItem.COMPONENT_TYPE;
	}

}
