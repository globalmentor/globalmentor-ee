package com.garretwilson.net.http.webdav;

import java.io.*;
import java.net.*;
import java.util.*;
import static java.util.Collections.*;

import javax.mail.internet.ContentType;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.xml.parsers.DocumentBuilder;

import org.apache.catalina.util.XMLWriter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.garretwilson.io.OutputStreamUtilities;
import static com.garretwilson.io.ContentTypeUtilities.*;
import com.garretwilson.model.Resource;
import static com.garretwilson.net.URIUtilities.*;
import static com.garretwilson.net.http.webdav.WebDAVConstants.*;
import static com.garretwilson.text.CharacterConstants.*;
import com.garretwilson.text.xml.QualifiedName;
import com.garretwilson.text.xml.XMLProcessor;
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
	protected final static List<QualifiedName> ALL_PROPERTIES=emptyList();

	/**The constant property list indicating all property names.*/
	protected final static List<QualifiedName> PROPERTY_NAMES=emptyList();

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
			final URI resourceURI=getResourceURI(request);	//get the URI of the requested resource
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

	/**Services the PROPFIND method.
  @param request The HTTP request.
  @param response The HTTP response.
  @exception ServletException if there is a problem servicing the request.
  @exception IOException if there is an error reading or writing data.
  */
	protected void doPropfind(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
	{
		final URI resourceURI=getResourceURI(request);	//get the URI of the requested resource
		if(LIST_DIRECTORIES)	//if we allow directory listing
		{
			final int depth=getDepth(request);	//determine the requested depth
			List<QualifiedName> propertyList=ALL_PROPERTIES;	//default to listing all properties
			try
			{
				final Document document=getXML(request);	//get the XML from the request body
				final Element documentElement=document.getDocumentElement();	//get the document element
					//TODO check to make sure the document element is correct
				propertyList=getProperties(documentElement);	//get the property list from the XML document
			}
			catch(final IOException ioException)	//if there was a problem reading the XML body content
			{
				Debug.warn(ioException);	//assume there is no content and keep going, keeping the default ALL_PROPERTIES TODO fix better
			}
		}
		else	//if directory listing is not allowed
		{
				//show which methods we support
			final Set<WebDAVMethod> allowedMethodSet=getAllowedMethods(resourceURI);	//get the allowed methods
			response.addHeader(ALLOW_HEADER, CollectionUtilities.toString(allowedMethodSet, COMMA_CHAR));	//put the allowed methods in the "allow" header, separated by commas
      response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);	//show that we don't allow listing properties
		}

/*TODO fix

      boolean exists = true;
      Object object = null;
      try {
          object = resources.lookup(path);
      } catch (NamingException e) {
          exists = false;
          int slash = path.lastIndexOf('/');
          if (slash != -1) {
              String parentPath = path.substring(0, slash);
              Vector currentLockNullResources =
                  (Vector) lockNullResources.get(parentPath);
              if (currentLockNullResources != null) {
                  Enumeration lockNullResourcesList =
                      currentLockNullResources.elements();
                  while (lockNullResourcesList.hasMoreElements()) {
                      String lockNullPath = (String)
                          lockNullResourcesList.nextElement();
                      if (lockNullPath.equals(path)) {
                          resp.setStatus(WebdavStatus.SC_MULTI_STATUS);
                          resp.setContentType("text/xml; charset=UTF-8");
                          // Create multistatus object
                          XMLWriter generatedXML =
                              new XMLWriter(resp.getWriter());
                          generatedXML.writeXMLHeader();
                          generatedXML.writeElement
                              (null, "multistatus"
                               + generateNamespaceDeclarations(),
                               XMLWriter.OPENING);
                          parseLockNullProperties
                              (req, generatedXML, lockNullPath, type,
                               properties);
                          generatedXML.writeElement(null, "multistatus",
                                                    XMLWriter.CLOSING);
                          generatedXML.sendData();
                          return;
                      }
                  }
              }
          }
      }

      if (!exists) {
          resp.sendError(HttpServletResponse.SC_NOT_FOUND, path);
          return;
      }

      resp.setStatus(WebdavStatus.SC_MULTI_STATUS);

      resp.setContentType("text/xml; charset=UTF-8");

      // Create multistatus object
      XMLWriter generatedXML = new XMLWriter(resp.getWriter());
      generatedXML.writeXMLHeader();

      generatedXML.writeElement(null, "multistatus"
                                + generateNamespaceDeclarations(),
                                XMLWriter.OPENING);

      if (depth == 0) {
          parseProperties(req, generatedXML, path, type,
                          properties);
      } else {
          // The stack always contains the object of the current level
          Stack stack = new Stack();
          stack.push(path);

          // Stack of the objects one level below
          Stack stackBelow = new Stack();

          while ((!stack.isEmpty()) && (depth >= 0)) {

              String currentPath = (String) stack.pop();
              parseProperties(req, generatedXML, currentPath,
                              type, properties);

              try {
                  object = resources.lookup(currentPath);
              } catch (NamingException e) {
                  continue;
              }

              if ((object instanceof DirContext) && (depth > 0)) {

                  try {
                      NamingEnumeration enumeration = resources.list(currentPath);
                      while (enumeration.hasMoreElements()) {
                          NameClassPair ncPair =
                              (NameClassPair) enumeration.nextElement();
                          String newPath = currentPath;
                          if (!(newPath.endsWith("/")))
                              newPath += "/";
                          newPath += ncPair.getName();
                          stackBelow.push(newPath);
                      }
                  } catch (NamingException e) {
                      resp.sendError
                          (HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                           path);
                      return;
                  }

                  // Displaying the lock-null resources present in that
                  // collection
                  String lockPath = currentPath;
                  if (lockPath.endsWith("/"))
                      lockPath =
                          lockPath.substring(0, lockPath.length() - 1);
                  Vector currentLockNullResources =
                      (Vector) lockNullResources.get(lockPath);
                  if (currentLockNullResources != null) {
                      Enumeration lockNullResourcesList =
                          currentLockNullResources.elements();
                      while (lockNullResourcesList.hasMoreElements()) {
                          String lockNullPath = (String)
                              lockNullResourcesList.nextElement();
                          parseLockNullProperties
                              (req, generatedXML, lockNullPath, type,
                               properties);
                      }
                  }

              }

              if (stack.isEmpty()) {
                  depth--;
                  stack = stackBelow;
                  stackBelow = new Stack();
              }

              generatedXML.sendData();

          }
      }

      generatedXML.writeElement(null, "multistatus",
                                XMLWriter.CLOSING);

      generatedXML.sendData();
*/
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
	@return A document representing the XML information.
	@exception IOException if there is an error reading the XML.
	*/
	protected Document getXML(final HttpServletRequest request) throws IOException
	{
		final InputStream inputStream=request.getInputStream();	//get an input stream to the request content
		final XMLProcessor xmlProcessor=new XMLProcessor();	//create a new XML processor to process the information TODO use a generic way of getting the XML processor
		return xmlProcessor.parseDocument(inputStream, null);	//parse the document
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
	protected List<QualifiedName> getProperties(final Element element)
	{
		final List<QualifiedName> propertyList=new ArrayList<QualifiedName>();	//create a list of qualified names
		final NodeList childList=element.getChildNodes();	//get a list of element children
		for(int childIndex=0; childIndex<childList.getLength(); ++childIndex)	//look at each child node
		{
			final Node childNode=childList.item(childIndex);	//get this child node
			if(childNode.getNodeType()==Node.ELEMENT_NODE)	//if this is an element
			{
//G***del				final Element childElement=(Element)childNode;	//get a reference to this element
				if(WEBDAV_NAMESPACE_URI.toString().equals(childNode.getNamespaceURI()))	//if this is a WebDAV element
				{
					final String childLocalName=childNode.getLocalName();	//get the child element's local name
					if(ELEMENT_PROP.equals(childLocalName))	//allprop
					{
						final NodeList propertyChildList=childNode.getChildNodes();	//get a list of property element children
						for(int propertyChildIndex=0; propertyChildIndex<propertyChildList.getLength(); ++propertyChildIndex)	//look at each property child node
						{
							final Node propertyChildNode=childList.item(propertyChildIndex);	//get this property child node
							if(propertyChildNode.getNodeType()==Node.ELEMENT_NODE)	//if this is an element
							{
//G***del								final Element propertyChildElement=(Element)propertyChildNode;	//get a reference to this element
									//create a qualified name for this property
								final QualifiedName qname=new QualifiedName(URI.create(propertyChildNode.getNamespaceURI()), propertyChildNode.getPrefix(), propertyChildNode.getLocalName());
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
