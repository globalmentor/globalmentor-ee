package com.garretwilson.faces.taglib.xhtml;

import javax.faces.application.Application;
import javax.faces.component.*;
import javax.faces.context.FacesContext;
import javax.faces.webapp.UIComponentTag;

import com.garretwilson.faces.*;
import com.garretwilson.faces.component.*;
import com.garretwilson.faces.component.renderkit.xhtml.ButtonRenderer;
import com.garretwilson.faces.el.*;

/**An abstract base class for command component and renderer tags.
@author Garret Wilson
*/
public abstract class AbstractCommandTag extends AbstractXHTMLTag
{

	/**@return The type of the component to use for this tag.*/
	public String getComponentType() {return UICommand.COMPONENT_TYPE;}

	/**The command action.*/
	private String action;

		/**@return The command action.*/
		public String getAction() {return action;}

		/**Sets the command action.
		@param action The new command action.
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
		if(component instanceof UIBasicCommand)	//if the component is a basic command with its extended functionality
		{
			final UIBasicCommand command=(UIBasicCommand)component;	//cast the component to a command component TODO maybe refactor this out
			if(getValue()!=null)	//if we have a value
			{
					//create an expression from the value
				command.setValueExpression(ExpressionUtilities.createExpression(getFacesContext(), getValue()));
			}
		}
   }

	/**Release our resources.*/
	public void release()
	{
		super.release();	//release the default resources
		value=null;
	}
}
