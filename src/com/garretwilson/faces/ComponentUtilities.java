package com.garretwilson.faces;

import java.util.*;

import javax.faces.component.*;
import javax.faces.context.FacesContext;

import com.garretwilson.util.NameValuePair;

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

}
