package com.garretwilson.faces.context;

import java.util.Iterator;
import java.util.Locale;
import javax.faces.FacesException;
import javax.faces.application.Application;
import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.*;
import javax.faces.render.RenderKit;

/**Class for decorating an existing JSF context.
@author Garret Wilson
*/
public class FacesContextDecorator extends FacesContext
{

	/**The context being decorated.*/
	private final FacesContext facesContext;

		/**@return The context being decorated.*/
		protected FacesContext getFacesContext() {return facesContext;}

	/**Creates a decorator for an existing JSF context.
	@param facesContext The JSF context being decorated.
	*/
	public FacesContextDecorator(final FacesContext facesContext)
	{
		this.facesContext=facesContext;	//save the decorated external context
	}

    /**
     * <p>Return the {@link Application} instance associated with this
     * web application.</p>
     *
     * @exception IllegalStateException if this method is called after
     *  this instance has been released
     */
    public Application getApplication()
		{
			return getFacesContext().getApplication();
		}


    /**
     * <p>Return an <code>Iterator</code> over the client identifiers for
     * which at least one {@link FacesMessage} has been queued.  If there are no
     * such client identifiers, an empty <code>Iterator</code> is returned.
     * If any messages have been queued that were not associated with any
     * specific client identifier, a <code>null</code> value will be included
     * in the iterated values.</p>
     *
     * @exception IllegalStateException if this method is called after
     *  this instance has been released
     */
    public Iterator getClientIdsWithMessages()
		{
			return getFacesContext().getClientIdsWithMessages();
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
			return getFacesContext().getExternalContext();
		}


    /**
     * <p>Return the maximum severity level recorded on any
     * {@link FacesMessage}s that has been queued, whether or not they are
     * associated with any specific {@link UIComponent}.  If no such messages
     * have been queued, return <code>null</code>.</p>
     *
     * @exception IllegalStateException if this method is called after
     *  this instance has been released
     */
    public Severity getMaximumSeverity()
		{
			return getFacesContext().getMaximumSeverity();
		}


    /**
     * <p>Return an <code>Iterator</code> over the {@link FacesMessage}s
     * that have been queued, whether or not they are associated with any
     * specific client identifier.  If no such messages have been queued,
     * return an empty <code>Iterator</code>.</p>
     *
     * @exception IllegalStateException if this method is called after
     *  this instance has been released
     */
    public Iterator getMessages()
		{
			return getFacesContext().getMessages();
		}


    /**
     * <p>Return an <code>Iterator</code> over the {@link FacesMessage}s that
     * have been queued that are associated with the specified client identifier
     * (if <code>clientId</code> is not <code>null</code>), or over the
     * {@link FacesMessage}s that have been queued that are not associated with
     * any specific client identifier (if <code>clientId</code> is
     * <code>null</code>).  If no such messages have been queued, return an
     * empty <code>Iterator</code>.</p>
     *
     * @param clientId The client identifier for which messages are
     *  requested, or <code>null</code> for messages not associated with
     *  any client identifier
     *
     * @exception IllegalStateException if this method is called after
     *  this instance has been released
     */
    public Iterator getMessages(String clientId)
		{
			return getFacesContext().getMessages(clientId);
		}


    /**
     * <p>Return the {@link RenderKit} instance for the render kit identifier
     * specified on our {@link UIViewRoot}, if there is one.  If there is no
     * current {@link UIViewRoot}, if the {@link UIViewRoot} does not have a
     * specified <code>renderKitId</code>, or if there is no {@link RenderKit}
     * for the specified identifier, return <code>null</code> instead.</p>
     */
    public RenderKit getRenderKit()
		{
			return getFacesContext().getRenderKit();
		}


    /**
     * <p>Return <code>true</code> if the <code>renderResponse()</code>
     * method has been called for the current request.</p>
     *
     * @exception IllegalStateException if this method is called after
     *  this instance has been released
     */
    public boolean getRenderResponse()
		{
			return getFacesContext().getRenderResponse();
		}


    /**
     * <p>Return <code>true</code> if the <code>responseComplete()</code>
     * method has been called for the current request.</p>
     *
     * @exception IllegalStateException if this method is called after
     *  this instance has been released
     */
    public boolean getResponseComplete()
		{
			return getFacesContext().getResponseComplete();
		}


    /**
     * <p>Return the {@link ResponseStream} to which components should
     * direct their binary output.  Within a given response, components
     * can use either the ResponseStream or the ResponseWriter,
     * but not both.
     *
     * @exception IllegalStateException if this method is called after
     *  this instance has been released
     */
    public ResponseStream getResponseStream()
		{
			return getFacesContext().getResponseStream();
		}


    /**
     * <p>Set the {@link ResponseStream} to which components should
     * direct their binary output.
     *
     * @param responseStream The new ResponseStream for this response
     *
     * @exception NullPointerException if <code>responseStream</code>
     *  is <code>null</code>
     *
     * @exception IllegalStateException if this method is called after
     *  this instance has been released
     */
    public void setResponseStream(ResponseStream responseStream)
		{
			getFacesContext().setResponseStream(responseStream);
		}


    /**
     * <p>Return the {@link ResponseWriter} to which components should
     * direct their character-based output.  Within a given response,
     * components can use either the ResponseStream or the ResponseWriter,
     * but not both.
     *
     * @exception IllegalStateException if this method is called after
     *  this instance has been released
     */
    public ResponseWriter getResponseWriter()
		{
			return getFacesContext().getResponseWriter();
		}


    /**
     * <p>Set the {@link ResponseWriter} to which components should
     * direct their character-based output.
     *
     * @param responseWriter The new ResponseWriter for this response
     *
     * @exception IllegalStateException if this method is called after
     *  this instance has been released
     * @exception NullPointerException if <code>responseWriter</code>
     *  is <code>null</code>
     */
    public void setResponseWriter(ResponseWriter responseWriter)
		{
			getFacesContext().setResponseWriter(responseWriter);
		}


    /**
     * <p>Return the root component that is associated with the this request.
     * </p>
     *
     * @exception IllegalStateException if this method is called after
     *  this instance has been released
     */
    public UIViewRoot getViewRoot()
		{
			return getFacesContext().getViewRoot();
		}


    /**
     * <p>Set the root component that is associated with this request.
     * This method can only be called by the application handler (or a
     * class that the handler calls), and only during the <em>Invoke
     * Application</em> phase of the request processing lifecycle.</p>
     *
     * @param root The new component {@link UIViewRoot} component
     *
     * @exception IllegalStateException if this method is called after
     *  this instance has been released
     * @exception NullPointerException if <code>root</code>
     *  is <code>null</code>
     */
    public void setViewRoot(UIViewRoot root)
		{
			getFacesContext().setViewRoot(root);
		}


    // ---------------------------------------------------------- Public Methods


    /**
     * <p>Append a {@link FacesMessage} to the set of messages associated with
     * the specified client identifier, if <code>clientId</code> is
     * not <code>null</code>.  If <code>clientId</code> is <code>null</code>,
     * this {@link FacesMessage} is assumed to not be associated with any
     * specific component instance.</p>
     *
     * @param clientId The client identifier with which this message is
     *  associated (if any)
     * @param message The message to be appended
     *
     * @exception IllegalStateException if this method is called after
     *  this instance has been released
     * @exception NullPointerException if <code>message</code>
     *  is <code>null</code>
     */
    public void addMessage(String clientId, FacesMessage message)
		{
			getFacesContext().addMessage(clientId, message);
		}


    /**
     * <p>Release any resources associated with this
     * <code>FacesContext</code> instance.  Faces implementations may
     * choose to pool instances in the associated {@link
     * FacesContextFactory} to avoid repeated object creation and
     * garbage collection.  After <code>release()</code> is called on a
     * <code>FacesContext</code> instance (until the
     * <code>FacesContext</code> instance has been recycled by the
     * implementation for re-use), calling any other methods will cause
     * an <code>IllegalStateException</code> to be thrown.</p>
     *
     * <p>The implementation must call {@link #setCurrentInstance}
     * passing <code>null</code> to remove the association between this
     * thread and this dead <code>FacesContext</code> instance.</p>
     *
     * @exception IllegalStateException if this method is called after
     *  this instance has been released
     */
    public void release()
		{
			getFacesContext().release();
		}


    /**
     * <p>Signal the JavaServer faces implementation that, as soon as the
     * current phase of the request processing lifecycle has been completed,
     * control should be passed to the <em>Render Response</em> phase,
     * bypassing any phases that have not been executed yet.</p>
     *
     * @exception IllegalStateException if this method is called after
     *  this instance has been released
     */
    public void renderResponse()
		{
			getFacesContext().renderResponse();
		}


    /**
     * <p>Signal the JavaServer Faces implementation that the HTTP response
     * for this request has already been generated (such as an HTTP redirect),
     * and that the request processing lifecycle should be terminated as soon
     * as the current phase is completed.</p>
     *
     * @exception IllegalStateException if this method is called after
     *  this instance has been released
     */
    public void responseComplete()
		{
			getFacesContext().responseComplete();
		}

}
