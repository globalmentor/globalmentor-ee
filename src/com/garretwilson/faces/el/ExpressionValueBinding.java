package com.garretwilson.faces.el;

import javax.faces.context.FacesContext;
import javax.faces.el.*;

/**A <code>ValueBinding</code> subclass that allows a general
	<code>Expression</code> to be used wherever a <code>ValueBinding</code>
	instance is called for.
@author Garret Wilson
@see Expression
*/
public class ExpressionValueBinding extends ValueBinding
{

	/**The represented expression.*/
	private final Expression<?> expression;

		/**@return The represented expression.*/
		protected Expression<?> getExpression() {return expression;}

	/**Expression constructor.
	@param expression The represented expression.
	@exception NullPointerException Thrown if the expression is <code>null</code>.
	*/
	public ExpressionValueBinding(final Expression<?> expression)
	{
		this.expression=expression;	//save the expression
		if(expression==null)	//if there is no expression
		{
			throw new NullPointerException("No expression provided.");
		}
	}

	/**Return the value of the expression.
	@param context The JSF context for the current request.
	@exception EvaluationException if an exception is thrown while getting
		the value (the thrown exception must be included as the
		<code>cause</code> property of this exception)
	@exception NullPointerException if <code>context</code>
		is <code>null</code>.
	@exception PropertyNotFoundException if a specified property name
		does not exist, or is not readable
	@see Expression#getValue(FacesContext)
	*/
	public Object getValue(final FacesContext context) throws EvaluationException, PropertyNotFoundException
	{
		return getExpression().getValue(context);	//return the expression's value
	}

	/**Set the value of the property represented by this value binding.
	@param context The JSF context for the current request.
	@param value The new value to be set.
	@exception EvaluationException if an exception is thrown while setting
		the value (the thrown exception must be included as the
		<code>cause</code> property of this exception)
	@exception NullPointerException if <code>context</code>
		is <code>null</code>.
	@exception PropertyNotFoundException if a specified property name
		does not exist, or is not writeable
	*/
	public void setValue(final FacesContext context, final Object value) throws EvaluationException, PropertyNotFoundException
	{
		throw new EvaluationException(new UnsupportedOperationException());	//we don't support setting an expression value TODO implement value-setting this in Expression<T>
	}

	/**Determines if specified property is immutable.
	@param context The JSF context for the current request.
	@exception EvaluationException if an exception is thrown while getting
		the description of the property (the thrown exception must be
		included as the <code>cause</code> property of this exception)
	@exception NullPointerException if <code>context</code>
		is <code>null</code>.
	@exception PropertyNotFoundException if a specified property name
		does not exist.
	*/
	public boolean isReadOnly(final FacesContext context) throws EvaluationException, PropertyNotFoundException
	{
		return true;	//we currently don't support setting an expression TODO add expression value setting
	}


	/**Return the type of the property represented by this value binding.
	@param context The JSF context for the current request.
	@exception EvaluationException if an exception is thrown while getting
		the description of the property (the thrown exception must be
		included as the <code>cause</code> property of this exception)
	@exception NullPointerException if <code>context</code>
		is <code>null</code>.
	@exception PropertyNotFoundException if a specified property name
		does not exist.
	@see Expression#getType(FacesContext)
	*/
	public Class getType(final FacesContext context) throws EvaluationException, PropertyNotFoundException
	{
		return getExpression().getType(context);	//return the expression type
	}

	/**Return the (possibly <code>null</code>) expression string,
		with leading and trailing delimiters, from which this
		<code>ValueBinding</code> was built.
	@see Expression#getExpressionString()
	*/
	public String getExpressionString()
	{
		return getExpression().getExpressionString();	//return the expression's expresion string
	}

}
