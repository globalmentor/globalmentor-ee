package com.garretwilson.faces.component;

import java.io.IOException;
import javax.faces.application.Application;
import javax.faces.component.*;
import javax.faces.context.FacesContext;
import javax.faces.el.*;
import com.garretwilson.faces.*;
import com.garretwilson.lang.ClassUtilities;

/**Represents an assignment to a variable of a value.
The value may be an object or, if a string, a value-binding expression.
A method may also be used to retrieve a value; if so, any
	<code>UIParameter</code> children are used as parameters to the method.
*/
public class UIAssignment extends UIComponentBase
{

	/**The component type.*/
	public static final String COMPONENT_TYPE=ClassUtilities.getFullName(UIAssignment.class, "Assignment");

	/**The component family.*/
	public static final String COMPONENT_FAMILY=COMPONENT_TYPE;

	/**The name of the value binding variable.*/
//G***del	public static final String VALUE_BINDING_VAR="value";

	/**The name of the method binding variable.*/
//G***del	public static final String METHOD_BINDING_VAR="method";

	/**@return The component family.*/
	public String getFamily()
	{
		return (COMPONENT_FAMILY);
	}

	/**Constructor.*/
	public UIAssignment()
	{
		super();	//construct the parent component
		setRendererType(null);	//assignments don't have renderers
	}

	/**The name of the variable to hold the result of the assignment.*/
	private String var=null;

		/**@return The name of the variable to hold the result of the assignment.*/
		public String getVar() {return var;}

		/**Sets the name of the variable to hold the result of the assignment.
		@param var The name of the variable to hold the assignment.
		*/
		public void setVar(final String var) {this.var=var;}

	/**The value binding, or <code>null</code> if there is no value binding.*/
	private ValueBinding valueBinding=null;

		/**@return The value binding, or <code>null</code> if there is no value binding.*/
		public ValueBinding getValueBinding() {return valueBinding;}

		/**Sets the value binding.
		@param valueBinding The value binding, or <code>null</code> if there is no
			value binding.
		*/
		public void setValueBinding(final ValueBinding valueBinding) {this.valueBinding=valueBinding;}

	/**The method binding, or <code>null</code> if there is no method binding.*/
	private MethodBinding methodBinding=null;

		/**@return The method binding, or <code>null</code> if there is no method binding.*/
		public MethodBinding getMethodBinding() {return methodBinding;}

		/**Sets the method binding.
		@param methodBinding The method binding, or <code>null</code> if there is no
			method binding.
		*/
		public void setMethodBinding(final MethodBinding methodBinding) {this.methodBinding=methodBinding;}
	
	/**The non-null array of parameter bindings for the method call, or
		<code>null</code> if there is no method binding.
	*/
	private ValueBinding[] parameterBindings=null;

		/**@return The non-null array of parameter bindings for the method call, or
			<code>null</code> if there is no method binding.
		@see #getMethodBinding()
		*/
		public ValueBinding[] getParameterBindings() {return parameterBindings;}

		/**Sets the parameter bindings
		@param parameterBindings The parameter bindings to be used by the used
			by the method binding instantiation.
		*/
		public void setParameterBindings(final ValueBinding[] parameterBindings) {this.parameterBindings=parameterBindings;}

	/**The value to be assigned to the variable.*/
//G***del	private Object value=null;

		/**@return The value to be assigned to the variable.*/
//G***del		public Object getValue() {return ComponentUtilities.getValue(getFacesContext(), this, value, VALUE_VAR);}

		/**Sets the value to be assigned to the variable.
		@param value The value to be assigned to the variable
		*/
//G***del		public void setValue(final Object value) {this.value=value;}

	/**The name of the variable to hold the method binding.*/
//G***del	private String method=null;

		/**@return The the method binding.*/
//G***del		public String getMethod() {return ComponentUtilities.getValue(getFacesContext(), this, method, METHOD_VAR);}

		/**Sets the method binding.
		@param method The method-binding expression.
		*/
//G***del		public void setMethod(final String method) {this.method=method;}

	/**Assigns the value or the method invocation result to the variable.
	@param context The JSF context.
	@exception IOException Thrown if there is an error writing to the output.
	@exception NullPointerException Thrown if there is neither a value binding
		or a method binding, or there is no parameter array for the method binding.
	*/
	public void encodeBegin(final FacesContext context) throws IOException
	{
		super.encodeBegin(context);	//do the default encoding
		if(isRendered())	//if this component is rendered
		{
			final Application application=context.getApplication();	//get the application
			final Object value;	//we'll determine the value
			if(getValueBinding()!=null)	//if we have a value binding
			{
				value=getValueBinding().getValue(context);	//use the value of the value binding
			}
			else if(getMethodBinding()!=null)	//if we have a method binding
			{
				final ValueBinding[] parameterBindings=getParameterBindings();	//get the parameter bindings
				assert parameterBindings!=null : "No parameter bindings present for invoking the method binding.";
				final Object[] parameters=new Object[parameterBindings.length];	//create an array of objects to hold the actual parameters
				for(int i=0; i<parameterBindings.length; ++i)	//look at each parameter value binding
				{
					parameters[i]=parameterBindings[i].getValue(context);	//get this parameter value
				}
				value=getMethodBinding().invoke(context, parameters);	//invoke the method binding
			}
			else	//if we have no value binding or method binding
			{
				throw new NullPointerException("No value binding or method binding for assignment.");
			}
   		final ValueBinding varValueBinding=application.createValueBinding("#{"+getVar()+"}");	//create a value binding for the variable TODO use constants
			varValueBinding.setValue(context, value);	//assign the value to the variable
		}
	}

	/**Saves the state of the component.
	@param context The faces context.
	*/
	public Object saveState(final FacesContext context)
	{
		return new Object[]{super.saveState(context), var};	//TODO finish
	}

	/**Restores the state of the component.
	@param context The faces context.
	@param state The state to restore.
	*/
	public void restoreState(final FacesContext context, final Object state)
	{
		final Object values[]=(Object[])state;
		super.restoreState(context, values[0]);
		var=(String)values[1];
			//TODO finish
	}

}
