package com.garretwilson.faces.taglib.xhtml;

import static com.garretwilson.faces.FacesConstants.*;

/**A tag representing a hypertext link.
@author Garret Wilson
*/
public class OutputLinkTag extends AbstractOutputTag
{
	/**@return The renderer for this component.*/
	public String getRendererType() {return LINK_RENDER_TYPE;}

}
