package com.garretwilson.faces.el;

import javax.faces.context.FacesContext;
import javax.faces.el.*;

/**The representation of a value bound to a property.
@author Garret Wilson
*/
public class ValueBindingExpression<T> implements Expression<T>
{

	/**The value binding.*/
	private final ValueBinding valueBinding;

		/**@return The value binding.*/
		protected ValueBinding getValueBinding() {return valueBinding;}

	/**Value binding constructor.
	@param valueBinding The value binding.
	*/
	public ValueBindingExpression(final ValueBinding valueBinding)
	{
		this.valueBinding=valueBinding;	//save the value binding
	}

	/**Determines the value of the expression.
	@param context The JSF context.
	@return The value of the expression.
	@exception EvaluationException if an exception is thrown while getting
		the value (the thrown exception must be included as the
		<code>cause</code> property of this exception).
	@exception NullPointerException if <code>context</code> is <code>null</code>.
	@exception PropertyNotFoundException if the value specifies a property name
		that does not exist or is not readable.
	@exception MethodNotFoundException if the value specifies a method that
		cannot be found.
	*/
	public T getValue(final FacesContext context) throws EvaluationException, NullPointerException, PropertyNotFoundException, MethodNotFoundException
	{
		return (T)getValueBinding().getValue(context);	//return the value cast to the correct type
	}

	/**Returns the type of value represented by this expression.
	@param context The JSF context.
	@return The type of object that will be returned as the value.
	@exception EvaluationException if an exception is thrown while getting
		the value (the thrown exception must be included as the
		<code>cause</code> property of this exception).
	@exception NullPointerException if <code>context</code> is <code>null</code>.
	@exception PropertyNotFoundException if the value specifies a property name
		that does not exist or is not readable.
	@exception MethodNotFoundException if the value specifies a method that
		cannot be found.
	*/
	public Class getType(FacesContext context) throws EvaluationException, NullPointerException, PropertyNotFoundException, MethodNotFoundException
	{
		return getValueBinding().getType(context);	//return the type of the value binding
	}

	/**@return the (possibly <code>null</code>) expression string,
		with leading and trailing delimiters, from which this
		expression was built.
	*/
	public String getExpressionString()
	{
		return getValueBinding().getExpressionString();	//return the value binding's expression string
	}

}
