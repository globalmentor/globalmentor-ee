package com.garretwilson.faces.component;

import javax.faces.component.UIData;
import com.garretwilson.faces.el.*;

/**Basic data component with enhanced functionality.
@author Garret Wilson
*/
public class UIBasicData extends UIData
{

	/**The attribute for storing the variable name.*/
	public final static String VAR_ATTRIBUTE="var";
	
	/**The expression representing the value, or <code>null</code> if there is no value expression.*/
	private Expression<?> valueExpression=null;

		/**@return The expression representing the value, or <code>null</code> if there is no value expression.
		@see UIData#getValue()
		*/
		public Expression<?> getValueExpression() {return valueExpression;}

		/**Sets the expression representing the value, which overrides any value binding
			or locally stored value.
		@param valueExpression The value expression to be represented by this data model.
		@see UIData#setValue()
		*/
		public void setValueExpression(final Expression<?> valueExpression) {this.valueExpression=valueExpression;}

	/**Default constructor.*/
	public UIBasicData()
	{
		super();	//construct the parent class
		setVar(VAR_ATTRIBUTE);	//show which attribute we use for storing the variable name
	}

	/**Return the value of the UIData.  This value must either be 
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
