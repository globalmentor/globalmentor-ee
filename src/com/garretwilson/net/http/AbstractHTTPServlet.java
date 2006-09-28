package com.garretwilson.net.http;

import java.io.*;
import java.net.*;
import java.security.Principal;
import java.text.DateFormat;
import java.util.*;
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
import static com.garretwilson.lang.ClassUtilities.getLocalName;

import com.garretwilson.model.Resource;
import com.garretwilson.net.http.DefaultHTTPServlet.HTTPServletResource;

import static com.garretwilson.net.URIConstants.*;
import static com.garretwilson.net.URIUtilities.*;
import static com.garretwilson.net.http.HTTPConstants.*;
import static com.garretwilson.net.http.webdav.WebDAVConstants.*;
import static com.garretwilson.servlet.http.HttpServletUtilities.*;
import static com.garretwilson.text.CharacterConstants.*;
import static com.garretwilson.text.CharacterEncodingConstants.*;
import static com.garretwilson.text.TextUtilities.*;

import com.garretwilson.text.CharacterEncoding;
import com.garretwilson.text.SyntaxException;
import com.garretwilson.text.xml.XMLDOMImplementation;
import com.garretwilson.text.xml.XMLProcessor;
import com.garretwilson.text.xml.XMLSerializer;

import com.garretwilson.util.*;

/**The base servlet class for implementing an HTTP server that access resources.
@see http://www.ietf.org/rfc/rfc2616.txt
@see http://www.mnot.net/cache_docs/
@author Garret Wilson
*/
public abstract class AbstractHTTPServlet<R extends Resource> extends BasicHTTPServlet	//TODO address http://lists.w3.org/Archives/Public/w3c-dist-auth/1999OctDec/0343.html
{

	protected final static boolean LIST_DIRECTORIES=false;	//TODO fix

	/**Whether access is restricted to read methods.*/
	private boolean readOnly=false;

		/**@return Whether access is restricted to read methods.*/
		protected boolean isReadOnly() {return readOnly;}

		/**Sets whether access is restricted to read methods.
		@param readOnly The new read-only status.
		*/
		protected void setReadOnly(final boolean readOnly) {this.readOnly=readOnly;}

	/**The DOM implementation used as a document factory.*/
	private final DOMImplementation domImplementation=new XMLDOMImplementation();	//TODO get this in a general way

		/**@return The DOM implementation used as a document factory.*/
		protected DOMImplementation getDOMImplementation() {return domImplementation;}

	/**An array of regular expressions matching user agents not correctly supporting redirects.
	@see http://lists.w3.org/Archives/Public/w3c-dist-auth/2002AprJun/0190.html
	@see http://purl.org/NET/http-errata#saferedirect
	@see http://httpd.apache.org/docs-2.0/env.html#special
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
				Pattern.compile("^WebDrive.*")	//http://lists.w3.org/Archives/Public/w3c-dist-auth/2002AprJun/0190.html
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
	
	/**Services the OPTIONS method.
  @param request The HTTP request.
  @param response The HTTP response.
  @exception ServletException if there is a problem servicing the request.
  @exception IOException if there is an error reading or writing data.
  */
	public void doOptions(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
	{
		final URI resourceURI=getResourceURI(request);	//get the URI of the requested resource
//TODO del Debug.trace("doing options for URI", resourceURI);
		final Set<String> allowedMethodSet=getAllowedMethods(resourceURI);	//get the allowed methods
		response.addHeader(ALLOW_HEADER, CollectionUtilities.toString(allowedMethodSet, COMMA_CHAR));	//put the allowed methods in the "allow" header, separated by commas
		response.setContentLength(0);	//set the content length to zero, according to the HTTP specification for OPTIONS
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
//	TODO del Debug.trace("getting resource URI");
		final URI resourceURI=getResourceURI(request);	//get the URI of the requested resource
//	TODO del Debug.trace("checking destination existence");
		final boolean exists=exists(resourceURI);	//see whether the resource already exists
//	TODO del Debug.trace("exists", exists);
		final R resource;	//we'll get the existing resource, if there is one 
		if(exists)	//if this resource exists
    {
			resource=getResource(request, resourceURI);	//get the resource information
    }
		else	//if the resource doesn't exist
			{
//		TODO del Debug.trace("trying to create resource");
			try
			{
				resource=createResource(resourceURI);	//create the resource TODO make sure no default resource content is created here
			}
			catch(final IllegalArgumentException illegalArgumentException)	//if this is an invalid resource URI
			{
//			TODO del Debug.warn(illegalArgumentException);
					throw new HTTPForbiddenException(illegalArgumentException);	//forbid creation of resources with invalid URIs
			}
		}
		try
		{
			final InputStream inputStream=request.getInputStream();	//get an input stream from the request
			final OutputStream outputStream=getOutputStream(request, resource);	//get an output stream to the resource
			try
			{
//				TODO del Debug.trace("trying to write");
				OutputStreamUtilities.copy(inputStream, outputStream);	//copy the file from the request to the resource
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

	/**Services the DELETE method.
  @param request The HTTP request.
  @param response The HTTP response.
  @exception ServletException if there is a problem servicing the request.
  @exception IOException if there is an error reading or writing data.
  */
	public void doDelete(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException
	{
		final URI resourceURI=getResourceURI(request);	//get the URI of the requested resource
		final boolean exists=exists(resourceURI);	//see whether the resource already exists
		final R resource;	//we'll get the existing resource, if there is one 
		if(exists)	//if this resource exists
    {
			resource=getResource(request, resourceURI);	//get the resource information
			deleteResource(resource);	//delete the resource
    }
    else	//if the resource does not exist
    {
			throw new HTTPNotFoundException(resourceURI.toString());	//show that we didn't find a resource for which to find properties					
    }					
  }

	/**Services the GET, HEAD, and POST methods.
  The response will be compressed if supported by the user agent.
  @param request The HTTP request.
  @param response The HTTP response.
  @param serveContent <code>true</code> if the contents of the resource should be returned.
  @exception ServletException if there is a problem servicing the request.
  @exception IOException if there is an error reading or writing data.
  */
	protected void serveResource(final HttpServletRequest request, final HttpServletResponse response, final boolean serveContent) throws ServletException, IOException
	{
		final URI resourceURI=getResourceURI(request);	//get the URI of the requested resource
//	TODO del Debug.trace("serving resource", resourceURI);
		if(exists(resourceURI))	//if this resource exists
    {
//TODO del Debug.trace("resource exists", resourceURI);
    	//TODO check if headers
    	final R resource=getResource(request, resourceURI);	//get a resource description
//G***del    	final ContentType contentType;	//determine the content type of the resource
    	if(isCollection(resource.getReferenceURI()))	//if the resource is a collection
    	{
//TODO del Debug.trace("is collection", resourceURI);
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
//TODO del Debug.trace("is not a collection; ready to send back file", resourceURI);
      	final Date lastModifiedDate=getLastModifiedDate(request, resource);	//get the last modified date of the resource
      	if(lastModifiedDate!=null)	//if we know when the resource was last modified; check this before adding headers, especially because we use weak validators (RFC 2616 10.3.5)---Last-Modified time is implicitly weak (RDF 2616 13.3.3)
      	{
//TODO del Debug.trace("last modified date:", new HTTPDateFormat().format(lastModifiedDate));
					final Date roundedLastModifiedDate=new Date((lastModifiedDate.getTime()/1000)*1000);	//round the date to the nearest millisecond before using it to compare, because the incoming date only has a one-second precision and comparing with the incoming rounded date would result in data being sent back unnecessarily; see Hunter, Jason, _Java Servlet Programming_, Second Edition, page 59
					try
					{
		      	final Date ifModifiedSinceDate=getIfModifiedSinceDate(request);	//get the If-Modified-Since date
/*TODO del 
	if(ifModifiedSinceDate!=null)
	{
		Debug.trace("If-Modified-Since:", new HTTPDateFormat().format(ifModifiedSinceDate));
		Debug.trace("ready to compare ifModifiedSince", ifModifiedSinceDate.getTime(), "and roundedLastModified", roundedLastModifiedDate.getTime(), "lastModified", lastModifiedDate.getTime());
	}
*/
		      	if(ifModifiedSinceDate!=null && ifModifiedSinceDate.compareTo(roundedLastModifiedDate)<=0)	//if there is an If-Modified-Since date and the resource was not modified since that date
		      	{
//TODO del Debug.trace("Not modified---use the value in the cache!");
		      		throw new HTTPNotModifiedException();	//stop serving content and indicate that the resource has not been modified
		      	}
		      	//TODO add support for If-Unmodified-Since
					}
					catch(final SyntaxException syntaxException)	//TODO fix better
					{
						throw new IllegalArgumentException(syntaxException);
					}
      	}
    		//    	TODO del Debug.trace("ready to send back a file");
    		final ContentType contentType=getContentType(resource);	//get the content type of the resource
      	if(contentType!=null)	//if we know the content type
	      {
//      	TODO del Debug.trace("setting content type to:", contentType);
//TODO del Debug.trace("setting content type to:", contentType);	//TODO del
      		response.setContentType(contentType.toString());	//tell the response which content type we're serving
      	}
      	if(HEAD_METHOD.equals(request.getMethod()))	//if this is a HEAD request, send back the content-length, but not for other methods, as we may compress the actual content TODO make sure this is the correct; RFC 2616 is ambiguous as to whether a HEAD content length should be the compressed length or the uncompresed length
      	{
					final long contentLength=getContentLength(request, resource);	//get the content length of the resource
	      	if(contentLength>=0)	//if we found a content length for the resource
		      {
	//      	TODO del Debug.trace("setting content length to:", contentLength);
	      		assert contentLength<Integer.MAX_VALUE : "Resource size "+contentLength+" is too large.";
	      		response.setContentLength((int)contentLength);	//tell the response the size of the resource      		
	      	}
      	}
      	if(lastModifiedDate!=null)	//if we know when the resource was last modified
      	{
					setLastModified(response, lastModifiedDate);	//set the last modified date header
      	}
      	if(serveContent)	//if we should serve content
      	{
      		//TODO fix ranges
      		final OutputStream outputStream;	//we'll determine the output stream
      		if(contentType!=null && isText(contentType))	//if this is a text content type TODO later add other content types, if they can be compressed
      		{
//TODO del      			Debug.trace("compressing content type:", contentType);
      			outputStream=getCompressedOutputStream(request, response);	//get the output stream, compressing it if we can TODO do we want to check for an IllegalStateException, and send back text if we can?
      		}
      		else	//if we don't know the conten type, or it isn't text
      		{
//TODO del      			Debug.trace("not compressing content type:", contentType);
      			outputStream=response.getOutputStream();	//get the output stream without compression, as this could be a binary resource, making compression counter-productive TODO do we want to check for an IllegalStateException, and send back text if we can?      			
      		}
      		final InputStream inputStream=new BufferedInputStream(getInputStream(request, resource));	//get an input stream to the resource
      		try
      		{
      			OutputStreamUtilities.copy(inputStream, outputStream);	//copy the input stream to the output stream
      		}
      		finally
      		{
     				inputStream.close();	//always close the input stream to the resource
      		}
      		outputStream.close();	//if there are no errors, close the output stream, which will write the remaining compressed data, if this is a compressed output stream
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
//	TODO del Debug.trace("requested URI", requestedResourceURI);
//G***del Debug.trace("ends with slash?", endsWith(requestURIString, PATH_SEPARATOR));
//G***del Debug.trace("exists?", exists(requestURI));
		final String requestResourceURIString=requestedResourceURI.toString();	//get the string version of the request URI
		if(!endsWith(requestResourceURIString, PATH_SEPARATOR))	//if the URI is not a collection URI
		{
			final URI collectionURI=URI.create(requestResourceURIString+PATH_SEPARATOR);	//add a trailing slash to get a collection URI
				//if this is a method referring to a collection
			if(MKCOL_METHOD.equals(method))	//TODO make sure this goes in the correct servlet
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
				try
				{
//TODO del	Debug.trace("requested resource exists:", exists(requestedResourceURI));
//TODO del	Debug.trace("other resource is collection:", isCollection(collectionURI));
					//if there is no such resource but there is a resource at the collection URI
					if(!exists(requestedResourceURI) && isCollection(collectionURI))
					{
						resourceURI=collectionURI;	//use the collection URI				
					}
				}
				catch(final IOException ioException)	//if there is an error checking existence or whether the resource is a collection
				{
					Debug.warn(ioException);	//don't do anything major, now---the request will fail, later, and there's no forwarding to be done for error-prone resources
				}
			}
		}
//TODO del Debug.trace("using URI", resourceURI);
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

	/**Retrieves an XML document from the body of an HTTP request.
	@param request The request from which to get the XML document.
	@return A document representing the XML information, or <code>null</code>
		if nothing but whitespace was included in the request.
	@exception IOException if there is an error reading the XML.
	*/
	protected Document getXML(final HttpServletRequest request) throws IOException
	{
//	TODO del Debug.trace("getting XML");
		final int contentLength=request.getContentLength();	//get the length of the request
//	TODO del Debug.trace("content length", contentLength);
		//TODO del; no content length means no content		assert contentLength>=0 : "Missing content length";
		if(contentLength>0)	//if content is present	//TODO fix chunked coding
		{
			final InputStream inputStream=request.getInputStream();	//get an input stream to the request content
//TODO del Debug.trace("Ready to get XML bytes of Content-Length:", contentLength);
			final byte[] content=InputStreamUtilities.getBytes(inputStream, contentLength);	//read the request TODO check for the content being shorter than expected
//TODO del Debug.trace("got bytes:", new String(content, "UTF-8"));
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
//TODO del Debug.trace("ready to parse");
				return xmlProcessor.parseDocument(xmlInputStream, null);	//parse the document				
			}
		}
/*TODO del; we accept no content as no XML
		else if(contentLength<0)	//if no content length was given
		{
			throw new HTTPLengthRequiredException();	//indicate that we require a content length
		}
*/
		return null;	//show that there is no content to return
	}

	/**Places an XML document into the body of an HTTP response.
	The XML will be sent back compressed if supported by the user agent.
	@param request The request for which this XML represents a response.
	@param response The response into which to place the XML document.
	@param document The XML document to place into the response.
	@exception IOException if there is an error writing the XML.
	*/
	protected void setXML(final HttpServletRequest request, final HttpServletResponse response, final Document document) throws IOException
	{
		final ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();	//create a byte array output stream to hold our outgoing data
		try
		{
			new XMLSerializer(true).serialize(document, byteArrayOutputStream, new CharacterEncoding(UTF_8, null, NO_BOM));	//serialize the document to the byte array with no byte order mark
			final byte[] bytes=byteArrayOutputStream.toByteArray();	//get the bytes we serialized
				//set the content type to text/xml; charset=UTF-8
			response.setContentType(ContentTypeUtilities.toString(TEXT, XML_SUBTYPE, new NameValuePair<String, String>(CHARSET_PARAMETER, UTF_8)));
//TODO del; this prevents compression			response.setContentLength(bytes.length);	//tell the response how many bytes to expect
			final OutputStream outputStream=getCompressedOutputStream(request, response);	//get an output stream to the response, compressing the output if possible
			final InputStream inputStream=new ByteArrayInputStream(bytes);	//get an input stream to the bytes
			try
			{
				OutputStreamUtilities.copy(inputStream, outputStream);	//write the bytes to the response
			}
			finally
			{
				inputStream.close();	//always close our input stream as good practice
			}
  		outputStream.close();	//if there are no errors, close the output stream, which will write the remaining compressed data, if this is a compressed output stream
		}
		finally
		{
			byteArrayOutputStream.close();	//always close the stream as good practice			
		}		
	}

  /**Determines the HTTP methods allowed for the requested resource.
  @param resourceURI The URI of a resource for which options should be obtained.
  @return A set of methods allowed for this resource.
	@exception IOException if there is an error accessing the resource.
  */
	protected Set<String> getAllowedMethods(final URI resourceURI) throws IOException
	{
		final Set<String> allowedMethods=new HashSet<String>();	//create a new set of method strings
		allowedMethods.add(OPTIONS_METHOD);	//we always allow options
		if(exists(resourceURI))	//if the resource exists
		{
			allowedMethods.add(GET_METHOD);
			allowedMethods.add(HEAD_METHOD);
			allowedMethods.add(POST_METHOD);
			if(LIST_DIRECTORIES)	//if we allow directory listings
			{
	//		TODO implement  			methodSet.add(PROPFIND);
			}
			if(!isCollection(resourceURI))	//if the resource is not a collection
			{
				allowedMethods.add(PUT_METHOD);	//allow saving a resource to this location  			
			}
		}
		else	//if the resource does not exist
		{
//  	TODO implement  		methodSet.add(LOCK);
			allowedMethods.add(PUT_METHOD);  		
		}
//	TODO implement  		methodSet.add(TRACE);
		return allowedMethods;	//return the allowed methods
	}

	/**Checks whether the given principal is authorized to invoke the given method on the given resource.
	This version restricts the HTTP methods PUT and DELETE if the servlet is read-only.
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
		if(!super.isAuthorized(request, resourceURI, method, principal, realm))	//if the request does not pass default authorization
		{
			return false;	//don't allow authorization
		}
		if(isReadOnly())	//if this servlet is read-only
		{
			if(PUT_METHOD.equals(method) || DELETE_METHOD.equals(method))	//disallow the PUT and DELETE methods
			{
				return false;	//don't allow write methods
			}
		}
		return true;	//report that the request is authorized
	}

	/**Determines the realm applicable for the resource indicated by the given URI
	This version returns the local class name of the servlet unless the servlet is read-only, in case it returns <code>null</code>.
	@param resourceURI The URI of the resource requested.
	@return The realm appropriate for the resource, or <code>null</code> if the given resource is not in a known realm.
	@exception HTTPInternalServerErrorException if there is an error getting the realm.
	@see #isReadOnly()
	*/
	protected String getRealm(final URI resourceURI) throws HTTPInternalServerErrorException
	{
		return isReadOnly() ? null : getLocalName(getClass());	//return the local name of the servlet class unless the servlet is read-only
	}

  /**Determines if the resource at a given URI exists.
  @param resourceURI The URI of the requested resource.
  @return <code>true</code> if the resource exists, else <code>false</code>.
	@exception IOException if there is an error accessing the resource.
  */
  protected abstract boolean exists(final URI resourceURI) throws IOException;

  /**Determines if the resource at a given URI is an existing collection.
  @param resourceURI The URI of the requested resource.
  @return <code>true</code> if the resource is a collection, else <code>false</code>.
	@exception IOException if there is an error accessing the resource.
	@see #exists(URI)
  */
  protected abstract boolean isCollection(final URI resourceURI) throws IOException;

	/**Determines the requested resource.
	@param request The HTTP request in response to which the resource is being retrieved.
	@param resourceURI The URI of the requested resource.
  @return An object providing an encapsulation of the requested resource,
  	but not necessarily the contents of the resource. 
	@exception IllegalArgumentException if the given resource URI does not represent a valid resource.
	@exception IOException if there is an error accessing the resource.
  */
	protected abstract R getResource(final HttpServletRequest request, final URI resourceURI) throws IllegalArgumentException, IOException;

	/**Determines the content type of the given resource.
	This default version returns the MIME content type servlet known by the servlet context.
	@param resource The resource for which the content type should be determined.
	@return The content type of the given resource, or <code>null</code> if no
		content type could be determined.
	@see ServletContext#getMimeType(java.lang.String)
	*/
	protected ContentType getContentType(final R resource)	//TODO see if this is correct to go here, and if we need to override it somewhere to supplement the server's list of content types
	{
		final String contentTypeString=getServletContext().getMimeType(getFileName(resource.getReferenceURI()));	//ask the servlet context for the MIME type
		return contentTypeString!=null ? createContentType(contentTypeString) : null;	//create a content type object if a content type string was returned
	}

	/**Determines the content length of the given resource.
	@param request The HTTP request in response to which the content length is being retrieved.
	@param resource The resource for which the content length should be determined.
	@return The content length of the given resource, or <code>-1</code> if no
		content type could be determined.
	@exception IOException Thrown if there is an error accessing the resource.
	*/
	protected abstract long getContentLength(final HttpServletRequest request, final R resource) throws IOException;

	/**Determines the last modified date of the given resource.
	@param request The HTTP request in response to which the last modified date is being retrieved.
	@param resource The resource for which the last modified date should be determined.
	@return The last modified date of the given resource, or <code>null</code> if no there is no known last modified date.
	@exception IOException Thrown if there is an error accessing the resource.
	*/
	protected abstract Date getLastModifiedDate(final HttpServletRequest request, final R resource) throws IOException;

	/**Retrieves an input stream to the given resource.
	@param request The HTTP request in response to which the input stream is being retrieved.
	@param resource The resource for which an input stream should be retrieved.
	@return An input stream to the given resource.
	@exception IOException Thrown if there is an error accessing the resource,
		such as a missing file or a resource that has no contents.
	*/
	protected abstract InputStream getInputStream(final HttpServletRequest request, final R resource) throws IOException;	//G***do we want to pass the resource or just the URI here?

	/**Retrieves an output stream to the given resource.
	@param request The HTTP request in response to which the output stream is being retrieved.
	@param resource The resource for which an output stream should be retrieved.
	@return An output stream to the given resource.
	@exception IOException Thrown if there is an error accessing the resource.
	*/
	protected abstract OutputStream getOutputStream(final HttpServletRequest request, final R resource) throws IOException;	//G***do we want to pass the resource or just the URI here?

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

	/**Retrieves an list of child resources of the given resource.
	@param resource The resource for which children should be returned.
	@return A list of child resources.
	@exception IOException Thrown if there is an error retrieving the list of child resources.
	*/
	protected abstract List<R> getChildResources(final R resource) throws IOException;	//G***do we want to pass the resource or just the URI here?

}
