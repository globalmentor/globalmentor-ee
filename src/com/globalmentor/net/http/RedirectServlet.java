package com.globalmentor.net.http;

import java.io.*;
import java.net.URI;
import javax.servlet.*;
import javax.servlet.http.*;

import static com.globalmentor.net.URIs.*;

/**An HTTP servlet that unconditionally redirects to a configured URI.
<p>The redirect URI, specified in the "redirectURI" initialization parameter,
	is interpreted according to to the following criteria:</p>
<ul>
	<li>If the URI is absolute, the redirect will occur to the absolute URI.</li>
	<li>If the URI is relative but the path is absolute, the redirect will occur
		relative to the web application context.</li> 
	<li>If the URI is relative and the path is relative, the redirect will occur
		relative to the requested URI collection.</li>
</ul>
<p>If the initialization parameter "permanent" is set to "true", the redirection
	will be be 301 Moved Permanently; otherwise, the redirection will be
	302 Found.</p>
@author Garret Wilson
*/
public class RedirectServlet extends BasicHTTPServlet
{

	/**The required initialization parameter "redirectURI" specifying the redirect URI.*/
	public final String REDIRECT_URI_PARAMETER="redirectURI";

	/**The optional initialization parameter "permanent" specifying whether the redirect should be permant.*/
	public final String PERMANENT_PARAMETER="permanent";

	/**Services an HTTP request based upon its method.
  This version redirects all request to a configured URI.
  @param method The HTTP method being serviced. 
  @param request The HTTP request.
  @param response The HTTP response.
  @exception ServletException if there is a problem servicing the request.
  @exception IOException if there is an error reading or writing data.
  */
	protected void doMethod(final String method, final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
	{
		final ServletConfig config=getServletConfig();	//get the servlet configuration
		final String requestedRedirectURIString=config.getInitParameter(REDIRECT_URI_PARAMETER);	//get the redirect URI parameter
		if(requestedRedirectURIString!=null)	//if a redirect URI was given
		{
			final StringBuilder queryURIBuilder=new StringBuilder(requestedRedirectURIString);	//build a redirect URI with the submitted query
			final String query=request.getQueryString();	//get the query string
			if(query!=null)	//if there was a query in the request
			{
				queryURIBuilder.append(QUERY_SEPARATOR).append(query);	//append the query to the redirect URI
			}
			final URI requestedRedirectURI=URI.create(queryURIBuilder.toString());	//see which redirect URI was requested, with submitted query
			final URI redirectURI;	//we'll determine the absolute redirect URI
			if(requestedRedirectURI.isAbsolute())	//if the given URI is absolute
			{
				redirectURI=requestedRedirectURI;	//use the provided URI unmodified
			}
			else	//if the given URI is not absolute
			{
				if(isAbsolutePath(requestedRedirectURI))	//if the requested URI has an absolute path
				{
						//TODO make sure that no authority was given
					redirectURI=URI.create(request.getContextPath()+requestedRedirectURI);	//append the redirect URI to the context path, along with the query string
				}
				else	//if the requested URI doesn't have an absolute path
				{
					final URI requestURI=getResourceURI(request);	//see which URI was requested
					redirectURI=getCurrentLevel(requestURI).resolve(requestedRedirectURI);	//resolve the requested redirect against the request current level
				}
			}
				//get the boolean value of the permanent parameter
			final boolean permanent=Boolean.parseBoolean(config.getInitParameter(PERMANENT_PARAMETER));
			if(permanent)	//if this is a permanent redirect
			{
				throw new HTTPMovedPermanentlyException(redirectURI);	//redirect permanently
			}
			else	//if the redirect is not permanent
			{
				throw new HTTPMovedTemporarilyException(redirectURI);	//redirect temporarily				
			}
		}
		else	//if no redirect URI was configured
		{
			throw new HTTPInternalServerErrorException("No redirect URI initialization parameter is configured.");
		}
  }

}