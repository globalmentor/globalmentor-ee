package com.garretwilson.net.http;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import static com.garretwilson.net.http.HTTPConstants.*;
import static com.garretwilson.servlet.http.HttpServletUtilities.*;
import com.garretwilson.util.Debug;

/**An HTTP servlet with extended functionality. 
@author Garret Wilson
*/
public class BasicHTTPServlet extends HttpServlet
{
	
	/**Services an HTTP request.
  This version provides support for special exceptions.
  Adding support for new HTTP methods should be done via the <code>doMethod()</code> method. 
  @param request The HTTP request.
  @param response The HTTP response.
  @exception ServletException if there is a problem servicing the request.
  @exception IOException if there is an error reading or writing data.
  @see #doMethod()
  */
	protected final void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
	{
//G***del Debug.setDebug(true);
		try
		{
			doMethod(request.getMethod(), request, response);	//allow the subclass to do special processing if needed
		}
		catch(final HTTPRedirectException redirectException)	//if a redirect was requested (3xx)
		{
			switch(redirectException.getStatusCode())	//check the status code
			{
				case SC_MOVED_PERMANENTLY:	//301
				case SC_MOVED_TEMPORARILY:	//302
					setLocation(response, redirectException.getLocation());	//set the redirect location
					response.sendError(redirectException.getStatusCode());	//send back the redirect status code as an error
					break;
				default:	//if we don't understand the error
					response.sendError(redirectException.getStatusCode());	//send back the redirect status code as an error
					break;
			}
		}
		catch(final HTTPUnauthorizedException unauthorizedException)	//401 Unauthorized
		{
//G***del Debug.trace("unauthorized; ready to issue challenge:", unauthorizedException.getAuthenticateChallenge());
				//issue the challenge in the WWW-authenticate header
			setWWWAuthenticate(response, unauthorizedException.getAuthenticateChallenge());
			response.sendError(unauthorizedException.getStatusCode());	//send back the redirect status code as an error
		}
		catch(final HTTPException exception)	//if an unknown HTTP error was encountered
		{
			response.sendError(exception.getStatusCode());	//send back the status code as an error
		}
  }

	/**Services an HTTP request based upon its method.
  Any subclass overriding this method must call the superclass version for normal handling of the standard
  @param method The HTTP method being serviced. 
  @param request The HTTP request.
  @param response The HTTP response.
  @exception ServletException if there is a problem servicing the request.
  @exception IOException if there is an error reading or writing data.
  */
	protected void doMethod(final String method, final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
	{
		super.service(request, response);	//do the default servicing of this method
  }

}
