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

package com.globalmentor.faces.component.renderkit.xhtml;

import javax.faces.component.*;

import com.globalmentor.faces.component.*;

import static com.globalmentor.w3c.spec.HTML.*;

/**Renders XHTML definition lists.
@author Garret Wilson
*/
public class DefinitionListRenderer extends ListRenderer
{

	/**The type of renderer for an XHTML definition list.*/
	public static final String RENDERER_TYPE=UIDefinitionList.COMPONENT_TYPE;

	/**Default constructor.*/
	public DefinitionListRenderer()
	{
		super(ELEMENT_DL);	//use the <dl> element for definition list rendering
	}

	/**@return The name of the XML element for an item in a column.
	@param column The column for which to return the list item name.
	*/
	protected String getItemElementName(final UIColumn column)
	{
		if(column instanceof UITerm) {	//if this is a term
			return ELEMENT_DT;	//return the term element
		}
		else if(column instanceof UIDefinition) {	//if this is a definition
			return ELEMENT_DD;	//return the definition element
		}
		else {	//if we don't recognize the type of column
			return super.getItemElementName(column);	//return the default item element name for this column
		}
	}
}
