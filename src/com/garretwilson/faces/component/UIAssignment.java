package com.garretwilson.faces.component;

import java.io.IOException;
import javax.faces.application.Application;
import javax.faces.component.*;
import javax.faces.context.FacesContext;
import javax.faces.el.*;
import com.garretwilson.faces.*;
import com.garretwilson.faces.el.*;
import com.garretwilson.lang.ClassUtilities;

/**Represents an assignment to a variable of a value.
The value may be an object or, if a string, a value-binding expression.
A method may also be used to retrieve a value; if so, any
	<code>UIParameter</code> children are used as parameters to the method.
*/
public class UIAssignment extends UIComponentBase
{

	/**The component type.*/
	public static final String COMPONENT_TYPE=ClassUtilities.getFullName(UIAssignment.class, "Assignment");

	/**The component family.*/
	public static final String COMPONENT_FAMILY=COMPONENT_TYPE;

	/**The name of the value binding variable.*/
//G***del	public static final String VALUE_BINDING_VAR="value";

	/**The name of the method binding variable.*/
//G***del	public static final String METHOD_BINDING_VAR="method";

	/**@return The component family.*/
	public String getFamily()
	{
		return (COMPONENT_FAMILY);
	}

	/**Constructor.*/
	public UIAssignment()
	{
		super();	//construct the parent component
		setRendererType(null);	//assignments don't have renderers
	}

	/**The name of the variable to hold the result of the assignment.*/
	private String var=null;

		/**@return The name of the variable to hold the result of the assignment.*/
		public String getVar() {return var;}

		/**Sets the name of the variable to hold the result of the assignment.
		@param var The name of the variable to hold the assignment.
		*/
		public void setVar(final String var) {this.var=var;}

	/**The expression encapsulating the value or reference.*/
	private Expression<?> expression;

		/**@return The expression encapsulating the value or reference.*/
		public Expression<?> getExpression() {return expression;}

		/**Sets the expression.
		@param expression The expression encapsulating the value or reference.
		*/
		public void setExpression(final Expression<?> expression) {this.expression=expression;}

	/**Assigns the value or the method invocation result to the variable.
	@param context The JSF context.
	@exception IOException Thrown if there is an error writing to the output.
	@exception NullPointerException Thrown if there is neither a value binding
		or a method binding, or there is no parameter array for the method binding.
	*/
	public void encodeBegin(final FacesContext context) throws IOException
	{
		super.encodeBegin(context);	//do the default encoding
		if(isRendered())	//if this component is rendered
		{
			final Expression<?> expression=getExpression();	//get our value expression
			if(expression!=null)	//if we have an expression
			{
				final Object value=expression.getValue(context);	//get the expression value
				final Application application=context.getApplication();	//get the JSF application
				final ValueBinding varValueBinding=application.createValueBinding("#{"+getVar()+"}");	//create a value binding for the variable TODO refactor into an ExpressionUtilities method
				varValueBinding.setValue(context, value);	//assign the expression value to the variable
			}
			else	//if we have no value binding or method binding
			{
				throw new NullPointerException("No expression to assign.");
			}
		}
	}

	/**Saves the state of the component.
	@param context The faces context.
	*/
	public Object saveState(final FacesContext context)
	{
		return new Object[]{super.saveState(context), var};	//TODO finish
	}

	/**Restores the state of the component.
	@param context The faces context.
	@param state The state to restore.
	*/
	public void restoreState(final FacesContext context, final Object state)
	{
		final Object values[]=(Object[])state;
		super.restoreState(context, values[0]);
		var=(String)values[1];
			//TODO finish
	}

}
