package com.garretwilson.faces.taglib;

import java.io.Serializable;
import java.util.*;

import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.el.MethodBinding;
import javax.faces.el.ValueBinding;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.webapp.UIComponentBodyTag;
import javax.faces.webapp.UIComponentTag;
import com.garretwilson.faces.component.UIAssignment;

import static com.garretwilson.text.CharacterConstants.*;

/**The tag for creating a <code>UIAssignment</code> component.
@author Garret Wilson
*/
public class AssignmentTag extends UIComponentTag
{
	/**@return The type of the component to use for this tag.*/
	public String getComponentType() {return UIAssignment.COMPONENT_TYPE;}

	/**@return <code>null</code>, as there is no renderer for this component.*/
	public String getRendererType() {return null;}

	/**The name of the variable to which the value or method result will be assigned.*/
	private String var=null;

		/**@return The name of the variable to which the value or method result will be assigned.*/
		public String getVar() {return var;}

		/**Sets the assignment variable.
		@param var The name of the variable to which the value or method result will be assigned.
		*/
		public void setVar(final String var) {this.var=var;}

	/**The value-binding value expression, or <code>null</code> if there is no value.*/
	private String value=null;

		/**@return The value-binding value expression, or <code>null</code> if there is no value.*/
		public String getValue() {return value;}

		/**Sets the value expression.
		@param var The value-binding value expression, or <code>null</code> if there is no value.
		*/
		public void setValue(final String value) {this.value=value;}

	/**The method-binding value expression, or <code>null</code> if there is no method.*/
	private String method=null;

		/**@return The method-binding value expression, or <code>null</code> if there is no method.*/
		public String getMethod() {return method;}

		/**Sets the method expression.
		@param var The method-binding value expression, or <code>null</code> if there is no method.
		*/
		public void setMethod(final String method) {this.method=method;}

	/**The parameter value-binding expressions, separated by commas.*/
	private String params=null;

		/**@return The parameter value-binding expressions, separated by commas.*/
		public String getParams() {return params;}

		/**Sets the parameter expressions.
		@param The parameter value-binding expressions, separated by commas.
		*/
		public void setParams(final String params) {this.params=params;}


	/**Sets the component properties.
	@param component The component, which must be a <code>UIAssignment</code>.
	@see UIAssignment.
	*/
	protected void setProperties(final UIComponent component)
	{
		super.setProperties(component);	//set the default properties
		final FacesContext context=getFacesContext();	//get the JSF context
		final Application application=context.getApplication();	//get the application
		final UIAssignment assignment=(UIAssignment)component;	//get the component as an assignment component
		final String var=getVar();	//get the variable name
		if(var!=null)	//if there is a variable
		{
			assignment.setVar(var);	//tell the assignment which variable to use
		}
		final String value=getValue();	//see if there is a value
		if(value!=null)	//if there is a value
		{
			if(isValueReference(value))	//if the value is a reference (it should be)
			{
				assignment.setValueBinding(application.createValueBinding(value));	//create a value binding for the value and store it in the assignment
			}
		}
		final String method=getMethod();	//see if there is a method
		if(method!=null)	//if there is a method
		{
			final List<ValueBinding> parameterValueBindingList=new ArrayList<ValueBinding>();	//create a new list for the parameters
			final String params=getParams();	//get the parameters, if any
			if(params!=null)	//if parameters were given
			{
				final StringTokenizer tokenizer=new StringTokenizer(params, WHITESPACE_CHARS+',');	//tokenize the parameters by commas
				while(tokenizer.hasMoreTokens())	//if there are more tokens
				{
					final String param=tokenizer.nextToken();	//get the next parameter
					if(isValueReference(param))	//if the parameter is a reference (it should be)
					{
						parameterValueBindingList.add(application.createValueBinding(param));	//create a value binding for the parameter and store it in our list
					}
					else	//if the parameter is not a value binding
					{
						throw new IllegalArgumentException(params);	//TODO is this the correct exception to use?
					}
				}
			}
				//store the parameter value bindings in an array
			final ValueBinding[] parameterValueBindings=parameterValueBindingList.toArray(new ValueBinding[parameterValueBindingList.size()]);
			assignment.setParameterBindings(parameterValueBindings);	//tell the assignment which parameters to use
			final Class[] parameterTypes=new Class[parameterValueBindings.length];	//create an array of classes, indicating parameter types
			for(int i=0; i<parameterValueBindings.length; ++i)	//look at each parameter value binding
			{
				parameterTypes[i]=parameterValueBindings[i].getType(context);	//get the type of this parameter
			}
			if(isValueReference(method))	//if the method is a reference (it should be)
			{
				assignment.setMethodBinding(application.createMethodBinding(method, parameterTypes));	//create a method binding for the method and store it in the assignment
			}
		}
	}

	/**Releases resources used by the tag.*/
	public void release()
	{
		super.release();	//do the default release
		var=null;
		value=null;
		method=null;
		params=null;
	}

}