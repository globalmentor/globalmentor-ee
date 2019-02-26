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

package com.globalmentor.faces.taglib;

import javax.faces.context.FacesContext;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * Provides JSF-specific support for tag creation.
 * @author Garret Wilson
 */
public class FacesTagSupport extends TagSupport //TODO probably remove this class; we shouldn't cache the faces context, or we should know when to do it and when not
{

	/** The cached faces context. */
	private FacesContext facesContext = null;

	/** @return The lazily-created cached faces context instance. */
	protected FacesContext getFacesContext() {
		//TODO testing			if(facesContext==null)	//if we haven't yet retrieved the faces context
		{
			facesContext = FacesContext.getCurrentInstance(); //get the faces context
		}
		return facesContext; //return the cached context
	}

}
