package com.garretwilson.faces.component;

import java.net.URI;
import java.util.*;

import javax.faces.application.Application;
import javax.faces.component.*;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.webapp.UIComponentTag;

import com.garretwilson.faces.*;
import com.garretwilson.util.NameValuePair;

import static com.garretwilson.faces.ValueUtilities.*;
import static com.garretwilson.faces.component.ComponentConstants.*;

/**Utilities for working with JavaServer Faces components.
@author Garret Wilson
*/
public class ComponentUtilities
{

	/**Searches up the component hierarchy and returns the first found instance
	 	of a parent component of the given class.
	@param component The component the parent of which is to be found.
	@param parentClass The class of parent to find (e.g. <code>UIForm.class</code>.
	@return The first parent of the given component that is an instance of the
		given class, or <code>null</code> if no such parent could be found.
	*/
	public static UIComponent getParent(UIComponent component, final Class parentClass)
	{
		do
		{
			component=component.getParent();	//look at the component's parent
			if(parentClass.isInstance(component))	//if the component is an instance of the requested parent class
			{
				break;	//stop looking for parents
			}	
		}
		while(component!=null);	//keep looking until we run out of parents
		return component;	//return the component we found, or null
	}

	/**Retrieves the values of the first direct <code>UIParameter</code>
	 	children of the given component with the given name.
	@param context The JSF context.
	@param component The component for which parameters should be retrieved.
	@param name The name of the parameter to retrieve.
	@return The parameter value, or <code>null</code> if no parameter with the
		given name exists.
	*/
	public static Object getParameter(final FacesContext context, final UIComponent component, final String name)
	{
		for(Object child:component.getChildren())	//look at all children
		{
			if(child instanceof UIParameter)	//if this child is a parameter
			{
				final UIParameter parameter=(UIParameter)child;	//cast the child to a parameter
				if(name.equals(parameter.getName()))	//if this parameter has the correct name
				{
					return parameter.getValue();	//return the parameter value
				}
			}
		}
		return null;	//show that we couldn't find a matching parameter
	}

	/**Retrieves the names and values of all direct <code>UIParameter</code>
	 	children of the given component.
	@param context The JSF context.
	@param component The component for which parameters should be retrieved.
	@return A non-<code>null</code> array of parameters.
	*/
	public static NameValuePair<String, Object>[] getParameters(final FacesContext context, final UIComponent component)
	{
		final List<NameValuePair<String, Object>> nameValuePairList=new ArrayList<NameValuePair<String, Object>>(component.getChildCount());	//create a list of name-value pairs long enough to store all direct children, if needed
		for(Object child:component.getChildren())	//look at all children
		{
			if(child instanceof UIParameter)	//if this child is a parameter
			{
				final UIParameter parameter=(UIParameter)child;	//cast the child to a parameter
				nameValuePairList.add(new NameValuePair<String, Object>(parameter.getName(), parameter.getValue()));	//create a new name-value pair representing this parameter and add it to our list 
			}
		}
		return nameValuePairList.toArray(new NameValuePair[nameValuePairList.size()]);	//G***why won't this allow the generic designation?
	}

	/**Creates and returns a map of the names and values of all direct
	 	<code>UIParameter</code> children of the given component.
	If multiple parameters with the same name exist, only the first one will be
		returned. Parameters with no name will be ignored.
	@param context The JSF context.
	@param component The component for which parameters should be retrieved.
	@return A non-<code>null</code> map of parameter values, keyed to parameter
		names.
	*/
	public static Map<String, Object> getParameterMap(final FacesContext context, final UIComponent component)
	{
		final Map<String, Object> parameterMap=new HashMap<String, Object>();	//create a map of objects keyed to strings
		for(Object child:component.getChildren())	//look at all children
		{
			if(child instanceof UIParameter)	//if this child is a parameter
			{
				final UIParameter parameter=(UIParameter)child;	//cast the child to a parameter
				final String name=parameter.getName();	//get the parameter name
				if(name!=null && !parameterMap.containsKey(name))	//if this parameter has a name and is not already stored in the map
				{
					parameterMap.put(parameter.getName(), parameter.getValue());	//add this name-value pair to the map
				}
			}
		}
		return parameterMap;	//return the map of parameters
	}

	/**Determines if a component is mutable; that is, not disabled and not
		read-only.
	@param component The component to check.
	@return <code>true</code> if the component is not disabled and not read-only.
	@see #isDisabled()
	@see #isReadonly()
	*/
	public static boolean isMutable(final UIComponent component)
	{
		return !isDisabled(component) && !isReadonly(component);	//the component is mutable if it isn't disabled and it isn't read-only
	}
	
	/**Determines if a component is disabled.
	@param component The component to check.
	@return <code>true</code> if the component is disabled.
	@see ComponentConstants#DISABLED_ATTRIBUTE
	*/
	public static boolean isDisabled(final UIComponent component)
	{
		return isTrueObject(component.getAttributes().get(DISABLED_ATTRIBUTE));	//see if the component is disabled
	}
	
	/**Determines if a component is read-only.
	@param component The component to check.
	@return <code>true</code> if the component is read-only.
	@see ComponentConstants#READONLY_ATTRIBUTE
	*/
	public static boolean isReadonly(final UIComponent component)
	{
		return isTrueObject(component.getAttributes().get(READONLY_ATTRIBUTE));	//see if the component is read-only
	}

	/**Sets a string value of a component.
	@param component The component on which the value should be set.
	@param attributeName The name of the attribute.
	@param attributeValue The value of the attribute; either a value-binding
		expression or a <code>String</code>.
	@see String
	*/
	public static void setStringValue(final UIComponent component, final String attributeName, final String attributeValue)
	{
		if(attributeValue!=null)	//if there is an attribute value
		{
			if(UIComponentTag.isValueReference(attributeValue))	//if the string is a value reference
				setValueBinding(component, attributeName, attributeValue);	//set the value binding of the component
      else	//if the string is not a value reference
      	component.getAttributes().put(attributeName, attributeValue);	//store the string value in the component's attributes
		}
	}

	/**Sets an integer value of a component.
	@param component The component on which the value should be set.
	@param attributeName The name of the attribute.
	@param attributeValue The value of the attribute; either a value-binding
		expression or an <code>Integer</code>.
	@exception NumberFormatException if the attribute value does not contain a
		parsable integer.
	@see Integer
	*/
	public static void setIntegerValue(final UIComponent component, final String attributeName, final String attributeValue)
	{
		if(attributeValue!=null)	//if there is an attribute value
		{
			if(UIComponentTag.isValueReference(attributeValue))	//if the string is a value reference
				setValueBinding(component, attributeName, attributeValue);	//set the value binding of the component
      else	//if the string is not a value reference
      	component.getAttributes().put(attributeName, new Integer(attributeValue));	//store the integer value in the component's attributes
		}
	}

	/**Sets a boolean value of a component.
	@param component The component on which the value should be set.
	@param attributeName The name of the attribute.
	@param attributeValue The value of the attribute; either a value-binding
		expression or a <code>Boolean</code>.
	@see Integer
	*/
	public static void setBooleanValue(final UIComponent component, final String attributeName, final String attributeValue)
	{
		if(attributeValue!=null)	//if there is an attribute value
		{
			if(UIComponentTag.isValueReference(attributeValue))	//if the string is a value reference
				setValueBinding(component, attributeName, attributeValue);	//set the value binding of the component
      else	//if the string is not a value reference
      	component.getAttributes().put(attributeName, new Boolean(attributeValue));	//store the integer value in the component's attributes
		}
	}

	/**Sets a URI value of a component.
	@param component The component on which the value should be set.
	@param attributeName The name of the attribute.
	@param attributeValue The value of the attribute; either a value-binding
		expression or a <code>URI</code>.
	@throws IllegalArgumentException If the given string violates RFC&nbsp;2396
	@exception NumberFormatException if the attribute value does not contain a
		parsable integer.
	@see Integer
	*/
	public static void setURIValue(final UIComponent component, final String attributeName, final String attributeValue)
	{
		if(attributeValue!=null)	//if there is an attribute value
		{
			if(UIComponentTag.isValueReference(attributeValue))	//if the string is a value reference
				setValueBinding(component, attributeName, attributeValue);	//set the value binding of the component
      else	//if the string is not a value reference
      	component.getAttributes().put(attributeName, URI.create(attributeValue));	//store the integer value in the component's attributes
		}
	}

	/**Looks for a value in a value binding, if the value is not already present
		and returns <code>null</code> as a last result.
	@param context The JSF context.
	@param value The existing value, or <code>null</code> if there is no value
		present.
	@param name The name of the value binding to be used if there is no given
		<var>value</var>.
	@return The existing value, the value binding, or <code>null</code> if
		neither are present.
	*/
	public static <T> T getValue(final FacesContext context, final UIComponent component, final T value, final String name)
	{
		return getValue(context, component, value, name, null);	//get the value, returning null as a default
	}

	/**Looks for a value in a value binding, if the value is not already present,
		and returns a default value as a last result.
	@param context The JSF context.
	@param value The existing value, or <code>null</code> if there is no value
		present.
	@param name The name of the value binding to be used if there is no given
		<var>value</var>.
	@param defaultValue The default value if there is no existing value and no
		value binding.
	@return The existing value, the value binding, or if neither are present,
		the default value.
	*/
	public static <T> T getValue(final FacesContext context, final UIComponent component, final T value, final String name, final T defaultValue)
	{
		if(value!=null)	//if there is a value
		{
			return value;	//return the value
		}
		else	//if there is no value
		{
			final ValueBinding valueBinding=component.getValueBinding(name);	//get a value binding for the given name
			if(valueBinding!=null)	//if we have a value binding
			{
				return (T)valueBinding.getValue(context);	//get the value of the value binding
			}
			else	//if we have no value binding
			{
				return defaultValue;	//return the default value
			}
		}
	}
	
	/**Sets a value binding expression attribute value for a component, keyed to the attribute name.
	@param component The component on which the value should be set.
	@param attributeName The name under which the value-binding will be stored.
	@param attributeValue The value-binding expression.
	 */
	protected static void setValueBinding(final UIComponent component, final String attributeName, final String attributeValue)
	{
		final FacesContext facesContext=FacesContext.getCurrentInstance();	//get the JSF context
		final ValueBinding valueBinding=facesContext.getApplication().createValueBinding(attributeValue);	//create a value binding for the attribute value
		component.setValueBinding(attributeName, valueBinding);	//set the value binding for the component
	}

}
