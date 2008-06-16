package com.globalmentor.faces.taglib.xhtml;

import com.globalmentor.faces.component.UITerm;

/**A tag representing a definition in a list.
@author Garret Wilson
@see com.globalmentor.faces.taglib.xhtml.DefinitionListTag
@see com.globalmentor.faces.taglib.xhtml.DefinitionTag
*/
public class TermTag extends AbstractXHTMLTag
{

	/**@return <code>null</code> indicating no default renderer.*/
	public String getRendererType() {return null;}

	/**@return The type of the component to use for this tag.*/
	public String getComponentType() {return UITerm.COMPONENT_TYPE;}

}

