package com.garretwilson.net.http;

import java.io.*;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import static java.util.Collections.*;

import javax.servlet.*;
import javax.servlet.http.*;

import com.garretwilson.lang.CharSequenceUtilities;
import com.garretwilson.util.*;
import com.garretwilson.security.*;
import com.garretwilson.servlet.ServletUtilities;
import com.garretwilson.text.SyntaxException;
import com.garretwilson.text.W3CDateFormat;
import static com.garretwilson.io.FileUtilities.*;
import static com.garretwilson.net.URIUtilities.*;
import static com.garretwilson.net.http.HTTPConstants.*;
import static com.garretwilson.net.http.webdav.WebDAVConstants.*;
import static com.garretwilson.servlet.http.HttpServletUtilities.*;

/**An HTTP servlet with extended functionality.
This servlet supports the following initialization parameters:
<dl>
	<dt>{@value ServletConstants#DATA_DIRECTORY_INIT_PARAMETER}</dt> <dd>The directory for storing data.</dd>
	<dt>{@value ServletConstants#LOG_DIRECTORY_INIT_PARAMETER}</dt> <dd>The directory for storing logs.</dd>
	<dt>{@value #DEBUG_INIT_PARAMETER}</dt> <dd>Whether debugging is turned on.</dd> 
	<dt>{@value #DEBUG_REPORT_LEVEL_INIT_PARAMETER}</dt> <dd>The level of debug reporting for the JVM of type {@link Debug.ReportLevel}. If multiple servlets specify this value, the last one initialized will have precedence.</dd> 
	<dt>{@value #LOG_HTTP_INIT_PARAMETER}</dt> <dd>Whether HTTP communication is logged.</dd> 
 </dl>
@author Garret Wilson
*/
public class BasicHTTPServlet extends HttpServlet
{

	/**The init parameter, "debug", used to specify whether debugging is turned on; should be "true" or "false".*/
	public final static String DEBUG_INIT_PARAMETER="debug";

	/**The init parameter, "debugReportLevel", used to specify the level of debug reporting for the JVM of type {@link Debug.ReportLevel}.*/
	public final static String DEBUG_REPORT_LEVEL_INIT_PARAMETER="debugReportLevel";

	/**The init parameter, "logHTTP", used to specify whether HTTP communication should be logged; should be "true" or "false".*/
	public final static String LOG_HTTP_INIT_PARAMETER="logHTTP";

	/**The cached data directory, or <code>null</code> if it has not yet been initialized.*/
//TODO del if not needed	private File dataDirectory=null;

		/**Determines the data directory.
		After the first retrieval, this directory is cached.
		@param context The servlet context from which to retrieve init parameters.
		@return A file representing the preferred data directory.
		@exception ServletException if the {@value ServletConstants#DATA_DIRECTORY_INIT_PARAMETER} init directory was not specified and the real path to <code>WEB-INF</code> could not be determined.
		@see ServletUtilities#getDataDirectory(ServletContext)
		*/
/*TODO del if not needed
		public File getDataDirectory(final ServletContext context) throws ServletException
		{
			if(dataDirectory==null)	//if the data directory has not been determined (there is a benign race condition here)
			{
				dataDirectory=ServletUtilities.getDataDirectory(context);	//get the data directory from the init parameter if possible
				if(dataDirectory==null)	//if we can't get the WEB-INF directory
				{
					throw new ServletException(DATA_DIRECTORY_INIT_PARAMETER+" init parameter not specified and real path to WEB-INF directory not available.");
				}
			}
			return dataDirectory;	//return the data directory
		}
*/

	/**The cached log directory, or <code>null</code> if it has not yet been initialized.*/
//TODO del if not needed	private File logDirectory=null;

		/**Determines the log directory.
		After the first retrieval, this directory is cached.
		@param context The servlet context from which to retrieve init parameters.
		@return A file representing the preferred log directory.
		@exception ServletException if the {@value ServletConstants#LOG_DIRECTORY_INIT_PARAMETER} init directory was not specified and the real path to <code>WEB-INF</code> could not be determined.
		@see ServletUtilities#getLogDirectory(ServletContext)
		*/
/*TODO del if not needed
		public File getLogDirectory(final ServletContext context) throws ServletException
		{
			if(logDirectory==null)	//if the log directory has not been determined (there is a benign race condition here)
			{
				logDirectory=ServletUtilities.getLogDirectory(context);	//get the log directory from the init parameter if possible
				if(logDirectory==null)	//if we can't get the WEB-INF directory
				{
					throw new ServletException(LOG_DIRECTORY_INIT_PARAMETER+" init parameter not specified and real path to WEB-INF directory not available.");
				}
			}
			return logDirectory;	//return the data directory
		}
*/

	/**The cached shared debug log file, or <code>null</code> if it has not yet been initialized.*/
	private static File debugLogFile=null;

		/**Determines the debug log file for the entire JVM.
		After the first retrieval, this file is cached.
		The first servlet to request this file gets priority.
		@param context The servlet context from which to retrieve init parameters.
		@return The file for debug logging.
		@exception ServletException if the {@value ServletConstants#LOG_DIRECTORY_INIT_PARAMETER} init directory was not specified and the real path to <code>WEB-INF</code> could not be determined.
		@see ServletUtilities#getLogDirectory(ServletContext)
		*/
		protected static File getDebugLogFile(final ServletContext context)
		{
			if(debugLogFile==null)	//if no log file has been determined (there is a benign race condition here)
			{
				final DateFormat logFilenameDateFormat=new W3CDateFormat(W3CDateFormat.Style.DATE);	//create a formatter for the log filename
				final String logFilename="debug "+logFilenameDateFormat.format(new Date())+".log";	//create a filename in the form "date debug.log" TODO use a constant
				debugLogFile=new File(ServletUtilities.getLogDirectory(context), logFilename);	//create the log file from the log directory and the log filename
			}
			return debugLogFile;	//return the log file
		}

	/**Initializes the servlet.
	This version ensures the log directory exists.
	This version configures debugging.
	This version configures HTTP logging.
	@param servletConfig The servlet configuration.
	@exception ServletException if there is a problem initializing.
	*/
	public void init(final ServletConfig servletConfig) throws ServletException
	{
		super.init(servletConfig);	//do the default initialization
		try	//configure the debug level before we do anything else
		{
			final String debugString=servletConfig.getInitParameter(DEBUG_INIT_PARAMETER);	//get the debug setting from the init parameters
			if(debugString!=null)	//if there is a debug setting specified
			{
				final Boolean debugBoolean=Boolean.valueOf(debugString);	//convert the string to a boolean object
				try
				{
					Debug.setDebug(debugBoolean.booleanValue());	//change the debug to the specified level
				}
				catch(final IOException ioException)	//if we have a problem turning on debugging
				{
					throw new ServletException(ioException);
				}					
			}
			final String debugReportLevelString=servletConfig.getInitParameter(DEBUG_REPORT_LEVEL_INIT_PARAMETER);	//get the debug level from the init parameters
			if(debugReportLevelString!=null)	//if there is a debug report level specified
			{
				Debug.setMinimumReportLevel(Debug.ReportLevel.valueOf(debugReportLevelString));	//change the debug to the specified level
			}
		}
		catch(final IllegalArgumentException illegalArgumentException)	//if an illegal debug report level was reported
		{
			throw new ServletException(illegalArgumentException);
		}
		try	//make sure the log directory exists
		{
			ensureDirectoryExists(getLogDirectory(getServletContext()));	//make sure the log directory exists
		}
		catch(final IOException ioException)	//if we can't create the log directory
		{
			throw new ServletException(ioException);
		}
		try	//configure the debug output
		{
			Debug.setOutput(getDebugLogFile(getServletContext()));	//set the log file
		}
		catch(final IOException ioException)	//if we can't access the debug file
		{
			throw new ServletException(ioException);
		}
			//configure HTTP logging
		final String logHTTPString=servletConfig.getInitParameter(LOG_HTTP_INIT_PARAMETER);	//get the HTTP log setting from the init parameters
		if(logHTTPString!=null)	//if there is an HTTP log setting specified
		{
			final Boolean logHTTPBoolean=Boolean.valueOf(logHTTPString);	//convert the string to a boolean object
			HTTPClient.getInstance().setLogged(logHTTPBoolean.booleanValue());	//turn HTTP logging on or off
		}		
//TODO del Debug.trace("servlet", servletConfig.getServletName(), "using log directory", getLogDirectory(servletConfig.getServletContext()));
	}

	/**A thread-safe map of nonces, keyed to nonce ID strings.*/
	private final Map<String, Nonce> nonceMap=new ConcurrentHashMap<String, Nonce>();

		/**Stores a nonce for later retrieval.
		@param nonceID The ID of the nonce; usually a hash of the nonce information.
		@param nonce The nonce to store, keyed to its ID.
		*/
		protected void storeNonce(final String nonceID, final Nonce nonce)
		{
			nonceMap.put(nonceID, nonce);	//store the nonce in the map
		}
		
		/**Retrieves a nonce by its ID.
		This implementation takes the time to clear old nonces.
		@param nonceID The ID of the nonce to retrieve.
		@return The nonce with the given ID, or <code>null</code> if there is no matching nonce.
		*/
		protected Nonce getNonce(final String nonceID)
		{
			final Nonce nonce=nonceMap.get(nonceID);	//get the nonce with the given ID
			final long currentTimeMillis=System.currentTimeMillis();	//get the current time
			for(final Map.Entry<String, Nonce> nonceEntry:nonceMap.entrySet())	//for each nonce entry in the map TODO remove old nonces some other way, perhaps in a separate thread
			{
				if(currentTimeMillis-nonceEntry.getValue().getTime().getTime()>=60*60*1000)	//if the nonce has been around for an hour
				{
					nonceMap.remove(nonceEntry.getKey());	//remove this ultra-stale nonce
				}
			}
			return nonce;	//return the nonce with the given ID
		}

	/**The thread-safe map of principal IDs weakly keyed to nonces.*/
	private final Map<Nonce, String> noncePrincipalIDMap=synchronizedMap(new WeakHashMap<Nonce, String>());

		/**Associates a principal with a nonce.
		@param nonce The nonce with which a principal should be associated.
		@param principalID The ID of the principal to associate with the nonce.
		*/
		protected void setNoncePrincipalID(final Nonce nonce, final String principalID)
		{
			noncePrincipalIDMap.put(nonce, principalID);	//associate the principal with the nonce
		}

		/**Retrieves the principal associated with a nonce.
		@param nonce The nonce for which a principal should be associated.
		@return The ID of the principal associated with the given nonce, or <code>null</code> if there is no principal associated with the given nonce.
		*/
		protected String getNoncePrincipalID(final Nonce nonce)
		{
			return noncePrincipalIDMap.get(nonce);	//return the ID of the principal associated with this nonce, if there is one
		}	

	/**Whether this servlet has been initialized from an incoming request.*/
	private boolean isInitializedFromRequest=false;

	/**The context path of the servlet. This is set on the first request received by the servlet.*/
	private String contextPath=null;

		/**@return The context path of the servlet: either a string starting with '/' or the empty string.
		@exception IllegalStateException if this servlet has not yet received any requests.
		*/
		protected String getContextPath()	//TODO determine if this is encoded or not
		{
			if(contextPath==null)	//if the context path has not been set
			{
				throw new IllegalStateException("Servlet has received no requests and context path has not yet been set.");
			}
			return contextPath;	//return the servlet's context path
		}

	/**The path of the servlet. This is set on the first request received by the servlet.*/
	private String servletPath=null;

		/**@return The path of the servlet: either a string starting with '/' or the empty string.
		@exception IllegalStateException if this servlet has not yet received any requests.
		*/
		protected String getServletPath()
		{
			if(servletPath==null)	//if the context path has not been set
			{
				throw new IllegalStateException("Servlet has received no requests and servlet path has not yet been set.");
			}
			return servletPath;	//return the servlet path
		}

	/**From an absolute path to the server domain determines the context-relative absolute path.
	@param resourceServerAbsolutePath The absolute path to the server domain.
	@return An absolute path relative to the servlet context.
	@exception IllegalArgumentException if the given server absolute path does not begin with the servlet context path.
	@see #getContextPath()
	*/
	protected String getResourceContextAbsolutePath(final String resourceServerAbsolutePath)
	{
		final String contextPath=getContextPath();	//get the servlet context path
		if(!resourceServerAbsolutePath.startsWith(contextPath))	//if the server absolute path does not start with the context path
		{
			throw new IllegalArgumentException("Resource server absolute path "+resourceServerAbsolutePath+" is not located under context path "+contextPath);
		}
		final String resourceContextAbsolutePath=resourceServerAbsolutePath.substring(contextPath.length());	//remove the context path
		if(!isAbsolutePath(resourceContextAbsolutePath))	//if the resulting path is not absolute, we split a segment in two
		{
			throw new IllegalArgumentException("Resource server absolute path "+resourceServerAbsolutePath+" is not located under context path "+contextPath);			
		}
		return resourceContextAbsolutePath;	//create the context-relative absolute path
	}

	/**Initializes the servlet upon receipt of the first request.
	This version sets the context path and the servlet path.
	@param request The servlet request.
	@exception IllegalStateException if this servlet has already been initialized from a request.
	@exception ServletException if there is a problem initializing.
	*/
	public void init(final HttpServletRequest request) throws ServletException
	{
		if(isInitializedFromRequest)	//if we've already initialized from a request
		{
			throw new IllegalStateException("Servlet already initialized from a request.");
		}
		contextPath=request.getContextPath();	//set the context path from the request
		servletPath=request.getServletPath();	//set the servlet path from the request
	}

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
/*G***fix
Debug.setDebug(true);
Debug.setVisible(true);
*/
/*TODO del
Debug.trace("servicing method", request.getMethod());
Debug.trace("servlet path:", request.getServletPath());
Debug.trace("request URI:", request.getRequestURI());
Debug.trace("request URL:", request.getRequestURL());
Debug.trace("path info:", request.getPathInfo());
*/
		Debug.log("("+request.getRemoteAddr()+")", request.getMethod(), request.getRequestURL().toString(), request.getQueryString());	//log the request
		if(!isInitializedFromRequest)	//if we haven't initialized from a request, yet TODO fix race condition here
		{
			init(request);	//initialize from this request
			isInitializedFromRequest=true;	//show that we have initialized from a request
		}
		else	//if we have initialized from a request, make sure the variables are still the same
		{
			final String requestContextPath=request.getContextPath();	//get the current context path for this request
			if(!requestContextPath.equals(contextPath))	//if the context path has changed (we expect the context path to stay the same through the life of this servlet)
			{
				throw new IllegalStateException("Servlet context path changed unexpectedly from "+contextPath+" to "+requestContextPath);
			}
			final String requestServletPath=request.getServletPath();	//get the current servlet path for this request
			if(!requestServletPath.equals(servletPath))	//if the servlet path has changed (we expect the servlet path to stay the same through the life of this servlet)
			{
				throw new IllegalStateException("Servlet path changed unexpectedly from "+servletPath+" to "+requestServletPath);
			}
		}
		try
		{
			if(!OPTIONS_METHOD.equals(request.getMethod()))	//G***testing
				checkAuthorization(request);	//check to see if the request is authorized
			doMethod(request.getMethod(), request, response);	//allow the subclass to do special processing if needed
		}
		catch(final OutOfMemoryError outOfMemoryError)	//if there was an out-of-memory error, log the info before rethrowing the error
		{
			final Runtime runtime=Runtime.getRuntime();	//get the runtime instance
			Debug.warn(outOfMemoryError, "memory max", runtime.maxMemory(), "total", runtime.totalMemory(), "free", runtime.freeMemory(), "used", runtime.totalMemory()-runtime.freeMemory());
			throw outOfMemoryError;	//rethrow the error
		}
		catch(final AssertionError assertionError)	//if there was an assertion error, that's a serious internal server error
		{
			Debug.warn(assertionError);	//log the problem
			response.sendError(SC_INTERNAL_SERVER_ERROR, assertionError.getMessage());	//send back a 500 Internal Server Error			
		}
		catch(final NullPointerException nullPointerException)	//if there was a null pointer exception, that's a serious internal server error
		{
			Debug.warn(nullPointerException);	//log the problem
			response.sendError(SC_INTERNAL_SERVER_ERROR, nullPointerException.getMessage());	//send back a 500 Internal Server Error			
		}
		catch(final ClassCastException classCastException)	//if there was a class cast exception, that's a serious internal server error
		{
			Debug.warn(classCastException);	//log the problem
			response.sendError(SC_INTERNAL_SERVER_ERROR, classCastException.getMessage());	//send back a 500 Internal Server Error			
		}
		catch(final IllegalArgumentException illegalArgumentException)	//if some method ran into an illegal argument, we assume the client is responsible
		{
			Debug.warn(illegalArgumentException);	//log the problem
			response.sendError(SC_BAD_REQUEST, illegalArgumentException.getMessage());	//send back a 400 Bad Request error
		}
		catch(final IllegalStateException illegalStateException)	//if there was an illgal state exception, that's a serious internal server error
		{
			Debug.warn(illegalStateException);	//log the problem
			response.sendError(SC_INTERNAL_SERVER_ERROR, illegalStateException.getMessage());	//send back a 500 Internal Server Error			
		}
		catch(final UnsupportedOperationException unsupportedOperationException)	//if some operation is not supported by the server
		{
			Debug.warn(unsupportedOperationException);	//log the problem
			response.sendError(SC_NOT_IMPLEMENTED, unsupportedOperationException.getMessage());	//send back a 401 Not Implemented error
		}
		catch(final HTTPMovedPermanentlyException movedPermanentlyException)	//if a permanent redirect was requested (301)
		{
			final URI locationURI=movedPermanentlyException.getLocation();	//get the redirect location
			if(locationURI!=null)	//if a location was given
			{
				setLocation(response, locationURI);	//set the redirect location
			}
			response.sendError(movedPermanentlyException.getStatusCode());	//send back the redirect status code as an error
		}
		catch(final HTTPMovedTemporarilyException movedTemporarilyException)	//if a temporary redirect was requested (302)
		{
			final URI locationURI=movedTemporarilyException.getLocation();	//get the redirect location
			if(locationURI!=null)	//if a location was given
			{
				setLocation(response, locationURI);	//set the redirect location
			}
			response.sendError(movedTemporarilyException.getStatusCode());	//send back the redirect status code as an error
		}
		catch(final HTTPRedirectException redirectException)	//if a general redirect was requested (3xx)
		{
			response.sendError(redirectException.getStatusCode());	//send back the redirect status code as an error
		}
		catch(final HTTPUnauthorizedException unauthorizedException)	//401 Unauthorized
		{
//G***del Debug.trace("unauthorized; ready to issue challenge:", unauthorizedException.getAuthenticateChallenge());
			
			
			//G***testing
			response.setHeader(DAV_HEADER, "1,2");	//we support WebDAV levels 1 and 2
				//issue the challenge in the WWW-authenticate header
			setWWWAuthenticate(response, unauthorizedException.getAuthenticateChallenge());
			response.sendError(unauthorizedException.getStatusCode());	//send back the status code as an error
		}
		catch(final HTTPNotFoundException httpNotFoundException)	//404 Not Found
		{
			response.sendError(httpNotFoundException.getStatusCode(), httpNotFoundException.getMessage());	//send back the status code as an error, but don't bother logging the error
		}
		catch(final HTTPMethodNotAllowedException methodNotAllowedException)	//405 Method Not Allowed
		{
			Debug.warn(methodNotAllowedException);	//log the problem
			setAllow(response, methodNotAllowedException.getAllowedMethods());	//report the allowed methods
			response.sendError(methodNotAllowedException.getStatusCode());	//send back the status code as an error
		}
		catch(final HTTPException httpException)	//if any other HTTP error was encountered
		{
			Debug.warn(httpException);	//log the problem
			response.sendError(httpException.getStatusCode(), httpException.getMessage());	//send back the status code as an error
		}
		catch(final MissingResourceException missingResourceException)	//if there is a resource missing, the server isn't property configured
		{
			Debug.warn(missingResourceException);	//log the problem
					//TODO find out why this isn't working with Tomcat 5.5.9
			response.sendError(SC_INTERNAL_SERVER_ERROR, missingResourceException.getMessage());	//send back a 500 Internal Server Error			
		}
		catch(final RuntimeException runtimeException)	//if there are any other runtime errors
		{
			Debug.error(runtimeException);	//log the error
			throw runtimeException;	//let the container take care of the error
		}
  }

	/**Services an HTTP request based upon its method.
  Any subclass overriding this method must call the superclass version
  	for normal handling of the standard HTTP methods.
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
	<p>If it is determined that the requested resource is located in another location,
	this method may throw an <code>HTTPRedirectException</code> with the new location.
	This method may choose to continue processing the request (e.g. if a client cannot
	handle redirects) using a different URI; in this case the new URI will be returned.</p>
  @param request The HTTP request indicating the requested resource.
  @return The URI of the requested resource, which may be different from the URL
  	specified in the request.
  @exception HTTPRedirectException if the request should be redirected to another URI.
  @see HttpServletRequest#getRequestURL()
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

	/**@return A newly generated nonce.*/
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
/*TODO del if not needed
	protected Nonce createNonce(final String nonceString) throws SyntaxException
	{
		return new DefaultNonce(getNoncePrivateKey());	//create a default nonce using our private key
	}
*/

	/**The number of milliseconds after which a nonce will expire: 60 seconds.*/
	protected final long NONCE_EXPIRATION_DURATION=60*1000;

	/**Determines if the given nonce is valid.
	This includes checking:
	<ul>
		<li>The nonce private key to ensure it matches this server's nonce private key.</li>
	</ul>
  @param request The HTTP request.
	@param nonce The nonce to check for validity.
	@return <code>true</code> if the nonce is not valid.
	@see #getNoncePrivateKey()
	*/
	protected boolean isValid(final HttpServletRequest request, final Nonce nonce)
	{
		return getNoncePrivateKey().equals(nonce.getPrivateKey());	//check for an identical key
	}

	/**Determines if the given nonce is stale.
	@param nonce The nonce to check for staleness.
	@return <code>true</code> if the nonce time has lapsed beyond the maximum allowed.
	@see #NONCE_EXPIRATION_DURATION
	*/
	protected boolean isStale(final Nonce nonce)
	{
//TODO del Debug.trace("checking staleness of nonce", nonce, "with time", nonce.getTime().getTime());
		return System.currentTimeMillis()-nonce.getTime().getTime()>NONCE_EXPIRATION_DURATION;	//see if the difference between now and then is longer than we allow
	}

	/**Checks whether the given request is authorized.
  @param request The HTTP request.
	@exception HTTPInternalServerErrorException if there is an error checking the authorization.
	@exception HTTPBadRequestException if the given credentials could not be understood.
  @exception HTTPRedirectException if the request should be redirected to another URI.
	@exception HTTPForbiddenException if the requested resource is not within any realm of authorization.
	@exception HTTPUnauthorizedException if the credentials do not provide authorization for access to the resource indicated by the given URI.
	@see #checkAuthorization(HttpServletRequest, URI, String, String, AuthenticateCredentials)
	*/
	protected void checkAuthorization(final HttpServletRequest request) throws HTTPInternalServerErrorException, HTTPBadRequestException, HTTPRedirectException, HTTPForbiddenException, HTTPUnauthorizedException
	{
		try
		{
			final URI resourceURI=getResourceURI(request);	//get the requested URI
			final AuthenticateCredentials credentials=getAuthorization(request);	//get the credentials from this request, if any
			checkAuthorization(request, resourceURI, request.getMethod(), request.getRequestURI(), credentials);	//check authorization for these credentials
		}
		catch(final SyntaxException syntaxException)	//if the credentials weren't syntactically correct
		{
			throw new HTTPBadRequestException(syntaxException);
		}
	}

	/**Checks whether the given credentials provide authorization for the given method.
  @param request The HTTP request.
	@param resourceURI The URI of the resource requested.
	@param method The HTTP method requested on the resource.
	@param requestURI The request URI as given in the HTTP request.
	@param credentials The principal's credentials, or <code>null</code> if no credentials are available.
	@exception HTTPInternalServerErrorException if there is an error checking the authorization.
	@exception HTTPForbiddenException if the requested resource is not within any realm of authorization.
	@exception HTTPUnauthorizedException if the credentials do not provide authorization for access to the resource indicated by the given URI.
	@see #getPrincipal(AuthenticateCredentials)
	@see #isAuthenticated(HttpServletRequest, URI, String, String, Principal, String, AuthenticateCredentials)
	@see #isAuthorized(HttpServletRequest, URI, String, Principal, String)
	@see #createAuthenticateChallenge(URI, String, Principal, String, AuthenticateCredentials, Nonce, boolean)
	*/
	protected void checkAuthorization(final HttpServletRequest request, final URI resourceURI, final String method, final String requestURI, final AuthenticateCredentials credentials) throws HTTPInternalServerErrorException, HTTPForbiddenException, HTTPUnauthorizedException
	{
		final Principal principal=getPrincipal(credentials);	//get the principal providing credentials
		final String realm=getRealm(resourceURI);	//get the realm for this resource
		boolean isAuthenticated=false;	//every principal by default is unauthenticated
		if(isAuthenticated(request, resourceURI, method, requestURI, principal, realm, credentials))	//if this principal is authenticated
		{
			isAuthenticated=true;	//the request is authenticated
		}
		authenticated(request, resourceURI, method, requestURI, principal, realm, credentials, isAuthenticated);	//indicate whether the principal has been authenticated
		boolean isAuthorized=false;	//every principal by default is unauthorized
		if(isAuthenticated && isAuthorized(request, resourceURI, method, principal, realm))	//if this principal is authorized
		{
			isAuthorized=true;	//the request is both authenticated and authorized
		}
		if(credentials!=null)	//if credentials were provided
		{
			Debug.log("authorized", isAuthorized, resourceURI, method, principal, realm);	//log the authorization result
		}
		if(!isAuthorized)	//if authentication and authorization didn't succeed, throw an exception
		{
			if(realm!=null)	//if we have a realm to authenticate
			{
				final AuthenticateChallenge challenge=createAuthenticateChallenge(resourceURI, method, principal, realm, credentials, createNonce(), false);	//create an authenticate challenge with a newly created nonce
				throw new HTTPUnauthorizedException(challenge);	//throw an unauthorized exception with the challenge
			}
			else	//if the requested resource is not within a realm
			{
				throw new HTTPForbiddenException(resourceURI.toString());	//the request is forbidden for this resource
			}
		}
	}

	/**Called when a principal has went through authentication and indicates the result of authentication.
  @param request The HTTP request.
	@param resourceURI The URI of the resource requested.
	@param method The HTTP method requested on the resource.
	@param requestURI The request URI as given in the HTTP request.
	@param principal The principal requesting authentication, or <code>null</code> if the principal is not known.
	@param realm The realm for which the principal was authenticated.
	@param credentials The principal's credentials, or <code>null</code> if no credentials are available.
	@param authenticated <code>true</code> if the principal succeeded in authentication, else <code>false</code>.
	*/
	protected void authenticated(final HttpServletRequest request, final URI resourceURI, final String method, final String requestURI, final Principal principal, final String realm, final AuthenticateCredentials credentials, final boolean authenticated)
	{
	}

	/**Looks up a principal from the given credentials.
	@param credentials The principal's credentials, or <code>null</code> if no credentials are available.
	@return The principal corresponding to the given ID, or <code>null</code> if no principal could be determined.
	@exception HTTPInternalServerErrorException if there is an error getting the principal.
	@see #getPrincipal(String)
	*/
	protected Principal getPrincipal(final AuthenticateCredentials credentials) throws HTTPInternalServerErrorException
	{
		if(credentials!=null)	//if we have credentials
		{
			final String principalID=credentials.getPrincipalID();	//get the ID of the principal
//TODO delDebug.trace("checking credentials with ID", principalID);
			final int separatorIndex=CharSequenceUtilities.indexOf(principalID, '\\');	//G***testing
			if(separatorIndex>=0)
			{
//TODO del Debug.trace("using real ID", principalID.substring(separatorIndex+1));
				return getPrincipal(principalID.substring(separatorIndex+1));	//G***testing
			}
			else
			{
				return getPrincipal(principalID);
			}
		}
		else	//if we have no credentials
		{
			return null;	//there is no principal
		}
//G***del		return credentials!=null ? getPrincipal(credentials.getPrincipalID()) : null;	//get the principal providing credentials, if there are credentials
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
  @param request The HTTP request.
	@param resourceURI The URI of the resource requested.
	@param method The HTTP method requested on the resource.
	@param requestURI The request URI as given in the HTTP request.
	@param principal The principal requesting authentication, or <code>null</code> if the principal is not known.
	@param realm The expected realm for the given request URI.
	@param credentials The principal's credentials, or <code>null</code> if no credentials are available.
	@return <code>true</code> if the given credentials provide authentication for the given principal.
	@exception HTTPInternalServerErrorException if there is an error determining if the principal is authenticated.
	@exception HTTPUnauthorizedException if the credentials represent a stale digest authentication nonce.
	*/
	protected boolean isAuthenticated(final HttpServletRequest request, final URI resourceURI, final String method, final String requestURI, final Principal principal, final String realm, final AuthenticateCredentials credentials) throws HTTPInternalServerErrorException, HTTPUnauthorizedException
	{
//TODO del Debug.trace("authenticating");
		final String credentialsRealm=credentials!=null ? credentials.getRealm() : null;	//see if the credentials reports the realm, if we have credentials
//TODO del Debug.trace("got realm", realm);
		if(credentialsRealm!=null && !credentialsRealm.equals(realm))	//if a realm is given but it doesn't equal the expected realm for the requested resource
		{
//TODO del Debug.trace("realm doesn't match", getRealm(resourceURI));
			return false;	//don't allow credentials marked for one realm to be used for another realm
		}
		if(credentials!=null)	//if there are credentials given
		{
			if(credentials instanceof DigestAuthenticateCredentials)	//if these are digest credentials, make sure they are valid
			{
				final DigestAuthenticateCredentials digestCredentials=(DigestAuthenticateCredentials)credentials;	//get the credentials as digest credentials
//TODO del Debug.trace("comparing credentials URI", digestCredentials.getURI(), "against request URI", requestURI);
				if(!requestURI.equals(digestCredentials.getURI().toString()))	//if the request is for some other resource than the credentials indicate	//TODO remove toString() when we downgrade digest-uri to a String
				{
					return false;	//don't allow authentication for other resources
				}
//			TODO del Debug.trace("getting nonce for credentials nonce ID", digestCredentials.getNonce());
				final Nonce nonce=getNonce(digestCredentials.getNonce());	//get the nonce the request is using
				if(nonce==null || !isValid(request, nonce))	//if we have no knowledge of this nonce or the nonce is invalid
				{
//				TODO del Debug.trace("nonce is not valid", nonce);
					return false;	//the nonce is either very old or an incorrect nonce altogether
				}
				if(principal==null)	//if no principal was given
				{
					return false;	//an anonymous principal cannot authenticate against given credentials
				}			
				final char[] password=getPassword(principal);	//get the password for the principal
//TODO del Debug.trace("got password for credentials", new String(password));
				if(password==null || !digestCredentials.isValid(method, password))	//see if the credentials are valid for this principal's password
				{
					return false;	//indicate that the credentials have an invalid password
				}
				setNoncePrincipalID(nonce, principal.getName());	//associate this principal with the nonce
//			TODO del Debug.trace("checking staleness");
				if(isStale(nonce))	//if the nonce is stale
				{
//				TODO del Debug.trace("nonce is stale");
					final Nonce newNonce=createNonce();	//create a new, unstale nonce
					setNoncePrincipalID(newNonce, principal.getName());	//associate this principal with the new nonce
					final AuthenticateChallenge challenge=createAuthenticateChallenge(resourceURI, method, principal, realm, credentials, newNonce, true);	//create an authenticate challenge with the new nonce
					throw new HTTPUnauthorizedException(challenge);	//throw an unauthorized exception with the challenge
				}
				return true;	//the credentials passed all the requirements
			}
			else	//if we don't recognize the credentials
			{
				return false;	//we can't authenticate credentials we don't recognize				
			}
		}
		else	//if no credentials were given
		{
//TODO del Debug.trace("there were no credentials given, and principal is", principal);
			return principal==null;	//only anonymous principals can authenticate against missing credentials
		}
	}

	/**Checks whether the given principal is authorized to invoke the given method on the given resource.
	This version authorized any principal invoking any method on any resource in any realm.
  @param request The HTTP request.
	@param resourceURI The URI of the resource requested.
	@param method The HTTP method requested on the resource.
	@param principal The principal requesting authentication, or <code>null</code> if the principal is not known.
	@param realm The realm with which the resource is associated, or <code>null</code> if the realm is not known.
	@return <code>true</code> if the given principal is authorized to perform the given method on the resource represented by the given URI.
	@exception HTTPInternalServerErrorException if there is an error determining if the principal is authorized.
	*/
	protected boolean isAuthorized(final HttpServletRequest request, final URI resourceURI, final String method, final Principal principal, final String realm) throws HTTPInternalServerErrorException
	{
		return true;	//default to authorizing access
	}

	/**Creates an authentication challenge for the given resource
	This creates a digest authenticate challenge so any subclass should first call this method.
	This version stores the given nonce under a nonce ID digest generated by the credentials.
	@param resourceURI The URI of the resource requested.
	@param method The HTTP method requested on the resource.
	@param principal The principal requesting authentication, or <code>null</code> if the principal is not known.
	@param realm The realm in which the resource is located.
	@param credentials The principal's credentials, or <code>null</code> if no credentials are available.
	@param nonce The nonce to be used in the challenge.
	@param stale Whether the previous request from the client was rejected because the nonce value was stale.
	@return An authenticate challenge for the given resource URI and method.
	@exception HTTPInternalServerErrorException if there is an error creating the authenticate challenge.
	*/
	protected AuthenticateChallenge createAuthenticateChallenge(final URI resourceURI, final String method, final Principal principal, final String realm, final AuthenticateCredentials credentials, final Nonce nonce, final boolean stale) throws HTTPInternalServerErrorException
	{
		try
		{
			final DigestAuthenticateChallenge challenge=new DigestAuthenticateChallenge(realm, nonce.toString(), stale);	//create a new digest authenticate challenge for the resource's realm, using the given nonce
			storeNonce(challenge.getNonceDigest(), nonce);	//store the nonce under its digest value
			return challenge;	//return the challenge
		}
		catch(final NoSuchAlgorithmException noSuchAlgorithmException)	//if the default algorithm (MD5) is not supported
		{
			throw new HTTPInternalServerErrorException(noSuchAlgorithmException);	//indicate that we don't support the algorithm
		}
	}

	/**Determines the realm applicable for the resource indicated by the given URI.
	This version returns <code>null</code>.
	@param resourceURI The URI of the resource requested.
	@return The realm appropriate for the resource, or <code>null</code> if the given resource is not in a known realm.
	@exception HTTPInternalServerErrorException if there is an error getting the realm.
	*/
	protected String getRealm(final URI resourceURI) throws HTTPInternalServerErrorException
	{
		return null;
	}

}