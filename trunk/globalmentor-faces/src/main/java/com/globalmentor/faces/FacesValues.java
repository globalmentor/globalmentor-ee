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

package com.globalmentor.faces;

import javax.faces.context.FacesContext;
import javax.faces.webapp.UIComponentTag;

/**Utility methods for working with JavaServer Faces value.
@author Garret Wilson
*/
public class FacesValues
{

  /**Retrieves a boolean value from the string, whether it's a literal value
   	or a value-binding expression.
  @param facesContext The JSF context.
  @param valueString The string representing the value or a value-binding
  	expression.
  @return The boolean value represented by the string.
  @throws ClassCastException if the value is not a valid <code>Boolean</code>
  	value, or the bound expression is not a <code>Boolean</code> value.
	*/
  public static boolean getBooleanValue(final FacesContext facesContext, final String valueString)
  {
  	final Boolean booleanObject;
  	if(UIComponentTag.isValueReference(valueString)) {	//if the string is a JSF value reference
  			//get the value from the reference, assuming it's a Boolean value
      booleanObject=(Boolean)facesContext.getApplication().createValueBinding(valueString).getValue(facesContext);
  	}
  	else
  	{
  		booleanObject=Boolean.valueOf(valueString);	//convert the string to a Boolean
  	}
  	return booleanObject.booleanValue();	//return the boolean value
  }

	/**Determines if the object is the string "true" or the object
		<code>Boolean.TRUE</code>.
	@param object The object to checks.
	@return <code>true</code> if the object is <code>Boolean.TRUE</code>
		or the string "true".
	*/
	public static boolean isTrueObject(final Object object)
	{
		return Boolean.TRUE.equals(object) || Boolean.TRUE.toString().equals(object);	//see if this is a boolean true or a "true" string
	}
}
