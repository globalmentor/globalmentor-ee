package com.globalmentor.faces.taglib.xhtml;

import com.globalmentor.faces.*;

import static com.globalmentor.faces.Faces.*;

/**A tag representing a command rendered as a link.
@author Garret Wilson
*/
public class CommandLinkTag extends AbstractCommandTag
{

	/**@return The renderer for this component.*/
	public String getRendererType() {return LINK_RENDER_TYPE;}

}
