package com.garretwilson.faces.el;

import java.io.*;
import java.util.*;
import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import javax.faces.el.*;
import com.garretwilson.io.*;
import com.garretwilson.util.Debug;

import static com.garretwilson.faces.el.ExpressionConstants.*;
import static com.garretwilson.text.CharacterConstants.*;

/**Utilities for working with the extended JSF expression language.
@author Garret Wilson
*/
public class ExpressionUtilities
{

	/**Creates an expression representing either a reference of unknown type or
		a literal string value if no reference is present.
	@param application The JSF application.
	@param The string representing the expression.
	@return An expression containing a string value or reference.
	@exception IllegalArgumentException if <var>string</var> does not contain a
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
	@exception IllegalArgumentException if <var>string</var> does not contain a
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
	@exception IllegalArgumentException if <var>string</var> does not contain a
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
//G***del Debug.setDebug(true);
//G***del Debug.trace("creating value-binding expression for string: ", string);
				final ValueBinding valueBinding=application.createValueBinding(string);	//create a value binding for the string
//G***Debug.trace("value-binding type: ", valueBinding.getType(FacesContext.getCurrentInstance()));
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
	@exception IllegalArgumentException if <var>string</var> does not contain a
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
	@exception NullPointerException if <var>string</var> is <code>null</code>.
	*/
	public static boolean isReferenceExpression(final String string)
	{
		return string.length()>=3	//if there is enough room for the reference characters
				&& string.charAt(0)==REFERENCE_CHAR	//#
				&& string.charAt(1)==REFERENCE_EXPRESSION_BEGIN_CHAR	//.{...
				&& string.charAt(string.length()-1)==REFERENCE_EXPRESSION_END_CHAR;	//...}
	}

	/**Determines if the expression conforms to the syntax requirements of an
		extended method binding expression.
	@param string The string value of the expression.
	@return <code>true</code> if the string references a method.
	@exception NullPointerException if <var>string</var> is <code>null</code>.
	@see #isReferenceExpression()
	*/
	public static boolean isMethodReference(final String string)
	{
		if(isReferenceExpression(string))	//if the string is a reference
		{
			final int groupBeginIndex=string.indexOf(GROUP_BEGIN_CHAR);	//find out where this group begins
			return groupBeginIndex>=0 && groupBeginIndex<string.indexOf(GROUP_END_CHAR);	//see if there are group characters in order (...)
		}
		return false;	//this isn't even a reference
	}

}
