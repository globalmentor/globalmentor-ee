package com.garretwilson.faces.render;

import java.io.*;
import java.util.*;

import javax.faces.component.*;
import javax.faces.context.*;

import com.globalmentor.io.ContentTypes;

import static com.garretwilson.text.xml.xhtml.XHTML.*;
import static com.globalmentor.io.ContentTypeConstants.*;

/**Utilities for rendering components.
@author Garret Wilson
*/
public class RenderUtilities
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
					continue;	//G***testing; fix correctly
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
		writer.writeAttribute(ELEMENT_SCRIPT_ATTRIBUTE_TYPE, ContentTypes.toString(ContentTypes.TEXT_PRIMARY_TYPE, JAVASCRIPT_SUBTYPE), ELEMENT_SCRIPT_ATTRIBUTE_TYPE);	//type="text/javascript"
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
/*G***del if not needed
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
