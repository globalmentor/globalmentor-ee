package com.garretwilson.net.http;

import java.io.*;
import java.util.Date;

import javax.servlet.*;
import javax.servlet.http.*;

import static com.garretwilson.net.http.HTTPConstants.*;
import com.garretwilson.security.*;
import static com.garretwilson.servlet.http.HttpServletUtilities.*;
import com.garretwilson.util.Debug;
import com.garretwilson.util.SyntaxException;

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
		catch(final IllegalArgumentException illegalArgumentException)	//if some method ran into an illegal argument, we assume the client is responsible
		{
			Debug.warn(illegalArgumentException);	//log the problem
			response.sendError(SC_BAD_REQUEST, illegalArgumentException.getMessage());	//send back a 400 Bad Request error
		}
		catch(final UnsupportedOperationException unsupportedOperationException)	//if some operation is not supported bythe server
		{
			Debug.warn(unsupportedOperationException);	//log the problem
			response.sendError(SC_NOT_IMPLEMENTED, unsupportedOperationException.getMessage());	//send back a 401 Not Implemented error
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
			response.sendError(exception.getStatusCode(), exception.getMessage());	//send back the status code as an error
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


	/**@return A private key used in generating and validating nonces.
	This implementation returns the name of the servlet class.
	*/
	protected String getNoncePrivateKey()
	{
		return getClass().getName();	//return the name of the implementing servlet class
	}
	
	/**@return A newly generated nonce.
	@see #createNonce(String)
	*/
	protected Nonce createNonce()
	{
		return new DefaultNonce(getNoncePrivateKey());	//create a default nonce using our private key
	}

	/**Creates a nonce from an existing nonce string representation.
	@param nonceString The unhashed string representing the nonce.
	@return A nonce containing values represented by the nonce string.
	@exception SyntaxException if the given string does not have the correct format for this type of nonce.
	@see #createNonce()
	*/
	protected Nonce createNonce(final String nonceString) throws SyntaxException
	{
		return new DefaultNonce(getNoncePrivateKey());	//create a default nonce using our private key
	}

	/**The number of milliseconds after which a nonce will expire: 60 seconds.*/
	protected final long NONCE_EXPIRATION_DURATION=60*1000;

	/**Determines if the given nonce is valid.
	This includes checking:
	<ul>
		<li>The nonce private key to ensure it matches this server's nonce private key.</li>
		<li>The time to ensure that too much time has not lapsed.</li>
	</ul>
	@param nonce The nonce to check for validity.
	@return <code>true</code> if the nonce is not valid.
	@see #getNoncePrivateKey()
	@see #isStale(Nonce)
	*/
	protected boolean isValid(final Nonce nonce)
	{
		return getNoncePrivateKey().equals(nonce.getPrivateKey()) && !isStale(nonce);	//check for an identical key and for staleness
	}

	/**Determines if the given nonce is stale.
	@param nonce The nonce to check for staleness.
	@return <code>true</code> if the nonce time has lapsed beyond the maximum allowed.
	@see #NONCE_EXPIRATION_DURATION
	*/
	protected boolean isStale(final Nonce nonce)
	{
		return System.currentTimeMillis()-nonce.getTime().getTime()<NONCE_EXPIRATION_DURATION;	//see if the difference between now and then is longer than we allow
	}

	/**Checks whether the given method is authorized*/
	/*TODO finish
		public void checkAuthorization(final URI resourceURI, final String method) throws HTTPUnauthorizedException
		{
			
		}
		*/
}