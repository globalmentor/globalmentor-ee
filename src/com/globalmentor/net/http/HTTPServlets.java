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

package com.globalmentor.net.http;

import java.io.*;
import java.net.URI;
import java.text.*;
import java.text.ParseException;
import java.util.*;
import static java.util.Arrays.*;
import static java.util.Collections.*;
import java.util.regex.*;
import java.util.zip.*;

import javax.mail.internet.*;
import javax.servlet.http.*;

import com.globalmentor.io.*;
import com.globalmentor.java.CharSequences;
import com.globalmentor.net.*;
import com.globalmentor.net.mime.*;
import com.globalmentor.text.SyntaxException;
import com.globalmentor.util.*;

import static com.globalmentor.io.ContentTypes.*;
import static com.globalmentor.java.CharSequences.*;
import static com.globalmentor.java.Enums.*;
import static com.globalmentor.net.Servlets.*;
import static com.globalmentor.net.URIs.*;
import static com.globalmentor.net.http.Cookies.*;
import static com.globalmentor.net.http.HTTP.*;
import static com.globalmentor.net.http.HTTPFormatter.*;
import static com.globalmentor.net.http.HTTPParser.*;
import static com.globalmentor.net.http.webdav.WebDAV.*;
import static com.globalmentor.util.Locales.*;
import static com.globalmentor.util.regex.Matchers.*;

/**Constants and utilities for working with servlets.
@author Garret Wilson
*/
public class HTTPServlets
{

	/**The parameter that indicates the destination to which a series of web
		pages is progressing (e.g. the web page to which the user should be
		transferred after login is complete).
	*/
	public final static String DESTINATION_PARAMETER="destination";	//TODO move this to somewhere appropriate
	
	/**The custom attribute that indicates the exception that has occurred.*/
	public final static String EXCEPTION_ATTRIBUTE="exception";	//TODO move this to somewhere appropriate
	
	/**The name of the session tracking cookie (SRV.7.1.1).*/
	public final static String SESSION_ID_COOKIE_NAME="JSESSIONID";
	
	/**The name of the session identifier URL parameter used in URL rewriting (SRV.7.1.3).*/
	public final static String SESSION_ID_URL_PARAMETER="jsessionid";

	/**The private, static date format for reading and writing HTTP dates.
	Access to this object's methods should be synchronized on the object.
	*/
	private final static DateFormat HTTP_DATE_FORMAT=new HTTPDateFormat(); 

	/**Returns the headers indicating the accepted content types.
	@param request The HTTP request object.
	@return An enumeration of "accept" headers.
	@see HTTP#ACCEPT_HEADER
	*/
	public static Enumeration getAccept(final HttpServletRequest request)
	{
		return request.getHeaders(ACCEPT_HEADER); //return the accept headers
	}

	/**Returns a list of content types accepted by the client.
	@param request The HTTP request object.
	@return An array of content types accepted by the client.
	@see #getAccept(HttpServletRequest)
	*/
	public static ContentType[] getAcceptedContentTypes(final HttpServletRequest request)	//TODO allow for q designation
	{
		final List<ContentType> contentTypeList=new ArrayList<ContentType>();	//create a list of content types
		final Enumeration acceptEnumeration=getAccept(request);	//get the accept headers
		while(acceptEnumeration.hasMoreElements())	//while there are more accept headers
		{
			final String accept=(String)acceptEnumeration.nextElement();	//get the next accept header
			final StringTokenizer stringTokenizer=new StringTokenizer(accept, String.valueOf(LIST_DELIMITER));	//get the individual content types indicated TODO use a constant
			while(stringTokenizer.hasMoreTokens())	//while there are more tokens
			{
				final String token=stringTokenizer.nextToken();	//get the next token
				try
				{
					contentTypeList.add(new ContentType(trimRightFirst(token, ';').toString().trim()));	//add a new content type to the list, trimming off any whitespace and any quality designation TODO use a constant
				}
				catch(final javax.mail.internet.ParseException parseException)	//ignore content type strings that aren't syntactically correct
				{			
				}
			}
		}		
		return contentTypeList.toArray(new ContentType[contentTypeList.size()]); //return the list of content types as an array
	}

	/**Determines if the client accepts the given content type.
	Wildcard content types are correctly matched.
	@param request The HTTP request object.
	@param contentType The content type to check.
	@return <code>true</code> if the client accepts the given content type.
	*/
	public static boolean isAcceptedContentType(final HttpServletRequest request, final ContentType contentType)
	{
		return isAcceptedContentType(request, contentType, true);	//check accepted content types, matching wildcards
	}

	/**Determines if the client accepts the given content type.
	@param request The HTTP request object.
	@param contentType The content type to check.
	@param matchWildcards <code>true</code> if the content type should be matched against wildcard sequences, as is normal.
	@return <code>true</code> if the client accepts the given content type.
	*/
	public static boolean isAcceptedContentType(final HttpServletRequest request, final ContentType contentType, final boolean matchWildcards)
	{
		final ContentType[] acceptedContentTypes=getAcceptedContentTypes(request);	//get the accepted content types
		if(acceptedContentTypes.length==0)	//if no content types are listed as being accepted, then everything is accepted
		{
			return true;	//this content type (and all other content types) is accepted
		}
		for(final ContentType acceptedContentType:acceptedContentTypes)	//look at each content type
		{
			if(matchWildcards || acceptedContentType.getBaseType().indexOf(WILDCARD_CHAR)<0)	//only match wildcards if we were asked to
			{
				if(contentType.match(acceptedContentType))	//if our content type matches an accepted content type (make sure we match to the accepted content type, which can have wildcards)
				{
					return true;	//show that we found a match
				}
				if("*/*".equals(acceptedContentType.getBaseType()))	//if this is the wildcard content type TODO use a constant
				{
					return true;
				}
			}
		}
		return false;	//show that we didn't find an accepted content type
	}

	/**Returns a compatible content type accepted by the given request, based
		upon the given preferred content type.
	In particular, if "application/xhtml+xml" is preferred but not accepted by
		the requesting user agent, "text/html" is returned.
	This method is primarily for sending "application/xhtml+xml" content back
		to Microsoft Internet Explorer 6, which only supports XHTML when served as
		"text/html".
	@param request The HTTP request object.
	@param preferredContentType The content type preferred.
	@return The accepted content type, or <code>null</code> if no compatible
		content type is accepted by the requesting client.
	*/
/*G***del this method; IE sends the wildcard in its accept header, claiming that it accepts everything, so we'll just have to do a single check for the one XHTML media type---a general method won't work
	public static ContentType getAcceptedContentType(final HttpServletRequest request, final ContentType preferredContentType)
	{
		ContentType acceptedContentType=null;	//start out assuming that the preferred content type isn't accepted
		if(isAcceptedContentType(request, preferredContentType))	//if the preferred content type is accepted
		{
			acceptedContentType=preferredContentType;	//show that the preferred content type is OK
		}
		else	//if the preferred content type won't work
		{
			if(preferredContentType.match(XHTML_CONTENT_TYPE))	//if the preferred content type is "application/xhtml+xml"
			{
				if(isAcceptedContentType(request, HTML_CONTENT_TYPE))	//if the client accepts "text/html"
				{
					acceptedContentType=HTML_CONTENT_TYPE;	//step down to "text/html"
				}				
			}
		}
		return acceptedContentType;	//return the accepted content type we found
	}
*/

	/**Returns the headers indicating the accepted languages .
	@param request The HTTP request object.
	@return An enumeration of "accept-language" headers.
	@see HTTP#ACCEPT_LANGUAGE_HEADER
	*/
	@SuppressWarnings("unchecked")
	public static Enumeration<String> getAcceptLanguage(final HttpServletRequest request)
	{
		return (Enumeration<String>)request.getHeaders(ACCEPT_LANGUAGE_HEADER); //return the accept language headers
	}

	/**Returns a list of languages accepted by the client.
	@param request The HTTP request object.
	@return An array of locales representing languages accepted by the client.
	@see #getAcceptLanguage(HttpServletRequest)
	*/
	public static Locale[] getAcceptedLanguages(final HttpServletRequest request)	//TODO allow for q designation
	{
		final List<Locale> localeList=new ArrayList<Locale>();	//create a list of content types
		final Enumeration<String> acceptLanguageEnumeration=getAcceptLanguage(request);	//get the accept language headers
		while(acceptLanguageEnumeration.hasMoreElements())	//while there are more accept headers
		{
			final String acceptLanguage=(String)acceptLanguageEnumeration.nextElement();	//get the next accept header
			final StringTokenizer stringTokenizer=new StringTokenizer(acceptLanguage, String.valueOf(LIST_DELIMITER));	//get the individual content types indicated TODO use a constant
			while(stringTokenizer.hasMoreTokens())	//while there are more tokens
			{
				final String token=stringTokenizer.nextToken();	//get the next token
				final String language=trimRightFirst(token, ';').toString().trim();	//trim any quality designation TODO use a constant, and preferably cycle through all languages to get the one with the highest quality
				final Locale locale=createLocale(language);	//create a locale, converting from the HTTP language identification to Java Locale format
				localeList.add(locale);	//add a new locale to the list
			}
		}		
		return localeList.toArray(new Locale[localeList.size()]); //return the list of locales as an array
	}	

	/**Returns the header indicating the accepted encoding.
	@param request The HTTP request object.
	@return The content of the "Accept-Encoding" header, or <code>null</code> if there is no such header.
	@see HTTP#ACCEPT_ENCODING_HEADER
	*/
	public static String getAcceptEncoding(final HttpServletRequest request)
	{
		return request.getHeader(ACCEPT_ENCODING_HEADER); //return the accept encoding header
	}

	/**Determines if the client accepts the given encoding as indicated by the {@value HTTP#ACCEPT_ENCODING_HEADER} header.
	@param request The HTTP request object.
	@param contentCoding The transfer coding to check.
	@return <code>true</code> if the client accepts the given transfer coding according to RFC 2616, 14.3 Accept-Encoding.
	*/
	public static boolean isAcceptedEncoding(final HttpServletRequest request, final ContentCoding contentCoding)
	{
		final String acceptEncoding=getAcceptEncoding(request);	//get the accept encoding header
		if(acceptEncoding!=null)	//if there is an accept encoding header
		{
			try
			{
				final WeightedValue<String>[] weightedAcceptedEncodings=parseWeightedList(new ParseReader(acceptEncoding));	//parse the elements of the list
				for(final WeightedValue<String> weightedAcceptedEncoding:weightedAcceptedEncodings)	//for each weighted accepted encoding
				{
					final String acceptedEncoding=weightedAcceptedEncoding.getValue();	//get this accepted encoding
					if(contentCoding.toString().equalsIgnoreCase(acceptedEncoding) || CharSequences.equals(acceptedEncoding, WILDCARD_CHAR))	//if this encoding is the correct encoding or the wildcard character
					{
						if(weightedAcceptedEncoding.getQValue()>0)	//if this is not a zero weight (meaning unaccepted)
						{
							return true;	//this encoding is accepted
						}
						else	//if the qvalue is zero
						{
							if(!CharSequences.equals(acceptedEncoding, WILDCARD_CHAR))	//don't exclude this value simply because *;q=0 is present; we exclude non-specified values, anyway
							{
								return false;	//this value has been explitly excluded
							}
						}
					}
				}
			}
			catch(final IOException ioException)	//we don't expect an I/O error reading from a string
			{
				throw new AssertionError(ioException);
			}
		}
		return false;	//we couldn't find an acceptable encoding
	}
	
	/**Returns the authorization credentials of a request.
	This method does not allow the wildcard '*' request-URI for the digest URI parameter.
	@param request The HTTP request object.
	@return The credentials from the authorization header,
		or <code>null</code> if there is no such header.
	@exception SyntaxException if the given header was not syntactically correct.
	@exception IllegalArgumentException if the authorization information is not supported. 
	@see HTTP#AUTHORIZATION_HEADER
	*/
	public static AuthenticateCredentials getAuthorization(final HttpServletRequest request) throws SyntaxException, IllegalArgumentException
	{
		final String authorizationHeader=request.getHeader(AUTHORIZATION_HEADER); //get the authorization information
		return authorizationHeader!=null ? parseAuthorizationHeader(authorizationHeader) : null;	//parse the authorization header, if present
	}

	/**Returns the <code>If-Modified-Since</code> date of a request.
	@param request The HTTP request object.
	@return The date representing the last modified date, or <code>null</code> if there is no such header.
	@exception SyntaxException if the given header was not syntactically correct.
	@see HTTP#IF_MODIFIED_SINCE_HEADER
	*/
	public static Date getIfModifiedSinceDate(final HttpServletRequest request) throws SyntaxException
	{
		final String ifModifiedSinceHeader=request.getHeader(IF_MODIFIED_SINCE_HEADER); //get the If-Modified-Since information
		if(ifModifiedSinceHeader!=null)	//if there is If-Modified-Since information
		{
			try
			{
				synchronized(HTTP_DATE_FORMAT)	//don't allow concurrent access to the date format object
				{
					return HTTP_DATE_FORMAT.parse(ifModifiedSinceHeader);	//parse and return the date
				}
			}
			catch(final ParseException parseException)	//if there was a parsing error
			{
				throw new SyntaxException(parseException, ifModifiedSinceHeader);
			}
		}
		else	//if there is no such header
		{
			return null;	//indicate that there was no such header
		}
	}

	/**Returns the <code>If-Unmodified-Since</code> date of a request.
	@param request The HTTP request object.
	@return The date representing the last modified date, or <code>null</code> if there is no such header.
	@exception SyntaxException if the given header was not syntactically correct.
	@see HTTP#IF_UNMODIFIED_SINCE_HEADER
	*/
	public static Date getIfUnmodifiedSinceDate(final HttpServletRequest request) throws SyntaxException
	{
		final String ifUnmodifiedSinceHeader=request.getHeader(IF_UNMODIFIED_SINCE_HEADER); //get the If-Unmodified-Since information
		if(ifUnmodifiedSinceHeader!=null)	//if there is If-Unmodified-Since information
		{
			try
			{
				synchronized(HTTP_DATE_FORMAT)	//don't allow concurrent access to the date format object
				{
					return HTTP_DATE_FORMAT.parse(ifUnmodifiedSinceHeader);	//parse and return the date
				}
			}
			catch(final ParseException parseException)	//if there was a parsing error
			{
				throw new SyntaxException(parseException, ifUnmodifiedSinceHeader);
			}
		}
		else	//if there is no such header
		{
			return null;	//indicate that there was no such header
		}
	}

/*TODO fix	
	final String ifModifiedSinceHeader=request.getHeader(IF_MODIFIED_SINCE_HEADER);
	if(ifModifiedSinceHeader!=null)
	{
		Debug.trace("ifModifiedSince:", ifModifiedSinceHeader);
	}
	final String ifUnmodifiedSinceHeader=request.getHeader(IF_UNMODIFIED_SINCE_HEADER);
	if(ifUnmodifiedSinceHeader!=null)
	{
		Debug.trace("ifUnmodifiedSince:", ifUnmodifiedSinceHeader);
	}
*/

	/**Finds the current locale and returns it as a locale object. The locale is
		located by the following search order:
		<ol>
			<li>The request parameters are searched for a string "locale" parameter.</li>
			<li>The current session is searched for the <code>Locale</code> object in
				the attribute, "locale".</li>
			<li>The request's cookies are searched for the string value in the cookie,
				"locale".</li>
			<li>The request's default locale is used.</li>
		</ol>
	@param request The HTTP request object which may contain a session attribute
		or a cookie with the locale.
	@return A new locale object created for the appropriate locale.
	@see Servlets#LOCALE_ATTRIBUTE
	@see Servlets#LOCALE_COOKIE
	@see Servlets#LOCALE_PARAMETER
	@deprecated
	*/
	public static Locale getLocale(final HttpServletRequest request)
	{
		final String requestLocaleString=request.getParameter(LOCALE_PARAMETER);  //see  if the locale was passed as a parameter string
		if(requestLocaleString!=null) //if there was a locale parameter
			return Locales.createLocale(requestLocaleString);  //create a locale from the parameter and return it
		final HttpSession session=request.getSession(); //get the current session or, if there is no session, create one
		final Object sessionLocale=session.getAttribute(LOCALE_ATTRIBUTE);  //try to get the locale from the session G***use a constant here
		if(sessionLocale instanceof Locale) //if the object is a session
			return (Locale)sessionLocale; //we found the locale in the session
		final Cookie localeCookie=getCookie(request.getCookies(), LOCALE_COOKIE);	//get the locale coookie, if there is one
		if(localeCookie!=null)  //if there is a locale cookie
			return Locales.createLocale(localeCookie.getValue());  //create a locale from the cookie value and return it
		return request.getLocale(); //if we couldn't find any other locale, just get the default request locale
	}

	/**Sets the locale by storing it in the session and saving it as a cookie so that it will persist past the current session.
	@param request The HTTP request object from which to retrieve the current session.
	@param response The HTTP response.
	@see Servlets#LOCALE_ATTRIBUTE
	@see Servlets#LOCALE_COOKIE
	*/
	public static void setLocale(final Locale locale, final HttpServletRequest request, final HttpServletResponse response)
	{
		final HttpSession session=request.getSession(); //get the current session or, if there is no session, create one
		session.setAttribute(LOCALE_ATTRIBUTE, locale); //store the locale in the current session for quick lookup
		final Cookie localeCookie=new Cookie(LOCALE_COOKIE, locale.toString());	//create a new cookie with the locale string
		localeCookie.setMaxAge(999999); //set the cookie's expiration date G***fix
		response.addCookie(localeCookie); //set the cookie in the response so that it will be available next time
	}

	
	/**Determines the raw, unencoded servlet path info.
	This method is analogous to {@link HttpServletRequest#getPathInfo()} except that this method will return "/" instead of <code>null</code>.
	This version does not function with encoded servlet paths.
	@param request The HTTP request object.
	@return The unencoded path information, beginning with "/", or simply "/" if there is no path information.
	@exception IllegalArgumentException if the context path is encoded, or if the request URI does not begin with the context path and servlet path. 
	*/
	public static String getRawPathInfo(final HttpServletRequest request)
	{
//TODO del System.out.println("normal path info: "+request.getPathInfo());		
		final String contextPath=request.getContextPath();	//get the servlet context path (raw) ("/xxx" or "")
//TODO del System.out.println("context path: "+contextPath);
		String servletPath=request.getServletPath();	//get the path of the servlet itself (decoded) ("/xxx" or "")
//TODO del System.out.println("servlet path: "+servletPath);
		if(ROOT_PATH.equals(servletPath))	//if a bare root path was returned (e.g. WebSphere 6.0 bug when matching "/*")
		{
			servletPath="";	//convert the servlet path to what it should be
		}
//TODO del System.out.println("reformed servlet path: "+servletPath);
		final String pathPrefix=contextPath+servletPath;	//the path should be prefixed by the context path and servlet path
//TODO del System.out.println("path prefix: "+pathPrefix);
		final String requestURI=request.getRequestURI();	//get the request URI (raw) (we can't trust the path info, as it unencodes the data without being asked)
//TODO del System.out.println("request URI: "+requestURI);
		if(!requestURI.startsWith(pathPrefix))	//if the path info doesn't begin with the expected prefix
		{
			throw new IllegalArgumentException("Request URI "+requestURI+" does not start with context path and servlet path.");
		}
		return requestURI.substring(pathPrefix.length());	//skip over the context path and servlet path to get the raw path info
	}
	
	/**Returns the header indicating the referring location.
	@param request The HTTP request object.
	@return The "referer" header, or <code>null</code> if there is no such header.
	@see HttpServletConstants#REFERER_HEADER
	*/
	public static String getReferer(final HttpServletRequest request)
	{
		return request.getHeader(REFERER_HEADER); //return the referring location header
	}

	/**Returns the header indicating the referring location as a URI.
	@param request The HTTP request object.
	@return The "referer" header, or <code>null</code> if there is no such header.
	@exception IllegalArgumentException if the value of the referrer is not a valid URI.
	@see HttpServletConstants#REFERER_HEADER
	*/
	public static URI getRefererURI(final HttpServletRequest request)
	{
		final String referrer=getReferer(request);	//get the referrer string
		return referrer!=null ? URI.create(referrer) : null;	//return the referrer, if any, as a URI
	}

	/**Determines the full request URI, including any query parameters.
	@param request The HTTP request object.
	@return A full URI indicating the requested address, including query parameters, if any.
	*/
	public static URI getRequestURI(final HttpServletRequest request)
	{
		final StringBuffer requestURL=request.getRequestURL();	//get the request URL
		final String queryString=request.getQueryString();	//get the query string, if any
		if(queryString!=null && queryString.length()>0)	//if there is a query string (Tomcat 5.5.16 returns an empty string for no query, even though the Java Servlet specification 2.4 says that it should return null)
		{
			requestURL.append(QUERY_SEPARATOR).append(queryString);	//append the query string
		}
		return URI.create(requestURL.toString());	//return the full URI we constructed
	}

	/**Retrieves any exception in the request attributes indicating an error.
	@param request The HTTP request object.
	@return The "exception" attribute, or <code>null</code> if there is no
		exception or the object represented is not an exception.
	@see HTTPServlets#EXCEPTION_ATTRIBUTE
	*/
	public static Exception getExceptionAttribute(final HttpServletRequest request)
	{
		final Object exceptionObject=request.getAttribute(EXCEPTION_ATTRIBUTE); //get the exception attribute
		return exceptionObject instanceof Exception ? (Exception)exceptionObject : null;	//return the exception, if that's what it is
	}

	/**Sets an attribute indicating an error occurred.
	@param request The HTTP request object.
	@param exception The exception indicating the error that occurred.
	@see HTTPServlets#EXCEPTION_ATTRIBUTE
	*/
	public static void setExceptionAttribute(final HttpServletRequest request, final Exception exception)
	{
		request.setAttribute(EXCEPTION_ATTRIBUTE, exception);	//store the exception in the attributes
	}

	/**Returns the destination indicating the location to which a series of web
		pages is progressing (e.g. the web page to which the user should be
		transferred after login is complete).
	@param request The HTTP request object.
	@return The "destination" parameter, or <code>null</code> if there is no such parameter.
	@see HTTPServlets#DESTINATION_PARAMETER
	*/
	public static String getDestinationParameter(final HttpServletRequest request)
	{
		return request.getParameter(DESTINATION_PARAMETER); //return the destination location parameter, if there is one
	}

	/**Determines the destination indicating the location to which a series of web
		pages is progressing (e.g. the web page to which the user should be
		transferred after login is complete).
	<p>The destination is determined in the following order:</p>
	<ol>
		<li>The destination parameter is used, if available.</li>
		<li>The referring location is used as the destination, if available.</li>
		<li>The default destination is used, if provided.</li>
	<ol>
	@param request The HTTP request object.
	@param defaultDestination The destination to use by default, or
		<code>null</code> if there should be no default.
	@return The appriate destination, or <code>null</code> if the destination
		could not be determined.
	@see #getDestinationParameter
	@see #getReferer
	*/
	public static String getDestination(final HttpServletRequest request, final String defaultDestination)
	{
		String destination=getDestinationParameter(request);	//try to get the destination parameter
		if(destination==null)	//if no destination parameter was present
		{
			destination=getReferer(request);	//get the referring location
			if(destination==null)	//if no referring header was present
			{
				destination=defaultDestination;	//use the default
			}
		}
		return destination;	//return whatever destination we found
	}

	/**Returns the destination indicating the location to which a series of web
		pages is progressing (e.g. the web page to which the user should be
		transferred after login is complete). If no destination is available, the
		redirect goes to the root ("/").
	@param request The HTTP request.
	@return The target for redirection.
	*/	
/*G***del
	protected static String getDestination(final HttpServletRequest request)
	{
		final String destination=getDestination(request);	//get the URL of the place they want to eventually go, if there is one
		return destination!=null ? destination : String.valueOf(URIConstants.PATH_SEPARATOR);	//we'll redirect to the main page if we don't know where to go
	}
*/

	/**Returns the value of the WebDAV Destination header.
	@param request The HTTP request object.
	@return The URI indicating the destination of a COPY or MOVE,
		or <code>null</code> if the Destination header is not present.
	@see WebDAV#DESTINATION_HEADER
	@exception IllegalArgumentException if the Destination header value is not a valid
		URI or the represented URI is not absolute.
	*/
	public static URI getDestination(final HttpServletRequest request) throws IllegalArgumentException	//TODO maybe put this in special WebDAV utilities
	{
		final String destinationHeader=request.getHeader(DESTINATION_HEADER); //get the destination header value
		if(destinationHeader!=null)	//if there is a destination header value
		{
			final URI destinationURI=URI.create(destinationHeader);	//create a URI from the given value
			if(!destinationURI.isAbsolute() || !isAbsolutePath(destinationURI))	//if the URI is not absolute and the path is not absolute
			{
				throw new IllegalArgumentException(DESTINATION_HEADER+" header value "+destinationHeader+" is not absolute.");
			}
			return destinationURI;	//return the URI we created from the value
		}
		else	//if there is no destination header
		{
			return null;	//there is no destination URI
		}
	}	
	
	/**Returns the status of the WebDAV Overwrite header.
	@param request The HTTP request object.
	@return <code>true</code> if the Overwrite header is missing or "T", 
		or <code>false</code> if the value is "F".
	@exception IllegalArgumentException if the overwrite header is present and is not "T" or "F". 
	@see WebDAV#OVERWRITE_HEADER
	@see WebDAV#OVERWRITE_FALSE
	@see WebDAV#OVERWRITE_TRUE
	*/
	public static boolean isOverwrite(final HttpServletRequest request) throws IllegalArgumentException	//TODO maybe put this in special WebDAV utilities
	{
		final String overwriteHeader=request.getHeader(OVERWRITE_HEADER); //get the overwrite header value
		if(overwriteHeader!=null)	//if the overwrite header was given
		{
			if(overwriteHeader.equals(OVERWRITE_FALSE))	//if the value is "F"
			{
				return false;	//return false
			}
			else if(!overwriteHeader.equals(OVERWRITE_TRUE))	//if the value is present, but not "T" or "F"
			{
				throw new IllegalArgumentException("Illegal header "+OVERWRITE_HEADER+" value "+overwriteHeader+".");
			}
		}
		return true;	//default to allowing overwrite
	}

	/**Returns the headers indicating the user agent.
	@param request The HTTP request object.
	@return An enumeration of "UserAgent" headers.
	@see HttpServletConstants#ACCEPT_HEADER
	*/
	public static String getUserAgent(final HttpServletRequest request)	//TODO change to enumeration---or concatenate all user agents
	{
		return request.getHeader(USER_AGENT_HEADER);	//return the user agent
	}

	/**The name of the user agent, such as "Mozilla" or "MSIE".*/
	public final static String USER_AGENT_NAME_PROPERTY="user.agent.name";

		/**The Firefox user agent name.*/
		public final static String USER_AGENT_NAME_FIREFOX="Firefox";
		/**The Microsoft Internet Explorer user agent name.*/
		public final static String USER_AGENT_NAME_MSIE="MSIE";
		/**The Mozilla user agent name.*/
		public final static String USER_AGENT_NAME_MOZILLA="Mozilla";
		/**The Opera user agent name.*/
		public final static String USER_AGENT_NAME_OPERA="Opera";
		/**The wget user agent name.*/
		public final static String USER_AGENT_NAME_WGET="Wget";

			//spiders; see http://en.wikipedia.org/wiki/User-agent

		/**The Alexa user agent name.*/
		public final static String USER_AGENT_NAME_ALEXA="ia_archiver";
		/**The Baidu spider user agent name.*/
		public final static String USER_AGENT_NAME_BAIDU_SPIDER="Baiduspider";
		/**The Gigabot spider user agent name.*/
		public final static String USER_AGENT_NAME_GIGABOT="Gigabot";
		/**The Googlebot spider user agent name.*/
		public final static String USER_AGENT_NAME_GOOGLEBOT="Googlebot";
		/**The Googlebot Image spider user agent name.*/
		public final static String USER_AGENT_NAME_GOOGLEBOT_IMAGE="Googlebot-Image";
		/**The Apache Jakarta JMeter load tester user agent name.
		Although JMeter by default does not actually use this user agent name, it is included here for consistency in custom load-testing.
		*/
		public final static String USER_AGENT_NAME_JMETER="JMeter";
		/**The MSNbot spider user agent name.*/
		public final static String USER_AGENT_NAME_MSNBOT="msnbot";
		/**The AltaVista Scooter spider user agent name.*/
		public final static String USER_AGENT_NAME_SCOOTER="Scooter-3.2.EX";
		/**The W3C Validator user agent name.*/
		public final static String USER_AGENT_NAME_W3C_VALIDATOR="W3C_Validator";
		/**The Yahoo! MMCrawler spider user agent name.*/
		public final static String USER_AGENT_NAME_YAHOO_MMCRAWLER="Yahoo!-MMCrawler";

		/**The read-only set of spiders that do not support sessions.*/
		public final static Set<String> UNSESSIONED_SPIDER_USER_AGENT_NAMES=unmodifiableSet(new HashSet<String>(asList(
				USER_AGENT_NAME_ALEXA, USER_AGENT_NAME_BAIDU_SPIDER, USER_AGENT_NAME_GIGABOT, USER_AGENT_NAME_GOOGLEBOT,
				USER_AGENT_NAME_GOOGLEBOT_IMAGE, USER_AGENT_NAME_JMETER, USER_AGENT_NAME_MSNBOT, USER_AGENT_NAME_SCOOTER,
				USER_AGENT_NAME_W3C_VALIDATOR, USER_AGENT_NAME_YAHOO_MMCRAWLER)));		
		
		/**The Microsoft Internet Explorer 6 Security Version 1 identifier.*/
		public final static String USER_AGENT_MSIE6_SV1="SV1";
	
		
	/**The version of the user agent, stored as a string.*/
	public final static String USER_AGENT_VERSION_PROPERTY="user.agent.version";

	/**The version number of the user agent, stored as a {@link Double}.*/
	public final static String USER_AGENT_VERSION_NUMBER_PROPERTY="user.agent.version.number";

	/**The version numbers of the user agent, stored as an array of ints (<code>int[]</code>).*/
	public final static String USER_AGENT_VERSION_NUMBERS_PROPERTY="user.agent.version.numbers";

	/**The pattern for matching a general user agent. The user agent name is the first matching group, and the entire version number is the second matching group.*/
	private final static Pattern USER_AGENT_PATTERN=Pattern.compile("([\\S&&[^/]]+)(?:/("+PRODUCT_VERSION_REGEX+"))?");

	/**The pattern for matching the Firefox user agent. The entire version number is the first matching group.*/
	private final static Pattern FIREFOX_PATTERN=Pattern.compile("Firefox/("+PRODUCT_VERSION_REGEX+")");

	/**The pattern for matching the Opera user agent. The entire version number is the first matching group.
	This pattern recognizes, for example, both "Opera/7.54" and "Opera 7.54".
	*/
	private final static Pattern OPERA_PATTERN=Pattern.compile("Opera[/ ]("+PRODUCT_VERSION_REGEX+")");

	/**The pattern for matching the MSIE user agent. The entire version number is the first matching group.
	Microsoft recommended regular expression: "MSIE ([0-9]{1,}[\\.0-9]{0,})"
	@see <a href="http://msdn.microsoft.com/workshop/author/dhtml/overview/browserdetection.asp">Detecting Internet Explorer More Effectively</a>
	*/
	private final static Pattern MSIE_PATTERN=Pattern.compile("MSIE ("+PRODUCT_VERSION_REGEX+")");

	/**Retrieves a map of properties describing user agent information.
	For known browsers, the user agent string will be parsed with specific knowledge of the evolution of the user of the user agent string.
	@param httpRequest The HTTP request.
	@return A map of properties describing user agent information. 
	*/
	public static Map<String, Object> getUserAgentProperties(final HttpServletRequest httpRequest)
	{
		final Map<String, Object> properties=new HashMap<String, Object>();	//create a new map to hold our properties
		final String userAgent=getUserAgent(httpRequest);	//get the user agent string
		if(userAgent!=null)	//if there is a user agent string
		{
			//e.g. Opera: "Opera/7.54 (Windows NT 5.1; U)"
			//e.g. Opera: "Mozilla/5.0 (Windows NT 5.1; U) Opera 7.54"
			//e.g. Opera: "Mozilla/4.78 (Windows NT 5.1; U) Opera 7.54"
			//e.g. Opera: "Mozilla/3.0 (Windows NT 5.1; U) Opera 7.54"
			//e.g. Opera: "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1) Opera 7.54"
			//e.g. Firefox 1.5: "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8) Gecko/20051111 Firefox/1.5"
			//e.g. IE 6.0: "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; SV1; .NET CLR 1.1.4322; .NET CLR 2.0.50727)"
			//e.g. Safari 1.3.2: "Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en) AppleWebKit/312.8 (KHTML, like Gecko) Safari/312.5"
//TODO del Debug.trace("user agent:", userAgent);
			String userAgentName=null;	//we'll determine the user agent name
			String userAgentVersion=null;	//we'll determine the user agent version string
			int[] userAgentVersionNumbers=null;	//we'll determine the version numbers
			final Matcher operaMatcher=OPERA_PATTERN.matcher(userAgent);	//first match for Opera, which can masquerade as other browsers
			if(operaMatcher.find())	//if the user agent string finds an Opera match
			{
				userAgentName=USER_AGENT_NAME_OPERA;	//show that this is Opera
				userAgentVersion=operaMatcher.group(1);	//the first group is the entire version number
				userAgentVersionNumbers=getIntGroups(operaMatcher, 2);	//parse out the version, skipping the entire version string group
			}
			else	//if this is not Opera
			{
				final Matcher ieMatcher=MSIE_PATTERN.matcher(userAgent);	//match for IE
				if(ieMatcher.find())	//if the user agent string finds an IE match
				{
					userAgentName=USER_AGENT_NAME_MSIE;	//show that this is MSIE
					userAgentVersion=ieMatcher.group(1);	//the first group is the entire version number
					userAgentVersionNumbers=getIntGroups(ieMatcher, 2);	//parse out the version, skipping the entire version string group
				}
				else	//if this is not IE
				{
					final Matcher firefoxMatcher=FIREFOX_PATTERN.matcher(userAgent);	//match for Firefox
					if(firefoxMatcher.find())	//if the user agent string finds a Firefox match
					{
						userAgentName=USER_AGENT_NAME_FIREFOX;	//show that this is Firefox
						userAgentVersion=firefoxMatcher.group(1);	//the first group is the entire version number
						userAgentVersionNumbers=getIntGroups(firefoxMatcher, 2);	//parse out the version, skipping the entire version string group
					}
				}
			}
			if(userAgentName==null)	//if the user agent isn't any we recognize, detect the general user agent name
			{
				final Matcher userAgentMatcher=USER_AGENT_PATTERN.matcher(userAgent);	//match for general user agent
				if(userAgentMatcher.find())	//if the user agent string finds a user agent match
				{
					userAgentName=userAgentMatcher.group(1);	//extract the user agent name
					userAgentVersion=userAgentMatcher.group(2);	//the second group is the entire version number
					if(userAgentVersion!=null)	//if there is a user agent version
					{
						userAgentVersionNumbers=getIntGroups(userAgentMatcher, 3);	//parse out the version
					}
				}
			}
//TODO del Debug.trace("user agent name:", userAgentName, "with version", userAgentVersion, "with version numbers", userAgentVersionNumbers!=null ? Arrays.toString(userAgentVersionNumbers) : null);
			if(userAgentName!=null)	//if we determined a user agent name
			{
				properties.put(USER_AGENT_NAME_PROPERTY, userAgentName);	//store the user agent name
			}
			if(userAgentVersion!=null)	//if we determined a user agent version
			{
				properties.put(USER_AGENT_VERSION_PROPERTY, userAgentVersion);	//store the user agent version
			}
			if(userAgentVersionNumbers!=null)	//if we determined a user agent version numbers
			{
				properties.put(USER_AGENT_VERSION_NUMBERS_PROPERTY, userAgentVersionNumbers);	//store the user agent version numbers
				if(userAgentVersionNumbers.length>0)	//if there is at least one version number
				{
					final StringBuilder userAgentVersionNumberStringBuilder=new StringBuilder().append(userAgentVersionNumbers[0]);	//create a string with the first version number
					if(userAgentVersionNumbers.length>1)	//if there are at least two version numbers
					{
						userAgentVersionNumberStringBuilder.append('.').append(userAgentVersionNumbers[1]);	//append the second version number
					}
					final Double userAgentVersionNumber=Double.valueOf(userAgentVersionNumberStringBuilder.toString());	//create a Double from the first two version numbers
					properties.put(USER_AGENT_VERSION_NUMBER_PROPERTY, userAgentVersionNumber);	//store the user agent version number
				}
			}
		}
		return properties;	//return the properties
	}
	
	/**Sets the response header indicating the supported methods.
	@param response The HTTP response.
	@param allowed The set of supported methods.
	*/
	public static void setAllow(final HttpServletResponse response, final Set<String> allowed)
	{
		final String allow=formatList(new StringBuilder(), allowed).toString();	//format an HTTP list from the allowed mnethods
		response.setHeader(ALLOW_HEADER, allow);	//set the supported methods
	}

	/**Sets the response header indicating the content description.
	@param response The HTTP response.
	@param contentDescription The content description.
	*/
	public static void setContentDescription(final HttpServletResponse response, final String contentDescription)
	{
		response.setHeader(CONTENT_DESCRIPTION_HEADER, contentDescription);	//set the content description
	}

	/**Sets the response header indicating the content disposition.
	@param response The HTTP response.
	@param contentDispositionType The content disposition type.
	@param filename The disposition filename, or <code>null</code> if there is no filename to suggest.
	*/
	public static void setContentDisposition(final HttpServletResponse response, final ContentDispositionType contentDispositionType, final String filename)
	{
		try
		{
			final ContentDisposition contentDisposition=new ContentDisposition(getSerializationName(contentDispositionType));	//create a new content disposition object with the disposition type
			if(filename!=null)	//if a filename is provided
			{
				contentDisposition.setParameter("filename", filename);	//set the filename parameter TODO use a constant
			}
			setContentDisposition(response, contentDisposition);	//set the content disposition header with our constructed content disposition
		}
		catch(final javax.mail.internet.ParseException parseException)	//we're constructing the disposition type from known values, so we should never get this
		{
			throw new AssertionError(parseException);
		}
	}

	/**Sets the response header indicating the content disposition.
	@param response The HTTP response.
	@param contentDisposition The content disposition.
	*/
	public static void setContentDisposition(final HttpServletResponse response, final ContentDisposition contentDisposition)
	{
		response.setHeader(CONTENT_DISPOSITION_HEADER, contentDisposition.toString());	//set the content disposition
	}

	/**Sets the response header indicating the content encoding.
	@param response The HTTP response.
	@param contentCoding The content coding.
	*/
	public static void setContentEncoding(final HttpServletResponse response, final ContentCoding contentCoding)
	{
		response.setHeader(CONTENT_ENCODING_HEADER, contentCoding.toString());	//set the content encoding
	}

	/**Sets the response header indicating the canonical location of the resource.
	@param response The HTTP response.
	@param contentLocation The content location.
	*/
	public static void setContentLocation(final HttpServletResponse response, final URI contentLocation)
	{
		response.setHeader(CONTENT_LOCATION_HEADER, contentLocation.toString());	//set the content location
	}

	/**Sets the response header indicating the content language.
	@param response The HTTP response.
	@param languages The content languages.
	@exception IllegalArgumentException if no languages are provided.
	*/
	public static void setContentLanguage(final HttpServletResponse response, final Locale... languages)
	{
		if(languages.length==0)	//if no languages were provided
		{
			throw new IllegalArgumentException("At least one language must be provided.");
		}
		final Object[] languageTags=new String[languages.length];	//create a new string to hold language tags
		for(int i=languageTags.length-1; i>=0; --i)	//for each language
		{
			languageTags[i]=getLanguageTag(languages[i]);	//create a language tag for this language
		}
		final String contentLanguage=formatList(new StringBuilder(), languageTags).toString();	//format an HTTP list from the language tags
		response.setHeader(CONTENT_LANGUAGE_HEADER, contentLanguage);	//set the supported methods
	}

	/**Sets the response header indicating the last modified date of the resource.
	@param response The HTTP response.
	@param lastModifiedDate The last modified date of the resource.
	*/
	public static void setLastModified(final HttpServletResponse response, final Date lastModifiedDate)
	{
		synchronized(HTTP_DATE_FORMAT)	//don't allow concurrent access to the date format object
		{
			response.setHeader(LAST_MODIFIED_HEADER, HTTP_DATE_FORMAT.format(lastModifiedDate));	//set the last modified date
		}
	}
	
	/**Sets the appropriate response headers to prevent the user agent from caching the response content.
	This method sends appropriate headers based upon the browser type.
	@param response The HTTP request.
	@param response The HTTP response.
	@see HTTP#PRAGMA_HEADER
	@see HTTP#CACHE_CONTROL_HEADER
	@see HTTP#EXPIRES_HEADER
	@see <a href="http://support.microsoft.com/kb/q234067/">How to prevent caching in Internet Explorer</a>
	@see <a href="http://support.microsoft.com/kb/q199805/">PRB: Forward and Back Button Behavior in Internet Explorer</a>
	*/
	public static void setNoCache(final HttpServletRequest request, final HttpServletResponse response)
	{
		
		final Map<String, Object> userAgentProperties=getUserAgentProperties(request);	//get the user agent properties for this request
		if(USER_AGENT_NAME_MSIE.equals(userAgentProperties.get(USER_AGENT_NAME_PROPERTY)))	//if this is IE, set Cache-Control: no-cache
		{
			response.setHeader(CACHE_CONTROL_HEADER, NO_CACHE_CACHE_CONTROL);	//RFC 2616 14.9.1, 14.9.2; do not sent "no-cache,no-store" because this will cause IE6 to ignore the value and return cached pages in response to the browser Back and Forward buttons
		}
		else	//for all other browsers, send back Cache-Control: no-cache,no-store (which is required in Mozilla to prevent caching for the Back and Forward buttons)
		{
			response.setHeader(CACHE_CONTROL_HEADER, formatList(new StringBuilder(), NO_CACHE_CACHE_CONTROL, NO_STORE_CACHE_CONTROL).toString());	//RFC 2616 14.9.1, 14.9.2			
		}
		response.setHeader(PRAGMA_HEADER, NO_CACHE_PRAGMA);	//RFC 2616 14.9, 14.32
		response.setDateHeader(EXPIRES_HEADER, 0);	//RFC 2616 14.21		
	}

	/**Sets information about the content being returned.
	@param response The HTTP response.
	@param contentType The MIME content type.
	@param contentDisposition The disposition, such as the preferred filename.
	@param contentDescription Text describing the content.
	@see HTTPConstants#CONTENT_DISPOSITION_HEADER
	@see HTTPConstants#CONTENT_DESCRIPTION_HEADER
	*/
/*G**del; there's no real need to have a separate method for this
	public static void setContentInformation(final HttpServletResponse response, final String contentType, final String contentDisposition, final String contentDescription)
	{
		response.setContentType(contentType);	//set the content type
		response.setHeader(CONTENT_DISPOSITION_HEADER, contentDisposition);	//set the content disposition
		response.setHeader(CONTENT_DESCRIPTION_HEADER, contentDescription);	//set the content description
	}
*/

	/**Sets the response header indicating the destination of a redirection.
	@param response The HTTP response.
	@param location The redirection location.
	*/
	public static void setLocation(final HttpServletResponse response, final URI location)
	{
		response.setHeader(LOCATION_HEADER, location.toString());	//set the location
	}


	/**Sets the session ID of a URI.
	The session ID is appended to the URI path according to the <cite>Java Servlet Specification 2.4</cite> SRV7.7.1.3 URI Rewriting.
	If the URI already contains this session URI designation, the URI is returned unchanged.
	@param uri The for which a session URI should be set.
	@param sessionID The session ID to designate within the URI.
	@return A new URI with the given session ID designation.
	@see HTTPServlets#SESSION_ID_URL_PARAMETER
	*/
	public static URI setSessionID(final URI uri, final String sessionID)	//TODO fix to remove any session ID, even a different one
	{
		final String sessionSuffix=new StringBuilder().append(';').append(SESSION_ID_URL_PARAMETER).append('=').append(sessionID).toString();
		final String rawPath=uri.getRawPath();	//get the raw path, if any
		if(rawPath==null || !rawPath.endsWith(sessionSuffix))	//if the path does not already contain this session designation
		{
			return changeRawPath(uri, rawPath==null ? sessionSuffix : rawPath+sessionSuffix);	//add the suffix to any path
		}
		else	//if the path already ends with the session designation
		{
			return uri;	//return the URI unchanged
		}
	}
	
	
	/**Sets the response header challenging the client to authenticate itself.
	@param response The HTTP response.
	@param challenge The authenticate challenge to issue to the client.
	*/
	public static void setWWWAuthenticate(final HttpServletResponse response, final AuthenticateChallenge challenge)
	{
		response.setHeader(WWW_AUTHENTICATE_HEADER, formatWWWAuthenticateHeader(new StringBuilder(), challenge).toString());	//set the WWW-Authenticate header
	}

	/**Redirects to the destination, based upon the information given
		in the request.
	<p>The destination is determined in the following order:</p>
	<ol>
		<li>The destination parameter is used, if available.</li>
		<li>The root path ("/") is used.</li>
	<ol>
	@param request The HTTP request.
	@param response The HTTP response.
	@see #getDestinationParameter(HttpServletRequest)
	@exception IOException Thrown if there is a problem reading or writing data.
	*/
	public static void sendDestinationRedirect(final HttpServletRequest request, final HttpServletResponse response) throws IOException
	{
		sendDestinationRedirect(request, response, ROOT_PATH);	//send the redirect, using the root path as the default
	}

	/**Redirects to the destination, based upon the information given
		in the request and in the parameters.
	<p>The destination is determined in the following order:</p>
	<ol>
		<li>The destination parameter is used, if available.</li>
		<li>The default destination is used, if provided.</li>
		<li>The root path ("/") is used.</li>
	<ol>
	@param request The HTTP request.
	@param response The HTTP response.
	@param defaultDestination The destination to use by default, or
		<code>null</code> if there should be no default.
	@see #getDestinationParameter(HttpServletRequest)
	@exception IOException Thrown if there is a problem reading or writing data.
	*/
	public static void sendDestinationRedirect(final HttpServletRequest request, final HttpServletResponse response, final String defaultDestination) throws IOException
	{
		final String destination=getDestinationParameter(request);	//get the URL of the place they want to eventually go, if there is one
		response.sendRedirect(destination!=null ? destination : defaultDestination);  //redirect to the redirect target, or the default if there is no redirect target
	}

	/**Sends content in the response, optionally as an attachment.
	Caching is turned off.
	@param request The HTTP request.
	@param response The HTTP request.
	@param inputStream An input stream to the content to return.
	@param size The size of the content, or <code>-1</code> if not known.
	@param contentType The MIME type of the content, or <code>null</code> if not
		known, in which case the <code>application/octet-stream</code> type will be used.
	@param contentDispositionType The content disposition type.
	@param filename The disposition filename, or <code>null</code> if there is no filename to suggest.
	@param description The description of the content, or <code>null</code> if there is no description available.
	@exception IOException if there is an error reading the content or writing it to the response.
	@see #setContentDisposition(HttpServletResponse, ContentDispositionType, String)
	@see #setContentDescription(HttpServletResponse, String)
	*/
	public static void sendContent(final HttpServletRequest request, final HttpServletResponse response, final InputStream inputStream, final int size, final ContentType contentType, final ContentDispositionType contentDispositionType, final String filename, final String description) throws IOException
	{
		setNoCache(request, response);	//turn off caching
			//set the content type to the media type, if we have one, else a binary file
		response.setContentType(contentType!=null ? contentType.toString() : APPLICATION_OCTET_STREAM_CONTENT_TYPE.toString());
		if(filename!=null)	//if a filename is given, send the content as an attachment
		{
			setContentDisposition(response, contentDispositionType, filename);	//set the content disposition
		}
		if(description!=null)	//if a description is given
		{
			setContentDescription(response, description);	//tell the client more information about the resource
		}
		if(size>=0)	//if we know the size
		{
			response.setContentLength(size);	//show how many bytes we're going to download
		}
		final OutputStream outputStream=new BufferedOutputStream(response.getOutputStream());	//get an output stream to the response
		InputStreams.copy(inputStream, outputStream);  //copy the file to the output stream
		outputStream.flush(); //flush the output stream
	}

	/**Gets a compressed output stream to the HTTP response, but only if the HTTP request indicates that the user agent supports compression.
	The {@value HTTP#CONTENT_ENCODING_HEADER} header is updated if needed.
	Because IE6 with a service pack before 2002-01-08 will ignore the first 2K of compressed data,
		this method only compresses the data if IE6 indicates that Service Pack 2 or equivalent is installed by the presence of
		the {@value #USER_AGENT_MSIE6_SV1} value in the {@value HTTP#ACCEPT_ENCODING_HEADER} header.
	<p>Technically a {@value HTTP#TRANSFER_ENCODING_HEADER} header should be used for dynamically-compressed data,
		but currently browsers do not send a {@value HTTP#TE_HEADER} header to indicate that they can support transfer-encoding data.</p>
	@param request The HTTP request.
	@param response The HTTP response.
	@exception IOException if there is an error writing the data to the response.
	@see <a href="http://support.microsoft.com/default.aspx?scid=kb;en-us;Q312496">Internet Explorer May Lose the First 2,048 Bytes of Data That Are Sent Back from a Web Server That Uses HTTP Compression</a>
	@see <a href="http://blogs.msdn.com/ie/archive/2004/09/02/224902.aspx">XPSP2 and its slightly updated user agent string</a>
	@see <a href="http://www-128.ibm.com/developerworks/web/library/wa-httpcomp/">Speed Web delivery with HTTP compression</a>
	*/
	public static OutputStream getCompressedOutputStream(final HttpServletRequest request, final HttpServletResponse response) throws IOException
	{
		OutputStream outputStream=response.getOutputStream();	//default to the response's normal output stream
		final ContentCoding contentCoding=isAcceptedEncoding(request, ContentCoding.gzip) ? ContentCoding.gzip :	//use gzip if possible
				isAcceptedEncoding(request, ContentCoding.compress) ? ContentCoding.compress : null;	//use compress if gzip isn't available, or nothing if neither is available
		if(contentCoding!=null)	//if the request accepts gzip or compress
		{
			final String userAgent=getUserAgent(request);	//get the user agent string
			final Map<String, Object> userAgentProperties=getUserAgentProperties(request);	//get the user agent properties for this request
			final Object version=userAgentProperties.get(USER_AGENT_VERSION_NUMBER_PROPERTY);	//get the version number
			if(!USER_AGENT_NAME_MSIE.equals(userAgentProperties.get(USER_AGENT_NAME_PROPERTY))	//if this isn't MSIE
					|| (version instanceof Number && ((Number)version).doubleValue()>6.0)	//or this is an MSIE version greater than 6.0
					|| (userAgent!=null && userAgent.indexOf(USER_AGENT_MSIE6_SV1)>=0))	//or this is MSIE with Service Pack 2 installed (only IE6 would have SP2)
			{
//TODO del Debug.traceStack("using encoding:", contentCoding);
				setContentEncoding(response, contentCoding);	//set the content coding
					//TODO decide whether to set the "Vary" header; see http://www.unix.org.ua/orelly/java-ent/servlet/ch06_02.htm
				switch(contentCoding)	//see which output stream we should create
				{
					case gzip:
						outputStream=new GZIPOutputStream(outputStream);	//wrap the output stream in a gzip output stream
						break;
					case compress:
						outputStream=new ZipOutputStream(outputStream);	//wrap the output stream in a zip output stream
						((ZipOutputStream)outputStream).putNextEntry(new ZipEntry("response"));	//create a dummy zip entry; see http://www.unix.org.ua/orelly/java-ent/servlet/ch06_02.htm
						break;
					default:	//if we don't recognize the content coding, the code isn't synchronized
						throw new AssertionError("Unaccounted for content coding: "+contentCoding);
				}
			}
		}
		return outputStream;	//return the output stream
	}

}