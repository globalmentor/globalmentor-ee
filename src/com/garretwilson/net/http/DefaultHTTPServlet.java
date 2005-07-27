package com.garretwilson.net.http;

import java.io.*;
import java.net.*;
import java.util.*;
import static java.util.Collections.*;

import static com.garretwilson.lang.ObjectUtilities.*;
import com.garretwilson.model.DefaultResource;
import com.garretwilson.net.URIUtilities;
import com.garretwilson.util.Debug;

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
  @param resourceURI The URI of the requested resource.
  @return <code>true</code> if the resource exists, else <code>false</code>.
	@exception IOException if there is an error accessing the resource.
  */
  protected boolean exists(final URI resourceURI) throws IOException
  {
		final String resourceContextAbsolutePath=getResourceContextAbsolutePath(resourceURI.getPath());	//get the absolute path relative to the context
  	return getServletContext().getResource(resourceContextAbsolutePath)!=null;	//return whether the servlet has mapped a resource to this path
  }

  /**Determines if the resource at a given URI is an existing collection.
  @param resourceURI The URI of the requested resource.
  @return <code>true</code> if the resource is a collection, else <code>false</code>.
	@exception IOException if there is an error accessing the resource.
	@see #exists(URI)
  */
  protected boolean isCollection(final URI resourceURI) throws IOException
  {
		final String resourceContextAbsolutePath=getResourceContextAbsolutePath(resourceURI.getPath());	//get the absolute path relative to the context
		return URIUtilities.isContainerPath(resourceContextAbsolutePath) && exists(resourceURI);	//return whether the context absolute path ends in a slash and the resource exists
  }

	/**Determines the requested resource.
	@param resourceURI The URI of the requested resource.
  @return An object providing an encapsulation of the requested resource,
  	but not necessarily the contents of the resource. 
	@exception IllegalArgumentException if the given resource URI does not represent a valid resource.
	@exception IOException if there is an error accessing the resource.
  */
	protected HTTPServletResource getResource(final URI resourceURI) throws IllegalArgumentException, IOException
	{
		final String resourceContextAbsolutePath=getResourceContextAbsolutePath(resourceURI.getPath());	//get the absolute path relative to the context
		return new HTTPServletResource(resourceURI, resourceContextAbsolutePath);	//create a new default resource
	}

	/**Determines the content length of the given resource.
	@param resource The resource for which the content length should be determined.
	@return The content length of the given resource, or <code>-1</code> if no
		content type could be determined.
	@exception IOException Thrown if there is an error accessing the resource;
	*/
	protected long getContentLength(final HTTPServletResource resource) throws IOException
	{
		return resource.getContentLength();	//return the content length of the resource
	}

	/**Retrieves an input stream to the given resource.
	@param resource The resource for which an input stream should be retrieved.
	@return An input stream to the given resource.
	@exception IOException Thrown if there is an error accessing the resource,
		such as a missing file or a resource that has no contents.
	*/
	protected InputStream getInputStream(final HTTPServletResource resource) throws IOException
	{
		return resource.getInputStream();	//return the input stream to the resource, creating one if we haven't yet done so
	}

	/**Retrieves an output stream to the given resource.
	@param resource The resource for which an output stream should be retrieved.
	@return An output stream to the given resource.
	@exception IOException Thrown if there is an error accessing the resource.
	*/
	protected OutputStream getOutputStream(final HTTPServletResource resource) throws IOException
	{
		throw new UnsupportedOperationException("DefaultHTTPServlet writing not yet implemented.");
	}

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
	protected HTTPServletResource createResource(final URI resourceURI) throws IllegalArgumentException, IOException, HTTPConflictException
	{
		throw new UnsupportedOperationException("DefaultHTTPServlet writing not yet implemented.");
	}

	/**Creates a collection resource.
	@param resourceURI The URI of the resource to create.
	@return The description of a newly created resource, or <code>null</code> if the resource is not allowed to be created.
	@exception IllegalArgumentException if the given resource URI does not represent a valid resource in a valid burrow.
	@exception IOException Thrown if there is an error creating the resource.
	@exception HTTPConflictException if an intermediate collection required for creating this collection does not exist.
	@see #createResource(URI)
	*/
	protected HTTPServletResource createCollection(final URI resourceURI) throws IllegalArgumentException, IOException, HTTPConflictException
	{
		throw new UnsupportedOperationException("DefaultHTTPServlet writing not yet implemented.");
	}

	/**Deletes a resource.
	@param resource The resource to delete.
	@exception IOException Thrown if the resource could not be deleted.
	*/
	protected void deleteResource(final HTTPServletResource resource) throws IOException
	{
		throw new UnsupportedOperationException("DefaultHTTPServlet writing not yet implemented.");		
	}

	/**Retrieves an list of child resources of the given resource.
	@param resource The resource for which children should be returned.
	@return A list of child resources.
	@exception IOException Thrown if there is an error retrieving the list of child resources.
	*/
	protected List<HTTPServletResource> getChildResources(final HTTPServletResource resource) throws IOException
	{
		return emptyList();	//TODO implement		
//	TODO del when works  	return false;	//TODO fix, noting that getResourcePaths() seems to take a web application-relative path rather than a context-relative path
	}

	/**A resource with associated context-relative absolute path.
	@author Garret Wilson
	*/
	protected class HTTPServletResource extends DefaultResource	//TODO create a cache of these resources with cached content lengths, etc.; but that would entail checking cache settings and such
	{

		/**The absolute path of the resource relative to the servlet context.*/
		private final String resourceContextAbsolutePath;

			/**@public The absolute path of the resource relative to the servlet context.*/
			public String getResourceContextAbsolutePath() {return resourceContextAbsolutePath;}

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

		/**The content length of the resource, or -1 if the length has not yet been initialized.*/
		private long contentLength=-1;

			/**@return The content length of the resource.
			@exception IOException if there is an error getting the length of the resource.
			*/
			public long getContentLength() throws IOException
			{
				return getURLConnection().getContentLength();	//get a connection to the resource and return the length from the connection
			}

		/**The lazily-created input stream to the resource.*/
		private InputStream inputStream=null;

			/**@return The lazily-created input stream to the resource.
			@exception IOException if there is an error getting an input stream to the resource.
			*/
			public InputStream getInputStream() throws IOException	//TODO do we really want to assume there's only one input stream needed to the resource? maybe; probably not
			{
				if(inputStream==null)	//if we don't yet have an input stream to the resource
				{
					inputStream=getURLConnection().getInputStream();	//get an output stream to the resource from its URL connection
				}
				return inputStream;	//return the input stream we created, or the one we already had
			}

		/**Constructs a resource with a reference URI and a context-relative absolute path.
		@param referenceURI The reference URI for the new resource.
		@param resourceContextAbsolutePath The absolute path of the resource relative to the servlet context.
		@exception NullPointerException if the reference URI or path is <code>null</code>.
		@exception IllegalArgumentException if the given path is not in the correct form.
		*/
		public HTTPServletResource(final URI referenceURI, final String resourceContextAbsolutePath)
		{
			super(checkNull(referenceURI, "HTTP resource reference URI cannot be null."));	//construct the parent class
			this.resourceContextAbsolutePath=checkNull(resourceContextAbsolutePath, "HTTP resource context-relative absolute path cannot be null.");	//save the path
			try
			{
				url=getServletContext().getResource(getResourceContextAbsolutePath());	//get the URL of the resource
			}
			catch(final MalformedURLException malformedURLException)	//if the path was not well-formed
			{
				throw new IllegalArgumentException(malformedURLException);
			}
		}

	}

}
