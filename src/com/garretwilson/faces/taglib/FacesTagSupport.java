package com.garretwilson.faces.taglib;

import javax.faces.context.FacesContext;
import javax.servlet.jsp.tagext.TagSupport;

/**Provides JSF-specific support for tag creation.
@author Garret Wilson
*/
public class FacesTagSupport extends TagSupport
{

	/**The cached faces context.*/
	private FacesContext facesContext=null;

		/**@return The lazily-created cached faces context instance.*/
		protected FacesContext getFacesContext()
		{
//G***testing			if(facesContext==null)	//if we haven't yet retrieved the faces context
			{
				facesContext=FacesContext.getCurrentInstance();	//get the faces context
			}
			return facesContext;	//return the cached context
		}

}
