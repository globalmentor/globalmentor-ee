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

package com.globalmentor.faces.taglib;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.application.Application;
import javax.faces.component.UIComponent;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import javax.faces.event.ActionEvent;
import javax.faces.model.SelectItem;
import javax.faces.render.Renderer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

// Renderer for the UITabbedPane component

public class ForEachRenderer extends Renderer {

	// By default, getRendersChildren() returns false, so encodeChildren()
	// won't be invoked unless we override getRendersChildren() to return true

	public boolean getRendersChildren() {
		return true;
	}

	// The decode method gets the value of the request parameter whose name
	// is the client Id of the tabbedpane component. The request parameter 
	// is encoded as a hidden field by encodeHiddenField, which is called by 
	// encodeEnd. The value for the parameter is set by JavaScript generated 
	// by the encodeTab method. It is the name of a facet or a JSP page.

	// The decode method uses the request parameter value to set the
	// tabbedpane component's content attribute.
	// Finally, decode() queues an action event that's fired to registered
	// listeners in the Invoke Application phase of the JSF lifecycle. Action
	// listeners can be specified with the <corejsf:tabbedpane>'s actionListener
	// attribute or with <f:actionListener> tags in the body of the
	// <corejsf:tabbedpane> tag.
	/*TODO fix
	 public void decode(FacesContext context, UIComponent component) {
	    Map requestParams = context.getExternalContext().getRequestParameterMap();
	    String clientId = component.getClientId(context);
	
	    String content = (String) (requestParams.get(clientId));
	    component.getAttributes().put("content", content);
	    component.queueEvent(new ActionEvent(component));
	 }
	 */

	// The encodeBegin method writes the starting <table> HTML element
	// with the CSS class specified by the <corejsf:tabbedpane>'s styleClass
	// attribute (if supplied)
	/*TODO fix
	 public void encodeBegin(FacesContext context, UIComponent component)
	       throws java.io.IOException {
	    ResponseWriter writer = context.getResponseWriter();
	    writer.startElement("table", component);
	
	    String styleClass = (String) component.getAttributes().get("styleClass");
	    if (styleClass != null)
	       writer.writeAttribute("class", styleClass, null);
	
	    writer.write("\n"); // to make generated HTML easier to read
	 }
	 */

	// encodeChildren() is invoked by the JSF implementation after encodeBegin().
	// The children of the <corejsf:tabbedpane> component are UISelectItem
	// components, set with one or more <f:selectItem> tags or a single
	// <f:selectItems> tag in the body of <corejsf:tabbedpane>

	public void encodeChildren(FacesContext context, UIComponent component) throws java.io.IOException {
		if(component.getChildCount() > 0) {
			/*TODO del
			Debug.setDebug(true);
			Log.trace("items is a: ", component.getAttributes().get("items").getClass().getName());
			Log.trace("var is a: ", component.getAttributes().get("var").getClass().getName());
			*/

			//TODO del	   		String itemsString = (String) component.getAttributes().get("items");
			Collection items = (Collection)component.getAttributes().get("items");
			String varString = (String)component.getAttributes().get("var");

			Application app = context.getApplication();
			//TODO del   	final Object items=app.createValueBinding(itemsString).getValue(context);
			final ValueBinding varValueBinding = app.createValueBinding("#{" + varString + "}");
			for(Object object : items) {
				varValueBinding.setValue(context, object);
				Iterator children = component.getChildren().iterator();
				while(children.hasNext()) {
					UIComponent child = (UIComponent)children.next();
					if(child.isRendered()) {
						child.encodeBegin(context);
						if(child.getRendersChildren())
							child.encodeChildren(context);
						child.encodeEnd(context);
					}
				}
			}
		}

	}

	// encodeEnd() is invoked by the JSF implementation after encodeChildren().
	// encodeEnd() writes the table body and encodes the tabbedpane's content
	// in a single table row.

	// The content for the tabbed pane can be specified as either a URL for
	// a JSP page or a facet name, so encodeEnd() checks to see if it's a facet;
	// if so, it encodes it; if not, it includes the JSP page
	/*TODO fix
	 public void encodeEnd(FacesContext context, UIComponent component)
	       throws java.io.IOException {
	    ResponseWriter writer = context.getResponseWriter();
	    String content = (String) component.getAttributes().get("content");
	
	    writer.startElement("tbody", component);
	    writer.startElement("tr", component);
	    writer.startElement("td", component);
	
	    if (content != null) {
	       UIComponent facet = component.getFacet(content);
	       if (facet != null) {
	          if (facet.isRendered()) {
	             facet.encodeBegin(context);
	             if (facet.getRendersChildren())
	                facet.encodeChildren(context);
	             facet.encodeEnd(context);
	          }
	       } else
	          includePage(context, component);
	    }
	
	    writer.endElement("td");
	    writer.endElement("tr");
	    writer.endElement("tbody");
	
	    // Close off the column, row, and table elements
	    writer.endElement("table");
	
	    encodeHiddenField(context, writer, component);
	 }
	*/
}
