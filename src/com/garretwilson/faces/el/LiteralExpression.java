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
		final StringBuilder expressionStringBuilder=new StringBuilder();	//we'll build the expression
		expressionStringBuilder.append(REFERENCE_CHAR);	//#
		expressionStringBuilder.append(REFERENCE_EXPRESSION_BEGIN_CHAR);	//{
		expressionStringBuilder.append(value.toString());	//value TODO should we put quotes around this if it's a string?
		expressionStringBuilder.append(REFERENCE_EXPRESSION_END_CHAR);	//}
		return expressionStringBuilder.toString();	//return the expression string we built
	}

}
