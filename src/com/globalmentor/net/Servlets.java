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

package com.globalmentor.net;

import java.io.*;

import javax.servlet.*;


import static com.globalmentor.net.URIs.*;

/**Constants and utilities for working with servlets.
@author Garret Wilson
*/
public class Servlets
{

	/**The name of the servlet "WEB-INF" directory.*/
	public final static String WEB_INF_DIRECTORY_NAME="WEB-INF";

	/**The absolute path, relative to the servlet context and ending with a slash, of the WEB-INF directory.*/
	public final static String WEB_INF_DIRECTORY_PATH=ROOT_PATH+WEB_INF_DIRECTORY_NAME+PATH_SEPARATOR;

	/**The init parameter of the data directory.*/
	public final static String DATA_DIRECTORY_INIT_PARAMETER="dataDirectory";

	/**The init parameter of the log directory.*/
	public final static String LOG_DIRECTORY_INIT_PARAMETER="logDirectory";

	/**The init parameter of the temp directory.*/
	public final static String TEMP_DIRECTORY_INIT_PARAMETER="tempDirectory";

	/**The custom parameter that can be used to override the locale with a locale string.
	@deprecated
	*/
	public final static String LOCALE_PARAMETER="locale";

	/**The custom attribute that can be used to store a specified locale object.
	@deprecated
	*/
	public final static String LOCALE_ATTRIBUTE=LOCALE_PARAMETER;

	/**The custom cookie that can be used to store a locale with a locale string.
	@deprecated
	*/
	public final static String LOCALE_COOKIE=LOCALE_PARAMETER;

	/**The absolute application context-based base directory for icons.
	@deprecated
	*/
	public final static String ICON_DIRECTORY="/images/icons/";

	/**Determines the real path of the <code>/WEB-INF</code> directory.
	@param servletContext The servlet context with which the directory is associated.
	@return A file representing the server's real path of the <code>/WEB-INF</code> directory, or <code>null</code> if the real path of the directory is not available (if the application is being served from a .war file, for instance).
	*/
	public static File getWebInfDirectory(final ServletContext servletContext)
	{
		final String webInfRealPath=servletContext.getRealPath(WEB_INF_DIRECTORY_NAME);	//get the real path to the WEB-INF directory
		return webInfRealPath!=null ? new File(webInfRealPath) : null;	//if we found the WEB-INF path, return a file to it
	}

	/**Determines the data directory.
	The data directory is determined in this order:
	<ol>
		<li>The file for the value of the context parameter {@value Servlets#DATA_DIRECTORY_INIT_PARAMETER}.</li>
		<li>The file for the real path to <code>/WEB-INF/</code>.</li>
	</ol>
	@param context The servlet context from which to retrieve init parameters.
	@return A file representing the preferred data directory, or <code>null</code> if the directory is not available (if a <code>WEB-INF</code>-based directory is requested and the application is being served from a .war file, for instance).
	@see #getWebInfDirectory(ServletContext)
	*/
	public static File getDataDirectory(final ServletContext context)
	{
		final String path=context.getInitParameter(DATA_DIRECTORY_INIT_PARAMETER);	//get the init parameter, if there is one
		return path!=null ? new File(path) : getWebInfDirectory(context);	//return a file for the path, or the default WEB-INF-based path
	}

	/**Determines the correct data subdirectory.
	The data directory is determined in this order:
	<ol>
		<li>The file for the value of the given init parameter.</li>
		<li>The file for the default child directory of the data directory.</li>
	</ol>
	@param context The servlet context from which to retrieve init parameters.
	@param initParameter The init parameter containing the directory name
	@param defaultDirectoryName The default name to use for the directory if there is no init parameter value.
	@return A file representing the preferred log directory, or <code>null</code> if the directory is not available (if a <code>WEB-INF</code>-based directory is requested and the application is being served from a .war file, for instance).
	@see #getDataDirectory(ServletContext)
	*/
	public static File getDataDirectory(final ServletContext context, final String initParameter, final String defaultDirectoryName)
	{
		final String path=context.getInitParameter(initParameter);	//get the init parameter, if there is one
		if(path!=null)	//if there is an init parameter
		{
			return new File(path);	//return a file for the path
		}
		else	//if there is no init parameter
		{
			final File dataDirectory=getDataDirectory(context);	//get the data directory
			return dataDirectory!=null ? new File(dataDirectory, defaultDirectoryName) : null;	//return a file for the path based upon the data directory, or null if there is no data directory
		}	
	}

	/**Determines the log directory.
	The log directory is determined in this order:
	<ol>
		<li>The file for the value of the context parameter {@value Servlets#LOG_DIRECTORY_INIT_PARAMETER}.</li>
		<li>The file for the real path to <code><var>dataDirectory</var>/logs/</code>.</li>
	</ol>
	@param context The servlet context from which to retrieve init parameters.
	@return A file representing the preferred log directory, or <code>null</code> if the directory is not available (if a <code>WEB-INF</code>-based directory is requested and the application is being served from a .war file, for instance).
	@see #getDataDirectory(ServletContext, String, String)
	*/
	public static File getLogDirectory(final ServletContext context)
	{
		return getDataDirectory(context, LOG_DIRECTORY_INIT_PARAMETER, "logs");	//find this data directory 
	}

	/**Determines the temp directory.
	The log directory is determined in this order:
	<ol>
		<li>The file for the value of the context parameter {@value Servlets#TEMP_DIRECTORY_INIT_PARAMETER}.</li>
		<li>The file for the real path to <code><var>dataDirectory</var>/temp/</code>.</li>
	</ol>
	@param context The servlet context from which to retrieve init parameters.
	@return A file representing the preferred temp directory, or <code>null</code> if the directory is not available (if a <code>WEB-INF</code>-based directory is requested and the application is being served from a .war file, for instance).
	@see #getDataDirectory(ServletContext, String, String)
	*/
	public static File getTempDirectory(final ServletContext context)
	{
		return getDataDirectory(context, TEMP_DIRECTORY_INIT_PARAMETER, "temp");	//find this data directory 
	}

	/**Determines the path to an icon based upon its ID.
	@param iconID The ID of the icon; currently this must be its simple filename.
	@return The absolute application context-based path to the icon.
	@throws IllegalArgumentException if the icon ID contains the path separator,
		as this could cause security issues be walking the directory.
	@deprecated
	*/ 
	public static String getIconPath(final String iconID)
	{
		if(iconID.indexOf(URIs.PATH_SEPARATOR)>=0)	//if the ID contains a path separator
		{
			throw new IllegalArgumentException("Icon ID may not contain the path separator: "+iconID); 
		}
		return ICON_DIRECTORY+iconID;	//return the icon ID as a file in the icon directory
	}

}