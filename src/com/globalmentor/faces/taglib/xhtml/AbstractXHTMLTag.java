package com.globalmentor.faces.taglib.xhtml;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.webapp.UIComponentTag;

import com.globalmentor.faces.*;
import com.globalmentor.faces.component.*;
import com.globalmentor.faces.component.renderkit.xhtml.ListRenderer;
import com.globalmentor.faces.el.*;

import static com.globalmentor.faces.component.ComponentUtilities.*;
import static com.globalmentor.faces.taglib.xhtml.XHTMLTagConstants.*;

/**The abstract base class for XHTML tags.
@author Garret Wilson
*/
public abstract class AbstractXHTMLTag extends UIComponentTag
{

	/**The style class.*/
	private String styleClass;

		/**@return The style class.*/
		public String getStyleClass() {return styleClass;}

		/**Sets the style class.
		@param styleClass The style class.
		*/
		public void setStyleClass(final String styleClass) {this.styleClass=styleClass;}

	/**Sets the component properties from the tag's attributes.
	@param component The component the properties of which should be set.
	*/
	protected void setProperties(final UIComponent component)
	{
		super.setProperties(component);	//set the default properties
		setStringValue(component, STYLE_CLASS_ATTRIBUTE, getStyleClass());	//store the style class
   }

	/**Release our resources.*/
	public void release()
	{
		super.release();	//release the default resources
		styleClass=null;
	}
}
