package com.garretwilson.net.http.webdav;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

import com.garretwilson.io.FileResource;
import static com.garretwilson.io.FileUtilities.*;
import com.garretwilson.net.http.HTTPConflictException;

/**A WebDAV server that accesses resources from an underlying file system. 
@author Garret Wilson
*/
public abstract class FileWebDAVServlet extends AbstractWebDAVServlet<FileResource>	//TODO finish implementing WebDAV-specific methods
{

	//TODO fix checks for WEB-INF
	
  /**Determines if the resource at a given URI exists.
  @param resourceURI The URI of the requested resource.
  @return <code>true</code> if the resource exists, else <code>false</code>.
	@exception IOException if there is an error accessing the resource.
  */
  protected boolean exists(final URI resourceURI) throws IOException
  {
  	return getResource(resourceURI).getFile().exists();	//return whether the file exists
  }

  /**Determines if the resource at a given URI is an existing collection.
  @param resourceURI The URI of the requested resource.
  @return <code>true</code> if the resource is a collection, else <code>false</code>.
	@exception IOException if there is an error accessing the resource.
	@see #exists(URI)
  */
  protected boolean isCollection(final URI resourceURI) throws IOException
  {
  	return getResource(resourceURI).getFile().isDirectory();	//return whether the file is a directory
  }

	/**Determines the requested resource.
	@param resourceURI The URI of the requested resource.
  @return An object providing an encapsulation of the requested resource,
  	but not necessarily the contents of the resource. 
	@exception IllegalArgumentException if the given resource URI does not represent a valid resource.
	@exception IOException if there is an error accessing the resource.
  */
	protected FileResource getResource(final URI resourceURI) throws IllegalArgumentException, IOException
	{
		final String resourceContextAbsolutePath=getResourceContextAbsolutePath(resourceURI.getPath());	//get the absolute path relative to the context
		return new FileResource(new File(getServletContext().getRealPath(resourceContextAbsolutePath)), resourceURI);	//create a file to the real path in the file system
	}

	/**Determines the content length of the given resource.
	@param request The HTTP request in response to which the content length is being retrieved.
	@param resource The resource for which the content length should be determined.
	@return The content length of the given resource, or <code>-1</code> if no
		content type could be determined.
	@exception IOException Thrown if there is an error accessing the resource.
	*/
	protected long getContentLength(final HttpServletRequest request, final FileResource resource) throws IOException
	{
		return resource.getFile().length();	//return the length of the file
	}

	/**Determines the last modified date of the given resource.
	@param request The HTTP request in response to which the last modified date is being retrieved.
	@param resource The resource for which the last modified date should be determined.
	@return The last modified date of the given resource, or <code>null</code> if no there is no known last modified date.
	@exception IOException Thrown if there is an error accessing the resource.
	*/
	protected Date getLastModifiedDate(final HttpServletRequest request, final FileResource resource) throws IOException
	{
		final long lastModified=resource.getFile().lastModified();	//get the last modified information from the resource
		return lastModified>0 ? new Date(lastModified) : null;	//return the last modified date, if we have that information (File returns 0 an error)
	}

	/**Retrieves an input stream to the given resource.
	@param request The HTTP request in response to which the input stream is being retrieved.
	@param resource The resource for which an input stream should be retrieved.
	@return An input stream to the given resource.
	@exception IOException Thrown if there is an error accessing the resource,
		such as a missing file or a resource that has no contents.
	*/
	protected InputStream getInputStream(final HttpServletRequest request, final FileResource resource) throws IOException
	{
		return new FileInputStream(resource.getFile());	//return a an input stream from the file
	}

	/**Retrieves an output stream to the given resource.
	@param request The HTTP request in response to which the output stream is being retrieved.
	@param resource The resource for which an output stream should be retrieved.
	@return An output stream to the given resource.
	@exception IOException Thrown if there is an error accessing the resource.
	*/
	protected OutputStream getOutputStream(final HttpServletRequest request, final FileResource resource) throws IOException
	{
		return new FileOutputStream(resource.getFile());	//return an output stream to the file		
	}

	/**Creates a resource and returns an output stream for storing content.
	If the resource already exists, it will be replaced.
	For collections, {@link #createCollection(URI)} should be used instead.
	@param resourceURI The URI of the resource to create.
	@return An output stream for storing content in the resource.
	@exception IllegalArgumentException if the given resource URI does not represent a valid resource.
	@exception IOException Thrown if there is an error creating the resource.
	@exception HTTPConflictException if an intermediate collection required for creating this collection does not exist.
	@see #createCollection(URI)
	*/
	protected OutputStream createResource(final URI resourceURI) throws IllegalArgumentException, IOException, HTTPConflictException
	{
		final FileResource fileResource=getResource(resourceURI);	//get the resource associated with this URI
//TODO del if not needed		createNewFile(fileResource.getFile());	//create a new file
		final File file=fileResource.getFile();	//get the associated file
		if(!file.getParentFile().isDirectory())	//if the file's parent is not an existing directory
		{
			throw new HTTPConflictException();	//indicate the conflict with the parent resource TODO report the URI at some point, which is not the same as the URI of the parent file
		}
		return new FileOutputStream(file);	//return a new file output stream to the file
	}

	/**Creates a collection resource.
	@param resourceURI The URI of the resource to create.
	@return The description of a newly created resource, or <code>null</code> if the resource is not allowed to be created.
	@exception IllegalArgumentException if the given resource URI does not represent a valid resource.
	@exception IOException Thrown if there is an error creating the resource.
	@exception HTTPConflictException if an intermediate collection required for creating this collection does not exist.
	@see #createResource(URI)
	*/
	protected FileResource createCollection(final URI resourceURI) throws IllegalArgumentException, IOException, HTTPConflictException
	{
		final FileResource fileResource=getResource(resourceURI);	//get the resource associated with this URI
		final File file=fileResource.getFile();	//get the associated file
		if(!file.getParentFile().isDirectory())	//if the file's parent is not an existing directory
		{
			throw new HTTPConflictException();	//indicate the conflict with the parent resource TODO report the URI at some point, which is not the same as the URI of the parent file
		}
		mkdir(file);	//create the directory
		return fileResource;	//return the file resource		
	}

	/**Deletes a resource.
	@param resource The resource to delete.
	@exception IOException Thrown if the resource could not be deleted.
	*/
	protected void deleteResource(final FileResource resource) throws IOException
	{
		delete(resource.getFile(), true);	//recursively delete the resource
	}

	/**Retrieves an list of child resources of the given resource.
	@param resource The resource for which children should be returned.
	@return A list of child resources.
	@exception IOException Thrown if there is an error retrieving the list of child resources.
	*/
	protected List<FileResource> getChildResources(final FileResource resource) throws IOException
	{
		return resource.getChildResources();	//return the child resources of this file resource
	}

}