package com.garretwilson.faces.taglib.xhtml;

import com.garretwilson.faces.component.UIDefinitionList;
import com.garretwilson.faces.component.renderkit.xhtml.DefinitionListRenderer;

/**A tag representing a definition list component.
@author Garret Wilson
@see com.garretwilson.faces.taglib.xhtml.DefinitionTag
@see com.garretwilson.faces.taglib.xhtml.TermTag
*/
public class DefinitionListTag extends ListTag
{

	/**@return The type of the component to use for this tag.*/
	public String getComponentType() {return UIDefinitionList.COMPONENT_TYPE;}

	/**@return The default renderer for this component.*/
	public String getRendererType() {return DefinitionListRenderer.RENDERER_TYPE;}

}
