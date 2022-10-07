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

package com.globalmentor.faces.context;

import java.util.Iterator;
import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.component.UIViewRoot;
import javax.faces.context.*;
import javax.faces.render.RenderKit;

/**
 * Class for decorating an existing JSF context.
 * @author Garret Wilson
 */
public class FacesContextDecorator extends FacesContext {

	/** The context being decorated. */
	private final FacesContext facesContext;

	/** @return The context being decorated. */
	protected FacesContext getFacesContext() {
		return facesContext;
	}

	/**
	 * Creates a decorator for an existing JSF context.
	 * @param facesContext The JSF context being decorated.
	 */
	public FacesContextDecorator(final FacesContext facesContext) {
		this.facesContext = facesContext; //save the decorated external context
	}

	@Override
	public Application getApplication() {
		return getFacesContext().getApplication();
	}

	@Override
	public Iterator<String> getClientIdsWithMessages() {
		return getFacesContext().getClientIdsWithMessages();
	}

	@Override
	public ExternalContext getExternalContext() {
		return getFacesContext().getExternalContext();
	}

	@Override
	public Severity getMaximumSeverity() {
		return getFacesContext().getMaximumSeverity();
	}

	@Override
	public Iterator<FacesMessage> getMessages() {
		return getFacesContext().getMessages();
	}

	@Override
	public Iterator<FacesMessage> getMessages(String clientId) {
		return getFacesContext().getMessages(clientId);
	}

	@Override
	public RenderKit getRenderKit() {
		return getFacesContext().getRenderKit();
	}

	@Override
	public boolean getRenderResponse() {
		return getFacesContext().getRenderResponse();
	}

	@Override
	public boolean getResponseComplete() {
		return getFacesContext().getResponseComplete();
	}

	@Override
	public ResponseStream getResponseStream() {
		return getFacesContext().getResponseStream();
	}

	@Override
	public void setResponseStream(ResponseStream responseStream) {
		getFacesContext().setResponseStream(responseStream);
	}

	@Override
	public ResponseWriter getResponseWriter() {
		return getFacesContext().getResponseWriter();
	}

	@Override
	public void setResponseWriter(ResponseWriter responseWriter) {
		getFacesContext().setResponseWriter(responseWriter);
	}

	@Override
	public UIViewRoot getViewRoot() {
		return getFacesContext().getViewRoot();
	}

	@Override
	public void setViewRoot(UIViewRoot root) {
		getFacesContext().setViewRoot(root);
	}

	// ---------------------------------------------------------- Public Methods

	@Override
	public void addMessage(String clientId, FacesMessage message) {
		getFacesContext().addMessage(clientId, message);
	}

	@Override
	public void release() {
		getFacesContext().release();
	}

	@Override
	public void renderResponse() {
		getFacesContext().renderResponse();
	}

	@Override
	public void responseComplete() {
		getFacesContext().responseComplete();
	}

}
