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

	/**The value of the list data.*/
	private String value;

		/**@return The value of the list data.*/
		public String getValue() {return value;}

		/**Sets the value of the list data.
		@param value The value of the list data.
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
