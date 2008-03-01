package com.garretwilson.net.http;

import java.io.*;
import java.net.*;
import java.util.*;
import static java.util.Collections.*;

import javax.mail.internet.ContentType;
import javax.servlet.http.HttpServletRequest;

import static com.globalmentor.io.ContentTypeConstants.*;
import static com.globalmentor.io.ContentTypes.*;
import static com.globalmentor.java.Objects.*;

import com.garretwilson.net.DefaultResource;
import com.garretwilson.net.Resource;
import com.garretwilson.net.URIs;
import com.globalmentor.io.ContentTypes;
import com.globalmentor.urf.*;
import com.globalmentor.urf.content.Content;
import com.globalmentor.util.Debug;

/**The default implementation of an HTTP servlet that accesses files in the web application.
This servlet may access files within a War file because it uses general servlet routines for resource access.
For this reason the servlet is read-only by default, because the servlet context does not offer any writing methods.
@author Garret Wilson
*/
public class DefaultHTTPServlet extends AbstractHTTPServlet<DefaultHTTPServlet.HTTPServletResource>	//TODO implement writing using the resource URL methods
{

	//TODO fix checks for WEB-INF

	/**Default constructor.
	This servlet defaults to being read-only.
	*/
	public DefaultHTTPServlet()
	{
		setReadOnly(true);	//default to being read-only, because the servlet context only provides read methods
	}

  /**Determines if the resource at a given URI exists.
	@param request The HTTP request in response to which which existence of the resource is being determined.
  @param resourceURI The URI of the requested resource.
  @return <code>true</code> if the resource exists, else <code>false</code>.
	@exception IOException if there is an error accessing the resource.
  */
  protected boolean exists(final HttpServletRequest request, final URI resourceURI) throws IOException
  {
		final String resourceContextAbsolutePath=getResourceContextAbsolutePath(resourceURI.getPath());	//get the absolute path relative to the context
  	return getServletContext().getResource(resourceContextAbsolutePath)!=null;	//return whether the servlet has mapped a resource to this path
  }

  /**Determines if the resource at a given URI is an existing collection.
	@param request The HTTP request in response to which the collection is being checked.
  @param resourceURI The URI of the requested resource.
  @return <code>true</code> if the resource is a collection, else <code>false</code>.
	@exception IOException if there is an error accessing the resource.
	@see #exists(URI)
  */
  protected boolean isCollection(final HttpServletRequest request, final URI resourceURI) throws IOException
  {
		final String resourceContextAbsolutePath=getResourceContextAbsolutePath(resourceURI.getPath());	//get the absolute path relative to the context
		return URIs.isCollectionPath(resourceContextAbsolutePath) && exists(request, resourceURI);	//return whether the context absolute path ends in a slash and the resource exists
  }

	/**Determines the requested resource.
	@param request The HTTP request in response to which the resource is being retrieved.
	@param resourceURI The URI of the requested resource.
  @return An object providing an encapsulation of the requested resource,
  	but not necessarily the contents of the resource. 
	@exception IllegalArgumentException if the given resource URI does not represent a valid resource.
	@exception IOException if there is an error accessing the resource.
  */
	protected HTTPServletResource getResource(final HttpServletRequest request, final URI resourceURI) throws IllegalArgumentException, IOException
	{
		final String resourceContextAbsolutePath=getResourceContextAbsolutePath(resourceURI.getPath());	//get the absolute path relative to the context
		try
		{
			final URL resourceURL=getServletContext().getResource(resourceContextAbsolutePath);	//get the URL of the resource
			return new DefaultHTTPServletResource(resourceURI, resourceURL);	//create a new default resource
		}
		catch(final MalformedURLException malformedURLException)	//if the path was not well-formed
		{
			throw new IllegalArgumentException(malformedURLException);
		}
	}

	/**Determines the content type of the given resource.
	@param request The HTTP request in response to which the content type is being retrieved.
	@param resource The resource for which the content type should be determined.
	@return The content type of the given resource, or <code>null</code> if no content type could be determined.
	*/
	protected ContentType getContentType(final HttpServletRequest request, final HTTPServletResource resource) throws IOException
	{
		final ContentType contentType=resource.getContentType(request);	//see if the resource indicates its content type
		return contentType!=null ? contentType : super.getContentType(request, resource);	//if no content type is specified, return the default content type, if one can be determined
	}

	/**Determines the content length of the given resource.
	@param request The HTTP request in response to which the content length is being retrieved.
	@param resource The resource for which the content length should be determined.
	@return The content length of the given resource, or <code>-1</code> if no content length could be determined.
	@exception IOException Thrown if there is an error accessing the resource.
	*/
	protected long getContentLength(final HttpServletRequest request, final HTTPServletResource resource) throws IOException
	{
		return resource.getContentLength(request);	//return the content length of the resource
	}

	/**Determines the last modified date of the given resource.
	@param request The HTTP request in response to which the last modified date is being retrieved.
	@param resource The resource for which the last modified date should be determined.
	@return The last modified date of the given resource, or <code>null</code> if no there is no known last modified date.
	@exception IOException Thrown if there is an error accessing the resource.
	*/
	protected Date getLastModifiedDate(final HttpServletRequest request, final HTTPServletResource resource) throws IOException
	{
		final long lastModified=resource.getLastModified(request);	//get the last modified information from the resource
		return lastModified>=0 ? new Date(lastModified) : null;	//return the last modified date, if we have that information
	}

	/**Retrieves an input stream to the given resource.
	@param request The HTTP request in response to which the input stream is being retrieved.
	@param resource The resource for which an input stream should be retrieved.
	@return An input stream to the given resource.
	@exception IOException Thrown if there is an error accessing the resource,
		such as a missing file or a resource that has no contents.
	*/
	protected InputStream getInputStream(final HttpServletRequest request, final HTTPServletResource resource) throws IOException
	{
		return resource.getInputStream(request);	//return the input stream to the resource, creating one if we haven't yet done so
	}

	/**Retrieves an output stream to the given resource.
	@param request The HTTP request in response to which the output stream is being retrieved.
	@param resource The resource for which an output stream should be retrieved.
	@return An output stream to the given resource.
	@exception IOException Thrown if there is an error accessing the resource.
	*/
	protected OutputStream getOutputStream(final HttpServletRequest request, final HTTPServletResource resource) throws IOException
	{
		throw new UnsupportedOperationException("DefaultHTTPServlet writing not yet implemented.");
	}

	/**Creates a resource and returns an output stream for storing content.
	If the resource already exists, it will be replaced.
	For collections, {@link #createCollection(URI)} should be used instead.
	@param request The HTTP request in response to which a resource is being created.
	@param resourceURI The URI of the resource to create.
	@return An output stream for storing content in the resource.
	@exception IllegalArgumentException if the given resource URI does not represent a valid resource.
	@exception IOException Thrown if there is an error creating the resource.
	@exception HTTPConflictException if an intermediate collection required for creating this collection does not exist.
	@see #createCollection(URI)
	*/
	protected OutputStream createResource(final HttpServletRequest request, final URI resourceURI) throws IllegalArgumentException, IOException, HTTPConflictException
	{
		throw new UnsupportedOperationException("DefaultHTTPServlet writing not yet implemented.");
	}

	/**Creates a collection resource.
	@param request The HTTP request in response to which a collection is being created.
	@param resourceURI The URI of the resource to create.
	@return The description of a newly created resource, or <code>null</code> if the resource is not allowed to be created.
	@exception IllegalArgumentException if the given resource URI does not represent a valid resource.
	@exception IOException Thrown if there is an error creating the resource.
	@exception HTTPConflictException if an intermediate collection required for creating this collection does not exist.
	@see #createResource(URI)
	*/
	protected HTTPServletResource createCollection(final HttpServletRequest request, final URI resourceURI) throws IllegalArgumentException, IOException, HTTPConflictException
	{
		throw new UnsupportedOperationException("DefaultHTTPServlet writing not yet implemented.");
	}

	/**Deletes a resource.
	@param request The HTTP request in response to which a resource is being deleted.
	@param resource The resource to delete.
	@exception IOException Thrown if the resource could not be deleted.
	*/
	protected void deleteResource(final HttpServletRequest request, final HTTPServletResource resource) throws IOException
	{
		throw new UnsupportedOperationException("DefaultHTTPServlet writing not yet implemented.");		
	}

	/**Retrieves an list of child resources of the given resource.
	@param request The HTTP request in response to which child resources are being retrieved.
	@param resource The resource for which children should be returned.
	@return A list of child resources.
	@exception IOException Thrown if there is an error retrieving the list of child resources.
	*/
	protected List<HTTPServletResource> getChildResources(final HttpServletRequest request, final HTTPServletResource resource) throws IOException
	{
		return emptyList();	//TODO implement		
//	TODO del when works  	return false;	//TODO fix, noting that getResourcePaths() seems to take a web application-relative path rather than a context-relative path
	}

	/**A resource that can return connections and other information.
	@author Garret Wilson
	*/
	protected interface HTTPServletResource extends Resource
	{

		/**Returns the full content type of the resource, including any parameters.
		@param request The HTTP request in response to which the content type is being retrieved.
		@return The full content type of the resource with any parameters, or <code>null</code> if the content type could not be determined.
		@exception IOException if there is an error getting the type of the resource.
		*/
		public ContentType getContentType(final HttpServletRequest request) throws IOException;

		/**Returns the content length of the resource.
		@param request The HTTP request in response to which the content length is being retrieved.
		@return The content length of the resource, or <code>-1</code> if the content length could not be determined.
		@exception IOException if there is an error getting the length of the resource.
		*/
		public long getContentLength(final HttpServletRequest request) throws IOException;

		/**Returns the last modification time of the resource.
		@param request The HTTP request in response to which the last modified time is being retrieved.
		@return The time of last modification as the number of milliseconds since January 1, 1970 GMT, or <code>-1</code> if the last modified date could not be determined.
		@exception IOException if there is an error getting the last modified time.
		*/
		public long getLastModified(final HttpServletRequest request) throws IOException;

		/**Returns an input stream to the resource.
		@param request The HTTP request in response to which the input stream is being retrieved.
		@return The lazily-created input stream to the resource.
		@exception IOException if there is an error getting an input stream to the resource.
		*/
		public InputStream getInputStream(final HttpServletRequest request) throws IOException;
	}

	/**A resource that has retrieves its properties, if possible, from a given RDF description.
	Recognized properties are:
	<ul>
		<li>{@value Content#TYPE_PROPERTY_URI} TODO implement</li>
		<li>{@value Content#LENGTH_PROPERTY_URI}</li>
		<li>{@value Content#MODIFIED_PROPERTY_URI}</li>
	</ul>
	@author Garret Wilson
	*/
	protected abstract static class AbstractDescriptionResource extends DefaultResource implements HTTPServletResource
	{

		/**The description of the resource.*/
		private final URFResource resourceDescription;

			/**@return The description of the resource.*/
			public URFResource getResourceDescription() {return resourceDescription;}

		/**Returns the full content type of the resource, including any parameters.
		@param request The HTTP request in response to which the content type is being retrieved.
		@return The full content type of the resource with any parameters, or <code>null</code> if the content type could not be determined.
		@exception IOException if there is an error getting the type of the resource.
		*/
		public ContentType getContentType(final HttpServletRequest request) throws IOException
		{
			return Content.getFullContentType(getResourceDescription());	//return the full content type from the description, if possible
		}

		/**Returns the content length of the resource.
		@param request The HTTP request in response to which the content length is being retrieved.
		@return The content length of the resource, or <code>-1</code> if the content length could not be determined.
		@exception IOException if there is an error getting the length of the resource.
		*/
		public long getContentLength(final HttpServletRequest request) throws IOException
		{
			return Content.getContentLength(getResourceDescription());	//return the content length from the description, if possible
		}

		/**Returns the last modification time of the resource.
		@param request The HTTP request in response to which the last modified time is being retrieved.
		@return The time of last modification as the number of milliseconds since January 1, 1970 GMT, or <code>-1</code> if the last modified date could not be determined.
		@exception IOException if there is an error getting the last modified time.
		*/
		public long getLastModified(final HttpServletRequest request) throws IOException
		{
			final URFDateTime modifiedDateTime=Content.getModified(getResourceDescription());	//get the last modified date time from the description, if that property exists
			return modifiedDateTime!=null ? modifiedDateTime.getTime() : -1;	//return the milliseconds of the time, if the time is available
		}

		/**Constructs a resource with a reference URI and resource description.
		@param referenceURI The reference URI for the new resource.
		@param resourceDescription The description of the resource.
		@exception NullPointerException if the reference URI and/or resource description is <code>null</code>.
		*/
		public AbstractDescriptionResource(final URI referenceURI, final URFResource resourceDescription)
		{
			super(checkInstance(referenceURI, "HTTP resource reference URI cannot be null."));	//construct the parent class
			this.resourceDescription=checkInstance(resourceDescription, "Resource description cannot be null.");	//save the description
		}
	
	}

	/**A resource that knows how to retrieve information from a URL.
	@author Garret Wilson
	*/
	protected abstract static class AbstractURLHTTPServletResource extends DefaultResource implements HTTPServletResource	//TODO create a cache of these resources with cached content lengths, etc.; but that would entail checking cache settings and such
	{

		/**The non-standard "content/unknown" string returned by {@link URLConnection} when the content is unknown.*/
		private final static String CONTENT_UNKNOWN_CONTENT_TYPE_STRING=ContentTypes.toString(ContentTypes.CONTENT_PRIMARY_TYPE, ContentTypes.UNKNOWN_SUBTYPE);

		/**The URL of the resource.*/
		private final URL url;

			/**@return The URL of the resource.*/
			protected final URL getURL() {return url;}

		/**The lazily-created URL connection to the resource.*/
		private URLConnection urlConnection=null;

			/**@return The lazily-created URL connection to the resource.
			@exception IOException if there is an error getting a connection to the resource.
			*/
			protected URLConnection getURLConnection() throws IOException
			{
				if(urlConnection==null)	//if we don't yet have a URL connection to the resource
				{
					final URL url=getURL();	//get the resource URL
					assert url!=null : "URL unexpectedly null.";	//TODO check elsewhere to make sure we don't create instances of non-existent resource
					urlConnection=url.openConnection();	//open a connection to the resource
				}
				return urlConnection;	//return the connection we created, or the one we already had
			}

			/**Returns the full content type of the resource, including any parameters.
			@param request The HTTP request in response to which the content type is being retrieved.
			@return The full content type of the resource with any parameters, or <code>null</code> if the content type could not be determined.
			@exception IOException if there is an error getting the type of the resource.
			*/
			public ContentType getContentType(final HttpServletRequest request) throws IOException
			{
				final String contentTypeString=getURLConnection().getContentType();	//get the content type of the URL connection
				//URLConnection returns a non-standard "content/unknown" type if the content is unknown rather than returning null as the API claims; convert this value to null
				return contentTypeString!=null && !contentTypeString.equals(CONTENT_UNKNOWN_CONTENT_TYPE_STRING) ? createContentType(contentTypeString) : null;	//return a new content type if a content type is known
			}

			/**Returns the content length of the resource.
			@param request The HTTP request in response to which the content length is being retrieved.
			@return The content length of the resource.
			@exception IOException if there is an error getting the length of the resource.
			*/
			public long getContentLength(final HttpServletRequest request) throws IOException
			{
				return getURLConnection().getContentLength();	//get a connection to the resource and return the length from the connection
			}

			/**Returns the last modification time of the resource.
			@param request The HTTP request in response to which the last modified time is being retrieved.
			@return The time of last modification as the number of milliseconds since January 1, 1970 GMT.
			@exception IOException if there is an error getting the last modified time.
			*/
			public long getLastModified(final HttpServletRequest request) throws IOException
			{
				return getURLConnection().getLastModified();	//get a connection to the resource and return the last modified information from the connection
			}

		/**The lazily-created input stream to the resource.*/
		private InputStream inputStream=null;

			/**Returns an input stream to the resource.
			@param request The HTTP request in response to which the input stream is being retrieved.
			@return The lazily-created input stream to the resource.
			@exception IOException if there is an error getting an input stream to the resource.
			*/
			public InputStream getInputStream(final HttpServletRequest request) throws IOException	//TODO do we really want to assume there's only one input stream needed to the resource? maybe; probably not
			{
				if(inputStream==null)	//if we don't yet have an input stream to the resource
				{
					inputStream=getURLConnection().getInputStream();	//get an output stream to the resource from its URL connection
				}
				return inputStream;	//return the input stream we created, or the one we already had
			}

		/**Constructs a resource with a reference URI and a URL to connect to.
		@param referenceURI The reference URI for the new resource.
		@param resourceURL The URL to use for connecting to the resource.
		@exception NullPointerException if the reference URI and/or URL <code>null</code>.
		*/
		public AbstractURLHTTPServletResource(final URI referenceURI, final URL resourceURL)
		{
			super(checkInstance(referenceURI, "HTTP resource reference URI cannot be null."));	//construct the parent class
			this.url=checkInstance(resourceURL, "HTTP resource URL cannot be null.");
		}
	}

	/**A resource with associated context-relative absolute path.
	This resource knows how to retrieve information from the servlet web path.
	@author Garret Wilson
	*/
	protected static class DefaultHTTPServletResource extends AbstractURLHTTPServletResource	//TODO create a cache of these resources with cached content lengths, etc.; but that would entail checking cache settings and such
	{

		/**The absolute path of the resource relative to the servlet context.*/
//TODO del if not needed		private final String resourceContextAbsolutePath;

			/**@public The absolute path of the resource relative to the servlet context.*/
//TODO del if not needed			public String getResourceContextAbsolutePath() {return resourceContextAbsolutePath;}

		/**Constructs a resource with a reference URI and a context-relative absolute path.
		@param referenceURI The reference URI for the new resource.
		@param resourceURL The URL to use for connecting to the resource.
		@exception NullPointerException if the reference URI and/or URL <code>null</code>.
		*/
		public DefaultHTTPServletResource(final URI referenceURI, final URL resourceURL)
		{
			super(referenceURI, resourceURL);	//construct the parent class
		}

	}
	
	/**A resource that decorates an existing resource, caching the resource bytes for later.
	This class supports multithreaded resource access.
	@author Garret Wilson
	*/
	protected static abstract class AbstractByteCacheDecoratorResource extends DefaultResource implements HTTPServletResource
	{

		/**The decorated resource.*/
		private final HTTPServletResource resource;

			/**@return The decorated resource.*/
			protected HTTPServletResource getResource() {return resource;}

		/**The bytes that constitute the resource, or <code>null</code> if the resource has not yet been retrieved.*/
		private byte[] bytes=null;

		/**Loads bytes from the requested resource.
		@param request The HTTP request in response to which the bytes are being retrieved.
		@return The bytes that constitute the resource.
		@exception IOException if there is an error retrieving the bytes.
		*/
		protected abstract byte[] loadBytes(final HttpServletRequest request) throws IOException;

		/**Returns a reference to the resource bytes.
		@param request The HTTP request in response to which the bytes are being retrieved.
		If the bytes are retrieved from the decorated resource if they haven't already been.
		@return The bytes that constitute the resource.
		@exception IOException if there is an error retrieving the bytes.
		*/
		protected byte[] getBytes(final HttpServletRequest request) throws IOException
		{
			synchronized(resource)
			{
				if(bytes==null)	//if no bytes are available
				{
					bytes=loadBytes(request);	//load the bytes
				}
				return bytes;	//return the resource bytes
			}			
		}

		/**Returns the full content type of the resource, including any parameters.
		@param request The HTTP request in response to which the content type is being retrieved.
		@return The full content type of the resource with any parameters, or <code>null</code> if the content type could not be determined.
		@exception IOException if there is an error getting the type of the resource.
		*/
		public ContentType getContentType(final HttpServletRequest request) throws IOException
		{
			return getResource().getContentType(request);	//delegate to the decorated resource
		}

		/**Returns the content length of the resource.
		@param request The HTTP request in response to which the content length is being retrieved.
		@return The content length of the resource.
		@exception IOException if there is an error getting the length of the resource.
		*/
		public long getContentLength(final HttpServletRequest request) throws IOException
		{
			return getBytes(request).length;	//return the length of bytes
		}

		/**Determines the last modification time of the resource.
		This version delegates to the decorated resource.
		@param request The HTTP request in response to which the last modified time is being retrieved.
		@return The time of last modification as the number of milliseconds since January 1, 1970 GMT.
		@exception IOException if there is an error getting the last modified time.
		*/
		public synchronized long getLastModified(final HttpServletRequest request) throws IOException
		{
			return getResource().getLastModified(request);
		}

		/**Returns an input stream to the resource.
		@param request The HTTP request in response to which the input stream is being retrieved.
		@return The lazily-created input stream to the resource.
		@exception IOException if there is an error getting an input stream to the resource.
		*/
		public InputStream getInputStream(final HttpServletRequest request) throws IOException
		{
			return new ByteArrayInputStream(getBytes(request));	//return an input stream to the bytes
		}

		/**HTTP servlet resource constructor.
		@param resource The decorated HTTP servlet resource.
		@exception IllegalArgumentException if the given resource is <code>null</code>.
		*/
		public AbstractByteCacheDecoratorResource(final HTTPServletResource resource)
		{
			super(checkInstance(resource, "Resource cannot be null.").getURI());	//construct the parent class
			this.resource=resource;	//save the decorated resource
		}
	}
	
	
	
}
