package com.garretwilson.faces.application;

import javax.faces.application.FacesMessage;

/**Utilities for working with the JavaServer Faces messages.
@author Garret Wilson
*/
public class FacesMessageUtilities
{

	/**Creates a message based upon a throwable error.
	@param throwable The error on which to base the message.
	@return A Faces message appropriate for the error. 
	*/
	public static FacesMessage createMessage(final Throwable throwable)
	{		
		final String messageString=throwable.getMessage();	//get the throwable's message
		final FacesMessage message=new FacesMessage(messageString!=null ? messageString : throwable.toString());	//create a new faces message from the message string
    message.setSeverity(FacesMessage.SEVERITY_ERROR);	//show that this was an error		
		return message;	//return the message we created
	}

}
