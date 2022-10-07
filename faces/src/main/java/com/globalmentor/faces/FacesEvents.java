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

package com.globalmentor.faces;

import java.util.Map;

import javax.faces.context.FacesContext;
import javax.faces.event.FacesEvent;

import com.globalmentor.faces.component.FacesComponents;

/**
 * Utilities for working with JavaServer Faces events.
 * @author Garret Wilson
 */
public class FacesEvents {

	/**
	 * Retrieves the values of the first direct <code>UIParameter</code> children of the event's component with the given name.
	 * @param event The JSF event for which a parameter should be retrieved.
	 * @param context The JSF context.
	 * @param name The name of the parameter to retrieve.
	 * @return The parameter value, or <code>null</code> if no parameter with the given name exists.
	 */
	public static Object getParameter(final FacesEvent event, final FacesContext context, final String name) {
		return FacesComponents.getParameter(event.getComponent(), context, name); //get the parameters for the event's component
	}

	/**
	 * Retrieves the parameters of a given event, derived from the names and values of all direct <code>UIParameter</code> children of the component of the given
	 * component.
	 * @param event The JSF event for which parameters should be retrieved.
	 * @param context The JSF context.
	 * @return A non-<code>null</code> array of parameters.
	 */
	public static Map.Entry<String, Object>[] getParameters(final FacesEvent event, final FacesContext context) {
		return FacesComponents.getParameters(event.getComponent(), context); //get all the parameters for the event's component
	}

	/**
	 * Creates and returns a map of the names and values of all direct <code>UIParameter</code> children of the event's component. If multiple parameters with the
	 * same name exist, only the first one will be returned. Parameters with no name will be ignored.
	 * @param event The JSF event for which parameters should be retrieved.
	 * @param context The JSF context.
	 * @return A non-<code>null</code> map of parameter values, keyed to parameter names.
	 */
	public static Map<String, Object> getParameterMap(final FacesEvent event, final FacesContext context) {
		return FacesComponents.getParameterMap(event.getComponent(), context); //get the parameters for the event's component		
	}

}
