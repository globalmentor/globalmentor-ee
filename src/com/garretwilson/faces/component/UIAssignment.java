package com.garretwilson.faces.component;

import java.io.IOException;
import javax.faces.application.Application;
import javax.faces.component.*;
import javax.faces.context.FacesContext;
import javax.faces.el.*;

import com.garretwilson.lang.ClassUtilities;
import com.garretwilson.util.Debug;

import static com.garretwilson.faces.component.ComponentUtilities.*;

/**Represents an assignment to a variable of a value.
The value may be an object or, if a string, a value-binding extended JSF EL expression.
Components depending on this assignment should be included as children of this component.
TODO add true variable scope by checking assignment status beforehand and restoring it afterwards
*/
public class UIAssignment extends UIComponentBase
{

	/**The component type.*/
	public static final String COMPONENT_TYPE=ClassUtilities.getFullName(UIAssignment.class, "Assignment");

	/**The component family.*/
	public static final String COMPONENT_FAMILY=COMPONENT_TYPE;

	/**The name of the value binding variable.*/
	public static final String VALUE_VAR="value";

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

	/**The value, which overrides any value binding.*/
	private Object value=null;

		/**@return The value of the assignment.*/
		protected Object getValue()
		{
			return ComponentUtilities.getValue(this, getFacesContext(), value, VALUE_VAR);	//get the local value or value binding
		}		

		/**Sets the value.
		@param value The new assignment value.
		*/
		public void setValue(final Object value)
		{
			this.value=value;	//set the value
		}

	/**Indicates that the assignment component renders its own children.
  This prevents other components from including or excluding the assignment
  	children based upon their rendered status, which in turn may depend on
  	the variable assignment in this component.
  @return <code>true</code> indicating that this component should determine
  	child rendering.
  */
	public boolean getRendersChildren()
  {
		return true;	//control child rendering
  }

	/**Assigns the value or the method invocation result to the variable.
	@param context The JSF context.
	@exception IOException Thrown if there is an error writing to the output.
	*/
	public void encodeBegin(final FacesContext context) throws IOException
	{
		super.encodeBegin(context);	//do the default encoding
		if(isRendered())	//if this component is rendered
		{
			performAssignment(context);	//perform the assignment
		}
	}

  /**Encodes the children of this component.
	@param context The JSF context.
	@exception IOException Thrown if there is an error writing to the output.
	*/ 
  public void encodeChildren(FacesContext context) throws IOException
	{
		super.encodeBegin(context);	//do the default child encoding
		if(isRendered())	//if this component is rendered
		{
			encodeDescendants(this, context);	//encode our descendants
		}
  }
	
	/**Performs the component tree processing required by the
		<em>Apply Request Values</em> phase of the request processing
		lifecycle for all facets of this component, all children of this
    component, and this component itself.
	This version performs the requested assignment.
	@param context The JSF context for the request being processed.
	@exception NullPointerException if <code>context</code> is <code>null</code>,
		there is neither a value binding or a method binding, or there is no
		parameter array for the method binding.
	*/
	public void processDecodes(final FacesContext context)
	{
		if(isRendered())	//if this component is rendered
		{
			performAssignment(context);	//perform the assignment
		}
		super.processDecodes(context);	//do the default decoding
	}

	/**Performs the requested assignment.
	@param context The JSF context.
	*/
	protected void performAssignment(final FacesContext context)
	{
		final Object value=getValue();	//get our value
		if(value!=null)	//if we have a value
		{
/*G***del
			ValueBinding vb = getValueBinding("value");
    	if(vb!=null)
    	{
    		Debug.trace("performing assignment", getClientId(context), "with expression:", vb.getExpressionString(), "value", value);
    	}
    	else
    	{
    		Debug.trace("performing assignment", getClientId(context), "with value", value);
    	}
*/
/*G***del
Debug.setDebug(true);
Debug.setVisible(true);
Debug.trace("value of ", expression.getExpressionString(), "is", value, "of type", value!=null ? value.getClass() : null);
*/
			final Application application=context.getApplication();	//get the JSF application
			final ValueBinding varValueBinding=application.createValueBinding("#{"+getVar()+"}");	//create a value binding for the variable TODO refactor into an ExpressionUtilities method
			varValueBinding.setValue(context, value);	//assign the expression value to the variable
		}
	}

	/**Saves the state of the component.
	@param context The faces context.
	*/
	public Object saveState(final FacesContext context)
	{
		return new Object[]{super.saveState(context), var, value};
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
		value=values[2];
	}

}
