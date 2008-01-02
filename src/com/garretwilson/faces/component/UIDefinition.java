package com.garretwilson.faces.component;

import javax.faces.component.UIColumn;

import com.globalmentor.java.Classes;

/**A component representing a definition in a list of definitions.
@author Garret Wilson
@see UIDefinitionList
@see UITerm
*/
public class UIDefinition extends UIColumn
{
	/**The identifier for this component type.*/
	public static final String COMPONENT_TYPE=Classes.getFullName(UIDefinition.class, "Definition");

}
