package com.garretwilson.faces.taglib.xhtml;

import javax.faces.application.Application;
import javax.faces.component.*;
import javax.faces.context.FacesContext;
import javax.faces.webapp.UIComponentTag;

import com.garretwilson.faces.*;
import com.garretwilson.faces.component.*;
import com.garretwilson.faces.component.renderkit.xhtml.ButtonRenderer;
import com.garretwilson.faces.el.*;

/**A tag representing a command rendered as a button.
@author Garret Wilson
*/
public class CommandButtonTag extends AbstractCommandTag
{

	/**@return The default renderer for this component.*/
	public String getRendererType() {return "javax.faces.Button";}	//TODO use a constant

	/**Sets the component properties from the tag's attributes.
	@param component The component the properties of which should be set.
	*/
	protected void setProperties(final UIComponent component)
	{
		super.setProperties(component);	//set the default properties
   }
}
