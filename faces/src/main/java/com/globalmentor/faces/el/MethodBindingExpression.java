/*
 * Copyright © 1996-2008 GlobalMentor, Inc. <http://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.globalmentor.faces.el;

import java.io.*;
import java.util.*;
import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import javax.faces.el.*;

import com.globalmentor.io.*;

import static com.globalmentor.faces.el.FacesExpressions.*;
import static com.globalmentor.java.Characters.*;

/**
 * The representation of a value bound to a method. The actual method binding that is used to get method type and value is only created when it is needed (e.g.
 * to actually invoke the method or to get its type). This is because when the expression is created, all its arguments may not available for invocation or for
 * type-checking.
 * @author Garret Wilson
 */
public class MethodBindingExpression<T> implements Expression<T> {

	/** A reference to the application that owns our method binding. */
	//TODO del	private final Application application;

	/** @return A reference to the application that owns our method binding. */
	//TODO del		protected Application getApplication() {return application;}

	/** The string form of the expression. */
	private final String expressionString;

	/** The string form of the expression for the method binding name expcted by standard JSF EL. */
	private final String methodReference;

	/** The lazily-created method binding. */
	//TODO del	private MethodBinding methodBinding=null;

	/**
	 * Creates the method binding.
	 * @param context The JSF context, needed to create the method binding
	 * @return The method binding for the current parameter types.
	 */
	protected MethodBinding getMethodBinding(final FacesContext context) {
		//TODO del			if(methodBinding==null)	//if we haven't created the method-binding, yet
		{
			final Expression<?>[] parameterExpressions = getParameterExpressions(); //get the parameter expressions
			final Class<?>[] parameterTypes = new Class<?>[parameterExpressions.length]; //create an array of classes, indicating parameter types
			for(int i = 0; i < parameterExpressions.length; ++i) { //look at each parameter expression
				parameterTypes[i] = parameterExpressions[i].getType(context); //get the type of this parameter
				/*TODO fix
				getLogger().trace("*****does this fix the type bug? parameter type for expression {} is {}", parameterExpressions[i].getExpressionString(), parameterTypes[i]);	//get the type of this parameter
				final Object object=parameterExpressions[i].getValue(context);
				getLogger().trace("what if we get the value of the expression? {} of type {}", object, object.getClass());
									parameterTypes[i]=object.getClass();	//get the type of this parameter
				
				getLogger().trace("let's create a new value binding");
				final ValueBinding valueBinding=context.getApplication().createValueBinding("#{group}");
				getLogger().trace("created a value binding {} of type {} and value {}", valueBinding.getExpressionString(), valueBinding.getType(context), valueBinding.getValue(context));
				
				getLogger().trace("let's get the resolved value");
				final Object resolvedObject=context.getApplication().getVariableResolver().resolveVariable(context, "group");
				getLogger().trace("here's the resolved value of type {} and value {}", resolvedObject.getClass(), resolvedObject);
				*/

				/*TODO del when works
				getLogger().trace("looking at parameter expression: {}", parameterExpressions[i].getExpressionString());
				//TODO fix---why does this always return java.lang.Object					parameterTypes[i]=parameterExpressions[i].getType(context);	//get the type of this parameter
				getLogger().trace("*****does this fix the type bug? {}", parameterExpressions[i].getType(context));	//get the type of this parameter
									final Object parameterValue=parameterExpressions[i].getValue(context);	//TODO fix type bug---don't actually get the value here
									parameterTypes[i]=parameterValue!=null ? parameterValue.getClass() : null;	//get the type of this parameter	//TODO fix type bug---don't actually get the value here
				//TODO del; doesn't work with null values					parameterTypes[i]=parameterExpressions[i].getValue(context).getClass();	//get the type of this parameter	//TODO fix type bug---don't actually get the value here
				getLogger().trace("parameter expression type: {}", parameterExpressions[i].getType(context).getName());
				*/
			}
			//TODO del getLogger().trace("actually creating method binding");
			final MethodBinding methodBinding = context.getApplication().createMethodBinding(methodReference, parameterTypes); //create a method binding and return it
			//TODO del getLogger().trace("method binding: {}", methodBinding);
			return methodBinding;
		}
		//TODO del			return methodBinding;	//return the method binding
	}

	/** The non-null array of parameter expressions for the method call. */
	private final Expression<?>[] parameterExpressions;

	/**
	 * @return The non-null array of parameter expressions for the method call.
	 * @see #getMethodBinding(FacesContext)
	 */
	protected Expression<?>[] getParameterExpressions() {
		return parameterExpressions;
	}

	/**
	 * Method binding constructor.
	 * @param application The JSF application.
	 * @param expressionString The lexical form of the expression.
	 * @throws IllegalArgumentException if <var>expresionString</var> does not contain a valid extended JSF expression language method reference.
	 */
	public MethodBindingExpression(final Application application, final String expressionString) throws IllegalArgumentException {
		this.expressionString = expressionString; //save the expression string
		final int expressionBeginIndex = expressionString.indexOf(REFERENCE_EXPRESSION_BEGIN_CHAR) + 1; //find out where this expression begins
		final int expressionEndIndex = expressionString.lastIndexOf(REFERENCE_EXPRESSION_END_CHAR); //find out where this expression ends
		final int groupBeginIndex = expressionString.indexOf(GROUP_BEGIN_CHAR, expressionBeginIndex) + 1; //find out where the parameters begin
		final int groupEndIndex = expressionString.lastIndexOf(GROUP_END_CHAR, expressionEndIndex - 1); //find out where the parameters end
		final String params = expressionString.substring(groupBeginIndex, groupEndIndex); //get a string representing the parameters
		final List<Expression<?>> parameterExpressionList = new ArrayList<Expression<?>>(); //create a new list for the parameter expressions
		final Reader paramsReader = new StringReader(params); //create a reader for our parameters
		//tokenize the parameters by commas, grouping by reference groups and quotes
		final ReaderTokenizer tokenizer = new ReaderTokenizer(paramsReader, TRIM_CHARACTERS.add(PARAMETER_SEPARATOR_CHAR),
				String.valueOf(REFERENCE_EXPRESSION_BEGIN_CHAR) + QUOTATION_MARK_CHAR + APOSTROPHE_CHAR,
				String.valueOf(REFERENCE_EXPRESSION_END_CHAR) + QUOTATION_MARK_CHAR + APOSTROPHE_CHAR);
		while(tokenizer.hasNext()) { //if there are more tokens
			final String param = tokenizer.next(); //get the next parameter
			final int length = param.length(); //get the length of the parameter
			assert param.length() > 0 : "Unexpected empty parameter."; //tokenizers should never return empty strings
			final char firstChar = param.charAt(0); //get the first character
			final Expression<?> parameterExpression; //we'll determine if this is a literal expression or a value-binding expression
			if(length > 1 && (firstChar == QUOTATION_MARK_CHAR || firstChar == APOSTROPHE_CHAR) && firstChar == param.charAt(length - 1)) { //if the parameter is at least two characters, with beginning and ending quotes
				final String string = param.substring(1, length - 1); //get the string between the quotes
				parameterExpression = new LiteralExpression<String>(string); //create a literal expression from the string

			} else { //if this is not a literal expression, assume it's a reference expression
				parameterExpression = createReferenceExpression(application, param); //create an expression for the parameter
			}
			//TODO fix getLogger().trace("created parameter expression of type: {}", parameterExpression.getType(context));
			parameterExpressionList.add(parameterExpression); //add the parameter expression to our list
		}
		//store the parameter expressions
		parameterExpressions = parameterExpressionList.toArray(new Expression[parameterExpressionList.size()]); //create an array of expressions for the parameter expressions
		final String methodName = expressionString.substring(expressionBeginIndex, groupBeginIndex - 1); //extract the name of the method
		methodReference = String.valueOf(REFERENCE_CHAR) + REFERENCE_EXPRESSION_BEGIN_CHAR + methodName + REFERENCE_EXPRESSION_END_CHAR; //construct the method reference that standard JSF EL expects
	}

	/**
	 * Determines the value of the expression.
	 * @param context The JSF context.
	 * @return The value of the expression.
	 * @throws EvaluationException if an exception is thrown while getting the value (the thrown exception must be included as the <code>cause</code> property of
	 *           this exception).
	 * @throws NullPointerException if <code>context</code> is <code>null</code>.
	 * @throws PropertyNotFoundException if the value specifies a property name that does not exist or is not readable.
	 * @throws MethodNotFoundException if the value specifies a method that cannot be found.
	 */
	public T getValue(final FacesContext context) throws EvaluationException, NullPointerException, PropertyNotFoundException, MethodNotFoundException {
		//TODO del getLogger().trace("getting value of a method-binding expression: {}", getExpressionString());
		//TODO del		getMethodBinding(context);	//TODO del
		final Expression<?>[] parameterExpressions = getParameterExpressions(); //get the parameter expressions
		final Object[] parameterValues = new Object[parameterExpressions.length]; //create an array of objects to hold the actual parameter values
		for(int i = 0; i < parameterExpressions.length; ++i) { //look at each parameter expression
			parameterValues[i] = parameterExpressions[i].getValue(context); //get this parameter value
			//TODO del getLogger().trace("just got value for parameter {} {} of type {}", i, parameterValues[i], parameterExpressions[i].getType(context));
		}
		final MethodBinding methodBinding = getMethodBinding(context);
		//TODO del getLogger().trace("ready to invoke a method binding we got: {}", methodBinding);
		final T test = (T)methodBinding.invoke(context, parameterValues); //invoke the method with the parameter values and cast the return value to the correct type
		//TODO del getLogger().trace("value: {}", test);
		//TODO fix getLogger().trace("type: {}", getType(context));
		return test;
		//TODO bring back		return (T)getMethodBinding(context).invoke(context, parameterValues);	//invoke the method with the parameter values and cast the return value to the correct type
	}

	/**
	 * Set the value of the expression.
	 * @param context The JSF context for the current request.
	 * @param value The new value to be set.
	 * @throws EvaluationException if an exception is thrown while setting the value (the thrown exception must be included as the <code>cause</code> property of
	 *           this exception).
	 * @throws NullPointerException if <code>context</code> is <code>null</code>.
	 * @throws PropertyNotFoundException if a specified property name does not exist, or is not writeable
	 */
	public void setValue(final FacesContext context, final T value) throws EvaluationException, PropertyNotFoundException {
		throw new EvaluationException(new UnsupportedOperationException()); //we don't support setting a method value
	}

	/**
	 * Determines if the expression is immutable.
	 * @param context The JSF context for the current request.
	 * @throws EvaluationException if an exception is thrown while getting the description of the property (the thrown exception must be included as the
	 *           <code>cause</code> property of this exception).
	 * @throws NullPointerException if <code>context</code> is <code>null</code>.
	 * @throws PropertyNotFoundException if a specified property name does not exist.
	 */
	public boolean isReadOnly(final FacesContext context) throws EvaluationException, PropertyNotFoundException {
		return true; //method bindings are always read-only
	}

	/**
	 * Returns the type of value represented by this expression.
	 * @param context The JSF context.
	 * @return The type of object that will be returned as the value.
	 * @throws EvaluationException if an exception is thrown while getting the value (the thrown exception must be included as the <code>cause</code> property of
	 *           this exception).
	 * @throws NullPointerException if <code>context</code> is <code>null</code>.
	 * @throws PropertyNotFoundException if the value specifies a property name that does not exist or is not readable.
	 * @throws MethodNotFoundException if the value specifies a method that cannot be found.
	 */
	public Class<?> getType(FacesContext context) throws EvaluationException, NullPointerException, PropertyNotFoundException, MethodNotFoundException {
		/*TODO del
		getLogger().trace("getting type of a method-binding expression: {}", getExpressionString());
		getLogger().trace("type: {}", getMethodBinding(context).getType(context));
		*/
		return getMethodBinding(context).getType(context); //return the type of the method binding
	}

	/**
	 * @return the (possibly <code>null</code>) expression string, with leading and trailing delimiters, from which this expression was built.
	 */
	public String getExpressionString() {
		return expressionString; //return the expression as a string
	}
}
