package com.garretwilson.faces.context;

import java.util.*;
import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.*;
import javax.faces.render.RenderKit;

/**Decorates an existing JSF context by adding request parameters to the
	external context.
@author Garret Wilson
*/
public class RequestParametersFacesContextDecorator extends FacesContextDecorator
{

	/**The external context decorator.*/
	private final ExternalContext externalContextDecorator;

		/**@return The external context decorator.*/
		protected ExternalContext getExternalContextDecorator() {return externalContextDecorator;}	
	
	/**The map of request parameters, both those from the external context and those added.*/
	private final Map<String, String> allRequestParameterMap;

		/**@return The map of request parameters, both those from the external context and those added.*/
		protected Map<String, String> getAllRequestParameterMap() {return allRequestParameterMap;}

	/**Adds a request parameter to the context.
	@param name The request parameter name.
	@param value The request parameter value.
	*/
	public void putRequestParameter(final String name, final String value)
	{
		allRequestParameterMap.put(name, value);	//store this new parameter in our map
	}
	
	/**Creates a decorator for an existing JSF context.
	@param facesContext The JSF context being decorated.
	*/
	public RequestParametersFacesContextDecorator(final FacesContext facesContext)
	{
		super(facesContext);	//construct the parent class
		allRequestParameterMap=new HashMap<String, String>();	//create our own map of request parameters
			//add all the existing parameters to our map
		allRequestParameterMap.putAll(facesContext.getExternalContext().getRequestParameterMap());
			//create our own external context decorator that knows how to return all the parameters
		externalContextDecorator=new RequestParametersExternalContextDecorator(facesContext.getExternalContext());
	}

    /**
     * <p>Return the {@link ExternalContext} instance for this
     * <code>FacesContext</code> instance.</p>
     *
     * @exception IllegalStateException if this method is called after
     *  this instance has been released
     */
    public ExternalContext getExternalContext()
		{
			super.getExternalContext();	//call the super method to make sure we throw an illegal state exception if needed
			return getExternalContextDecorator();	//return our external context decorator
		}

		

	/**Decorates an existing JSF context by adding request parameters to the
		external context.
	@author Garret Wilson
	*/
	protected class RequestParametersExternalContextDecorator extends ExternalContextDecorator
	{
		
		/**Creates a decorator for an existing external context.
		@param externalContext The external context being decorated.
		*/
		public RequestParametersExternalContextDecorator(final ExternalContext externalContext)
		{
			super(externalContext);	//save the decorated external context
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
			return Collections.unmodifiableMap(getAllRequestParameterMap());	//return an unmodifiable copy of inclusive request parameter map
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
			return getRequestParameterMap().keySet().iterator();	//return an iterator to the request parameter names
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
/*TODO fix
    public Map getRequestParameterValuesMap()
		{
			return getRequestParameterMap().values().iterator();	//return an iterator to the request parameter values
		}
*/
    
	}
}
