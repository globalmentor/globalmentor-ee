/*
 * Copyright © 1996-2012 GlobalMentor, Inc. <https://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.globalmentor.servlet.http;

import java.io.*;
import java.net.URI;
import java.security.*;
import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import static java.util.Collections.*;

import javax.servlet.*;
import javax.servlet.http.*;

import org.slf4j.event.Level;

import com.globalmentor.java.CharSequences;
import com.globalmentor.management.profile.Profiler;
import com.globalmentor.management.profile.StackProbeOperation;
import com.globalmentor.net.http.*;

import com.globalmentor.security.*;
import com.globalmentor.servlet.Servlets;
import com.globalmentor.text.SyntaxException;
import com.globalmentor.text.W3CDateFormat;

import io.clogr.Clogged;
import io.clogr.Clogr;

import static com.globalmentor.io.Filenames.*;
import static com.globalmentor.io.Files.*;
import static com.globalmentor.java.Conditions.*;
import static com.globalmentor.net.HTTP.*;
import static com.globalmentor.net.URIs.*;
import static com.globalmentor.net.http.webdav.WebDAV.*;
import static com.globalmentor.servlet.Servlets.*;
import static com.globalmentor.servlet.http.HTTPServlets.*;

/**
 * An HTTP servlet with extended functionality. This servlet supports the following initialization parameters:
 * <dl>
 * <dt>{@link Servlets#LOG_DIRECTORY_INIT_PARAMETER}</dt>
 * <dd>The directory for storing logs.</dd>
 * <dt>{@value #DEBUG_INIT_PARAMETER}</dt>
 * <dd>Whether the servlet is in debug mode; should be "true" or "false"; sets the log level to debug if not explicitly set.</dd>
 * <dt>{@value #LOG_LEVEL_INIT_PARAMETER}</dt>
 * <dd>The level of logging for the JVM of type {@link Level}. If multiple servlets specify this value, the last one initialized will have precedence.</dd>
 * <dt>{@value #LOG_HTTP_INIT_PARAMETER}</dt>
 * <dd>Whether HTTP communication is logged.</dd>
 * <dt>{@value #PROFILE_INIT_PARAMETER}</dt>
 * <dd>Whether profiling should occur; should be "true" or "false".</dd>
 * </dl>
 * @author Garret Wilson
 */
public class BaseHTTPServlet extends HttpServlet implements Clogged {

	private static final long serialVersionUID = -7775451621358615903L;

	/**
	 * The init parameter, {@value #DEBUG_INIT_PARAMETER}, used to specify whether the servlet is in debug mode; should be "true" or "false"; sets the log level
	 * to debug if not explicitly set.
	 */
	public static final String DEBUG_INIT_PARAMETER = "debug";

	/** The init parameter, {@value #LOG_LEVEL_INIT_PARAMETER}, used to specify the level of logging for the JVM of type {@link Level}. */
	public static final String LOG_LEVEL_INIT_PARAMETER = "logLevel";

	/** The init parameter, {@value #LOG_HTTP_INIT_PARAMETER}, used to specify whether HTTP communication should be logged; should be "true" or "false". */
	public static final String LOG_HTTP_INIT_PARAMETER = "logHTTP";

	/** The init parameter, {@value #PROFILE_INIT_PARAMETER}, used to specify whether profiling should occur; should be "true" or "false". */
	public static final String PROFILE_INIT_PARAMETER = "profile";

	/** The cached shared default log file, or <code>null</code> if it has not yet been initialized. */
	private static File logFile = null;

	/**
	 * Determines the default log file for the entire JVM. After the first retrieval, this file is cached. The first servlet to request this file gets priority.
	 * @param context The servlet context from which to retrieve init parameters.
	 * @return The file for logging.
	 * @throws ServletException if the {@value Servlets#LOG_DIRECTORY_INIT_PARAMETER} init directory was not specified and the real path to <code>WEB-INF</code>
	 *           could not be determined.
	 * @see Servlets#getLogDirectory(ServletContext)
	 */
	protected static File getLogFile(final ServletContext context) throws ServletException {
		if(logFile == null) { //if no log file has been determined (there is a benign race condition here)
			final DateFormat logFilenameDateFormat = new W3CDateFormat(W3CDateFormat.Style.DATE); //create a formatter for the log filename
			//TODO get "log" extension from Clogr
			final String logFilename = addExtension("servlet-" + logFilenameDateFormat.format(new Date()), "log"); //create a filename in the form "servlet-YYYY-MM-DD.log"
			logFile = new File(getLogDirectory(context), logFilename); //create the log file from the log directory and the log filename
		}
		return logFile; //return the log file
	}

	/** Whether this servlet is profiled. */
	private boolean profiled = false;

	/** @return Whether this servlet is profiled. */
	protected boolean isProfiled() {
		return profiled;
	}

	/** Whether debugging is enabled for this servlet. */
	private boolean debug = false;

	/** @return Whether debugging is enabled for this servlet. */
	protected boolean isDebug() {
		return debug;
	}

	/** The log configuration for this servlet, or <code>null</code> if the servlet hasn't yet been initialized. */
	//TODO convert to Clogr: private DefaultLogConfiguration logConfiguration = null;

	/** @return The log configuration for this servlet, or <code>null</code> if the servlet hasn't yet been initialized. */
	/*TODO convert to Clogr
	protected LogConfiguration getLogConfiguration() {
		return logConfiguration;
	}
	*/

	/**
	 * Initializes the servlet. This version delegates to {@link #initialize(ServletConfig)}, throwing the appropriate {@link ServletException} if certain runtime
	 * exceptions are thrown. Child classes should override {@link #initialize(ServletConfig)}.
	 * @param servletConfig The servlet configuration.
	 * @throws ServletException if there is a problem initializing.
	 */
	public final void init(final ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig); //do the default initialization
		try {
			initialize(servletConfig); //perform initialization
		} catch(final IllegalArgumentException illegalArgumentException) {
			throw new ServletException(illegalArgumentException);
		} catch(final IllegalStateException illegalStateException) {
			throw new ServletException(illegalStateException);
		}
	}

	/**
	 * Initializes the servlet. Child classes should call this version. This version ensures the log directory exists. This version configures logging. This
	 * version configures HTTP logging.
	 * @param servletConfig The servlet configuration.
	 * @throws ServletException if there is a problem initializing.
	 */
	public void initialize(final ServletConfig servletConfig) throws ServletException, IllegalArgumentException, IllegalStateException {
		this.debug = Boolean.TRUE.equals(getBooleanInitParameter(servletConfig, DEBUG_INIT_PARAMETER)); //get the debug setting from the init parameters
		//configure the log level before we do anything else
		Level logLevel = getEnumInitParameter(servletConfig, LOG_LEVEL_INIT_PARAMETER, Level.class); //get the log level from the init parameters
		if(logLevel == null && isDebug()) { //if no log level is specified but we are in debug mode
			logLevel = Level.DEBUG; //default to debug log level
		}
		try { //make sure the log directory exists
			ensureDirectoryExists(getLogDirectory(getServletContext())); //make sure the log directory exists
		} catch(final IOException ioException) { //if we can't create the log directory
			throw new ServletException(ioException);
		}
		//TODO set up log configuration: logConfiguration = new DefaultLogConfiguration(getLogFile(getServletContext()));
		if(logLevel != null) { //configure the log level if given
			Clogr.getLoggingConcern().setLogLevel(logLevel); //TODO maybe set the log level directly on the log configuration when setting it up
		}
		/*TODO set up log configuration appropriately
		logConfiguration.setStandardOutput(isDebug()); //if we are debugging, turn on logging to the standard output
		Log.setDefaultConfiguration(logConfiguration); //set the default log configuration
		*/
		//configure HTTP logging
		final Boolean logHTTP = getBooleanInitParameter(servletConfig, LOG_HTTP_INIT_PARAMETER); //get the HTTP log setting from the init parameters
		if(logHTTP != null) { //if there is an HTTP log setting specified
			HTTPClient.getInstance().setLogged(logHTTP.booleanValue()); //turn HTTP logging on or off
		}
		//configure profiling
		this.profiled = Boolean.TRUE.equals(getBooleanInitParameter(servletConfig, PROFILE_INIT_PARAMETER)); //get the profile setting from the init parameters
		getLogger().info("Initializing servlet {}.", servletConfig.getServletName());
		if(isProfiled()) { //if we are being profiled, configure the stack probe
			Profiler.setStackProbeOperation(StackProbeOperation.forServer()); //configure the stack probe for use on a server
		}
	}

	/** Destroys the servlet. */
	public void destroy() {
		if(isProfiled()) { //if we are profiling the servlet, print out stack probe counts
			try {
				Profiler.determineStackProbeOperation().printStackProbeCounts(System.out);
			} catch(final IOException ioException) {
				throw unexpected(ioException);
			}
		}
		getLogger().info("Destroying servlet {}.", getServletConfig().getServletName());
		/*TODO destroy the log configuration if needed
		if(logConfiguration != null) { //if we initialized a log configuration
			logConfiguration.dispose(); //dispose of the log configuration
		}
		*/
		super.destroy();
	}

	/** A thread-safe map of nonces, keyed to nonce ID strings. */
	private final Map<String, Nonce> nonceMap = new ConcurrentHashMap<String, Nonce>();

	/**
	 * Stores a nonce for later retrieval.
	 * @param nonceID The ID of the nonce; usually a hash of the nonce information.
	 * @param nonce The nonce to store, keyed to its ID.
	 */
	protected void storeNonce(final String nonceID, final Nonce nonce) {
		nonceMap.put(nonceID, nonce); //store the nonce in the map
	}

	/**
	 * Retrieves a nonce by its ID. This implementation takes the time to clear old nonces.
	 * @param nonceID The ID of the nonce to retrieve.
	 * @return The nonce with the given ID, or <code>null</code> if there is no matching nonce.
	 */
	protected Nonce getNonce(final String nonceID) {
		final Nonce nonce = nonceMap.get(nonceID); //get the nonce with the given ID
		final long currentTimeMillis = System.currentTimeMillis(); //get the current time
		for(final Map.Entry<String, Nonce> nonceEntry : nonceMap.entrySet()) { //for each nonce entry in the map TODO remove old nonces some other way, perhaps in a separate thread
			if(currentTimeMillis - nonceEntry.getValue().getTime().getTime() >= 60 * 60 * 1000) { //if the nonce has been around for an hour
				nonceMap.remove(nonceEntry.getKey()); //remove this ultra-stale nonce
			}
		}
		return nonce; //return the nonce with the given ID
	}

	/** The thread-safe map of principal IDs weakly keyed to nonces. */
	private final Map<Nonce, String> noncePrincipalIDMap = synchronizedMap(new WeakHashMap<Nonce, String>());

	/**
	 * Associates a principal with a nonce.
	 * @param nonce The nonce with which a principal should be associated.
	 * @param principalID The ID of the principal to associate with the nonce.
	 */
	protected void setNoncePrincipalID(final Nonce nonce, final String principalID) {
		noncePrincipalIDMap.put(nonce, principalID); //associate the principal with the nonce
	}

	/**
	 * Retrieves the principal associated with a nonce.
	 * @param nonce The nonce for which a principal should be associated.
	 * @return The ID of the principal associated with the given nonce, or <code>null</code> if there is no principal associated with the given nonce.
	 */
	protected String getNoncePrincipalID(final Nonce nonce) {
		return noncePrincipalIDMap.get(nonce); //return the ID of the principal associated with this nonce, if there is one
	}

	/** Whether this servlet has been initialized from an incoming request. */
	private boolean isInitializedFromRequest = false;

	/** The context path of the servlet. This is set on the first request received by the servlet. */
	private String contextPath = null;

	/**
	 * @return The context path of the servlet: either a string starting with '/' or the empty string.
	 * @throws IllegalStateException if this servlet has not yet received any requests.
	 */
	protected String getContextPath() { //TODO determine if this is encoded or not
		if(contextPath == null) { //if the context path has not been set
			throw new IllegalStateException("Servlet has received no requests and context path has not yet been set.");
		}
		return contextPath; //return the servlet's context path
	}

	/** The path of the servlet. This is set on the first request received by the servlet. */
	private String servletPath = null;

	/**
	 * @return The path of the servlet: either a string starting with '/' or the empty string.
	 * @throws IllegalStateException if this servlet has not yet received any requests.
	 */
	protected String getServletPath() {
		if(servletPath == null) { //if the context path has not been set
			throw new IllegalStateException("Servlet has received no requests and servlet path has not yet been set.");
		}
		return servletPath; //return the servlet path
	}

	/**
	 * From an absolute path to the server domain determines the context-relative absolute path.
	 * @param resourceServerAbsolutePath The absolute path to the server domain.
	 * @return An absolute path relative to the servlet context.
	 * @throws IllegalArgumentException if the given server absolute path does not begin with the servlet context path.
	 * @see #getContextPath()
	 */
	protected String getResourceContextAbsolutePath(final String resourceServerAbsolutePath) {
		final String contextPath = getContextPath(); //get the servlet context path
		if(!resourceServerAbsolutePath.startsWith(contextPath)) { //if the server absolute path does not start with the context path
			throw new IllegalArgumentException("Resource server absolute path " + resourceServerAbsolutePath + " is not located under context path " + contextPath);
		}
		final String resourceContextAbsolutePath = resourceServerAbsolutePath.substring(contextPath.length()); //remove the context path
		if(!isPathAbsolute(resourceContextAbsolutePath)) { //if the resulting path is not absolute, we split a segment in two
			throw new IllegalArgumentException("Resource server absolute path " + resourceServerAbsolutePath + " is not located under context path " + contextPath);
		}
		return resourceContextAbsolutePath; //create the context-relative absolute path
	}

	/**
	 * Initializes the servlet upon receipt of the first request. This version sets the context path and the servlet path.
	 * @param request The servlet request.
	 * @throws IllegalStateException if this servlet has already been initialized from a request.
	 * @throws ServletException if there is a problem initializing.
	 */
	public void initialize(final HttpServletRequest request) throws ServletException {
		if(isInitializedFromRequest) { //if we've already initialized from a request
			throw new IllegalStateException("Servlet already initialized from a request.");
		}
		contextPath = request.getContextPath(); //set the context path from the request
		servletPath = request.getServletPath(); //set the servlet path from the request
	}

	/**
	 * Services an HTTP request. This version checks authorization for all requests. This version provides support for special exceptions. Adding support for new
	 * HTTP methods must be done via the <code>doMethod()</code> method.
	 * @param request The HTTP request.
	 * @param response The HTTP response.
	 * @throws ServletException if there is a problem servicing the request.
	 * @throws IOException if there is an error reading or writing data.
	 * @see #checkAuthorization(HttpServletRequest)
	 * @see #doMethod(String, HttpServletRequest, HttpServletResponse)
	 */
	protected final void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		/*TODO del
		getLogger().trace("servicing method {}", request.getMethod());
		getLogger().trace("servlet path: {}", request.getServletPath());
		getLogger().trace("request URI: {}", request.getRequestURI());
		getLogger().trace("request URL: {}", request.getRequestURL());
		getLogger().trace("path info: {}", request.getPathInfo());
		*/
		getLogger().info("({}) {} {} {} {}", request.getRemoteAddr(), request.getMethod(), request.getRequestURL(), request.getQueryString(),
				request.getContentType()); //log the request
		if(!isInitializedFromRequest) { //if we haven't initialized from a request, yet TODO fix race condition here
			initialize(request); //initialize from this request
			isInitializedFromRequest = true; //show that we have initialized from a request
		} else { //if we have initialized from a request, make sure the variables are still the same
			final String requestContextPath = request.getContextPath(); //get the current context path for this request
			if(!requestContextPath.equals(contextPath)) { //if the context path has changed (we expect the context path to stay the same through the life of this servlet)
				throw new IllegalStateException("Servlet context path changed unexpectedly from " + contextPath + " to " + requestContextPath);
			}
			final String requestServletPath = request.getServletPath(); //get the current servlet path for this request
			if(!requestServletPath.equals(servletPath)) { //if the servlet path has changed (we expect the servlet path to stay the same through the life of this servlet)
				throw new IllegalStateException("Servlet path changed unexpectedly from " + servletPath + " to " + requestServletPath);
			}
		}
		if(isProfiled()) { //if we are being profiled, make sure a stack probe is started
			Profiler.startStackProbe();
		}
		try {
			if(!OPTIONS_METHOD.equals(request.getMethod())) { //TODO testing
				checkAuthorization(request); //check to see if the request is authorized
			}
			doMethod(request.getMethod(), request, response); //allow the subclass to do special processing if needed
		} catch(final OutOfMemoryError outOfMemoryError) { //if there was an out-of-memory error, log the info before rethrowing the error
			final Runtime runtime = Runtime.getRuntime(); //get the runtime instance
			getLogger().warn("Out of memory. memory max: {}, total: {}, free: {}, used: {}", runtime.maxMemory(), runtime.totalMemory(), runtime.freeMemory(),
					runtime.totalMemory() - runtime.freeMemory(), outOfMemoryError);
			throw outOfMemoryError; //rethrow the error
		} catch(final AssertionError assertionError) { //if there was an assertion error, that's a serious internal server error
			getLogger().warn("Assertion error.", assertionError); //log the problem
			response.sendError(SC_INTERNAL_SERVER_ERROR, assertionError.getMessage()); //send back a 500 Internal Server Error			
		} catch(final NullPointerException nullPointerException) { //if there was a null pointer exception, that's a serious internal server error
			getLogger().warn("Null pointer.", nullPointerException); //log the problem
			response.sendError(SC_INTERNAL_SERVER_ERROR, nullPointerException.getMessage()); //send back a 500 Internal Server Error			
		} catch(final ClassCastException classCastException) { //if there was a class cast exception, that's a serious internal server error
			getLogger().warn("Bad cast.", classCastException); //log the problem
			response.sendError(SC_INTERNAL_SERVER_ERROR, classCastException.getMessage()); //send back a 500 Internal Server Error			
		} catch(final IllegalArgumentException illegalArgumentException) { //if some method ran into an illegal argument, we assume the client is responsible
			getLogger().warn("Illegal argument.", illegalArgumentException); //log the problem
			response.sendError(SC_BAD_REQUEST, illegalArgumentException.getMessage()); //send back a 400 Bad Request error
		} catch(final IllegalStateException illegalStateException) { //if there was an illegal state exception, that's a serious internal server error
			getLogger().warn("Illegal state.", illegalStateException); //log the problem
			response.sendError(SC_INTERNAL_SERVER_ERROR, illegalStateException.getMessage()); //send back a 500 Internal Server Error			
		} catch(final UnsupportedOperationException unsupportedOperationException) { //if some operation is not supported by the server
			getLogger().warn("Unsupported operation.", unsupportedOperationException); //log the problem
			response.sendError(SC_NOT_IMPLEMENTED, unsupportedOperationException.getMessage()); //send back a 401 Not Implemented error
		} catch(final HTTPMovedPermanentlyException movedPermanentlyException) { //if a permanent redirect was requested (301)
			final URI locationURI = movedPermanentlyException.getLocation(); //get the redirect location
			if(locationURI != null) { //if a location was given
				setLocation(response, locationURI); //set the redirect location
			}
			response.sendError(movedPermanentlyException.getStatusCode()); //send back the redirect status code as an error
		} catch(final HTTPMovedTemporarilyException movedTemporarilyException) { //if a temporary redirect was requested (302)
			final URI locationURI = movedTemporarilyException.getLocation(); //get the redirect location
			if(locationURI != null) { //if a location was given
				setLocation(response, locationURI); //set the redirect location
			}
			response.sendError(movedTemporarilyException.getStatusCode()); //send back the redirect status code as an error
		} catch(final HTTPRedirectException redirectException) { //if a general redirect was requested (3xx)
			response.sendError(redirectException.getStatusCode()); //send back the redirect status code as an error
		} catch(final HTTPUnauthorizedException unauthorizedException) { //401 Unauthorized
			//TODO testing
			response.setHeader(DAV_HEADER, "1,2"); //we support WebDAV levels 1 and 2
			//issue the challenge in the WWW-authenticate header
			setWWWAuthenticate(response, unauthorizedException.getAuthenticateChallenge());
			response.sendError(unauthorizedException.getStatusCode()); //send back the status code as an error
		} catch(final HTTPNotFoundException httpNotFoundException) { //404 Not Found
			response.sendError(httpNotFoundException.getStatusCode(), httpNotFoundException.getMessage()); //send back the status code as an error, but don't bother logging the error
		} catch(final HTTPMethodNotAllowedException methodNotAllowedException) { //405 Method Not Allowed
			getLogger().warn("HTTP method not allowed.", methodNotAllowedException); //log the problem
			setAllow(response, methodNotAllowedException.getAllowedMethods()); //report the allowed methods
			response.sendError(methodNotAllowedException.getStatusCode()); //send back the status code as an error
		} catch(final HTTPException httpException) { //if any other HTTP error was encountered
			getLogger().warn("HTTP error.", httpException); //log the problem
			response.sendError(httpException.getStatusCode(), httpException.getMessage()); //send back the status code as an error
		} catch(final IOException ioException) { //if there is some other I/O error
			getLogger().error("I/O error.", ioException); //log the problem
			throw ioException; //rethrow the exception to let the container handle it
		} catch(final MissingResourceException missingResourceException) { //if there is a resource missing, the server isn't property configured
			getLogger().warn("Missing resource", missingResourceException); //log the problem
			//TODO find out why this isn't working with Tomcat 5.5.9
			response.sendError(SC_INTERNAL_SERVER_ERROR, missingResourceException.getMessage()); //send back a 500 Internal Server Error			
		} catch(final RuntimeException runtimeException) { //if there are any other runtime exceptions
			getLogger().error("Runtime exception.", runtimeException); //log the error
			throw runtimeException; //let the container take care of the error
		} catch(final Error error) { //if there are any other errors
			getLogger().error("Error.", error); //log the error
			throw error; //let the container take care of the error
		} finally {
			if(isProfiled()) { //if we are being profiled, make sure we stop the stack probe (if no other stack probes are running)
				Profiler.stopStackProbe();
			}
		}
	}

	/**
	 * Services an HTTP request based upon its method. Any subclass overriding this method must call the superclass version for normal handling of the standard
	 * HTTP methods.
	 * @param method The HTTP method being serviced.
	 * @param request The HTTP request.
	 * @param response The HTTP response.
	 * @throws ServletException if there is a problem servicing the request.
	 * @throws IOException if there is an error reading or writing data.
	 */
	protected void doMethod(final String method, final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		super.service(request, response); //do the default servicing of this method
	}

	/**
	 * Determines the URI of the requested resource.
	 * <p>
	 * If it is determined that the requested resource is located in another location, this method may throw an <code>HTTPRedirectException</code> with the new
	 * location. This method may choose to continue processing the request (e.g. if a client cannot handle redirects) using a different URI; in this case the new
	 * URI will be returned.
	 * </p>
	 * @param request The HTTP request indicating the requested resource.
	 * @return The URI of the requested resource, which may be different from the URL specified in the request.
	 * @throws HTTPRedirectException if the request should be redirected to another URI.
	 * @see HttpServletRequest#getRequestURL()
	 */
	protected URI getResourceURI(final HttpServletRequest request) throws HTTPRedirectException {
		final String requestURIString = request.getRequestURL().toString(); //get the requested URI string
		return URI.create(requestURIString); //create a URI from the full request URL
	}

	/**
	 * @return A private key used in generating and validating nonces. This implementation returns the name of the servlet class.
	 */
	protected String getNoncePrivateKey() {
		return getClass().getName(); //return the name of the implementing servlet class
	}

	/** @return A newly generated nonce. */
	protected Nonce createNonce() {
		return new DefaultNonce(getNoncePrivateKey()); //create a default nonce using our private key
	}

	/**
	 * Creates a nonce from an existing nonce string representation.
	 * @param nonceString The unhashed string representing the nonce.
	 * @return A nonce containing values represented by the nonce string.
	 * @throws SyntaxException if the given string does not have the correct format for this type of nonce.
	 * @see #createNonce()
	 */
	/*TODO del if not needed
		protected Nonce createNonce(final String nonceString) throws SyntaxException
		{
			return new DefaultNonce(getNoncePrivateKey());	//create a default nonce using our private key
		}
	*/

	/** The number of milliseconds after which a nonce will expire: 60 seconds. */
	protected final long NONCE_EXPIRATION_DURATION = 60 * 1000;

	/**
	 * Determines if the given nonce is valid. This includes checking:
	 * <ul>
	 * <li>The nonce private key to ensure it matches this server's nonce private key.</li>
	 * </ul>
	 * @param request The HTTP request.
	 * @param nonce The nonce to check for validity.
	 * @return <code>true</code> if the nonce is not valid.
	 * @see #getNoncePrivateKey()
	 */
	protected boolean isValid(final HttpServletRequest request, final Nonce nonce) {
		return getNoncePrivateKey().equals(nonce.getPrivateKey()); //check for an identical key
	}

	/**
	 * Determines if the given nonce is stale.
	 * @param nonce The nonce to check for staleness.
	 * @return <code>true</code> if the nonce time has lapsed beyond the maximum allowed.
	 * @see #NONCE_EXPIRATION_DURATION
	 */
	protected boolean isStale(final Nonce nonce) {
		//TODO del getLogger().trace("checking staleness of nonce {} with time {}", nonce, nonce.getTime().getTime());
		return System.currentTimeMillis() - nonce.getTime().getTime() > NONCE_EXPIRATION_DURATION; //see if the difference between now and then is longer than we allow
	}

	/**
	 * Checks whether the given request is authorized.
	 * @param request The HTTP request.
	 * @throws HTTPInternalServerErrorException if there is an error checking the authorization.
	 * @throws HTTPBadRequestException if the given credentials could not be understood.
	 * @throws HTTPRedirectException if the request should be redirected to another URI.
	 * @throws HTTPForbiddenException if the requested resource is not within any realm of authorization.
	 * @throws HTTPUnauthorizedException if the credentials do not provide authorization for access to the resource indicated by the given URI.
	 * @see #checkAuthorization(HttpServletRequest, URI, String, String, AuthenticateCredentials)
	 */
	protected void checkAuthorization(final HttpServletRequest request)
			throws HTTPInternalServerErrorException, HTTPBadRequestException, HTTPRedirectException, HTTPForbiddenException, HTTPUnauthorizedException {
		try {
			final URI resourceURI = getResourceURI(request); //get the requested URI
			final AuthenticateCredentials credentials = getAuthorization(request); //get the credentials from this request, if any
			checkAuthorization(request, resourceURI, request.getMethod(), request.getRequestURI(), credentials); //check authorization for these credentials
		} catch(final SyntaxException syntaxException) { //if the credentials weren't syntactically correct
			throw new HTTPBadRequestException(syntaxException);
		}
	}

	/**
	 * Checks whether the given credentials provide authorization for the given method.
	 * @param request The HTTP request.
	 * @param resourceURI The URI of the resource requested.
	 * @param method The HTTP method requested on the resource.
	 * @param requestURI The request URI as given in the HTTP request.
	 * @param credentials The principal's credentials, or <code>null</code> if no credentials are available.
	 * @throws HTTPInternalServerErrorException if there is an error checking the authorization.
	 * @throws HTTPForbiddenException if the requested resource is not within any realm of authorization.
	 * @throws HTTPUnauthorizedException if the credentials do not provide authorization for access to the resource indicated by the given URI.
	 * @see #getPrincipal(AuthenticateCredentials)
	 * @see #isAuthenticated(HttpServletRequest, URI, String, String, Principal, String, AuthenticateCredentials)
	 * @see #isAuthorized(HttpServletRequest, URI, String, Principal, String)
	 * @see #createAuthenticateChallenge(URI, String, Principal, String, AuthenticateCredentials, Nonce, boolean)
	 */
	protected void checkAuthorization(final HttpServletRequest request, final URI resourceURI, final String method, final String requestURI,
			final AuthenticateCredentials credentials) throws HTTPInternalServerErrorException, HTTPForbiddenException, HTTPUnauthorizedException {
		final Principal principal = getPrincipal(credentials); //get the principal providing credentials
		final String realm = getRealm(resourceURI); //get the realm for this resource
		boolean isAuthenticated = false; //every principal by default is unauthenticated
		if(isAuthenticated(request, resourceURI, method, requestURI, principal, realm, credentials)) { //if this principal is authenticated
			isAuthenticated = true; //the request is authenticated
		}
		authenticated(request, resourceURI, method, requestURI, principal, realm, credentials, isAuthenticated); //indicate whether the principal has been authenticated
		boolean isAuthorized = false; //every principal by default is unauthorized
		if(isAuthenticated && isAuthorized(request, resourceURI, method, principal, realm)) { //if this principal is authorized
			isAuthorized = true; //the request is both authenticated and authorized
		}
		if(credentials != null) { //if credentials were provided
			getLogger().info("authorized {} {} {} {} {}", isAuthorized, resourceURI, method, principal, realm); //log the authorization result
		}
		if(!isAuthorized) { //if authentication and authorization didn't succeed, throw an exception
			if(realm != null) { //if we have a realm to authenticate
				final AuthenticateChallenge challenge = createAuthenticateChallenge(resourceURI, method, principal, realm, credentials, createNonce(), false); //create an authenticate challenge with a newly created nonce
				throw new HTTPUnauthorizedException(challenge); //throw an unauthorized exception with the challenge
			} else { //if the requested resource is not within a realm
				throw new HTTPForbiddenException(resourceURI.toString()); //the request is forbidden for this resource
			}
		}
	}

	/**
	 * Called when a principal has went through authentication and indicates the result of authentication.
	 * @param request The HTTP request.
	 * @param resourceURI The URI of the resource requested.
	 * @param method The HTTP method requested on the resource.
	 * @param requestURI The request URI as given in the HTTP request.
	 * @param principal The principal requesting authentication, or <code>null</code> if the principal is not known.
	 * @param realm The realm for which the principal was authenticated.
	 * @param credentials The principal's credentials, or <code>null</code> if no credentials are available.
	 * @param authenticated <code>true</code> if the principal succeeded in authentication, else <code>false</code>.
	 */
	protected void authenticated(final HttpServletRequest request, final URI resourceURI, final String method, final String requestURI, final Principal principal,
			final String realm, final AuthenticateCredentials credentials, final boolean authenticated) {
	}

	/**
	 * Looks up a principal from the given credentials.
	 * @param credentials The principal's credentials, or <code>null</code> if no credentials are available.
	 * @return The principal corresponding to the given ID, or <code>null</code> if no principal could be determined.
	 * @throws HTTPInternalServerErrorException if there is an error getting the principal.
	 * @see #getPrincipal(String)
	 */
	protected Principal getPrincipal(final AuthenticateCredentials credentials) throws HTTPInternalServerErrorException {
		if(credentials != null) { //if we have credentials
			final String principalID = credentials.getPrincipalID(); //get the ID of the principal
			final int separatorIndex = CharSequences.indexOf(principalID, '\\'); //TODO testing
			if(separatorIndex >= 0) {
				return getPrincipal(principalID.substring(separatorIndex + 1)); //TODO testing
			} else {
				return getPrincipal(principalID);
			}
		} else { //if we have no credentials
			return null; //there is no principal
		}
		//TODO del		return credentials!=null ? getPrincipal(credentials.getPrincipalID()) : null;	//get the principal providing credentials, if there are credentials
	}

	/**
	 * Looks up a principal from the given ID. This version returns <code>null</code>.
	 * @param id The ID of the principal.
	 * @return The principal corresponding to the given ID, or <code>null</code> if no principal could be determined.
	 * @throws HTTPInternalServerErrorException if there is an error getting the principal.
	 */
	protected Principal getPrincipal(final String id) throws HTTPInternalServerErrorException {
		return null; //the basic HTTP servlet doesn't know any principals
	}

	/**
	 * Looks up the corresponding password for the given principal. This version returns <code>null</code>.
	 * @param principal The principal for which a password should be returned.
	 * @return The password associated with the given principal, or <code>null</code> if no password is associated with the given principal.
	 * @throws HTTPInternalServerErrorException if there is an error getting the principal's password.
	 */
	protected char[] getPassword(final Principal principal) throws HTTPInternalServerErrorException {
		return null; //the basic HTTP servlet doesn't know any passwords
	}

	/**
	 * Checks whether the given credentials provide authentication for the given URI and method. This version does basic authenticity checks on the credentials,
	 * if provided, so any subclass should first call this method.
	 * <ul>
	 * <li>The realm, if provided, is checked against the expected realm for the given resource.</li>
	 * <li>For digest access identification, the resource URI is checked against those of the credentials.</li>
	 * </ul>
	 * This version allows authentication for all <code>null</code> principals with no credentials, and all non-<code>null</code> principals valid credentials.
	 * @param request The HTTP request.
	 * @param resourceURI The URI of the resource requested.
	 * @param method The HTTP method requested on the resource.
	 * @param requestURI The request URI as given in the HTTP request.
	 * @param principal The principal requesting authentication, or <code>null</code> if the principal is not known.
	 * @param realm The expected realm for the given request URI.
	 * @param credentials The principal's credentials, or <code>null</code> if no credentials are available.
	 * @return <code>true</code> if the given credentials provide authentication for the given principal.
	 * @throws HTTPInternalServerErrorException if there is an error determining if the principal is authenticated.
	 * @throws HTTPUnauthorizedException if the credentials represent a stale digest authentication nonce.
	 */
	protected boolean isAuthenticated(final HttpServletRequest request, final URI resourceURI, final String method, final String requestURI,
			final Principal principal, final String realm, final AuthenticateCredentials credentials)
			throws HTTPInternalServerErrorException, HTTPUnauthorizedException {
		//TODO del getLogger().trace("authenticating");
		final String credentialsRealm = credentials != null ? credentials.getRealm() : null; //see if the credentials reports the realm, if we have credentials
		//TODO del getLogger().trace("got realm {}", realm);
		if(credentialsRealm != null && !credentialsRealm.equals(realm)) { //if a realm is given but it doesn't equal the expected realm for the requested resource
			//TODO del getLogger().trace("realm {} doesn't match", getRealm(resourceURI));
			return false; //don't allow credentials marked for one realm to be used for another realm
		}
		if(credentials != null) { //if there are credentials given
			if(credentials instanceof DigestAuthenticateCredentials) { //if these are digest credentials, make sure they are valid
				final DigestAuthenticateCredentials digestCredentials = (DigestAuthenticateCredentials)credentials; //get the credentials as digest credentials
				//TODO del getLogger().trace("comparing credentials URI {} against request URI {}", digestCredentials.getURI(), requestURI);
				if(!requestURI.equals(digestCredentials.getURI().toString())) { //if the request is for some other resource than the credentials indicate	//TODO remove toString() when we downgrade digest-uri to a String
					return false; //don't allow authentication for other resources
				}
				//			TODO del getLogger().trace("getting nonce for credentials nonce ID {}", digestCredentials.getNonce());
				final Nonce nonce = getNonce(digestCredentials.getNonce()); //get the nonce the request is using
				if(nonce == null || !isValid(request, nonce)) { //if we have no knowledge of this nonce or the nonce is invalid
					//				TODO del getLogger().trace("nonce {} is not valid", nonce);
					return false; //the nonce is either very old or an incorrect nonce altogether
				}
				if(principal == null) { //if no principal was given
					return false; //an anonymous principal cannot authenticate against given credentials
				}
				final char[] password = getPassword(principal); //get the password for the principal
				//TODO del getLogger().trace("got password {} for credentials", new String(password));
				if(password == null || !digestCredentials.isValid(method, password)) { //see if the credentials are valid for this principal's password
					return false; //indicate that the credentials have an invalid password
				}
				setNoncePrincipalID(nonce, principal.getName()); //associate this principal with the nonce
				//			TODO del getLogger().trace("checking staleness");
				if(isStale(nonce)) { //if the nonce is stale
					//				TODO del getLogger().trace("nonce is stale");
					final Nonce newNonce = createNonce(); //create a new, unstale nonce
					setNoncePrincipalID(newNonce, principal.getName()); //associate this principal with the new nonce
					final AuthenticateChallenge challenge = createAuthenticateChallenge(resourceURI, method, principal, realm, credentials, newNonce, true); //create an authenticate challenge with the new nonce
					throw new HTTPUnauthorizedException(challenge); //throw an unauthorized exception with the challenge
				}
				return true; //the credentials passed all the requirements
			} else { //if we don't recognize the credentials
				return false; //we can't authenticate credentials we don't recognize				
			}
		} else { //if no credentials were given
			//TODO del getLogger().trace("there were no credentials given, and principal is {}", principal);
			return principal == null; //only anonymous principals can authenticate against missing credentials
		}
	}

	/**
	 * Checks whether the given principal is authorized to invoke the given method on the given resource. This version authorized any principal invoking any
	 * method on any resource in any realm.
	 * @param request The HTTP request.
	 * @param resourceURI The URI of the resource requested.
	 * @param method The HTTP method requested on the resource.
	 * @param principal The principal requesting authentication, or <code>null</code> if the principal is not known.
	 * @param realm The realm with which the resource is associated, or <code>null</code> if the realm is not known.
	 * @return <code>true</code> if the given principal is authorized to perform the given method on the resource represented by the given URI.
	 * @throws HTTPInternalServerErrorException if there is an error determining if the principal is authorized.
	 */
	protected boolean isAuthorized(final HttpServletRequest request, final URI resourceURI, final String method, final Principal principal, final String realm)
			throws HTTPInternalServerErrorException {
		return true; //default to authorizing access
	}

	/**
	 * Creates an authentication challenge for the given resource This creates a digest authenticate challenge so any subclass should first call this method. This
	 * version stores the given nonce under a nonce ID digest generated by the credentials.
	 * @param resourceURI The URI of the resource requested.
	 * @param method The HTTP method requested on the resource.
	 * @param principal The principal requesting authentication, or <code>null</code> if the principal is not known.
	 * @param realm The realm in which the resource is located.
	 * @param credentials The principal's credentials, or <code>null</code> if no credentials are available.
	 * @param nonce The nonce to be used in the challenge.
	 * @param stale Whether the previous request from the client was rejected because the nonce value was stale.
	 * @return An authenticate challenge for the given resource URI and method.
	 * @throws HTTPInternalServerErrorException if there is an error creating the authenticate challenge.
	 */
	protected AuthenticateChallenge createAuthenticateChallenge(final URI resourceURI, final String method, final Principal principal, final String realm,
			final AuthenticateCredentials credentials, final Nonce nonce, final boolean stale) throws HTTPInternalServerErrorException {
		try {
			final DigestAuthenticateChallenge challenge = new DigestAuthenticateChallenge(realm, nonce.toString(), stale); //create a new digest authenticate challenge for the resource's realm, using the given nonce
			storeNonce(challenge.getNonceDigest(), nonce); //store the nonce under its digest value
			return challenge; //return the challenge
		} catch(final NoSuchAlgorithmException noSuchAlgorithmException) { //if the default algorithm (MD5) is not supported
			throw new HTTPInternalServerErrorException(noSuchAlgorithmException); //indicate that we don't support the algorithm
		}
	}

	/**
	 * Determines the realm applicable for the resource indicated by the given URI. This version returns <code>null</code>.
	 * @param resourceURI The URI of the resource requested.
	 * @return The realm appropriate for the resource, or <code>null</code> if the given resource is not in a known realm.
	 * @throws HTTPInternalServerErrorException if there is an error getting the realm.
	 */
	protected String getRealm(final URI resourceURI) throws HTTPInternalServerErrorException {
		return null;
	}

}
