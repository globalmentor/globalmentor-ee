package com.garretwilson.faces.el;

import java.io.*;
import java.util.*;
import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import javax.faces.el.*;
import com.garretwilson.io.*;

import static com.garretwilson.faces.el.ExpressionConstants.*;
import static com.garretwilson.text.CharacterConstants.*;

/**Utilities for working with the extended JSF expression language.
@author Garret Wilson
*/
public class ExpressionUtilities
{

	/**Creates an expression representing either a reference of unknown type or
		a literal string value if no reference is present.
	@param context The JSF context.
	@param The string representing the expression.
	@return An expression containing a string value or reference.
	@exception IllegalArgumentException if <var>string</var> does not contain a
		valid extended JSF expression language reference.
	@see #createReferenceExpression(FacesContext, String)
	@see #isReferenceExpression()
	*/
	public static Expression<?> createExpression(final FacesContext context, final String string)
	{
		if(isReferenceExpression(string))	//if this is a reference expression
		{
			return createReferenceExpression(context, string);	//create a reference expression
		}
		else	//if this is not a reference expression
		{
			return new LiteralExpression<String>(string);	//use the string as-is
		}
	}

	/**Creates an expression representing a string, either a literal value
		or a reference.
	@param context The JSF context.
	@param The string representing the expression.
	@return An expression containing a string value or string reference.
	@exception IllegalArgumentException if <var>string</var> does not contain a
		valid extended JSF expression language reference.
	@see #createReferenceExpression(FacesContext, String)
	@see #isReferenceExpression()
	*/
	public static Expression<String> createStringExpression(final FacesContext context, final String string)
	{
		if(isReferenceExpression(string))	//if this is a reference expression
		{
			return createReferenceExpression(context, string);	//create a reference expression
		}
		else	//if this is not a reference expression
		{
			return new LiteralExpression<String>(string);	//use the string as-is
		}
	}
	
	/**Creates an expression in from a given string in extended-EL.
	@param context The JSF context.
	@param The string representing the expression.
	@return An expression for the appropriate type.
	@exception IllegalArgumentException if <var>string</var> does not contain a
		valid extended JSF expression language reference.
	@see #isReferenceExpression()
	*/
	public static <T> Expression<T> createReferenceExpression(final FacesContext context, final String string)
	{
		if(isReferenceExpression(string))	//if this is a reference expression
		{
			final Application application=context.getApplication();	//get the JSF application
			final int expressionBeginIndex=string.indexOf(REFERENCE_EXPRESSION_BEGIN_CHAR)+1;	//find out where this expression begins
			final int expressionEndIndex=string.lastIndexOf(REFERENCE_EXPRESSION_END_CHAR);	//find out where this expression ends
			if(isMethodReference(string))	//if this is a method reference
			{
				final int groupBeginIndex=string.indexOf(GROUP_BEGIN_CHAR, expressionBeginIndex)+1;	//find out where the parameters begin
				final int groupEndIndex=string.lastIndexOf(GROUP_END_CHAR, expressionEndIndex-1);	//find out where the parameters end
				final String params=string.substring(groupBeginIndex, groupEndIndex);	//get a string representing the parameters
				final List<Expression<?>> parameterExpressionList=new ArrayList<Expression<?>>();	//create a new list for the parameter expressions
				final Reader paramsReader=new StringReader(params);	//create a reader for our parameters
					//tokenize the parameters by commas, grouping by reference groups
				final ReaderTokenizer tokenizer=new ReaderTokenizer(paramsReader, WHITESPACE_CHARS+PARAMETER_SEPARATOR_CHAR, String.valueOf(REFERENCE_EXPRESSION_BEGIN_CHAR), String.valueOf(REFERENCE_EXPRESSION_END_CHAR));
				while(tokenizer.hasNext())	//if there are more tokens
				{
					final String param=tokenizer.next();	//get the next parameter
					final Expression<?> parameterExpression=createReferenceExpression(context, param);	//create an expression for the parameter
					parameterExpressionList.add(parameterExpression);	//add the parameter expression to our list
				}
					//store the parameter value bindings in an array
				final Expression<?>[] parameterExpressions=parameterExpressionList.toArray(new Expression[parameterExpressionList.size()]);	//create an array of expressions for the parameter expressions
				final Class[] parameterTypes=new Class[parameterExpressions.length];	//create an array of classes, indicating parameter types
				for(int i=0; i<parameterExpressions.length; ++i)	//look at each parameter expression
				{
					parameterTypes[i]=parameterExpressions[i].getType(context);	//get the type of this parameter
				}
				final String methodName=string.substring(expressionBeginIndex, groupBeginIndex-1);	//extract the name of the method
				final String methodReference=String.valueOf(REFERENCE_CHAR)+REFERENCE_EXPRESSION_BEGIN_CHAR+methodName+REFERENCE_EXPRESSION_END_CHAR;	//construct the method reference that standard JSF EL expects
				final MethodBinding methodBinding=application.createMethodBinding(methodReference, parameterTypes);	//create a method binding
				return new MethodBindingExpression(methodBinding, parameterExpressions);	//create and return a method binding expression from the method binding we created
			}
			else	//if this is not a method reference, it must be a value reference
			{
				final ValueBinding valueBinding=application.createValueBinding(string);	//create a value binding for the string
				return new ValueBindingExpression(valueBinding);	//create and return a value binding expression from the value binding we created
			}
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
