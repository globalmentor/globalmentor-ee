package com.garretwilson.faces.el;

import javax.faces.context.FacesContext;
import javax.faces.el.*;

import static com.garretwilson.faces.el.ExpressionConstants.*;

/**The representation of a literal value.
@author Garret Wilson
*/
public class LiteralExpression<T> implements Expression<T>
{

	/**The literal value.*/
	private final T value;

	/**Value constructor.
	@param value The literal value.
	*/
	public LiteralExpression(final T value)
	{
		this.value=value;	//save the value
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
		return value;	//return the literal value
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
		throw new EvaluationException(new UnsupportedOperationException());	//we don't support setting a literal value
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
		return true;	//literal values are always read-only
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
	public Class getType(final FacesContext context) throws EvaluationException, NullPointerException, PropertyNotFoundException, MethodNotFoundException
	{
		return value.getClass();	//return the type of the literal value
	}

	/**@return the (possibly <code>null</code>) expression string,
		with leading and trailing delimiters, from which this
		expression was built.
	*/
	public String getExpressionString()
	{
		return value.toString();	//return the string value of the literal expression
/*TODO fix if needed
		final StringBuilder expressionStringBuilder=new StringBuilder();	//we'll build the expression
		expressionStringBuilder.append(REFERENCE_CHAR);	//#
		expressionStringBuilder.append(REFERENCE_EXPRESSION_BEGIN_CHAR);	//{
		expressionStringBuilder.append(value.toString());	//value TODO should we put quotes around this if it's a string?
		expressionStringBuilder.append(REFERENCE_EXPRESSION_END_CHAR);	//}
		return expressionStringBuilder.toString();	//return the expression string we built
*/
	}

}
