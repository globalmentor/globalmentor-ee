package com.garretwilson.faces.component;

import com.garretwilson.faces.component.renderkit.xhtml.DefinitionListRenderer;
import com.garretwilson.lang.ClassUtilities;

/**A component containing a list of definitions.
@author Garret Wilson
*/
public class UIDefinitionList extends UIList
{
	public static final String COMPONENT_TYPE=ClassUtilities.getFullName(UIDefinitionList.class, "DefinitionList");

	/**Default constructor.*/
	public UIDefinitionList()
	{
		super();	//construct the parent class
		setRendererType(DefinitionListRenderer.RENDERER_TYPE);	//default to an XHTML &lt;dl&/gt; renderer
	}

}
