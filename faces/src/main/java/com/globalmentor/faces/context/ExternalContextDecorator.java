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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Principal;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.Map;

import javax.faces.context.*;

/**
 * Class for decorating an existing JSF external context.
 * @author Garret Wilson
 */
public class ExternalContextDecorator extends ExternalContext {

	/** The external context being decorated. */
	private final ExternalContext externalContext;

	/** @return The external context being decorated. */
	protected ExternalContext getExternalContext() {
		return externalContext;
	}

	/**
	 * Creates a decorator for an existing external context.
	 * @param externalContext The external context being decorated.
	 */
	public ExternalContextDecorator(final ExternalContext externalContext) {
		this.externalContext = externalContext; //save the decorated external context
	}

	@Override
	public void dispatch(String path) throws IOException {
		getExternalContext().dispatch(path);
	}

	@Override
	public String encodeActionURL(String url) {
		return getExternalContext().encodeActionURL(url);
	}

	@Override
	public String encodeNamespace(String name) {
		return getExternalContext().encodeNamespace(name);
	}

	@Override
	public String encodeResourceURL(String url) {
		return getExternalContext().encodeResourceURL(url);
	}

	@Override
	public String encodeWebsocketURL(String url) {
		return getExternalContext().encodeWebsocketURL(url);
	}

	@Override
	public Map<String, Object> getApplicationMap() {
		return getExternalContext().getApplicationMap();
	}

	@Override
	public String getAuthType() {
		return getExternalContext().getAuthType();
	}

	@Override
	public Object getContext() {
		return getExternalContext().getContext();
	}

	@Override
	public String getInitParameter(String name) {
		return getExternalContext().getInitParameter(name);
	}

	public Map<String, String> getInitParameterMap() {
		return getExternalContext().getInitParameterMap();
	}

	@Override
	public String getRemoteUser() {
		return getExternalContext().getRemoteUser();
	}

	@Override
	public Object getRequest() {
		return getExternalContext().getRequest();
	}

	@Override
	public String getRequestContextPath() {
		return getExternalContext().getRequestContextPath();
	}

	@Override
	public Map<String, Object> getRequestCookieMap() {
		return getExternalContext().getRequestCookieMap();
	}

	@Override
	public Map<String, String> getRequestHeaderMap() {
		return getExternalContext().getRequestHeaderMap();
	}

	@Override
	public Map<String, String[]> getRequestHeaderValuesMap() {
		return getExternalContext().getRequestHeaderValuesMap();
	}

	@Override
	public Locale getRequestLocale() {
		return getExternalContext().getRequestLocale();
	}

	@Override
	public Iterator<Locale> getRequestLocales() {
		return getExternalContext().getRequestLocales();
	}

	@Override
	public Map<String, Object> getRequestMap() {
		return getExternalContext().getRequestMap();
	}

	@Override
	public Map<String, String> getRequestParameterMap() {
		return getExternalContext().getRequestParameterMap();
	}

	@Override
	public Iterator<String> getRequestParameterNames() {
		return getExternalContext().getRequestParameterNames();
	}

	@Override
	public Map<String, String[]> getRequestParameterValuesMap() {
		return getExternalContext().getRequestParameterValuesMap();
	}

	@Override
	public String getRequestPathInfo() {
		return getExternalContext().getRequestPathInfo();
	}

	@Override
	public String getRequestServletPath() {
		return getExternalContext().getRequestServletPath();
	}

	@Override
	public URL getResource(String path) throws MalformedURLException {
		return getExternalContext().getResource(path);
	}

	@Override
	public InputStream getResourceAsStream(String path) {
		return getExternalContext().getResourceAsStream(path);
	}

	@Override
	public Set<String> getResourcePaths(String path) {
		return getExternalContext().getResourcePaths(path);
	}

	@Override
	public Object getResponse() {
		return getExternalContext().getResponse();
	}

	@Override
	public Object getSession(boolean create) {
		return getExternalContext().getSession(create);
	}

	@Override
	public Map<String, Object> getSessionMap() {
		return getExternalContext().getSessionMap();
	}

	@Override
	public Principal getUserPrincipal() {
		return getExternalContext().getUserPrincipal();
	}

	@Override
	public boolean isUserInRole(String role) {
		return getExternalContext().isUserInRole(role);
	}

	@Override
	public void log(String message) {
		getExternalContext().log(message);
	}

	@Override
	public void log(String message, Throwable exception) {
		getExternalContext().log(message, exception);
	}

	@Override
	public void redirect(String url) throws IOException {
		getExternalContext().redirect(url);
	}

}
