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

import javax.faces.FacesException;
import javax.faces.context.*;

/**Class for decorating an existing JSF external context.
@author Garret Wilson
*/
public class ExternalContextDecorator extends ExternalContext
{

	/**The external context being decorated.*/
	private final ExternalContext externalContext;

		/**@return The external context being decorated.*/
		protected ExternalContext getExternalContext() {return externalContext;}

	/**Creates a decorator for an existing external context.
	@param externalContext The external context being decorated.
	*/
	public ExternalContextDecorator(final ExternalContext externalContext)
	{
		this.externalContext=externalContext;	//save the decorated external context
	}

    /**
     * <p>Dispatch a request to the specified resource to create output
     * for this response.</p>
     *
     * <p><em>Servlet:</em> This must be accomplished by calling the
     * <code>javax.servlet.ServletContext</code> method
     * <code>getRequestDispatcher(path)</code>, and calling the
     * <code>forward()</code> method on the resulting object.</p>
     *
     * <p><em>Portlet:</em> This must be accomplished by calling the
     * <code>javax.portlet.PortletContext</code> method
     * <code>getRequestDispatcher()</code>, and calling the
     * <code>include()</code> method on the resulting object.</p>
     *
     * @param path Context relative path to the specified resource,
     *  which must start with a slash ("/") character
     *
     * @exception FacesException thrown if a <code>ServletException</code>
     *  or <code>PortletException</code> occurs
     * @exception IllegalArgumentException if no request dispatcher
     *  can be created for the specified path
     * @exception IllegalStateException if this method is called in a portlet
     *  environment, and the current request is an <code>ActionRequest</code>
     *  instead of a <code>RenderRequest</code>
     * @exception IOException if an input/output error occurs
     * @exception NullPointerException if <code>path</code>
     *  is <code>null</code>
     */
    public void dispatch(String path) throws IOException
		{
			getExternalContext().dispatch(path);
		}


    /**
     * <p>Return the input URL, after performing any rewriting needed to
     * ensure that it will correctly identify an addressable action in the
     * current application.<p>
     *
     * <p><em>Servlet:</em> This must be the value returned by the
     * <code>javax.servlet.http.HttpServletResponse</code> method
     * <code>encodeURL(url)</code>.</p>
     *
     * <p><em>Portlet:</em> This must be the value returned by the
     * <code>javax.portlet.PortletResponse</code> method
     * <code>encodeURL(url)</code>.</p>
     *
     * @param url The input URL to be encoded
     *
     * @exception NullPointerException if <code>url</code>
     *  is <code>null</code>
     */
    public String encodeActionURL(String url)
		{
			return getExternalContext().encodeActionURL(url);
		}
    

    /**
     * <p>Return the specified name, after prefixing it with a namespace
     * that ensures that it will be unique within the context of a
     * particular page.</p>
     *
     * <p><em>Servlet:</em> The input value must be returned unchanged.</p>
     *
     * <p><em>Portlet:</em> The returned value must be the input value
     * prefixed by the value returned by the
     * <code>javax.portlet.RenderResponse</code> method
     * <code>getNamespace()</code>.</p>
     *
     * @param name Name to be encoded
     *
     * @exception IllegalStateException if this method is called in a portlet
     *  environment, and the current response is an <code>ActionResponse</code>
     *  instead of a <code>RenderResponse</code>
     * @exception NullPointerException if <code>name</code>
     *  is <code>null</code>
     */
    public String encodeNamespace(String name)
		{
			return getExternalContext().encodeNamespace(name);
		}


    /**
     * <p>Return the input URL, after performing any rewriting needed to
     * ensure that it will correctly identify an addressable resource in the
     * current application.<p>
     *
     * <p><em>Servlet:</em> This must be the value returned by the
     * <code>javax.servlet.http.HttpServletResponse</code> method
     * <code>encodeURL(url)</code>.</p>
     *
     * <p><em>Portlet:</em> This must be the value returned by the
     * <code>javax.portlet.PortletResponse</code> method
     * <code>encodeURL(url)</code>.</p>
     *
     * @param url The input URL to be encoded
     *
     * @exception NullPointerException if <code>url</code>
     *  is <code>null</code>
     */
    // PENDING(craigmcc) - Currently identical to encodeActionURL()
    public String encodeResourceURL(String url)
		{
			return getExternalContext().encodeResourceURL(url);
		}
    

    /**
     * <p>Return a mutable <code>Map</code> representing the application scope
     * attributes for the current application.  The returned <code>Map</code>
     * must implement the entire contract for a modifiable map as described
     * in the JavaDocs for <code>java.util.Map</code>.  Modifications
     * made in the <code>Map</code> must cause the corresponding changes in
     * the set of application scope attributes.</p>
     *
     * <p><em>Servlet:</em>  This must be the set of attributes available via
     * the <code>javax.servlet.ServletContext</code> methods
     * <code>getAttribute()</code>, <code>getAttributeNames()</code>,
     * <code>removeAttribute()</code>, and <code>setAttribute()</code>.</p>
     *
     * <p><em>Portlet:</em>  This must be the set of attributes available via
     * the <code>javax.portlet.PortletContext</code> methods
     * <code>getAttribute()</code>, <code>getAttributeNames()</code>,
     * <code>removeAttribute()</code>, and <code>setAttribute()</code>.</p>
     */
    public Map getApplicationMap()
		{
			return getExternalContext().getApplicationMap();
		}


    /**
     * <p>Return the name of the authentication scheme used to authenticate
     * the current user, if any; otherwise, return <code>null</code>.
     * For standard authentication schemes, the returned value will match
     * one of the following constants:
     * <code>BASIC_AUTH</code>, <code>CLIENT_CERT_AUTH</code>,
     * <code>DIGEST_AUTH</code>, or <code>FORM_AUTH</code>.</p>
     *
     * <p><em>Servlet:</em> This must be the value returned by the
     * <code>javax.servlet.http.HttpServletRequest</code> method
     * <code>getAuthType()</code>.</p>
     *
     * <p><em>Portlet:</em> This must be the value returned by the
     * <code>javax.portlet.http.PortletRequest</code> method
     * <code>getAuthType()</code>.</p>
     */
    public String getAuthType()
		{
			return getExternalContext().getAuthType();
		}


    /**
     * <p>Return the application environment object instance for the current
     * appication.</p>
     *
     * <p><em>Servlet:</em>  This must be the current application's
     * <code>javax.servlet.ServletContext</code> instance.</p>
     *
     * <p><em>Portlet:</em>  This must be the current application's
     * <code>javax.portlet.PortletContext</code> instance.</p>
     */
    public Object getContext()
		{
			return getExternalContext().getContext();
		}


    /**
     * <p>Return the value of the specified application initialization
     * parameter (if any).</p>
     *
     * <p><em>Servlet:</em> This must be the result of the
     * <code>javax.servlet.ServletContext</code> method
     * <code>getInitParameter(name)</code>.</p>
     *
     * <p><em>Portlet:</em> This must be the result of the
     * <code>javax.portlet.PortletContext</code> method
     * <code>getInitParameter(name)</code>.</p>
     *
     * @param name Name of the requested initialization parameter
     *
     * @exception NullPointerException if <code>name</code>
     *  is <code>null</code>
     */
    public String getInitParameter(String name)
		{
			return getExternalContext().getInitParameter(name);
		}


    /**
     * <p>Return an immutable <code>Map</code> whose keys are the set of
     * application initialization parameter names configured for this
     * application, and whose values are the corresponding parameter
     * values.  The returned <code>Map</code> must implement the entire
     * contract for an unmodifiable map as described in the JavaDocs
     * for <code>java.util.Map</code>.</p>
     *
     * <p><em>Servlet:</em> This result must be as if it were synthesized
     * by calling the <code>javax.servlet.ServletContext</code>
     * method <code>getInitParameterNames</code>, and putting
     * each configured parameter name/value pair into the result.</p>
     *
     * <p><em>Portlet:</em> This result must be as if it were synthesized
     * by calling the <code>javax.portlet.PortletContext</code>
     * method <code>getInitParameterNames</code>, and putting
     * each configured parameter name/value pair into the result.</p>
     */
    public Map getInitParameterMap()
		{
			return getExternalContext().getInitParameterMap();
		}
    

    /**
     * <p>Return the login name of the user making the current request
     * if any; otherwise, return <code>null</code>.</p>
     *
     * <p><em>Servlet:</em> This must be the value returned by the
     * <code>javax.servlet.http.HttpServletRequest</code> method
     * <code>getRemoteUser()</code>.</p>
     *
     * <p><em>Portlet:</em> This must be the value returned by the
     * <code>javax.portlet.http.PortletRequest</code> method
     * <code>getRemoteUser()</code>.</p>
     */
    public String getRemoteUser()
		{
			return getExternalContext().getRemoteUser();
		}


    /**
     * <p>Return the environment-specific object instance for the current
     * request.</p>
     *
     * <p><em>Servlet:</em>  This must be the current request's
     * <code>javax.servlet.http.HttpServletRequest</code> instance.</p>
     *
     * <p><em>Portlet:</em>  This must be the current request's
     * <code>javax.portlet.PortletRequest</code> instance, which
     * will be either an <code>ActionRequest</code> or a
     * <code>RenderRequest</code> depending upon when this method
     * is called.</p>
     */
    public Object getRequest()
		{
			return getExternalContext().getRequest();
		}


    /**
     * <p>Return the portion of the request URI that identifies the web
     * application context for this request.</p>
     *
     * <p><em>Servlet:</em> This must be the value returned by the
     * <code>javax.servlet.http.HttpServletRequest</code> method
     * <code>getContextPath()</code>.</p>
     *
     * <p><em>Portlet:</em> This must be the value returned by the
     * <code>javax.portlet.PortletRequest</code> method
     * <code>getContextPath()</code>.</p>
     */
    public String getRequestContextPath()
		{
			return getExternalContext().getRequestContextPath();
		}


    /**
     * <p>Return an immutable <code>Map</code> whose keys are the set of
     * cookie names included in the current request, and whose
     * values (of type <code>javax.servlet.http.Cookie</code>)
     * are the first (or only) cookie for each cookie name
     * returned by the underlying request.  The returned
     * <code>Map</code> must implement the entire contract for an unmodifiable
     * map as described in the JavaDocs for <code>java.util.Map</code>.</p>
     *
     * <p><em>Servlet:</em> This must be the value returned by the
     * <code>javax.servlet.ServletRequest</code> method
     * <code>getCookies()</code>, unless <code>null</code> was returned,
     * in which case this must be a zero-length array.</p>
     *
     * <p><em>Portlet:</em> The must be a zero-length array.</p>
     */
    public Map getRequestCookieMap()
		{
			return getExternalContext().getRequestCookieMap();
		}
    

    /**
     * <p>Return an immutable <code>Map</code> whose keys are the set of
     * request header names included in the current request, and whose
     * values (of type String) are the first (or only) value for each
     * header name returned by the underlying request.  The returned
     * <code>Map</code> must implement the entire contract for an unmodifiable
     * map as described in the JavaDocs for <code>java.util.Map</code>.  In
     * addition, key comparisons must be performed in a case insensitive
     * manner.</p>
     *
     * <p><em>Servlet:</em> This must be the set of headers available via
     * the <code>javax.servlet.http.HttpServletRequest</code> methods
     * <code>getHeader()</code> and <code>getHeaderNames()</code>.</p>
     *
     * <p><em>Portlet:</em> This must be the set of properties available via
     * the <code>javax.portlet.PortletRequest</code> methods
     * <code>getProperty()</code> and <code>getPropertyNames()</code>.
     * As such, HTTP headers will only be included if they were provided
     * by the portlet container, and additional properties provided by
     * the portlet container may also be included.</p>
     */
    public Map getRequestHeaderMap()
		{
			return getExternalContext().getRequestHeaderMap();
		}
    

    /**
     * <p>Return an immutable <code>Map</code> whose keys are the set of
     * request header names included in the current request, and whose
     * values (of type String[]) are all of the value for each
     * header name returned by the underlying request.  The returned
     * <code>Map</code> must implement the entire contract for an unmodifiable
     * map as described in the JavaDocs for <code>java.util.Map</code>.  In
     * addition, key comparisons must be performed in a case insensitive
     * manner.</p>
     *
     * <p><em>Servlet:</em> This must be the set of headers available via
     * the <code>javax.servlet.http.HttpServletRequest</code> methods
     * <code>getHeaders()</code> and <code>getHeaderNames()</code>.</p>
     *
     * <p><em>Portlet:</em> This must be the set of properties available via
     * the <code>javax.portlet.PortletRequest</code> methods
     * <code>getProperties()</code> and <code>getPropertyNames()</code>.
     * As such, HTTP headers will only be included if they were provided
     * by the portlet container, and additional properties provided by
     * the portlet container may also be included.</p>
     */
    public Map getRequestHeaderValuesMap()
		{
			return getExternalContext().getRequestHeaderValuesMap();
		}
    

    /**
     * <p>Return the preferred <code>Locale</code> in which the client
     * will accept content.</p>
     *
     * <p><em>Servlet:</em> This must be the value returned by the
     * <code>javax.servlet.ServletRequest</code> method
     * <code>getLocale()</code>.</p>
     *
     * <p><em>Portlet:</em> This must be the value returned by the
     * <code>javax.portlet.PortletRequest</code> method
     * <code>getLocale()</code>.</p>
     */
    public Locale getRequestLocale()
		{
			return getExternalContext().getRequestLocale();
		}
    

    /**
     * <p>Return an <code>Iterator</code> over the preferred
     * <code>Locale</code>s specified in the request, in decreasing
     * order of preference.</p>
     *
     * <p><em>Servlet:</em> This must be an <code>Iterator</code>
     * over the values returned by the <code>javax.servlet.ServletRequest</code>
     * method <code>getLocales()</code>.</p>
     *
     * <p><em>Portlet:</em> This must be an <code>Iterator</code>
     * over the values returned by the <code>javax.portlet.PortletRequest</code>
     * method <code>getLocales()</code>.</p>
     */
    public Iterator getRequestLocales()
		{
			return getExternalContext().getRequestLocales();
		}


    /**
     * <p>Return a mutable <code>Map</code> representing the request scope
     * attributes for the current application.  The returned <code>Map</code>
     * must implement the entire contract for a modifiable map as described
     * in the JavaDocs for <code>java.util.Map</code>.  Modifications
     * made in the <code>Map</code> must cause the corresponding changes in
     * the set of request scope attributes.</p>
     *
     * <p><em>Servlet:</em>  This must be the set of attributes available via
     * the <code>javax.servlet.ServletRequest</code> methods
     * <code>getAttribute()</code>, <code>getAttributeNames()</code>,
     * <code>removeAttribute()</code>, and <code>setAttribute()</code>.</p>
     *
     * <p><em>Portlet:</em>  This must be the set of attributes available via
     * the <code>javax.portlet.PortletRequest</code> methods
     * <code>getAttribute()</code>, <code>getAttributeNames()</code>,
     * <code>removeAttribute()</code>, and <code>setAttribute()</code>.</p>
     */
    public Map getRequestMap()
		{
			return getExternalContext().getRequestMap();
		}


    /**
     * <p>Return an immutable <code>Map</code> whose keys are the set of
     * request parameters names included in the current request, and whose
     * values (of type String) are the first (or only) value for each
     * parameter name returned by the underlying request.  The returned
     * <code>Map</code> must implement the entire contract for an unmodifiable
     * map as described in the JavaDocs for <code>java.util.Map</code>.</p>
     *
     * <p><em>Servlet:</em> This must be the set of parameters available via
     * the <code>javax.servlet.ServletRequest</code> methods
     * <code>getParameter()</code> and <code>getParameterNames()</code>.</p>
     *
     * <p><em>Portlet:</em> This must be the set of parameters available via
     * the <code>javax.portlet.PortletRequest</code> methods
     * <code>getParameter()</code> and <code>getParameterNames()</code>.</p>
     */
    public Map getRequestParameterMap()
		{
			return getExternalContext().getRequestParameterMap();
		}
    

    /**
     * <p>Return an <code>Iterator</code> over the names of all request
     * parameters included in the current request.</p>
     *
     * <p><em>Servlet:</em> This must be an <code>Iterator</code> over the
     * values returned by the <code>javax.servlet.ServletRequest</code>
     * method <code>getParameterNames()</code>.</p>
     *
     * <p><em>Portlet:</em> This must be an <code>Iterator</code> over the
     * values returned by the <code>javax.portlet.PortletRequest</code>
     * method <code>getParameterNames()</code>.</p>
     */
    public Iterator getRequestParameterNames()
		{
			return getExternalContext().getRequestParameterNames();
		}


    /**
     * <p>Return an immutable <code>Map</code> whose keys are the set of
     * request parameters names included in the current request, and whose
     * values (of type String[]) are all of the values for each
     * parameter name returned by the underlying request.  The returned
     * <code>Map</code> must implement the entire contract for an unmodifiable
     * map as described in the JavaDocs for <code>java.util.Map</code>.</p>
     *
     * <p><em>Servlet:</em> This must be the set of parameters available via
     * the <code>javax.servlet.ServletRequest</code> methods
     * <code>getParameterValues()</code> and
     * <code>getParameterNames()</code>.</p>
     *
     * <p><em>Portlet:</em> This must be the set of parameters available via
     * the <code>javax.portlet.PortletRequest</code> methods
     * <code>getParameterValues()</code> and
     * <code>getParameterNames()</code>.</p>
     */
    public Map getRequestParameterValuesMap()
		{
			return getExternalContext().getRequestParameterValuesMap();
		}
    

    /**
     * <p>Return the extra path information (if any) included in the
     * request URI; otherwise, return <code>null</code>.</p>
     *
     * <p><em>Servlet:</em> This must be the value returned by the
     * <code>javax.servlet.http.HttpServletRequest</code> method
     * <code>getPathInfo()</code>.</p>
     *
     * <p><em>Portlet:</em> This must be <code>null</code>.</p>
     */
    public String getRequestPathInfo()
		{
			return getExternalContext().getRequestPathInfo();
		}
    

    /**
     * <p>Return the servlet path information (if any) included in the
     * request URI; otherwise, return <code>null</code>.</p>
     *
     * <p><em>Servlet:</em> This must be the value returned by the
     * <code>javax.servlet.http.HttpServletRequest</code> method
     * <code>getServletPath()</code>.</p>
     *
     * <p><em>Portlet:</em> This must be <code>null</code>.</p>
     */
    public String getRequestServletPath()
		{
			return getExternalContext().getRequestServletPath();
		}
    

    /**
     * <p>Return a <code>URL</code> for the application resource mapped to the
     * specified path, if it exists; otherwise, return <code>null</code>.</p>
     *
     * <p><em>Servlet:</em> This must be the value returned by the
     * <code>javax.servlet.ServletContext</code> method
     * <code>getResource(path)</code>.</p>
     *
     * <p><em>Portlet:</em> This must be the value returned by the
     * <code>javax.portlet.PortletContext</code> method
     * <code>getResource(path)</code>.</p>
     *
     * @param path The path to the requested resource, which must
     *  start with a slash ("/" character
     *
     * @exception MalformedURLException if the specified path
     *  is not in the correct form
     * @exception NullPointerException if <code>path</code>
     *  is <code>null</code>
     */
    public URL getResource(String path) throws MalformedURLException
		{
			return getExternalContext().getResource(path);
		}


    /**
     * <p>Return an <code>InputStream</code> for an application resource
     * mapped to the specified path, if it exists; otherwise, return
     * <code>null</code>.</p>
     *
     * <p><em>Servlet:</em> This must be the value returned by the
     * <code>javax.servlet.ServletContext</code> method
     * <code>getResourceAsStream(path)</code>.</p>
     *
     * <p><em>Portlet:</em> This must be the value returned by the
     * <code>javax.portlet.PortletContext</code> method
     * <code>getResourceAsStream(path)</code>.</p>
     *
     * @param path The path to the requested resource, which must
     *  start with a slash ("/" character
     *
     * @exception NullPointerException if <code>path</code>
     *  is <code>null</code>
     */
    public InputStream getResourceAsStream(String path)
		{
			return getExternalContext().getResourceAsStream(path);
		}


    /**
     * <p>Return the <code>Set</code> of resource paths for all application
     * resources whose resource path starts with the specified argument.</p>
     *
     * <p><em>Servlet:</em> This must be the value returned by the
     * <code>javax.servlet.ServletContext</code> method
     * <code>getResourcePaths(path).</p>
     *
     * <p><em>Servlet:</em> This must be the value returned by the
     * <code>javax.servlet.ServletContext</code> method
     * <code>getResourcePaths(path).</p>
     *
     * @param path Partial path used to match resources, which must
     *  start with a slash ("/") character
     *
     * @exception NullPointerException if <code>path</code>
     *  is <code>null</code>
     */
    public Set getResourcePaths(String path)
		{
			return getExternalContext().getResourcePaths(path);
		}


    /**
     * <p>Return the environment-specific object instance for the current
     * response.</p>
     *
     * <p><em>Servlet:</em>  This is the current request's
     * <code>javax.servlet.http.HttpServletResponse</code> instance.</p>
     *
     * <p><em>Portlet:</em>  This is the current request's
     * <code>javax.portlet.PortletResponse</code> instance, which
     * will be either an <code>ActionResponse</code> or a
     * <code>RenderResponse</code> depending upon when this method
     * is called.</p>
     */
    public Object getResponse()
		{
			return getExternalContext().getResponse();
		}


    /**
     * <p>If the <code>create</code> parameter is <code>true</code>,
     * create (if necessary) and return a session instance associated
     * with the current request.  If the <code>create</code> parameter
     * is <code>false</code> return any existing session instance
     * associated with the current request, or return <code>null</code> if
     * there is no such session.</p>
     *
     * <p><em>Servlet:</em> This must return the result of calling
     * <code>getSession(create)</code> on the underlying
     * <code>javax.servlet.http.HttpServletRequest</code> instance.</p>
     *
     * <p>em>Portlet:</em> This must return the result of calling
     * <code>getPortletSession(create)</code> on the underlying
     * <code>javax.portlet.PortletRequest</code> instance.</p>
     *
     * @param create Flag indicating whether or not a new session should be
     *  created if there is no session associated with the current request
     */
    public Object getSession(boolean create)
		{
			return getExternalContext().getSession(create);
		}


    /**
     * <p>Return a mutable <code>Map</code> representing the session scope
     * attributes for the current application.  The returned <code>Map</code>
     * must implement the entire contract for a modifiable map as described
     * in the JavaDocs for <code>java.util.Map</code>.  Modifications
     * made in the <code>Map</code> must cause the corresponding changes in
     * the set of session scope attributes.  Accessing attributes via this
     * <code>Map</code> must cause the creation of a session associated with
     * the current request, if such a session does not already exist.</p>
     *
     * <p><em>Servlet:</em>  This must be the set of attributes available via
     * the <code>javax.servlet.http.HttpServletSession</code> methods
     * <code>getAttribute()</code>, <code>getAttributeNames()</code>,
     * <code>removeAttribute()</code>, and <code>setAttribute()</code>.</p>
     *
     * <p><em>Portlet:</em>  This must be the set of attributes available via
     * the <code>javax.portlet.PortletSession</code> methods
     * <code>getAttribute()</code>, <code>getAttributeNames()</code>,
     * <code>removeAttribute()</code>, and <code>setAttribute()</code>.
     * All session attribute access must occur in PORTLET_SCOPE scope
     * within the session.</p>
     */
    public Map getSessionMap()
		{
			return getExternalContext().getSessionMap();
		}


    /**
     * <p>Return the <code>Principal</code> object containing the name of
     * the current authenticated user, if any; otherwise, return
     * <code>null</code>.</p>
     *
     * <p><em>Servlet:</em> This must be the value returned by the
     * <code>javax.servlet.http.HttpServletRequest</code> method
     * <code>getUserPrincipal()</code>.</p>
     *
     * <p><em>Portlet:</em> This must be the value returned by the
     * <code>javax.portlet.http.PortletRequest</code> method
     * <code>getUserPrincipal()</code>.</p>
     */
    public Principal getUserPrincipal()
		{
			return getExternalContext().getUserPrincipal();
		}


    /**
     * <p>Return <code>true</code> if the currently authenticated user is
     * included in the specified role.  Otherwise, return <code>false</code>.
     * </p>
     *
     * <p><em>Servlet:</em> This must be the value returned by the
     * <code>javax.servlet.http.HttpServletRequest</code> method
     * <code>isUserInRole(role)</code>.</p>
     *
     * <p><em>Portlet:</em> This must be the value returned by the
     * <code>javax.portlet.http.PortletRequest</code> method
     * <code>isUserInRole(role)</code>.</p>
     *
     * @param role Logical role name to be checked
     *
     * @exception NullPointerException if <code>role</code>
     *  is <code>null</code>
     */
    public boolean isUserInRole(String role)
		{
			return getExternalContext().isUserInRole(role);
		}


    /**
     * <p>Log the specified message to the application object.</p>
     *
     * <p><em>Servlet:</em> This must be performed by calling the
     * <code>javax.servlet.ServletContext</code> method
     * <code>log(String)</code>.</p>
     *
     * <p><em>Portlet:</em> This must be performed by calling the
     * <code>javax.portlet.PortletContext</code> method
     * <code>log(String)</code>.</p>
     *
     * @param message Message to be logged
     *
     * @exception NullPointerException if <code>message</code>
     *  is <code>null</code>
     */
    public void log(String message)
		{
			getExternalContext().log(message);
		}


    /**
     * <p>Log the specified message and exception to the application object.</p>
     *
     * <p><em>Servlet:</em> This must be performed by calling the
     * <code>javax.servlet.ServletContext</code> method
     * <code>log(String,Throwable)</code>.</p>
     *
     * <p><em>Portlet:</em> This must be performed by calling the
     * <code>javax.portlet.PortletContext</code> method
     * <code>log(String,Throwable)</code>.</p>
     *
     * @param message Message to be logged
     * @param exception Exception to be logged
     *
     * @exception NullPointerException if <code>message</code>
     *  or <code>exception</code> is <code>null</code>
     */
    public void log(String message, Throwable exception)
		{
			getExternalContext().log(message, exception);
		}


    /**
     * <p>Redirect a request to the specified URL, and cause the
     * <code>responseComplete()</code> method to be called on the
     * {@link FacesContext} instance for the current request.</p>
     *
     * <p><em>Servlet:</em> This must be accomplished by calling the
     * <code>javax.servlet.http.HttpServletResponse</code> method
     * <code>sendRedirect()</code>.</p>
     *
     * <p><em>Portlet:</em> This must be accomplished by calling the
     * <code>javax.portlet.ActionResponse</code> method
     * <code>sendRedirect()</code>.</p>
     *
     * @param url Absolute URL to which the client should be redirected
     *
     * @exception IllegalArgumentException if the specified url is relative
     * @exception IllegalStateException if, in a portlet environment,
     *  the current response object is a <code>RenderResponse</code>
     *  instead of an <code>ActionResponse</code>
     * @exception IllegalStateException if, in a servlet environment,
     *  the current response has already been committed
     * @exception IOException if an input/output error occurs
     */
    public void redirect(String url) throws IOException
		{
			getExternalContext().redirect(url);
		}

	
}
