package com.garretwilson.faces.taglib.xhtml;

import javax.faces.component.*;
import javax.faces.context.FacesContext;
import javax.faces.event.*;

import com.garretwilson.faces.*;
import com.garretwilson.faces.component.*;
import com.garretwilson.faces.el.*;
import com.garretwilson.util.Debug;

import static com.garretwilson.faces.component.ComponentConstants.*;
import static com.garretwilson.faces.component.ComponentUtilities.*;
import static com.garretwilson.faces.el.ExpressionUtilities.*;

/**An abstract base class for input component and renderer tags.
@author Garret Wilson
*/
public abstract class AbstractInputTag extends AbstractXHTMLTag
{

	/**@return The type of the component to use for this tag.*/
	public String getComponentType() {return UIInput.COMPONENT_TYPE;}

	/**The value of the component.*/
	private String value;

		/**@return The value of the component.*/
		public String getValue() {return value;}

		/**Sets the value of the component.
		@param value The value of the component.
		*/
		public void setValue(final String value) {this.value=value;}

	/**The component validator.*/
	private String validator;

		/**@return The component validator.*/
		public String getValidator() {return validator;}

		/**Sets the component validator.
		@param validator The new component validator.
		*/
		public void setValidator(final String validator) {this.validator=validator;}

//TODO implement converter atribute
	
	/**Whether the input is immediate.*/
	private String immediate;

		/**@return Whether the input is immediate.*/
		public String getImmediate() {return immediate;}

		/**Sets whether the input is immediate.
		@param immediate Whether the input should be immediate.
		*/
		public void setImmediate(final String immediate) {this.immediate=immediate;}

	/**Whether the input is required.*/
	private String required;

		/**@return Whether the input is required.*/
		public String getRequired() {return required;}

		/**Sets whether the input is required.
		@param required Whether the input is required.
		*/
		public void setRequired(final String required) {this.required=required;}

	/**The component value change listener.*/
	private String valueChangeListener;

		/**@return The component valuel change listener.*/
		public String getValueChangeListener() {return valueChangeListener;}

		/**Sets the component value change listener.
		@param valueChangeListener The new component value change listener.
		*/
		public void setValueChangeListener(final String valueChangeListener) {this.valueChangeListener=valueChangeListener;}

	/**Sets the component properties from the tag's attributes.
	@param component The component the properties of which should be set.
	*/
	protected void setProperties(final UIComponent component)
	{
		super.setProperties(component);	//set the default properties
		setStringValue(component, VALUE_ATTRIBUTE, getValue());	//set the value
		setMethodBindingAttribute(component, INPUT_VALIDATOR_ATTRIBUTE, getValidator(), FacesContext.class, UIComponent.class, Object.class);
//TODO fix converter
		setBooleanValue(component, IMMEDIATE_ATTRIBUTE, getImmediate());	//set the immediate attribute
		setBooleanValue(component, INPUT_REQUIRED_ATTRIBUTE, getRequired());	//set the required attribute
		setMethodBindingAttribute(component, INPUT_VALUE_CHANGE_LISTENER_ATTRIBUTE, getValueChangeListener(), ValueChangeEvent.class);
   }

	/**Release our resources.*/
	public void release()
	{
		super.release();	//release the default resources
		value=null;
		validator=null;
//TODO implement converter attribute
		immediate=null;
		required=null;
		valueChangeListener=null;
	}
}
