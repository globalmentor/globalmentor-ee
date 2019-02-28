/*
 * Copyright © 2012 GlobalMentor, Inc. <http://www.globalmentor.com/>
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

package com.globalmentor.servlet.http;

import static com.globalmentor.java.Characters.*;
import static com.globalmentor.java.Conditions.*;
import static com.globalmentor.servlet.http.HTTPServlets.*;
import static com.globalmentor.text.TextFormatter.*;
import static java.nio.charset.StandardCharsets.*;
import static java.util.Collections.*;

import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import com.globalmentor.collections.*;
import com.globalmentor.javascript.JSON;
import com.globalmentor.net.HTTP;
import com.globalmentor.text.Text;
import com.globalmentor.util.PropertiesUtilities;
import com.globalmentor.xml.spec.XML;

/**
 * A servlet that acts as a property/value storage.
 * 
 * <p>
 * This servlet allows a central storage of properties and values. It functions as a controller that uses a separate web page as its view. This implementation
 * provides no authentication, and maintains the same property store for all sessions. The properties and values are not persisted between server restarts.
 * </p>
 * 
 * <p>
 * The servlet is configured with a list of property names that are supported. When a <code>POST</code> request is sent to the servlet, all of the properties
 * with values supplied in the body are updated in the store. If multiple values are supplied for the same property, they are provided as a single
 * comma-separate value. If an unsupported property name is sent, {@value HTTP#SC_BAD_REQUEST} is returned. The most recent values are then returned in the body
 * of the response, according to the accepted content type of the client. Supported content types are:
 * </p>
 * <dl>
 * <dt>{@link Text#PLAIN_CONTENT_TYPE}</dt>
 * <dd>(default) Returns the properties in line-separated <code><var>property</var>=<var>value</var>CRLF</code> format using the UTF-8 charset.</dd>
 * <dt>{@link JSON#CONTENT_TYPE}</dt>
 * <dd>Returns the properties in JSON using the UTF-8 charset.</dd>
 * <dt>{@link XML#CONTENT_TYPE}</dt>
 * <dd>Returns the properties in Java {@link Properties} XML format using the UTF-8 charset.</dd>
 * </dl>
 * 
 * <p>
 * This implementation allows concurrent access and guarantees atomic property updates for each request.
 * </p>
 * 
 * @author Garret Wilson
 */
public class PropertyStoreServlet extends BaseHTTPServlet {

	/** The maximum length for a property value, 1024. This may be configurable in a future version. */
	public static final int MAX_PROPERTY_VALUE_LENGTH = 1 << 10;

	/** The comma-separated list of supported property names. */
	public final String PROPERTY_NAMES_INIT_PARAMETER = "propertyNames";

	/** The supported property names. */
	private final Set<String> supportedPropertyNames = new HashSet<String>();

	/** @return The supported property names. */
	protected Set<String> getSupportedPropertyNames() {
		return unmodifiableSet(supportedPropertyNames);
	}

	/**
	 * {@inheritDoc} This version reads and stores the supported property names.
	 * @throws IllegalStateException if the {@link #PROPERTY_NAMES_INIT_PARAMETER} init parameter does not exist.
	 */
	@Override
	public void initialize(ServletConfig servletConfig) throws ServletException, IllegalArgumentException, IllegalStateException {
		super.initialize(servletConfig);
		final String propertyNames = servletConfig.getInitParameter(PROPERTY_NAMES_INIT_PARAMETER);
		checkState(propertyNames != null, "No property names configuration.");
		addAll(this.supportedPropertyNames, propertyNames.split(String.valueOf(COMMA_CHAR))); //split out the property names and add them to our set
	}

	/**
	 * The map of properties and values. The map allows concurrent reads. Writing locks the map, and longer transactions may lock the map by first retrieving a
	 * write lock.
	 */
	private final ReadWriteLockMap<String, String> propertyMap = new DecoratorReadWriteLockMap<String, String>(new HashMap<String, String>());

	/** @return The map of properties and values. */
	protected ReadWriteLockMap<String, String> getPropertyMap() {
		return propertyMap;
	}

	/**
	 * Updates the properties from the given request.
	 * 
	 * @param request The servlet request potentially with property name/value pairs in its URL or POST body.
	 * @throws IllegalArgumentException if the one or more of the specified properties are not supported.
	 */
	protected void updateProperties(final HttpServletRequest request) {
		final Set<String> supportedPropertyNames = getSupportedPropertyNames(); //get the property names we support
		final Map<String, String> propertyUpdate = new HashMap<String, String>(); //create a map for updating the properties
		for(final Map.Entry<String, String[]> parameterEntry : request.getParameterMap().entrySet()) { //look at all the entries
			final String propertyName = parameterEntry.getKey();
			if(!supportedPropertyNames.contains(propertyName)) { //make sure we support this property
				throw new IllegalArgumentException("Unsupported property: " + propertyName);
			}
			propertyUpdate.put(propertyName, formatList(COMMA_CHAR, parameterEntry.getValue())); //combine the values into a single comma-separated value and store it
		}
		getPropertyMap().putAll(propertyUpdate); //update the property map with the new values; this is a thread-safe, atomic operation 
	}

	/**
	 * {@inheritDoc} This version stores all posted properties, and then delegates to {@link #doGet(HttpServletRequest, HttpServletResponse)}.
	 * @throws IllegalArgumentException if the one or more of the specified properties are not supported.
	 */
	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		updateProperties(request); //update the properties as requested
		doGet(request, response); //output the current values
	}

	/**
	 * {@inheritDoc} This version returns all currently stored properties in the requested format.
	 */
	@Override
	protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
		setNoCache(request, response); //turn off caching of the response
		final ReadWriteLockMap<String, String> propertyMap = getPropertyMap();
		propertyMap.readLock().lock(); //lock our property map for reading
		try {
			//see what content type is accepted by the client
			if(isAcceptedContentType(request, JSON.CONTENT_TYPE)) { //JSON
				response.setContentType(JSON.CONTENT_TYPE.toString()); //use JSON
				response.setCharacterEncoding(UTF_8.name()); //encode in UTF-8
				final Writer writer = response.getWriter(); //get a writer to return the response
				JSON.appendValue(writer, propertyMap); //append the property map to the output
			} else if(isAcceptedContentType(request, XML.CONTENT_TYPE)) { //XML
				response.setContentType(XML.CONTENT_TYPE.toString()); //use JSON
				response.setCharacterEncoding(UTF_8.name()); //encode in UTF-8
				final OutputStream outputStream = response.getOutputStream(); //get an output stream to return the response
				final Properties properties = PropertiesUtilities.toProperties(propertyMap); //convert the map to a properties object
				properties.storeToXML(outputStream, null, UTF_8.name());
			} else { //plain text (default)
				response.setContentType(Text.PLAIN_CONTENT_TYPE.toString()); //use text/plain
				response.setCharacterEncoding(UTF_8.toString()); //encode in UTF-8
				final Writer writer = response.getWriter(); //get a writer to return the response
				for(final Map.Entry<String, String> propertyEntry : propertyMap.entrySet()) {
					writer.append(propertyEntry.getKey()).append(EQUALS_SIGN_CHAR).append(propertyEntry.getValue()).append(CARRIAGE_RETURN_CHAR).append(LINE_FEED_CHAR); //property=valueCRLF

				}
			}
		} finally {
			propertyMap.readLock().unlock(); //always release our read lock
		}
	}
}
