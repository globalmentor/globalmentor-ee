package com.garretwilson.faces.component;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;

import javax.faces.application.Application;
import javax.faces.component.*;
import javax.faces.context.FacesContext;
import javax.faces.el.*;
import javax.faces.webapp.UIComponentTag;

import com.garretwilson.faces.*;
import com.garretwilson.faces.el.*;
import com.garretwilson.util.Debug;
import com.garretwilson.util.NameValuePair;

import static com.garretwilson.faces.FacesConstants.*;
import static com.garretwilson.faces.ValueUtilities.*;
import static com.garretwilson.faces.component.ComponentConstants.*;
import static com.garretwilson.faces.el.ExpressionUtilities.*;

/**Utilities for working with JavaServer Faces components.
@author Garret Wilson
*/
public class ComponentUtilities
{

	/**Creates a <code>UIColumn</code> component.
	@param application The current JSF application
	@return A new <code>UIColumn</code> component.
	*/
	public static UIColumn createColumn(final Application application)
	{
		final UIColumn column=(UIColumn)createComponent(application, UIColumn.COMPONENT_TYPE);	//create a column component
		return column;	//return the component
	}

	/**Creates a <code>UICommand</code> component rendered as a link with the given action.
	@param application The current JSF application
	@param action The command action.
	@return A new <code>UICommand</code> component with the given values.
	*/
	public static UICommand createCommandLink(final Application application, final String action)
	{
		final UICommand command=createCommand(application, action);	//create a command component
		command.setRendererType(LINK_RENDER_TYPE);	//render the command as a link
		return command;	//return the component
	}

	/**Creates a default <code>UICommand</code> component.
	@param application The current JSF application
	@return A new <code>UICommand</code> component.
	*/
	public static UICommand createCommand(final Application application)
	{
		return createCommand(application, null);	//create a command with no action
	}

	/**Creates a <code>UICommand</code> component with the given action.
	@param application The current JSF application
	@param action The command action.
	@return A new <code>UICommand</code> component with the given values.
	*/
	public static UICommand createCommand(final Application application, final String action)
	{
		final UICommand command=(UICommand)createComponent(application, UICommand.COMPONENT_TYPE);	//create a command component
		if(action!=null)	//if we have an action
		{
				//create an expression from the action, and wrap it in a method-binding subclass so that UICommand will recognize it
			command.setAction(new ExpressionMethodBinding(createExpression(application, action)));
		}
		return command;	//return the component
	}

	/**Creates a <code>UIData</code> component with the given value and var.
	@param application The current JSF application
	@param value The value of the data.
	@param var The name of the variable to use for iteration, with no
		value-binding allowed.
	@return A new <code>UIData</code> component with the given values.
	*/
	public static UIData createData(final Application application, final String value, final String var)
	{
		final UIData data=(UIData)createComponent(application, UIData.COMPONENT_TYPE);	//create a data component
		setStringValue(data, VALUE_ATTRIBUTE, value);	//store the value, creating a value binding if necessary
		data.setVar(var);	//set the variable name
		return data;	//return the component
	}

	/**Creates a <code>UIGraphic</code> component with the given URL and alternate text.
	@param application The current JSF application
	@param url The context-relative URL of the graphic.
	@param alt The alternate text of the graphic.
	@return A new <code>UIGraphic</code> component with the given values.
	*/
	public static UIGraphic createGraphic(final Application application, final String url, final String alt)
	{
		final UIGraphic graphic=(UIGraphic)createComponent(application, UIGraphic.COMPONENT_TYPE);	//create a graphic component
		setStringValue(graphic, GRAPHIC_URL_ATTRIBUTE, url);	//store the URL, creating a value binding if necessary
		setStringValue(graphic, GRAPHIC_ALT_ATTRIBUTE, alt);	//store the alternate text, creating a value binding if necessary
		return graphic;	//return the component
	}

	/**Creates a <code>UIInputFile</code> file upload component with the given id.
	@param application The current JSF application
	@param id The new ID.
	@return A new <code>UIInputFile</code> component with the given values.
	*/
	public static UIInputFile createInputFile(final Application application, final String id)
	{
		final UIInputFile inputFile=(UIInputFile)createComponent(application, UIInputFile.COMPONENT_TYPE, id);	//create an input file component with the given ID
		return inputFile;	//return the component
	}

	/**Creates a <code>UIInput</code> component rendered as hidden with the given id and value.
	@param application The current JSF application
	@param id The new ID.
	@param value The new value.
	@return A new <code>UIInput</code> component with the given values.
	*/
	public static UIInput createInputHidden(final Application application, final String id, final String value)
	{
		final UIInput input=createInput(application, id, value);	//create an input component with the given ID and value
		input.setRendererType(HIDDEN_RENDER_TYPE);	//render the input hidden
		return input;	//return the component
	}

	/**Creates a <code>UIInput</code> component with the given id and value.
	@param application The current JSF application
	@param id The new ID.
	@param value The new value.
	@return A new <code>UIInput</code> component with the given values.
	*/
	public static UIInput createInput(final Application application, final String id, final String value)
	{
		final UIInput input=(UIInput)createComponent(application, UIInput.COMPONENT_TYPE, id);	//create an input component with the given ID
		setStringValue(input, VALUE_ATTRIBUTE, value);	//store the value, creating a value binding if necessary
		return input;	//return the component
	}

	/**Creates a <code>UIOutput</code> component with the given value.
	@param application The current JSF application
	@param value The value of the output to create.
	@return A new <code>UIOutput</code> component with the given values.
	*/
	public static UIOutput createOutput(final Application application, final String value)
	{
		final UIOutput output=(UIOutput)createComponent(application, UIOutput.COMPONENT_TYPE);	//create an output component
		setStringValue(output, VALUE_ATTRIBUTE, value);	//store the value, creating a value binding if necessary
		return output;	//return the component
	}

	/**Creates a <code>UIPanel</code> component rendered as a group.
	@param application The current JSF application
	@return A new <code>UIPanel</code> component.
	*/
	public static UIPanel createPanelGroup(final Application application)
	{
		final UIPanel panel=createPanel(application);	//create a panel component
		panel.setRendererType(GROUP_RENDER_TYPE);	//render the panel as a group
		return panel;	//return the component
	}

	/**Creates a <code>UIPanel</code> component.
	@param application The current JSF application
	@return A new <code>UIPanel</code> component.
	*/
	public static UIPanel createPanel(final Application application)
	{
		final UIPanel panel=(UIPanel)createComponent(application, UIPanel.COMPONENT_TYPE);	//create a panel component
		return panel;	//return the component
	}

	/**Creates a <code>UIComponent</code> with a unique ID.
	@param application The current JSF application
	@param componentType The type of component to create.
	@return A new component.
	*/
	public static UIComponent createComponent(final Application application, final String componentType)
	{
		return createComponent(application, componentType, FacesContext.getCurrentInstance().getViewRoot().createUniqueId());	//TODO fix
	}

	/**Creates a <code>UIComponent</code> with the given ID.
	@param application The current JSF application
	@param componentType The type of component to create.
	@return A new component with the given ID.
	*/
	public static UIComponent createComponent(final Application application, final String componentType, final String id)
	{
		final UIComponent component=application.createComponent(componentType);	//create The component
		component.setId(id);	//set the component ID
		return component;	//return the component we created
	}

	/**Encodes a component and its children, either by delegating to the component
 		if that component can render its children, or by recursively rendering that
 		component's children manually.
	<p>If the component is not rendered, no action is taken.</p>
	@param component The component the children of which to register.
	@param context The Faces context.
	*/
	public static void encodeTree(final UIComponent component, final FacesContext context) throws IOException
	{
		if(component.isRendered())	//if the component is rendered
		{
			component.encodeBegin(context);	//begin the component
			if(component.getRendersChildren())	//if the component renders its children
			{
				component.encodeChildren(context);	//ask the component to render its children
			}
			else	//if the component can't render its own children, we'll have to do it instead
			{
				encodeDescendants(component, context);	//encode the component's children ourselves
			}
			component.encodeEnd(context);	//end the component
	  }
	}
	
	/**Manually encode a component's children. Each child will be encoded either
	 	by delegating to the child component if that component can render its
	 	children, or by recursively rendering that component's children manually.
	<p>If the component is not rendered, no action is taken.</p>
	@param component The component the children of which to register.
	@param context The Faces context.
	*/
	public static void encodeDescendants(final UIComponent component, final FacesContext context) throws IOException
	{
		for(Object child:component.getChildren())	//look at each of the component's children
		{
			encodeTree((UIComponent)child, context);	//recursively encode this child and its descendants
	  }
	}

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
	@param component The component for which parameters should be retrieved.
	@param context The JSF context.
	@param name The name of the parameter to retrieve.
	@return The parameter value, or <code>null</code> if no parameter with the
		given name exists.
	*/
	public static Object getParameter(final UIComponent component, final FacesContext context, final String name)
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
	@param component The component for which parameters should be retrieved.
	@param context The JSF context.
	@return A non-<code>null</code> array of parameters.
	*/
	public static NameValuePair<String, Object>[] getParameters(final UIComponent component, final FacesContext context)
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
	@param component The component for which parameters should be retrieved.
	@param context The JSF context.
	@return A non-<code>null</code> map of parameter values, keyed to parameter
		names.
	*/
	public static Map<String, Object> getParameterMap(final UIComponent component, final FacesContext context)
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
	This method recognizes extended JSF EL for property-value and method-value binding.
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
			if(isReferenceExpression(attributeValue))	//if the string is a value reference
				setValueBinding(component, attributeName, attributeValue);	//set the value binding of the component
      else	//if the string is not a value reference
      	component.getAttributes().put(attributeName, attributeValue);	//store the string value in the component's attributes
		}
	}

	/**Sets an integer value of a component.
	This method recognizes extended JSF EL for property-value and method-value binding.
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
			if(isReferenceExpression(attributeValue))	//if the string is a value reference
				setValueBinding(component, attributeName, attributeValue);	//set the value binding of the component
      else	//if the string is not a value reference
      	component.getAttributes().put(attributeName, new Integer(attributeValue));	//store the integer value in the component's attributes
		}
	}

	/**Sets a boolean value of a component.
	This method recognizes extended JSF EL for property-value and method-value binding.
	@param component The component on which the value should be set.
	@param attributeName The name of the attribute.
	@param attributeValue The value of the attribute; either a value-binding
		expression or a <code>Boolean</code>.
	@see Boolean
	*/
	public static void setBooleanValue(final UIComponent component, final String attributeName, final String attributeValue)
	{
		if(attributeValue!=null)	//if there is an attribute value
		{
			if(isReferenceExpression(attributeValue))	//if the string is a value reference
				setValueBinding(component, attributeName, attributeValue);	//set the value binding of the component
      else	//if the string is not a value reference
      	component.getAttributes().put(attributeName, Boolean.valueOf(attributeValue));	//store the integer value in the component's attributes
		}
	}

	/**Sets a URI value of a component.
	This method recognizes extended JSF EL for property-value and method-value binding.
	@param component The component on which the value should be set.
	@param attributeName The name of the attribute.
	@param attributeValue The value of the attribute; either a value-binding
		expression or a <code>URI</code>.
	@throws IllegalArgumentException If the given string violates RFC&nbsp;2396
	@see URI
	*/
	public static void setURIValue(final UIComponent component, final String attributeName, final String attributeValue) throws IllegalArgumentException
	{
		if(attributeValue!=null)	//if there is an attribute value
		{
			if(isReferenceExpression(attributeValue))	//if the string is a value reference
				setValueBinding(component, attributeName, attributeValue);	//set the value binding of the component
      else	//if the string is not a value reference
      	component.getAttributes().put(attributeName, URI.create(attributeValue));	//store the URI value in the component's attributes
		}
	}

	/**Sets a file value of a component.
	This method recognizes extended JSF EL for property-value and method-value binding.
	@param component The component on which the value should be set.
	@param attributeName The name of the attribute.
	@param attributeValue The value of the attribute; either a value-binding
		expression or a <code>URI</code>.
	@see File
	*/
	public static void setFileValue(final UIComponent component, final String attributeName, final String attributeValue)
	{
		if(attributeValue!=null)	//if there is an attribute value
		{
			if(isReferenceExpression(attributeValue))	//if the string is a value reference
				setValueBinding(component, attributeName, attributeValue);	//set the value binding of the component
      else	//if the string is not a value reference
      	component.getAttributes().put(attributeName, new File(attributeValue));	//store the file value in the component's attributes
		}
	}

	/**Looks for a value in a value binding, if the value is not already present
		and returns <code>null</code> as a last result.
	@param component The component from which a value should be obtained.
	@param context The JSF context.
	@param value The existing value, or <code>null</code> if there is no value
		present.
	@param name The name of the value binding to be used if there is no given
		<var>value</var>.
	@return The existing value, the value binding, or <code>null</code> if
		neither are present.
	*/
	public static <T> T getValue(final UIComponent component, final FacesContext context, final T value, final String name)
	{
		return getValue(component, context, value, name, null);	//get the value, returning null as a default
	}

	/**Looks for a value in a value binding, if the value is not already present,
		and returns a default value as a last result.
	@param component The component from which a value should be obtained.
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
	public static <T> T getValue(final UIComponent component, final FacesContext context, final T value, final String name, final T defaultValue)
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
	This method recognizes extended JSF EL for property-value and method-value binding.
	@param component The component on which the value should be set.
	@param attributeName The name under which the value-binding will be stored.
	@param attributeValue The value-binding expression.
	*/
	protected static void setValueBinding(final UIComponent component, final String attributeName, final String attributeValue)
	{
//G***del when works		final FacesContext facesContext=FacesContext.getCurrentInstance();	//get the JSF context
		final Application application=FacesContext.getCurrentInstance().getApplication();	//get the JSF application
			//create an expression and wrap it in a value binding
		final ValueBinding valueBinding=new ExpressionValueBinding(createExpression(application, attributeValue));
//G***del when works		final ValueBinding valueBinding=facesContext.getApplication().createValueBinding(attributeValue);	//create a value binding for the attribute value
		component.setValueBinding(attributeName, valueBinding);	//set the value binding for the component
	}

	/**Sets a method binding expression attribute value for a component, keyed to the attribute name.
	@param component The component on which the value should be set.
	@param attributeName The name under which the method-binding will be stored.
	@param attributeValue The method-binding expression, or <code>null</code> if
		no method-binding expression should be set.
	@param parameterTypes The parameter types of the method-binding expression.
	*/
	public static void setMethodBindingAttribute(final UIComponent component, final String attributeName, final String attributeValue, final Class... parameterTypes)
	{
		if(attributeValue!=null)	//if there is an attribute value
		{
			final Application application=FacesContext.getCurrentInstance().getApplication();	//get the JSF application
			final MethodBinding methodBinding=application.createMethodBinding(attributeValue, parameterTypes);	//create a method binding for the attribute value
     	component.getAttributes().put(attributeName, methodBinding);	//store the method binding in the component's attributes
		}
	}

}
