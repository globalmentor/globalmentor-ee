package com.garretwilson.faces.taglib.xhtml;


import static com.garretwilson.faces.FacesConstants.*;

/**A tag representing a command rendered as a button.
@author Garret Wilson
*/
public class CommandButtonTag extends AbstractCommandTag
{

	/**@return The renderer for this component.*/
	public String getRendererType() {return BUTTON_RENDER_TYPE;}

}
