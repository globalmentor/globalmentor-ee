package com.garretwilson.faces.el;

import javax.faces.context.FacesContext;
import javax.faces.el.*;

/**The representation of a value bound to a method.
@author Garret Wilson
*/
public class MethodBindingExpression<T> implements Expression<T>
{

	/**The method binding.*/
	private final MethodBinding methodBinding;

		/**@return The method binding.*/
		protected MethodBinding getMethodBinding() {return methodBinding;}

	/**The non-null array of parameter expressions for the method call.*/
	private final Expression<?>[] parameterExpressions;

		/**@return The non-null array of parameter expressions for the method call.
		@see #getMethodBinding()
		*/
		protected Expression<?>[] getParameterExpressions() {return parameterExpressions;}

	/**Method binding constructor.
	@param methodBinding The method binding.
	@param parameterExpressions The parameter expressions to be used by the
		method binding invocation.
	*/
	public MethodBindingExpression(final MethodBinding methodBinding, final Expression<?>... parameterExpressions)
	{
		this.methodBinding=methodBinding;	//save the method binding
		this.parameterExpressions=parameterExpressions;	//save the parameter expressions
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
		final Expression<?>[] parameterExpressions=getParameterExpressions();	//get the parameter expressions
		final Object[] parameterValues=new Object[parameterExpressions.length];	//create an array of objects to hold the actual parameter values
		for(int i=0; i<parameterExpressions.length; ++i)	//look at each parameter expression
		{
			parameterValues[i]=parameterExpressions[i].getValue(context);	//get this parameter value
		}
		return (T)getMethodBinding().invoke(context, parameterValues);	//invoke the method with the parameter values and cast the return value to the correct type
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
		return getMethodBinding().getType(context);	//return the type of the method binding
	}
}
