package com.garretwilson.faces.component;

import javax.faces.component.UICommand;
import com.garretwilson.faces.el.*;

/**Basic command component with enhanced functionality.
@author Garret Wilson
*/
public class UIBasicCommand extends UICommand
{

	/**The expression representing the value, or <code>null</code> if there is no value expression.*/
	private Expression<?> valueExpression=null;

		/**@return The expression representing the value, or <code>null</code> if there is no value expression.
		@see #getValue()
		*/
		public Expression<?> getValueExpression() {return valueExpression;}

		/**Sets the expression representing the value, which overrides any value binding
			or locally stored value.
		@param valueExpression The value expression to be represented by this data model.*/
		public void setValueExpression(final Expression<?> valueExpression) {this.valueExpression=valueExpression;}

	/**Default constructor.*/
	public UIBasicCommand()
	{
		super();	//construct the parent class
	}

	/**Return the value of the component.  This value must either be 
		be of type <code>DataModel</code>, or a type that can be adapted
		into a <code>DataModel</code>.
	<p>This version uses the value of the value expression, if any; otherwise, it
		returns the value in the default manner.</p>
	*/
	public Object getValue()
	{
		final Expression<?> valueExpression=getValueExpression();	//get the value expression
		if(valueExpression!=null)	//if there is value expression
		{
			return valueExpression.getValue(getFacesContext());	//return the value of the expression
		}
		else	//if there is no value expression
		{
			return super.getValue();	//return the value in the default way
		}
	}

}
