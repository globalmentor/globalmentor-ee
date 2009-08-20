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

package com.globalmentor.net.http.webdav;

import java.io.*;
import java.net.*;
import java.security.Principal;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import com.globalmentor.collections.DecoratorIDedMappedList;
import com.globalmentor.net.Resource;
import com.globalmentor.net.http.*;
import static com.globalmentor.net.http.HTTP.*;
import static com.globalmentor.net.http.HTTPServlets.*;
import static com.globalmentor.net.http.webdav.WebDAV.*;
import com.globalmentor.text.xml.XML;
import com.globalmentor.util.*;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

/**The base servlet class for implementing a WebDAV server as defined by
<a href="http://www.ietf.org/rfc/rfc2518.txt">RFC 2518</a>,	"HTTP Extensions for Distributed Authoring -- WEBDAV". 
@author Garret Wilson
*/
public abstract class AbstractWebDAVServlet<R extends Resource> extends AbstractHTTPServlet<R>	//TODO address http://lists.w3.org/Archives/Public/w3c-dist-auth/1999OctDec/0343.html
{
		
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
		super.doOptions(request, response);	//do the default servicing of options
		response.setHeader(DAV_HEADER, "1,2");	//we support WebDAV levels 1 and 2
		response.addHeader(MS_AUTHOR_VIA_HEADER, MS_AUTHOR_VIA_DAV);	//tell Microsoft editing tools to use WebDAV rather than FrontPage
	}

	/**Services the COPY method.
  @param request The HTTP request.
  @param response The HTTP response.
  @exception ServletException if there is a problem servicing the request.
  @exception IOException if there is an error reading or writing data.
  */
	public void doCopy(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
	{
		final URI resourceURI=getResourceURI(request);	//get the URI of the requested resource
Debug.trace("moving; checking to see if resource exists", resourceURI);			
		if(exists(request, resourceURI))	//if this resource exists
	    {
Debug.trace("resource exists; getting resource");			
			final R resource=getResource(request, resourceURI);	//get the resource information
Debug.trace("getting destination");			
			final URI requestedDestinationURI=getDestination(request);	//get the destination URI for the operation
			if(requestedDestinationURI!=null)	//if a destination was given
				{
Debug.trace("requested destination", requestedDestinationURI);
					//get the canonical destination URI
				final URI destinationURI=getResourceURI(request, requestedDestinationURI, request.getMethod(), resourceURI);
				final boolean destinationExists=exists(request, destinationURI);	//see whether the destination resource already exists
Debug.trace("destination exists?", destinationExists);			
				final Depth depth=getDepth(request);	//determine the requested depth
Debug.trace("depth requested:", depth);
				final boolean overwrite=isOverwrite(request);	//see if we should overwrite an existing destination resource
Debug.trace("is overwrite?", overwrite);			
				copyResource(request, resource, destinationURI, depth==Depth.INFINITY ? -1 : depth.ordinal(), overwrite);	//copy the resource to its new location
				if(destinationExists)	//if the destination resource already existed
				{
					response.setStatus(HttpServletResponse.SC_NO_CONTENT);	//indicate success by showing that there is no content to return
					response.setContentLength(0);	//TODO check; this seems to be needed---should we throw an HTTPException or set the response instead?
				}
				else	//if the destination resource did not exist already
				{
					response.setStatus(HttpServletResponse.SC_CREATED);	//indicate that we created the resource
					response.setContentLength(0);	//TODO check; this seems to be needed---should we throw an HTTPException or set the response instead?
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

	/**Services the MOVE method.
  @param request The HTTP request.
  @param response The HTTP response.
  @exception ServletException if there is a problem servicing the request.
  @exception IOException if there is an error reading or writing data.
  */
	public void doMove(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
	{
		final URI resourceURI=getResourceURI(request);	//get the URI of the requested resource
Debug.trace("moving; checking to see if resource exists", resourceURI);			
		if(exists(request, resourceURI))	//if this resource exists
	    {
Debug.trace("resource exists; getting resource");			
			final R resource=getResource(request, resourceURI);	//get the resource information
Debug.trace("getting destination");			
			final URI requestedDestinationURI=getDestination(request);	//get the destination URI for the operation
			if(requestedDestinationURI!=null)	//if a destination was given
				{
Debug.trace("requested destination", requestedDestinationURI);
					//get the canonical destination URI
				final URI destinationURI=getResourceURI(request, requestedDestinationURI, request.getMethod(), resourceURI);
				final boolean destinationExists=exists(request, destinationURI);	//see whether the destination resource already exists
Debug.trace("destination exists?", destinationExists);			
				final boolean overwrite=isOverwrite(request);	//see if we should overwrite an existing destination resource
Debug.trace("is overwrite?", overwrite);			
				moveResource(request, resource, destinationURI, overwrite);	//move the resource to its new location
				if(destinationExists)	//if the destination resource already existed
				{
					response.setStatus(HttpServletResponse.SC_NO_CONTENT);	//indicate success by showing that there is no content to return
					response.setContentLength(0);	//TODO check; this seems to be needed---should we throw an HTTPException or set the response instead?
				}
				else	//if the destination resource did not exist already
				{
					response.setStatus(HttpServletResponse.SC_CREATED);	//indicate that we created the resource
					response.setContentLength(0);	//TODO check; this seems to be needed---should we throw an HTTPException or set the response instead?
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

	/**Services the MkCol method.
  @param request The HTTP request.
  @param response The HTTP response.
  @exception ServletException if there is a problem servicing the request.
  @exception IOException if there is an error reading or writing data.
  */
	public void doMkCol(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
	{
		final URI resourceURI=getResourceURI(request);	//get the URI of the requested resource
		final boolean exists=exists(request, resourceURI);	//see whether the resource already exists
		final R resource;	//we'll get the existing resource, if there is one
		if(!exists)	//if the resource doesn't exist
		{
			try
			{
				resource=createCollection(request, resourceURI);	//create the resource
			}
			catch(final IllegalArgumentException illegalArgumentException)	//if this is an invalid resource URI
			{
				throw new HTTPForbiddenException(illegalArgumentException);	//forbid creation of resources with invalid URIs
			}
		}
		else	//if the resource doesn't exist
		{
			throw new HTTPMethodNotAllowedException(getAllowedMethods(request, resourceURI));	//report that we don't allow creating collections that already exist, indicating the methods we do allow for this resource				
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
		if(true/*TODO fix---is this valid for WebDAV? LIST_DIRECTORIES*/)	//if we allow directory listing
		{
			if(exists(request, resourceURI))	//if the resource exists
			{
				final Depth depth=getDepth(request);	//determine the requested depth
				DecoratorIDedMappedList<URI, WebDAVPropertyName> propertyList=ALL_PROPERTIES;	//default to listing all properties
				final WebDAVXMLGenerator webdavXMLGenerator=new WebDAVXMLGenerator();	//create a WebDAV XML generator
				try
				{
					final Document document=getXML(request, webdavXMLGenerator.getDocumentBuilder());	//get the XML from the request body
					if(document!=null)	//if there was an XML document in the request
					{
						final Element documentElement=document.getDocumentElement();	//get the document element
							//TODO check to make sure the document element is correct
						propertyList=WebDAVXMLProcessor.getPropfindProperties(request, documentElement);	//get the property list from the XML document
					}
				}
				catch(final DOMException domException)	//any XML problem here is the client's fault
				{
		      throw new HTTPBadRequestException(domException);	//show that the XML wasn't correct				
				}
				catch(final SAXException saxException)	//any XML problem here is the client's fault
				{
		      throw new HTTPBadRequestException(saxException);	//show that the XML wasn't correct				
				}
				try
				{
					final List<R> resourceList=getResources(resourceURI, depth==Depth.INFINITY ? -1 : depth.ordinal());	//get a list of resources
					final Document multistatusDocument=webdavXMLGenerator.createMultistatusDocument();	//create a multistatus document
					for(final R resource:resourceList)	//for each resource
					{
						final Element responseElement=webdavXMLGenerator.addResponse(multistatusDocument.getDocumentElement());	//add a response
						webdavXMLGenerator.addHref(responseElement, resource.getURI());	//show this resource's URI
						final Element propstatElement=webdavXMLGenerator.addPropstat(responseElement);	//add a property container
						final Element propElement=webdavXMLGenerator.addProp(propstatElement);	//add a property element
						findProperties(request, resource, propElement, propertyList, webdavXMLGenerator);	//find the properties for this resource
						webdavXMLGenerator.addStatus(propstatElement, "HTTP/1.1 200 OK");	//TODO use a real status here; use constants
						//TODO add a response description here
					}
					response.setStatus(SC_MULTI_STATUS);	//show that we will be sending back multistatus content
Debug.trace("Ready to send back XML:", XML.toString(multistatusDocument));
					setXML(request, response, multistatusDocument);	//put the XML in our response and send it back, compressed if possible
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
			throw new HTTPMethodNotAllowedException(getAllowedMethods(request, resourceURI));	//report that we don't allow this method, indicating the methods we do allow for this resource
		}
  }

	/**Determines the requested depth.
  @param request The HTTP request.
  @return The depth or <code>Depth.INFINITY</code> if an infinite, undefined, or or unrecognized depth is indicated.
  */
	protected Depth getDepth(final HttpServletRequest request)
	{
		final String depthString=request.getHeader(DEPTH_HEADER);	//get the depth header
		if(depthString==null)	//no depth specified
		{
			return Depth.INFINITY;	//default to infinity
		}
		else if(DEPTH_0.equals(depthString))	//0
		{
			return Depth.ZERO;
		}
		else if(DEPTH_1.equals(depthString))	//1
		{
			return Depth.ONE;
		}
		else if(DEPTH_INFINITY.equals(depthString))	//infinity
		{
			return Depth.INFINITY;
		}
		else	//unrecognized depth (technically an error)
		{
			return Depth.INFINITY;	//default to infinity
		}
	}
	
  /**Determines the HTTP methods allowed for the requested resource.
  This version adds support for WebDAV methods.
  @param request The HTTP request indicating the requested resource.
  @param resourceURI The URI of a resource for which options should be obtained.
  @return A set of methods allowed for this resource.
	@exception IOException if there is an error accessing the resource.
  */
	protected Set<String> getAllowedMethods(final HttpServletRequest request, final URI resourceURI) throws IOException
	{
		final Set<String> allowedMethods=new HashSet<String>(super.getAllowedMethods(request, resourceURI));	//create a new set of method strings, initializing them with the default allowed methods
		if(exists(request, resourceURI))	//if the resource exists
		{
			allowedMethods.add(COPY_METHOD);
//  	TODO implement  		methodSet.add(WebDAVMethod.LOCK);
			allowedMethods.add(MOVE_METHOD);

			if(true/*TODO fix---is this valid for WebDAV? LIST_DIRECTORIES*/)	//if we allow directory listings
			{
				allowedMethods.add(PROPFIND_METHOD);
			}
//  	TODO implement  		methodSet.add(PROPPATCH);
//  	TODO implement  		methodSet.add(UNLOCK);
		}
		else	//if the resource does not exist
		{
//  	TODO implement  		methodSet.add(LOCK);
			allowedMethods.add(MKCOL_METHOD);
		}
		return allowedMethods;	//return the allowed methods
	}

	/**Checks whether the given principal is authorized to invoke the given method on the given resource.
	This version restricts the WebDAV methods COPY, MOVE, and MKCOL if the servlet is read-only.
	This version recognizes the COPY and MOVE WebDAV methods and correctly calls this method on their destination URIs.
	Any child class must call this method.
  @param request The HTTP request.
	@param resourceURI The URI of the resource requested.
	@param method The HTTP method requested on the resource.
	@param principal The principal requesting authentication, or <code>null</code> if the principal is not known.
	@param realm The realm with which the resource is associated, or <code>null</code> if the realm is not known.
	@return <code>true</code> if the given principal is authorized to perform the given method on the resource represented by the given URI.
	@exception HTTPInternalServerErrorException if there is an error determining if the principal is authorized.
	@see #isReadOnly()
	*/
	protected boolean isAuthorized(final HttpServletRequest request, final URI resourceURI, final String method, final Principal principal, final String realm) throws HTTPInternalServerErrorException
	{
		boolean isAuthorized=super.isAuthorized(request, resourceURI, method, principal, realm);	//see if this principal passes the default authorization checks
		if(isAuthorized)	//if this principal passes the default authorization checks
		{
			if(isReadOnly())	//if this servlet is read-only
			{
				if(COPY_METHOD.equals(method) || MOVE_METHOD.equals(method) || MKCOL_METHOD.equals(method))	//disallow the COPY, MOVE, and MKCOL methods
				{
					return false;	//don't allow write methods
				}
			}
			if(COPY_METHOD.equals(method) || MOVE_METHOD.equals(method))	//if this is COPY or MOVE
			{
				final URI requestedDestinationURI=getDestination(request);	//get the destination URI for the operation
				if(requestedDestinationURI!=null)	//if a destination was given (ignore missing destinations---a principal is authorized to copy or move a resource to nowhere)
				{
Debug.trace("checking authorization for requested destination", requestedDestinationURI);
						//get the canonical destination URI
					final URI destinationURI=getResourceURI(request, requestedDestinationURI, request.getMethod(), resourceURI);
						//for COPY and MOVE, make sure the principal is authorized to do a PUT on the destination
					isAuthorized=isAuthorized(request, destinationURI, PUT_METHOD, principal, realm);
				}
			}
		}
		return isAuthorized;	//return whether the principal is authorized
	}

	/**Copies all the requested resource properties to the given property XML element.
	@param request The HTTP request in response to which properties are being retrieved.
	@param resource The resource the properties of which should be found.
	@param propertyElement The XML element which will receive a representation of the resource properties.
	@param properties A list of all requested properties, or {@link WebDAV#ALL_PROPERTIES} or {@link WebDAV#PROPERTY_NAMES} indicating all properties or all property names, respectively.
	@param webdavXMLGenerator The generator for constructing XML to represent WebDAV information.
	@see WebDAV#ALL_PROPERTIES
	@see WebDAV#PROPERTY_NAMES
	@exception DOMException if there is an error updating the properties element.
	@exception IOException if there is an error accessing the resource.
	*/
	protected abstract void findProperties(final HttpServletRequest request, final R resource, final Element propertyElement, final DecoratorIDedMappedList<URI, WebDAVPropertyName> properties, final WebDAVXMLGenerator webdavXMLGenerator) throws DOMException, IOException;

	/**Retrieves a list of resources and child resources to the given depth.
	@param resourceURI The URI of the requested resource.
  @param depth The zero-based depth of child resources to retrieve, or
  	<code>-1</code> if all progeny should be included.
  @return A list of resources and optionally children as specified..  
	@exception IllegalArgumentException if the given resource URI does not represent a valid resource.
	@exception IOException if there is an error accessing the resources.
  */
	protected abstract List<R> getResources(final URI resourceURI, final int depth) throws IllegalArgumentException, IOException;

	/**Copies a resource.
	@param request The HTTP request in response to which the resource is being copied.
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
	protected abstract void copyResource(final HttpServletRequest request, final R resource, final URI destinationURI, final int depth, final boolean overwrite) throws IllegalArgumentException, IOException, HTTPConflictException, HTTPPreconditionFailedException;

	/**Moves a resource.
	@param request The HTTP request in response to which the resource is being moved.
	@param resource The resource to move.
	@param destinationURI The destination URI to which the resource should be moved.
	@param overwrite <code>true</code> if any existing resource at the destination should be overwritten, else <code>false</code>.
	@exception IllegalArgumentException if the given resource URI does not represent a valid resource in a valid burrow.
	@exception IOException Thrown if there is an error moving the resource.
	@exception HTTPConflictException if an intermediate collection required for creating this collection does not exist.
	@exception HTTPPreconditionFailedException if a resource already exists at the destination and <var>overwrite</var> is <code>false</code>.
	*/
	protected abstract void moveResource(final HttpServletRequest request, final R resource, final URI destinationURI, final boolean overwrite) throws IllegalArgumentException, IOException, HTTPConflictException, HTTPPreconditionFailedException;

}
