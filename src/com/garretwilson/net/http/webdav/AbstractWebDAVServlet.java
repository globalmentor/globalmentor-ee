package com.garretwilson.net.http.webdav;

import java.io.*;
import java.net.*;
import java.security.Principal;
import java.util.*;
import static java.util.Collections.*;
import java.util.regex.Pattern;

import javax.mail.internet.ContentType;
import javax.servlet.*;
import javax.servlet.http.*;

import org.w3c.dom.*;

import static com.garretwilson.io.ContentTypeConstants.*;
import com.garretwilson.io.ContentTypeUtilities;
import com.garretwilson.io.InputStreamUtilities;
import com.garretwilson.io.OutputStreamUtilities;
import static com.garretwilson.io.ContentTypeUtilities.*;
import com.garretwilson.lang.CharacterUtilities;
import static com.garretwilson.lang.CharSequenceUtilities.*;
import com.garretwilson.model.Resource;
import static com.garretwilson.net.URIConstants.*;
import static com.garretwilson.net.URIUtilities.*;
import com.garretwilson.net.http.*;
import static com.garretwilson.net.http.HTTPConstants.*;
import static com.garretwilson.net.http.webdav.WebDAVConstants.*;
import static com.garretwilson.net.http.webdav.WebDAVMethod.*;
import static com.garretwilson.servlet.http.HttpServletUtilities.*;
import static com.garretwilson.text.CharacterConstants.*;
import static com.garretwilson.text.CharacterEncodingConstants.*;

import com.garretwilson.text.CharacterEncoding;
import com.garretwilson.text.xml.QualifiedName;
import com.garretwilson.text.xml.XMLDOMImplementation;
import com.garretwilson.text.xml.XMLProcessor;
import com.garretwilson.text.xml.XMLSerializer;
import com.garretwilson.text.xml.XMLUtilities;
import static com.garretwilson.text.xml.XMLUtilities.*;
import com.garretwilson.util.*;

/**The base servlet class for implementing a WebDAV server as defined by
<a href="http://www.ietf.org/rfc/rfc2518.txt">RFC 2518</a>,	"HTTP Extensions for Distributed Authoring -- WEBDAV". 
@author Garret Wilson
*/
public abstract class AbstractWebDAVServlet<R extends Resource> extends BasicHTTPServlet	//TODO address http://lists.w3.org/Archives/Public/w3c-dist-auth/1999OctDec/0343.html
{
	
	private final static boolean LIST_DIRECTORIES=true;	//TODO fix

	private final static boolean READ_ONLY=false;	//TODO fix

	/**The constant property list indicating all properties.*/
	protected final static IDMappedList<URI, QualifiedName> ALL_PROPERTIES=new IDMappedList<URI, QualifiedName>((Map<URI, QualifiedName>)emptyMap(), (List<QualifiedName>)emptyList());

	/**The constant property list indicating all property names.*/
	protected final static IDMappedList<URI, QualifiedName> PROPERTY_NAMES=new IDMappedList<URI, QualifiedName>((Map<URI, QualifiedName>)emptyMap(), (List<QualifiedName>)emptyList());

	/**The DOM implementation used as a document factory.*/
	private final DOMImplementation domImplementation=new XMLDOMImplementation();	//TODO get this in a general way

		/**@return The DOM implementation used as a document factory.*/
		protected DOMImplementation getDOMImplementation() {return domImplementation;}

	/**An array of regular expressions matching user agents not correctly supporting redirects.
	@see http://httpd.apache.org/docs-2.0/env.html#special
	@see http://lists.w3.org/Archives/Public/w3c-dist-auth/2002AprJun/0190.html
	@see http://purl.org/NET/http-errata#saferedirect
	*/
	private final static Pattern[] REDIRECT_UNSUPPORTED_AGENTS=new Pattern[]
		  {
				Pattern.compile("^gnome-vfs.*"),	//Gnome; see http://bugzilla.gnome.org/show_bug.cgi?id=92908 ; https://bugzilla.redhat.com/beta/show_bug.cgi?id=106290
//G***del				"gnome-vfs/*"	//see http://mail.gnome.org/archives/gnome-vfs-list/2002-December/msg00028.html
				Pattern.compile("Microsoft Data Access Internet Publishing Provider.*"),	//http://lists.w3.org/Archives/Public/w3c-dist-auth/2002AprJun/0190.html
				Pattern.compile("Microsoft-WebDAV-MiniRedir/5\\.1\\.2600.*"),	//http://mailman.lyra.org/pipermail/dav-dev/2003-June/004777.html
				Pattern.compile("^DAVAccess/1\\.[01234]\\.[1234].*"),	//iCal; see http://macintouch.com/panreader02.html
				Pattern.compile("^Dreamweaver-WebDAV-SCM1\\.0[23].*"),	//Dreamweaver MX, 2003; see http://archive.webct.com/docs/mail/nov03/0018.html
				Pattern.compile("^neon.*"),	//neon; see http://archive.webct.com/docs/mail/nov03/0018.html ; http://www.mail-archive.com/tomcat-dev@jakarta.apache.org/msg53373.html
				Pattern.compile("^WebDAVFS.*"),	//Macintosh OS X Jaquar; see http://www.askbjoernhansen.com/archives/2002/08/27/000115.html
//G***del				"^WebDAVFS/1.[012]",	//Macintosh; see http://www.macosxhints.com/article.php?story=20021114063433862
				Pattern.compile("^WebDrive.*"),	//http://lists.w3.org/Archives/Public/w3c-dist-auth/2002AprJun/0190.html
		  };


	/**Determines if the user agent sending the request supports redirects.
	An agent is assumed to support redirects unless its name is recognized
		as an agent not supporting redirects.
	@param request The HTTP request.
	@return <code>true</code> if the agent sending the request is not known
		 to be redirect-averse.
	@see #REDIRECT_UNSUPPORTED_AGENTS
	*/
	protected static boolean isRedirectSupported(final HttpServletRequest request)	//TODO maybe transfer this to BasicServlet
	{
		final String userAgent=getUserAgent(request);	//get the user agent sending this request
//G***del Debug.trace("checking user agent for redirect support", userAgent);
		for(final Pattern pattern:REDIRECT_UNSUPPORTED_AGENTS)	//look at each agent not supporting redirects
		{
//G***del Debug.trace("checking pattern", pattern);
			if(pattern.matcher(userAgent).matches())	//if this is a buggy user agent
			{
				return false;	//show that we recognize this user agent as one not correctly supporting redirects
			}
		}
		return true;	//if we didn't recognize the user agent, we assume it supports redirects
//G***del		return false;	//TODO fix
	}
	
	/**Services an HTTP request based upon its method.
	This version provides support for WebDAV methods.
  @param method The HTTP method being serviced. 
  @param request The HTTP request.
  @param response The HTTP response.
  @exception ServletException if there is a problem servicing the request.
  @exception IOException if there is an error reading or writing data.
  */
	protected void doMethod(final String method, final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
	{
		final WebDAVMethod webdavMethod;
		try
		{
			webdavMethod=WebDAVMethod.valueOf(method);	//see which WebDAV method was requested
		}
		catch(final IllegalArgumentException illegalArgumentException)	//if we don't understand the method
		{
			super.doMethod(method, request, response);	//do the default servicing
			return;	//don't process the method further
		}		
		switch(webdavMethod)	//see which WebDAV method this is
		{
			case COPY:
				doCopy(request, response);	//delegate to the copy method
				break;
			case MOVE:
				doMove(request, response);	//delegate to the move method
				break;
			case MKCOL:
				doMkCol(request, response);	//delegate to the mkcol method
				break;
			case PROPFIND:
				doPropFind(request, response);	//delegate to the propfind method
				break;
			default:
				super.doMethod(method, request, response);	//do the default servicing if we don't service the method
		}
  }

	/**Services the OPTIONS method.
  @param request The HTTP request.
  @param response The HTTP response.
  @exception ServletException if there is a problem servicing the request.
  @exception IOException if there is an error reading or writing data.
  */
	public void doOptions(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
	{
		final URI resourceURI=getResourceURI(request);	//get the URI of the requested resource
Debug.trace("doing options for URI", resourceURI);
		response.setHeader(DAV_HEADER, "1,2");	//we support WebDAV levels 1 and 2
		final Set<WebDAVMethod> allowedMethodSet=getAllowedMethods(resourceURI);	//get the allowed methods
		response.addHeader(ALLOW_HEADER, CollectionUtilities.toString(allowedMethodSet, COMMA_CHAR));	//put the allowed methods in the "allow" header, separated by commas
		response.addHeader(MS_AUTHOR_VIA_HEADER, MS_AUTHOR_VIA_DAV);	//tell Microsoft editing tools to use WebDAV rather than FrontPage
	}

	/**Services the HEAD method.
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

	/**Services the PUT method.
  @param request The HTTP request.
  @param response The HTTP response.
  @exception ServletException if there is a problem servicing the request.
  @exception IOException if there is an error reading or writing data.
  */
	public void doPut(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
	{
		if(!READ_ONLY)	//if this servlet is not read-only
		{
Debug.trace("getting resource URI");
			final URI resourceURI=getResourceURI(request);	//get the URI of the requested resource
Debug.trace("checking destination existence");
			final boolean exists=exists(resourceURI);	//see whether the resource already exists
Debug.trace("exists", exists);
			final R resource;	//we'll get the existing resource, if there is one 
			if(exists)	//if this resource exists
	    {
				resource=getResource(resourceURI);	//get the resource information
	    }
			else	//if the resource doesn't exist
			{
Debug.trace("trying to create resource");
				try
				{
					resource=createResource(resourceURI);	//create the resource TODO make sure no default resource content is created here
				}
				catch(final IllegalArgumentException illegalArgumentException)	//if this is an invalid resource URI
				{
Debug.warn(illegalArgumentException);
					throw new HTTPForbiddenException(illegalArgumentException);	//forbid creation of resources with invalid URIs
				}
			}
			try
			{
				final InputStream inputStream=request.getInputStream();	//get an input stream from the request
				final OutputStream outputStream=getOutputStream(resource);	//get an output stream to the resource
				try
				{
Debug.trace("trying to write");
					OutputStreamUtilities.write(inputStream, outputStream);	//copy the file from the request to the resource
				}
				finally
				{
					outputStream.close();	//always close the output stream
				}
			}
			catch(final IOException ioException)	//if we have any problems saving the resource contents
			{
				if(!exists)	//if the resource didn't exist before
				{
					deleteResource(resource);	//delete the resource, as we weren't able to save its contents
				}
				throw ioException;	//rethrow the exception
			}
			if(exists)	//if the resource already existed
			{
				response.setStatus(HttpServletResponse.SC_NO_CONTENT);	//indicate success by showing that there is no content to return
			}
			else	//if the resource did not exist already
			{
				response.setStatus(HttpServletResponse.SC_CREATED);	//indicate that we created the resource
			}
		}
		else	//if this servlet is read-only
		{
			throw new HTTPForbiddenException();	//indicate that this method is forbidden
		}
  }

	/**Services the DELETE method.
  @param request The HTTP request.
  @param response The HTTP response.
  @exception ServletException if there is a problem servicing the request.
  @exception IOException if there is an error reading or writing data.
  */
	public void doDelete(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
	{
		if(!READ_ONLY)	//if this servlet is not read-only
		{
			final URI resourceURI=getResourceURI(request);	//get the URI of the requested resource
			final boolean exists=exists(resourceURI);	//see whether the resource already exists
			final R resource;	//we'll get the existing resource, if there is one 
			if(exists)	//if this resource exists
	    {
				resource=getResource(resourceURI);	//get the resource information
				deleteResource(resource);	//delete the resource
	    }
	    else	//if the resource does not exist
	    {
  			throw new HTTPNotFoundException(resourceURI.toString());	//show that we didn't find a resource for which to find properties					
	    }					
		}
		else	//if this servlet is read-only
		{
			throw new HTTPForbiddenException();	//indicate that this method is forbidden
		}
  }

	/**Services the COPY method.
  @param request The HTTP request.
  @param response The HTTP response.
  @exception ServletException if there is a problem servicing the request.
  @exception IOException if there is an error reading or writing data.
  */
	public void doCopy(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
	{
		if(!READ_ONLY)	//if this servlet is not read-only
		{
			final URI resourceURI=getResourceURI(request);	//get the URI of the requested resource
Debug.trace("moving; checking to see if resource exists", resourceURI);			
			if(exists(resourceURI))	//if this resource exists
	    {
Debug.trace("resource exists; getting resource");			
				final R resource=getResource(resourceURI);	//get the resource information
Debug.trace("getting destination");			
				final URI requestedDestinationURI=getDestination(request);	//get the destination URI for the operation
				if(requestedDestinationURI!=null)	//if a destination was given
				{
Debug.trace("requested destination", requestedDestinationURI);
						//get the canonical destination URI
					final URI destinationURI=getResourceURI(requestedDestinationURI, request.getMethod(), resourceURI);
					final boolean destinationExists=exists(destinationURI);	//see whether the destination resource already exists
	Debug.trace("destination exists?", destinationExists);			
					final int depth=getDepth(request);	//determine the requested depth
Debug.trace("depth requested:", depth);
					final boolean overwrite=isOverwrite(request);	//see if we should overwrite an existing destination resource
	Debug.trace("is overwrite?", overwrite);			
					copyResource(resource, destinationURI, depth, overwrite);	//copy the resource to its new location
					if(destinationExists)	//if the destination resource already existed
					{
						response.setStatus(HttpServletResponse.SC_NO_CONTENT);	//indicate success by showing that there is no content to return
					}
					else	//if the destination resource did not exist already
					{
						response.setStatus(HttpServletResponse.SC_CREATED);	//indicate that we created the resource
					}
				}
				else	//if there is no destination header
				{
					throw new HTTPBadRequestException("Missing Destination header.");
				}
	    }
			else	//if the resource doesn't exist
			{
				throw new HTTPNotFoundException();	//we can't move what's not there
			}
		}
		else	//if this servlet is read-only
		{
			throw new HTTPForbiddenException();	//indicate that this method is forbidden
		}
  }

	/**Services the MOVE method.
  @param request The HTTP request.
  @param response The HTTP response.
  @exception ServletException if there is a problem servicing the request.
  @exception IOException if there is an error reading or writing data.
  */
	public void doMove(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
	{
		if(!READ_ONLY)	//if this servlet is not read-only
		{
			final URI resourceURI=getResourceURI(request);	//get the URI of the requested resource
Debug.trace("moving; checking to see if resource exists", resourceURI);			
			if(exists(resourceURI))	//if this resource exists
	    {
Debug.trace("resource exists; getting resource");			
				final R resource=getResource(resourceURI);	//get the resource information
Debug.trace("getting destination");			
				final URI requestedDestinationURI=getDestination(request);	//get the destination URI for the operation
				if(requestedDestinationURI!=null)	//if a destination was given
				{
Debug.trace("requested destination", requestedDestinationURI);
						//get the canonical destination URI
					final URI destinationURI=getResourceURI(requestedDestinationURI, request.getMethod(), resourceURI);
					final boolean destinationExists=exists(destinationURI);	//see whether the destination resource already exists
	Debug.trace("destination exists?", destinationExists);			
					final boolean overwrite=isOverwrite(request);	//see if we should overwrite an existing destination resource
	Debug.trace("is overwrite?", overwrite);			
					moveResource(resource, destinationURI, overwrite);	//move the resource to its new location
					if(destinationExists)	//if the destination resource already existed
					{
						response.setStatus(HttpServletResponse.SC_NO_CONTENT);	//indicate success by showing that there is no content to return
					}
					else	//if the destination resource did not exist already
					{
						response.setStatus(HttpServletResponse.SC_CREATED);	//indicate that we created the resource
					}
				}
				else	//if there is no destination header
				{
					throw new HTTPBadRequestException("Missing Destination header.");
				}
	    }
			else	//if the resource doesn't exist
			{
				throw new HTTPNotFoundException();	//we can't move what's not there
			}
		}
		else	//if this servlet is read-only
		{
			throw new HTTPForbiddenException();	//indicate that this method is forbidden
		}
  }

	/**Services the MkCol method.
  @param request The HTTP request.
  @param response The HTTP response.
  @exception ServletException if there is a problem servicing the request.
  @exception IOException if there is an error reading or writing data.
  */
	public void doMkCol(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
	{
		if(!READ_ONLY)	//if this servlet is not read-only
		{
			final URI resourceURI=getResourceURI(request);	//get the URI of the requested resource
			final boolean exists=exists(resourceURI);	//see whether the resource already exists
			final R resource;	//we'll get the existing resource, if there is one
			if(!exists(resourceURI))	//if the resource doesn't exist
			{
				try
				{
					resource=createCollection(resourceURI);	//create the resource
				}
				catch(final IllegalArgumentException illegalArgumentException)	//if this is an invalid resource URI
				{
					throw new HTTPForbiddenException(illegalArgumentException);	//forbid creation of resources with invalid URIs
				}
			}
			else	//if the resource doesn't exist
			{
				throw new HTTPMethodNotAllowedException(getAllowedMethods(resourceURI));	//report that we don't allow creating collections that already exist, indicating the methods we do allow for this resource				
			}
		}
		else	//if this servlet is read-only
		{
			throw new HTTPForbiddenException();	//indicate that this method is forbidden
		}
  }

	/**Services the PROPFIND method.
  @param request The HTTP request.
  @param response The HTTP response.
  @exception ServletException if there is a problem servicing the request.
  @exception IOException if there is an error reading or writing data.
  */
	protected void doPropFind(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
	{
		final URI resourceURI=getResourceURI(request);	//get the URI of the requested resource
Debug.trace("doing propfind for URI", resourceURI);
		if(LIST_DIRECTORIES)	//if we allow directory listing
		{
			if(exists(resourceURI))	//if the resource exists
			{
				final int depth=getDepth(request);	//determine the requested depth
Debug.trace("depth requested:", depth);
				IDMappedList<URI, QualifiedName> propertyList=ALL_PROPERTIES;	//default to listing all properties
				try
				{
					final Document document=getXML(request);	//get the XML from the request body
					if(document!=null)	//if there was an XML document in the request
					{
	Debug.trace("Found XML request content:", XMLUtilities.toString(document));
						final Element documentElement=document.getDocumentElement();	//get the document element
							//TODO check to make sure the document element is correct
						propertyList=getProperties(documentElement);	//get the property list from the XML document
					}
				}
				catch(final DOMException domException)	//any XML problem here is the client's fault
				{
		      throw new HTTPBadRequestException(domException);	//show that the XML wasn't correct				
				}
				try
				{
					final List<R> resourceList=getResources(resourceURI, depth);	//get a list of resources
					final Document multistatusDocument=createMultistatusDocument();	//create a multistatus document
					for(final R resource:resourceList)	//for each resource
					{
						final Element responseElement=addResponse(multistatusDocument.getDocumentElement());	//add a response
						addHref(responseElement, resource.getReferenceURI());	//show this resource's URI
						final Element propstatElement=addPropstat(responseElement);	//add a property container
						final Element propElement=addProp(propstatElement);	//add a property element
						findProperties(resource, propElement, propertyList);	//find the properties for this resource
						addStatus(propstatElement, "HTTP/1.1 200 OK");	//TODO use a real status here; use constants
						//TODO add a response description here
					}
					response.setStatus(SC_MULTI_STATUS);	//show that we will be sending back multistatus content
Debug.trace("Ready to send back XML:", XMLUtilities.toString(multistatusDocument));
					setXML(response, multistatusDocument);	//put the XML in our response and send it back
				}
				catch(final DOMException domException)	//any XML problem here is the server's fault
				{
					Debug.error(domException);	//report the error
					throw new HTTPInternalServerErrorException(domException);	//show that the XML wasn't correct
				}
	    }
	    else	//if the resource does not exist
	    {
  			throw new HTTPNotFoundException(resourceURI.toString());	//show that we didn't find a resource for which to find properties					
	    }					
		}
		else	//if directory listing is not allowed
		{
			throw new HTTPMethodNotAllowedException(getAllowedMethods(resourceURI));	//report that we don't allow this method, indicating the methods we do allow for this resource
		}
  }

	/**Services the GET method.
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
    			throw new HTTPNotFoundException(resourceURI.toString());	//show that we didn't find a resource to return				
    		}    		
    	}
    	else	//if this resource is not a collection
	    	{
Debug.trace("ready to send back a file");
    		final ContentType contentType=getContentType(resource);	//get the content type of the resource
      	if(contentType!=null)	//if we know the content type
	      	{
Debug.trace("setting content type to:", contentType);
      		response.setContentType(contentType.toString());	//tell the response which content type we're serving
      	}
      	final long contentLength=getContentLength(resource);	//get the content length of the resource
      	if(contentLength>=0)	//if we found a content length for the resource
	      {
Debug.trace("setting content length to:", contentLength);
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
			throw new HTTPNotFoundException(resourceURI.toString());	//show that we didn't find a resource for which to find properties					
    }
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
		final URI requestedResourceURI=super.getResourceURI(request);	//get the default resource URI for this request
		final URI resourceURI=getResourceURI(requestedResourceURI, request.getMethod(), null);	//get the correct URI for the resource
		if(!resourceURI.equals(requestedResourceURI))	//if the real resource URI is different from the one requested
		{
			if(isRedirectSupported(request))	//if redirection is supported by the user agent sending the request
			{
				throw new HTTPMovedPermanentlyException(resourceURI);	//report back that this resource has permanently moved to its correct location URI
			}
		}
		return resourceURI;	//return the resource URI
	}

	/**Determines the URI of a requested resource, using an optional resource
	 	as an analogy.
	This method determines if a non-collection resource (i.e. one not ending in '/')
	should represent a collection if one of the following conditions apply:
	<ul>
		<li>The given method is a collection-specific method.</li>
		<li>The requested non-collection URI does not exist, but there exists a collection
			at the location of the URI with an appended '/'.</li>
		<li>The analogous resource, if present, is a collection.</li>
	</ul>
  @param requestedResourceURI The requested absolute URI of the resource.
  @param method The HTTP request method.
  @param analogousResourceURI The URI of a resource to use by analogy, or <code>null</code> if
  	no analogous resource is known. This parameter is useful when used with the COPY or MOVE method.
  @return The canonical URI of the requested resource, which may be different
  	than the requested resource URI.
  */
	protected URI getResourceURI(final URI requestedResourceURI, final String method, final URI analogousResourceURI)
	{
		URI resourceURI=requestedResourceURI;	//start off assuming we'll use the requested URI
Debug.trace("requested URI", requestedResourceURI);
//G***del Debug.trace("ends with slash?", endsWith(requestURIString, PATH_SEPARATOR));
//G***del Debug.trace("exists?", exists(requestURI));
		final String requestResourceURIString=requestedResourceURI.toString();	//get the string version of the request URI
		if(!endsWith(requestResourceURIString, PATH_SEPARATOR))	//if the URI is not a collection URI
		{
			final URI collectionURI=URI.create(requestResourceURIString+PATH_SEPARATOR);	//add a trailing slash to get a collection URI
				//if this is a method referring to a collection
			if(MKCOL_METHOD.equals(method))
			{
				resourceURI=collectionURI;	//use the collection URI
			}
			else if(analogousResourceURI!=null)	//if there is an analogous resource
			{
					//if the analogous resource ends with '/'
				if(endsWith(analogousResourceURI.toString(), PATH_SEPARATOR))
				{
					resourceURI=collectionURI;	//use the collection URI					
				}
			}
			else	//if there is no analogous resource (don't do the liberal non-existence check if there is an analogous resource, because this could prevent MOVE or COPY from a non-collection to another non-collection when a similarly-named collection exists)
			{
Debug.trace("requested resource exists:", exists(requestedResourceURI));
Debug.trace("other resource is collection:", isCollection(collectionURI));
				//if there is no such resource but there is a resource at the collection URI
				if(!exists(requestedResourceURI) && isCollection(collectionURI))
				{
					resourceURI=collectionURI;	//use the collection URI				
				}				
			}
		}
Debug.trace("using URI", resourceURI);
		return resourceURI;	//return the resource URI we decided on
	}

	/**Determines the URI of a requested resource from its requested URI.
	<p>If it is determined that the requested resource is located in another location,
	the new URI will be returned.</p>
  @param requestURI The absolute URI of the requested resource.
  @return The URI of the requested resource, which may be different from the URI
  	specified in the request.
  @exception IllegalArgumentException if the given URI is not absolute with an absolute path.
  @see HttpServletRequest#getRequestURL()
  */
/*G***fix
	protected URI getResourceURI(final URI requestURI)
	{
Debug.trace("request URI", requestURI);
//G***del Debug.trace("ends with slash?", endsWith(requestURIString, PATH_SEPARATOR));
//G***del Debug.trace("exists?", exists(requestURI));
		final String requestURIString=requestURI.toString();	//get the string version of the request URI
		if(!endsWith(requestURIString, PATH_SEPARATOR))	//if the URI is not a collection URI
		{
			final URI redirectURI=URI.create(requestURIString+PATH_SEPARATOR);	//add a trailing slash to get a collection URI
			final boolean isCollectionMethod=MKCOL_METHOD.equals(request.getMethod());	//see if this is a method referring to a collection
			final boolean redirect;	//determine if we need to redirect
			if(isCollectionMethod)	//if this is a collection-specific method
			{
				redirect=true;	//redirect to the real collection URI
			}
			else	//if this is not a collection-specific method
			{
				redirect=!exists(requestURI) && isCollection(redirectURI);	//redirect if there is no such file but redirecting would take the client to a collection
			}
			if(redirect)	//if we should redirect
			{
Debug.trace("sending redirect", redirectURI);
				if(isRedirectSupported(request))	//if redirection is supported by the user agent sending the request
				{
					throw new HTTPMovedPermanentlyException(redirectURI);	//report back that this resource has permanently moved to its correct location URI
				}
				else	//if we can't redirect
				{
					return redirectURI;	//we'll just pretend they requested the correct URI					
				}
			}
		}
		return requestURI;	//return the requested URI
	}
*/

	/**Determines the requested depth.
  @param request The HTTP request.
  @return <code>0</code>, <code>1</code>, or <code>-1</code> if an infinite depth is indicated.
  */
	protected int getDepth(final HttpServletRequest request)
	{
		final String depthString=request.getHeader(DEPTH_HEADER);	//get the depth header
		if(depthString==null)	//no depth specified
		{
			return -1;	//default to infinity
		}
		else if(DEPTH_0.equals(depthString))	//0
		{
			return 0;
		}
		else if(DEPTH_1.equals(depthString))	//1
		{
			return 1;
		}
		else if(DEPTH_INFINITY.equals(depthString))	//infinity
		{
			return -1;
		}
		else	//unrecognized depth (technically an error)
		{
			return -1;	//default to infinity
		}
	}
	
	/**Retrieves an XML document from the body of an HTTP request.
	@param request The request from which to get the XML document.
	@return A document representing the XML information, or <code>null</code>
		if nothing but whitespace was included in the request.
	@exception IOException if there is an error reading the XML.
	*/
	protected Document getXML(final HttpServletRequest request) throws IOException
	{
Debug.trace("getting XML");
		final int contentLength=request.getContentLength();	//get the length of the request
Debug.trace("content length", contentLength);
		assert contentLength>=0 : "Missing content length";
		if(contentLength>0)	//if content is present
		{
			final InputStream inputStream=request.getInputStream();	//get an input stream to the request content
			final byte[] content=InputStreamUtilities.getBytes(inputStream, contentLength);	//read the request TODO check for the content being shorter than expected
			boolean hasContent=false;	//we'll start out assuming there actually is no content
			for(final byte b:content)	//look at each byte in the content
			{
				if(!CharacterUtilities.isWhitespace((char)b))	//if this byte doesn't represent whitespace (ignoring the encoding is fine, because another encoding would require content, so if we find non-whitespace it means there is some content)
				{
					hasContent=true;	//we have content
					break;	//stop looking for content
				}
			}
			if(hasContent)	//if we have content
			{
				final InputStream xmlInputStream=new ByteArrayInputStream(content);	//create a new input stream from the bytes we read
				final XMLProcessor xmlProcessor=new XMLProcessor();	//create a new XML processor to process the information TODO use a generic way of getting the XML processor
				return xmlProcessor.parseDocument(xmlInputStream, null);	//parse the document				
			}
		}
		else if(contentLength<0)	//if no content length was given
		{
			throw new HTTPLengthRequiredException();	//indicate that we require a content length
		}
		return null;	//show that there is no content to return
	}

	/**Places an XML document into the body of an HTTP response.
	@param response The response into which to place the XML document.
	@param document The XML document to place into the response.
	@exception IOException if there is an error writing the XML.
	*/
	protected void setXML(final HttpServletResponse response, final Document document) throws IOException
	{
		final ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();	//create a byte array output stream to hold our outgoing data
		new XMLSerializer(true).serialize(document, byteArrayOutputStream, new CharacterEncoding(UTF_8, null, NO_BOM));	//serialize the document to the byte array with no byte order mark
		final byte[] bytes=byteArrayOutputStream.toByteArray();	//get the bytes we serialized
			//set the content type to text/xml; charset=UTF-8
		response.setContentType(ContentTypeUtilities.toString(TEXT, XML_SUBTYPE, new NameValuePair<String, String>(CHARSET_PARAMETER, UTF_8)));
		response.setContentLength(bytes.length);	//tell the response how many bytes to expect
		final OutputStream outputStream=response.getOutputStream();	//get an output stream to the response
		final InputStream inputStream=new ByteArrayInputStream(bytes);	//get an input stream to the bytes
		try
		{
			OutputStreamUtilities.write(inputStream, outputStream);	//write the bytes to the response
		}
		catch(final IOException ioException)	//if anything goes wrong
		{
			inputStream.close();	//always close our input stream
		}
	}

	/**@return A new XML document representing multistatus.
	@exception DOMException if there is an error creating the document.
	*/
	protected Document createMultistatusDocument() throws DOMException
	{
		return getDOMImplementation().createDocument(WEBDAV_NAMESPACE, createQualifiedName(WEBDAV_NAMESPACE_PREFIX, ELEMENT_MULTISTATUS), null);	//create a multistatus document
	}

	/**Creates a response and appends it to the given element.
	@param element An XML element representing a multistatus.
	@return A WebDAV response XML element.
	@exception DOMException if there is an error creating the element.
	*/
	protected Element addResponse(final Element element) throws DOMException
	{
		return appendElement(element, WEBDAV_NAMESPACE, createQualifiedName(WEBDAV_NAMESPACE_PREFIX, ELEMENT_RESPONSE));	//create a response element
	}

	/**Creates an href element with the URI as its content and adds it to the given element.
	@param element An XML element representing, for example, a response.
	@param uri The URI to use as the href.
	@return A WebDAV href XML element with the URI as its content.
	@exception DOMException if there is an error creating the element.
	*/
	protected Element addHref(final Element element, final URI uri) throws DOMException
	{
			//create an href element with the URI as its content and append it to the given element
		return appendElement(element, WEBDAV_NAMESPACE, createQualifiedName(WEBDAV_NAMESPACE_PREFIX, ELEMENT_HREF), uri.toString());
	}

	/**Creates a property container element.
	@param element An XML element representing, for example, a response.
	@return A WebDAV propstat XML element.
	@exception DOMException if there is an error creating the element.
	*/
	protected Element addPropstat(final Element element) throws DOMException
	{
			//create a propstat element and append it to the given element
		return appendElement(element, WEBDAV_NAMESPACE, createQualifiedName(WEBDAV_NAMESPACE_PREFIX, ELEMENT_PROPSTAT));
	}

	/**Creates a property element.
	@param element An XML element representing, for example, a property container.
	@return A WebDAV property XML element.
	@exception DOMException if there is an error creating the element.
	*/
	protected Element addProp(final Element element) throws DOMException
	{
			//create a property element and append it to the given element
		return appendElement(element, WEBDAV_NAMESPACE, createQualifiedName(WEBDAV_NAMESPACE_PREFIX, ELEMENT_PROP));
	}

	/**Creates a status element with the status text as its content and adds it to the given element.
	@param element An XML element representing, for example, a propstat.
	@param status The text of the status report.
	@return A WebDAV status XML element with the status text as its content.
	@exception DOMException if there is an error creating the element.
	*/
	protected Element addStatus(final Element element, final String status) throws DOMException
	{
			//create a status element with the status text as its content and append it to the given element
		return appendElement(element, WEBDAV_NAMESPACE, createQualifiedName(WEBDAV_NAMESPACE_PREFIX, ELEMENT_STATUS), status);
	}

	/**Creates and adds a resource type element with a resource type child element.
	Properties in the WebDAV namespace will have the correct prefix determined.
	@param element An XML element representing, for example, a property.
	@param typeNamespaceURI The namespace URI of the property,
		or <code>null</code> to indicate no type. 
	@param typeLocalName The local name of the property,
		or <code>null</code> to indicate no type. 
	@return A WebDAV resource type XML element with the optional type indicated by a child element.
	@exception DOMException if there is an error creating the elements.
	*/
	protected Element addResourceType(final Element element, final String typeNamespaceURI, final String typeLocalName) throws DOMException
	{
			//create and append a resource type element
		final Element resourceTypeElement=appendElement(element, WEBDAV_NAMESPACE, createQualifiedName(WEBDAV_NAMESPACE_PREFIX, RESOURCE_TYPE_PROPERTY_NAME));
		if(typeNamespaceURI!=null && typeLocalName!=null)	//if a type was given
		{
			final String prefix=WEBDAV_NAMESPACE.equals(typeNamespaceURI) ? WEBDAV_NAMESPACE_PREFIX : null;	//see if we should use the WebDAV previx TODO look up other names from the XMLSerializer namespace prefix map
			appendElement(resourceTypeElement, typeNamespaceURI, createQualifiedName(prefix, typeLocalName));
		}
		return resourceTypeElement;	//return the element we created
	}

	/**Retrieves a list of properties parsed from the children of the given XML element.
	The <code>allprop</code> and <code>propname</code> conditions are supported.
	@param element The XML element parent of the property list.
	@return A list of all requested properties, or <code>ALL_PROPERTIES</code> or
		<code>PROPERTY_NAMES</code> indicating all properties or all property names,
		respectively.
	@see #ALL_PROPERTIES
	@see #PROPERTY_NAMES
	*/
	protected IDMappedList<URI, QualifiedName> getProperties(final Element element)
	{
		final IDMappedList<URI, QualifiedName> propertyList=new IDMappedList(new HashMap<URI, QualifiedName>(), new ArrayList<QualifiedName>());	//create a list of qualified names
		final NodeList childList=element.getChildNodes();	//get a list of element children
		for(int childIndex=0; childIndex<childList.getLength(); ++childIndex)	//look at each child node
		{
			final Node childNode=childList.item(childIndex);	//get this child node
			if(childNode.getNodeType()==Node.ELEMENT_NODE)	//if this is an element
			{
//G***del				final Element childElement=(Element)childNode;	//get a reference to this element
				if(WEBDAV_NAMESPACE.equals(childNode.getNamespaceURI()))	//if this is a WebDAV element
				{
					final String childLocalName=childNode.getLocalName();	//get the child element's local name
					if(ELEMENT_PROP.equals(childLocalName))	//allprop
					{
						final NodeList propertyChildList=childNode.getChildNodes();	//get a list of property element children
						for(int propertyChildIndex=0; propertyChildIndex<propertyChildList.getLength(); ++propertyChildIndex)	//look at each property child node
						{
							final Node propertyChildNode=propertyChildList.item(propertyChildIndex);	//get this property child node
							if(propertyChildNode.getNodeType()==Node.ELEMENT_NODE)	//if this is an element
							{
//G***del								final Element propertyChildElement=(Element)propertyChildNode;	//get a reference to this element
									//create a qualified name for this property
								final QualifiedName qname=new QualifiedName(propertyChildNode.getNamespaceURI(), propertyChildNode.getPrefix(), propertyChildNode.getLocalName());
								propertyList.add(qname);	//add the property to the list
							}
						}
					}
					else if(ELEMENT_ALLPROP.equals(childLocalName))	//allprop
					{
						return ALL_PROPERTIES;	//show that all properties were requested
					}
					else if(PROPERTY_NAMES.equals(childLocalName))	//propname
					{
						return PROPERTY_NAMES;	//show that properties names were requested
					}
				}
			}
		}
		return propertyList;	//return our list of properties
	}

  /**Determines the WebDAV methods allowed for the requested resource.
  @param resourceURI The URI of a resource for which options should be obtained.
  @return A set of methods allowed for this resource.
  */
	protected EnumSet<WebDAVMethod> getAllowedMethods(final URI resourceURI)	//TODO we probably can't keep using generics---when we implement DeltaV, there will probably be more methods, and other servlets may allow custom methods
	{
		final EnumSet<WebDAVMethod> methodSet=EnumSet.of(OPTIONS);	//we always allow options 
		if(exists(resourceURI))	//if the resource exists
		{
			methodSet.add(COPY);
			methodSet.add(GET);
			methodSet.add(DELETE);
			methodSet.add(HEAD);
//  	TODO implement  		methodSet.add(WebDAVMethod.LOCK);
			methodSet.add(MOVE);
			methodSet.add(POST);
			if(LIST_DIRECTORIES)	//if we allow directory listings
			{
//  		TODO implement  			methodSet.add(PROPFIND);
			}
//  	TODO implement  		methodSet.add(PROPPATCH);
//  	TODO implement  		methodSet.add(TRACE);
//  	TODO implement  		methodSet.add(UNLOCK);
			if(!isCollection(resourceURI))	//if the resource is not a collection
			{
				methodSet.add(PUT);	//allow saving a resource to this location  			
			}
		}
		else	//if the resource does not exist
		{
//  	TODO implement  		methodSet.add(LOCK);
			methodSet.add(MKCOL);
			methodSet.add(PUT);  		
		}
		return methodSet;	//return the allowed methods
	}

	/**Checks whether the given principal is authorized to invoke the given method on the given resource.
	This version performs no specific checks, but recognizes the COPY and MOVE WebDAV methods and
		correctly calls this method on their destination URIs. For this reason, any child class must
		call this method.
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
		boolean isAuthorized=super.isAuthorized(request, resourceURI, method, principal, realm);	//see if this principal passes the default authorization checks
		if(isAuthorized)	//if this principal passes the default authorization checks
		{
			if(COPY_METHOD.equals(method) || MOVE_METHOD.equals(method))	//if this is COPY or MOVE
			{
				final URI requestedDestinationURI=getDestination(request);	//get the destination URI for the operation
				if(requestedDestinationURI!=null)	//if a destination was given (ignore missing destinations---a principal is authorized to copy or move a resource to nowhere)
				{
Debug.trace("checking authorization for requested destination", requestedDestinationURI);
						//get the canonical destination URI
					final URI destinationURI=getResourceURI(requestedDestinationURI, request.getMethod(), resourceURI);
						//for COPY and MOVE, make sure the principal is authorized to do a PUT on the destination
					isAuthorized=isAuthorized(request, destinationURI, PUT_METHOD, principal, realm);
				}
			}
		}
		return isAuthorized;	//return whether the principal is authorized
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
  	but not necessarily the contents of the resource. 
	@exception IllegalArgumentException if the given resource URI does not represent a valid resource.
  */
	protected abstract R getResource(final URI resourceURI) throws IllegalArgumentException;

	/**Retrieves a list of resources and child resources to the given depth.
	@param resourceURI The URI of the requested resource.
  @param depth The zero-based depth of child resources to retrieve, or
  	<code>-1</code> if all progeny should be included.
  @return A list of resources and optionally children as specified..  
	@exception IllegalArgumentException if the given resource URI does not represent a valid resource.
  */
	protected abstract List<R> getResources(final URI resourceURI, final int depth) throws IllegalArgumentException;

	/**Copies all the requested resource properties to the given property XML element.
	@param resource The resource the properties of which should be found.
	@param propertyElement The XML element which will receive a representation of the resource properties.
	@param properties A list of all requested properties, or <code>ALL_PROPERTIES</code> or
		<code>PROPERTY_NAMES</code> indicating all properties or all property names,
		respectively.
	@see #ALL_PROPERTIES
	@see #PROPERTY_NAMES
	@exception DOMException if there is an error updating the properties element.
	*/
	protected abstract void findProperties(final R resource, final Element propertyElement, final IDMappedList<URI, QualifiedName> properties) throws DOMException;

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

	/**Retrieves an output stream to the given resource.
	@param resource The resource for which an output stream should be retrieved.
	@return An output stream to the given resource.
	@exception IOException Thrown if there is an error accessing the resource.
	*/
	protected abstract OutputStream getOutputStream(final R resource) throws IOException;	//G***do we want to pass the resource or just the URI here?

	/**Creates a resource.
	For collections, <code>createCollection</code> should be used instead.
	@param resourceURI The URI of the resource to create.
	@return The description of a newly created resource, or <code>null</code> if
		the resource is not allowed to be created.
	@exception IllegalArgumentException if the given resource URI does not represent a valid resource in a valid burrow.
	@exception IOException Thrown if there is an error creating the resource.
	@exception HTTPConflictException if an intermediate collection required for creating this collection does not exist.
	@see #createCollection(URI)
	*/
	protected abstract R createResource(final URI resourceURI) throws IllegalArgumentException, IOException, HTTPConflictException;

	/**Creates a collection resource.
	@param resourceURI The URI of the resource to create.
	@return The description of a newly created resource, or <code>null</code> if
		the resource is not allowed to be created.
	@exception IllegalArgumentException if the given resource URI does not represent a valid resource in a valid burrow.
	@exception IOException Thrown if there is an error creating the resource.
	@exception HTTPConflictException if an intermediate collection required for creating this collection does not exist.
	@see #createResource(URI)
	*/
	protected abstract R createCollection(final URI resourceURI) throws IllegalArgumentException, IOException, HTTPConflictException;

	/**Deletes a resource.
	@param resource The resource to delete.
	@exception IOException Thrown if the resource could not be deleted.
	*/
	protected abstract void deleteResource(final R resource) throws IOException;

	/**Copies a resource.
	@param resource The resource to copy.
	@param destinationURI The destination URI to which the resource should be copied.
	@param depth The zero-based depth of child resources which should
		recursively be copied, or <code>-1</code> for an infinite depth.
	@param overwrite <code>true</code> if any existing resource at the destination should be overwritten, else <code>false</code>.
	@exception IllegalArgumentException if the given resource URI does not represent a valid resource in a valid burrow.
	@exception IOException Thrown if there is an error copying the resource.
	@exception HTTPConflictException if an intermediate collection required for creating this collection does not exist.
	@exception HTTPPreconditionFailedException if a resource already exists at the destination and <var>overwrite</var> is <code>false</code>.
	*/
	protected abstract void copyResource(final R resource, final URI destinationURI, final int depth, final boolean overwrite) throws IllegalArgumentException, IOException, HTTPConflictException, HTTPPreconditionFailedException;

	/**Moves a resource.
	@param resource The resource to move.
	@param destinationURI The destination URI to which the resource should be moved.
	@param overwrite <code>true</code> if any existing resource at the destination should be overwritten, else <code>false</code>.
	@exception IllegalArgumentException if the given resource URI does not represent a valid resource in a valid burrow.
	@exception IOException Thrown if there is an error moving the resource.
	@exception HTTPConflictException if an intermediate collection required for creating this collection does not exist.
	@exception HTTPPreconditionFailedException if a resource already exists at the destination and <var>overwrite</var> is <code>false</code>.
	*/
	protected abstract void moveResource(final R resource, final URI destinationURI, final boolean overwrite) throws IllegalArgumentException, IOException, HTTPConflictException, HTTPPreconditionFailedException;

	/**Retrieves an list of child resources of the given resource.
	@param resource The resource for which children should be returned.
	@return A list of child resources.
	@exception IOException Thrown if there is an error retrieving the list of child resources.
	*/
	protected abstract List<R> getChildResources(final R resource) throws IOException;	//G***do we want to pass the resource or just the URI here?

}
