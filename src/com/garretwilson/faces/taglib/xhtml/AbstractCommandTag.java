package com.garretwilson.faces.taglib.xhtml;

import javax.faces.application.Application;
import javax.faces.component.*;
import javax.faces.context.FacesContext;

import com.garretwilson.faces.*;
import com.garretwilson.faces.component.*;
import com.garretwilson.faces.el.*;

import static com.garretwilson.faces.component.ComponentConstants.*;
import static com.garretwilson.faces.component.ComponentUtilities.*;
import static com.garretwilson.faces.el.ExpressionUtilities.*;

/**An abstract base class for command component and renderer tags.
@author Garret Wilson
*/
public abstract class AbstractCommandTag extends AbstractXHTMLTag
{

	/**@return The type of the component to use for this tag.*/
	public String getComponentType() {return UICommand.COMPONENT_TYPE;}

	/**The component action.*/
	private String action;

		/**@return The component action.*/
		public String getAction() {return action;}

		/**Sets the component acdtion.
		@param action The new component action.
		*/
		public void setAction(final String action) {this.action=action;}

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
		final FacesContext context=getFacesContext();	//get the JSF context
		final Application application=context.getApplication();	//get the JSF application
		final UICommand command=(UICommand)component;	//get the component as a command
		if(getAction()!=null)	//if we have an action
		{
				//create an expression from the action, and wrap it in a method-binding subclass so that UICommand will recognize it
			command.setAction(new ExpressionMethodBinding(createExpression(application, getAction())));
		}
		setStringValue(component, VALUE_ATTRIBUTE, getValue());	//set the value
   }

	/**Release our resources.*/
	public void release()
	{
		super.release();	//release the default resources
		action=null;
		value=null;
	}
}
