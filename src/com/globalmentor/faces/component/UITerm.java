package com.globalmentor.faces.component;

import java.io.File;
import java.net.URI;
import javax.faces.component.UIColumn;

import com.globalmentor.java.Classes;

/**A component representing a term in a list of definitions.
@author Garret Wilson
@see UIDefinitionList
@see UIDefinition
*/
public class UITerm extends UIColumn
{

	public static final String COMPONENT_TYPE=Classes.getFullName(UITerm.class, "Term");

	public UITerm()
	{
		super();
	}

}
