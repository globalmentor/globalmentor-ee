package com.garretwilson.faces.taglib.xhtml;

import com.garretwilson.faces.component.UIDefinition;

/**A tag representing a definition in a list.
@author Garret Wilson
@see com.garretwilson.faces.taglib.xhtml.DefinitionListTag
@see com.garretwilson.faces.taglib.xhtml.TermTag
*/
public class DefinitionTag extends AbstractXHTMLTag
{

	/**@return <code>null</code> indicating no default renderer.*/
	public String getRendererType() {return null;}

	/**@return The type of the component to use for this tag.*/
	public String getComponentType() {return UIDefinition.COMPONENT_TYPE;}

}
