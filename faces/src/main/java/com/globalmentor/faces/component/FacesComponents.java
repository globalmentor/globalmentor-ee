/*
 * Copyright © 1996-2008 GlobalMentor, Inc. <https://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.globalmentor.faces.component;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.AbstractMap.SimpleImmutableEntry;

import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.component.*;
import javax.faces.context.FacesContext;
import javax.faces.el.*;

import com.globalmentor.faces.el.*;

import static com.globalmentor.faces.FacesValues.*;
import static com.globalmentor.faces.application.FacesMessages.*;
import static com.globalmentor.faces.component.FacesComponentConstants.*;
import static com.globalmentor.faces.el.FacesExpressions.*;
import static com.globalmentor.java.Java.*;

/**
 * Utilities for working with JavaServer Faces components.
 * @author Garret Wilson
 */
public class FacesComponents {

	//message methods

	/**
	 * Adds a message based upon a throwable error.
	 * @param component The component for which a message should be added.
	 * @param context The Faces context.
	 * @param throwable The error on which to base the message.
	 * @return A Faces message appropriate for the error.
	 */
	public static FacesMessage addMessage(final UIComponent component, final FacesContext context, final Throwable throwable) {
		final FacesMessage message = createMessage(throwable); //create a message and add it to the context based upon the component ID	
		context.addMessage(component.getClientId(context), message); //add the message
		return message; //return the message
	}

	/**
	 * Constructs an ID from several ID components separated by the naming containiner separator character.
	 * @param rootID The root ID.
	 * @param postfixes The postfixes, each of which will be appended prefixed by the separator character.
	 * @return A composite ID.
	 * @see NamingContainer#SEPARATOR_CHAR
	 */
	public static String createID(final String rootID, final String... postfixes) {
		final StringBuilder stringBuilder = new StringBuilder(rootID); //create a string builder starting with the root ID
		for(final String postfix : postfixes) { //look at each of the following IDs
			stringBuilder.append(NamingContainer.SEPARATOR_CHAR).append(postfix); //:id
		}
		return stringBuilder.toString(); //return the ID we constructed
	}

	/** The character to replace the first character if needed. */
	protected static final char REPLACEMENT_FIRST_CHAR = 'x';

	/** The character to use to replace any other character. */
	protected static final char REPLACEMENT_CHAR = '_';

	/**
	 * Creates a valid ID segmeent by replacing every non-ID character with an underscore ('_') character. If the first character of the string cannot begin a
	 * valid ID character, it will be replaced with an 'x'.
	 * @param string The string to be changed to a valid ID.
	 * @return The string modified to be a valid ID.
	 * @throws IllegalArgumentException if the string has no characters.
	 */
	public static String createValidID(final String string) { //TODO use constants
		if(string.length() == 0) { //if the string has no characters
			throw new IllegalArgumentException(string);
		}
		final StringBuilder stringBuilder = new StringBuilder(string); //create a string builder from the string, so that we can modify it as necessary
		final char firstChar = stringBuilder.charAt(0); //get the first character
		if(!Character.isLetter(firstChar) && firstChar != '_') { //if the first character isn't valid
			stringBuilder.setCharAt(0, REPLACEMENT_FIRST_CHAR); //make the first character valid
		}
		for(int i = stringBuilder.length() - 1; i > 0; --i) { //look at each character in the string, except the first (which we've already checked)
			final char character = stringBuilder.charAt(i); //get this character
			if(!Character.isLetter(character) && !Character.isDigit(character) && character != '-' && character != '_') {
				stringBuilder.setCharAt(i, REPLACEMENT_CHAR); //make this character valid
			}
		}
		return stringBuilder.toString(); //return the string we constructed
	}

	/**
	 * Encodes a component and its children, either by delegating to the component if that component can render its children, or by recursively rendering that
	 * component's children manually.
	 * <p>
	 * If the component is not rendered, no action is taken.
	 * </p>
	 * @param component The component the children of which to register.
	 * @param context The Faces context.
	 * @throws IOException if an I/O error occurs while rendering.
	 */
	public static void encodeTree(final UIComponent component, final FacesContext context) throws IOException {
		if(component.isRendered()) { //if the component is rendered
			component.encodeBegin(context); //begin the component
			if(component.getRendersChildren()) { //if the component renders its children
				component.encodeChildren(context); //ask the component to render its children
			} else { //if the component can't render its own children, we'll have to do it instead
				encodeDescendants(component, context); //encode the component's children ourselves
			}
			component.encodeEnd(context); //end the component
		}
	}

	/**
	 * Manually encode a component's children. Each child will be encoded either by delegating to the child component if that component can render its children,
	 * or by recursively rendering that component's children manually.
	 * <p>
	 * If the component is not rendered, no action is taken.
	 * </p>
	 * @param component The component the children of which to register.
	 * @param context The Faces context.
	 * @throws IOException if an I/O error occurs while rendering.
	 */
	public static void encodeDescendants(final UIComponent component, final FacesContext context) throws IOException {
		for(Object child : component.getChildren()) { //look at each of the component's children
			encodeTree((UIComponent)child, context); //recursively encode this child and its descendants
		}
	}

	/**
	 * Searches up the component hierarchy and returns the first found instance of a parent component of the given class.
	 * @param component The component the parent of which is to be found.
	 * @param parentClass The class of parent to find (e.g. <code>UIForm.class</code>.
	 * @return The first parent of the given component that is an instance of the given class, or <code>null</code> if no such parent could be found.
	 */
	public static UIComponent getParent(UIComponent component, final Class<?> parentClass) {
		do {
			component = component.getParent(); //look at the component's parent
			if(parentClass.isInstance(component)) { //if the component is an instance of the requested parent class
				break; //stop looking for parents
			}
		} while(component != null); //keep looking until we run out of parents
		return component; //return the component we found, or null
	}

	/**
	 * Retrieves the value of the first direct <code>UIParameter</code> children of the given component with the given name.
	 * @param component The component for which parameters should be retrieved.
	 * @param context The JSF context.
	 * @param name The name of the parameter to retrieve, or <code>null</code> if only a parameter with no name should be returned.
	 * @return The parameter value, or <code>null</code> if no parameter with the given name exists.
	 */
	public static Object getParameter(final UIComponent component, final FacesContext context, final String name) {
		for(Object child : component.getChildren()) { //look at all children
			if(child instanceof UIParameter) { //if this child is a parameter
				final UIParameter parameter = (UIParameter)child; //cast the child to a parameter
				if(Objects.equals(name, parameter.getName())) { //if this parameter has the correct name
					return parameter.getValue(); //return the parameter value
				}
			}
		}
		return null; //show that we couldn't find a matching parameter
	}

	/**
	 * Retrieves the names and values of all direct <code>UIParameter</code> children of the given component.
	 * @param component The component for which parameters should be retrieved.
	 * @param context The JSF context.
	 * @return A non-<code>null</code> array of parameters.
	 */
	@SuppressWarnings("unchecked")
	public static Map.Entry<String, Object>[] getParameters(final UIComponent component, final FacesContext context) {
		final List<Map.Entry<String, Object>> parameterList = new ArrayList<Map.Entry<String, Object>>(component.getChildCount()); //create a list of name-value pairs long enough to store all direct children, if needed
		for(Object child : component.getChildren()) { //look at all children
			if(child instanceof UIParameter) { //if this child is a parameter
				final UIParameter parameter = (UIParameter)child; //cast the child to a parameter
				parameterList.add(new SimpleImmutableEntry<String, Object>(parameter.getName(), parameter.getValue())); //create a new name-value pair representing this parameter and add it to our list 
			}
		}
		return (Map.Entry<String, Object>[])parameterList.toArray(new Map.Entry<?, ?>[0]); //TODO why won't this allow the generic designation?
	}

	/**
	 * Creates and returns a map of the names and values of all direct <code>UIParameter</code> children of the given component. If multiple parameters with the
	 * same name exist, only the first one will be returned. Parameters with no name will be ignored.
	 * @param component The component for which parameters should be retrieved.
	 * @param context The JSF context.
	 * @return A non-<code>null</code> map of parameter values, keyed to parameter names.
	 */
	public static Map<String, Object> getParameterMap(final UIComponent component, final FacesContext context) {
		final Map<String, Object> parameterMap = new HashMap<String, Object>(); //create a map of objects keyed to strings
		for(Object child : component.getChildren()) { //look at all children
			if(child instanceof UIParameter) { //if this child is a parameter
				final UIParameter parameter = (UIParameter)child; //cast the child to a parameter
				final String name = parameter.getName(); //get the parameter name
				if(name != null && !parameterMap.containsKey(name)) { //if this parameter has a name and is not already stored in the map
					parameterMap.put(parameter.getName(), parameter.getValue()); //add this name-value pair to the map
				}
			}
		}
		return parameterMap; //return the map of parameters
	}

	/** The constant string indicating all names should be retrieved. */
	public static final String ALL_NAMES = new String();

	/**
	 * Retrieves the values of all direct <code>UIParameter</code> children.
	 * @param component The component for which parameters should be retrieved.
	 * @param context The JSF context.
	 * @return A non-<code>null</code> array of parameter values.
	 */
	public static Object[] getParameterValues(final UIComponent component, final FacesContext context) {
		return getParameterValues(component, context, ALL_NAMES); //get all parameter values, regardless of the name
	}

	/**
	 * Retrieves the values of all direct <code>UIParameter</code> children of the given component with the given name.
	 * @param component The component for which parameters should be retrieved.
	 * @param context The JSF context.
	 * @param name The name of the parameter to retrieve; <code>null</code> for only parameters with no name specified, or the <code>ALL_NAMES</code> object for
	 *          all parameters to be included.
	 * @return A non-<code>null</code> array of parameter values.
	 * @see #ALL_NAMES
	 */
	public static Object[] getParameterValues(final UIComponent component, final FacesContext context, final String name) {
		final List<Object> valueList = new ArrayList<Object>(component.getChildCount()); //create a list of values long enough to store all direct children, if needed
		for(Object child : component.getChildren()) { //look at all children
			if(child instanceof UIParameter) { //if this child is a parameter
				final UIParameter parameter = (UIParameter)child; //cast the child to a parameter
				final String parameterName = parameter.getName(); //get the parameter name
				if(ALL_NAMES == name || Objects.equals(name, parameterName)) { //if all names should be included, or if the name matches
					valueList.add(parameter.getValue()); //add the value to our list
				}
			}
		}
		return valueList.toArray(new Object[valueList.size()]); //send back an array of values
	}

	/**
	 * Determines the variable name for a support variable to be stored in the request parameter map.
	 * @param supportClass The class of the support object.
	 * @return The variable name to use for the support object.
	 */
	public static String getSupportVariableName(final Class<?> supportClass) {
		return createVariableName(supportClass.getName()); //create a variable name from the class name of the support object		
	}

	/**
	 * Gets an expression in the form <code>#{<var>supportVar</var>.<var>predicate</var>}</code> for a support variable in the parameter map.
	 * @param supportClass The class of the support variable in the parameter map.
	 * @param predicate The predicate of the expression to create.
	 * @return An expression in the form <code>#{<var>supportVar</var>.<var>predicate</var>}</code>.
	 */
	public static String createSupportExpression(final Class<?> supportClass, final String predicate) {
		return createValueBindingExpressionString(getSupportVariableName(supportClass) + OBJECT_PREDICATE_SEPARATOR + predicate);
	}

	/**
	 * Publishes a support object in the current request.
	 * @param component The component for which support is being published.
	 * @param context The JSF context.
	 * @param support The support object to make available.
	 */
	public static void giveSupport(final UIComponent component, final FacesContext context, final Object support) { //TODO do something to make sure the component ID is taken into account	
		context.getExternalContext().getRequestMap().put(getSupportVariableName(support.getClass()), support); //put the support in the request map keyed to our special support variable
	}

	/**
	 * Determines if a component is mutable; that is, not disabled and not read-only.
	 * @param component The component to check.
	 * @return <code>true</code> if the component is not disabled and not read-only.
	 * @see #isDisabled(UIComponent)
	 * @see #isReadonly(UIComponent)
	 */
	public static boolean isMutable(final UIComponent component) {
		return !isDisabled(component) && !isReadonly(component); //the component is mutable if it isn't disabled and it isn't read-only
	}

	/**
	 * Determines if a component is disabled.
	 * @param component The component to check.
	 * @return <code>true</code> if the component is disabled.
	 * @see FacesComponentConstants#DISABLED_ATTRIBUTE
	 */
	public static boolean isDisabled(final UIComponent component) {
		return isTrueObject(component.getAttributes().get(DISABLED_ATTRIBUTE)); //see if the component is disabled
	}

	/**
	 * Determines if a component is read-only.
	 * @param component The component to check.
	 * @return <code>true</code> if the component is read-only.
	 * @see FacesComponentConstants#READONLY_ATTRIBUTE
	 */
	public static boolean isReadonly(final UIComponent component) {
		return isTrueObject(component.getAttributes().get(READONLY_ATTRIBUTE)); //see if the component is read-only
	}

	/**
	 * Sets an object value of a component. This method recognizes extended JSF EL for property-value and method-value binding.
	 * @param component The component on which the value should be set.
	 * @param attributeName The name of the attribute.
	 * @param attributeValue The value of the attribute; either a value-binding expression or an <code>Object</code>.
	 * @see Object
	 */
	public static void setObjectValue(final UIComponent component, final String attributeName, final Object attributeValue) {
		if(attributeValue != null) { //if there is an attribute value
			if(attributeValue instanceof String && isReferenceExpression((String)attributeValue)) //if the value is a string value reference
				setValueBinding(component, attributeName, (String)attributeValue); //set the value binding of the component
			else //if the string is not a value reference
				component.getAttributes().put(attributeName, attributeValue); //store the value in the component's attributes
		}
	}

	/**
	 * Sets a value binding value of a component. This method recognizes extended JSF EL for property-value and method-value binding.
	 * @param component The component on which the value should be set.
	 * @param attributeName The name of the attribute.
	 * @param attributeValue The value-binding value of the attribute.
	 * @throws IllegalArgumentException if the attribute value is not a reference expression.
	 */
	public static void setValueBindingValue(final UIComponent component, final String attributeName, final String attributeValue) {
		if(attributeValue != null) { //if there is an attribute value
			if(isReferenceExpression(attributeValue)) //if the string is a value reference
				setValueBinding(component, attributeName, attributeValue); //set the value binding of the component
			else { //if the string is not a value reference
				throw new IllegalArgumentException("The string \"" + attributeValue + "\" is not a value binding expression.");
			}
		}
	}

	/**
	 * Sets a string value of a component. This method recognizes extended JSF EL for property-value and method-value binding.
	 * @param component The component on which the value should be set.
	 * @param attributeName The name of the attribute.
	 * @param attributeValue The value of the attribute; either a value-binding expression or a <code>String</code>.
	 * @see String
	 */
	public static void setStringValue(final UIComponent component, final String attributeName, final String attributeValue) {
		if(attributeValue != null) { //if there is an attribute value
			if(isReferenceExpression(attributeValue)) //if the string is a value reference
				setValueBinding(component, attributeName, attributeValue); //set the value binding of the component
			else //if the string is not a value reference
				component.getAttributes().put(attributeName, attributeValue); //store the string value in the component's attributes
		}
	}

	/**
	 * Sets an integer value of a component. This method recognizes extended JSF EL for property-value and method-value binding.
	 * @param component The component on which the value should be set.
	 * @param attributeName The name of the attribute.
	 * @param attributeValue The value of the attribute; either a value-binding expression or an <code>Integer</code>.
	 * @throws NumberFormatException if the attribute value does not contain a parsable integer.
	 * @see Integer
	 */
	public static void setIntegerValue(final UIComponent component, final String attributeName, final String attributeValue) {
		if(attributeValue != null) { //if there is an attribute value
			if(isReferenceExpression(attributeValue)) //if the string is a value reference
				setValueBinding(component, attributeName, attributeValue); //set the value binding of the component
			else //if the string is not a value reference
				component.getAttributes().put(attributeName, Integer.valueOf(attributeValue)); //store the integer value in the component's attributes
		}
	}

	/**
	 * Sets a boolean value of a component. This method recognizes extended JSF EL for property-value and method-value binding.
	 * @param component The component on which the value should be set.
	 * @param attributeName The name of the attribute.
	 * @param attributeValue The value of the attribute; either a value-binding expression or a <code>Boolean</code>.
	 * @see Boolean
	 */
	public static void setBooleanValue(final UIComponent component, final String attributeName, final String attributeValue) {
		if(attributeValue != null) { //if there is an attribute value
			if(isReferenceExpression(attributeValue)) //if the string is a value reference
				setValueBinding(component, attributeName, attributeValue); //set the value binding of the component
			else //if the string is not a value reference
				component.getAttributes().put(attributeName, Boolean.valueOf(attributeValue)); //store the integer value in the component's attributes
		}
	}

	/**
	 * Sets a URI value of a component. This method recognizes extended JSF EL for property-value and method-value binding.
	 * @param component The component on which the value should be set.
	 * @param attributeName The name of the attribute.
	 * @param attributeValue The value of the attribute; either a value-binding expression or a <code>URI</code>.
	 * @throws IllegalArgumentException If the given string violates RFC&nbsp;2396
	 * @see URI
	 */
	public static void setURIValue(final UIComponent component, final String attributeName, final String attributeValue) throws IllegalArgumentException {
		if(attributeValue != null) { //if there is an attribute value
			if(isReferenceExpression(attributeValue)) //if the string is a value reference
				setValueBinding(component, attributeName, attributeValue); //set the value binding of the component
			else //if the string is not a value reference
				component.getAttributes().put(attributeName, URI.create(attributeValue)); //store the URI value in the component's attributes
		}
	}

	/**
	 * Sets a file value of a component. This method recognizes extended JSF EL for property-value and method-value binding.
	 * @param component The component on which the value should be set.
	 * @param attributeName The name of the attribute.
	 * @param attributeValue The value of the attribute; either a value-binding expression or a <code>URI</code>.
	 * @see File
	 */
	public static void setFileValue(final UIComponent component, final String attributeName, final String attributeValue) {
		if(attributeValue != null) { //if there is an attribute value
			if(isReferenceExpression(attributeValue)) //if the string is a value reference
				setValueBinding(component, attributeName, attributeValue); //set the value binding of the component
			else //if the string is not a value reference
				component.getAttributes().put(attributeName, new File(attributeValue)); //store the file value in the component's attributes
		}
	}

	/**
	 * Looks for a value in a value binding, if the value is not already present and returns <code>null</code> as a last result.
	 * @param <T> The type of value value expected.
	 * @param component The component from which a value should be obtained.
	 * @param context The JSF context.
	 * @param value The existing value, or <code>null</code> if there is no value present.
	 * @param name The name of the value binding to be used if there is no given <var>value</var>.
	 * @return The existing value, the value binding, or <code>null</code> if neither are present.
	 */
	public static <T> T getValue(final UIComponent component, final FacesContext context, final T value, final String name) {
		return getValue(component, context, value, name, null); //get the value, returning null as a default
	}

	/**
	 * Looks for a value in a value binding, if the value is not already present, and returns a default value as a last result. The default value is turned if the
	 * local value, the value binding, and the value binding value all return <code>null</code>.
	 * @param <T> The type of value value expected.
	 * @param component The component from which a value should be obtained.
	 * @param context The JSF context.
	 * @param value The existing value, or <code>null</code> if there is no value present.
	 * @param name The name of the value binding to be used if there is no given <var>value</var>.
	 * @param defaultValue The default value if there is no existing value and no value binding.
	 * @return The existing value, the value binding, or if neither are present, the default value.
	 */
	public static <T> T getValue(final UIComponent component, final FacesContext context, final T value, final String name, final T defaultValue) {
		if(value != null) { //if there is a value
			return value; //return the value
		} else { //if there is no value
			final ValueBinding valueBinding = component.getValueBinding(name); //get a value binding for the given name
			if(valueBinding != null) { //if we have a value binding
				final T valueBindingValue = (T)valueBinding.getValue(context); //get the value of the value binding
				if(valueBindingValue != null) { //if the value binding has a value
					return valueBindingValue; //return the value from the value binding
				}
			}
		}
		return defaultValue; //return the default value if we can't find a better value
	}

	/**
	 * Sets a value binding expression attribute value for a component, keyed to the attribute name. This method recognizes extended JSF EL for property-value and
	 * method-value binding.
	 * @param component The component on which the value should be set.
	 * @param attributeName The name under which the value-binding will be stored.
	 * @param attributeValue The value-binding expression.
	 */
	protected static void setValueBinding(final UIComponent component, final String attributeName, final String attributeValue) {
		final Application application = FacesContext.getCurrentInstance().getApplication(); //get the JSF application
		//create an expression and wrap it in a value binding
		final ValueBinding valueBinding = new ExpressionValueBinding(createExpression(application, attributeValue));
		component.setValueBinding(attributeName, valueBinding); //set the value binding for the component
	}

	/**
	 * Sets a method binding expression attribute value for a component, keyed to the attribute name.
	 * @param component The component on which the value should be set.
	 * @param attributeName The name under which the method-binding will be stored.
	 * @param attributeValue The method-binding expression, or <code>null</code> if no method-binding expression should be set.
	 * @param parameterTypes The parameter types of the method-binding expression.
	 */
	public static void setMethodBindingAttribute(final UIComponent component, final String attributeName, final String attributeValue,
			final Class<?>... parameterTypes) {
		if(attributeValue != null) { //if there is an attribute value
			final Application application = FacesContext.getCurrentInstance().getApplication(); //get the JSF application
			final MethodBinding methodBinding = application.createMethodBinding(attributeValue, parameterTypes); //create a method binding for the attribute value
			component.getAttributes().put(attributeName, methodBinding); //store the method binding in the component's attributes
		}
	}

}
