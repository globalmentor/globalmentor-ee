package com.garretwilson.faces.component;

import java.net.URI;

import javax.faces.component.UICommand;
import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;

import com.garretwilson.util.Debug;

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

/*G***del
    public boolean isRendered() {
Debug.setDebug(true);
Debug.setVisible(true);
Debug.trace("checking to see if button is rendered:", getClientId(FacesContext.getCurrentInstance()));
    	ValueBinding vb = getValueBinding("rendered");
if(vb!=null)//
{
	Debug.trace("value binding with expression:", vb.getExpressionString(), "value", vb.getValue(FacesContext.getCurrentInstance()), "type", vb.getType(FacesContext.getCurrentInstance()));
}
return super.isRendered();
        }
*/
		
	/**Default constructor.*/
	public UIBasicCommand()
	{
		super();	//construct the parent class
	}

}
