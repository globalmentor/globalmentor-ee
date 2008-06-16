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

package com.globalmentor.faces.component.renderkit;

import java.io.IOException;

import javax.faces.FactoryFinder;
import javax.faces.component.UIComponent;
import javax.faces.convert.ConverterException;
import javax.faces.context.FacesContext;
import javax.faces.render.*;

/**Wraps an existing renderer for decoration.
@author Garret Wilson
*/
public class RendererDecorator extends Renderer
{

	/**The ID of the render kit to use for finding a renderer to wrap.*/
	private final String renderKitID;

		/**@return The ID of the render kit to use for finding a renderer to wrap.*/
		protected String getRenderKitID() {return renderKitID;}

	/**The component family of the renderer to wrap.*/
	private final String componentFamily;

		/**@return The component family of the renderer to wrap.*/
		protected String getComponentFamily() {return componentFamily;}

	/**The type of renderer to wrap.*/
	private final String renderType;

		/**@return The type of renderer to wrap.*/
		protected String getRenderType() {return renderType;}
		
	/**We'll cache whether the wrapped renderer renders children, as we can't double-check each
		time. We'll default to false.
	*/
	private boolean rendersChildren=false;

	/**Constructs a renderer wrapper.
	@param renderKitID The ID of the render kit to use for finding a renderer to wrap.
	@param componentFamily The component family of the renderer to wrap.
	@param renderType The type of renderer to wrap.
	*/
	public RendererDecorator(final String renderKitID, final String componentFamily, final String renderType)
	{
		this.renderKitID=renderKitID;
		this.componentFamily=componentFamily;
		this.renderType=renderType;
	}

	/**Retrieves the appropriate renderer to decorate.
	@param context The JSF context.
	@return A factory-created renderer to be decorated.
	*/
	protected Renderer getRenderer(final FacesContext context)
	{
			//get a render kit factory
		final RenderKitFactory renderKitFactory=(RenderKitFactory)FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
			//get a render kit
		final RenderKit renderKit=renderKitFactory.getRenderKit(context, getRenderKitID());
			//get an appropriate renderer from this render kit
		final Renderer renderer=renderKit.getRenderer(getComponentFamily(), getRenderType());
		rendersChildren=renderer.getRendersChildren();	//cache whether this renderer renders children, so that we can return the correct value later
		return renderer;	//return the renderer to decorate
	}

	/**Decodes the component information.
	This version delegates to the wrapped renderer.
	@param context The JSF context.
	@param component The component being decoded.
	@exception IOException Thrown if there is an error writing the output.
	@exception NullPointerException Thrown if <var>context</var> or
		<var>component</var> is <code>null</code>.
	*/
	public void decode(final FacesContext context, final UIComponent component)
	{
		getRenderer(context).decode(context, component);	//delegate to the wrapped renderer
	}

	/**Begins encoding the component.
	This version delegates to the wrapped renderer.
	@param context The JSF context.
	@param component The component being rendered.
	@exception IOException Thrown if there is an error writing the output.
	@exception NullPointerException Thrown if <var>context</var> or
		<var>component</var> is <code>null</code>.
	*/
	public void encodeBegin(final FacesContext context, final UIComponent component) throws IOException
	{
		getRenderer(context).encodeBegin(context, component);	//delegate to the wrapped renderer
	}

	/**Encodes the children of the component.
	This version delegates to the wrapped renderer.
	@param context The JSF context.
	@param component The component being rendered.
	@exception IOException Thrown if there is an error writing the output.
	@exception NullPointerException Thrown if <var>context</var> or
		<var>component</var> is <code>null</code>.
	*/
	public void encodeChildren(final FacesContext context, final UIComponent component) throws IOException
	{
		getRenderer(context).encodeChildren(context, component);	//delegate to the wrapped renderer
	}

	/**Encodes the end of the component.
	This version delegates to the wrapped renderer.
	@param context The JSF context.
	@param component The component being rendered.
	@exception IOException Thrown if there is an error writing the output.
	@exception NullPointerException Thrown if <var>context</var> or
		<var>component</var> is <code>null</code>.
	*/
	public void encodeEnd(final FacesContext context, final UIComponent component) throws IOException
	{
		getRenderer(context).encodeEnd(context, component);	//delegate to the wrapped renderer
	}

	/**Converts the component-generated client ID to a form suitable for
		transmission to the client.
	This version delegates to the wrapped renderer.
	@param context The JSF context.
	@param clientID The client identifier to be converted to client a specific format.
	@exception NullPointerException if <var>context</var>
		or <var>clientID</var> is <code>null</code>.
	*/
	public String convertClientId(final FacesContext context, final String clientID)
	{
		return getRenderer(context).convertClientId(context, clientID);	//delegate to the wrapped renderer
	}

	/**@return A flag indicating whether this renderer is responsible
		for rendering the children the component it is asked to render.
	This returns the value from the most recent wrapped renderer, or
		<code>false</code> if no wrapped renderer has yet been retrieved.
	*/
	public boolean getRendersChildren()
	{
		return rendersChildren;	//return whatever we've cached
	}

	/**Attempt to convert previously stored state information into an
		object of the type required for this component.
	This version delegates to the wrapped renderer.
	@param context The JSF context.
	@param component The component to be decoded.
	@param submittedValue A value stored on the component during
		<code>decode</code>.
	@exception ConverterException if the submitted value
		cannot be converted successfully.
	@exception NullPointerException if <code>context</code>
		or <code>component</code> is <code>null</code>
	*/
	public Object getConvertedValue(final FacesContext context, final UIComponent component, final Object submittedValue) throws ConverterException
	{
		return getRenderer(context).getConvertedValue(context, component, submittedValue);	//delegate to the wrapped renderer
	}

}
