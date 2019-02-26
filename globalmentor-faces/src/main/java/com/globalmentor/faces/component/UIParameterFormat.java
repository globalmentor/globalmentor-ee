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

package com.globalmentor.faces.component;

import java.text.MessageFormat;

import javax.faces.component.UIParameter;
import javax.faces.context.FacesContext;

import static com.globalmentor.faces.component.FacesComponents.*;
import static com.globalmentor.java.Classes.*;

/**A parameter component with a formatted value.
	The format template syntax is identical to that of <code>MessageFormat</code>,
	and the value of each <code>UIParameter</code> child will be used as a
	parameter to the template. 
@author Garret Wilson
*/
public class UIParameterFormat extends UIParameter
{
	/**The identifier for this component type.*/
	public static final String COMPONENT_TYPE=getFullName(UIParameterFormat.class, "ParameterFormat");

  /**@return The value of the component, after performing any
   	necessary formatting.
  */
	public Object getValue()
	{
		final Object value=super.getValue();	//get the assigned value, if any
		if(value instanceof String) {	//if the value is a string
			final FacesContext context=getFacesContext();	//get our context
			final String pattern=(String)value;	//get the formatting pattern
			final MessageFormat messageFormat=new MessageFormat(pattern, context.getViewRoot().getLocale());	//create a message formatter using the correct locale
			final Object[] parameterValues=getParameterValues(this, context);	//get all our parameters
			return messageFormat.format(parameterValues);	//format the value based upon the parameters and return the result	
		}
		else {	//if the value isn't a string
			return value;	//return the value normally
		}
  }

}
