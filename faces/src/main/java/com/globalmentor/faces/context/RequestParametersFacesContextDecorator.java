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

package com.globalmentor.faces.context;

import java.util.*;
import javax.faces.context.*;

/**
 * Decorates an existing JSF context by adding request parameters to the external context.
 * @author Garret Wilson
 */
public class RequestParametersFacesContextDecorator extends FacesContextDecorator {

	/** The external context decorator. */
	private final ExternalContext externalContextDecorator;

	/** @return The external context decorator. */
	protected ExternalContext getExternalContextDecorator() {
		return externalContextDecorator;
	}

	/** The map of request parameters, both those from the external context and those added. */
	private final Map<String, Object> allRequestParameterMap;

	/** @return The map of request parameters, both those from the external context and those added. */
	protected Map<String, Object> getAllRequestParameterMap() {
		return allRequestParameterMap;
	}

	/**
	 * Adds a request parameter to the context.
	 * @param name The request parameter name.
	 * @param value The request parameter value.
	 */
	public void putRequestParameter(final String name, final Object value) {
		allRequestParameterMap.put(name, value); //store this new parameter in our map
	}

	/**
	 * Creates a decorator for an existing JSF context.
	 * @param facesContext The JSF context being decorated.
	 */
	public RequestParametersFacesContextDecorator(final FacesContext facesContext) {
		super(facesContext); //construct the parent class
		allRequestParameterMap = new HashMap<String, Object>(); //create our own map of request parameters
		//add all the existing parameters to our map
		allRequestParameterMap.putAll(facesContext.getExternalContext().getRequestParameterMap());
		//create our own external context decorator that knows how to return all the parameters
		externalContextDecorator = new RequestParametersExternalContextDecorator(facesContext.getExternalContext());
	}

	@Override
	public ExternalContext getExternalContext() {
		super.getExternalContext(); //call the super method to make sure we throw an illegal state exception if needed
		return getExternalContextDecorator(); //return our external context decorator
	}

	/**
	 * Decorates an existing JSF context by adding request parameters to the external context.
	 * @author Garret Wilson
	 */
	protected class RequestParametersExternalContextDecorator extends ExternalContextDecorator {

		/**
		 * Creates a decorator for an existing external context.
		 * @param externalContext The external context being decorated.
		 */
		public RequestParametersExternalContextDecorator(final ExternalContext externalContext) {
			super(externalContext); //save the decorated external context
		}

		@Override
		public Map getRequestParameterMap() {
			//TODO fix; this was implemented before generics, and may depend on being able to pass objects via putRequestParameter() above
			return Collections.unmodifiableMap(getAllRequestParameterMap()); //return an unmodifiable copy of inclusive request parameter map
		}

		@Override
		public Iterator<String> getRequestParameterNames() {
			return getRequestParameterMap().keySet().iterator(); //return an iterator to the request parameter names
		}

		/**
		 * <p>
		 * Return an immutable <code>Map</code> whose keys are the set of request parameters names included in the current request, and whose values (of type
		 * String[]) are all of the values for each parameter name returned by the underlying request. The returned <code>Map</code> must implement the entire
		 * contract for an unmodifiable map as described in the JavaDocs for <code>java.util.Map</code>.
		 * </p>
		 *
		 * <p>
		 * <em>Servlet:</em> This must be the set of parameters available via the <code>javax.servlet.ServletRequest</code> methods
		 * <code>getParameterValues()</code> and <code>getParameterNames()</code>.
		 * </p>
		 *
		 * <p>
		 * <em>Portlet:</em> This must be the set of parameters available via the <code>javax.portlet.PortletRequest</code> methods
		 * <code>getParameterValues()</code> and <code>getParameterNames()</code>.
		 * </p>
		 */
		/*TODO fix
		public Map getRequestParameterValuesMap()
				{
					return getRequestParameterMap().values().iterator();	//return an iterator to the request parameter values
				}
		*/

	}
}
