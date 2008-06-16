package com.globalmentor.faces.taglib.xhtml;

import javax.faces.application.Application;
import javax.faces.component.*;
import javax.faces.context.FacesContext;

import com.globalmentor.faces.*;
import com.globalmentor.faces.component.*;
import com.globalmentor.faces.el.*;

import static com.globalmentor.faces.component.ComponentConstants.*;
import static com.globalmentor.faces.component.ComponentUtilities.*;

/**An abstract base class for output component and renderer tags.
@author Garret Wilson
*/
public abstract class AbstractOutputTag extends AbstractXHTMLTag
{

	/**@return The type of the component to use for this tag.*/
	public String getComponentType() {return UIOutput.COMPONENT_TYPE;}

	/**The value of the component.*/
	private String value;

		/**@return The value of the component.*/
		public String getValue() {return value;}

		/**Sets the value of the component.
		@param value The value of the component.
		*/
		public void setValue(final String value) {this.value=value;}

	/**Sets the component properties from the tag's attributes.
	@param component The component the properties of which should be set.
	*/
	protected void setProperties(final UIComponent component)
	{
		super.setProperties(component);	//set the default properties
		setStringValue(component, VALUE_ATTRIBUTE, getValue());	//set the value
   }

	/**Release our resources.*/
	public void release()
	{
		super.release();	//release the default resources
		value=null;
	}
}
