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

package com.globalmentor.faces.render;

import java.io.*;
import java.util.*;

import javax.faces.component.*;
import javax.faces.context.*;

import static com.globalmentor.javascript.JavaScript.*;

import static com.globalmentor.text.xml.xhtml.XHTML.*;

/**Utilities for rendering components.
@author Garret Wilson
*/
public class FacesRender
{
	
	/**Renders attributes of the component that should be passed through
		with no modification.
	@param writer The writer rendering the component.
	@param component The component being rendered.
	@param attributes The names of the attributes to render
	*/
	public static void renderPassthroughAttributes(final ResponseWriter writer, final UIComponent component, final String[] attributes) throws IOException
	{
		final Map attributeMap=component.getAttributes();	//get the map of component attributes
		for(final String attribute:attributes)	//look at each of the passed attributes
		{
			final Object value=attributeMap.get(attribute);	//get this attribute
			if(value!=null)	//if we have a value TODO check to see if we should render the value, according to the special wrapper type MIN_VALUEs
			{
				if(value instanceof Integer && ((Integer)value).intValue()==Integer.MIN_VALUE)
				{
					continue;	//TODO testing; fix correctly
				}
				writer.writeAttribute(attribute, value, attribute);	//render the attribute
			}
    }
	}

	/**Renders a JavaScript script element with JavaScript content.
	@param writer The writer rendering the component.
	@param component The component being rendered, if any.
	@param javascript The text of the JavaScript code to render
	*/
	public static void renderJavaScript(final ResponseWriter writer, final UIComponent component, final String javascript) throws IOException
	{

		writer.startElement(ELEMENT_SCRIPT, component);	//<script>
		writer.writeAttribute(ELEMENT_SCRIPT_ATTRIBUTE_TYPE, JAVASCRIPT_OBSOLETE_CONTENT_TYPE.toString(), ELEMENT_SCRIPT_ATTRIBUTE_TYPE);	//type="text/javascript"
		writer.writeText("<!--", null);	//<!--
		writer.writeText(javascript, null);	//write the JavaScript code
		writer.writeText("-->", null);	//-->
		writer.endElement(ELEMENT_SCRIPT);	//</script>
	}

	/**Renders a JavaScript script element with popup window code.
	<p>Inspired by the HTML code tutorial at
			<a href="http://www.htmlcodetutorial.com/linking/linking_famsupp_70.html"
				http://www.htmlcodetutorial.com/linking/linking_famsupp_70.html</a>.</p>
	@param writer The writer rendering the component.
	@param component The component being rendered.
	@param attributes The names of the attributes to render
	*/
/*TODO del if not needed
	public static void renderPopupJavaScript(final ResponseWriter writer, final UIComponent component) throws IOException
	{
		final String popupJavaScript=	//TODO move to JavaScript repository
				"function popup(link, windowname)"+
				"{"+
					"if(!window.focus) return true;"+	//if the browser doesn't handle window focusing, don't do anything else
					"window.open(link, windowname, 'width=400,height=200,scrollbars=yes');"+	//open up the link
					"return false;"+	//don't do further processing
				"}";
		renderJavaScript(writer, component, popupJavaScript);	//render the popup code
	}
*/
}
