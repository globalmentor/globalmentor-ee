package com.garretwilson.faces.taglib.xhtml;

import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.webapp.UIComponentTag;

import com.garretwilson.faces.*;
import com.garretwilson.faces.component.*;
import com.garretwilson.faces.component.renderkit.xhtml.ListRenderer;
import com.garretwilson.faces.el.*;

/**A tag representing a list component.
@author Garret Wilson
*/
public class ListTag extends AbstractXHTMLTag
{

	/**@return The type of the component to use for this tag.*/
	public String getComponentType() {return UIList.COMPONENT_TYPE;}

	/**@return The default renderer for this component.*/
	public String getRendererType() {return ListRenderer.RENDERER_TYPE;}

	/**The value of the list data.*/
	private String value;

		/**@return The value of the list data.*/
		public String getValue() {return value;}

		/**Sets the value of the list data.
		@param value The value of the list data.
		*/
		public void setValue(final String value) {this.value=value;}

	/**The variable used for iterating through the list rows.*/
	private String var;

		/**@return The variable used for iterating through the list rows.*/
		public String getVar() {return var;}

		/**Sets The variable used for iterating through the list rows.
		@param var The variable used for iterating through the list rows.
		*/
		public void setVar(final String var)  {this.var=var;}

	/**Sets the component properties from the tag's attributes.
	@param component The component the properties of which should be set.
	*/
	protected void setProperties(final UIComponent component)
	{
		super.setProperties(component);	//set the default properties
/*G***fix if needed
if (value != null)
{
if (isValueReference(value))
{
viewer.setValueBinding("rows", app.createValueBinding(rows));
}
else
{
viewer.setRows(Integer.parseInt(rows));
}
}      
*/
		final UIBasicData data=(UIBasicData)component;	//cast the component to a data component TODO probably move this up into a AbstractDataTag component
		ComponentUtilities.setStringValue(component, UIBasicData.VAR_ATTRIBUTE, getVar());	//store the variable name
		if(getValue()!=null)	//if we have a value
		{
				//create an expression from the value
			data.setValueExpression(ExpressionUtilities.createExpression(getFacesContext(), getValue()));
		}
   }

	/**Release our resources.*/
	public void release()
	{
		super.release();	//release the default resources
		value=null;
		var=null;
	}
}
