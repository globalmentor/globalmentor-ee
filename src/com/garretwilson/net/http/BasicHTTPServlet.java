package com.garretwilson.net.http;

import java.io.*;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.util.Date;

import javax.servlet.*;
import javax.servlet.http.*;

import static com.garretwilson.lang.ClassUtilities.*;
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
	This version checks authorization for all requests.
  This version provides support for special exceptions.
  Adding support for new HTTP methods must be done via the <code>doMethod()</code> method. 
  @param request The HTTP request.
  @param response The HTTP response.
  @exception ServletException if there is a problem servicing the request.
  @exception IOException if there is an error reading or writing data.
  @see #checkAuthorization(HttpServletRequest)
  @see #doMethod()
  */
	protected final void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
	{
/*G***del
Debug.setDebug(true);
Debug.setVisible(true);
*/
Debug.trace("servicing method", request.getMethod());
Debug.trace("servlet path:", request.getServletPath());
Debug.trace("request URI:", request.getRequestURI());
Debug.trace("request URL:", request.getRequestURL());
Debug.trace("path info:", request.getPathInfo());
		try
		{
			checkAuthorization(request);	//check to see if the request is authorized
			doMethod(request.getMethod(), request, response);	//allow the subclass to do special processing if needed
		}
		catch(final AssertionError assertionError)	//if there was an assertion error, that's a serious internal server error
		{
			Debug.warn(assertionError);	//log the problem
			response.sendError(SC_INTERNAL_SERVER_ERROR, assertionError.getMessage());	//send back a 500 Internal Server Error			
		}
		catch(final IllegalArgumentException illegalArgumentException)	//if some method ran into an illegal argument, we assume the client is responsible
		{
			Debug.warn(illegalArgumentException);	//log the problem
			response.sendError(SC_BAD_REQUEST, illegalArgumentException.getMessage());	//send back a 400 Bad Request error
		}
		catch(final UnsupportedOperationException unsupportedOperationException)	//if some operation is not supported by the server
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
		catch(final HTTPMethodNotAllowedException methodNotAllowedException)	//405 Method Not Allowed
		{
			setAllow(response, methodNotAllowedException.getAllowedMethods());	//report the allowed methods
			response.sendError(methodNotAllowedException.getStatusCode());	//send back the redirect status code as an error
		}
		catch(final HTTPException exception)	//if any other HTTP error was encountered
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

	/**Determines the URI of the requested resource.
  @param request The HTTP request indicating the requested resource.
  @return The URI of the requested resource.
  @exception HTTPRedirectException if the request should be redirected to another URI.
  */
	protected URI getResourceURI(final HttpServletRequest request) throws HTTPRedirectException
	{
		final String requestURIString=request.getRequestURL().toString();	//get the requested URI string
		return URI.create(requestURIString);	//create a URI from the full request URL
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

	/**Checks whether the given request is authorized.
  @param request The HTTP request.
	@param resourceURI The URI of the resource requested.
	@param method The HTTP method requested on the resource.
	@param credentials The principal's credentials.
	@exception HTTPInternalServerErrorException if there is an error checking the authorization.
  @exception HTTPRedirectException if the request should be redirected to another URI.
	@exception HTTPUnauthorizedException if the credentials do not provide authorization for access to the resource indicated by the given URI.
	@see #checkAuthorization(URI, String, AuthenticateCredentials)
	*/
	protected void checkAuthorization(final HttpServletRequest request) throws HTTPInternalServerErrorException, HTTPRedirectException, HTTPUnauthorizedException
	{
		try
		{
			final URI resourceURI=getResourceURI(request);	//get the requested URI
			final AuthenticateCredentials credentials=getAuthorization(request);	//get the credentials from this request, if any
			checkAuthorization(resourceURI, request.getMethod(), request.getRequestURI(), credentials);	//check authorization for these credentials
		}
		catch(final SyntaxException syntaxException)	//if the credentials weren't syntactically correct
		{
			throw new HTTPInternalServerErrorException(syntaxException);	//TODO change this to an HTTPBadRequestException when implemented
		}
	}

	/**Checks whether the given credentials provide authorization for the given method.
	@param resourceURI The URI of the resource requested.
	@param method The HTTP method requested on the resource.
	@param requestURI The request URI as given in the HTTP request.
	@param credentials The principal's credentials, or <code>null</code> if no credentials are available.
	@exception HTTPInternalServerErrorException if there is an error checking the authorization.
	@exception HTTPUnauthorizedException if the credentials do not provide authorization for access to the resource indicated by the given URI.
	@see #getPrincipal(AuthenticateCredentials)
	@see #isAuthenticated(URI, String, Principal, AuthenticateCredentials)
	@see #isAuthorized(URI, String, Principal)
	@see #createAuthenticateChallenge(URI, String, Principal, AuthenticateCredentials)
	*/
	protected void checkAuthorization(final URI resourceURI, final String method, final String requestURI, final AuthenticateCredentials credentials) throws HTTPInternalServerErrorException, HTTPUnauthorizedException
	{
		final Principal principal=getPrincipal(credentials);	//get the principal providing credentials
		if(isAuthenticated(resourceURI, method, requestURI, principal, credentials))	//if this principal is authenticated
		{
				//determine the realm (which has already been confirmed, if present); it is more effecient to get the realm from the credentials, if we can
			final String realm=credentials!=null && credentials.getRealm()!=null ? credentials.getRealm() : getRealm(resourceURI);
			if(isAuthorized(resourceURI, method, requestURI, principal, realm))	//if this principal is authorized
			{
				return;	//return normally; the request is both authenticated and authorized
			}
		}
		final AuthenticateChallenge challenge=createAuthenticateChallenge(resourceURI, method, principal, credentials);	//create an authenticate challenge
		throw new HTTPUnauthorizedException(challenge);	//throw an unauthorized exception with the challenge
	}

	/**Looks up a principal from the given credentials.
	@param credentials The principal's credentials, or <code>null</code> if no credentials are available.
	@return The principal corresponding to the given ID, or <code>null</code> if no principal could be determined.
	@exception HTTPInternalServerErrorException if there is an error getting the principal.
	@see #getPrincipal(String)
	*/
	protected Principal getPrincipal(final AuthenticateCredentials credentials) throws HTTPInternalServerErrorException
	{
		return credentials!=null ? getPrincipal(credentials.getPrincipalID()) : null;	//get the principal providing credentials, if there are credentials
	}

	/**Looks up a principal from the given ID.
	This version returns <code>null</code>. 
	@param id The ID of the principal.
	@return The principal corresponding to the given ID, or <code>null</code> if no principal could be determined.
	@exception HTTPInternalServerErrorException if there is an error getting the principal.
	*/
	protected Principal getPrincipal(final String id) throws HTTPInternalServerErrorException
	{
		return null;	//the basic HTTP servlet doesn't know any principals
	}

	/**Looks up the corresponding password for the given principal.
	This version returns <code>null</code>. 
	@param principal The principal for which a password should be returned.
	@return The password associated with the given principal, or <code>null</code> if no password is associated with the given principal.
	@exception HTTPInternalServerErrorException if there is an error getting the principal's password.
	*/
	protected char[] getPassword(final Principal principal) throws HTTPInternalServerErrorException
	{
		return null;	//the basic HTTP servlet doesn't know any passwords
	}

	/**Checks whether the given credentials provide authentication for the given URI and method.
	This version does basic authenticity checks on the credentials, if provided,
		so any subclass should first call this method.
	<ul>
		<li>The realm, if provided, is checked against the expected realm for the given resource.</li>
		<li>For digest access identification, the resource URI is checked against those of the credentials.</li>	
	</ul>
	This version allows authentication for all <code>null</code> principals with no credentials,
		and all non-<code>null</code> principals valid credentials.
	@param resourceURI The URI of the resource requested.
	@param method The HTTP method requested on the resource.
	@param requestURI The request URI as given in the HTTP request.
	@param principal The principal requesting authentication, or <code>null</code> if the principal is not known.
	@param credentials The principal's credentials, or <code>null</code> if no credentials are available.
	@return <code>true</code> if the given credentials provide authentication for the given principal.
	@exception HTTPInternalServerErrorException if there is an error determining if the principal is authenticated.
	*/
	protected boolean isAuthenticated(final URI resourceURI, final String method, final String requestURI, final Principal principal, final AuthenticateCredentials credentials) throws HTTPInternalServerErrorException
	{
Debug.trace("authenticating");
		final String realm=credentials!=null ? credentials.getRealm() : null;	//see if the credentials reports the realm, if we have credentials
Debug.trace("got realm", realm);
		if(realm!=null && !realm.equals(getRealm(resourceURI)))	//if a realm is given but it doesn't equal the expected realm for the requested resource
		{
Debug.trace("realm doesn't match", getRealm(resourceURI));
			return false;	//don't allow credentials marked for one realm to be used for another realm
		}
		if(credentials!=null)	//if there are credentials given
		{
			if(credentials instanceof DigestAuthenticateCredentials)	//if these are digest credentials, make sure they are valid
			{
				final DigestAuthenticateCredentials digestCredentials=(DigestAuthenticateCredentials)credentials;	//get the credentials as digest credentials
Debug.trace("comparing credentials URI", digestCredentials.getURI(), "against request URI", requestURI);
				if(!requestURI.equals(digestCredentials.getURI().toString()))	//if the request is for some other resource than the credentials indicate	//TODO remove toString() when we downgrade digest-uri to a String
				{
					return false;	//don't allow authentication for other resources
				}
				//TODO later find a way to recover the original nonce and make sure it isn't stale
				if(principal!=null)	//if a principal was given
				{
					final char[] password=getPassword(principal);	//get the password for the principal
Debug.trace("got password for credentials", new String(password));
					return password!=null && digestCredentials.isValid(method, password);	//see if the credentials are valid for this principal's password
				}
				else	//if no principal is given
				{
					return false;	//an anonymous principal cannot authenticate against given credentials
				}			
			}
			else	//if we don't recognize the credentials
			{
				return false;	//we can't authenticate credentials we don't recognize				
			}
		}
		else	//if no credentials were given
		{
			return principal==null;	//only anonymous principals can authenticate against missing credentials
		}
	}

	/**Checks whether the given principal is authorized to invoke the given method on the given resource.
	This version authorized any principal invoking any method on any resource in any realm.
	@param resourceURI The URI of the resource requested.
	@param method The HTTP method requested on the resource.
	@param requestURI The request URI as given in the HTTP request.
	@param principal The principal requesting authentication, or <code>null</code> if the principal is not known.
	@param realm The realm with which the resource is associated.
	@return <code>true</code> if the given principal is authorized to perform the given method on the resource represented by the given URI.
	@exception HTTPInternalServerErrorException if there is an error determining if the principal is authorized.
	*/
	protected boolean isAuthorized(final URI resourceURI, final String method, final String requestURI, final Principal principal, final String realm) throws HTTPInternalServerErrorException
	{
		return true;	//default to authorizing access
	}

	/**Creates an authentication challenge for the given resource
	This creates a digest authenticate challenge
		so any subclass should first call this method.
	This version allows authentication for all valid credentials.
	@param resourceURI The URI of the resource requested.
	@param method The HTTP method requested on the resource.
	@param principal The principal requesting authentication, or <code>null</code> if the principal is not known.
	@param credentials The principal's credentials, or <code>null</code> if no credentials are available.
	@return An authenticate challenge for the given resource URI and method.
	@exception HTTPInternalServerErrorException if there is an error creating the authenticate challenge.
	*/
	protected AuthenticateChallenge createAuthenticateChallenge(final URI resourceURI, final String method, final Principal principal, final AuthenticateCredentials credentials) throws HTTPInternalServerErrorException
	{
		try
		{
			return new DigestAuthenticateChallenge(getRealm(resourceURI), createNonce().toString());	//create a new digest authenticate challenge for the resource's realm, using a new nonce
		}
		catch(final NoSuchAlgorithmException noSuchAlgorithmException)	//if the default algorithm (MD5) is not supported
		{
			throw new HTTPInternalServerErrorException(noSuchAlgorithmException);	//indicate that we don't support the algorithm
		}
	}

	/**Determines the realm applicable for the resource indicated by the given URI
	This version returns the local class name of the servlet.
	@param resourceURI The URI of the resource requested.
	@return The realm appropriate for the resource.
	@exception HTTPInternalServerErrorException if there is an error getting the realm.
	*/
	protected String getRealm(final URI resourceURI) throws HTTPInternalServerErrorException
	{
		return getLocalName(getClass());	//return the local name of the servlet class
	}

}