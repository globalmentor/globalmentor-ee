package com.garretwilson.net.http.webdav;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.mail.internet.ContentType;
import javax.servlet.*;
import javax.servlet.http.*;

import com.garretwilson.io.OutputStreamUtilities;
import static com.garretwilson.io.ContentTypeUtilities.*;
import com.garretwilson.model.Resource;
import static com.garretwilson.net.URIUtilities.*;
import static com.garretwilson.net.http.webdav.WebDAVConstants.*;
import static com.garretwilson.text.CharacterConstants.*;
import com.garretwilson.util.*;

/**The base servlet class for implementing a WebDAV server as defined by
<a href="http://www.ietf.org/rfc/rfc2518.txt">RFC 2518</a>,	"HTTP Extensions for Distributed Authoring -- WEBDAV". 
@author Garret Wilson
*/
public abstract class AbstractWebDAVServlet<R extends Resource> extends HttpServlet
{
	
	private final static boolean LIST_DIRECTORIES=true;	//TODO fix

	/**Determines the URI of the requested resource.
  @param request The HTTP request indicating the requested resource.
  @return The URI of the requested resource.
  */
	protected URI getResourceURI(final HttpServletRequest request)
	{
		return URI.create(request.getRequestURL().toString());	//return a URI created from the full request URL
	}

	/**Services an HTTP request.
  This version provides support for WebDAV methods.
  @param request The HTTP request.
  @param response The HTTP response.
  @exception ServletException if there is a problem servicing the request.
  @exception IOException if there is an error reading or writing data.
  */
	protected void service(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
	{
		final String method=request.getMethod();	//get the method
Debug.setDebug(true);
Debug.setVisible(true);
Debug.trace("servicing method", method);
Debug.trace("servlet path:", request.getServletPath());
Debug.trace("request URI:", request.getRequestURI());
Debug.trace("request URL:", request.getRequestURL());
Debug.trace("path info:", request.getPathInfo());
		/*G***TODO fix other methods
		if(WebDAVMethod.GET.toString().equals(method))	//GET
		{
			
		}
		else	//if the request was not recognized
*/
		{
			super.service(request, response);	//do the default servicing
		}
/*G***del
      if (method.equals(METHOD_PROPFIND)) {
          doPropfind(req, resp);
      } else if (method.equals(METHOD_PROPPATCH)) {
          doProppatch(req, resp);
      } else if (method.equals(METHOD_MKCOL)) {
          doMkcol(req, resp);
      } else if (method.equals(METHOD_COPY)) {
          doCopy(req, resp);
      } else if (method.equals(METHOD_MOVE)) {
          doMove(req, resp);
      } else if (method.equals(METHOD_LOCK)) {
          doLock(req, resp);
      } else if (method.equals(METHOD_UNLOCK)) {
          doUnlock(req, resp);
      } else {
          // DefaultServlet processing
          super.service(req, resp);
      }
*/
	
  }

	/**Services the OPTIONS method.
	@param req The request
  @param request The HTTP request.
  @param response The HTTP response.
  @exception ServletException if there is a problem servicing the request.
  @exception IOException if there is an error reading or writing data.
  */
	public void doOptions(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
	{
Debug.trace("doing options");
		final URI resourceURI=getResourceURI(request);	//get the URI of the requested resource
Debug.trace("using resource URI", resourceURI);
		response.addHeader(DAV_HEADER, "1,2");	//we support WebDAV levels 1 and 2
		final Set<WebDAVMethod> allowedMethodSet=getAllowedMethods(resourceURI);	//get the allowed methods
		response.addHeader(ALLOW_HEADER, CollectionUtilities.toString(allowedMethodSet, COMMA_CHAR));	//put the allowed methods in the "allow" header, separated by commas
		response.addHeader(MS_AUTHOR_VIA_HEADER, MS_AUTHOR_VIA_DAV);	//tell Microsoft editing tools to use WebDAV rather than FrontPage
	}

	/**Services the HEAD method.
	@param req The request
  @param request The HTTP request.
  @param response The HTTP response.
  @exception ServletException if there is a problem servicing the request.
  @exception IOException if there is an error reading or writing data.
  */
	public void doHead(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
	{
		serveResource(request, response, false);	//serve the resource without its content
	}

	/**Services the GET method.
	@param req The request
  @param request The HTTP request.
  @param response The HTTP response.
  @exception ServletException if there is a problem servicing the request.
  @exception IOException if there is an error reading or writing data.
  */
	public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
	{
		serveResource(request, response, true);	//serve the resource with its content
		//TODO ignore any broken pipe error
	}

	/**Services the POST method.
	This version delegates to <code>doGet()</code>.
	@param req The request
  @param request The HTTP request.
  @param response The HTTP response.
  @exception ServletException if there is a problem servicing the request.
  @exception IOException if there is an error reading or writing data.
  @see #doGet(HttpServletRequest, HttpServletResponse)
  */
	public void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
	{
		doGet(request, response);	//delegate to the GET method servicing
	}

	/**Services the GET method.
	@param req The request
  @param request The HTTP request.
  @param response The HTTP response.
  @param serveContent <code>true</code> if the contents of the resource should be returned.
  @exception ServletException if there is a problem servicing the request.
  @exception IOException if there is an error reading or writing data.
  */
	protected void serveResource(final HttpServletRequest request, final HttpServletResponse response, final boolean serveContent) throws ServletException, IOException
	{
		final URI resourceURI=getResourceURI(request);	//get the URI of the requested resource
Debug.trace("serving resource", resourceURI);
		if(exists(resourceURI))	//if this resource exists
    {
    	//TODO check if headers
    	final R resource=getResource(resourceURI);	//get a resource description
//G***del    	final ContentType contentType;	//determine the content type of the resource
    	if(isCollection(resource.getReferenceURI()))	//if the resource is a collection
    	{
    		if(LIST_DIRECTORIES)	//if we should list directories
    		{
    			final Writer writer=response.getWriter();
    			response.setContentType("text/plain");
    			final List<R> resourceList=getChildResources(resource);
    			for(final R childResource:resourceList)
    			{
    				writer.append(childResource.toString()).append('\n');
    			}
    			
//TODO fix          contentType = "text/html;charset=UTF-8";
    			
    		}
    		else	//if we're not allowed to list directories
    		{
    			response.sendError(HttpServletResponse.SC_NOT_FOUND, resourceURI.toString());	//show that we didn't find a resource to return
//G***del    			return;	//don't do anything else
    		}    		
    	}
    	else	//if this resource is not a collection
    	{
    		final ContentType contentType=getContentType(resource);	//get the content type of the resource
      	if(contentType!=null)	//if we know the content type
      	{
      		response.setContentType(contentType.toString());	//tell the response which content type we're serving
      	}
      	final long contentLength=getContentLength(resource);	//get the content length of the resource
      	if(contentLength>=0)	//if we found a content length for the resource
      	{
      		assert contentLength<Integer.MAX_VALUE : "Resource size "+contentLength+" is too large.";
      		response.setContentLength((int)contentLength);	//tell the response the size of the resource      		
      	}
      	if(serveContent)	//if we should serve content
      	{
      		//TODO fix ranges
      		final OutputStream outputStream=response.getOutputStream();	//get the output stream TODO do we want to check for an IllegalStateException, and send back text if we can?
      		final InputStream inputStream=getInputStream(resource);	//get an input stream to the resource
      		try
      		{
      			OutputStreamUtilities.write(inputStream, outputStream);	//copy the input stream to the output stream
      		}
      		finally
      		{
     				inputStream.close();	//always close the input stream to the resource
      		}
      	}
    	}
    }
    else	//if the resource does not exist
    {
    	response.sendError(HttpServletResponse.SC_NOT_FOUND, resourceURI.toString());
    }
	}

  /**Determines the WebDAV methods allowed for the requested resource.
  @param resourceURI The URI of a resource for which options should be obtained.
  @return A set of methods allowed for this resource.
  */
	protected Set<WebDAVMethod> getAllowedMethods(final URI resourceURI)
	{
		final Set<WebDAVMethod> methodSet=EnumSet.of(WebDAVMethod.OPTIONS);	//we always allow options 
		if(exists(resourceURI))	//if the resource exists
		{
			methodSet.add(WebDAVMethod.COPY);
			methodSet.add(WebDAVMethod.GET);
			methodSet.add(WebDAVMethod.DELETE);
			methodSet.add(WebDAVMethod.HEAD);
//  	TODO implement  		methodSet.add(WebDAVMethod.LOCK);
			methodSet.add(WebDAVMethod.MOVE);
			methodSet.add(WebDAVMethod.POST);
			if(LIST_DIRECTORIES)	//if we allow directory listings
			{
//  		TODO implement  			methodSet.add(WebDAVMethod.PROPFIND);
			}
//  	TODO implement  		methodSet.add(WebDAVMethod.PROPPATCH);
//  	TODO implement  		methodSet.add(WebDAVMethod.TRACE);
//  	TODO implement  		methodSet.add(WebDAVMethod.UNLOCK);
			if(!isCollection(resourceURI))	//if the resource is not a collection
			{
				methodSet.add(WebDAVMethod.PUT);	//allow saving a resource to this location  			
			}
		}
		else	//if the resource does not exist
		{
//  	TODO implement  		methodSet.add(WebDAVMethod.LOCK);
			methodSet.add(WebDAVMethod.MKCOL);
			methodSet.add(WebDAVMethod.PUT);  		
		}
		return methodSet;	//return the allowed methods
	}

  /**Determines if the resource at a given URI exists.
  @param resourceURI The URI of the requested resource.
  @return <code>true</code> if the resource exists, else <code>false</code>.
  */
  protected abstract boolean exists(final URI resourceURI);

  /**Determines if the resource at a given URI is a collection.
  @param resourceURI The URI of the requested resource.
  @return <code>true</code> if the resource is a collection, else <code>false</code>.
  */
  protected abstract boolean isCollection(final URI resourceURI);

	/**Determines the requested resource.
  @param resourceURI The URI of the requested resource.
  @return An object providing an encapsulation of the requested resource,
  	but not necessarily the contents of the resource, or <code>null</code>
  	if no such resource exists. 
  */
	protected abstract R getResource(final URI resourceURI);

	/**Determines the content type of the given resource.
	This default version returns the MIME content type servlet known by the servlet context.
	@param resource The resource for which the content type should be determined.
	@return The content type of the given resource, or <code>null</code> if no
		content type could be determined.
	@see ServletContext#getMimeType(java.lang.String)
	*/
	protected ContentType getContentType(final R resource)
	{
		final String contentTypeString=getServletContext().getMimeType(getFileName(resource.getReferenceURI()));	//ask the servlet context for the MIME type
		return contentTypeString!=null ? createContentType(contentTypeString) : null;	//create a content type object if a content type string was returned
	}

	/**Determines the content length of the given resource.
	@param resource The resource for which the content length should be determined.
	@return The content length of the given resource, or <code>-1</code> if no
		content type could be determined.
	*/
	protected abstract long getContentLength(final R resource);

	/**Retrieves an input stream to the given resource.
	@param resource The resource for which an input stream should be retrieved.
	@return An input stream to the given resource.
	@exception IOException Thrown if there is an error accessing the resource,
		such as a missing file or a resource that has no contents.
	*/
	protected abstract InputStream getInputStream(final R resource) throws IOException;	//G***do we want to pass the resource or just the URI here?

	/**Retrieves an list of child resources of the given resource.
	@param resource The resource for which children should be returned.
	@return A list of child resources.
	@exception IOException Thrown if there is an error retrieving the list of child resources.
	*/
	protected abstract List<R> getChildResources(final R resource) throws IOException;	//G***do we want to pass the resource or just the URI here?

}
