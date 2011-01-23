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

import java.net.URI;

import javax.faces.component.UICommand;

/**Basic command component with enhanced functionality.
The command recognizes special parameters that control features
of the command itself, rather than being passed back to the server.
@author Garret Wilson
*/
public class UIBasicCommand extends UICommand
{

	/**The confirmation message to display before processing the command.*/
	public final static String CONFIRM_PARAMETER="commandConfirm";
	
	/**The alert message to display after confirmation but before processing the command.*/
	public final static String ALERT_PARAMETER="commandAlert";
	
	/**The popup URI value binding variable name.*/
	public static String POPUP_URI_PROPERTY="popupURI";

	/**The popup URI, which overrides any value binding.*/
	private URI popupURI=null;

		/**@return The popup URI.*/
		public URI getPopupURI()
		{
			return FacesComponents.getValue(this, getFacesContext(), popupURI, POPUP_URI_PROPERTY);	//get the local value or value binding
		}		

		/**Sets the popup URI.
		@param uri The new popup URI.
		*/
		public void setPopupURI(final URI uri)
		{
			popupURI=uri;	//set the popup URI
		}

}
