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

package com.globalmentor.faces.taglib;

import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.webapp.UIComponentTag;

import com.globalmentor.faces.component.UIAssignment;

import static com.globalmentor.faces.component.FacesComponents.*;

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

	/**The value of the assignment.*/
	private String value;

		/**@return The value of the assignment.*/
		public String getValue() {return value;}

		/**Sets the value of the assignment.
		@param value The value of the assignment.
		*/
		public void setValue(final String value) {this.value=value;}

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
		if(var!=null) {	//if there is a variable
			assignment.setVar(var);	//tell the assignment which variable to use
		}
		setStringValue(component, UIAssignment.VALUE_PROPERTY, getValue());	//set the value
	}

	/**Releases resources used by the tag.*/
	public void release()
	{
		super.release();	//do the default release
		var=null;
		value=null;
	}

}