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

package com.globalmentor.faces.taglib;

import java.io.IOException;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.jsp.JspException;

import com.globalmentor.faces.FacesValues;

/**
 * A tag for conditionally redirecting a JSF+JSP page.
 * <p>
 * The tag accepts the following attributes:
 * </p>
 * <dl>
 * <dt>url</dt>
 * <dd>(required) The absolute URL for redirecting, resolved to the web application's context.</dd>
 * <dt>when</dt>
 * <dd>(optional) The condition for redirecting; may be a boolean string or a boolean value binding.</dd>
 * </dl>
 * @author Garret Wilson
 */
public class RedirectTag extends FacesTagSupport {

	private static final long serialVersionUID = 2071167228798654648L;

	/** @return <code>null</code> indicating that this tag does not use a renderer. */
	/*TODO del
		public String getRendererType()
		{
			return null;
		}
	*/

	/** @return The type of the component to use for this tag. */
	//TODO del	public String getComponentType() {return null;}	//TODO testing
	//TODO fix	public String getComponentType() {return UITerm.COMPONENT_TYPE;}

	/** The value of the URL to which to redirect. */
	private String url = null;

	/** @return The value of the URL to which to redirect. */
	protected String getUrl() {
		return url;
	}

	/**
	 * Sets the value of the URL to which to redirect.
	 * @param url A string representation of an absolute URL, which will be made relative to the web context.
	 */
	public void setUrl(final String url) {
		this.url = url;
	}

	/** The condition for redirecting, or <code>null</code> if redirecting should be unconditional. */
	private String when = null;

	/** @return The condition for redirecting, or <code>null</code> if redirecting should be unconditional. */
	protected String getWhen() {
		return when;
	}

	/**
	 * Sets the condition for redirecting.
	 * @param when The condition for redirecting, or <code>null</code> if redirecting should be unconditional.
	 */
	public void setWhen(final String when) {
		this.when = when;
	}

	/**
	 * Redirects to the indicated URL if the given condition holds.
	 * @throws JspException if a JSP error occurs.
	 */
	public int doStartTag() throws JspException {
		final FacesContext facesContext = getFacesContext(); //get the faces context
		final String when = getWhen(); //see when we should redirect; if there is no value, we should always redirect
		if(when == null || FacesValues.getBooleanValue(facesContext, when)) { //if we should redirect
			//TODO make sure this is an absolute URL
			final ExternalContext externalContext = facesContext.getExternalContext(); //get a context to the outside world
			try {
				externalContext.redirect(externalContext.encodeResourceURL(externalContext.getRequestContextPath() + getUrl())); //redirect to the URL, relative to the request context path TODO allow full absolute URIs, maybe
			} catch(IOException ioException) { //if there is an error
				throw new JspException(ioException); //pass the exception back
			}
		}
		return SKIP_BODY; //we have no body to render		
	}

	/** Releases references to any acquired resources. */
	public void release() {
		url = null;
		when = null;
	}
}
