/*
 * Copyright © 1996-2008 GlobalMentor, Inc. <https://www.globalmentor.com/>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.globalmentor.servlet.http.webdav;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

import com.globalmentor.io.FileResource;
import static com.globalmentor.io.Files.*;
import com.globalmentor.net.http.HTTPConflictException;

/**
 * A WebDAV server that accesses resources from an underlying file system.
 * @author Garret Wilson
 */
public abstract class FileWebDAVServlet extends AbstractWebDAVServlet<FileResource> { //TODO finish implementing WebDAV-specific methods

	private static final long serialVersionUID = 8114228458331897082L;

	//TODO fix checks for WEB-INF

	/**
	 * Determines if the resource at a given URI exists.
	 * @param request The HTTP request in response to which existence of the resource is being determined.
	 * @param resourceURI The URI of the requested resource.
	 * @return <code>true</code> if the resource exists, else <code>false</code>.
	 * @throws IOException if there is an error accessing the resource.
	 */
	protected boolean exists(final HttpServletRequest request, final URI resourceURI) throws IOException {
		return getResource(resourceURI).getFile().exists(); //return whether the file exists
	}

	/**
	 * Determines if the resource at a given URI is an existing collection.
	 * @param request The HTTP request in response to which the collection is being checked.
	 * @param resourceURI The URI of the requested resource.
	 * @return <code>true</code> if the resource is a collection, else <code>false</code>.
	 * @throws IOException if there is an error accessing the resource.
	 * @see #exists(HttpServletRequest, URI)
	 */
	protected boolean isCollection(final HttpServletRequest request, final URI resourceURI) throws IOException {
		return getResource(resourceURI).getFile().isDirectory(); //return whether the file is a directory
	}

	/**
	 * Determines the requested resource.
	 * @param resourceURI The URI of the requested resource.
	 * @return An object providing an encapsulation of the requested resource, but not necessarily the contents of the resource.
	 * @throws IllegalArgumentException if the given resource URI does not represent a valid resource.
	 * @throws IOException if there is an error accessing the resource.
	 */
	protected FileResource getResource(final URI resourceURI) throws IllegalArgumentException, IOException {
		final String resourceContextAbsolutePath = getResourceContextAbsolutePath(resourceURI.getPath()); //get the absolute path relative to the context
		return new FileResource(new File(getServletContext().getRealPath(resourceContextAbsolutePath)), resourceURI); //create a file to the real path in the file system
	}

	/**
	 * Determines the content length of the given resource.
	 * @param request The HTTP request in response to which the content length is being retrieved.
	 * @param resource The resource for which the content length should be determined.
	 * @return The content length of the given resource, or <code>-1</code> if no content type could be determined.
	 * @throws IOException Thrown if there is an error accessing the resource.
	 */
	protected long getContentLength(final HttpServletRequest request, final FileResource resource) throws IOException {
		return resource.getFile().length(); //return the length of the file
	}

	/**
	 * Determines the last modified date of the given resource.
	 * @param request The HTTP request in response to which the last modified date is being retrieved.
	 * @param resource The resource for which the last modified date should be determined.
	 * @return The last modified date of the given resource, or <code>null</code> if no there is no known last modified date.
	 * @throws IOException Thrown if there is an error accessing the resource.
	 */
	protected Date getLastModifiedDate(final HttpServletRequest request, final FileResource resource) throws IOException {
		final long lastModified = resource.getFile().lastModified(); //get the last modified information from the resource
		return lastModified > 0 ? new Date(lastModified) : null; //return the last modified date, if we have that information (File returns 0 an error)
	}

	/**
	 * Retrieves an input stream to the given resource.
	 * @param request The HTTP request in response to which the input stream is being retrieved.
	 * @param resource The resource for which an input stream should be retrieved.
	 * @return An input stream to the given resource.
	 * @throws IOException Thrown if there is an error accessing the resource, such as a missing file or a resource that has no contents.
	 */
	protected InputStream getInputStream(final HttpServletRequest request, final FileResource resource) throws IOException {
		return new FileInputStream(resource.getFile()); //return a an input stream from the file
	}

	/**
	 * Retrieves an output stream to the given resource.
	 * @param request The HTTP request in response to which the output stream is being retrieved.
	 * @param resource The resource for which an output stream should be retrieved.
	 * @return An output stream to the given resource.
	 * @throws IOException Thrown if there is an error accessing the resource.
	 */
	protected OutputStream getOutputStream(final HttpServletRequest request, final FileResource resource) throws IOException {
		return new FileOutputStream(resource.getFile()); //return an output stream to the file		
	}

	/**
	 * Creates a resource and returns an output stream for storing content. If the resource already exists, it will be replaced. For collections,
	 * {@link #createCollection(HttpServletRequest, URI)} should be used instead.
	 * @param request The HTTP request in response to which a resource is being created.
	 * @param resourceURI The URI of the resource to create.
	 * @return An output stream for storing content in the resource.
	 * @throws IllegalArgumentException if the given resource URI does not represent a valid resource.
	 * @throws IOException Thrown if there is an error creating the resource.
	 * @throws HTTPConflictException if an intermediate collection required for creating this collection does not exist.
	 * @see #createCollection(HttpServletRequest, URI)
	 */
	protected OutputStream createResource(final HttpServletRequest request, final URI resourceURI)
			throws IllegalArgumentException, IOException, HTTPConflictException {
		final FileResource fileResource = getResource(resourceURI); //get the resource associated with this URI
		//TODO del if not needed		createNewFile(fileResource.getFile());	//create a new file
		final File file = fileResource.getFile(); //get the associated file
		if(!file.getParentFile().isDirectory()) { //if the file's parent is not an existing directory
			throw new HTTPConflictException(); //indicate the conflict with the parent resource TODO report the URI at some point, which is not the same as the URI of the parent file
		}
		return new FileOutputStream(file); //return a new file output stream to the file
	}

	/**
	 * Creates a collection resource.
	 * @param request The HTTP request in response to which a collection is being created.
	 * @param resourceURI The URI of the resource to create.
	 * @return The description of a newly created resource, or <code>null</code> if the resource is not allowed to be created.
	 * @throws IllegalArgumentException if the given resource URI does not represent a valid resource.
	 * @throws IOException Thrown if there is an error creating the resource.
	 * @throws HTTPConflictException if an intermediate collection required for creating this collection does not exist.
	 * @see #createCollection(HttpServletRequest, URI)
	 */
	protected FileResource createCollection(final HttpServletRequest request, final URI resourceURI)
			throws IllegalArgumentException, IOException, HTTPConflictException {
		final FileResource fileResource = getResource(resourceURI); //get the resource associated with this URI
		final File file = fileResource.getFile(); //get the associated file
		if(!file.getParentFile().isDirectory()) { //if the file's parent is not an existing directory
			throw new HTTPConflictException(); //indicate the conflict with the parent resource TODO report the URI at some point, which is not the same as the URI of the parent file
		}
		mkdir(file); //create the directory
		return fileResource; //return the file resource		
	}

	/**
	 * Deletes a resource.
	 * @param request The HTTP request in response to which a resource is being deleted.
	 * @param resource The resource to delete.
	 * @throws IOException Thrown if the resource could not be deleted.
	 */
	protected void deleteResource(final HttpServletRequest request, final FileResource resource) throws IOException {
		delete(resource.getFile(), true); //recursively delete the resource
	}

	/**
	 * Retrieves an list of child resources of the given resource.
	 * @param request The HTTP request in response to which a resource is being created.
	 * @param resource The resource for which children should be returned.
	 * @return A list of child resources.
	 * @throws IOException Thrown if there is an error retrieving the list of child resources.
	 */
	protected List<FileResource> getChildResources(final HttpServletRequest request, final FileResource resource) throws IOException {
		return resource.getChildResources(); //return the child resources of this file resource
	}

}
