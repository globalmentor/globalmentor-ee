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

package com.globalmentor.faces.component.renderkit.xhtml;

import java.io.IOException;
import java.util.*;
import static java.util.Collections.*;

import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.Renderer;

import static com.globalmentor.faces.render.FacesRender.*;
import static com.globalmentor.faces.taglib.xhtml.XHTMLTags.*;
import static com.globalmentor.text.xml.xhtml.XHTML.*;

/**Encapsulates basic XHTML rendering functionality.
<p>Inspired by <code>com.sun.faces.renderkit.html_basic.HtmlBasicRenderer.java</code>.</p>
@author Garret Wilson
*/
public abstract class AbstractXHTMLRenderer extends Renderer
{

	/**Default XHTML attributes to pass through when rendering.*/
	public final static String[] DEFAULT_PASSTHROUGH_ATTRIBUTES=new String[]	//TODO use constants
			{
        "accept",
        "accesskey",
        "alt",
        "bgcolor",
        "border",
        "cellpadding",
        "cellspacing",
        "charset",
        "cols",
        "coords",
        "dir",
        "enctype",
        "frame",
        "height",
        "hreflang",
        "lang",
        "longdesc",
        "maxlength",
        "onblur",
        "onchange",
        "onclick",
        "ondblclick",
        "onfocus",
        "onkeydown",
        "onkeypress",
        "onkeyup",
        "onload",
        "onmousedown",
        "onmousemove",
        "onmouseout",
        "onmouseover",
        "onmouseup",
        "onreset",
        "onselect",
        "onsubmit",
        "onunload",
        "rel",
        "rev",
        "rows",
        "rules",
        "shape",
        "size",
        "style",
        "summary",
        "tabindex",
        "target",
        "title",
        "usemap",
        "width"
			};

    /**
     * This array contains attributes that have a boolean value in JSP,
     * but have have no value in HTML.  For example "disabled" or
     * "readonly". <P>
     *
     * @see renderBooleanPassthruAttributes
     */
/*TODO decide what to do with these
    private static String booleanPassthruAttributes[] = {
        "disabled",
        "readonly",
        "ismap"
    };
*/

	/**The set of attributes to pass through when rendering.*/
	private final Set<String> passthroughAttributeSet;

		/**@return The set of attributes to pass through when rendering.*/
		protected Set<String> getPassthroughAttributeSet() {return passthroughAttributeSet;}

	/**@return The attributes to pass through when rendering.*/
//TODO del	public String[] getPassthroughAttributes() {return DEFAULT_PASSTHROUGH_ATTRIBUTES;}

	/**@return The name of the XML element for the component.*/
//TODO del	protected abstract String getComponentElementName();

	/**The name of the XML element for the component.*/
	private final String componentElementName;

		/**@return The name of the XML element for the component.*/
		protected String getComponentElementName() {return componentElementName;}

	/**XML element name constructor.
	@param componentElementName The name of the XML element to render.
	*/
	public AbstractXHTMLRenderer(final String componentElementName)
	{
		this.componentElementName=componentElementName;	//save the element name
		passthroughAttributeSet=new HashSet<String>(DEFAULT_PASSTHROUGH_ATTRIBUTES.length);	//create the set of passthrough attributes
		addAll(passthroughAttributeSet, DEFAULT_PASSTHROUGH_ATTRIBUTES);	//initialize our set of passthrough attributes to the default
	}

	/**Begins encoding the component.
	@param context The JSF context.
	@param component The component being rendered.
	@throws IOException Thrown if there is an error writing the output.
	@throws NullPointerException Thrown if <var>context</var> or
		<var>component</var> is <code>null</code>.
	*/
	public void encodeBegin(final FacesContext context, final UIComponent component) throws IOException
	{
		super.encodeBegin(context, component);	//do the default encoding
		if(component.isRendered())	//if the component should be rendered
		{
			final ResponseWriter writer=context.getResponseWriter();	//get the response writer
			final Map attributeMap=component.getAttributes();	//get the map of attributes
			writer.startElement(getComponentElementName(), component);	//write the starting tag
			writeIDAttribute(context, writer, component);	//write the ID attribute
			final String styleClass=(String)attributeMap.get(STYLE_CLASS_ATTRIBUTE);	//get the style class, if there is one
			if(styleClass!=null)	//if there is a style class attribute
			{
				writer.writeAttribute(ATTRIBUTE_CLASS, styleClass, STYLE_CLASS_ATTRIBUTE);	//write the style class attribute
			}
			renderPassthroughAttributes(writer, component, (String[])getPassthroughAttributeSet().toArray(new String[getPassthroughAttributeSet().size()]));	//render the XHTML passthrough attributes
//TODO fix        Util.renderBooleanPassThruAttributes(writer, component);
		}
	}

	/**Encodes the end of the component.
	@param context The JSF context.
	@param component The component being rendered.
	@throws IOException Thrown if there is an error writing the output.
	@throws NullPointerException Thrown if <var>context</var> or
		<var>component</var> is <code>null</code>.
	*/
	public void encodeEnd(final FacesContext context, final UIComponent component) throws IOException
	{
		if(component.isRendered())	//if the component should be rendered
		{
			final ResponseWriter writer=context.getResponseWriter();	//get the response writer
			writer.endElement(getComponentElementName());	//write the ending tag
			writer.write('\n');	//write a newline after the ending tag
    }
		super.encodeEnd(context, component);	//do the default encoding
	}

	/**@return <code>true</code> if the component has an <code>id</code> attribute that isn't automatically generated.*/ 
	protected boolean hasCustomID(final UIComponent component)
	{
		final String id=component.getId();	//get the component's ID
		return id!=null && !id.startsWith(UIViewRoot.UNIQUE_ID_PREFIX);	//see if there is an ID that wasn't generated
	}

	/**Writes the component ID, if there is a custom ID.
	@param context The faces context.
	@param writer The output writer.
	@param component The component being rendered.
	@see #hasCustomID(UIComponent)
	*/
	protected void writeIDAttribute(final FacesContext context, ResponseWriter writer, UIComponent component)
	{
		if(hasCustomID(component))	//if this component has a custom ID
		{
			try
			{
				writer.writeAttribute(ATTRIBUTE_ID, component.getClientId(context), ATTRIBUTE_ID);	//write the ID
       }
			catch(IOException ioException)
			{
				throw new AssertionError(ioException);
			}
    }
	}

}
