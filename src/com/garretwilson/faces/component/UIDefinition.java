package com.garretwilson.faces.component;

import javax.faces.component.UIColumn;

import com.garretwilson.lang.ClassUtilities;

/**A component representing a definition in a list of definitions.
@author Garret Wilson
@see UIDefinitionList
@see UITerm
*/
public class UIDefinition extends UIColumn
{
	/**The identifier for this component type.*/
	public static final String COMPONENT_TYPE=ClassUtilities.getFullName(UIDefinition.class, "Definition");

}
