package com.garretwilson.faces.taglib.xhtml;

import javax.faces.webapp.UIComponentTag;

import com.garretwilson.faces.*;

import static com.garretwilson.faces.FacesConstants.*;

/**A tag representing a command rendered as a link.
@author Garret Wilson
*/
public class CommandLinkTag extends AbstractCommandTag
{

	/**@return The renderer for this component.*/
	public String getRendererType() {return LINK_RENDER_TYPE;}

}
