package com.garretwilson.faces.taglib.xhtml;

import javax.faces.application.Application;
import javax.faces.component.*;
import javax.faces.context.FacesContext;
import javax.faces.webapp.UIComponentTag;

import com.garretwilson.faces.*;
import com.garretwilson.faces.component.*;
import com.garretwilson.faces.el.*;
import com.garretwilson.faces.component.ComponentUtilities;

import static com.garretwilson.faces.FacesConstants.*;
import static com.garretwilson.faces.component.ComponentConstants.*;
import static com.garretwilson.faces.component.ComponentUtilities.*;
import static com.garretwilson.text.xml.xhtml.XHTMLConstants.*;

/**A tag representing a form component.
@author Garret Wilson
*/
public class FormTag extends AbstractXHTMLTag
{

	/**@return The type of the component to use for this tag.*/
	public String getComponentType() {return UIForm.COMPONENT_TYPE;}

	/**@return The default renderer for this component.*/
	public String getRendererType() {return FORM_RENDER_TYPE;}

	/**The encoding type.*/
	private String enctype;

		/**@return The encoding type.*/
		public String getEnctype() {return enctype;}

		/**Sets the encoding type.
		@param enctype The encoding type.
		*/
		public void setEnctype(final String enctype) {this.enctype=enctype;}

	/**Sets the component properties from the tag's attributes.
	@param component The component the properties of which should be set.
	*/
	protected void setProperties(final UIComponent component)
	{
		super.setProperties(component);	//set the default properties
		setStringValue(component, ELEMENT_FORM_ATTRIBUTE_ENCTYPE, getEnctype());
	}

	/**Release our resources.*/
	public void release()
	{
		super.release();	//release the default resources
		enctype=null;
	}
}