package com.garretwilson.faces.taglib.xhtml;

import com.garretwilson.faces.*;

import static com.garretwilson.faces.FacesConstants.*;

/**A tag representing a output rendered as text.
@author Garret Wilson
*/
public class OutputTextTag extends AbstractOutputTag
{

	/**@return The renderer for this component.*/
	public String getRendererType() {return TEXT_RENDER_TYPE;}

}
