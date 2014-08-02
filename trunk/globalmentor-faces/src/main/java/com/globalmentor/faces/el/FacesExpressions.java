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

import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import javax.faces.el.*;

/**Utilities for working with the extended JSF expression language.
@author Garret Wilson
*/
public class FacesExpressions
{

	/**The JSF EL '#' character, indicating a reference.*/
	public final static char REFERENCE_CHAR='#';
	/**The JSF EL '{' character, indicating the start of a reference expression.*/
	public final static char REFERENCE_EXPRESSION_BEGIN_CHAR='{';
	/**The JSF EL '}' character, indicating the end of a reference expression.*/
	public final static char REFERENCE_EXPRESSION_END_CHAR='}';
	/**The extended JSF EL '(' character, indicating the start of a group.*/
	public final static char GROUP_BEGIN_CHAR='(';
	/**The extended JSF EL ')' character, indicating the end of a group.*/
	public final static char GROUP_END_CHAR=')';
	/**The extended JSF EL ',' character, which separates parameters.*/
	public final static char PARAMETER_SEPARATOR_CHAR=',';

	/**Creates an expression representing either a reference of unknown type or
		a literal string value if no reference is present.
	@param application The JSF application.
	@param The string representing the expression.
	@return An expression containing a string value or reference.
	@throws IllegalArgumentException if <var>string</var> does not contain a
		valid extended JSF expression language reference.
	@see #createReferenceExpression(FacesContext, String)
	@see #isReferenceExpression()
	*/
	public static Expression<?> createExpression(final Application application, final String string)
	{
		if(isReferenceExpression(string))	//if this is a reference expression
		{
			return createReferenceExpression(application, string);	//create a reference expression
		}
		else	//if this is not a reference expression
		{
			return new LiteralExpression<String>(string);	//use the string as-is
		}
	}

	/**Creates an expression representing a string, either a literal value
		or a reference.
	@param application The JSF application.
	@param The string representing the expression.
	@return An expression containing a string value or string reference.
	@throws IllegalArgumentException if <var>string</var> does not contain a
		valid extended JSF expression language reference.
	@see #createReferenceExpression(FacesContext, String)
	@see #isReferenceExpression()
	*/
	public static Expression<String> createStringExpression(final Application application, final String string)
	{
		if(isReferenceExpression(string))	//if this is a reference expression
		{
			return createReferenceExpression(application, string);	//create a reference expression
		}
		else	//if this is not a reference expression
		{
			return new LiteralExpression<String>(string);	//use the string as-is
		}
	}
	
	/**Creates an expression from a given string in extended-EL.
	@param application The JSF application.
	@param The string representing the expression.
	@return An expression for the appropriate type.
	@throws IllegalArgumentException if <var>string</var> does not contain a
		valid extended JSF expression language reference.
	@see #isReferenceExpression()
	*/
	public static <T> Expression<T> createReferenceExpression(final Application application, final String string)
	{
		if(isReferenceExpression(string))	//if this is a reference expression
		{
			if(isMethodReference(string))	//if this is a method reference
			{
				return createMethodBindingExpression(application, string);	//create a method binding expression
			}
			else	//if this is not a method reference, it must be a value reference
			{
//TODO del Log.trace("creating value-binding expression for string: ", string);
				final ValueBinding valueBinding=application.createValueBinding(string);	//create a value binding for the string
//TODO Log.trace("value-binding type: ", valueBinding.getType(FacesContext.getCurrentInstance()));
				return new ValueBindingExpression(valueBinding);	//create and return a value binding expression from the value binding we created
			}
		}
		else	//if this is not a reference expression
		{
			throw new IllegalArgumentException(string);
		}
	}

	/**Creates a method binding expression from a given string in extended-EL.
	@param application The JSF application.
	@param The string representing the expression.
	@return An expression for the appropriate type.
	@throws IllegalArgumentException if <var>string</var> does not contain a
		valid extended JSF expression language method reference.
	@see #isReferenceExpression()
	*/
	public static <T> Expression<T> createMethodBindingExpression(final Application application, final String string)
	{
		if(isMethodReference(string))	//if this is a method reference
		{
			return new MethodBindingExpression(application, string);	//create a method binding reference
		}
		else	//if this is not a reference expression
		{
			throw new IllegalArgumentException(string);
		}
	}

	/**Determines if the expression conforms to the syntax requirements of a
		value binding or method binding expression.
	@param string The string value of the expression.
	@return <code>true</code> if the string references a property or method.
	@throws NullPointerException if <var>string</var> is <code>null</code>.
	*/
	public static boolean isReferenceExpression(final String string)
	{
		return string.length()>=3	//if there is enough room for the reference characters
				&& string.charAt(0)==FacesExpressions.REFERENCE_CHAR
				&& string.charAt(1)==FacesExpressions.REFERENCE_EXPRESSION_BEGIN_CHAR
				&& string.charAt(string.length()-1)==FacesExpressions.REFERENCE_EXPRESSION_END_CHAR;	//...}
	}

	/**Determines if the expression conforms to the syntax requirements of an
		extended method binding expression.
	@param string The string value of the expression.
	@return <code>true</code> if the string references a method.
	@throws NullPointerException if <var>string</var> is <code>null</code>.
	@see #isReferenceExpression()
	*/
	public static boolean isMethodReference(final String string)
	{
		if(isReferenceExpression(string))	//if the string is a reference
		{
			final int groupBeginIndex=string.indexOf(FacesExpressions.GROUP_BEGIN_CHAR);	//find out where this group begins
			return groupBeginIndex>=0 && groupBeginIndex<string.indexOf(FacesExpressions.GROUP_END_CHAR);	//see if there are group characters in order (...)
		}
		return false;	//this isn't even a reference
	}

	/**Creates a JSF EL expression string.
	@param expression The expression to convert to a value binding
		reference.
	@return A value binding expression string in the format
		"#{<var>expression</var>}".
	*/
	public static String createValueBindingExpressionString(final String expression)
	{
		return new StringBuilder().append(FacesExpressions.REFERENCE_CHAR).append(FacesExpressions.REFERENCE_EXPRESSION_BEGIN_CHAR)
				.append(expression).append(FacesExpressions.REFERENCE_EXPRESSION_END_CHAR).toString();
	}

}
