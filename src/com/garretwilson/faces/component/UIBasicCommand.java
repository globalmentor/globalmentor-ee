package com.garretwilson.faces.component;

import java.net.URI;

import javax.faces.component.UICommand;

/**Basic command component with enhanced functionality.
@author Garret Wilson
*/
public class UIBasicCommand extends UICommand
{

	/**The popup URI value binding variable name.*/
	public static String POPUP_URI_VAR="popupURI";

	/**The popup URI, which overrides any value binding.*/
	private URI popupURI=null;

		/**@return The popup URI.*/
		public URI getPopupURI()
		{
			return ComponentUtilities.getValue(this, getFacesContext(), popupURI, POPUP_URI_VAR);	//get the local value or value binding
		}		

		/**Sets the popup URI.
		@param uri The new popup URI.
		*/
		public void setPopupURI(final URI uri)
		{
			popupURI=uri;	//set the popup URI
		}

	/**Default constructor.*/
	public UIBasicCommand()
	{
		super();	//construct the parent class
	}

}
