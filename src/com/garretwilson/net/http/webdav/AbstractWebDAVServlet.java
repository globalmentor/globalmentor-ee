package com.garretwilson.net.http.webdav;

import java.io.*;
import java.net.*;
import java.util.*;
import static java.util.Collections.*;

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
import static com.garretwilson.net.http.HTTPConstants.*;
import static com.garretwilson.net.http.webdav.WebDAVConstants.*;
import static com.garretwilson.text.CharacterConstants.*;
import static com.garretwilson.text.CharacterEncodingConstants.*;
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
public abstract class AbstractWebDAVServlet<R extends Resource> extends HttpServlet	//TODO address http://lists.w3.org/Archives/Public/w3c-dist-auth/1999OctDec/0343.html
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
/*G***del
Debug.setDebug(true);
Debug.setVisible(true);
*/
Debug.trace("servicing method", method);
/*G***del
Debug.trace("servlet path:", request.getServletPath());
Debug.trace("request URI:", request.getRequestURI());
Debug.trace("request URL:", request.getRequestURL());
Debug.trace("path info:", request.getPathInfo());
*/
		if(WebDAVMethod.PROPFIND.toString().equals(method))	//PROPFIND
		{
			doPropfind(request, response);	//delegate to the propfind method
		}
		else	//if the request was not recognized
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
  @param request The HTTP request.
  @param response The HTTP response.
  @exception ServletException if there is a problem servicing the request.
  @exception IOException if there is an error reading or writing data.
  */
	public void doOptions(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
	{
		final URI resourceURI=getResourceURI(request, response);	//get the URI of the requested resource
		if(resourceURI!=null)	//if we have an appropriate resource URI
		{
Debug.trace("doing options for URI", resourceURI);
			response.addHeader(DAV_HEADER, "1,2");	//we support WebDAV levels 1 and 2
			final Set<WebDAVMethod> allowedMethodSet=getAllowedMethods(resourceURI);	//get the allowed methods
			response.addHeader(ALLOW_HEADER, CollectionUtilities.toString(allowedMethodSet, COMMA_CHAR));	//put the allowed methods in the "allow" header, separated by commas
			response.addHeader(MS_AUTHOR_VIA_HEADER, MS_AUTHOR_VIA_DAV);	//tell Microsoft editing tools to use WebDAV rather than FrontPage
		}
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
			final URI resourceURI=getResourceURI(request, response);	//get the URI of the requested resource
			if(resourceURI!=null)	//if we have an appropriate resource URI
			{
				final boolean exists=exists(resourceURI);	//see whether the resource already exists
				final R resource;	//we'll get the existing resource, if there is one 
				if(exists(resourceURI))	//if this resource exists
		    {
					resource=getResource(resourceURI);	//get the resource information
		    }
				else	//if the resource doesn't exist
				{
					resource=createResource(resourceURI);	//create the resource TODO make sure no default resource content is created here
				}
				if(resource!=null)	//if we were able to create a resource
				{
					try
					{
						final InputStream inputStream=request.getInputStream();	//get an input stream from the request
						final OutputStream outputStream=getOutputStream(resource);	//get an output stream to the resource
						try
						{
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
				else	//if we couldn't create a resource
				{
	        response.sendError(HttpServletResponse.SC_CONFLICT);	//indicate there's a conflict creating the resource				
				}
			}
			else	//if this servlet is read-only
			{
				response.sendError(HttpServletResponse.SC_FORBIDDEN);	//indicate that this method is forbidden
			}
		}
  }

	/**Services the PROPFIND method.
  @param request The HTTP request.
  @param response The HTTP response.
  @exception ServletException if there is a problem servicing the request.
  @exception IOException if there is an error reading or writing data.
  */
	protected void doPropfind(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
	{
		final URI resourceURI=getResourceURI(request, response);	//get the URI of the requested resource
		if(resourceURI!=null)	//if we have an appropriate resource URI
		{
Debug.trace("doing propfind for URI", resourceURI);
			if(LIST_DIRECTORIES)	//if we allow directory listing
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
		      response.sendError(HttpServletResponse.SC_BAD_REQUEST, domException.getMessage());	//show that the XML wasn't correct				
				}
				try
				{
					final List<R> resourceList=getResources(resourceURI, depth);	//get a list of resources
					if(resourceList!=null)	//if we found the resource
					{
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
					else	//if we couldn't get the list of resource
					{
	    			response.sendError(SC_NOT_FOUND, resourceURI.toString());	//show that we didn't find a resource for which to find properties					
					}
				}
				catch(final DOMException domException)	//any XML problem here is the server's fault
				{
					Debug.error(domException);	//report the error
		      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, domException.getMessage());	//show that the XML wasn't correct				
				}
			}
			else	//if directory listing is not allowed
			{
					//show which methods we support
				final Set<WebDAVMethod> allowedMethodSet=getAllowedMethods(resourceURI);	//get the allowed methods
				response.addHeader(ALLOW_HEADER, CollectionUtilities.toString(allowedMethodSet, COMMA_CHAR));	//put the allowed methods in the "allow" header, separated by commas
	      response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);	//show that we don't allow listing properties
			}
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
		final URI resourceURI=getResourceURI(request, response);	//get the URI of the requested resource
Debug.trace("serving resource", resourceURI);
		if(resourceURI!=null)	//if we have an appropriate resource URI
		{
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
	    	response.sendError(HttpServletResponse.SC_NOT_FOUND, resourceURI.toString());
	    }
		}
	}

	/**Determines the URI of the requested resource.
  @param request The HTTP request indicating the requested resource.
  @param response The HTTP response, so that redirects may occur.
  @return The URI of the requested resource, or <code>null</code> if there is no
  	appropriate URI indicating that no further processing should occur.
  @exception IOException if there is an error redirecting if needed.
  */
	protected URI getResourceURI(final HttpServletRequest request, final HttpServletResponse response) throws IOException	//TODO convert to using new URI()
	{
		final String requestURIString=request.getRequestURL().toString();	//get the requested URI string
		final URI requestURI=URI.create(requestURIString);	//create a URI from the full request URL
Debug.trace("request URI", requestURI);
//G***del Debug.trace("ends with slash?", endsWith(requestURIString, PATH_SEPARATOR));
//G***del Debug.trace("exists?", exists(requestURI));
		if(!endsWith(requestURIString, PATH_SEPARATOR) && !exists(requestURI))	//if the client asked for a collection that doesn't exist
		{
			final URI redirectURI=URI.create(requestURIString+PATH_SEPARATOR);	//add a trailing slash
//G***del Debug.trace("checking redirect URI", redirectURI);
			if(isCollection(redirectURI))	//if the URI with a trailing slash added is a collection
			{
Debug.trace("sending redirect", redirectURI);
				response.sendRedirect(redirectURI.toString());	//redirect to the collection
				return null;	//don't send back a URI
			}
		}
		return requestURI;	//return the requested URI
//G***del when works		return URI.create(request.getRequestURL().toString());	//return a URI created from the full request URL
	}

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
		new XMLSerializer(true).serialize(document, byteArrayOutputStream, UTF_8);	//serialize the document to the byte array
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

	/**Retrieves a list of resources and child resources to the given depth.
	@param resourceURI The URI of the requested resource.
  @param depth The zero-based depth of child resources to retrieve, or
  	<code>-1</code> if all progeny should be included.
  @return A list of resources and optionally children as specified,
  	or <code>null</code> if no such resource exists.  
  */
	protected abstract List<R> getResources(final URI resourceURI, final int depth);

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
	@param resourceURI The URI of the resource to create.
	@return The description of a newly created resource, or <code>null</code> if
		the resource is not allowed to be created.
	@exception IOException Thrown if there is an error creating the resource.
	*/
	protected abstract R createResource(final URI resourceURI) throws IOException;

	/**Deletes a resource.
	@param resource The resource to delete.
	@exception IOException Thrown if the resource could not be deleted.
	*/
	protected abstract void deleteResource(final R resource) throws IOException;

	/**Retrieves an list of child resources of the given resource.
	@param resource The resource for which children should be returned.
	@return A list of child resources.
	@exception IOException Thrown if there is an error retrieving the list of child resources.
	*/
	protected abstract List<R> getChildResources(final R resource) throws IOException;	//G***do we want to pass the resource or just the URI here?

}
