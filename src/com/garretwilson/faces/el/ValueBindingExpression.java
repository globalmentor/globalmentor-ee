package com.garretwilson.faces.el;

import javax.faces.context.FacesContext;
import javax.faces.el.*;
import com.garretwilson.util.Debug;

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
//G***del Debug.setDebug(true);
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
/*G***del
Debug.trace("getting value of a value-binding expression: ", getExpressionString());
Debug.trace("value: ", getValueBinding().getValue(context));
Debug.trace("type: ", getValueBinding().getType(context));
Debug.trace("type of value: ", getValueBinding().getValue(context).getClass());
*/
		return (T)getValueBinding().getValue(context);	//return the value cast to the correct type
	}

	/**Set the value of the expression.
	@param context The JSF context for the current request.
	@param value The new value to be set.
	@exception EvaluationException if an exception is thrown while setting
		the value (the thrown exception must be included as the
		<code>cause</code> property of this exception).
	@exception NullPointerException if <code>context</code>
		is <code>null</code>.
	@exception PropertyNotFoundException if a specified property name
		does not exist, or is not writeable
	*/
	public void setValue(final FacesContext context, final T value) throws EvaluationException, PropertyNotFoundException
	{
		getValueBinding().setValue(context, value);	//set the value of the value binding
	}

	/**Determines if the expression is immutable.
	@param context The JSF context for the current request.
	@exception EvaluationException if an exception is thrown while getting
		the description of the property (the thrown exception must be
		included as the <code>cause</code> property of this exception).
	@exception NullPointerException if <code>context</code>
		is <code>null</code>.
	@exception PropertyNotFoundException if a specified property name
		does not exist.
	*/
	public boolean isReadOnly(final FacesContext context) throws EvaluationException, PropertyNotFoundException
	{
		return getValueBinding().isReadOnly(context);	//return whether the underlying value binding is read-only
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
/*G***del
Debug.trace("getting type of a value-binding expression: ", getExpressionString());
Debug.trace("value: ", getValueBinding().getValue(context));
Debug.trace("type: ", getValueBinding().getType(context));
Debug.trace("type of value: ", getValueBinding().getValue(context).getClass());
*/
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
